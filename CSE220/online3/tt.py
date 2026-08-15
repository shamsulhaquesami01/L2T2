
# , [

import numpy as np
def compute_cft(t,x,f ):
    t = np.asarray(t)
    x=np.asarray(x)
    f=np.asarray(f)

    exponent = np.exp(-1j*2*np.pi*f[:,None]*t [None ,:])
    integrand = exponent* x[None,:]
    cft = np.trapezoid(integrand , t , axis=1)
    return cft

