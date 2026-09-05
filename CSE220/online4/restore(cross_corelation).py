import numpy as np
from transforms import FFTTransformer

def find_shift(reference, observed):
    fft = FFTTransformer()
    X = fft.transform(reference)
    Y = fft.transform(observed)
    corr=fft.inverse(np.conjugate(X)*Y).real
    return int(np.argmax(corr))

x = np.array([3,1,4,1,5,9,2,6], dtype=float)
y = np.roll(x, 5)
s = find_shift(x, y)
restored = np.roll(y, -s)
print("shift =", s)
print("match =", np.allclose(restored, x))