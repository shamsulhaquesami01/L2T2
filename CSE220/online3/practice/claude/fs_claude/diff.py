
import matplotlib.pyplot as plt
import numpy as np



class FourierEpicycles:
    def __init__(self, t, signal, n_harmonics):
        self.t = t
        self.signal = signal
        self.N = n_harmonics
        self.T = float(t[-1] - t[0])
        self.omega = 2 * np.pi / self.T
        self.coeffs = {}

    def calculate_cn(self, n):
        kernel = np.exp(-1j * n * self.omega * self.t)
        integrand = self.signal * kernel
        integral = np.trapezoid(integrand, self.t)
        return integral / self.T

    def calculate_all_coefficients(self):
        for n in range(-self.N, self.N + 1):
            self.coeffs[n] = self.calculate_cn(n)

    def approximate(self, t):
        t = np.asarray(t)
        reconstruction = np.zeros_like(t, dtype=complex)
        for n, coefficient in self.coeffs.items():
            reconstruction += coefficient * np.exp(1j * n * self.omega * t)
        return reconstruction


def calculate_mse(array1, array2, mask=None):
    """
    Computes the Mean Squared Error for magnitude and wrapped phase.
    """
    mse_mag = np.mean((np.abs(array1) - np.abs(array2))**2)
    phase_diff = (np.angle(array1) - np.angle(array2) + np.pi) % (2 * np.pi)
    if mask is not None:
        phase_diff = phase_diff[mask]- np.pi
    mse_phase = np.mean(phase_diff**2)
    return mse_mag, mse_phase

def shift_periodic(t, z, t0, T):
    t_shifted = (t - t0) % T
    z_re = np.interp(t_shifted, t, np.real(z))
    if np.iscomplexobj(z):
        z_im = np.interp(t_shifted, t, np.imag(z))
        return z_re + 1j * z_im
    return z_re   # stays real-valued if the input was real

if __name__ == "__main__":
    t=np.linspace(0,2*np.pi,3000)
    x=np.cos(t)+0.3j*np.sin(3*t)
    y=-np.sin(t)+0.9j*np.cos(3*t)
    fsx=FourierEpicycles(t,x,100)
    fsx.calculate_all_coefficients()
    cn=np.array(list(fsx.coeffs.values()))
    fsy=FourierEpicycles(t,y,100)
    fsy.calculate_all_coefficients()
    dn=np.array(list(fsy.coeffs.values()))
    n_values=np.array(list(fsx.coeffs.keys()))
    dn_theory=1j*n_values*fsx.omega*cn

    mask = np.abs(cn) > 1e-6
    mse_mag, mse_phase = calculate_mse(dn,dn_theory,mask=mask)
    print(mse_mag)
    print(mse_phase)

