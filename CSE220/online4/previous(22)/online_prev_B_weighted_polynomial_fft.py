"""Previous-year Online Section B: weighted polynomial product by FFT/IFFT.

The handout is internally inconsistent about OUTPUT ORDER:
- Example 1 is printed in descending-power order.
- Example 2's listed result is the reverse (ascending-power order).

Therefore this solution exposes an explicit output_order argument rather than
silently guessing.  The underlying weighted product is unambiguous.
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


def weighted_polynomial_product(P_desc, Q_desc, W_desc, output_order="descending"):
    """Compute the weighted product.

    P_desc, Q_desc, W_desc are aligned in descending powers.
    output_order:
        "descending" -> polynomial-style coefficients, highest power first.
        "ascending"  -> constant term first.
    """
    P = np.asarray(P_desc, dtype=np.int64).reshape(-1)
    Q = np.asarray(Q_desc, dtype=np.int64).reshape(-1)
    W = np.asarray(W_desc, dtype=np.int64).reshape(-1)

    if P.size == 0 or Q.size == 0:
        return []
    if W.size != P.size:
        raise ValueError("W must contain one weight for every coefficient of P")

    weighted_P_desc = P * W
    needed = weighted_P_desc.size + Q.size - 1
    N = next_power_of_two(needed)

    ap = np.zeros(N, dtype=np.complex128)
    bp = np.zeros(N, dtype=np.complex128)
    ap[:weighted_P_desc.size] = weighted_P_desc
    bp[:Q.size] = Q

    fft = FFTTransformer()
    conv_desc = np.rint(
        fft.inverse(fft.transform(ap) * fft.transform(bp)).real[:needed]
    ).astype(np.int64).tolist()

    if output_order == "descending":
        return conv_desc
    if output_order == "ascending":
        return conv_desc[::-1]
    raise ValueError("output_order must be 'descending' or 'ascending'")


if __name__ == "__main__":
    # Matches Example 1 exactly as printed.
    ex1 = weighted_polynomial_product([1, 3, 2], [4, 1], [3, 2, 1], "descending")
    print("Example 1 (descending):", ex1)
    assert ex1 == [12, 27, 14, 2]

    # Example 2 in the handout is printed in the opposite orientation.
    ex2 = weighted_polynomial_product(
        [1, 3, 2, 6, 7], [4, 1], [3, 2, 1, 5, 6], "ascending"
    )
    print("Example 2 (ascending, as printed in handout):", ex2)
    assert ex2 == [42, 198, 122, 14, 27, 12]

    print("If your actual online states one output order clearly, use that order.")
