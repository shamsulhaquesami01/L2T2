import sys
from pathlib import Path
import numpy as np

# Provided utilities
from svg_utils import load_svg_path
from epicycle_animation import save_outputs

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
    if len(sys.argv) < 2:
        print("Usage: python3 assignment.py <path_to_svg> [n_harmonics]")
        sys.exit(1)

    svg_path = sys.argv[1]
    N_HARMONICS = int(sys.argv[2]) if len(sys.argv) > 2 else 150

    # 1. Load the original drawing
    t, z = load_svg_path(svg_path, num_points=1000)

    # 2. Compute Original Coefficients
    fs_orig = FourierEpicycles(t, z, n_harmonics=N_HARMONICS)
    fs_orig.calculate_all_coefficients()

    # 3. Simulate a Time Shift
    # We circular-shift (roll) the array by 100 indices to simulate 
    # starting the pen drawing at a later time.
    shift_idx = 100
    t0 = t[shift_idx]  # The physical time delay in seconds
    z_shifted = np.roll(z, shift_idx)

    # 4. Compute Experimental Shifted Coefficients
    fs_shifted = FourierEpicycles(t, z_shifted, n_harmonics=N_HARMONICS)
    fs_shifted.calculate_all_coefficients()

    # 5. Extract Coefficients to NumPy Arrays for Vectorized Math
    # Extract keys (n) and values (c_n) directly from the dictionary
    n_values = np.array(list(fs_orig.coeffs.keys()))
    c_orig = np.array(list(fs_orig.coeffs.values()))
    c_shifted_exp = np.array(list(fs_shifted.coeffs.values()))
    
    # 6. Compute Theoretical Shifted Coefficients
    # The property: f(t - t0) <=> c_n * exp(-j * n * omega * t0)
    omega = fs_orig.omega
    c_shifted_theo = c_orig * np.exp(-1j * n_values * omega * t0)

    # 7. Error Analysis
    mse_mag, mse_phase = calculate_mse(c_shifted_exp, c_shifted_theo)
    
    print(f"--- 1D Fourier Series Time-Shift Error Analysis ---")
    print(f"Time shift (t0) : {t0:.4f} seconds")
    print(f"Magnitude MSE   : {mse_mag:.10e}")
    print(f"Phase MSE       : {mse_phase:.10e}")