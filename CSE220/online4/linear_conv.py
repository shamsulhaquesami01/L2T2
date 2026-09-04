import numpy as np
from transforms import DFTAnalyzer, FFTTransformer, ArbitraryLengthFFT

def next_power_of_two(n):
    """Return the smallest power of two >= n, with minimum 1."""
    if n < 1:
        return 1
    p = 1
    while p < n:
        p <<= 1
    return p

def linear_conv_fft(x, h):
    x = np.asarray(x, dtype=complex)
    h = np.asarray(h, dtype=complex)
    needed = len(x) + len(h) - 1
    N = next_power_of_two(needed)
    xp = np.zeros(N, dtype=complex)
    hp = np.zeros(N, dtype=complex)
    xp[:len(x)] = x
    hp[:len(h)] = h
    fft = FFTTransformer()
    y = fft.inverse(fft.transform(xp) * fft.transform(hp))
    return y[:needed]

x = np.array([2,4,5,6,5,3,2,1])
h = np.array([1,2,1])
a = np.convolve(x, h)
b = linear_conv_fft(x, h)
print(np.allclose(a, b, atol=1e-9))
