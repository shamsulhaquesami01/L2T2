import numpy as np
import matplotlib.pyplot as plt


# ============================================================
# 1. CFT CALCULATOR
# ============================================================

def compute_cft(t, x, f):
    t = np.asarray(t)
    x = np.asarray(x)
    f = np.asarray(f)
    exponential = np.exp(
        -1j * 2 * np.pi * f[:, None] * t[None, :]
    )
    integrand = exponential * x[None, :]
    X = np.trapezoid(integrand, t, axis=1)

    return X

def calculate_mse(array1, array2):
    """
    Standard Magnitude and Wrapped Phase MSE Calculator.
    """
    mse_mag = np.mean((np.abs(array1) - np.abs(array2))**2)
    phase_diff = (np.angle(array1) - np.angle(array2) + np.pi) % (2 * np.pi) - np.pi
    mse_phase = np.mean(phase_diff**2)
    return mse_mag, mse_phase


# Time interval
t_start = -8
t_end = 8
# Number of time samples
N = 3000
# Frequency shift
f0 = 3
# Frequency range
f_start = -10
f_end = 10
# Number of frequency samples
M = 1000

t = np.linspace(
    t_start,
    t_end,
    N
)

f = np.linspace(
    f_start,
    f_end,
    M
)

x1 = np.exp(-t ** 2)

x2 = np.where(
    np.abs(t) <= 1,
    1.0,
    0.0
)

y = (
    2 * x1
    + x2 * np.cos(2 * np.pi * f0 * t)
)

print("Calculating X1(f)...")
X1 = compute_cft(
    t,
    x1,
    f
)

print("Calculating X2(f)...")
X2 = compute_cft(
    t,
    x2,
    f
)

print("Calculating Y(f)...")

Y = compute_cft(
    t,
    y,
    f
)


print("Calculating shifted X2 spectra...")

X2_minus = compute_cft(
    t,
    x2,
    f - f0
)

X2_plus = compute_cft(
    t,
    x2,
    f + f0
)

Y_theory = (
    2 * X1
    + 0.5 * (X2_minus + X2_plus)
)

mse_mag, mse_phase = calculate_mse(
    Y,
    Y_theory
)


print("\n==========================================")
print("CFT LINEARITY + COSINE MODULATION RESULTS")
print("==========================================")

print(f"Magnitude MSE = {mse_mag:.10e}")
print(f"Phase MSE     = {mse_phase:.10e}")


magnitude_threshold = 1e-6 * np.max(np.abs(Y_theory))

mask = np.abs(Y_theory) > magnitude_threshold


phase_difference = (
    np.angle(Y[mask])
    - np.angle(Y_theory[mask])
    + np.pi
) % (2 * np.pi) - np.pi

masked_phase_mse = np.mean(
    phase_difference ** 2
)

print(
    f"Masked Phase MSE = {masked_phase_mse:.10e}"
)

print(
    f"Number of frequency points used for phase MSE: "
    f"{np.sum(mask)} / {len(f)}"
)

plt.figure(figsize=(12, 8))

plt.subplot(3, 1, 1)
plt.plot(t, x1)
plt.title(r"$x_1(t) = e^{-t^2}$")
plt.xlabel("Time t")
plt.ylabel("Amplitude")
plt.grid(True)


plt.subplot(3, 1, 2)
plt.plot(t, x2)
plt.title(r"$x_2(t)$ = rectangular pulse")
plt.xlabel("Time t")
plt.ylabel("Amplitude")
plt.grid(True)


plt.subplot(3, 1, 3)
plt.plot(t, y)
plt.title(
    r"$y(t)=2x_1(t)+x_2(t)\cos(2\pi f_0t)$"
)
plt.xlabel("Time t")
plt.ylabel("Amplitude")
plt.grid(True)

plt.tight_layout()

plt.figure(figsize=(12, 8))

plt.subplot(3, 1, 1)

plt.plot(
    f,
    np.abs(X1)
)

plt.title(r"$|X_1(f)|$")
plt.xlabel("Frequency f")
plt.ylabel("Magnitude")
plt.grid(True)


plt.subplot(3, 1, 2)

plt.plot(
    f,
    np.abs(X2)
)

plt.title(r"$|X_2(f)|$")
plt.xlabel("Frequency f")
plt.ylabel("Magnitude")
plt.grid(True)


plt.subplot(3, 1, 3)

plt.plot(
    f,
    np.abs(Y),
    label="Actual CFT"
)

plt.plot(
    f,
    np.abs(Y_theory),
    "--",
    label="Theoretical CFT"
)

plt.title(r"Comparison of $|Y(f)|$")
plt.xlabel("Frequency f")
plt.ylabel("Magnitude")
plt.legend()
plt.grid(True)

plt.tight_layout()


plt.figure(figsize=(12, 6))

plt.plot(
    f,
    np.real(Y),
    label="Actual Re{Y(f)}"
)

plt.plot(
    f,
    np.real(Y_theory),
    "--",
    label="Theoretical Re{Y(f)}"
)

plt.title("Real Part of Y(f)")
plt.xlabel("Frequency f")
plt.ylabel("Real Part")
plt.legend()
plt.grid(True)

plt.tight_layout()

plt.figure(figsize=(12, 6))

plt.plot(
    f,
    np.imag(Y),
    label="Actual Im{Y(f)}"
)

plt.plot(
    f,
    np.imag(Y_theory),
    "--",
    label="Theoretical Im{Y(f)}"
)

plt.title("Imaginary Part of Y(f)")
plt.xlabel("Frequency f")
plt.ylabel("Imaginary Part")
plt.legend()
plt.grid(True)

plt.tight_layout()


# ============================================================
# 16. PLOT PHASE
# ============================================================

plt.figure(figsize=(12, 6))

plt.plot(
    f,
    np.angle(Y),
    label="Actual Phase"
)

plt.plot(
    f,
    np.angle(Y_theory),
    "--",
    label="Theoretical Phase"
)

plt.title("Phase Comparison")
plt.xlabel("Frequency f")
plt.ylabel("Phase (radians)")
plt.legend()
plt.grid(True)

plt.tight_layout()


# ============================================================
# 17. DISPLAY ALL PLOTS
# ============================================================

plt.show()