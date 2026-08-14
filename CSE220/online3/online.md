# Signals & Systems: Exam Prep Master Guide
**Exam Date:** Saturday, August 15th
**Focus:** Fourier Series, CFT, and NumPy Implementations

---

### Module 1: The Numerical Engine

Before manipulating Fourier space, you must master the discrete building blocks. In Python, signals are arrays, and we rely heavily on Euler's formula to represent rotating vectors:
$$e^{j\theta} = \cos(\theta) + j\sin(\theta)$$

#### 1. Complex Numbers in NumPy
NumPy handles complex arithmetic natively. The imaginary unit is strictly written as `1j`. 

```python
import numpy as np

# Define a discrete time array
t = np.linspace(0, 2 * np.pi, 100)

# Create a complex signal: f(t) = e^(-t) + j*sin(t)
signal = np.exp(-t) + 1j * np.sin(t)

# Extract components instantly
real_part = np.real(signal)   # Extracts e^(-t)
imag_part = np.imag(signal)   # Extracts sin(t)
magnitude = np.abs(signal)    # sqrt(real^2 + imag^2)
phase = np.angle(signal)      # arctan(imag / real)
```

#### 2. Numerical Integration (`np.trapezoid`)
To approximate continuous integration $\int f(t)dt$ on discrete arrays, we calculate the area of trapezoids between data points.

```python
t = np.linspace(0, 5, 1000)
f_t = np.exp(-t)

# Integrate using np.trapezoid
# arg1: the y-values (function output)
# arg2: the x-values (time/spatial axis)
area = np.trapezoid(f_t, t) 
print(f"Area: {area}") # Evaluates to approx 0.993
```
*Note:* `np.trapezoid` integrates real and imaginary arrays simultaneously, returning a single complex scalar.

#### 3. Numerical Differentiation (`np.gradient`)
Instead of standard difference arrays (`np.diff`) which reduce the array size by 1, use `np.gradient`. It applies the Central Difference theorem to maintain the exact shape of your arrays, which is critical for pointwise operations.

```python
t = np.linspace(0, 2 * np.pi, 100)
f_t = np.sin(t)

# Calculate the exact time step
dt = t[1] - t[0]

# Calculate the derivative: d/dt sin(t) = cos(t)
derivative = np.gradient(f_t, dt)
```

---

### Module 2: Fourier Series Deep Dive

The Fourier Series is used exclusively for **periodic signals** (signals that repeat forever with a fundamental period $T$).

#### 1. The Core Theory & Orthogonality
Any periodic function $f(t)$ can be represented as an infinite sum of rotating complex exponentials:
$$f(t) = \sum_{n=-\infty}^{\infty} c_n e^{jn\omega_0 t}$$

Where $\omega_0 = \frac{2\pi}{T}$ is the fundamental frequency. 

**The Orthogonality Principle:** 
How do we isolate a single coefficient $c_m$? We use orthogonality. If you integrate the product of two complex exponentials with different integer frequencies over one full period, the result is exactly 0.
$$\int_{0}^{T} e^{jn\omega_0 t} \cdot e^{-jm\omega_0 t} dt = \begin{cases} 0 & \text{if } n \neq m \\ T & \text{if } n = m \end{cases}$$

**Deriving the Coefficient Formula:**
1. Multiply both sides of the Fourier Series by $e^{-jm\omega_0 t}$.
2. Integrate both sides over the period $T$.
3. Because of orthogonality, every single term in the infinite sum collapses to 0 *except* where $n = m$.
4. You are left with: $\int_{0}^{T} f(t) e^{-jm\omega_0 t} dt = c_m \cdot T$.
5. Divide by $T$ to isolate the coefficient:
$$c_n = \frac{1}{T}\int_{0}^{T} f(t)e^{-jn\omega_0 t} dt$$

#### 2. Analytical Examples: $\cos(t)$ and $\sin(t)$
You don't always need to integrate. Using Euler's identity, you can find the coefficients of basic trigonometric functions instantly (an $O(1)$ mental operation).

**Example: $f(t) = \cos(t)$**
*   Euler expansion: $\cos(t) = \frac{1}{2}e^{jt} + \frac{1}{2}e^{-jt}$
*   Match this to the Fourier Series sum $\sum c_n e^{jnt}$.
*   **Result:** $c_1 = 0.5$, $c_{-1} = 0.5$, and all other $c_n = 0$.

**Example: $f(t) = \sin(t)$**
*   Euler expansion: $\sin(t) = \frac{e^{jt} - e^{-jt}}{2j} = -\frac{1}{2}j e^{jt} + \frac{1}{2}j e^{-jt}$
*   **Result:** $c_1 = -0.5j$, $c_{-1} = 0.5j$, and all other $c_n = 0$.

#### 3. Analytical Example: The Square Wave using $u(t)$
Let's introduce the **Unit Step Function, $u(t)$**. 
*   $u(t) = 1$ for $t \ge 0$
*   $u(t) = 0$ for $t < 0$

We can use $u(t)$ to mathematically draw a rectangular pulse. Let's define a periodic square wave $f(t)$ with period $T=2\pi$. For one period from $-\pi$ to $\pi$, the signal is $1$ between $-\frac{\pi}{2}$ and $\frac{\pi}{2}$, and $0$ elsewhere.

Using step functions, a single pulse is: 
$$f(t) = u\left(t + \frac{\pi}{2}\right) - u\left(t - \frac{\pi}{2}\right)$$

Let's derive $c_n$ analytically:
$$c_n = \frac{1}{2\pi} \int_{-\pi}^{\pi} f(t) e^{-jnt} dt = \frac{1}{2\pi} \int_{-\pi/2}^{\pi/2} 1 \cdot e^{-jnt} dt$$
$$c_n = \frac{1}{2\pi} \left[ \frac{e^{-jnt}}{-jn} \right]_{-\pi/2}^{\pi/2} = \frac{1}{-j2\pi n} \left( e^{-jn\pi/2} - e^{jn\pi/2} \right)$$

Using Euler's identity ($\sin(\theta) = \frac{e^{j\theta} - e^{-j\theta}}{2j}$), this simplifies beautifully into a `sinc` function:
$$c_n = \frac{\sin(n\pi/2)}{n\pi}$$

#### 4. NumPy Implementation: Square Wave Fourier Series
If you face a complex periodic signal on the exam that you cannot solve analytically, here is the exact code to compute its coefficients numerically.

```python
import numpy as np
import matplotlib.pyplot as plt

# 1. Setup the time array and parameters
T = 2 * np.pi
omega_0 = 2 * np.pi / T
t = np.linspace(-T/2, T/2, 1000)

# 2. Build the Square Wave using NumPy's version of u(t)
# np.where(condition, true_value, false_value)
u_shifted_left = np.where(t >= -np.pi/2, 1, 0)
u_shifted_right = np.where(t >= np.pi/2, 1, 0)
square_wave = u_shifted_left - u_shifted_right

# 3. Calculate c_n for harmonics n from -5 to 5
N = 5
coeffs = {}

for n in range(-N, N + 1):
    integrand = square_wave * np.exp(-1j * n * omega_0 * t)
    # Divide by T for the forward Series calculation
    c_n = np.trapezoid(integrand, t) / T 
    coeffs[n] = c_n

# Print the magnitude of c_1
print(f"c_1 numerical: {np.abs(coeffs[1]):.4f}")
print(f"c_1 analytical (1/pi): {1/np.pi:.4f}")
```


### Module 3: Continuous Fourier Transform (CFT) Fundamentals

When a signal stops repeating and becomes a single, aperiodic event (like a single sudden pulse or a decaying exponential curve), the fundamental period stretches to infinity ($T \to \infty$). The discrete sum of the Fourier Series evolves into a continuous integral over all time.

#### 1. The Core CFT Equations
*   **Forward Transform (Time Domain to Frequency Domain):**
    $$F(\omega) = \int_{-\infty}^{\infty} f(t) e^{-j\omega t} dt$$
*   **Inverse Transform (Frequency Domain to Time Domain):**
    $$f(t) = \frac{1}{2\pi} \int_{-\infty}^{\infty} F(\omega) e^{j\omega t} d\omega$$
    *(Note the $\frac{1}{2\pi}$ factor when integrating with respect to angular frequency $\omega$.)*

#### 2. The Dirac Delta Function, $\delta(t)$
The Delta function is a purely mathematical construct representing an infinitely narrow, infinitely tall spike at $t=0$, with a total area of exactly 1.
*   $\delta(t) = 0$ for $t \neq 0$
*   $\int_{-\infty}^{\infty} \delta(t) dt = 1$

**The Sifting Property (Crucial for Analytical Exams):**
If you multiply any function by a shifted delta function and integrate, the delta function "sifts" out the value of that function at the exact moment of the shift.
$$\int_{-\infty}^{\infty} f(t) \delta(t - t_0) dt = f(t_0)$$

**CFT of $\delta(t)$:**
Using the sifting property, we evaluate the CFT integral at $t=0$:
$$F(\omega) = \int_{-\infty}^{\infty} \delta(t) e^{-j\omega t} dt = e^{-j\omega(0)} = 1$$
*(Physical meaning: A perfectly sharp impulse in time contains infinite, equal energy across all possible frequencies).*

#### 3. The Unit Step Function, $u(t)$
$u(t) = 1$ for $t \ge 0$, and $0$ for $t < 0$.
Because the step function does not decay back to zero as $t \to \infty$, its CFT cannot be computed using a simple bounded integral. It requires generalized function theory, utilizing the delta function to represent its infinite DC average (zero frequency).

**CFT of $u(t)$:**
$$F\{u(t)\} = \pi\delta(\omega) + \frac{1}{j\omega}$$

#### 4. NumPy Implementation: The 1D CFT and Approximating Delta
Since arrays cannot represent infinity, we approximate $\delta(t)$ as a very narrow rectangular pulse where the `height` $\times$ `width` equals 1. We approximate the infinite integration bounds by using a sufficiently large, finite time array.

```python
import numpy as np

# 1. Setup a large time window to approximate [-infinity, infinity]
t = np.linspace(-10, 10, 2000)
dt = t[1] - t[0]

# 2. Approximate Dirac Delta at t=0
# It must have an area of 1 (height = 1/dt, width = dt)
delta_t = np.where(np.abs(t) < dt/2, 1/dt, 0)

# 3. Setup a frequency axis
omega = np.linspace(-10, 10, 500)
F_omega = np.zeros_like(omega, dtype=complex)

# 4. Compute the 1D CFT numerically 
# This is the 1D equivalent of Phase 1 from your offline assignment
for i, w in enumerate(omega):
    integrand = delta_t * np.exp(-1j * w * t)
    # Integrate over the time axis
    F_omega[i] = np.trapezoid(integrand, t)

# The result F_omega will be approx 1 + 0j across all frequencies
print(f"CFT of delta at w=5: {F_omega[np.argmin(np.abs(omega-5))]:.4f}")
```

### Module 4: Core CFT Properties

In an exam setting, you rarely compute the Fourier Transform of a complex signal from scratch. You identify a basic signal (like $e^{-at}u(t)$ or $\delta(t)$), find its standard transform, and then apply these properties to account for modifications.

#### 1. Linearity
If you have a signal that is a sum of multiple parts, you can transform each part individually and add the results.
*   **Time Domain:** $a f(t) + b g(t)$
*   **Frequency Domain:** $a F(\omega) + b G(\omega)$

#### 2. Time Shifting
Delaying or advancing a signal in time does not change its frequency content (its magnitude), it only shifts its phase. A time shift becomes a complex exponential multiplier in the frequency domain.
*   **Time Domain:** $f(t - t_0)$ *(Shift right by $t_0$)*
*   **Frequency Domain:** $F(\omega) e^{-j\omega t_0}$

**Example:** What is the CFT of $\delta(t - 3)$?
We know $F\{\delta(t)\} = 1$. Applying the shift property with $t_0 = 3$:
$$F\{\delta(t - 3)\} = 1 \cdot e^{-j\omega(3)} = e^{-j3\omega}$$

#### 3. Time Reversal
Flipping a signal horizontally in the time domain flips its frequency spectrum horizontally.
*   **Time Domain:** $f(-t)$
*   **Frequency Domain:** $F(-\omega)$

#### 4. Time Scaling (Compression and Expansion)
This is a critical physical property: **If you compress a signal in time, it expands in the frequency domain** (and vice-versa). A fast, short pulse requires highly spread-out, high-frequency waves to build it.
*   **Time Domain:** $f(at)$
*   **Frequency Domain:** $\frac{1}{|a|} F\left(\frac{\omega}{a}\right)$

**Example:** If $f(2t)$ is a time-compressed signal (runs twice as fast), its spectrum becomes $\frac{1}{2} F\left(\frac{\omega}{2}\right)$, meaning the frequencies are stretched out twice as wide.

---

### Module 5: Advanced CFT Operations

These operations define how calculus and algebraic modifications in the time domain map to algebraic multipliers in the frequency domain.

#### 1. Differentiation in Time
Taking the derivative of a signal in the time domain is equivalent to multiplying its frequency spectrum by $j\omega$. This emphasizes high frequencies (which makes sense, as derivatives highlight sharp edges and rapid changes).
*   **Time Domain:** $\frac{d}{dt}f(t)$
*   **Frequency Domain:** $j\omega \cdot F(\omega)$

**Example:** Find the CFT of the derivative of the unit step function, $\frac{d}{dt}u(t)$.
Mathematically, the derivative of a step function is an infinite spike: $\delta(t)$.
Let's prove it using the property. We know $F\{u(t)\} = \pi\delta(\omega) + \frac{1}{j\omega}$.
Multiply by $j\omega$:
$$j\omega \left[ \pi\delta(\omega) + \frac{1}{j\omega} \right] = j\omega\pi\delta(\omega) + 1$$
Because $\delta(\omega)$ is only non-zero at $\omega=0$, multiplying it by $\omega$ forces it to $0$. Thus, the first term vanishes, leaving exactly $1$, which is indeed the CFT of $\delta(t)$.

#### 2. Integration in Time
Integrating a signal in the time domain divides its frequency spectrum by $j\omega$ (acting as a low-pass filter, smoothing the signal), but we must also add a DC offset term in case the integral accumulates infinite area.
*   **Time Domain:** $\int_{-\infty}^{t} f(\tau) d\tau$
*   **Frequency Domain:** $\frac{1}{j\omega}F(\omega) + \pi F(0)\delta(\omega)$

#### 3. Multiplication by $t$ (The Frequency Derivative)
If a signal is multiplied by $t$ in the time domain, you take the derivative of its frequency spectrum and multiply by $j$.
*   **Time Domain:** $t \cdot f(t)$
*   **Frequency Domain:** $j \frac{d}{d\omega} F(\omega)$

#### 4. Frequency Shifting (Modulation)
Multiplying a signal by a complex exponential in the time domain shifts its entire frequency spectrum. This is the mathematical foundation of AM/FM radio and telecommunications.
*   **Time Domain:** $f(t) e^{j\omega_0 t}$
*   **Frequency Domain:** $F(\omega - \omega_0)$

**NumPy Proof of Frequency Shifting:**
```python
import numpy as np
import matplotlib.pyplot as plt

t = np.linspace(-10, 10, 2001)
dt = t[1] - t[0]

# Base signal: A wide rectangular pulse (low frequencies)
f_t = np.where(np.abs(t) < 2, 1, 0) 

# Modulate the signal by multiplying it by a high-frequency carrier (w0 = 15)
omega_0 = 15
f_t_modulated = f_t * np.cos(omega_0 * t) # cos(wt) contains e^(jwt) and e^(-jwt)

# If you were to plot the CFT of f_t_modulated, you would see the 
# original low-frequency spectrum perfectly shifted to center around +15 and -15.
```
### CFT Mathematical Proofs: The Master Arsenal

For your online exam, you must be able to prove *why* these properties work. Every single proof relies on standard integral calculus techniques: **U-Substitution** or **Integration by Parts**. 

The fundamental definition we will use for every proof is:
$$F(\omega) = \int_{-\infty}^{\infty} f(t) e^{-j\omega t} dt$$

---

#### 1. Time Shifting Property (Detailed Breakdown)
**Statement:** $F\{f(t - t_0)\} = F(\omega) e^{-j\omega t_0}$

**Proof via U-Substitution:**
Let's plug the shifted signal into the fundamental definition:
$$F\{f(t - t_0)\} = \int_{-\infty}^{\infty} f(t - t_0) e^{-j\omega t} dt$$

1.  **The Substitution:** Let $\tau = t - t_0$. 
    *   This means $t = \tau + t_0$.
    *   Taking the derivative, $dt = d\tau$.
    *   Since the bounds are infinity, shifting them by a constant $t_0$ leaves them at $-\infty$ and $\infty$.
2.  **Plug into the integral:**
    $$= \int_{-\infty}^{\infty} f(\tau) e^{-j\omega (\tau + t_0)} d\tau$$
3.  **Expand the exponent:**
    $$= \int_{-\infty}^{\infty} f(\tau) e^{-j\omega \tau} e^{-j\omega t_0} d\tau$$
4.  **Extract the constant:** The term $e^{-j\omega t_0}$ has no $\tau$ in it, so it acts as a constant with respect to the integral. Pull it out front:
    $$= e^{-j\omega t_0} \left[ \int_{-\infty}^{\infty} f(\tau) e^{-j\omega \tau} d\tau \right]$$
5.  **Identify the definition:** The bracketed integral is exactly the definition of the original Fourier Transform $F(\omega)$ (just with a dummy variable $\tau$ instead of $t$).
    $$= e^{-j\omega t_0} F(\omega)$$
*(Q.E.D.)*

---

#### 2. Linearity Property
**Statement:** $F\{a f(t) + b g(t)\} = aF(\omega) + bG(\omega)$

**Proof:**
$$= \int_{-\infty}^{\infty} [a f(t) + b g(t)] e^{-j\omega t} dt$$
By the distributive property of integrals, split it into two:
$$= \int_{-\infty}^{\infty} a f(t) e^{-j\omega t} dt + \int_{-\infty}^{\infty} b g(t) e^{-j\omega t} dt$$
Pull out the scalar constants $a$ and $b$:
$$= a \int_{-\infty}^{\infty} f(t) e^{-j\omega t} dt + b \int_{-\infty}^{\infty} g(t) e^{-j\omega t} dt$$
$$= aF(\omega) + bG(\omega)$$
*(Q.E.D.)*

---

#### 3. Time Reversal Property
**Statement:** $F\{f(-t)\} = F(-\omega)$

**Proof:**
$$F\{f(-t)\} = \int_{-\infty}^{\infty} f(-t) e^{-j\omega t} dt$$
1. Let $\tau = -t$. Therefore, $t = -\tau$ and $dt = -d\tau$.
2. When $t = \infty$, $\tau = -\infty$. When $t = -\infty$, $\tau = \infty$.
$$= \int_{\infty}^{-\infty} f(\tau) e^{-j\omega (-\tau)} (-d\tau)$$
3. Use the negative sign from $-d\tau$ to flip the integral bounds back to normal:
$$= \int_{-\infty}^{\infty} f(\tau) e^{j\omega \tau} d\tau$$
4. Rewrite the positive exponent to match the standard definition format:
$$= \int_{-\infty}^{\infty} f(\tau) e^{-j(-\omega) \tau} d\tau$$
This is exactly the definition of the CFT, but evaluated at $-\omega$ instead of $\omega$.
$$= F(-\omega)$$
*(Q.E.D.)*

---

#### 4. Time Scaling Property
**Statement:** $F\{f(at)\} = \frac{1}{|a|} F\left(\frac{\omega}{a}\right)$

**Proof:**
$$F\{f(at)\} = \int_{-\infty}^{\infty} f(at) e^{-j\omega t} dt$$
Let $\tau = at$. Therefore, $t = \frac{\tau}{a}$ and $dt = \frac{d\tau}{a}$.

*Case 1: $a > 0$ (Bounds stay the same)*
$$= \int_{-\infty}^{\infty} f(\tau) e^{-j\omega (\tau/a)} \frac{d\tau}{a} = \frac{1}{a} \int_{-\infty}^{\infty} f(\tau) e^{-j(\omega/a)\tau} d\tau = \frac{1}{a} F\left(\frac{\omega}{a}\right)$$

*Case 2: $a < 0$ (Bounds flip)*
The limits swap to $\int_{\infty}^{-\infty}$. We use the negative sign from $a$ (since $a$ is negative, $a = -|a|$) to flip the bounds back.
Combined into a single rule, the scalar multiplier outside is always absolute:
$$= \frac{1}{|a|} F\left(\frac{\omega}{a}\right)$$
*(Q.E.D.)*

---

#### 5. Differentiation in Time
**Statement:** $F\left\{\frac{d}{dt}f(t)\right\} = j\omega F(\omega)$

**Proof via Inverse Transform:**
It is much easier to prove this using the Inverse CFT equation:
$$f(t) = \frac{1}{2\pi} \int_{-\infty}^{\infty} F(\omega) e^{j\omega t} d\omega$$
Take the derivative with respect to $t$ on both sides:
$$\frac{d}{dt} f(t) = \frac{d}{dt} \left[ \frac{1}{2\pi} \int_{-\infty}^{\infty} F(\omega) e^{j\omega t} d\omega \right]$$
Move the derivative inside the integral (since the integral is with respect to $\omega$, not $t$):
$$= \frac{1}{2\pi} \int_{-\infty}^{\infty} F(\omega) \left[ \frac{d}{dt} e^{j\omega t} \right] d\omega$$
$$= \frac{1}{2\pi} \int_{-\infty}^{\infty} F(\omega) (j\omega e^{j\omega t}) d\omega$$
Group $(j\omega F(\omega))$ together:
$$= \frac{1}{2\pi} \int_{-\infty}^{\infty} [j\omega F(\omega)] e^{j\omega t} d\omega$$
Looking at the Inverse CFT structure, the term in the brackets is exactly the frequency spectrum of the left side.
$$F\left\{\frac{d}{dt}f(t)\right\} = j\omega F(\omega)$$
*(Q.E.D.)*

---

#### 6. Multiplication by t (Frequency Differentiation)
**Statement:** $F\{t \cdot f(t)\} = j \frac{d}{d\omega} F(\omega)$

**Proof:**
Start with the standard forward CFT equation:
$$F(\omega) = \int_{-\infty}^{\infty} f(t) e^{-j\omega t} dt$$
Take the derivative of both sides with respect to $\omega$:
$$\frac{d}{d\omega} F(\omega) = \frac{d}{d\omega} \int_{-\infty}^{\infty} f(t) e^{-j\omega t} dt$$
Move the derivative inside the integral:
$$= \int_{-\infty}^{\infty} f(t) \left[ \frac{d}{d\omega} e^{-j\omega t} \right] dt$$
$$= \int_{-\infty}^{\infty} f(t) (-jt e^{-j\omega t}) dt$$
Pull the $-j$ constant outside the integral:
$$\frac{d}{d\omega} F(\omega) = -j \int_{-\infty}^{\infty} [t f(t)] e^{-j\omega t} dt$$
The integral on the right is exactly the CFT of $[t f(t)]$. 
Divide both sides by $-j$ (which is the same as multiplying by $j$, since $\frac{1}{-j} = j$):
$$j \frac{d}{d\omega} F(\omega) = \int_{-\infty}^{\infty} [t f(t)] e^{-j\omega t} dt$$
$$F\{t \cdot f(t)\} = j \frac{d}{d\omega} F(\omega)$$
*(Q.E.D.)*

---

#### 7. Frequency Shifting (Modulation)
**Statement:** $F\{f(t) e^{j\omega_0 t}\} = F(\omega - \omega_0)$

**Proof:**
$$= \int_{-\infty}^{\infty} [f(t) e^{j\omega_0 t}] e^{-j\omega t} dt$$
Combine the exponents:
$$= \int_{-\infty}^{\infty} f(t) e^{-j\omega t + j\omega_0 t} dt$$
Factor out $-jt$:
$$= \int_{-\infty}^{\infty} f(t) e^{-j(\omega - \omega_0) t} dt$$
This is the exact definition of the CFT, but evaluated at the frequency $(\omega - \omega_0)$.
$$= F(\omega - \omega_0)$$
*(Q.E.D.)*


### Module 6: Fourier Series vs. Fourier Transform (The Conceptual Bridge)

Understanding the physical and mathematical differences between the Fourier Series (FS) and the Continuous Fourier Transform (CFT) is the most critical conceptual leap in signals and systems.

#### 1. The Fundamental Difference: Periodicity
*   **Fourier Series (FS):** Built strictly for **Periodic Signals** (signals that repeat perfectly every $T$ seconds). Because the signal repeats, it can only be built using frequencies that perfectly fit within that period. This creates a **Discrete Spectrum** (a line spectrum). The frequencies are integers multiples ($n$) of a fundamental frequency $\omega_0$.
*   **Fourier Transform (CFT):** Built for **Aperiodic Signals** (single events, pulses, decaying curves). Mathematically, we treat an aperiodic signal as a periodic signal where the period has stretched to infinity ($T \to \infty$). Because the period is infinite, *any* frequency can fit inside it. This creates a **Continuous Spectrum**.

#### 2. Why the Formulas Differ (Sums vs. Integrals)
**The Synthesis (Reconstruction) Formulas:**
*   **FS:** $f(t) = \sum_{n=-\infty}^{\infty} c_n e^{jn\omega_0 t}$
    *(We add up discrete, individual rotating vectors using a summation).*
*   **CFT:** $f(t) = \frac{1}{2\pi} \int_{-\infty}^{\infty} F(\omega) e^{j\omega t} d\omega$
    *(The frequencies are so densely packed that the summation turns into an integral over $\omega$).*

**The Analysis (Coefficient) Formulas and the $1/T$ factor:**
*   **FS:** $c_n = \frac{1}{T}\int_{0}^{T} f(t)e^{-jn\omega_0 t} dt$
    *(We are calculating the **Average Power** of a specific frequency over one finite period. Hence, we divide by $T$).*
*   **CFT:** $F(\omega) = \int_{-\infty}^{\infty} f(t) e^{-j\omega t} dt$
    *(Because $T = \infty$, dividing by $T$ would make everything zero. Instead, the CFT calculates the **Total Accumulated Energy** of that frequency across all of time. There is no division).*

#### 3. The Intuition of "Frequency Density"
Think of the Fourier Transform $F(\omega)$ not as an absolute amplitude, but as a **Density** (like probability density or mass density). 

If you ask, "What is the exact amplitude of a 50Hz sine wave in an aperiodic pulse?", the answer is mathematically zero. Because the spectrum is continuous, the energy is spread infinitely thin across all possible decimal frequencies. 

Instead, $F(\omega)$ represents the *density* of frequencies near $\omega$. To get the actual amplitude (mass), you must integrate that density over a small frequency band $d\omega$. This is exactly why the inverse CFT requires an integral: you are summing up slices of density ($F(\omega) d\omega$) to rebuild the physical signal.

---

### Module 7: Fourier Series Properties & Proofs

Just like the CFT, the discrete Fourier Series has properties that allow you to skip heavy integration. Let the FS coefficients of a periodic signal $x(t)$ be denoted as $c_n$, and the coefficients of $y(t)$ be $d_n$. Both share the period $T$ and fundamental frequency $\omega_0$.

#### 1. Linearity
**Statement:** The FS coefficients of $a x(t) + b y(t)$ are $a c_n + b d_n$.

**Proof:**
$$c_{n, new} = \frac{1}{T} \int_{0}^{T} [a x(t) + b y(t)] e^{-jn\omega_0 t} dt$$
Distribute the integral and the constant $1/T$:
$$= a \left( \frac{1}{T} \int_{0}^{T} x(t) e^{-jn\omega_0 t} dt \right) + b \left( \frac{1}{T} \int_{0}^{T} y(t) e^{-jn\omega_0 t} dt \right)$$
Substitute the definitions of $c_n$ and $d_n$:
$$= a c_n + b d_n$$
*(Q.E.D.)*

#### 2. Time Shifting
**Statement:** If you shift a periodic signal in time by $t_0$, the magnitude of its frequencies does not change, but every coefficient is multiplied by a phase shift $e^{-jn\omega_0 t_0}$. 
$x(t - t_0) \longleftrightarrow c_n e^{-jn\omega_0 t_0}$

**Proof:**
$$c_{n, new} = \frac{1}{T} \int_{0}^{T} x(t - t_0) e^{-jn\omega_0 t} dt$$
Let $\tau = t - t_0$. Then $t = \tau + t_0$, and $dt = d\tau$. 
Because $x(t)$ is periodic, shifting the integration bounds by a constant $t_0$ covers the exact same area as integrating from $0$ to $T$. We can safely keep the bounds as one full period $T$.
$$= \frac{1}{T} \int_{0}^{T} x(\tau) e^{-jn\omega_0 (\tau + t_0)} d\tau$$
Expand the exponent:
$$= \frac{1}{T} \int_{0}^{T} x(\tau) e^{-jn\omega_0 \tau} e^{-jn\omega_0 t_0} d\tau$$
Pull the constant $e^{-jn\omega_0 t_0}$ out of the integral:
$$= e^{-jn\omega_0 t_0} \left[ \frac{1}{T} \int_{0}^{T} x(\tau) e^{-jn\omega_0 \tau} d\tau \right]$$
The bracketed term is the original $c_n$:
$$= c_n e^{-jn\omega_0 t_0}$$
*(Q.E.D.)*

#### 3. Time Reversal
**Statement:** Flipping a signal in time flips the index of its coefficients.
$x(-t) \longleftrightarrow c_{-n}$

**Proof:**
$$c_{n, new} = \frac{1}{T} \int_{-T/2}^{T/2} x(-t) e^{-jn\omega_0 t} dt$$
*(Note: Integrating from $-T/2$ to $T/2$ is perfectly valid for periodic signals and makes this proof cleaner).*
Let $\tau = -t$. Then $dt = -d\tau$. The bounds flip from $T/2$ to $-T/2$.
$$= \frac{1}{T} \int_{T/2}^{-T/2} x(\tau) e^{-jn\omega_0 (-\tau)} (-d\tau)$$
Use the negative sign to flip the integration bounds back to normal:
$$= \frac{1}{T} \int_{-T/2}^{T/2} x(\tau) e^{jn\omega_0 \tau} d\tau$$
Rewrite the exponent to match the standard formula format:
$$= \frac{1}{T} \int_{-T/2}^{T/2} x(\tau) e^{-j(-n)\omega_0 \tau} d\tau$$
This is exactly the formula for the $(-n)$-th coefficient.
$$= c_{-n}$$
*(Q.E.D.)*

#### 4. Parseval's Theorem for Fourier Series (Conservation of Power)
**Statement:** The average power calculated in the time domain is exactly equal to the sum of the powers of the individual frequency harmonics in the frequency domain. Energy is perfectly conserved.

**Time Domain Power = Frequency Domain Power**
$$\frac{1}{T} \int_{0}^{T} |x(t)|^2 dt = \sum_{n=-\infty}^{\infty} |c_n|^2$$

**Proof:**
Start with the time-domain power, substituting one of the $x(t)$ terms with its Fourier Series expansion:
$$\text{Power} = \frac{1}{T} \int_{0}^{T} x(t) x^*(t) dt$$
*(Note: $x^*(t)$ is the complex conjugate. For real signals, $x(t) = x^*(t)$).*
Substitute $x(t) = \sum c_n e^{jn\omega_0 t}$:
$$= \frac{1}{T} \int_{0}^{T} \left( \sum_{n=-\infty}^{\infty} c_n e^{jn\omega_0 t} \right) x^*(t) dt$$
Swap the integral and the summation (valid for linear operations):
$$= \sum_{n=-\infty}^{\infty} c_n \left[ \frac{1}{T} \int_{0}^{T} x^*(t) e^{jn\omega_0 t} dt \right]$$
Look closely at the bracketed term. It is almost the formula for $c_n$, but with a conjugated signal and a positive exponent. By the properties of complex conjugates, this entire bracket is exactly $c_n^*$.
$$= \sum_{n=-\infty}^{\infty} c_n \cdot c_n^*$$
Since any complex number multiplied by its conjugate equals its magnitude squared ($z \cdot z^* = |z|^2$):
$$= \sum_{n=-\infty}^{\infty} |c_n|^2$$
*(Q.E.D.)*

### Module 8: FS Calculus Properties & The Compression Trap

Calculus in the frequency domain is incredibly elegant. Taking a derivative simply multiplies your coefficients by $jn\omega_0$, while integration divides them. However, applying these blindly without checking the boundary conditions is exactly how students lose marks.

#### 1. Differentiation in Time (Fourier Series)
**Statement:** Taking the derivative of a periodic signal multiplies its Fourier coefficients by $jn\omega_0$.
$x'(t) \longleftrightarrow jn\omega_0 c_n$

**Proof:**
Start with the synthesis equation:
$$x(t) = \sum_{n=-\infty}^{\infty} c_n e^{jn\omega_0 t}$$
Take the derivative of both sides with respect to $t$:
$$\frac{d}{dt}x(t) = \frac{d}{dt} \left[ \sum_{n=-\infty}^{\infty} c_n e^{jn\omega_0 t} \right]$$
Move the derivative inside the summation:
$$= \sum_{n=-\infty}^{\infty} c_n \left[ \frac{d}{dt} e^{jn\omega_0 t} \right]$$
$$= \sum_{n=-\infty}^{\infty} c_n (jn\omega_0 e^{jn\omega_0 t})$$
Group the terms to match the FS structure:
$$= \sum_{n=-\infty}^{\infty} (jn\omega_0 c_n) e^{jn\omega_0 t}$$
The new coefficient is exactly $(jn\omega_0 c_n)$. 
*(Q.E.D.)*

#### 2. Integration in Time (Fourier Series)
**Statement:** Integrating a periodic signal divides its coefficients by $jn\omega_0$. 
$\int x(t) dt \longleftrightarrow \frac{c_n}{jn\omega_0}$

**THE INTEGRATION TRAP:** This property is **ONLY VALID if $c_0 = 0$**. 
Why? The $c_0$ coefficient represents the DC offset (the average value of the signal). 
*   If $c_0 \neq 0$, it means your signal has a constant average height (like $f(t) = 5$). 
*   If you integrate a constant, you get a line with a slope ($\int 5 dt = 5t$). 
*   The function $5t$ ramps up to infinity! It is no longer a periodic signal, meaning it no longer has a Fourier Series. 
*   Furthermore, if you plug $n=0$ into the division property, you get $\frac{c_0}{0}$, which explodes to infinity. 
**Exam Rule:** Before integrating a Fourier Series, you must prove the average area over one period is zero.

**Proof (Assuming $c_0 = 0$):**
$$x(t) = \sum_{n \neq 0} c_n e^{jn\omega_0 t}$$
Integrate both sides:
$$\int x(t) dt = \int \left( \sum_{n \neq 0} c_n e^{jn\omega_0 t} \right) dt = \sum_{n \neq 0} c_n \int e^{jn\omega_0 t} dt$$
$$= \sum_{n \neq 0} \frac{c_n}{jn\omega_0} e^{jn\omega_0 t}$$
*(Q.E.D.)*

---

### The Ultimate Exam Trap: Compression + Differentiation

This is the classic "boss fight" question in Signals & Systems. 
You are given a signal $x(t)$, and you are asked to find the Fourier Transform (or Series) of the derivative of a compressed version of that signal: $F \left\{ \frac{d}{dt} [x(at)] \right\}$.

If you blindly apply the properties in the wrong order, you will get the wrong amplitude. 

#### The Flawed Logic (How Students Fail):
1.  "The derivative property says $F\{x'(t)\} = j\omega X(\omega)$."
2.  "The scaling property says I replace $\omega$ with $\frac{\omega}{a}$ and multiply by $\frac{1}{|a|}$."
3.  "Therefore, the answer is $\frac{1}{|a|} \left( j \frac{\omega}{a} \right) X\left(\frac{\omega}{a}\right)$."
**This is WRONG.** You just found the transform of $x'(at)$, NOT the transform of $\frac{d}{dt}[x(at)]$.

#### The Correct Logic (The Chain Rule):
Let's look at the time domain. By the Chain Rule of calculus:
$$\frac{d}{dt} [x(at)] = a \cdot x'(at)$$
When you compress a signal (e.g., $a=2$), you are squishing it horizontally. Because it covers the same vertical height in half the time, **its slope becomes twice as steep!** That amplitude multiplier $a$ is critical.

#### The Foolproof Mathematical Pipeline
To solve this without falling into the trap, **never do two properties at once**. Create an intermediate variable.

**Step 1:** Define the scaled signal.
Let $y(t) = x(at)$.
Apply the scaling property to get its spectrum:
$$Y(\omega) = \frac{1}{|a|} X\left(\frac{\omega}{a}\right)$$

**Step 2:** Apply the derivative property to the *new* signal.
We want $F \left\{ \frac{d}{dt} y(t) \right\}$.
According to the derivative property, we just multiply the new spectrum by $j\omega$:
$$= j\omega Y(\omega)$$

**Step 3:** Substitute $Y(\omega)$ back in.
$$= j\omega \left[ \frac{1}{|a|} X\left(\frac{\omega}{a}\right) \right]$$

#### Comparing the Results:
*   **Wrong (Blind Application):** $\frac{j\omega}{a|a|} X\left(\frac{\omega}{a}\right)$
*   **Right (Chain Rule/Pipeline):** $\frac{j\omega}{|a|} X\left(\frac{\omega}{a}\right)$

The wrong answer is off by a factor of $\frac{1}{a}$. If your compression factor was 2, your final amplitudes will be exactly half of what they should be. 

**Exam Takeaway:** Always let $y(t) = \text{inner function}$, apply the first property to find $Y(\omega)$, and then apply the outer mathematical operation to $y(t)$. Treat properties sequentially, not simultaneously.

#### 3. Time Scaling (Compression and Expansion) in Fourier Series
**Statement:** If you compress a periodic signal in time by a factor of $a$ ($a > 0$), its Fourier coefficients $c_n$ **remain exactly the same**. However, the signal's fundamental frequency increases by a factor of $a$ (from $\omega_0$ to $a\omega_0$). 
$x(at) \longleftrightarrow c_n$ *(with new fundamental frequency $\omega_0' = a\omega_0$)*

**The Conceptual Trap:** 
Students often try to apply the CFT scaling property $\left( \frac{1}{|a|} X\left(\frac{\omega}{a}\right) \right)$ to Fourier Series coefficients and divide their $c_n$ by $a$. This is fatally wrong. 
*   **Why?** In the Fourier Series, $c_n$ represents the *average* power of a harmonic over one period. If you compress a signal by playing it twice as fast ($a=2$), you also shrink the period by half ($T' = T/2$). Because both the signal and the averaging window shrink by the exact same proportion, the average power of each harmonic remains identical. The harmonics just spread further apart on the frequency axis!

**Proof:**
Let $x(t)$ have period $T$, fundamental frequency $\omega_0 = \frac{2\pi}{T}$, and coefficients $c_n$.
Let our new scaled signal be $y(t) = x(at)$. 
The new period is $T' = \frac{T}{a}$, and the new fundamental frequency is $\omega_0' = a\omega_0$.

Let's calculate the new coefficients, $d_n$, using the standard FS formula:
$$d_n = \frac{1}{T'} \int_{0}^{T'} y(t) e^{-jn\omega_0' t} dt$$

Substitute $y(t) = x(at)$, $T' = \frac{T}{a}$, and $\omega_0' = a\omega_0$:
$$d_n = \frac{a}{T} \int_{0}^{T/a} x(at) e^{-jn(a\omega_0) t} dt$$

Perform U-Substitution. Let $\tau = at$.
*   Then $d\tau = a \cdot dt$, which means $dt = \frac{d\tau}{a}$.
*   When $t = 0$, $\tau = 0$.
*   When $t = \frac{T}{a}$, $\tau = T$.

Plug these back into the integral:
$$d_n = \frac{a}{T} \int_{0}^{T} x(\tau) e^{-jn\omega_0 \tau} \left(\frac{d\tau}{a}\right)$$

The $a$ in the numerator and the $a$ in the denominator perfectly cancel each other out:
$$d_n = \frac{1}{T} \int_{0}^{T} x(\tau) e^{-jn\omega_0 \tau} d\tau$$

Look at the final equation. It is the exact definition of the original coefficient $c_n$.
$$d_n = c_n$$
*(Q.E.D.)*

### Practice Problem: The Ultimate FS Exam Trap
**Given:** A periodic signal $x(t)$ with Fourier Series coefficients $c_k$ and fundamental frequency $\omega_0$.
**Find:** The Fourier Series coefficients of $z(t) = \frac{d}{dt}[x(4 - 3t)]$.

---

#### The Solution (Sequential Pipeline)

To avoid the chain rule trap, we will build the signal step-by-step from the inside out, updating the coefficient and the fundamental frequency at each stage.

**Step 1: The Time Shift**
Let $v(t) = x(t + 4)$.
*   According to the time-shifting property, shifting by $+4$ multiplies the coefficient by $e^{jk\omega_0(4)}$.
*   **New Coefficient:** $d_k = c_k e^{j4k\omega_0}$
*   **Current Fundamental Frequency:** $\omega_0$ (unchanged).

**Step 2: The Time Scaling & Reversal**
Let $y(t) = v(-3t) = x(-3t + 4) = x(4 - 3t)$.
*   We are scaling by a factor of $a = -3$. 
*   **The Reversal:** The negative sign flips the frequency axis, meaning harmonic $k$ becomes harmonic $-k$. We must substitute $-k$ into our previous coefficient $d_k$.
*   **The Scaling Trap:** Remember, scaling in FS *does not change the amplitude*, but it **does multiply the fundamental frequency** by $|a|$. 
*   **New Coefficient:** $e_k = d_{-k} = c_{-k} e^{j4(-k)\omega_0} = c_{-k} e^{-j4k\omega_0}$
*   **New Fundamental Frequency:** $\omega_0' = |-3|\omega_0 = 3\omega_0$

**Step 3: The Differentiation**
Let $z(t) = \frac{d}{dt} y(t) = \frac{d}{dt}[x(4 - 3t)]$.
*   According to the FS differentiation property, we multiply the coefficient by $jk(\text{Fundamental Frequency})$. 
*   **The Final Trap:** You MUST use the *new* fundamental frequency ($\omega_0' = 3\omega_0$), not the original one!
*   **Final Coefficient:** $f_k = e_k \cdot (jk\omega_0')$
$$f_k = \left( c_{-k} e^{-j4k\omega_0} \right) \cdot (jk(3\omega_0))$$

Rearranging for the final, clean answer:
$$f_k = 3jk\omega_0 c_{-k} e^{-j4k\omega_0}$$

---

#### Verifying with the Time-Domain Chain Rule
Let's prove this is correct by doing the calculus in the time domain first.
By the Chain Rule:
$$\frac{d}{dt}[x(4 - 3t)] = -3 \cdot x'(4 - 3t)$$

1.  The coefficient for $x'(t)$ is $(jk\omega_0 c_k)$.
2.  Shift it by $+4$: $(jk\omega_0 c_k) e^{j4k\omega_0}$.
3.  Scale and reverse by $-3$: Substitute $-k$ into the whole thing. 
    $\rightarrow (j(-k)\omega_0 c_{-k}) e^{j4(-k)\omega_0} = -jk\omega_0 c_{-k} e^{-j4k\omega_0}$.
4.  Finally, multiply by the $-3$ from the chain rule:
    $-3 \cdot (-jk\omega_0 c_{-k} e^{-j4k\omega_0}) = 3jk\omega_0 c_{-k} e^{-j4k\omega_0}$.

Both methods yield the exact same result!

### Module 9: The CFT Scale-Derivative Paradox

**The Problem:** Find the CFT of $\frac{d}{dt}[x(at)]$.

Your instinct might be to combine the properties like this:
1. Scaling makes the spectrum $\frac{1}{|a|} X\left(\frac{\omega}{a}\right)$.
2. Differentiation means multiplying by $j\omega$.
3. Since I scaled, shouldn't $\omega$ change to $\omega/a$ everywhere? So the answer should be $\frac{1}{|a|} \left(j\frac{\omega}{a}\right) X\left(\frac{\omega}{a}\right)$.

**This is mathematically incorrect.** Here is why, proven two different ways.

#### Method 1: The Sequential Pipeline (The Safe Way)
Never apply two properties to the same variable at the same time. Build a new signal.

**Step 1:** Define the scaled signal. 
Let $y(t) = x(at)$. 
Its transform is $Y(\omega) = \frac{1}{|a|} X\left(\frac{\omega}{a}\right)$.

**Step 2:** Differentiate the *new* signal.
We are looking for $F\left\{\frac{d}{dt} y(t)\right\}$.
The absolute rule for differentiating *any* signal $y(t)$ is to multiply its spectrum by $j\omega$. It does not matter what is inside $Y(\omega)$. The $\omega$ in $j\omega$ belongs to the outer frequency axis, not the inner scaling.
$$F\{y'(t)\} = j\omega Y(\omega)$$

**Step 3:** Substitute $Y(\omega)$ back in.
$$= j\omega \left[ \frac{1}{|a|} X\left(\frac{\omega}{a}\right) \right] = \frac{j\omega}{|a|} X\left(\frac{\omega}{a}\right)$$

#### Method 2: The Time-Domain Chain Rule (The Proof)
Let's prove *why* Method 1 is correct by doing the calculus first, which will explain exactly where your missing $\omega/a$ went.

By the Chain Rule of Calculus:
$$\frac{d}{dt} [x(at)] = a \cdot x'(at)$$

Now, let's take the Fourier Transform of this result: $F\{a \cdot x'(at)\}$.

1.  Let $v(t) = x'(t)$. We know from the basic derivative property that $V(\omega) = j\omega X(\omega)$.
2.  Now apply the scaling property to $v(at)$:
    $$F\{v(at)\} = \frac{1}{|a|} V\left(\frac{\omega}{a}\right)$$
3.  Substitute our $V$ equation into this. **Here is where your intuition comes in!** Because we are substituting into $V$, we *do* replace the $\omega$ with $\omega/a$ inside the derivative multiplier:
    $$V\left(\frac{\omega}{a}\right) = \left( j\frac{\omega}{a} \right) X\left(\frac{\omega}{a}\right)$$
    So, $F\{v(at)\} = \frac{1}{|a|} \left( j\frac{\omega}{a} \right) X\left(\frac{\omega}{a}\right)$.
4.  But wait! We forgot the $a$ from the Chain Rule! 
    $F\{a \cdot v(at)\} = a \cdot \left[ \frac{1}{|a|} \left( j\frac{\omega}{a} \right) X\left(\frac{\omega}{a}\right) \right]$
5.  Look at what happens to the constants: The $a$ from the chain rule perfectly cancels the $a$ in the denominator of your $j(\omega/a)$ term!
    $$= a \cdot \frac{j\omega}{a |a|} X\left(\frac{\omega}{a}\right) = \frac{j\omega}{|a|} X\left(\frac{\omega}{a}\right)$$

#### The Core Takeaway
Your intuition was actually 100% correct—the derivative multiplier *does* momentarily become $j(\omega/a)$. But the Chain Rule spits out an extra $a$ multiplier in the time domain that perfectly cancels it out, leaving you with just $j\omega$. 

By using the **Sequential Pipeline** (Method 1), you automatically account for this cancellation without having to do the algebraic gymnastics of Method 2.