import numpy as np
from transforms import FFTTransformer

x= np.array([3,5,2,0,1,2,4,1], dtype=float)
N=len(x)
engine = FFTTransformer()
X=engine.transform(x)

Et= np.sum(np.abs(x)**2)
Ef=np.sum(np.abs(X)**2)/N
print(np.allclose(Et,Ef,atol=1e-9))