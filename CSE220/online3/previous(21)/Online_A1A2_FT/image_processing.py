import numpy as np
import matplotlib.pyplot as plt


# ============================================================
# 1. LOAD IMAGE
# ============================================================

image = plt.imread("noisy_image.png")


# ============================================================
# 2. SHOW ORIGINAL IMAGE
# ============================================================

plt.figure()
plt.title("Original Image")
plt.imshow(image, cmap="gray")
plt.show()


# ============================================================
# 3. CONVERT TO GRAYSCALE
# ============================================================

if image.ndim == 3:

    # The uploaded PNG has RGBA channels.
    # We only use RGB, not the alpha channel.

    image = image[:, :, :3]

    image = np.mean(
        image,
        axis=2
    )


# ============================================================
# 4. NORMALIZE IMAGE
# ============================================================

# If image values are 0 to 255,
# convert them to 0 to 1.

if image.max() > 1:

    image = image / 255.0


print("Image shape:", image.shape)


# ============================================================
# 5. FOURIER TRANSFORM ROW BY ROW
# ============================================================

# axis=1 means:
#
# For every row, perform FFT across the columns.
#
# image.shape = (number_of_rows, number_of_columns)
#
# So:
#
# axis=0 -> move vertically through rows
# axis=1 -> move horizontally through columns
#
# Since the noise is vertical stripes,
# we want to analyze the horizontal direction.
# Therefore we use axis=1.

F = np.fft.fft(
    image,
    axis=1
)


# ============================================================
# 6. FIND THE COMMON NOISE SPECTRUM
# ============================================================

# Average the Fourier transform of all rows.

noise_spectrum = np.mean(
    F,
    axis=0
)


# ============================================================
# 7. REMOVE THE COMMON NOISE
# ============================================================

F_clean = (
    F
    - noise_spectrum[None, :]
)


# ============================================================
# 8. INVERSE FOURIER TRANSFORM
# ============================================================

denoised_image = np.fft.ifft(
    F_clean,
    axis=1
).real


# ============================================================
# 9. CONTRAST NORMALIZATION
# ============================================================

# The recovered letter has relatively small amplitude.
# Stretch it to [0, 1] so that it becomes visible.

minimum = denoised_image.min()
maximum = denoised_image.max()

denoised_image = (
    denoised_image - minimum
) / (
    maximum - minimum
)


# ============================================================
# 10. SAVE RESULT
# ============================================================

plt.imsave(
    "denoised_image.png",
    denoised_image,
    cmap="gray"
)


# ============================================================
# 11. DISPLAY RESULT
# ============================================================

plt.figure()

plt.title("Denoised Image")

plt.imshow(
    denoised_image,
    cmap="gray"
)

plt.axis("off")

plt.show()