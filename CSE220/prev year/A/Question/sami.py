import numpy as np

x=np.zeros(100)
y=x*2+1

n = np.arange(5, 10, 1)
m=np.zeros_like(n)
print(n)
print(m)

b = np.array([10, 20, 30, 40, 50])
q = np.array([15, 42, 7])
idx = np.searchsorted(b, q) 
print(idx)