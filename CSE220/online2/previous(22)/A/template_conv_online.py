"""
CSE220 Online 2 (Step Response)

Instructions:
- Copy (or import) your completed Signal and LTI_System classes from Offline 1.
- Implement the TODO functions below.
- Do NOT use numpy.convolve / scipy.signal / any built-in convolution.
"""

import numpy as np
import matplotlib.pyplot as plt


# Paste/Import your Offline 1 implementations here
# from your_offline1_file import Signal, LTI_System

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

    # Create a finite discrete-time signal over the given integer range.
    def __init__(self, start_time, end_time):
        self.start_time=start_time
        self.end_time=end_time
        self.values=np.zeros(end_time-start_time+1)

    # Return the number of stored samples in the signal.
    def __len__(self):
        return len(self.values)

    # Return the integer time indices covered by the signal.
    def times(self):
        return range(self.start_time ,self.end_time+1)

    # Return the signal value at the given time index.
    def get_value_at_time(self, t):
        if (self.start_time <= t <= self.end_time): return self.values[t-self.start_time]
        return 0.0

    # Set the signal value at the given time index.
    def set_value_at_time(self, t, value):
         if (self.start_time <= t <= self.end_time): self.values[t-self.start_time]= float(value)
         else: raise IndexError(f"Time index {t} out of bounds [{self.start_time} , {self.end_time}]")

    # Return a shifted copy of the signal.
    def shift(self, k):
        new_signal = DiscreteSignal(self.start_time+k, self.end_time+k)
        new_signal.values = self.values.copy()
        return new_signal

    # Return the sum of this signal and another signal.
    def add(self, other):
        start = min(self.start_time, other.start_time)
        end= max(self.end_time, other.end_time)
        new_signal = DiscreteSignal(start,end)
        for t in new_signal.times():
            new_signal.set_value_at_time(t,self.get_value_at_time(t)+other.get_value_at_time(t))
        return new_signal

    # Return a scaled copy of the signal.
    def multiply(self, scalar):
        new_signal = DiscreteSignal(self.start_time, self.end_time)
        new_signal.values=self.values*float(scalar)
        return new_signal

    # Return the nonzero samples of the signal.
    def nonzero_samples(self, tolerance=1e-12):
        return [(t,val) for t,val in zip(self.times(),self.values) if abs(val)>tolerance]

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

    # Store the impulse response that defines the LTI system.
    def __init__(self, impulse_response):
        self.h=impulse_response

    # Return the output time range for the convolution result.
    def output_range(self, input_signal):
        start = self.h.start_time + input_signal.start_time
        end = self.h.end_time + input_signal.end_time
        return start,end

    # Arguments: input_signal is a DiscreteSignal representing x[n].
    # Returns: list of (k, component_signal) for each nonzero input sample x[k].
    # Example: x[2] = 3 contributes the component 3*h[n - 2].
    def get_response_components(self, input_signal):
        components = []
        for k, x_k in input_signal.nonzero_samples():
            shifted_h = self.h.shift(k)
            scaled_shifted_h = shifted_h.multiply(x_k)
            components.append((k, scaled_shifted_h))
        return components

    # Return the system output using superposition of response components.
    def output_by_superposition(self, input_signal):
        out_start, out_end = self.output_range(input_signal)
        y = DiscreteSignal(out_start, out_end)
        
        components = self.get_response_components(input_signal)
        for k, component in components:
            y = y.add(component)
            
        return y

    # Arguments: input_signal is a DiscreteSignal and n is one output time index.
    # Returns: list of (k, x_k, h_n_minus_k, product) nonzero contribution tuples.
    # Example: a term may look like (2, 3.0, 0.5, 1.5) for x[2]h[n - 2].
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

    # Arguments: input_signal is a DiscreteSignal and n is one output time index.
    # Returns: float, the convolution-sum value y[n].
    def output_at_time(self, input_signal, n):
        contributions = self.get_contributions_at_time(input_signal, n)
        return float(sum(term for _, _, _, term in contributions))

    # Return the complete output signal of the LTI system.
    def output(self, input_signal):
        out_start, out_end = self.output_range(input_signal)
        y = DiscreteSignal(out_start, out_end)
        
        for n in y.times():
            y.set_value_at_time(n, self.output_at_time(input_signal, n))
            
        return y

def read_signal_from_file(filename: str, INF: int) -> DiscreteSignal:
    sig = DiscreteSignal(-INF,INF)
    with open(filename, "r", encoding="utf-8") as f:
        nstart, nend = map(int, f.readline().strip().split())
        vals = list(map(float, f.readline().strip().split()))
    assert len(vals) == (nend - nstart + 1)
    for i, v in enumerate(vals):
        sig.set_value_at_time(nstart + i, v)
    return sig


def first_difference(sig: DiscreteSignal) -> DiscreteSignal:
    """
    Returns Δsig[n] = sig[n] - sig[n-1] (assume outside range is 0).
    Must use Signal.shift/add/multiply.
    """
    x_n__minus= sig.shift(1)
    del_x=sig.add(x_n__minus.multiply(-1))
    return del_x


def impulse_from_step_response(step_response: DiscreteSignal) -> DiscreteSignal:
    """
    Given s[n], compute h[n] = s[n] - s[n-1] (with s[-1]=0).
    Must use only Signal operations.
    """
    s_n_minus=step_response.shift(1)
    h=step_response.add(s_n_minus.multiply(-1))
    return h


def output_using_step_response(x: DiscreteSignal, step_response: DiscreteSignal) -> DiscreteSignal:
    """
    Compute y[n] using ONLY step response:
        y = (Δx * s)
    You must reuse your Offline 1 LTI_System machinery (linear combination of impulses).
    """
    system = LTISystem(step_response)
    y= system.output(x)
    return y


# Main (demo workflow)
if __name__ == "__main__":
    # Choose INF large enough for your signals
    INF = 50

    # ---- Load provided files ----
    s = read_signal_from_file("step_response.txt", INF)
    x = read_signal_from_file("input_signal.txt", INF)

    # ---- Part 1: recover impulse response ----
    h = impulse_from_step_response(s)

    s.plot("Step Response s[n]")
    h.plot("Recovered Impulse Response h[n] = s[n] - s[n-1]")

    # ---- Part 2: output using only step response ----
    dx = first_difference(x)
    y_s = output_using_step_response(dx,s)

    x.plot("Input x[n]")
    dx.plot("First Difference Δx[n]")
    y_s.plot("Output y_s[n] computed via step response")

    # ---- Part 3: verify with impulse-response method ----
    sys_h = h
    system = LTISystem(h)
    y_h = system.output(x)
    y_h.plot("Output y_h[n] computed via impulse response")

    # Check if outputs match closely
    if np.allclose(y_s.values, y_h.values, atol=1e-6):
        print("Outputs match closely!")
    else:
        print("Outputs differ!")
