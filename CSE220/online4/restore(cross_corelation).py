import numpy as np
from transforms import FFTTransformer

def find_shift(reference, observed):
    fft = FFTTransformer()
    X = fft.transform(reference)
    Y = fft.transform(observed)
    corr=fft.inverse