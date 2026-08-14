import numpy as np
import matplotlib.pyplot as plt

class ContinuousSignal:
    def __init__(self, t):
        self.t = t
        self.values = np.zeros_like(t, dtype=complex)

class SignalGenerator(ContinuousSignal):
    def __init__(self, t):
        super().__init__(t)

    def generate_base_signal(self, scale_a=1, phase_f0=0):
        """
        TODO 1: Generate x(t) = Square(a*t) + Triangle(a*t)
        Apply the phase shift exp(j * 2 * pi * f0 * t) to the final combined signal.
        Note: You must mathematically define the square and triangle waves here using np.sign/np.arcsin, etc.
        Store the result in self.values.
        """
        # Square wave: Sign of a sine wave
        scaled_t=scale_a*self.t
        square = np.sign(np.sin(2 * np.pi * scaled_t))
        
        # Triangle wave: Arcsin of a sine wave (normalized to unit amplitude)
        triangle = (2 / np.pi) * np.arcsin(np.sin(2 * np.pi * scaled_t))

        base=square+triangle

        multiplier = np.exp(1j*2*np.pi*phase_f0*self.t)

        self.values=base*multiplier

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
        signal_2d = self.signal.values[np.newaxis, :]
        
        real_part = np.trapezoid(signal_2d * np.cos(phase), self.t, axis=1)
        imag_part = -np.trapezoid(signal_2d * np.sin(phase), self.t, axis=1)
        
        self.spectrum = real_part + 1j * imag_part

def theoretical_scaled_spectrum(X_f, f, a, f0):
    """
    TODO 3: Given the original spectrum X(f), compute the theoretical modified spectrum.
    Formula: (1 / |a|) * X((f - f0) / a)
    You will need to use np.interp to interpolate the shifted/scaled frequency indices 
    back onto the original frequency grid.
    Returns: The theoretical Y(f) array.
    """
    f_query = (f - f0) / a
    
    # 2. Interpolate the Real part
    # np.interp evaluates the original (f, real(X)) graph at the new f_query coordinates.
    # left=0, right=0 ensures frequencies pushed outside the bounds default to zero.
    real_interp = np.interp(f_query, f, np.real(X_f), left=0, right=0)
    
    # 3. Interpolate the Imaginary part
    imag_interp = np.interp(f_query, f, np.imag(X_f), left=0, right=0)
    
    # 4. Recombine and apply magnitude scale (1 / |a|)
    theoretical_Y = (1 / abs(a)) * (real_interp + 1j * imag_interp)
    
    return theoretical_Y
def calculate_mse(array1, array2):
    """
    Standard Magnitude and Wrapped Phase MSE Calculator.
    """
    mse_mag = np.mean((np.abs(array1) - np.abs(array2))**2)
    phase_diff = (np.angle(array1) - np.angle(array2) + np.pi) % (2 * np.pi) - np.pi
    mse_phase = np.mean(phase_diff**2)
    return mse_mag, mse_phase

if __name__ == "__main__":
    t = np.linspace(-5, 5, 2000)
    f = np.linspace(-10, 10, 1000)

    a = 10
    f0 = 10

    # Generate Original Signal
    x_t = SignalGenerator(t)
    x_t.generate_base_signal(scale_a=1, phase_f0=0)

    # Generate Modified Signal
    y_t = SignalGenerator(t)
    y_t.generate_base_signal(scale_a=a, phase_f0=f0)

    # Compute CFTs
    cft_x = CFTAnalyzer(x_t, f)
    cft_x.compute_cft()

    cft_y = CFTAnalyzer(y_t, f)
    cft_y.compute_cft()

    # Get theoretical spectrum
    theoretical_Y = theoretical_scaled_spectrum(cft_x.spectrum, f, a, f0)

    plt.figure(figsize=(12, 5))
    
    plt.subplot(1, 2, 1)
    plt.plot(f, np.abs(cft_y.spectrum), label='Experimental |Y(f)|')
    plt.plot(f, np.abs(theoretical_Y), '--', label='Theoretical scaled |X(f)|')
    plt.title("Scaling and Shift Property (Magnitude)")
    plt.legend()

    plt.subplot(1, 2, 2)
    plt.plot(f, np.angle(cft_y.spectrum), label='Experimental Phase')
    plt.plot(f, np.angle(theoretical_Y), '--', label='Theoretical Phase')
    plt.title("Scaling and Shift Property (Phase)")
    plt.legend()
    
    plt.tight_layout()
    plt.show()

    # Execute Error Analysis
    mse_mag, mse_phase = calculate_mse(cft_y.spectrum, theoretical_Y)
    print("--- Time Scaling & Phase Shift Error Analysis ---")
    print(f"Magnitude MSE : {mse_mag:.10e}")
    print(f"Phase MSE     : {mse_phase:.10e}")