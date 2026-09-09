

import argparse
import os

import numpy as np

from bench_utils import plot_runtime_curve, time_best, timing_table_lines
from image_utils import load_image, make_kernel, save_comparison, save_image, save_kernel_preview
from io_utils import write_report
from transforms import DFTAnalyzer, FFTTransformer, ArbitraryLengthFFT, next_power_of_two


def transform_2d(plane, engine):
    """ 2D transform: every row, then every column."""
    x = np.asarray(plane, dtype=np.complex128)
    row_done = np.empty_like(x)
    for r in range(x.shape[0]):
        row_done[r, :] = engine.transform(x[r, :])
    out = np.empty_like(row_done)
    for c in range(x.shape[1]):
        out[:, c] = engine.transform(row_done[:, c])
    return out


def inverse_2d(spectrum, engine):
    """2D inverse transform."""
    x = np.asarray(spectrum, dtype=np.complex128)
    col_done = np.empty_like(x)
    for c in range(x.shape[1]):
        col_done[:, c] = engine.inverse(x[:, c])
    out = np.empty_like(col_done)
    for r in range(x.shape[0]):
        out[r, :] = engine.inverse(col_done[r, :])
    return out


def _transform_shape(h, w, kh, kw, engine):
    H, W = h + kh - 1, w + kw - 1
    if engine.name == "fft":
        H, W = next_power_of_two(H), next_power_of_two(W)
    return H, W


def convolve_plane(plane, kernel, engine, circular=False):
    """Convolve one 2D plane through the frequency domain."""
    plane = np.asarray(plane, dtype=np.float64)
    kernel = np.asarray(kernel, dtype=np.float64)
    h, w = plane.shape
    kh, kw = kernel.shape

    if circular:
        if engine.name == "fft" and ((h & (h - 1)) or (w & (w - 1))):
            raise ValueError("FFT circular convolution requires power-of-two image dimensions")
        H, W = h, w
        kp = np.zeros((H, W), dtype=np.float64)
        kp[:kh, :kw] = kernel
        # kernel centre at the origin for circular convolution.
        kp = np.roll(kp, -(kh // 2), axis=0)
        kp = np.roll(kp, -(kw // 2), axis=1)
        return inverse_2d(transform_2d(plane, engine) * transform_2d(kp, engine), engine).real

    H, W = _transform_shape(h, w, kh, kw, engine)
    pp = np.zeros((H, W), dtype=np.float64)
    kp = np.zeros((H, W), dtype=np.float64)
    pp[:h, :w] = plane
    kp[:kh, :kw] = kernel
    full = inverse_2d(transform_2d(pp, engine) * transform_2d(kp, engine), engine).real
    return full[kh // 2:kh // 2 + h, kw // 2:kw // 2 + w]


def convolve_image(image, kernel, engine, circular=False):
    """Convolve a grayscale image or each RGB channel independently."""
    image = np.asarray(image, dtype=np.float64)
    if image.ndim == 2:
        return convolve_plane(image, kernel, engine, circular)
    return np.stack([convolve_plane(image[:, :, c], kernel, engine, circular) for c in range(image.shape[2])], axis=2)


def convolve_plane_direct(plane, kernel):
    """ four-loop spatial convolution with zero padding."""
    plane = np.asarray(plane, dtype=np.float64)
    kernel = np.asarray(kernel, dtype=np.float64)
    h, w = plane.shape
    kh, kw = kernel.shape
    cr, cc = kh // 2, kw // 2
    out = np.zeros_like(plane, dtype=np.float64)
    for r in range(h):
        for c in range(w):
            total = 0.0
            for i in range(kh):
                rr = r + cr - i
                if rr < 0 or rr >= h:
                    continue
                for j in range(kw):
                    cc2 = c + cc - j
                    if 0 <= cc2 < w:
                        total += plane[rr, cc2] * kernel[i, j]
            out[r, c] = total
    return out


def _make_requested_kernel(name, param):
    if name == "bokeh":
        return make_kernel("bokeh", radius=param)
    if name == "gaussian":
        return make_kernel("gaussian", size=param)
    if name == "box":
        return make_kernel("box", size=param)
    if name == "motion":
        return make_kernel("motion", length=param, angle=30.0)
    raise ValueError("unknown kernel: %r" % name)


def run_single(path, kernel_name, param, engine_name, out_dir, gray=False):
    """Blurring one image, save all required figures, and verify against direct convolution."""
    os.makedirs(out_dir, exist_ok=True)
    image = load_image(path, as_gray=gray)
    kernel = _make_requested_kernel(kernel_name, param)
    engine = {"dft": DFTAnalyzer, "fft": FFTTransformer, "arbitrary": ArbitraryLengthFFT}[engine_name]()

    blurred = convolve_image(image, kernel, engine, circular=False)
    wraparound = convolve_image(image, kernel, engine, circular=True)
    save_image(blurred, os.path.join(out_dir, "blurred.png"))
    save_image(wraparound, os.path.join(out_dir, "wraparound.png"))
    save_kernel_preview(kernel, os.path.join(out_dir, "kernel.png"))
    save_comparison([image, blurred, wraparound], ["original", "blurred", "wraparound"], os.path.join(out_dir, "comparison.png"))

    plane = image if image.ndim == 2 else image[:, :, 0]
    crop = plane[:64, :64]
    spectral = convolve_plane(crop, kernel, engine, circular=False)
    direct = convolve_plane_direct(crop, kernel)
    err = float(np.max(np.abs(spectral - direct)))
    H, W = _transform_shape(plane.shape[0], plane.shape[1], kernel.shape[0], kernel.shape[1], engine)
    write_report(os.path.join(out_dir, "report.txt"), [
        "Task B -- 2D convolution through the frequency domain",
        "image               : %s  (%d x %d, %s)" % (path, plane.shape[0], plane.shape[1], "gray" if image.ndim == 2 else "RGB"),
        "kernel              : %s  (%d x %d)" % (kernel_name, kernel.shape[0], kernel.shape[1]),
        "engine              : %s" % engine_name,
        "linear-conv size    : %d x %d" % (plane.shape[0] + kernel.shape[0] - 1, plane.shape[1] + kernel.shape[1] - 1),
        "transform size      : %d x %d" % (H, W),
        "max |spectral - direct| on 64x64 crop : %.3e" % err,
        "verification        : %s" % ("MATCH" if err <= 1e-9 else "MISMATCH"),
    ])
    if err > 1e-9:
        raise AssertionError("spectral/direct verification failed")


IMAGE_SIZES = [16, 32, 64, 128, 256, 512]
KERNEL_RADII = [1, 3, 7, 15, 31]
BENCH_RADIUS = 7
BENCH_SIZE = 256
TIME_BUDGET = 8.0


def run_benchmark(path, out_dir):
    """Run the two required Task B runtime studies."""
    full = load_image(path, as_gray=True)

    def sweep(make_call, points):
        xs, ys = [], []
        for x, arg in points:
            seconds = time_best(make_call(arg), repeats=1)
            xs.append(x)
            ys.append(seconds)
            if seconds > TIME_BUDGET:
                break
        return xs, ys

    kernel = make_kernel("bokeh", radius=BENCH_RADIUS)
    crops = [(n, full[:n, :n].copy()) for n in IMAGE_SIZES]
    size_series = {
        "Naive DFT (row-column)": sweep(lambda img: lambda: convolve_plane(img, kernel, DFTAnalyzer()), crops),
        "Radix-2 FFT (row-column)": sweep(lambda img: lambda: convolve_plane(img, kernel, FFTTransformer()), crops),
        "Direct spatial convolution": sweep(lambda img: lambda: convolve_plane_direct(img, kernel), crops),
    }
    size_plot = os.path.join(out_dir, "runtime_vs_image_size.png")
    plot_runtime_curve(size_series, size_plot, title="Task B: %d x %d blur of an N x N image" % kernel.shape, xlabel="image side length N (pixels)", references=("n3", "n2"))

    image = full[:BENCH_SIZE, :BENCH_SIZE].copy()
    kernels = [(make_kernel("bokeh", radius=r).shape[0], make_kernel("bokeh", radius=r)) for r in KERNEL_RADII]
    kernel_series = {
        "Direct spatial convolution": sweep(lambda k: lambda: convolve_plane_direct(image, k), kernels),
        "Radix-2 FFT (row-column)": sweep(lambda k: lambda: convolve_plane(image, k, FFTTransformer()), kernels),
    }
    kernel_plot = os.path.join(out_dir, "runtime_vs_kernel_size.png")
    plot_runtime_curve(kernel_series, kernel_plot, title="Task B: %d x %d image, growing kernel" % image.shape, xlabel="kernel side length K (pixels)", references=("n2",))
    os.makedirs(out_dir, exist_ok=True)
    write_report(os.path.join(out_dir, "report.txt"), [
        "Task B -- runtime benchmark", "",
        "Study 1: fixed %d x %d kernel, growing image" % kernel.shape, "",
    ] + timing_table_lines(size_series, size_label="N") + [
        "", "plot: runtime_vs_image_size.png", "",
        "Study 2: fixed %d x %d image, growing kernel" % image.shape, "",
    ] + timing_table_lines(kernel_series, size_label="K") + ["", "plot: runtime_vs_kernel_size.png"])


def main():
    ap = argparse.ArgumentParser(description="2D convolution by DFT/FFT")
    ap.add_argument("image")
    ap.add_argument("--kernel", default="bokeh", choices=["bokeh", "gaussian", "box", "motion"])
    ap.add_argument("--param", type=float, default=9)
    ap.add_argument("--engine", default="fft", choices=["dft", "fft", "arbitrary"])
    ap.add_argument("--gray", action="store_true")
    ap.add_argument("--out-dir", default="outputs")
    ap.add_argument("--benchmark", action="store_true")
    args = ap.parse_args()
    os.makedirs(args.out_dir, exist_ok=True)
    if args.benchmark:
        run_benchmark(args.image, args.out_dir)
    else:
        run_single(args.image, args.kernel, args.param, args.engine, args.out_dir, gray=args.gray)


if __name__ == "__main__":
    main()
