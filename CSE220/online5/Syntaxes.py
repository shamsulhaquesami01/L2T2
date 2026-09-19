"""CSE220 online syntax reference: essential Python, NumPy, FFT, sampling, and plotting patterns."""

# Run this file section by section while studying. Most examples also execute as one script.

import matplotlib.pyplot as plt
import numpy as np


# =============================================================================
# 1. BASIC PYTHON VALUES AND OPERATORS
# =============================================================================

integer_value = 5
float_value = 2.5
complex_value = 3 + 4j
text_value = "sampling"
boolean_value = True
nothing = None

addition = 7 + 3
subtraction = 7 - 3
multiplication = 7 * 3
normal_division = 7 / 3       # 2.333...
integer_division = 7 // 3      # 2
remainder = 7 % 3              # 1
power = 2**3                    # 8

equal = 5 == 5
not_equal = 5 != 3
less_than = 3 < 5
combined_condition = (3 < 5) and (10 > 2)
alternative_condition = (3 > 5) or (10 > 2)
opposite = not True


# =============================================================================
# 2. STRINGS AND FORMATTED OUTPUT
# =============================================================================

name = "Nyquist"
rate = 400
message = f"{name} test uses fs = {rate} Hz"
print(message)
print("rate:", rate)


# =============================================================================
# 3. LISTS, TUPLES, DICTIONARIES, AND SETS
# =============================================================================

frequencies = [30, 80, 140]
frequencies.append(200)
first = frequencies[0]
last = frequencies[-1]
middle = frequencies[1:3]      # indices 1 and 2; stop index is excluded
every_second = frequencies[::2]
reversed_list = frequencies[::-1]

point = (2, 5)                 # tuple: normally treated as fixed
x_coordinate, y_coordinate = point

report = {
    "fmax": 140,
    "nyquist_rate": 280,
    "safe": True,
}
safe = report["safe"]
report["fs"] = 400

unique_values = {10, 10, 20, 30}  # duplicates are removed


# =============================================================================
# 4. IF, ELIF, ELSE, AND CONDITIONAL EXPRESSIONS
# =============================================================================

fs = 400
fmax = 140

if fs > 2 * fmax:
    status = "safe"
elif fs == 2 * fmax:
    status = "boundary"
else:
    status = "aliasing possible"

short_status = "safe" if fs > 2 * fmax else "unsafe"


# =============================================================================
# 5. LOOPS, RANGE, ENUMERATE, ZIP, AND COMPREHENSIONS
# =============================================================================

for i in range(5):             # 0, 1, 2, 3, 4
    pass

for i in range(1, 6, 2):       # 1, 3, 5
    pass

for index, frequency in enumerate([30, 80, 140]):
    print(index, frequency)

for frequency, amplitude in zip([30, 80], [1.0, 0.5]):
    print(frequency, amplitude)

squares = [value**2 for value in range(5)]
even_squares = [value**2 for value in range(10) if value % 2 == 0]


# =============================================================================
# 6. FUNCTIONS, DEFAULT VALUES, MULTIPLE RETURNS, AND UNPACKING
# =============================================================================

def nyquist_report(frequencies, fs=1000):
    """Return multiple values as a tuple."""
    fmax = max(frequencies)
    return fmax, 2 * fmax, fs > 2 * fmax


maximum, nyquist_rate, is_safe = nyquist_report([30, 80, 140], fs=400)


def keyword_example(*, fs, duration):
    """The star forces fs and duration to be passed by name."""
    return int(round(fs * duration))


sample_count = keyword_example(fs=400, duration=1.0)


# =============================================================================
# 7. EXCEPTIONS AND INPUT VALIDATION
# =============================================================================

def validate_sampling_rate(fs):
    if fs <= 0:
        raise ValueError("fs must be positive")
    return fs


try:
    validate_sampling_rate(400)
except ValueError as error:
    print("invalid input:", error)


# =============================================================================
# 8. CONVERTING DATA TO NUMPY ARRAYS
# =============================================================================

a = np.array([1, 2, 3])
b = np.asarray([4, 5, 6], dtype=float)
c = np.asarray(a, dtype=np.float64)

print(a.shape)                 # (3,)
print(a.ndim)                  # 1
print(a.size)                  # 3
print(a.dtype)

integer_array = b.astype(np.int64)
complex_array = np.asarray([1, 2], dtype=np.complex128)


# =============================================================================
# 9. CREATING COMMON ARRAYS
# =============================================================================

zeros = np.zeros(5)
integer_zeros = np.zeros(5, dtype=int)
ones = np.ones(5)
constant = np.full(5, 7)
empty_for_later = np.empty(5)

indices = np.arange(5)                    # [0, 1, 2, 3, 4]
times = np.arange(0, 1, 0.25)             # stop value 1 is excluded
inclusive_grid = np.linspace(0, 1, 5)     # includes both endpoints

identity = np.eye(3)
diagonal = np.diag([1, 2, 3])


# =============================================================================
# 10. SAMPLING-TIME GRIDS
# =============================================================================

fs = 1000
duration = 1.0
N = int(round(fs * duration))
t = np.arange(N) / fs                     # exactly N samples

old_t = np.arange(3) / 2                  # samples at 0, 0.5, 1.0
new_t = np.linspace(old_t[0], old_t[-1], 9)


# =============================================================================
# 11. ARRAY INDEXING AND SLICING
# =============================================================================

x = np.array([10, 20, 30, 40, 50, 60])
first = x[0]
last = x[-1]
section = x[1:4]               # [20, 30, 40]
from_index_two = x[2:]
before_index_four = x[:4]
every_second = x[::2]
downsampled = x[::3]
reversed_x = x[::-1]

x_copy = x.copy()
x_copy[0] = 999
x_copy[1:3] = [111, 222]


# =============================================================================
# 12. TWO-DIMENSIONAL INDEXING
# =============================================================================

matrix = np.array([
    [1, 2, 3],
    [4, 5, 6],
])

element = matrix[1, 2]         # 6
first_row = matrix[0, :]
second_column = matrix[:, 1]
submatrix = matrix[:, 1:]


# =============================================================================
# 13. BOOLEAN MASKS AND CONDITIONAL SELECTION
# =============================================================================

values = np.array([-3, -1, 0, 2, 5])
positive_mask = values > 0
positive_values = values[positive_mask]
inside_band = values[(values >= -1) & (values <= 2)]

replaced = np.where(values < 0, 0, values)
indices_of_positive = np.flatnonzero(values > 0)
any_negative = np.any(values < 0)
all_finite = np.all(np.isfinite(values))


# =============================================================================
# 14. ELEMENTWISE NUMPY MATHEMATICS
# =============================================================================

x = np.array([1.0, 2.0, 3.0])
y = np.array([4.0, 5.0, 6.0])

elementwise_sum = x + y
elementwise_product = x * y
elementwise_power = x**2
square_root = np.sqrt(x)
absolute_value = np.abs(np.array([-2, 3]))
rounded = np.round([1.2, 2.8])
nearest_integer = np.rint([1.2, 2.8]).astype(np.int64)

angles = np.array([0, np.pi / 2, np.pi])
sines = np.sin(angles)
cosines = np.cos(angles)
complex_exponential = np.exp(1j * angles)

minimum_pairwise = np.minimum(x, y)
maximum_pairwise = np.maximum(x, y)
clipped = np.clip(np.array([-2, 3, 10]), 0, 5)


# =============================================================================
# 15. AGGREGATION AND AXIS
# =============================================================================

matrix = np.array([
    [1, 2, 3],
    [4, 5, 6],
])

total = np.sum(matrix)                     # 21
column_sums = np.sum(matrix, axis=0)       # [5, 7, 9]
row_sums = np.sum(matrix, axis=1)          # [6, 15]

mean = np.mean(matrix)
standard_deviation = np.std(matrix)
minimum = np.min(matrix)
maximum = np.max(matrix)
minimum_index = np.argmin(matrix)
maximum_index = np.argmax(matrix)


# =============================================================================
# 16. RESHAPING, FLATTENING, AND ADDING DIMENSIONS
# =============================================================================

x = np.arange(6)
reshaped = x.reshape(2, 3)
flat_view = reshaped.ravel()
flat_copy = reshaped.flatten()

tones = np.array([80, 260, 430])           # shape (3,)
tone_column = tones[:, None]               # shape (3, 1)
tone_column_2 = tones.reshape(-1, 1)       # equivalent

t_small = np.array([0.0, 0.1, 0.2, 0.3])  # shape (4,)
time_row = t_small[None, :]                 # shape (1, 4)


# =============================================================================
# 17. BROADCASTING
# =============================================================================

# (3, 1) multiplied by (4,) broadcasts to (3, 4).
frequency_time_grid = tones[:, None] * t_small

# This explicit form produces the same result.
explicit_grid = tones[:, None] * t_small[None, :]
assert np.array_equal(frequency_time_grid, explicit_grid)

# Three sine-wave rows, one row for each tone.
sine_rows = np.sin(2 * np.pi * tones[:, None] * t_small)

# Add the rows at every time position to form one composite signal.
composite = np.sum(sine_rows, axis=0)


# =============================================================================
# 18. STACKING AND CONCATENATION
# =============================================================================

a = np.array([1, 2, 3])
b = np.array([4, 5, 6])

joined_end_to_end = np.concatenate((a, b))
rows = np.vstack((a, b))                   # shape (2, 3)
# Older NumPy also offered np.row_stack as an alias; np.vstack is preferred.
columns = np.column_stack((a, b))          # shape (3, 2)
columns_hstack = np.hstack((a[:, None], b[:, None]))

# Useful for returning [original_frequency, alias_frequency, flag].
original = np.array([40, 260, 540])
folded = np.array([40, 240, 40])
flags = np.array([0, 1, 1])
alias_table = np.column_stack((original, folded, flags))


# =============================================================================
# 19. SORTING, ARGSORT, PARTITION, AND UNIQUE VALUES
# =============================================================================

values = np.array([40, 10, 30, 20])
sorted_values = np.sort(values)             # [10, 20, 30, 40]
ascending_indices = np.argsort(values)      # indices producing ascending order
descending_indices = np.argsort(values)[::-1]
descending_values = values[descending_indices]

magnitudes = np.array([2, 50, 10, 40, 5])
three_largest_unsorted_indices = np.argpartition(magnitudes, -3)[-3:]
three_largest_indices = three_largest_unsorted_indices[
    np.argsort(magnitudes[three_largest_unsorted_indices])[::-1]
]
three_largest_values = magnitudes[three_largest_indices]

unique, counts = np.unique([1, 1, 2, 3, 3, 3], return_counts=True)


# =============================================================================
# 20. SEARCHING AND COMPARING FLOATING-POINT VALUES
# =============================================================================

x = np.array([0.1 + 0.2])
y = np.array([0.3])
close = np.isclose(x[0], y[0])
arrays_close = np.allclose(x, y)

locations = np.where(np.array([10, 20, 30, 20]) == 20)[0]
insertion_position = np.searchsorted(np.array([10, 20, 30]), 25)


# =============================================================================
# 21. PADDING, REPEATING, TILING, AND ZERO INSERTION
# =============================================================================

x = np.array([2, 5, 3])
padded = np.pad(x, (2, 3), mode="constant")
zoh_values = np.repeat(x, 3)                # [2,2,2,5,5,5,3,3,3]
tiled = np.tile(x, 2)                       # [2,5,3,2,5,3]

L = 3
upsampled = np.zeros(len(x) * L)
upsampled[::L] = x                          # [2,0,0,5,0,0,3,0,0]


# =============================================================================
# 22. LINEAR INTERPOLATION
# =============================================================================

samples = np.array([0, 2, 1], dtype=float)
fs = 2
old_t = np.arange(len(samples)) / fs
new_t = np.linspace(old_t[0], old_t[-1], 9)
linear_result = np.interp(new_t, old_t, samples)


# =============================================================================
# 23. SINC AND SINC RECONSTRUCTION
# =============================================================================

# np.sinc(u) means sin(pi*u)/(pi*u), with np.sinc(0) handled safely as 1.
sinc_values = np.sinc(np.array([-1.0, 0.0, 1.0]))


def sinc_reconstruct(samples, fs, t_new):
    samples = np.asarray(samples, dtype=float)
    t_new = np.asarray(t_new, dtype=float)
    sample_t = np.arange(len(samples)) / fs
    T = 1 / fs
    sinc_matrix = np.sinc((t_new[:, None] - sample_t[None, :]) / T)
    return sinc_matrix @ samples


# =============================================================================
# 24. CONVOLUTION AND A SHORT FIR LOW-PASS FILTER
# =============================================================================

signal = np.array([1, 2, 3, 4], dtype=float)
kernel = np.array([0.25, 0.5, 0.25])
full_convolution = np.convolve(signal, kernel, mode="full")
same_length_convolution = np.convolve(signal, kernel, mode="same")
valid_convolution = np.convolve(signal, kernel, mode="valid")


def lowpass_fir(cutoff, fs, numtaps):
    if not (0 < cutoff < fs / 2):
        raise ValueError("cutoff must be between 0 and fs/2")
    if numtaps < 3 or numtaps % 2 == 0:
        raise ValueError("numtaps must be odd and at least 3")
    n = np.arange(numtaps) - (numtaps - 1) / 2
    h = 2 * cutoff / fs * np.sinc(2 * cutoff * n / fs)
    h *= np.hamming(numtaps)
    return h / np.sum(h)


# =============================================================================
# 25. ALIAS-FREQUENCY FORMULA
# =============================================================================

def alias_frequency(frequency, fs):
    return np.abs(((frequency + fs / 2) % fs) - fs / 2)


alias_of_430_at_400 = alias_frequency(430, 400)  # 30 Hz


# =============================================================================
# 26. FFT, IFFT, AND PHYSICAL FREQUENCY ARRAYS
# =============================================================================

fs = 1000
t = np.arange(fs) / fs
x = np.sin(2 * np.pi * 50 * t)

X_full = np.fft.fft(x)
frequency_full = np.fft.fftfreq(len(x), d=1 / fs)

X_one_sided = np.fft.rfft(x)
frequency_one_sided = np.fft.rfftfreq(len(x), d=1 / fs)
magnitude = np.abs(X_one_sided)
phase = np.angle(X_one_sided)

reconstructed = np.fft.ifft(X_full).real
assert np.allclose(x, reconstructed)

non_dc_peak_index = 1 + np.argmax(magnitude[1:])
dominant_frequency = frequency_one_sided[non_dc_peak_index]

frequency_resolution = fs / len(x)
physical_frequency_of_bin_k = lambda k: k * fs / len(x)


# =============================================================================
# 27. FFT SHIFT
# =============================================================================

# fftshift is useful for displaying negative frequencies on the left and
# positive frequencies on the right, with zero frequency in the center.
shifted_spectrum = np.fft.fftshift(X_full)
shifted_frequencies = np.fft.fftshift(frequency_full)
unshifted_spectrum = np.fft.ifftshift(shifted_spectrum)


# =============================================================================
# 28. COMPLEX-NUMBER SYNTAX USED BY THE DFT
# =============================================================================

z = 3 + 4j
real_part = z.real
imaginary_part = z.imag
magnitude_of_z = abs(z)
conjugate = np.conj(z)

complex_array = np.array([1 + 2j, 3 - 4j])
array_real = complex_array.real
array_imaginary = complex_array.imag
array_magnitude = np.abs(complex_array)


# =============================================================================
# 29. MATRIX MULTIPLICATION
# =============================================================================

A = np.array([[1, 2], [3, 4]])
v = np.array([5, 6])
matrix_vector_product = A @ v

# In sinc reconstruction: (P by N) @ (N,) gives (P,).
sinc_matrix_example = np.ones((10, 4))
four_samples = np.array([1, 2, 3, 4])
ten_outputs = sinc_matrix_example @ four_samples


# =============================================================================
# 30. RANDOM VALUES FOR QUICK TESTING
# =============================================================================

rng = np.random.default_rng(12345)
uniform_random = rng.random(5)
normal_random = rng.normal(loc=0, scale=1, size=5)
random_integers = rng.integers(0, 10, size=5)


# =============================================================================
# 31. MATPLOTLIB PATTERNS USED IN THE ONLINE
# =============================================================================

def plotting_examples(show=False):
    fs = 100
    t = np.arange(fs) / fs
    x = np.sin(2 * np.pi * 5 * t)

    plt.figure(figsize=(8, 4))
    plt.plot(t, x, label="continuous-looking line")
    plt.scatter(t[::5], x[::5], label="selected samples")
    plt.xlabel("Time (s)")
    plt.ylabel("Amplitude")
    plt.title("Sampled sine")
    plt.grid()
    plt.legend()
    plt.tight_layout()
    if show:
        plt.show()
    else:
        plt.close()

    frequencies = np.fft.rfftfreq(len(x), d=1 / fs)
    magnitude = np.abs(np.fft.rfft(x)) / len(x)
    plt.figure(figsize=(8, 4))
    plt.stem(frequencies, magnitude)
    plt.xlim(0, fs / 2)
    plt.xlabel("Frequency (Hz)")
    plt.ylabel("Magnitude")
    plt.grid()
    plt.tight_layout()
    if show:
        plt.show()
    else:
        plt.close()


# =============================================================================
# 32. COMMON EXAM-SCALE PATTERNS
# =============================================================================

def sample_multitone(freqs, amps, fs, duration):
    freqs = np.asarray(freqs, dtype=float)
    amps = np.asarray(amps, dtype=float)
    if freqs.shape != amps.shape or freqs.size == 0:
        raise ValueError("freqs and amps must be nonempty and match")
    N = int(round(fs * duration))
    t = np.arange(N) / fs
    x = np.sum(
        amps[:, None] * np.sin(2 * np.pi * freqs[:, None] * t), axis=0
    )
    return t, x


def downsample(x, fs, M):
    if M < 1 or int(M) != M:
        raise ValueError("M must be a positive integer")
    return np.asarray(x)[:: int(M)], fs / M


def upsample_zeros(x, fs, L):
    if L < 1 or int(L) != L:
        raise ValueError("L must be a positive integer")
    L = int(L)
    x = np.asarray(x)
    y = np.zeros(len(x) * L, dtype=np.result_type(x, float))
    y[::L] = x
    return y, fs * L


# =============================================================================
# 33. FREQUENT MISTAKES TO AVOID
# =============================================================================

# Wrong: np.arange(0, duration, fs)       # fs is a rate, not a time step
# Right: np.arange(0, duration, 1/fs)
# Safer: np.arange(round(fs*duration)) / fs

# Wrong after downsampling: new_fs = fs * M
# Right after downsampling: new_fs = fs / M

# Wrong after upsampling: new_fs = fs / L
# Right after upsampling: new_fs = fs * L

# Wrong: t[None:]                         # normal slice; does not add an axis
# Right: t[None, :]                       # shape becomes (1, N)

# Wrong: frequencies * t                 # incompatible shapes if lengths differ
# Right: frequencies[:, None] * t        # produces all frequency/time pairs

# Wrong: filter after aliasing occurred
# Right: low-pass first, then downsample

# Wrong: use fs/N labels with a one-sided array without constructing its axis
# Right: np.fft.rfftfreq(N, d=1/fs)


if __name__ == "__main__":
    plotting_examples(show=False)
    demo_t, demo_x = sample_multitone([30, 80], [1, 0.5], 400, 1)
    print("\nReference file executed successfully.")
    print("multitone samples:", len(demo_x))
    print("dominant FFT frequency:", dominant_frequency)
    print("alias of 430 Hz at fs=400 Hz:", alias_of_430_at_400)
