"""Task A: multiply huge decimal integers using transform-domain convolution."""

import argparse
import os
import sys

import numpy as np

from bench_utils import plot_runtime_curve, time_best, timing_table_lines
from io_utils import random_decimal, read_operands, write_report, write_text
from transforms import DFTAnalyzer, FFTTransformer, ArbitraryLengthFFT, next_power_of_two

sys.set_int_max_str_digits(2_000_000)

BASE_DIGITS = 4
BASE = 10 ** BASE_DIGITS


def to_limbs(text, base_digits=BASE_DIGITS):
    """Convert a signed decimal string to (sign, little-endian base-10**base_digits limbs)."""
    if base_digits < 1:
        raise ValueError("base_digits must be >= 1")
    text = str(text).strip()
    sign = -1 if text.startswith("-") else 1
    if text[:1] in "+-":
        text = text[1:]
    if not text or not text.isdigit():
        raise ValueError("invalid decimal integer")
    text = text.lstrip("0") or "0"
    limbs = []
    for end in range(len(text), 0, -base_digits):
        start = max(0, end - base_digits)
        limbs.append(int(text[start:end]))
    return sign, np.asarray(limbs, dtype=np.int64)


def from_limbs(sign, limbs, base_digits=BASE_DIGITS):
    """Propagate carries through convolution coefficients and return a decimal string."""
    base = 10 ** base_digits
    vals = [int(v) for v in np.asarray(limbs, dtype=np.int64).reshape(-1)]
    if not vals:
        return "0"
    i = 0
    while i < len(vals):
        q, r = divmod(vals[i], base)
        vals[i] = r
        if q:
            if i + 1 == len(vals):
                vals.append(q)
            else:
                vals[i + 1] += q
        i += 1
    while len(vals) > 1 and vals[-1] == 0:
        vals.pop()
    if all(v == 0 for v in vals):
        return "0"
    out = str(vals[-1]) + "".join(str(v).zfill(base_digits) for v in reversed(vals[:-1]))
    return ("-" if sign < 0 else "") + out


def multiply_transform(a, b, engine):
    """Multiply limb polynomials by pointwise multiplication in the transform domain."""
    a = np.asarray(a, dtype=np.int64).reshape(-1)
    b = np.asarray(b, dtype=np.int64).reshape(-1)
    needed = a.size + b.size - 1

    if engine.name == "arbitrary":
        N = needed
    else:
        N = next_power_of_two(needed)
        
    ap = np.zeros(N, dtype=np.complex128)
    bp = np.zeros(N, dtype=np.complex128)
    ap[:a.size] = a
    bp[:b.size] = b
    product = engine.inverse(engine.transform(ap) * engine.transform(bp))
    return np.rint(product.real).astype(np.int64), N


def multiply_schoolbook(a, b):
    """Optional O(n^2) limb-based baseline."""
    a = np.asarray(a, dtype=np.int64).reshape(-1)
    b = np.asarray(b, dtype=np.int64).reshape(-1)
    out = np.zeros(a.size + b.size - 1, dtype=np.int64)
    for i, av in enumerate(a):
        out[i:i + b.size] += av * b
    return out


def multiply(text_a, text_b, method):
    """Return (product_string, transform_length, limbs_a, limbs_b)."""
    sign_a, a = to_limbs(text_a)
    sign_b, b = to_limbs(text_b)
    if method == "schoolbook":
        conv = multiply_schoolbook(a, b)
        N = 0
    else:
        engine_cls = {
            "dft": DFTAnalyzer,
            "fft": FFTTransformer,
            "arbitrary": ArbitraryLengthFFT,
        }[method]
        conv, N = multiply_transform(a, b, engine_cls())
    return from_limbs(sign_a * sign_b, conv), N, a, b


def run_single(path, method, out_dir):
    """Run one Task A input and produce product.txt plus report.txt."""
    os.makedirs(out_dir, exist_ok=True)
    text_a, text_b = read_operands(path)
    product, N, a, b = multiply(text_a, text_b, method)
    expected = str(int(text_a) * int(text_b))
    verdict = "MATCH" if product == expected else "MISMATCH"
    write_text(os.path.join(out_dir, "product.txt"), product)
    write_report(os.path.join(out_dir, "report.txt"), [
        "Task A -- big-integer multiplication by spectral convolution",
        "input file          : %s" % path,
        "method              : %s" % method,
        "digits of A / B     : %d / %d" % (len(text_a.lstrip("+-").lstrip("0") or "0"), len(text_b.lstrip("+-").lstrip("0") or "0")),
        "base                : 10^%d" % BASE_DIGITS,
        "limbs of A / B      : %d / %d" % (a.size, b.size),
        "transform length N  : %d" % N,
        "digits of product   : %d" % len(product.lstrip("-")),
        "verification        : %s" % verdict,
    ])
    if verdict != "MATCH":
        raise AssertionError("MISMATCH: transform product differs from Python verification")


DFT_SIZES = [128, 256, 512, 1024, 2048, 4096]
FFT_SIZES = [128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072]
TIME_BUDGET = 20.0


def run_benchmark(out_dir):
    """Measure the naive DFT, radix-2 FFT, and optional schoolbook baseline."""
    def measure(method, sizes):
        xs, ys = [], []
        for digits in sizes:
            a = random_decimal(digits, seed=digits)
            b = random_decimal(digits, seed=digits + 1)
            seconds = time_best(lambda: multiply(a, b, method), repeats=2)
            xs.append(digits)
            ys.append(seconds)
            if seconds > TIME_BUDGET:
                break
        return xs, ys

    series = {
        "Naive DFT": measure("dft", DFT_SIZES),
        "Radix-2 FFT": measure("fft", FFT_SIZES),
    }
    try:
        series["Schoolbook"] = measure("schoolbook", FFT_SIZES)
    except NotImplementedError:
        pass
    os.makedirs(out_dir, exist_ok=True)
    plot_path = os.path.join(out_dir, "runtime_bigmul.png")
    plot_runtime_curve(series, plot_path, title="Task A: big-integer multiplication", xlabel="decimal digits per operand", references=("n2", "nlogn"))
    write_report(os.path.join(out_dir, "report.txt"), ["Task A -- runtime benchmark", ""] + timing_table_lines(series, size_label="digits") + ["", "plot: runtime_bigmul.png"])


def main():
    ap = argparse.ArgumentParser(description="Big-integer multiplication by DFT/FFT")
    ap.add_argument("input", nargs="?")
    ap.add_argument("--engine", default="fft", choices=["dft", "fft", "schoolbook", "arbitrary"])
    ap.add_argument("--out-dir", default="outputs")
    ap.add_argument("--benchmark", action="store_true")
    args = ap.parse_args()
    os.makedirs(args.out_dir, exist_ok=True)
    if args.benchmark:
        run_benchmark(args.out_dir)
    elif not args.input:
        ap.error("an input file is required unless --benchmark is given")
    else:
        run_single(args.input, args.engine, args.out_dir)


if __name__ == "__main__":
    main()
