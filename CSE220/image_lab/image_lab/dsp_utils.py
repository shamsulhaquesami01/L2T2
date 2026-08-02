"""
dsp_utils.py

Core DSP routines for the Image Lab, written with NumPy only.

This module deliberately contains NO Django and NO Pillow imports. It is pure
math on NumPy arrays, so it can be unit-tested and reused from a notebook.

Conventions used everywhere in this file:

  * An image is a float64 array with values in [0.0, 1.0].
  * Grayscale images have shape (H, W); colour images have shape (H, W, C).
  * Row index is `y` / `m` (vertical), column index is `x` / `n` (horizontal).
  * Every 2D routine is written for a single channel and lifted to colour by
    `_per_channel`, so the maths below only ever has to consider one plane.
"""

from __future__ import annotations

import numpy as np
from numpy.lib.stride_tricks import sliding_window_view

# Padding modes we expose in the UI, mapped to the equivalent np.pad mode.
PAD_MODES = {
    "reflect": "reflect",      # abc -> cba|abc|cba   (no edge duplication)
    "edge": "edge",            # abc -> aaa|abc|ccc   (clamp / replicate)
    "wrap": "wrap",            # abc -> abc|abc|abc   (circular convolution)
    "zero": "constant",        # abc -> 000|abc|000   (linear convolution)
}


# ---------------------------------------------------------------------------
# Small helpers
# ---------------------------------------------------------------------------


# Return the image as float64 in [0, 1], accepting uint8 or float input.
def to_float(image):
    array = np.asarray(image)
    if array.dtype == np.uint8:
        return array.astype(np.float64) / 255.0
    return np.clip(array.astype(np.float64), 0.0, 1.0)


# Return the image as uint8 in [0, 255], clipping out-of-range values.
def to_uint8(image):
    return np.clip(np.asarray(image) * 255.0, 0.0, 255.0).round().astype(np.uint8)


# Apply a single-plane function to every colour channel and restack the result.
def _per_channel(func, image, *args, **kwargs):
    image = np.asarray(image)
    if image.ndim == 2:
        return func(image, *args, **kwargs)
    planes = [func(image[..., c], *args, **kwargs) for c in range(image.shape[-1])]
    return np.stack(planes, axis=-1)


# Return the luminance plane of an image using the ITU-R BT.601 weights.
def to_gray(image):
    image = np.asarray(image)
    if image.ndim == 2:
        return image
    return image[..., :3] @ np.array([0.299, 0.587, 0.114])


# ---------------------------------------------------------------------------
# 2D convolution
# ---------------------------------------------------------------------------
#
# The 2D convolution of an image f with a kernel h is defined as
#
#     g[m, n] = sum_i sum_j  f[i, j] * h[m - i, n - j]
#
# Substituting u = m - i and v = n - j turns this into the form we can actually
# evaluate with array slicing:
#
#     g[m, n] = sum_u sum_v  h[u, v] * f[m - u, n - v]
#
# The minus signs are what separate *convolution* from *correlation*. Reversing
# the kernel along both axes once, up front, converts the expression into a
# plain correlation, which is what the sliding-window dot product computes:
#
#     h_flipped[u, v] = h[kh - 1 - u, kw - 1 - v]
#     g[m, n] = sum_u sum_v  h_flipped[u, v] * f[m + u - ay, n + v - ax]
#
# where (ay, ax) is the anchor offset that keeps the output aligned with the
# input, i.e. the position of the kernel centre.
#
# This flip matters: for a symmetric kernel (box, Gaussian) convolution and
# correlation agree, but for an asymmetric kernel (Sobel, first difference,
# emboss) they differ by a 180-degree rotation of the response.


# Return the (top, bottom, left, right) pad widths that keep the output size.
def _anchor_padding(kh, kw):
    top = kh // 2
    left = kw // 2
    return top, kh - 1 - top, left, kw - 1 - left


# Convolve one 2D plane with a 2D kernel, preserving the input shape.
def _convolve2d_plane(plane, kernel, pad_mode="reflect"):
    kh, kw = kernel.shape
    top, bottom, left, right = _anchor_padding(kh, kw)

    # Reverse the kernel on both axes so the correlation below equals convolution.
    flipped = kernel[::-1, ::-1]

    np_mode = PAD_MODES.get(pad_mode, "reflect")
    if np_mode == "constant":
        padded = np.pad(plane, ((top, bottom), (left, right)), mode="constant", constant_values=0.0)
    else:
        padded = np.pad(plane, ((top, bottom), (left, right)), mode=np_mode)

    # sliding_window_view is a stride trick: it produces an (H, W, kh, kw) view
    # of every kh-by-kw neighbourhood without copying the underlying data.
    windows = sliding_window_view(padded, (kh, kw))

    # Contract each window against the flipped kernel -> one output sample.
    return np.einsum("mnuv,uv->mn", windows, flipped, optimize=True)


# Convolve an image (grayscale or colour) with a 2D kernel.
def convolve2d(image, kernel, pad_mode="reflect"):
    kernel = np.asarray(kernel, dtype=np.float64)
    if kernel.ndim != 2:
        raise ValueError("Kernel must be a 2D array")
    if kernel.size == 0:
        raise ValueError("Kernel must not be empty")
    return _per_channel(_convolve2d_plane, np.asarray(image, dtype=np.float64), kernel, pad_mode)


# Convolve one 2D plane with a separable kernel given as two 1D factors.
def _convolve_separable_plane(plane, kx, ky, pad_mode="reflect"):
    # A separable kernel satisfies h[u, v] = ky[u] * kx[v], so the 2D sum
    # factorises into a pass along x followed by a pass along y. Cost drops
    # from O(kh * kw) to O(kh + kw) multiplies per output pixel.
    out = plane
    if kx.size > 1 or not np.isclose(kx[0], 1.0):
        out = _convolve2d_plane(out, kx.reshape(1, -1), pad_mode)
    if ky.size > 1 or not np.isclose(ky[0], 1.0):
        out = _convolve2d_plane(out, ky.reshape(-1, 1), pad_mode)
    return out


# Convolve an image with a separable kernel defined by 1D factors kx and ky.
def convolve_separable(image, kx, ky, pad_mode="reflect"):
    kx = np.asarray(kx, dtype=np.float64).ravel()
    ky = np.asarray(ky, dtype=np.float64).ravel()
    return _per_channel(_convolve_separable_plane, np.asarray(image, dtype=np.float64), kx, ky, pad_mode)


# ---------------------------------------------------------------------------
# Kernel construction
# ---------------------------------------------------------------------------


# Return a 1D Gaussian kernel sampled at integer offsets and normalised to sum 1.
def gaussian_kernel1d(sigma, radius=None):
    if sigma <= 0:
        return np.array([1.0])
    if radius is None:
        # 3 sigma captures ~99.7% of the Gaussian's mass; truncating there keeps
        # the kernel small without a visible error.
        radius = max(1, int(np.ceil(3.0 * sigma)))
    x = np.arange(-radius, radius + 1, dtype=np.float64)
    k = np.exp(-(x ** 2) / (2.0 * sigma ** 2))
    return k / k.sum()


# Return a square 2D Gaussian kernel built as the outer product of 1D factors.
def gaussian_kernel2d(sigma, size=None):
    radius = None if size is None else max(1, int(size) // 2)
    k1d = gaussian_kernel1d(sigma, radius)
    return np.outer(k1d, k1d)


# Return an n-by-n box (moving average) kernel.
def box_kernel(n):
    n = max(1, int(n))
    return np.full((n, n), 1.0 / (n * n), dtype=np.float64)


# Scale a kernel so its coefficients sum to 1, leaving zero-sum kernels alone.
def normalize_kernel(kernel):
    kernel = np.asarray(kernel, dtype=np.float64)
    total = kernel.sum()
    # Edge-detector kernels legitimately sum to 0 and must not be rescaled.
    if np.isclose(total, 0.0):
        return kernel
    return kernel / total


# Named 3x3 kernels offered as starting points in the kernel editor.
KERNEL_PRESETS = {
    "identity": [[0, 0, 0], [0, 1, 0], [0, 0, 0]],
    "box_blur_3": [[1 / 9, 1 / 9, 1 / 9], [1 / 9, 1 / 9, 1 / 9], [1 / 9, 1 / 9, 1 / 9]],
    "gaussian_3": [[1 / 16, 2 / 16, 1 / 16], [2 / 16, 4 / 16, 2 / 16], [1 / 16, 2 / 16, 1 / 16]],
    "sharpen": [[0, -1, 0], [-1, 5, -1], [0, -1, 0]],
    "sharpen_strong": [[-1, -1, -1], [-1, 9, -1], [-1, -1, -1]],
    "laplacian": [[0, 1, 0], [1, -4, 1], [0, 1, 0]],
    "sobel_x": [[-1, 0, 1], [-2, 0, 2], [-1, 0, 1]],
    "sobel_y": [[-1, -2, -1], [0, 0, 0], [1, 2, 1]],
    "emboss": [[-2, -1, 0], [-1, 1, 1], [0, 1, 2]],
}


# Return the unsharp-mask result: original + amount * (original - blurred).
def unsharp_mask(image, sigma=1.0, amount=1.0, pad_mode="reflect"):
    # Sharpening is a high-pass operation. The cheapest high-pass is
    # "signal minus its own low-pass", and adding a multiple of that detail
    # layer back onto the original boosts high spatial frequencies.
    k1d = gaussian_kernel1d(sigma)
    blurred = convolve_separable(image, k1d, k1d, pad_mode)
    detail = np.asarray(image, dtype=np.float64) - blurred
    return np.asarray(image, dtype=np.float64) + amount * detail


# ---------------------------------------------------------------------------
# Resampling: nearest neighbour, bilinear, and anti-aliased downsampling
# ---------------------------------------------------------------------------
#
# All resampling here uses the *half-pixel centre* convention. Pixel k of an
# N-wide row is treated as covering [k, k+1) with its centre at k + 0.5. To find
# where output pixel `d` lands in the input we match normalised centres:
#
#     (d + 0.5) / out = (s + 0.5) / in
#     s = (d + 0.5) * (in / out) - 0.5
#
# This is the convention that keeps an image geometrically centred after a
# resize. The naive alternative, s = d * (in / out), shifts the picture by half
# a pixel and is a classic source of "my resized image drifted" bugs.


# Return the source coordinates that each output sample reads from.
def _sample_coords(in_size, out_size):
    return (np.arange(out_size, dtype=np.float64) + 0.5) * (in_size / out_size) - 0.5


# Resize an image by picking the single closest input pixel for each output pixel.
def resize_nearest(image, out_h, out_w):
    image = np.asarray(image, dtype=np.float64)
    in_h, in_w = image.shape[:2]

    ys = np.clip(np.rint(_sample_coords(in_h, out_h)).astype(np.int64), 0, in_h - 1)
    xs = np.clip(np.rint(_sample_coords(in_w, out_w)).astype(np.int64), 0, in_w - 1)

    # Advanced indexing broadcasts to (out_h, out_w) and carries channels along.
    return image[ys[:, None], xs[None, :]]


# Resize an image by bilinear interpolation of the four surrounding pixels.
def resize_bilinear(image, out_h, out_w):
    image = np.asarray(image, dtype=np.float64)
    in_h, in_w = image.shape[:2]

    y = _sample_coords(in_h, out_h)
    x = _sample_coords(in_w, out_w)

    y0 = np.floor(y).astype(np.int64)
    x0 = np.floor(x).astype(np.int64)
    y1 = y0 + 1
    x1 = x0 + 1

    # Fractional distance from the top-left neighbour, i.e. the blend weights.
    wy = (y - y0).reshape(-1, 1)
    wx = (x - x0).reshape(1, -1)

    # Clamp *after* computing the weights so edge pixels extend rather than wrap.
    y0c, y1c = np.clip(y0, 0, in_h - 1), np.clip(y1, 0, in_h - 1)
    x0c, x1c = np.clip(x0, 0, in_w - 1), np.clip(x1, 0, in_w - 1)

    if image.ndim == 3:
        wy = wy[..., None]
        wx = wx[..., None]

    top_left = image[y0c[:, None], x0c[None, :]]
    top_right = image[y0c[:, None], x1c[None, :]]
    bottom_left = image[y1c[:, None], x0c[None, :]]
    bottom_right = image[y1c[:, None], x1c[None, :]]

    # Interpolate along x first, then blend the two rows along y.
    top = top_left * (1.0 - wx) + top_right * wx
    bottom = bottom_left * (1.0 - wx) + bottom_right * wx
    return top * (1.0 - wy) + bottom * wy


# Return the Gaussian sigma needed to band-limit before a given downsample.
def antialias_sigma(in_size, out_size):
    """
    Decimation factor D = in_size / out_size.

    Keeping every D-th sample moves the Nyquist limit from pi down to pi / D
    (in normalised radians per sample of the ORIGINAL grid). Any energy above
    pi / D does not simply vanish -- it folds back into the baseband and
    reappears as a false low-frequency pattern. That fold-back is aliasing, and
    it is why a downscaled brick wall or striped shirt turns into moire.

    The fix required by the sampling theorem is to low-pass filter *before*
    discarding samples, so nothing above the new Nyquist limit survives to fold.
    A Gaussian is the usual practical choice (strictly positive, separable, no
    ringing). Matching its effective width to the decimation factor gives

        sigma = (D - 1) / 2

    which is 0 when D <= 1 (upsampling needs no prefilter, there is no new
    Nyquist limit to respect) and grows linearly as the reduction gets harsher.
    """
    decimation = in_size / float(out_size)
    if decimation <= 1.0:
        return 0.0
    return (decimation - 1.0) / 2.0


# Resize an image, optionally low-pass filtering first to suppress aliasing.
def resize(image, out_h, out_w, method="bilinear", antialias=True, pad_mode="reflect"):
    image = np.asarray(image, dtype=np.float64)
    in_h, in_w = image.shape[:2]

    work = image
    if antialias:
        sigma_y = antialias_sigma(in_h, out_h)
        sigma_x = antialias_sigma(in_w, out_w)
        if sigma_y > 0.0 or sigma_x > 0.0:
            # Separate sigmas so non-uniform scaling is filtered correctly on
            # each axis. A zero sigma yields the length-1 identity kernel.
            work = convolve_separable(
                work,
                gaussian_kernel1d(sigma_x),
                gaussian_kernel1d(sigma_y),
                pad_mode,
            )

    if method == "nearest":
        return resize_nearest(work, out_h, out_w)
    return resize_bilinear(work, out_h, out_w)


# ---------------------------------------------------------------------------
# Noise models and cleaning filters
# ---------------------------------------------------------------------------


# Add zero-mean additive white Gaussian noise of the given standard deviation.
def add_gaussian_noise(image, sigma=0.05, seed=None):
    image = np.asarray(image, dtype=np.float64)
    rng = np.random.default_rng(seed)
    return np.clip(image + rng.normal(0.0, sigma, image.shape), 0.0, 1.0)


# Replace a fraction of pixels with pure white or pure black impulses.
def add_salt_pepper(image, amount=0.05, salt_ratio=0.5, seed=None):
    image = np.asarray(image, dtype=np.float64)
    rng = np.random.default_rng(seed)
    out = image.copy()

    # Corrupt whole pixels (all channels together), which is how impulse noise
    # actually behaves on a sensor -- not each channel independently.
    mask_shape = image.shape[:2]
    draw = rng.random(mask_shape)
    salt = draw < (amount * salt_ratio)
    pepper = (draw >= (amount * salt_ratio)) & (draw < amount)

    out[salt] = 1.0
    out[pepper] = 0.0
    return out


# Replace each pixel with the median of its neighbourhood.
def _median_plane(plane, size, pad_mode="reflect"):
    size = max(1, int(size))
    if size == 1:
        return plane
    radius = size // 2
    np_mode = PAD_MODES.get(pad_mode, "reflect")
    if np_mode == "constant":
        padded = np.pad(plane, radius, mode="constant", constant_values=0.0)
    else:
        padded = np.pad(plane, radius, mode=np_mode)
    windows = sliding_window_view(padded, (size, size))
    return np.median(windows, axis=(-2, -1))


# Apply a median filter, the standard non-linear cure for impulse noise.
def median_filter2d(image, size=3, pad_mode="reflect"):
    # The median is order-statistic based, not a convolution: an isolated
    # salt/pepper spike is an extreme value and gets ranked out entirely,
    # whereas any linear filter would smear it across the neighbourhood.
    return _per_channel(_median_plane, np.asarray(image, dtype=np.float64), size, pad_mode)


# Apply an n-by-n moving-average filter.
def mean_filter2d(image, size=3, pad_mode="reflect"):
    n = max(1, int(size))
    ones = np.ones(n, dtype=np.float64) / n
    return convolve_separable(image, ones, ones, pad_mode)


# Apply a Gaussian low-pass filter of the given sigma.
def gaussian_blur(image, sigma=1.0, pad_mode="reflect"):
    k1d = gaussian_kernel1d(sigma)
    return convolve_separable(image, k1d, k1d, pad_mode)


# ---------------------------------------------------------------------------
# Error measures
# ---------------------------------------------------------------------------


# Return the mean squared error between two equally shaped images.
def mse(a, b):
    a = np.asarray(a, dtype=np.float64)
    b = np.asarray(b, dtype=np.float64)
    return float(np.mean((a - b) ** 2))


# Return the mean absolute error between two equally shaped images.
def mae(a, b):
    a = np.asarray(a, dtype=np.float64)
    b = np.asarray(b, dtype=np.float64)
    return float(np.mean(np.abs(a - b)))


# Return the peak signal-to-noise ratio in dB, assuming a peak value of 1.0.
def psnr(a, b, peak=1.0):
    error = mse(a, b)
    if error <= 1e-20:
        return float("inf")
    return float(10.0 * np.log10((peak ** 2) / error))


# Return the mean structural similarity index between two images.
def ssim(a, b, sigma=1.5, peak=1.0):
    # Gaussian-weighted local statistics, following Wang et al. (2004).
    a = to_gray(np.asarray(a, dtype=np.float64))
    b = to_gray(np.asarray(b, dtype=np.float64))

    c1 = (0.01 * peak) ** 2
    c2 = (0.03 * peak) ** 2
    k1d = gaussian_kernel1d(sigma)

    def blur(plane):
        return convolve_separable(plane, k1d, k1d, "reflect")

    mu_a, mu_b = blur(a), blur(b)
    # var(X) = E[X^2] - E[X]^2, evaluated under the same Gaussian window.
    var_a = blur(a * a) - mu_a ** 2
    var_b = blur(b * b) - mu_b ** 2
    cov = blur(a * b) - mu_a * mu_b

    numerator = (2 * mu_a * mu_b + c1) * (2 * cov + c2)
    denominator = (mu_a ** 2 + mu_b ** 2 + c1) * (var_a + var_b + c2)
    return float(np.mean(numerator / denominator))


# Return a contrast-stretched absolute difference map for visual comparison.
def difference_map(a, b, gain=1.0):
    diff = np.abs(np.asarray(a, dtype=np.float64) - np.asarray(b, dtype=np.float64))
    if gain == "auto":
        peak = diff.max()
        gain = 1.0 if peak <= 1e-12 else 1.0 / peak
    return np.clip(diff * float(gain), 0.0, 1.0)
