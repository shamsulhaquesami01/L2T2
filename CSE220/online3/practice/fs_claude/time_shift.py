import sys
from pathlib import Path
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


def calculate_mse(array1, array2):
    """
    Computes the Mean Squared Error for magnitude and wrapped phase.
    """
    mse_mag = np.mean((np.abs(array1) - np.abs(array2))**2)
    phase_diff = (np.angle(array1) - np.angle(array2) + np.pi) % (2 * np.pi) - np.pi
    mse_phase = np.mean(phase_diff**2)
    return mse_mag, mse_phase

if __name__ == "__main__":
    t=  np.linspace(0,2*np.pi,50)
    z = np.cos(t)+0.6*1j*np.sin(2*t)
    fs = FourierEpicycles(t,z,100)
    fs.calculate_all_coefficients()
    cn= np.array(list(fs.coeffs.values()))
    t0= fs.T/6
    g=np.roll(z,t0)
    fs_shifted=FourierEpicycles(t,g,100)
    fs_shifted.calculate_all_coefficients()
    dn=np.array(list(fs_shifted.coeffs.values()))
    dn_theory=cn*np.exp(-1j*fs.omega*t0)
    mse_mag , mse_phase = calculate_mse(dn,dn_theory)

