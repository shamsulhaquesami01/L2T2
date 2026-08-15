
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
    """
    Universally shifts a periodic signal regardless of the time array's starting bound.
    """
    t_shifted = ((t - t0 - t[0]) % T) + t[0]
    
    z_re = np.interp(t_shifted, t, np.real(z))
    if np.iscomplexobj(z):
        z_im = np.interp(t_shifted, t, np.imag(z))
        return z_re + 1j * z_im
    return z_re  # stays real-valued if the input was real

if __name__ == "__main__":
    t=  np.linspace(0,2*np.pi,4000)
    z = np.cos(t)+0.6*1j*np.sin(2*t)
    fs = FourierEpicycles(t,z,100)
    fs.calculate_all_coefficients()
    n_values=np.array(list(fs.coeffs.keys()))
    cn= np.array(list(fs.coeffs.values()))
    t0= fs.T/6
    g=np.roll(z,t0)
    fs_shifted=FourierEpicycles(t,g,100)
    fs_shifted.calculate_all_coefficients()
    dn=np.array(list(fs_shifted.coeffs.values()))
    dn_theory=cn*np.exp(-1j*fs.omega*t0)
    mask = np.abs(cn) > 1e-6
    mse_mag , mse_phase = calculate_mse(dn,dn_theory,mask=mask)
    fig, axes = plt.subplots(2, 1, figsize=(9, 7))
 
    # magnitude overlay
    ax = axes[0]
    ax.stem(n_values, np.abs(cn), linefmt='C0-', markerfmt='C0o', basefmt=' ', label='|cn| (original)')
    ax.stem(n_values, np.abs(dn), linefmt='C1--', markerfmt='C1x', basefmt=' ', label='|dn| (shifted, measured)')
    ax.set_xlabel('n')
    ax.set_ylabel('magnitude')
    ax.set_title('Magnitude spectrum: original vs. shifted')
    ax.legend()
 
    # phase overlay -- only plot where the coefficient is meaningful
    ax = axes[1]
    n_masked = n_values[mask]
    ax.stem(n_masked, np.angle(dn)[mask], linefmt='C1--', markerfmt='C1x', basefmt=' ', label='∠dn (measured)')
    ax.stem(n_masked, np.angle(dn_theory)[mask], linefmt='C2:', markerfmt='C2^', basefmt=' ', label='∠dn (theory)')
    ax.set_xlabel('n')
    ax.set_ylabel('phase (rad)')
    ax.set_title('Phase spectrum: measured vs. theoretical (noise-floor harmonics excluded)')
    ax.legend()
 
    plt.tight_layout()
    plt.savefig('problem1_verification.png', dpi=150)
    print("Saved plot to problem1_verification.png")
    plt.show()

