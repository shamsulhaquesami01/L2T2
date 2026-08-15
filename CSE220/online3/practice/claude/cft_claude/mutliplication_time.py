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
# NUMERICAL CONVOLUTION
# ============================================================

def conv_numeric(t, x1, x2):

    y = np.zeros(
        len(t),
        dtype=complex
    )

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


# ============================================================
# TIME AXIS
# ============================================================

t = np.linspace(
    -10,
    10,
    2000
)


# ============================================================
# DEFINE x(t) AND y(t)
# ============================================================

def signal_x(t):
    return np.exp(-t**2)


def signal_y(t):
    return np.exp(-(t - 2)**2)


x = signal_x(t)
y = signal_y(t)


# ============================================================
# TIME-DOMAIN MULTIPLICATION
#
# z(t) = x(t)y(t)
# ============================================================

z = x * y


# ============================================================
# FREQUENCY AXIS
# ============================================================

f = np.linspace(
    -5,
    5,
    2000
)


# ============================================================
# CFT OF x(t)
# ============================================================

X = compute_cft(
    t,
    x,
    f
)


# ============================================================
# CFT OF y(t)
# ============================================================

Y = compute_cft(
    t,
    y,
    f
)


# ============================================================
# ACTUAL RESULT
#
# CFT{x(t)y(t)}
# ============================================================

Z_actual = compute_cft(
    t,
    z,
    f
)


# ============================================================
# THEORETICAL RESULT
#
# X(f) * Y(f)
#
# IMPORTANT:
#
# X and Y are functions of f.
#
# Therefore f becomes the integration axis.
# ============================================================

Z_theory = conv_numeric(
    f,
    X,
    Y
)


# ============================================================
# ERROR
# ============================================================

mse = np.mean(
    np.abs(
        Z_actual - Z_theory
    ) ** 2
)

print("Complex MSE =", mse)


# ============================================================
# PLOT ORIGINAL SIGNALS
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    t,
    x,
    label="x(t)"
)

plt.plot(
    t,
    y,
    label="y(t)"
)

plt.xlabel("t")
plt.ylabel("Amplitude")
plt.title("Original Signals")
plt.grid(True)
plt.legend()

plt.show()


# ============================================================
# PLOT MULTIPLICATION
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    t,
    z,
    label="z(t) = x(t)y(t)"
)

plt.xlabel("t")
plt.ylabel("Amplitude")
plt.title("Multiplication in Time")
plt.grid(True)
plt.legend()

plt.show()


# ============================================================
# COMPARE CFT{x(t)y(t)}
# WITH
# X(f) * Y(f)
# ============================================================

plt.figure(figsize=(10, 5))

plt.plot(
    f,
    np.abs(Z_actual),
    label="|CFT{x(t)y(t)}|"
)

plt.plot(
    f,
    np.abs(Z_theory),
    "--",
    label="|X(f) * Y(f)|"
)

plt.xlabel("Frequency f")
plt.ylabel("Magnitude")
plt.title(
    "Multiplication in Time ↔ Convolution in Frequency"
)

plt.grid(True)
plt.legend()

plt.show()