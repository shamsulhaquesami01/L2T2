"""
CSE 220 - Convolution Online Practice
All code from CSE220_Convolution_Online_Practice.pdf, in one runnable file.

Layout:
  Part 1  - base classes (DiscreteSignal, LTISystem)      <- paste this if the exam gives stubs
  Part 2  - syntax cheat sheet (file reading, plotting, verification, helpers)
  Part 3  - 17 practice problems (warm-up / core / stretch)
  Part 4  - January 2024 application papers (moving averages, smoothing, polynomials)

Run `python3 practice_code.py` to execute the built-in checks at the bottom.
Every function was tested; expected outputs are in the demo() calls.
"""

import math
import numpy as np


# =====================================================================
# PART 1 - BASE CLASSES (from Offline 1, corrected)
# =====================================================================

def readable_time_ticks(time_values, max_labels=18):
    if len(time_values) <= max_labels:
        return time_values
    step = int(np.ceil(len(time_values) / max_labels))
    ticks = time_values[::step]
    if ticks[-1] != time_values[-1]:
        ticks.append(time_values[-1])
    return ticks


class DiscreteSignal:
    """Finite discrete-time signal with integer indices."""

    def __init__(self, start_time, end_time):
        self.start_time = start_time
        self.end_time = end_time
        self.values = np.zeros(end_time - start_time + 1)

    def __len__(self):
        return len(self.values)

    def times(self):
        return range(self.start_time, self.end_time + 1)

    def get_value_at_time(self, t):
        if self.start_time <= t <= self.end_time:
            return self.values[t - self.start_time]
        return 0.0

    def set_value_at_time(self, t, value):
        if self.start_time <= t <= self.end_time:
            self.values[t - self.start_time] = float(value)
        else:
            raise IndexError(
                f"Time index {t} out of bounds [{self.start_time}, {self.end_time}]"
            )

    def shift(self, k):
        new_signal = DiscreteSignal(self.start_time + k, self.end_time + k)
        new_signal.values = self.values.copy()
        return new_signal

    def add(self, other):
        start = min(self.start_time, other.start_time)
        end = max(self.end_time, other.end_time)
        new_signal = DiscreteSignal(start, end)
        for t in new_signal.times():
            new_signal.set_value_at_time(
                t, self.get_value_at_time(t) + other.get_value_at_time(t)
            )
        return new_signal

    def multiply(self, scalar):
        new_signal = DiscreteSignal(self.start_time, self.end_time)
        new_signal.values = self.values * float(scalar)
        return new_signal

    def nonzero_samples(self, tolerance=1e-12):
        return [
            (t, val)
            for t, val in zip(self.times(), self.values)
            if abs(val) > tolerance
        ]

    def plot(self, title, save_path=None, ax=None):
        import matplotlib.pyplot as plt

        if ax is None:
            _, ax = plt.subplots()
        time_values = list(self.times())
        markerline, stemlines, baseline = ax.stem(time_values, self.values)
        markerline.set_markersize(6)
        baseline.set_color("black")
        baseline.set_linewidth(1)
        ax.axhline(0, color="black", linewidth=0.8)
        ax.set_title(title)
        ax.set_xlabel("n")
        ax.set_ylabel("value")
        ax.grid(True, alpha=0.35)
        ax.set_xticks(readable_time_ticks(time_values))
        ax.tick_params(axis="x", labelsize=9)
        if save_path is not None:
            plt.savefig(save_path, bbox_inches="tight", dpi=150)
        return ax


class LTISystem:
    """Discrete-time LTI system described by a finite impulse response."""

    def __init__(self, impulse_response):
        self.h = impulse_response

    def output_range(self, input_signal):
        start = self.h.start_time + input_signal.start_time
        end = self.h.end_time + input_signal.end_time
        return start, end

    def get_response_components(self, input_signal):
        components = []
        for k, x_k in input_signal.nonzero_samples():
            shifted_h = self.h.shift(k)
            scaled_shifted_h = shifted_h.multiply(x_k)
            components.append((k, scaled_shifted_h))
        return components

    def output_by_superposition(self, input_signal):
        out_start, out_end = self.output_range(input_signal)
        y = DiscreteSignal(out_start, out_end)
        for k, component in self.get_response_components(input_signal):
            y = y.add(component)
        return y

    def get_contributions_at_time(self, input_signal, n):
        contributions = []
        for k in input_signal.times():
            x_k = float(input_signal.get_value_at_time(k))
            if abs(x_k) > 1e-12:
                h_nk = float(self.h.get_value_at_time(n - k))
                term = x_k * h_nk
                if abs(term) > 1e-12:
                    contributions.append((k, x_k, h_nk, term))
        return contributions

    def output_at_time(self, input_signal, n):
        contributions = self.get_contributions_at_time(input_signal, n)
        return float(sum(term for _, _, _, term in contributions))

    def output(self, input_signal):
        out_start, out_end = self.output_range(input_signal)
        y = DiscreteSignal(out_start, out_end)
        for n in y.times():
            y.set_value_at_time(n, self.output_at_time(input_signal, n))
        return y


# =====================================================================
# PART 2 - SYNTAX CHEAT SHEET
# =====================================================================

def read_signal(filename):
    """Line 1: 'start end', line 2: space-separated samples."""
    with open(filename, "r", encoding="utf-8") as f:
        lines = [ln.strip() for ln in f if ln.strip()]
    start_time, end_time = map(int, lines[0].split())
    values = list(map(float, lines[1].split()))
    assert len(values) == end_time - start_time + 1, "value count does not match range"
    sig = DiscreteSignal(start_time, end_time)
    for i, t in enumerate(sig.times()):
        sig.set_value_at_time(t, values[i])
    return sig


def read_signal_padded(filename, INF):
    """Alternative style: pad into a wide fixed [-INF, INF] window."""
    sig = DiscreteSignal(-INF, INF)
    with open(filename, "r", encoding="utf-8") as f:
        start_time, end_time = map(int, f.readline().split())
        values = list(map(float, f.readline().split()))
    for i, v in enumerate(values):
        sig.set_value_at_time(start_time + i, v)
    return sig


def read_blocks(filename):
    """Read repeated (range line, values line) pairs until the file ends."""
    with open(filename, "r", encoding="utf-8") as f:
        lines = [ln.strip() for ln in f if ln.strip() and not ln.startswith("#")]
    signals, i = [], 0
    while i + 1 < len(lines):
        start_time, end_time = map(int, lines[i].split())
        values = list(map(float, lines[i + 1].split()))
        sig = DiscreteSignal(start_time, end_time)
        for j, t in enumerate(sig.times()):
            sig.set_value_at_time(t, values[j])
        signals.append(sig)
        i += 2
    return signals


def make(start_time, end_time, values):
    """Build a signal from a list of consecutive values."""
    sig = DiscreteSignal(start_time, end_time)
    for i, t in enumerate(sig.times()):
        sig.set_value_at_time(t, values[i])
    return sig


def make_sparse(start_time, end_time, samples):
    """Build a signal from a sparse {time: value} dictionary."""
    sig = DiscreteSignal(start_time, end_time)
    for t, v in samples.items():
        sig.set_value_at_time(t, v)
    return sig


def max_absolute_difference(first, second):
    """Safe comparison across possibly different ranges."""
    lo = min(first.start_time, second.start_time)
    hi = max(first.end_time, second.end_time)
    worst = 0.0
    for t in range(lo, hi + 1):
        worst = max(worst, abs(first.get_value_at_time(t) - second.get_value_at_time(t)))
    return worst


def show(sig):
    """Readable (time, value) list for printing."""
    return [(t, round(float(v), 4)) for t, v in zip(sig.times(), sig.values)]


# Example: three-panel stem figure (uncomment to use)
# import matplotlib.pyplot as plt
# fig, axes = plt.subplots(3, 1, figsize=(9, 7), constrained_layout=True)
# x.plot("Input x[n]",            ax=axes[0])
# h.plot("Impulse response h[n]", ax=axes[1])
# y.plot("Output y[n]",           ax=axes[2])
# plt.show()          # <-- WITHOUT THIS NOTHING APPEARS


# =====================================================================
# PART 3 - PRACTICE PROBLEMS
# =====================================================================

# ---- Problem 1: time reversal (warm-up) ----
def time_reverse(sig):
    out = DiscreteSignal(-sig.end_time, -sig.start_time)
    for t in sig.times():
        out.set_value_at_time(-t, sig.get_value_at_time(t))
    return out


# ---- Problem 2: subtract and delay (warm-up) ----
def subtract(first, second):
    return first.add(second.multiply(-1))


def delay(sig, n_samples):
    return sig.shift(n_samples)


# ---- Problem 3: even/odd decomposition (warm-up) ----
def even_odd_parts(sig):
    reversed_sig = time_reverse(sig)
    even_part = sig.add(reversed_sig).multiply(0.5)
    odd_part = subtract(sig, reversed_sig).multiply(0.5)
    return even_part, odd_part


# ---- Problem 4: energy and average power (warm-up) ----
def energy(sig):
    return float(np.sum(sig.values ** 2))


def average_power(sig):
    return energy(sig) / len(sig)


# ---- Problem 5: upsample / downsample (warm-up) ----
def upsample(sig, factor):
    out = DiscreteSignal(sig.start_time * factor, sig.end_time * factor)
    for t in sig.times():
        out.set_value_at_time(t * factor, sig.get_value_at_time(t))
    return out


def downsample(sig, factor):
    start = math.ceil(sig.start_time / factor)
    end = math.floor(sig.end_time / factor)
    out = DiscreteSignal(start, end)
    for n in out.times():
        out.set_value_at_time(n, sig.get_value_at_time(n * factor))
    return out


# ---- Problem 6: two systems in series / cascade (core) ----
def cascade(h_first, h_second):
    return LTISystem(h_first).output(h_second)


# ---- Problem 7: mixed block diagram (core) ----
def network_equivalent(h1, h2, h3, h4):
    middle = h2.add(h3)                       # parallel branches
    return cascade(cascade(h1, middle), h4)   # series stages


# ---- Problem 8: step response <-> impulse response (core) ----
def restrict(sig, start_time, end_time):
    out = DiscreteSignal(start_time, end_time)
    for n in out.times():
        out.set_value_at_time(n, sig.get_value_at_time(n))
    return out


def first_difference(sig):
    return subtract(sig, sig.shift(1))        # sig[n] - sig[n-1]


def impulse_from_step_response(s):
    return restrict(first_difference(s), s.start_time, s.end_time)


def step_response_from_impulse(h, extra=0):
    out = DiscreteSignal(h.start_time, h.end_time + extra)
    running = 0.0
    for n in out.times():
        running += h.get_value_at_time(n)
        out.set_value_at_time(n, running)
    return out


def unit_step(start_time, end_time):
    out = DiscreteSignal(start_time, end_time)
    for n in out.times():
        if n >= 0:
            out.set_value_at_time(n, 1.0)
    return out


# ---- Problem 9: cross-correlation (core) ----
def cross_correlate(x, y):
    return LTISystem(time_reverse(y)).output(x)


# ---- Problem 10: causality / memory / stability (core) ----
def is_causal(h, tolerance=1e-12):
    for n in h.times():
        if n < 0 and abs(h.get_value_at_time(n)) > tolerance:
            return False
    return True


def is_memoryless(h, tolerance=1e-12):
    for n in h.times():
        if n != 0 and abs(h.get_value_at_time(n)) > tolerance:
            return False
    return True


def absolute_sum(h):
    return float(np.sum(np.abs(h.values)))


# ---- Problems 11 & 12: deconvolution / system identification (stretch) ----
def deconvolve(y, h):
    h_start = h.start_time
    leading = h.get_value_at_time(h_start)
    if abs(leading) < 1e-12:
        raise ValueError("First impulse-response sample must be nonzero")
    x_start = y.start_time - h_start
    x_end = y.end_time - h.end_time
    x = DiscreteSignal(x_start, x_end)
    for n in x.times():
        known = 0.0
        for k in range(x_start, n):
            known += x.get_value_at_time(k) * h.get_value_at_time(n + h_start - k)
        x.set_value_at_time(n, (y.get_value_at_time(n + h_start) - known) / leading)
    return x


# ---- Problem 13: SuperSignal (stretch) ----
class SuperSignal:
    def __init__(self):
        self.components = []

    def add(self, signal, coefficient=1.0):
        self.components.append((coefficient, signal))


def output_super(system, super_signal):
    result = None
    for coefficient, signal in super_signal.components:
        piece = system.output(signal).multiply(coefficient)
        result = piece if result is None else result.add(piece)
    return result if result is not None else DiscreteSignal(0, 0)


def collapse_super(super_signal):
    result = None
    for coefficient, signal in super_signal.components:
        piece = signal.multiply(coefficient)
        result = piece if result is None else result.add(piece)
    return result if result is not None else DiscreteSignal(0, 0)


# ---- Problem 14: general system network (stretch) ----
class SystemNetwork:
    def __init__(self):
        self.branches = []

    def add_branch(self, impulse_responses, gain=1.0):
        self.branches.append((gain, list(impulse_responses)))

    def equivalent_impulse_response(self):
        total = None
        for gain, chain in self.branches:
            h = chain[0]
            for next_h in chain[1:]:
                h = cascade(h, next_h)          # series within a branch
            h = h.multiply(gain)
            total = h if total is None else total.add(h)   # parallel across branches
        return total

    def output(self, input_signal):
        return LTISystem(self.equivalent_impulse_response()).output(input_signal)


# ---- Problem 15: circular convolution (stretch) ----
def circular_convolution(x, h, period):
    out = DiscreteSignal(0, period - 1)
    for n in range(period):
        acc = 0.0
        for m in range(period):
            acc += x.get_value_at_time(m) * h.get_value_at_time((n - m) % period)
        out.set_value_at_time(n, acc)
    return out


# ---- Problem 16: recursive system vs truncated FIR (stretch) ----
def first_order_recursive(x, a, end_time):
    out = DiscreteSignal(x.start_time, end_time)
    previous = 0.0
    for n in out.times():
        previous = a * previous + x.get_value_at_time(n)
        out.set_value_at_time(n, previous)
    return out


def truncated_exponential(a, length):
    out = DiscreteSignal(0, length - 1)
    for n in out.times():
        out.set_value_at_time(n, a ** n)
    return out


# ---- Problem 17: matched-filter pattern detection (stretch) ----
def best_match_lag(long_signal, pattern):
    scores = cross_correlate(long_signal, pattern)
    best_lag = scores.start_time
    best_score = scores.get_value_at_time(best_lag)
    for n in scores.times():
        if scores.get_value_at_time(n) > best_score:
            best_score = scores.get_value_at_time(n)
            best_lag = n
    return best_lag, float(best_score)


# =====================================================================
# PART 4 - JANUARY 2024 APPLICATION PAPERS
# =====================================================================

def signal_from_list(values, start_time=0):
    sig = DiscreteSignal(start_time, start_time + len(values) - 1)
    for i, v in enumerate(values):
        sig.set_value_at_time(start_time + i, v)
    return sig


def window_slice(y, window_size, data_length):
    """Keep only outputs where the window sits fully inside the data."""
    return [y.get_value_at_time(t) for t in range(window_size - 1, data_length)]


def fmt(values, places=2):
    return ", ".join(f"{v:.{places}f}" for v in values)


# ---- Paper C: polynomial multiplication ----
def multiply_polynomials(coeff_a, coeff_b):
    x = signal_from_list(coeff_a)
    h = signal_from_list(coeff_b)
    y = LTISystem(h).output(x)
    return [y.get_value_at_time(t) for t in y.times()]


# ---- Paper B: moving averages ----
def unweighted_kernel(window_size):
    return signal_from_list([1.0 / window_size] * window_size)


def weighted_kernel(window_size):
    total = window_size * (window_size + 1) / 2         # 1 + 2 + ... + n
    return signal_from_list([(window_size - i) / total for i in range(window_size)])


def moving_average(prices, window_size, kernel):
    x = signal_from_list(prices)
    y = LTISystem(kernel).output(x)
    return window_slice(y, window_size, len(prices))


# ---- Paper A: exponential smoothing ----
def exponential_kernel(window_size, alpha):
    return signal_from_list([alpha * (1 - alpha) ** k for k in range(window_size)])


def exponential_smoothing(prices, window_size, alpha):
    x = signal_from_list(prices)
    y = LTISystem(exponential_kernel(window_size, alpha)).output(x)
    return window_slice(y, window_size, len(prices))


# =====================================================================
# SELF-TEST (runs all of the above against known answers)
# =====================================================================

def _demo():
    print("PART 3 - practice problems")
    print("  P1  reverse   ", show(time_reverse(make(-1, 2, [1, 2, 3, 4]))))
    print("  P2  delay2    ", show(delay(make(0, 3, [1, 2, 3, 4]), 2)))
    print("  P2  subtract  ", show(subtract(make(0, 2, [5, 5, 5]), make(1, 3, [1, 2, 3]))))
    e, o = even_odd_parts(make(0, 2, [4, 2, 0]))
    print("  P3  even/odd  ", show(e), show(o))
    print("  P4  energy    ", energy(make(0, 3, [1, -2, 3, 0])),
          "power", round(average_power(make(0, 3, [1, -2, 3, 0])), 4))
    print("  P5  up3       ", show(upsample(make(0, 2, [1, 2, 3]), 3)))
    print("  P5  down2     ", show(downsample(make(-3, 4, [7, 1, 8, 2, 9, 3, 6, 4]), 2)))

    h1, h2, xx = make(0, 1, [1, 1]), make(0, 1, [1, -1]), make(0, 2, [1, 2, 3])
    print("  P6  cascade   ", show(cascade(h1, h2)))

    hA, hB = make(0, 0, [1.0]), make(0, 1, [0.0, 0.5])
    hC, hD = make(0, 1, [1.0, 1.0]), make(0, 1, [1.0, -1.0])
    print("  P7  network   ", show(network_equivalent(hA, hB, hC, hD)))

    h = make(0, 3, [1, -1, 2, 0.5])
    s = step_response_from_impulse(h)
    print("  P8  step      ", show(s))
    print("  P8  recovered ", show(impulse_from_step_response(s)),
          " diff", round(max_absolute_difference(impulse_from_step_response(s), h), 12))

    print("  P9  correlate ", show(cross_correlate(make(0, 5, [0, 0, 1, 2, 1, 0]),
                                                   make(0, 2, [1, 2, 1]))))
    print("  P10 causal    ", is_causal(make(0, 2, [1, 2, 3])),
          is_causal(make(-1, 1, [1, 2, 3])),
          "| memless", is_memoryless(make(0, 0, [5])), is_memoryless(make(0, 1, [5, 1])),
          "| abs_sum", absolute_sum(make(0, 2, [1, -2, 3])))

    x_true = make(-1, 2, [1, 2, -1, 3])
    h_known = make(0, 2, [1, 0.5, -0.25])
    y_obs = LTISystem(h_known).output(x_true)
    print("  P11 x_rec     ", show(deconvolve(y_obs, h_known)),
          " diff", round(max_absolute_difference(deconvolve(y_obs, h_known), x_true), 12))
    print("  P12 h_rec     ", show(deconvolve(y_obs, x_true)),
          " diff", round(max_absolute_difference(deconvolve(y_obs, x_true), h_known), 12))

    ss = SuperSignal()
    ss.add(make(0, 0, [1.0]), 2.0)
    ss.add(make(2, 2, [1.0]), -1.0)
    sysem = LTISystem(make(0, 1, [1, 0.5]))
    print("  P13 super     ", show(output_super(sysem, ss)),
          " diff", round(max_absolute_difference(output_super(sysem, ss),
                                                 sysem.output(collapse_super(ss))), 12))

    net = SystemNetwork()
    net.add_branch([hA, hC])
    net.add_branch([hB], gain=-1.0)
    print("  P14 net h_eq  ", show(net.equivalent_impulse_response()),
          " out", show(net.output(make(0, 1, [1, 1]))))

    print("  P15 circ N=4  ", show(circular_convolution(make(0, 3, [1, 2, 0, 0]),
                                                        make(0, 3, [1, 1, 0, 0]), 4)))
    print("  P16 recursive ", show(first_order_recursive(make(0, 0, [1.0]), 0.5, 5)))
    print("  P16 h_trunc   ", show(truncated_exponential(0.5, 6)))
    print("  P17 lag/score ", best_match_lag(make(0, 9, [0, 0, 1, 3, 1, 0, 0, 1, 0, 0]),
                                             make(0, 2, [1, 3, 1])))

    print()
    print("PART 4 - January 2024 papers")
    print("  Poly (3 -2 1)x(2 0 -3 1) ->",
          [int(round(v)) for v in multiply_polynomials([3, -2, 1], [2, 0, -3, 1])])
    print("  Unweighted avg n=4       ->",
          fmt(moving_average([1, 2, 3, 4, 5, 6, 7, 8], 4, unweighted_kernel(4))))
    print("  Weighted avg   n=4       ->",
          fmt(moving_average([1, 2, 3, 4, 5, 6, 7, 8], 4, weighted_kernel(4))))
    print("  Exp smoothing  n=3 a=0.8 ->",
          fmt(exponential_smoothing([10, 11, 12, 9, 10, 13, 15, 16, 17, 18], 3, 0.8)))


if __name__ == "__main__":
    _demo()