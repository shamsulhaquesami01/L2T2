import numpy as np
from transforms import DFTAnalyzer, FFTTransformer, ArbitraryLengthFFT

engine = FFTTransformer()

def circular_conv_direct(x,h):
    x = np.asarray(x, dtype=complex)
    h = np.asarray(h, dtype=complex)
    N = len(x)
    assert len(h) == N

    y = np.zeros(N, dtype=complex)
    for n in range(N):
        for m in range(N):
            y[n] += x[m] * h[(n-m) % N]
    return y

x = np.array([2,4,5,6,5,3,2,1], dtype=float)
h = np.array([1,2,1,0,0,0,0,0], dtype=float)
direct = circular_conv_direct(x, h)
spectral = engine.inverse(engine.transform(x) * engine.transform(h))
print(np.allclose(direct, spectral, atol=1e-9))
print(np.real_if_close(spectral))
