import numpy as np
def complete_real_spectrum(first_half, N):
    """first_half must contain bins 0..N//2 for even N."""
    X = np.zeros(N, dtype=complex)
    X[:N//2 + 1] = first_half
    for k in range(1, N//2):
        X[N-k] = np.conjugate(X[k])
    return X
N = 8
known = np.array([
10+0j, 2+3j, -1+4j, 5-2j, 7+0j
])
X = complete_real_spectrum(known, N)
print(X)
print(np.allclose(X[(-np.arange(N)) % N],
np.conjugate(X)))

