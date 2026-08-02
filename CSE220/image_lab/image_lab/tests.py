"""
Correctness tests for the DSP core.

Run with:  python manage.py test image_lab

The convolution test is the important one: it compares the vectorised
implementation against a literal, deliberately slow transcription of the
convolution sum, so the fast version is checked against the definition itself
rather than against another library.
"""

import numpy as np
from django.test import SimpleTestCase

from . import dsp_utils as dsp


# Evaluate the 2D convolution sum directly from its definition, with zero padding.
def naive_convolve2d(f, h):
    kh, kw = h.shape
    ay, ax = kh // 2, kw // 2
    height, width = f.shape
    out = np.zeros_like(f)
    for m in range(height):
        for n in range(width):
            total = 0.0
            for u in range(kh):
                for v in range(kw):
                    i, j = m - u + ay, n - v + ax
                    if 0 <= i < height and 0 <= j < width:
                        total += h[u, v] * f[i, j]
            out[m, n] = total
    return out


class ConvolutionTests(SimpleTestCase):
    def setUp(self):
        self.rng = np.random.default_rng(20250802)

    def test_matches_the_convolution_sum(self):
        f = self.rng.random((9, 11))
        h = self.rng.random((3, 5))
        np.testing.assert_allclose(dsp.convolve2d(f, h, "zero"), naive_convolve2d(f, h), atol=1e-12)

    def test_convolution_is_not_correlation(self):
        # An asymmetric kernel must give a different answer than correlation,
        # which is what proves the kernel flip is actually happening.
        f = self.rng.random((8, 8))
        h = np.array([[1.0, 2.0, 3.0]])
        self.assertFalse(np.allclose(dsp.convolve2d(f, h, "zero"), dsp.convolve2d(f, h[:, ::-1], "zero")))

    def test_identity_kernel_is_a_no_op(self):
        f = self.rng.random((7, 9))
        identity = np.array(dsp.KERNEL_PRESETS["identity"], dtype=float)
        np.testing.assert_allclose(dsp.convolve2d(f, identity), f, atol=1e-12)

    def test_separable_matches_full_2d(self):
        f = self.rng.random((16, 20))
        k = dsp.gaussian_kernel1d(1.7)
        np.testing.assert_allclose(
            dsp.convolve_separable(f, k, k), dsp.convolve2d(f, np.outer(k, k)), atol=1e-12
        )

    def test_colour_is_filtered_per_channel(self):
        f = self.rng.random((6, 6, 3))
        h = dsp.box_kernel(3)
        out = dsp.convolve2d(f, h)
        self.assertEqual(out.shape, f.shape)
        for c in range(3):
            np.testing.assert_allclose(out[..., c], dsp.convolve2d(f[..., c], h), atol=1e-12)

    def test_even_sized_kernel_preserves_shape(self):
        f = self.rng.random((10, 10))
        self.assertEqual(dsp.convolve2d(f, np.ones((2, 4)) / 8).shape, f.shape)

    def test_blur_reduces_variance_and_keeps_brightness(self):
        f = self.rng.random((32, 32))
        blurred = dsp.convolve2d(f, dsp.box_kernel(5))
        self.assertLess(blurred.var(), f.var())
        # A unity-DC-gain kernel must not shift the mean brightness.
        self.assertAlmostEqual(blurred.mean(), f.mean(), places=2)

    def test_gaussian_kernel_is_normalised(self):
        self.assertAlmostEqual(dsp.gaussian_kernel2d(2.0).sum(), 1.0, places=12)

    def test_zero_sum_kernel_is_left_alone_by_normalise(self):
        laplacian = np.array(dsp.KERNEL_PRESETS["laplacian"], dtype=float)
        np.testing.assert_allclose(dsp.normalize_kernel(laplacian), laplacian)


class ResampleTests(SimpleTestCase):
    def setUp(self):
        self.rng = np.random.default_rng(7)

    def test_resize_to_same_size_is_identity(self):
        f = self.rng.random((8, 12))
        np.testing.assert_allclose(dsp.resize(f, 8, 12, "bilinear", antialias=False), f, atol=1e-12)

    def test_bilinear_upsample_does_not_shift_the_image(self):
        # The half-pixel-centre convention must keep the mean in place; the
        # naive s = d * (in/out) mapping fails this by half a pixel.
        ramp = np.tile(np.linspace(0.0, 1.0, 16), (4, 1))
        up = dsp.resize_bilinear(ramp, 4, 32)
        self.assertAlmostEqual(up.mean(), ramp.mean(), places=2)

    def test_nearest_preserves_exact_sample_values(self):
        f = self.rng.random((5, 5))
        out = dsp.resize_nearest(f, 10, 10)
        self.assertTrue(np.isin(out, f).all())

    def test_upsampling_needs_no_prefilter(self):
        self.assertEqual(dsp.antialias_sigma(100, 200), 0.0)
        self.assertEqual(dsp.antialias_sigma(100, 100), 0.0)

    def test_sigma_grows_with_decimation(self):
        self.assertAlmostEqual(dsp.antialias_sigma(100, 50), 0.5)
        self.assertAlmostEqual(dsp.antialias_sigma(100, 25), 1.5)

    def test_antialiasing_suppresses_alias_energy(self):
        # A fine grating well above the post-decimation Nyquist limit. Without a
        # prefilter it folds down into a strong low-frequency moire; with one it
        # is attenuated before sampling and the result is nearly flat.
        n = np.arange(256)
        grating = np.tile(0.5 + 0.5 * np.cos(np.pi * 0.9 * n), (256, 1))

        without_aa = dsp.resize(grating, 256, 32, "bilinear", antialias=False)
        with_aa = dsp.resize(grating, 256, 32, "bilinear", antialias=True)

        self.assertLess(with_aa.var(), without_aa.var() / 5.0)


class NoiseAndMetricTests(SimpleTestCase):
    def setUp(self):
        self.rng = np.random.default_rng(11)

    def test_metrics_are_zero_for_identical_images(self):
        a = self.rng.random((16, 16))
        self.assertEqual(dsp.mse(a, a), 0.0)
        self.assertEqual(dsp.psnr(a, a), float("inf"))
        self.assertAlmostEqual(dsp.ssim(a, a), 1.0, places=6)

    def test_psnr_matches_the_closed_form(self):
        a = self.rng.random((16, 16))
        # A constant error of 0.1 gives MSE = 0.01 and PSNR = 10*log10(1/0.01).
        self.assertAlmostEqual(dsp.psnr(a, a + 0.1), 20.0, places=6)

    def test_salt_pepper_only_writes_extremes(self):
        base = np.full((64, 64), 0.5)
        noisy = dsp.add_salt_pepper(base, amount=0.2, seed=3)
        changed = noisy[noisy != 0.5]
        self.assertTrue(np.isin(changed, [0.0, 1.0]).all())

    def test_noise_is_reproducible_for_a_fixed_seed(self):
        base = self.rng.random((32, 32))
        np.testing.assert_array_equal(
            dsp.add_gaussian_noise(base, 0.1, seed=42),
            dsp.add_gaussian_noise(base, 0.1, seed=42),
        )

    def test_median_beats_mean_on_impulse_noise(self):
        # The headline result of the noise module: an order-statistic filter
        # rejects impulses outright, a linear one can only smear them.
        base = np.tile(np.linspace(0.2, 0.8, 64), (64, 1))
        noisy = dsp.add_salt_pepper(base, amount=0.08, seed=5)

        median_psnr = dsp.psnr(base, dsp.median_filter2d(noisy, 3))
        mean_psnr = dsp.psnr(base, dsp.mean_filter2d(noisy, 3))
        self.assertGreater(median_psnr, mean_psnr)

    def test_mean_filter_equals_box_convolution(self):
        f = self.rng.random((20, 20))
        np.testing.assert_allclose(dsp.mean_filter2d(f, 3), dsp.convolve2d(f, dsp.box_kernel(3)), atol=1e-12)


class OperationRegistryTests(SimpleTestCase):
    def test_every_operation_returns_panels_and_metrics(self):
        from .operations import OPERATIONS

        image = np.random.default_rng(1).random((24, 24, 3))
        defaults = {
            "convolve": {"kernel": dsp.KERNEL_PRESETS["sharpen"]},
            "resample": {"scale": 0.25},
            "noise": {"noise_model": "salt_pepper", "clean_filter": "median"},
        }

        for op_id, operation in OPERATIONS.items():
            with self.subTest(op=op_id):
                result = operation.handler(image, defaults.get(op_id, {}))
                self.assertGreater(len(result.panels), 0)
                self.assertGreater(len(result.metrics), 0)
                for panel in result.panels:
                    self.assertEqual(panel.image.ndim, 3)
                    self.assertTrue(np.isfinite(panel.image).all())

    def test_bad_kernel_is_rejected(self):
        from .operations import op_convolve

        image = np.zeros((8, 8))
        with self.assertRaises(ValueError):
            op_convolve(image, {"kernel": [[1, 2], [3]]})       # ragged rows
        with self.assertRaises(ValueError):
            op_convolve(image, {"kernel": []})                   # empty
