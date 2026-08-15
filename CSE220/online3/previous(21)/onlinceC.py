import numpy as np
import matplotlib.pyplot as plt


# ============================================================
# CFT
# ============================================================

def compute_cft(t, x, f):

    t = np.asarray(t)
    x = np.asarray(x)
    f = np.asarray(f)

    exponential = np.exp(
        -1j * 2 * np.pi
        * f[:, None]
        * t[None, :]
    )

    integrand = (
        exponential
        * x[None, :]
    )

    X = np.trapezoid(
        integrand,
        t,
        axis=1
    )

    return X


# ============================================================
# GIVEN FUNCTION
# ============================================================

def given_function(t):

    return (
        2 * np.sin(14 * np.pi * t)
        - np.sin(2 * np.pi * t)
        * (
            4 * np.sin(2 * np.pi * t)
            * np.sin(14 * np.pi * t)
            - 1
        )
    )


# ============================================================
# TIME AXIS
# ============================================================

t = np.linspace(
    -2,
    2,
    2000
)


# ============================================================
# ORIGINAL SIGNAL
# ============================================================

x = given_function(t)


# ============================================================
# PLOT ORIGINAL
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(t, x)

plt.xlabel("t")
plt.ylabel("f(t)")

plt.title("Given Function")

plt.grid(True)

plt.xlim(-1, 1)

plt.show()


# ============================================================
# FREQUENCY AXIS
# ============================================================

f = np.linspace(
    -12,
    12,
    2000
)


# ============================================================
# CFT
# ============================================================

X = compute_cft(
    t,
    x,
    f
)


# ============================================================
# MAGNITUDE SPECTRUM
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    f,
    np.abs(X)
)

plt.xlabel("Frequency (Hz)")
plt.ylabel("|X(f)|")

plt.title("CFT Magnitude Spectrum")

plt.grid(True)

plt.show()


# ============================================================
# RECONSTRUCT USING FREQUENCIES FOUND FROM CFT
# ============================================================

# From the CFT peaks:
#
# f = 1 Hz
# f = 5 Hz
# f = 9 Hz

f1 = 1
f2 = 5
f3 = 9


reconstructed = (
    np.sin(2 * np.pi * f1 * t)
    + np.sin(2 * np.pi * f2 * t)
    + np.sin(2 * np.pi * f3 * t)
)


# ============================================================
# PLOT COMPARISON
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    t,
    x,
    label="Original"
)

plt.plot(
    t,
    reconstructed,
    "--",
    label="Reconstructed"
)

plt.xlabel("t")
plt.ylabel("Amplitude")

plt.title(
    "Original vs Reconstructed"
)

plt.xlim(-1, 1)

plt.grid(True)

plt.legend()

plt.show()


# ============================================================
# MSE
# ============================================================

mse = np.mean(
    (x - reconstructed) ** 2
)

print("MSE =", mse)