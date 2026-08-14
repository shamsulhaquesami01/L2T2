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


class InverseCFT2D:
    """Reconstructs the spatial-domain image from a (filtered) 2D frequency
    spectrum using separable numerical integration."""

    def __init__(self, real, imag, u, v, x, y):
        self.real = real
        self.imag = imag
        self.u = u
        self.v = v
        self.x = x
        self.y = y

    def reconstruct(self):
        """
        Perform the inverse 2D Continuous Fourier Transform:

            I(x,y) = Integral Integral F(u,v) exp(j*2*pi*(u*x + v*y)) du dv

        using the same separable-integration strategy as compute_cft():
        expand the complex exponential into cos/sin via Euler's identity,
        integrate over v first (for every (y, u) pair), then integrate
        that result over u (for every (x, y) pair). Use np.trapezoid.

        self.real, self.imag are the (possibly filtered) frequency-domain
        components; self.u, self.v are the frequency axes they were
        computed on; self.x, self.y are the spatial axes to reconstruct
        onto.

        Returns
        -------
        image : 2D numpy array of shape (len(self.y), len(self.x))
            The reconstructed real-valued spatial-domain signal. Note
            that after a high-pass filter this is NOT a valid image on
            its own (it will contain negative values, since the DC/
            low-frequency component that carried the average brightness
            has been removed) -- see the command-line entry point below
            for how it gets turned into a displayable edge map.
        """
        cos_u = np.cos(2 * np.pi * self.u[:, np.newaxis] * self.x[np.newaxis, :])
        sin_u = np.sin(2 * np.pi * self.u[:, np.newaxis] * self.x[np.newaxis, :])
        cos_v = np.cos(2 * np.pi * self.v[:, np.newaxis] * self.y[np.newaxis, :])
        sin_v = np.sin(2 * np.pi * self.v[:, np.newaxis] * self.y[np.newaxis, :])


        real_cos_v = np.trapezoid(self.real[np.newaxis, :, :] * cos_v.T[:, :, np.newaxis], self.v, axis=1)
        real_sin_v = np.trapezoid(self.real[np.newaxis, :, :] * sin_v.T[:, :, np.newaxis], self.v, axis=1)
        imag_cos_v = np.trapezoid(self.imag[np.newaxis, :, :] * cos_v.T[:, :, np.newaxis], self.v, axis=1)
        imag_sin_v = np.trapezoid(self.imag[np.newaxis, :, :] * sin_v.T[:, :, np.newaxis], self.v, axis=1)
        
        first_part = real_cos_v - imag_sin_v
        second_part = real_sin_v + imag_cos_v
        image = np.trapezoid(
            first_part[:, np.newaxis, :] * cos_u.T[np.newaxis, :, :]
            - second_part[:, np.newaxis, :] * sin_u.T[np.newaxis, :, :],
            self.u,
            axis=2,
        )
        return image

def calculate_mse(array1, array2):
    """
    Computes the Mean Squared Error for magnitude and wrapped phase 
    between two 2D complex arrays.
    """
    mse_mag = np.mean((np.abs(array1) - np.abs(array2))**2)
    phase_diff = (np.angle(array1) - np.angle(array2) + np.pi) % (2 * np.pi) - np.pi
    mse_phase = np.mean(phase_diff**2)
    return mse_mag, mse_phase

if __name__ == "__main__":
    # Hardcoded Input Parameters
    input_path = "pikachu.png"
    
    # 1. Load the Original Image
    img_orig = ContinuousImage(input_path)
    
    # 2. Compute Original CFT
    cft_orig = CFT2D(img_orig)
    real_orig, imag_orig = cft_orig.compute_cft()
    F_orig = real_orig + 1j * imag_orig
    
    # 3. Apply Spatial Shift via array rolling
    # Shift right by 50 pixels, and up by 30 pixels (negative rows = up)
    shift_x_pixels = 50
    shift_y_pixels = -30
    
    img_shifted = ContinuousImage(input_path)
    img_shifted.image = np.roll(img_orig.image, shift=(shift_y_pixels, shift_x_pixels), axis=(0, 1))
    
    # 4. Compute Shifted Experimental CFT
    cft_shifted = CFT2D(img_shifted)
    real_shifted, imag_shifted = cft_shifted.compute_cft()
    F_shifted_exp = real_shifted + 1j * imag_shifted
    
    # 5. Compute Theoretical Shifted Spectrum
    # Extract the physical spacing between pixels in the continuous domain
    dx = img_orig.x[1] - img_orig.x[0]
    dy = img_orig.y[1] - img_orig.y[0]
    
    # Convert pixel shifts to exact continuous physical offsets
    x0 = shift_x_pixels * dx
    y0 = shift_y_pixels * dy
    
    # Broadcast 1D frequency axes into a 2D matrix
    # u is horizontal (columns), v is vertical (rows)
    u_2d = cft_orig.u[np.newaxis, :] 
    v_2d = cft_orig.v[:, np.newaxis] 
    
    # Theoretical Formula: F(u,v) * exp(-j * 2pi * (u*x0 + v*y0))
    phase_shift_matrix = -2 * np.pi * (u_2d * x0 + v_2d * y0)
    F_shifted_theo = F_orig * np.exp(1j * phase_shift_matrix)
    
    # 6. Error Analysis
    mse_mag, mse_phase = calculate_mse(F_shifted_exp, F_shifted_theo)
    
    print(f"--- 2D Spatial Shift Error Analysis ---")
    print(f"Shift applied (pixels): X={shift_x_pixels}, Y={shift_y_pixels}")
    print(f"Physical offset (x0, y0): ({x0:.4f}, {y0:.4f})")
    print(f"Magnitude MSE : {mse_mag:.10e}")
    print(f"Phase MSE     : {mse_phase:.10e}")