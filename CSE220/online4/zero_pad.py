import numpy as np
from transforms import DFTAnalyzer, FFTTransformer, ArbitraryLengthFFT

engine = FFTTransformer()
x=np.array([1,2,3,4,0,0,0,0], dtype=float)
N=len(x)

X=engine.transform(x)

y=np.zeros(4*N)

y[:N]=x

Y=engine.transform(y)

print(np.allclose(X,Y[::4],atol=1e-9))