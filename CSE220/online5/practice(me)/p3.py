import numpy as np


fs = 1000
T=1/fs

t= np.arange(0 ,1, T)

x1=np.sin(np.pi*2*100*t)
x2=np.sin(np.pi*2*900*t)


print(max(abs(x1+x2)))