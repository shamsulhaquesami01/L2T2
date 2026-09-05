import numpy as np
from transforms import FFTTransformer

engine=FFTTransformer()

x = np.array([1,7,2,9,3,4,8,0], dtype=float)
N=len(x)
X=engine.transform(x)

idx=(-np.arange(N))%N
x_rev=x[idx]
X_rev_direct=engine.transform(x_rev)

X_rev_propert=X[idx]

print(np.allclose(X_rev_direct,X_rev_propert,atol=1e-9))