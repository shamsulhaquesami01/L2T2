
import numpy as np


def alias_table(freqs, fs):
    freqs = np.asarray(freqs, dtype=float)
    if fs <= 0 or np.any(freqs < 0):
        raise ValueError("fs must be positive and frequencies nonnegative")
    folded = np.abs(((freqs + fs/2) % fs) - fs/2)
    aliased = (freqs > fs/2).astype(int)
    return np.column_stack((freqs, folded, aliased))

print(alias_table([40, 260, 540, 760], 2000))