import numpy as np
import matplotlib.pyplot as plt

class ContinuousSignal:
    def __init__(self, t):
        self.t = t
        self.values = np.zeros_like(t, dtype=complex)

class SignalGenerator(ContinuousSignal):
    def __init__(self, t):
        super().__init__(t)

    def gaussian(self, a, t_shift=0):
        """
        TODO 1: Generate the Gaussian signal x(t) = exp(-a(t - t_shift)^2)
        Note: The time shift must be implemented directly in this evaluation.
        Store the result in self.values.
        """
        self.values= np.exp(-a*(self.t-t_shift)**2);

class CFTAnalyzer:
    def __init__(self, signal, f):
        self.signal = signal
        self.t = signal.t
        self.f = f
        self.spectrum = np.zeros_like(f, dtype=complex)

    def compute_cft(self):
        """
        TODO 2: Compute the Continuous Fourier Transform of self.signal over frequencies self.f.
        You MUST use np.trapezoid(). np.fft is strictly prohibited.
        """
        phase = 2 * np.pi * self.f[:, np.newaxis] * self.t[np.newaxis, :]
                
                # 2. Broadcast the signal values across the frequencies
        signal_2d = self.signal.values[np.newaxis, :]
                
                # 3. Integrate over the time axis (axis=1) using the trapezoid rule
        real_part = np.trapezoid(signal_2d * np.cos(phase), self.t, axis=1)
        imag_part = -np.trapezoid(signal_2d * np.sin(phase), self.t, axis=1)
                
                # 4. Store as a complex array in self.spectrum
        self.spectrum = real_part + 1j * imag_part

def calculate_time_shift_errors(X_f, Y_f, f, t0):
    """
    TODO 3: Numerically verify the time-shift property.
    Compute MSEmag = 1/N * sum(|X(f)| - |Y(f)|)^2
    Compute MSEphase = 1/N * sum( (∠Y(f) - (∠X(f) - 2*pi*f*t0))^2 )
    Returns: mse_mag, mse_phase
    """
    mag_X = np.abs(X_f)
    mag_Y = np.abs(Y_f)
    mse_mag = np.mean((mag_X - mag_Y)**2)

    phase_X = np.angle(X_f)
    phase_Y = np.angle(Y_f)

    phase_Y_thoery= phase_X-(2*np.pi*f*t0)
    phase_diff=phase_Y-phase_Y_thoery
    phase_diff_wrapped=(phase_diff+np.pi)%(2*np.pi)-np.pi
    mse_phase=np.mean(phase_diff_wrapped**2)
    return mse_mag,mse_phase

if __name__ == "__main__":
    # TODO 4: Define the time axis t in [-5, 5] with 2000 samples
    t = np.linspace(-5,5,2000)
    
    # TODO 5: Define the frequency axis f in [-10, 10] with 1000 samples
    f = np.linspace(-10,10,1000)

    # Generate Original Signal (a=1)
    x_t = SignalGenerator(t)
    x_t.gaussian(a=1, t_shift=0)

    # Generate Shifted Signal (t0=1)
    y_t = SignalGenerator(t)
    y_t.gaussian(a=1, t_shift=1)

    # Compute CFTs
    cft_x = CFTAnalyzer(x_t, f)
    cft_x.compute_cft()

    cft_y = CFTAnalyzer(y_t, f)
    cft_y.compute_cft()

    # TODO 6: Plot the magnitude and phase spectra of both signals
    plt.figure(figsize=(12, 5))
    
    plt.subplot(1, 2, 1)
    plt.plot(f, np.abs(cft_x.spectrum), label='|X(f)|', linewidth=2)
    plt.plot(f, np.abs(cft_y.spectrum), '--', label='|Y(f)|', linewidth=2)
    plt.title("Magnitude Consistency")
    plt.legend()

    plt.subplot(1, 2, 2)
    plt.plot(f, np.angle(cft_x.spectrum), label='Phase X(f)', alpha=0.7)
    plt.plot(f, np.angle(cft_y.spectrum), label='Phase Y(f)', alpha=0.7)
    plt.title("Phase Shift")
    plt.legend()
    
    plt.tight_layout()
    plt.show()




    mse_mag,mse_phase = calculate_time_shift_errors(cft_x.spectrum, cft_y.spectrum,f,1)
    print("--- Time-Shift Error Analysis ---")
    print(f"Magnitude MSE : {mse_mag:.10e}")
    print(f"Phase MSE     : {mse_phase:.10e}")