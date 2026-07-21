#!/usr/bin/env python3
"""
Reference ALU truth table for Section A2, Group 03, and comparison against a
circuit-dumped truth table (check.txt).

Spec (A2, Group 3):
    cs2 cs1 cs0
     0   0   0   Decrement A
     0   0   1   Add with carry
     1   0   0   Sub with borrow
     1   0   1   Transfer A
     X   1   0   OR
     X   1   1   AND

File format:
    CS2 CS1 CS0 B[3..0] A[3..0] | O[3..0] VZSC[3..0]
Flag output column order is V, Z, S, C.
"""

import re
import sys

MASK = 0xF

def add5(x, y, cin):
    """4-bit adder returning (result4, carry_out, overflow)."""
    s = (x & MASK) + (y & MASK) + (cin & 1)
    res = s & MASK
    cout = (s >> 4) & 1
    carry_into_msb = (((x & 0x7) + (y & 0x7) + (cin & 1)) >> 3) & 1
    v = carry_into_msb ^ cout
    return res, cout, v

def reference(cs2, cs1, cs0, A, B):
    """Return (O, C, S, V, Z) for the given control signals and operands."""
    if cs1 == 1:
        # ---- Logical ops: cs2 is don't-care ----
        if cs0 == 0:
            O = A | B          # OR
        else:
            O = A & B          # AND
        O &= MASK
        C = 0                  # cleared for logical ops
        V = 0                  # cleared for logical ops
        S = (O >> 3) & 1
        Z = 1 if O == 0 else 0
        return O, C, S, V, Z

    # ---- Arithmetic ops (cs1 == 0) ----
    key = (cs2, cs0)
    if key == (0, 0):          # 000 Decrement A: A + 1111 + 0
        O, C, V = add5(A, 0xF, 0)
    elif key == (0, 1):        # 001 Add with carry: A + B + 1
        O, C, V = add5(A, B, 1)
    elif key == (1, 0):        # 100 Sub with borrow: A + B' + 0  (= A - B - 1)
        O, C, V = add5(A, (~B) & MASK, 0)
    elif key == (1, 1):        # 101 Transfer A: A + 0000 + 0
        O, C, V = add5(A, 0, 0)
    else:
        raise ValueError("unreachable")

    S = (O >> 3) & 1
    Z = 1 if O == 0 else 0
    return O, C, S, V, Z


def op_name(cs2, cs1, cs0):
    if cs1 == 1:
        return "OR" if cs0 == 0 else "AND"
    return {(0, 0): "Decrement A", (0, 1): "Add with carry",
            (1, 0): "Sub with borrow", (1, 1): "Transfer A"}[(cs2, cs0)]


def bits(n, w=4):
    return format(n & ((1 << w) - 1), f"0{w}b")


# ------------------------------------------------------------------ parse file
line_re = re.compile(
    r'^\s*([01])\s+([01])\s+([01])\s+([01]{4})\s+([01]{4})\s*\|\s*'
    r'([01]{4})\s+([01]{4})\s*$'
)

def parse(path):
    rows = {}
    with open(path) as f:
        for raw in f:
            m = line_re.match(raw.strip())
            if not m:
                continue
            cs2, cs1, cs0 = int(m[1]), int(m[2]), int(m[3])
            B = int(m[4], 2)
            A = int(m[5], 2)
            O = int(m[6], 2)
            vzsc = m[7]                       # V Z S C
            V, Z, S, C = (int(c) for c in vzsc)
            rows[(cs2, cs1, cs0, A, B)] = (O, C, S, V, Z)
    return rows


def main(path):
    circuit = parse(path)

    total = 0
    matches = 0
    mism_by_op = {}
    mism_samples = {}
    field_fail = {"O": 0, "C": 0, "S": 0, "V": 0, "Z": 0}
    missing = 0

    for cs2 in (0, 1):
        for cs1 in (0, 1):
            for cs0 in (0, 1):
                for A in range(16):
                    for B in range(16):
                        total += 1
                        exp = reference(cs2, cs1, cs0, A, B)
                        key = (cs2, cs1, cs0, A, B)
                        got = circuit.get(key)
                        if got is None:
                            missing += 1
                            continue
                        if got == exp:
                            matches += 1
                        else:
                            name = op_name(cs2, cs1, cs0)
                            mism_by_op[name] = mism_by_op.get(name, 0) + 1
                            # which fields differ
                            labels = ["O", "C", "S", "V", "Z"]
                            for i, lab in enumerate(labels):
                                if got[i] != exp[i]:
                                    field_fail[lab] += 1
                            if len(mism_samples.setdefault(name, [])) < 6:
                                mism_samples[name].append((key, exp, got))

    print("=" * 74)
    print("A2 / GROUP 03  ALU verification")
    print("=" * 74)
    print(f"Rows in file (data)      : {len(circuit)}")
    print(f"Rows expected            : {total}")
    if missing:
        print(f"Rows MISSING from file   : {missing}")
    print(f"Exact matches            : {matches}")
    print(f"Mismatches               : {total - matches - missing}")
    print("-" * 74)

    if matches == total:
        print("RESULT: PERFECT MATCH — every row is correct. ✅")
        return

    print("Mismatches grouped by operation:")
    for name in ["Decrement A", "Add with carry", "Sub with borrow",
                 "Transfer A", "OR", "AND"]:
        cnt = mism_by_op.get(name, 0)
        # rows per op: 000/001/100/101 -> 256 each; OR/AND -> 512 each
        per = 512 if name in ("OR", "AND") else 256
        flag = "  <-- ERRORS" if cnt else "  ok"
        print(f"  {name:<16}: {cnt:>4} / {per}{flag}")

    print("-" * 74)
    print("Which output field is wrong (count of rows where that bit differs):")
    for lab in ["O", "C", "S", "V", "Z"]:
        print(f"  {lab}: {field_fail[lab]}")

    print("-" * 74)
    print("Sample mismatches (expected vs your circuit):")
    for name, samples in mism_samples.items():
        print(f"\n  [{name}]")
        print(f"    {'CS2 CS1 CS0':<12} {'B':>5} {'A':>6}   "
              f"{'O':>5} {'C':>2}{'S':>2}{'V':>2}{'Z':>2}   "
              f"| {'O':>5} {'C':>2}{'S':>2}{'V':>2}{'Z':>2}")
        print(f"    {'(inputs)':<12} {'':>5} {'':>6}   {'--expected--':>17}   | {'--yours--':>17}")
        for key, exp, got in samples:
            cs2, cs1, cs0, A, B = key
            eO, eC, eS, eV, eZ = exp
            gO, gC, gS, gV, gZ = got
            print(f"    {cs2}   {cs1}   {cs0}    {bits(B)} {bits(A)}   "
                  f"{bits(eO)} {eC:>2}{eS:>2}{eV:>2}{eZ:>2}   "
                  f"| {bits(gO)} {gC:>2}{gS:>2}{gV:>2}{gZ:>2}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "/mnt/user-data/uploads/check.txt")