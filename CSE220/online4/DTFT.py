import numpy as np
from transforms import DFTAnalyzer
def dtft(x,omega):
    x=np.asarray(x,dtype=complex).reshape(-1)

    omega=np.asarray(omega,dtype=complex).reshape(-1)

    n=np.arange(len(x))

    return np.exp(-1j*omega[:,None]*n[None,:])@ x

x = np.array([1,2,3,4], dtype=float)
N = len(x)
omega_k = 2*np.pi*np.arange(N)/N
X_from_dtft = dtft(x, omega_k)
X_dft = DFTAnalyzer().transform(x)
print(np.allclose(X_from_dtft, X_dft, atol=1e-9))