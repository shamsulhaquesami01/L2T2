import numpy as np


def lowpass_fir(cutoff, fs, numtaps):

    if not (0 < cutoff < fs / 2):

        raise ValueError(
            "cutoff must be between 0 and fs/2"
        )

    if numtaps < 3 or numtaps % 2 == 0:

        raise ValueError(
            "numtaps must be odd and at least 3"
        )

    n = (
        np.arange(numtaps)
        -
        (numtaps - 1) / 2
    )

    h = (
        2 * cutoff / fs
        *
        np.sinc(
            2 * cutoff * n / fs
        )
    )

    h = h * np.hamming(numtaps)

    h = h / np.sum(h)

    return h