import numpy as np
from transforms import DFTAnalyzer, FFTTransformer, ArbitraryLengthFFT

engine = FFTTransformer()
x=np.array([1,2,3,4,0,0,0,0], dtype=float)
N=len(x)

X= engine.transform(x)


m=3
k=np.arange(N)
y=np.roll(x,m)
Y_direct=engine.transform(y)

phase=np.exp(-2j*np.pi*k*m/N)
Y = phase*X


print(np.allclose(Y_direct,Y,atol=1e-9))
print("error :",np.max(np.abs(Y_direct-Y)))


