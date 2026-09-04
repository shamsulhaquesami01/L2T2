"""Previous-year Online Section A: big-integer multiplication from LSD-first digits.

FFT/IFFT-based circular convolution is zero-padded so it equals the required
linear convolution.  The FFT core is copied from our assignment so this is
self-contained and can be submitted as one Python file.
"""

import numpy as np


def next_power_of_two(n):
    """Return the smallest power of two >= n, with minimum 1."""
    if n < 1:
        return 1
    p = 1
    while p < n:
        p <<= 1
    return p


class FFTTransformer:
    """Radix-2 decimation-in-time Cooley-Tukey FFT copied from our assignment."""

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


def multiply_digit_arrays(A, B):
    A = np.asarray(A, dtype=np.int64).reshape(-1)
    B = np.asarray(B, dtype=np.int64).reshape(-1)

    if A.size == 0 or B.size == 0:
        return [0]
    if np.any((A < 0) | (A > 9)) or np.any((B < 0) | (B > 9)):
        raise ValueError("A and B must contain digits 0..9")

    needed = A.size + B.size - 1
    N = next_power_of_two(needed)

    ap = np.zeros(N, dtype=np.complex128)
    bp = np.zeros(N, dtype=np.complex128)
    ap[:A.size] = A
    bp[:B.size] = B

    fft = FFTTransformer()
    raw = fft.inverse(fft.transform(ap) * fft.transform(bp))
    coeff = np.rint(raw.real[:needed]).astype(np.int64).tolist()

    carry = 0
    i = 0
    while i < len(coeff) or carry:
        if i == len(coeff):
            coeff.append(0)
        total = coeff[i] + carry
        carry, coeff[i] = divmod(total, 10)
        i += 1

    while len(coeff) > 1 and coeff[-1] == 0:
        coeff.pop()
    return coeff


def digits_to_int(digits):
    return sum(int(d) * 10**i for i, d in enumerate(digits))


if __name__ == "__main__":
    ans1 = multiply_digit_arrays([3, 2, 1], [5, 4])
    print("Example 1:", ans1)
    assert ans1 == [5, 3, 5, 5]

    ans2 = multiply_digit_arrays([9, 9, 9], [9, 9])
    print("Example 2:", ans2)
    assert ans2 == [1, 0, 9, 8, 9]
