import numpy as np
from transforms import FFTTransformer

x = np.array([1,2,3,4,0,1,0,-1], dtype=complex)
N = len(x)
idx = (-np.arange(N)) % N
engine = FFTTransformer()

X=engine.transform(x)

left=engine.transform(X)
right = N * x[idx]
print(np.allclose(left, right, atol=1e-9))