"""Transform core for Task A and Task B."""

import numpy as np


def next_power_of_two(n):
    """Return the smallest power of two >= n, with minimum 1."""
    if n < 1:
        return 1
    p = 1
    while p < n:
        p <<= 1
    return p


class DFTAnalyzer:
    """Direct O(N^2) DFT and inverse DFT."""

    name = "dft"

    def transform(self, x):
        """Compute X[k] = sum_n x[n] exp(-2j*pi*k*n/N)."""
        x = np.asarray(x, dtype=np.complex128).reshape(-1)
        N = x.size
        if N == 0:
            return np.empty(0, dtype=np.complex128)
        n = np.arange(N, dtype=np.float64)
        W = np.exp(-2j * np.pi * n[:, None] * n[None, :] / N)
        return W @ x

    def inverse(self, spectrum):
        """Compute the inverse DFT, including the 1/N factor."""
        X = np.asarray(spectrum, dtype=np.complex128).reshape(-1)
        N = X.size
        if N == 0:
            return np.empty(0, dtype=np.complex128)
        k = np.arange(N, dtype=np.float64)
        W = np.exp(2j * np.pi * k[:, None] * k[None, :] / N)
        return (W @ X) / N


class FFTTransformer(DFTAnalyzer):
    """Radix-2 decimation-in-time Cooley-Tukey FFT."""

    name = "fft"

    @staticmethod
    def _core(x, inverse=False):
        a = np.asarray(x, dtype=np.complex128).reshape(-1).copy()
        N = a.size
        if N == 0:
            return a
        if N & (N - 1):
            raise ValueError("radix-2 FFT requires a power-of-two length")

        # Bit-reversal permutation.
        j = 0
        for i in range(1, N):
            bit = N >> 1
            while j & bit:
                j ^= bit
                bit >>= 1
            j ^= bit
            if i < j:
                a[i], a[j] = a[j], a[i]

        # One twiddle table is computed for each stage, then reused by all
        # butterflies in that stage.
        m = 2
        sign = 1.0 if inverse else -1.0
        while m <= N:
            half = m // 2
            tw = np.exp(sign * 2j * np.pi * np.arange(half) / m)
            for start in range(0, N, m):
                u = a[start:start + half].copy()
                v = tw * a[start + half:start + m]
                a[start:start + half] = u + v
                a[start + half:start + m] = u - v
            m <<= 1

        if inverse:
            a /= N
        return a

    def transform(self, x):
        return self._core(x, inverse=False)

    def inverse(self, spectrum):
        return self._core(spectrum, inverse=True)


class ArbitraryLengthFFT(FFTTransformer):
    """Bonus arbitrary-length O(N log N) FFT using Bluestein's algorithm."""

    name = "arbitrary"

    def transform(self, x):
        x = np.asarray(x, dtype=np.complex128).reshape(-1)
        N = x.size
        if N == 0:
            return x.copy()
        if N & (N - 1) == 0:
            return super().transform(x)

        # Bluestein converts the N-point DFT into a linear convolution.
        M = next_power_of_two(2 * N - 1)
        n = np.arange(N, dtype=np.int64)
        n2 = n.astype(np.float64) ** 2
        chirp = np.exp(-1j * np.pi * n2 / N)

        a = np.zeros(M, dtype=np.complex128)
        a[:N] = x * chirp

        b = np.zeros(M, dtype=np.complex128)
        b[:N] = np.exp(1j * np.pi * n2 / N)
        if N > 1:
            b[M - N + 1:] = b[1:N][::-1]

        A = super().transform(a)
        B = super().transform(b)
        conv = super().inverse(A * B)
        return chirp * conv[:N]

    def inverse(self, spectrum):
        X = np.asarray(spectrum, dtype=np.complex128).reshape(-1)
        N = X.size
        if N == 0:
            return X.copy()
        return np.conjugate(self.transform(np.conjugate(X))) / N
