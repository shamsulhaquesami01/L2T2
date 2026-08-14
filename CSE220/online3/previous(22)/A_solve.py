import numpy as np
import matplotlib.pyplot as plt

class ContinuousSignal:
    def __init__(self, t):
        self.t = t
        self.values = np.zeros_like(t, dtype=complex)

    def plot(self, title="Signal"):
        plt.plot(self.t, np.real(self.values), label='Real')
        plt.plot(self.t, np.imag(self.values), label='Imaginary')
        plt.title(title)
        plt.legend()
        plt.show()

class SignalGenerator(ContinuousSignal):
    def __init__(self, t):
        super().__init__(t)

    def generate_x(self):
        """
        TODO 1: Generate the original signal x(t) = 0.5cos(4t) + 0.5sin(6t)
        Store the result in self.values.
        """
        self.values=0.5*np.cos(4*self.t)+0.5*np.sin(6*self.t);

    def generate_y1(self):
        """
        TODO 2: Derive and generate the first analytical derivative y1(t) = d/dt x(t)
        Store the result in self.values.
        """
        self.values=-2*np.sin(4*self.t)+3*np.cos(6*self.t);

    def generate_y2(self):
        """
        TODO 3: Derive and generate the second analytical derivative y2(t) = d^2/dt^2 x(t)
        Store the result in self.values.
        """
        self.values=-8*np.cos(4*self.t)-18*np.sin(6*self.t);

class CFTAnalyzer:
    def __init__(self, signal, f):
        self.signal = signal
        self.t = signal.t
        self.f = f
        self.spectrum = np.zeros_like(f, dtype=complex)

    def compute_cft(self):
        """
        TODO 4: Compute the Continuous Fourier Transform of self.signal over frequencies self.f.
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

def calculate_mse(array1, array2):
    """
    TODO 5: Implement Mean Squared Error (MSE) calculation for both magnitude and phase.
    Ensure phase wrapping is handled correctly.
    Returns: mse_mag, mse_phase
    """
    pass

if __name__ == "__main__":
    t = np.linspace(-5, 5, 2000)
    f = np.linspace(-10, 10, 1000)

    # 1. Generate signals
    sig_x = SignalGenerator(t)
    sig_x.generate_x()

    sig_y1 = SignalGenerator(t)
    sig_y1.generate_y1()

    # 2. Compute Experimental CFTs
    cft_x = CFTAnalyzer(sig_x, f)
    cft_x.compute_cft()

    cft_y1 = CFTAnalyzer(sig_y1, f)
    cft_y1.compute_cft()

    # 3. Compute Theoretical CFT for y1(t)
    # TODO 6: Multiply the spectrum of x(t) by j2*pi*f to get the theoretical Y1(f)
    theoretical_Y1 = np.zeros_like(f, dtype=complex) 

    # 4. Verify Overlap
    # TODO 7: Plot |Y1(f)| vs |theoretical_Y1(f)| and their phases.
    
    # 5. Error Analysis
    # TODO 8: Call calculate_mse and print the results for the 1st derivative.
    # Repeat the plotting and MSE calculation for the 2nd and 3rd derivatives.