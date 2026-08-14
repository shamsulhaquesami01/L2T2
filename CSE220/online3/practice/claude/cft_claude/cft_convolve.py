import numpy as np
import matplotlib.pyplot as plt
import time


def compute_cft(t, x, f):
    t = np.asarray(t)
    x = np.asarray(x)
    f = np.asarray(f)
    exponential = np.exp(
        -1j * 2 * np.pi * f[:, None] * t[None, :]
    )
    integrand = exponential * x[None, :]

    X = np.trapezoid(
        integrand,
        t,
        axis=1
    )

    return X

def conv_numeric(t, x1, x2):
    y = np.zeros(len(t))

    for i in range(len(t)):

        shifted_time = t[i] - t

        x2_shifted = np.interp(
            shifted_time,
            t,
            x2,
            left=0,
            right=0
        )

        integrand = x1 * x2_shifted

        y[i] = np.trapezoid(
            integrand,
            t
        )

    return y

def rectangular_pulse(t):
    """
    x1(t) = 1, |t| <= 0.5
           0, otherwise

    Width = 1
    Amplitude = 1
    """

    return np.where(
        np.abs(t) <= 0.5,
        1.0,
        0.0
    )


# ============================================================
# 4. x2(t): TRIANGULAR PULSE
# ============================================================

def triangular_pulse(t):
    """
    x2(t):

        1 - |t|/0.5,    |t| <= 0.5
        0,              otherwise

    This gives:

        x2(0) = 1
        x2(+/-0.5) = 0

    Width = 1
    Peak = 1
    """

    return np.where(
        np.abs(t) <= 0.5,
        1 - np.abs(t) / 0.5,
        0.0
    )


# ============================================================
# 5. MSE CALCULATOR
# ============================================================

def calculate_mse(array1, array2):
    """
    Calculates magnitude MSE and wrapped phase MSE.
    """

    # --------------------------------------------------------
    # Magnitude MSE
    # --------------------------------------------------------

    mse_mag = np.mean(
        (np.abs(array1) - np.abs(array2)) ** 2
    )

    # --------------------------------------------------------
    # Wrapped phase difference
    # --------------------------------------------------------

    phase_diff = (
        np.angle(array1)
        - np.angle(array2)
        + np.pi
    ) % (2 * np.pi) - np.pi

    mse_phase = np.mean(
        phase_diff ** 2
    )

    return mse_mag, mse_phase


# ============================================================
# 6. PARAMETERS
# ============================================================

# ------------------------------------------------------------
# We don't need the full 2000-point grid for the convolution.
#
# Assignment specifically says approximately 400-500 points
# for the nested convolution.
# ------------------------------------------------------------

N = 500

# Time range
t_start = -2
t_end = 2

# Time samples
t = np.linspace(
    t_start,
    t_end,
    N
)

# Frequency range
f_start = -10
f_end = 10

# Number of frequency samples
M = 1000

f = np.linspace(
    f_start,
    f_end,
    M
)


# ============================================================
# 7. CREATE x1(t) AND x2(t)
# ============================================================

x1 = rectangular_pulse(t)

x2 = triangular_pulse(t)


# ============================================================
# 8. CALCULATE NUMERICAL CONVOLUTION
# ============================================================

print("Calculating numerical convolution...")
print(f"Number of time samples = {N}")
print("Please wait...\n")

start_time = time.perf_counter()

y = conv_numeric(
    t,
    x1,
    x2
)

end_time = time.perf_counter()

convolution_time = end_time - start_time


# ============================================================
# 9. CALCULATE Y(f)
#
# Y(f) = CFT{y(t)}
# ============================================================

print("Calculating CFT of numerical convolution...")

start_time = time.perf_counter()

Y = compute_cft(
    t,
    y,
    f
)

end_time = time.perf_counter()

cft_y_time = end_time - start_time


# ============================================================
# 10. CALCULATE X1(f)
# ============================================================

print("Calculating X1(f)...")

X1 = compute_cft(
    t,
    x1,
    f
)


# ============================================================
# 11. CALCULATE X2(f)
# ============================================================

print("Calculating X2(f)...")

X2 = compute_cft(
    t,
    x2,
    f
)


# ============================================================
# 12. CONVOLUTION THEOREM
#
# If
#
#       y(t) = x1(t) * x2(t)
#
# then
#
#       Y(f) = X1(f) X2(f)
#
# ============================================================

Y_theory = X1 * X2


# ============================================================
# 13. CALCULATE MSE
# ============================================================

mse_mag, mse_phase = calculate_mse(
    Y,
    Y_theory
)


# ============================================================
# 14. MASKED PHASE MSE
#
# Phase is unreliable when magnitude is close to zero.
# ============================================================

threshold = 1e-6 * np.max(
    np.abs(Y_theory)
)

mask = (
    np.abs(Y_theory) > threshold
)

phase_difference = (
    np.angle(Y[mask])
    - np.angle(Y_theory[mask])
    + np.pi
) % (2 * np.pi) - np.pi

masked_phase_mse = np.mean(
    phase_difference ** 2
)


# ============================================================
# 15. PRINT RESULTS
# ============================================================

print("\n")
print("===================================================")
print("       CFT CONVOLUTION THEOREM RESULTS")
print("===================================================")

print(f"Number of time samples : {N}")
print(f"Number of frequency samples : {M}")

print("\nRuntime:")
print(
    f"Numerical convolution : "
    f"{convolution_time:.6f} seconds"
)

print(
    f"CFT of y(t)           : "
    f"{cft_y_time:.6f} seconds"
)

print(
    f"Total main computation : "
    f"{convolution_time + cft_y_time:.6f} seconds"
)

print("\nMSE:")
print(
    f"Magnitude MSE         : "
    f"{mse_mag:.10e}"
)

print(
    f"Raw Phase MSE         : "
    f"{mse_phase:.10e}"
)

print(
    f"Masked Phase MSE      : "
    f"{masked_phase_mse:.10e}"
)

print(
    f"Phase points used     : "
    f"{np.sum(mask)} / {len(f)}"
)

print("===================================================")


# ============================================================
# 16. PLOT x1(t)
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    t,
    x1
)

plt.title(r"$x_1(t)$ - Rectangular Pulse")
plt.xlabel("Time t")
plt.ylabel("Amplitude")
plt.grid(True)

plt.tight_layout()


# ============================================================
# 17. PLOT x2(t)
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    t,
    x2
)

plt.title(r"$x_2(t)$ - Triangular Pulse")
plt.xlabel("Time t")
plt.ylabel("Amplitude")
plt.grid(True)

plt.tight_layout()


# ============================================================
# 18. PLOT y(t) = x1(t) * x2(t)
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    t,
    y
)

plt.title(r"$y(t) = x_1(t) * x_2(t)$")
plt.xlabel("Time t")
plt.ylabel("Amplitude")
plt.grid(True)

plt.tight_layout()


# ============================================================
# 19. PLOT |Y(f)| VS |X1(f)X2(f)|
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    f,
    np.abs(Y),
    label=r"$|Y(f)|$ from numerical convolution"
)

plt.plot(
    f,
    np.abs(Y_theory),
    "--",
    label=r"$|X_1(f)X_2(f)|$"
)

plt.title("CFT Convolution Theorem - Magnitude")
plt.xlabel("Frequency f")
plt.ylabel("Magnitude")

plt.legend()
plt.grid(True)

plt.tight_layout()


# ============================================================
# 20. PLOT REAL PART
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    f,
    np.real(Y),
    label=r"Re$\{Y(f)\}$"
)

plt.plot(
    f,
    np.real(Y_theory),
    "--",
    label=r"Re$\{X_1(f)X_2(f)\}$"
)

plt.title("Real Part Comparison")
plt.xlabel("Frequency f")
plt.ylabel("Real Part")

plt.legend()
plt.grid(True)

plt.tight_layout()


# ============================================================
# 21. PLOT IMAGINARY PART
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    f,
    np.imag(Y),
    label=r"Im$\{Y(f)\}$"
)

plt.plot(
    f,
    np.imag(Y_theory),
    "--",
    label=r"Im$\{X_1(f)X_2(f)\}$"
)

plt.title("Imaginary Part Comparison")
plt.xlabel("Frequency f")
plt.ylabel("Imaginary Part")

plt.legend()
plt.grid(True)

plt.tight_layout()


# ============================================================
# 22. PLOT PHASE
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    f,
    np.angle(Y),
    label="Numerical convolution"
)

plt.plot(
    f,
    np.angle(Y_theory),
    "--",
    label=r"$X_1(f)X_2(f)$"
)

plt.title("Phase Comparison")
plt.xlabel("Frequency f")
plt.ylabel("Phase (radians)")

plt.legend()
plt.grid(True)

plt.tight_layout()


# ============================================================
# 23. SHOW ALL FIGURES
# ============================================================

plt.show()