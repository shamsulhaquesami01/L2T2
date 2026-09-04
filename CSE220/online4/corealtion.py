
import numpy as np
from transforms import DFTAnalyzer, FFTTransformer, ArbitraryLengthFFT

engine=FFTTransformer()


def circular_shift_by_correlation(reference, shifted, engine):
    X = engine.transform(reference)
    Y = engine.transform(shifted)
    corr = engine.inverse(np.conjugate(X) * Y)
    return int(np.argmax(corr.real))


x = np.array([1,7,2,9,3,4,8,0], dtype=float)
true_shift = 3
y = np.roll(x, true_shift)
found = circular_shift_by_correlation(x, y, engine)
print("found =", found)
print("corrected =", np.roll(y, -found))