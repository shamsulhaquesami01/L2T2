import numpy as np


def dtft(x, omega):
    x = np.asarray(x, dtype=complex).reshape(-1)
    omega = np.asarray(omega, dtype=float).reshape(-1)
    n = np.arange(len(x))
    return np.exp(-1j * omega[:, None] * n[None, :]) @ x
x = np.array([1,2,3,4], dtype=float)
omega = np.linspace(-np.pi, np.pi, 1001)
Xw = dtft(x, omega)
print(Xw.shape) # one DTFT value per omega