
import numpy as np
import matplotlib.pyplot as plt
from imageio.v2 import imread


class ContinuousImage:
    """Represents a grayscale image as a continuous 2D spatial signal. (Given)"""

    def __init__(self, image_path):
        self.image = imread(image_path, mode='L').astype(float)
        self.image = self.image / np.max(self.image)

        # Continuous spatial coordinate vectors, both spanning [-1, 1]
        self.x = np.linspace(-1, 1, self.image.shape[1])
        self.y = np.linspace(-1, 1, self.image.shape[0])

    def show(self, title="Image"):
        plt.imshow(self.image, cmap='gray')
        plt.title(title)
        plt.axis('off')
        plt.show()


class CFT2D:
    """Computes the 2D Continuous Fourier Transform of a ContinuousImage
    using separable numerical (trapezoidal) integration."""

    def __init__(self, image_obj: ContinuousImage):
        self.I = image_obj.image
        self.x = image_obj.x
        self.y = image_obj.y

        # Frequency axes conjugate to x and y (given), spanning the full
        # Nyquist range implied by the sample spacing (dx, dy). This is
        # what lets the transform represent fine, edge-scale spatial
        # detail instead of only very coarse (near-DC) variation.
        dx = self.x[1] - self.x[0]
        dy = self.y[1] - self.y[0]
        self.u = np.linspace(-1 / (2 * dx), 1 / (2 * dx), self.I.shape[1])
        self.v = np.linspace(-1 / (2 * dy), 1 / (2 * dy), self.I.shape[0])

    def compute_cft(self):
        """
        Compute the real and imaginary parts of the 2D Continuous Fourier
        Transform of self.I, using SEPARABLE trapezoidal integration:

            Re{F(u,v)} =  Integral Integral I(x,y) cos(2*pi*(u*x + v*y)) dx dy
            Im{F(u,v)} = -Integral Integral I(x,y) sin(2*pi*(u*x + v*y)) dx dy

        Do NOT evaluate this as a direct 4-nested-loop double integral over
        (x, y, u, v) -- that is O(N^4) and will not finish in reasonable
        time. Instead exploit separability: expand cos(2*pi*(ux+vy)) and
        sin(2*pi*(ux+vy)) with the angle-sum identities, first integrate
        over x for every (y, u) pair, then integrate the result over y for
        every (u, v) pair. Each of the two stages is an O(N^3) operation
        (an O(N) numerical integral, repeated over an N x N grid), which is
        what makes this tractable.

        Use self.u and self.v (NOT self.x/self.y) as the frequency axes --
        they were already computed for you in __init__.

        Use np.trapezoid(..., axis=...) for the integration -- no built-in
        FFT/DFT routine (np.fft, scipy.fft, ...) may be used anywhere in
        this method.

        Returns
        -------
        real, imag : two 2D numpy arrays, each of shape self.I.shape
        """
        cos_x = np.cos(2 * np.pi * self.u[:, np.newaxis] * self.x[np.newaxis, :])
        sin_x = np.sin(2 * np.pi * self.u[:, np.newaxis] * self.x[np.newaxis, :])
        cos_y = np.cos(2 * np.pi * self.v[:, np.newaxis] * self.y[np.newaxis, :])
        sin_y = np.sin(2 * np.pi * self.v[:, np.newaxis] * self.y[np.newaxis, :])

        image_by_x_frequency = self.I[:, np.newaxis, :]
        x_cos = np.trapezoid(image_by_x_frequency * cos_x[np.newaxis, :, :], self.x, axis=2)
        x_sin = np.trapezoid(image_by_x_frequency * sin_x[np.newaxis, :, :], self.x, axis=2)

        real_uv = np.trapezoid(
            x_cos[:, :, np.newaxis] * cos_y.T[:, np.newaxis, :]
            - x_sin[:, :, np.newaxis] * sin_y.T[:, np.newaxis, :],
            self.y,
            axis=0,
        )
        imag_uv = -np.trapezoid(
            x_sin[:, :, np.newaxis] * cos_y.T[:, np.newaxis, :]
            + x_cos[:, :, np.newaxis] * sin_y.T[:, np.newaxis, :],
            self.y,
            axis=0,
        )

        return real_uv.T, imag_uv.T

    def plot_magnitude(self):
        """
        Plot the log-scaled magnitude spectrum of the 2D CFT computed by
        compute_cft(), i.e. plt.imshow(np.log(1 + magnitude), ...) where
        magnitude = sqrt(real**2 + imag**2). Purely for your own visual
        debugging -- not called by the command-line entry point below.
        """
        real, imag = self.compute_cft()
        magnitude = np.sqrt(real ** 2 + imag ** 2)

        plt.imshow(
            np.log(1 + magnitude),
            extent=[self.u[0], self.u[-1], self.v[0], self.v[-1]],
            origin='lower',
            aspect='auto',
            cmap='magma',
        )
        plt.colorbar(label='log(1 + magnitude)')
        plt.title('2D CFT Magnitude Spectrum')
        plt.xlabel('u frequency')
        plt.ylabel('v frequency')
        plt.show()


class FrequencyFilter:
    """Applies frequency-domain filtering operations. (Given)"""

    def high_pass(self, real, imag, cutoff):
        rows, cols = real.shape
        cx, cy = rows // 2, cols // 2

        real = real.copy()
        imag = imag.copy()
        for i in range(rows):
            for j in range(cols):
                if np.sqrt((i - cx) ** 2 + (j - cy) ** 2) <= cutoff:
                    real[i, j] = 0
                    imag[i, j] = 0
        return real, imag






if __name__ == "__main__":
    input_path = "pikachu.png"
    img = ContinuousImage(input_path)
    cft2d = CFT2D(img)
    real, imag = cft2d.compute_cft()
    F_uv = real + 1j * imag

    # 1. Simulate F(-u, -v) by flipping the matrix both vertically and horizontally
    F_flipped = np.flip(F_uv, axis=(0, 1))

    # 2. Take the complex conjugate
    F_conj_flipped = np.conj(F_flipped)

    # 3. Calculate Error (F(u,v) vs F*(-u,-v))
    mse_mag = np.mean((np.abs(F_uv) - np.abs(F_conj_flipped))**2)
    
    phase_diff = (np.angle(F_uv) - np.angle(F_conj_flipped) + np.pi) % (2 * np.pi) - np.pi
    mse_phase = np.mean(phase_diff**2)

    print("--- Conjugate Symmetry Verification ---")
    print(f"Magnitude MSE : {mse_mag:.10e}")
    print(f"Phase MSE     : {mse_phase:.10e}")