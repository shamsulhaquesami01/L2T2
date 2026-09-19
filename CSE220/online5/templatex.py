
import numpy as np

def sinc_reconstruct(samples, fs, t_new):

    T = 1 / fs

    result = np.zeros_like(
        t_new,
        dtype=float
    )

    for i in range(len(t_new)):

        for k in range(len(samples)):

            result[i] += (
                samples[k]
                *
                np.sinc(
                    (t_new[i] - k*T) / T
                )
            )

    return result



def zero_order_hold(samples, factor):

    return np.repeat(
        samples,
        factor
    )


def linear_reconstruct(samples, fs, new_fs):

    old_t = (
        np.arange(len(samples))
        / fs
    )

    duration = old_t[-1]

    new_t = np.arange(
        0,
        duration,
        1 / new_fs
    )

    y = np.interp(
        new_t,
        old_t,
        samples
    )

    return new_t, y