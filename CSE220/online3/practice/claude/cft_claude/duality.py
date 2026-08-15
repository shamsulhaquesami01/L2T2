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

def calculate_mse(array1, array2):

    mse_mag = np.mean(
        (np.abs(array1) - np.abs(array2)) ** 2
    )

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
# ORIGINAL TIME AXIS
# ============================================================

t = np.linspace(
    -10,
    10,
    3000
)


# ============================================================
# ORIGINAL SIGNAL
# ============================================================

def signal(t):

    return np.exp(-t**2)


x = signal(t)

f = np.linspace(
    -5,
    5,
    3000
)


X = compute_cft(
    t,
    x,
    f
)


# ============================================================
# STEP 2:
# Treat X(f) as a new time-domain signal
#
# New "time" axis = f
# New signal = X
#
# Calculate:
#
# CFT{X(t)}
#
# numerically as:
#
# compute_cft(f, X, new_f)
# ============================================================

new_f = t.copy()

Y = compute_cft(
    f,
    X,
    new_f
)


# ============================================================
# STEP 3:
# Theoretical result
#
# Duality:
#
# x(t) <----> X(f)
#
# therefore:
#
# X(t) <----> x(-f)
#
# ============================================================

Y_theory = signal(-new_f)


# ============================================================
# STEP 4:
# MSE
# ============================================================

mse_mag, mse_phase = calculate_mse(
    Y,
    Y_theory
)

print("Magnitude MSE =", mse_mag)
print("Phase MSE     =", mse_phase)


# ============================================================
# ORIGINAL SIGNAL
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    t,
    x,
    label="x(t)"
)

plt.xlabel("t")
plt.ylabel("Amplitude")
plt.title("Original Signal")
plt.grid(True)
plt.legend()

plt.show()


# ============================================================
# X(f)
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    f,
    np.real(X),
    label="Re{X(f)}"
)

plt.xlabel("f")
plt.ylabel("Amplitude")
plt.title("Fourier Transform X(f)")
plt.grid(True)
plt.legend()

plt.show()


# ============================================================
# DUALITY VERIFICATION
#
# Actual:
# CFT{X(t)}
#
# Theory:
# x(-f)
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    new_f,
    np.real(Y),
    label="CFT{X(t)}"
)

plt.plot(
    new_f,
    Y_theory,
    "--",
    label="x(-f)"
)

plt.xlabel("f")
plt.ylabel("Amplitude")
plt.title("CFT Duality Property")
plt.grid(True)
plt.legend()

plt.show()