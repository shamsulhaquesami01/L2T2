"""Previous-year Online Section C: undo row-wise circular image shifts.

For each row:
    r = IFFT(conj(FFT(original_row)) * FFT(shifted_row))
The index of the largest correlation peak is the circular RIGHT shift.
Undo it with np.roll(shifted_row, -shift).

Includes our assignment's radix-2 FFT and arbitrary-length Bluestein FFT.
"""

import argparse
import numpy as np
from PIL import Image


def next_power_of_two(n):
    if n < 1:
        return 1
    p = 1
    while p < n:
        p <<= 1
    return p


class FFTTransformer:
    name = "fft"

    @staticmethod
    def _core(x, inverse=False):
        a = np.asarray(x, dtype=np.complex128).reshape(-1).copy()
        N = a.size
        if N == 0:
            return a
        if N & (N - 1):
            raise ValueError("radix-2 FFT requires a power-of-two length")

        j = 0
        for i in range(1, N):
            bit = N >> 1
            while j & bit:
                j ^= bit
                bit >>= 1
            j ^= bit
            if i < j:
                a[i], a[j] = a[j], a[i]

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
    name = "arbitrary"

    def transform(self, x):
        x = np.asarray(x, dtype=np.complex128).reshape(-1)
        N = x.size
        if N == 0:
            return x.copy()
        if (N & (N - 1)) == 0:
            return super().transform(x)

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


def _as_channels(row):
    row = np.asarray(row, dtype=np.float64)
    return row[:, None] if row.ndim == 1 else row


def detect_right_shift(original_row, shifted_row, engine=None):
    """Return s such that shifted_row ~= np.roll(original_row, s, axis=0)."""
    if engine is None:
        engine = ArbitraryLengthFFT()

    a = _as_channels(original_row)
    b = _as_channels(shifted_row)
    if a.shape != b.shape:
        raise ValueError("row shapes do not match")

    width, channels = a.shape
    corr = np.zeros(width, dtype=np.float64)
    usable = 0

    for c in range(channels):
        x = a[:, c] - np.mean(a[:, c])
        y = b[:, c] - np.mean(b[:, c])
        if np.linalg.norm(x) < 1e-12 or np.linalg.norm(y) < 1e-12:
            continue

        X = engine.transform(x)
        Y = engine.transform(y)
        corr += engine.inverse(np.conjugate(X) * Y).real
        usable += 1

    if usable == 0:
        return 0
    return int(np.argmax(corr))


def correct_rolling_shutter(original, shifted):
    original = np.asarray(original, dtype=np.float64)
    shifted = np.asarray(shifted, dtype=np.float64)

    if original.shape != shifted.shape:
        raise ValueError("original and shifted images must have the same shape")
    if original.ndim not in (2, 3):
        raise ValueError("expected HxW or HxWxC image")

    H = original.shape[0]
    corrected = shifted.copy()
    shifts = np.zeros(H, dtype=np.int64)
    engine = ArbitraryLengthFFT()

    for r in range(H):
        s = detect_right_shift(original[r], shifted[r], engine)
        shifts[r] = s
        corrected[r] = np.roll(shifted[r], -s, axis=0)

    return corrected, shifts


def load_rgb(path):
    return np.asarray(Image.open(path).convert("RGB"), dtype=np.float64)


def save_rgb(array, path):
    out = np.clip(np.rint(array), 0, 255).astype(np.uint8)
    Image.fromarray(out, mode="RGB").save(path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("original")
    ap.add_argument("shifted")
    ap.add_argument("output", nargs="?", default="corrected.png")
    args = ap.parse_args()

    original = load_rgb(args.original)
    shifted = load_rgb(args.shifted)
    corrected, shifts = correct_rolling_shutter(original, shifted)
    save_rgb(corrected, args.output)

    err = np.abs(corrected - original)
    print("saved:", args.output)
    print("detected shifts:", shifts.tolist())
    print("shift range:", int(shifts.min()), "to", int(shifts.max()))
    print("max absolute pixel error:", float(err.max()))
    print("mean absolute pixel error:", float(err.mean()))
    print("exact pixel match:",
          bool(np.array_equal(np.rint(corrected).astype(np.uint8),
                              np.rint(original).astype(np.uint8))))
    print("JPEG note: lossy compression can prevent exact pixel equality even")
    print("when the geometric row shifts are detected and undone correctly.")


if __name__ == "__main__":
    main()
