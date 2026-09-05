import argparse
import os

import numpy as np

from image_utils import (
    load_image,
    save_image,
    save_comparison
)

from transforms import (
    FFTTransformer,
    DFTAnalyzer,
    ArbitraryLengthFFT
)







def transform_2d(image, engine):

    x = np.asarray(
        image,
        dtype=np.complex128
    )


    temp = np.empty_like(x)


    # rows

    for r in range(x.shape[0]):

        temp[r,:] = engine.transform(
            x[r,:]
        )


    out = np.empty_like(temp)


    # columns

    for c in range(x.shape[1]):

        out[:,c] = engine.transform(
            temp[:,c]
        )


    return out


def inverse_2d(spectrum, engine):

    x = np.asarray(
        spectrum,
        dtype=np.complex128
    )


    temp = np.empty_like(x)


    # inverse columns

    for c in range(x.shape[1]):

        temp[:,c] = engine.inverse(
            x[:,c]
        )


    out = np.empty_like(temp)


    # inverse rows

    for r in range(x.shape[0]):

        out[r,:] = engine.inverse(
            temp[r,:]
        )


    return out



def deblur_image(
        blurred,
        kernel,
        engine,
        eps=1e-8
):

    """
    Inverse filtering:

    F = B / K

    """

    # FFT of blurred image

    B = transform_2d(
        blurred,
        engine
    )


    # FFT of kernel

    K = transform_2d(
        kernel,
        engine
    )


    # avoid division by zero

    K_safe = K.copy()

    K_safe[
        np.abs(K_safe)<eps
    ] = eps



    # inverse filter

    restored_spectrum = B / K_safe



    # back to image

    restored = inverse_2d(
        restored_spectrum,
        engine
    )


    return np.real(restored)



# ---------------------------------------------------
# Main
# ---------------------------------------------------

def main():


    parser = argparse.ArgumentParser()


    parser.add_argument(
        "blurred",
        help="blurred image path"
    )


    parser.add_argument(
        "kernel",
        help="kernel image path"
    )


    parser.add_argument(
        "--out-dir",
        default="deblur_output"
    )


    args = parser.parse_args()



    os.makedirs(
        args.out_dir,
        exist_ok=True
    )


    engine = FFTTransformer()



    # load images

    blurred = load_image(
        args.blurred,
        as_gray=True
    )


    kernel = load_image(
        args.kernel,
        as_gray=True
    )



    restored = deblur_image(
        blurred,
        kernel,
        engine
    )



    save_image(
        restored,
        os.path.join(
            args.out_dir,
            "restored.png"
        )
    )


    save_comparison(
        [
            blurred,
            restored
        ],

        [
            "Blurred",
            "Restored"
        ],

        os.path.join(
            args.out_dir,
            "comparison.png"
        )
    )



if __name__=="__main__":
    main()