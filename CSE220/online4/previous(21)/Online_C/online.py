import numpy as np
import matplotlib.pyplot as plt
from PIL import Image

# Import existing assignment classes
from transforms import FFTTransformer, ArbitraryLengthFFT


def transform_2d(plane, engine):
    """Compute 2D forward DFT by transforming every row, then every column."""
    x = np.asarray(plane, dtype=np.complex128)
    row_done = np.empty_like(x)
    for r in range(x.shape[0]):
        row_done[r, :] = engine.transform(x[r, :])
    out = np.empty_like(row_done)
    for c in range(x.shape[1]):
        out[:, c] = engine.transform(row_done[:, c])
    return out


def inverse_2d(spectrum, engine):
    """Compute 2D inverse DFT by inverting every column, then every row."""
    x = np.asarray(spectrum, dtype=np.complex128)
    col_done = np.empty_like(x)
    for c in range(x.shape[1]):
        col_done[:, c] = engine.inverse(x[:, c])
    out = np.empty_like(col_done)
    for r in range(x.shape[0]):
        out[r, :] = engine.inverse(col_done[r, :])
    return out


def detect_2d_shift(reference_plane, shifted_plane, engine):
    """
    Detect vertical (row) and horizontal (col) shifts using 2D FFT cross-correlation.
    """
    # 1. Compute 2D spectra
    X = transform_2d(reference_plane, engine)
    Y = transform_2d(shifted_plane, engine)

    # 2. Cross-power spectrum: X* * Y
    cross_spectrum = np.conjugate(X) * Y

    # 3. 2D Inverse DFT to obtain spatial correlation matrix
    corr = inverse_2d(cross_spectrum, engine)

    # 4. Locate global maximum peak on the 2D grid
    peak_idx = np.unravel_index(np.argmax(corr.real), corr.shape)
    shift_r, shift_c = int(peak_idx[0]), int(peak_idx[1])

    # Convert wrap-around shifts greater than half-dimension to signed offsets
    H, W = reference_plane.shape
    if shift_r > H // 2:
        shift_r -= H
    if shift_c > W // 2:
        shift_c -= W

    return shift_r, shift_c


def realign_image(shifted_image, shift_r, shift_c):
    """Realign image by reversing the detected shifts directly in spatial domain."""
    if shifted_image.ndim == 2:
        return np.roll(shifted_image, (-shift_r, -shift_c), axis=(0, 1))
    else:
        # Realign all color channels along height (axis 0) and width (axis 1)
        return np.roll(shifted_image, (-shift_r, -shift_c), axis=(0, 1))


# =====================================================================
# Verification & Execution Script
# =====================================================================
if __name__ == "__main__":
    # Choose engine: ArbitraryLengthFFT works for any H x W;
    # FFTTransformer works when H and W are powers of two.
    engine = ArbitraryLengthFFT()

    # Load images or simulate satellite input
    try:
        orig_img = np.array(Image.open("satellite_original.png"), dtype=np.float64)
        shift_img = np.array(Image.open("satellite_shifted.png"), dtype=np.float64)
    except FileNotFoundError:
        # Synthetic fallback demonstration
        np.random.seed(42)
        base = np.zeros((128, 128), dtype=np.float64)
        base[30:70, 40:90] = 200.0  # Synthetic landmass feature
        orig_img = base
        true_r, true_c = 14, -23
        shift_img = np.roll(orig_img, (true_r, true_c), axis=(0, 1))
        print(f"Generated synthetic shift: vertical={true_r}, horizontal={true_c}")

    # Extract 2D plane for correlation (use luminance/channel 0 if RGB)
    ref_plane = orig_img if orig_img.ndim == 2 else orig_img[:, :, 0]
    tgt_plane = shift_img if shift_img.ndim == 2 else shift_img[:, :, 0]

    # Detect shifts
    found_r, found_c = detect_2d_shift(ref_plane, tgt_plane, engine)
    print(f"Detected shifts -> Vertical (row shift): {found_r}, Horizontal (col shift): {found_c}")

    # Re-align the shifted image
    aligned_img = realign_image(shift_img, found_r, found_c)

    # Verification: check alignment error
    diff = np.max(np.abs(orig_img - aligned_img))
    print(f"Max absolute difference after re-alignment: {diff:.3e}")

    # Plot comparison
    plt.figure(figsize=(12, 4))
    plt.subplot(1, 3, 1)
    plt.imshow(orig_img.astype(np.uint8), cmap="gray" if orig_img.ndim == 2 else None)
    plt.title("Original Reference")
    plt.axis("off")

    plt.subplot(1, 3, 2)
    plt.imshow(shift_img.astype(np.uint8), cmap="gray" if shift_img.ndim == 2 else None)
    plt.title(f"Shifted Image (r={found_r}, c={found_c})")
    plt.axis("off")

    plt.subplot(1, 3, 3)
    plt.imshow(aligned_img.astype(np.uint8), cmap="gray" if aligned_img.ndim == 2 else None)
    plt.title("Re-aligned Result")
    plt.axis("off")

    plt.tight_layout()
    plt.show()