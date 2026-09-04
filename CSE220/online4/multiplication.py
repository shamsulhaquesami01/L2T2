
import numpy as np
from transforms import DFTAnalyzer, FFTTransformer, ArbitraryLengthFFT

engine=FFTTransformer()

def circular_conv_same_length(a, b):
    N = len(a)
    out = np.zeros(N, dtype=complex)
    for k in range(N):
        for r in range(N):
            out[k] += a[r] * b[(k-r) % N]
    return out

x = np.array([1,2,3,4,0,0,0,0], dtype=complex)
h = np.array([2,1,0,3,1,0,0,0], dtype=complex)
N=len(x)
X = engine.transform(x)
H = engine.transform(h)
left = engine.transform(x*h)
right = circular_conv_same_length(X, H) / N
print(np.allclose(left, right, atol=1e-9))
