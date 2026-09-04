import numpy as np
from transforms import DFTAnalyzer, FFTTransformer, ArbitraryLengthFFT

engine = FFTTransformer()
x=np.array([1,2,3,4,0,0,0,0], dtype=float)
y=np.array([4,1,0,2,3,0,0,0], dtype=float)

X= engine.transform(x)
Y=engine.transform(y)

left = engine.transform(2.5*x+3.5*y)
right=2.5*X+3.5*Y

print(np.allclose(left,right,atol=1e-9))
print("error :",np.max(np.abs(left-right)))


