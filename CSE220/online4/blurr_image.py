import numpy as np

from transforms import FFTTransformer, next_power_of_two
from image_conv import transform_2d,inverse_2d

def convolve2d_blur(image,kernel):
    h,w = image.size
    kh,kw = kernel.size

    H=next_power_of_two(h+kh-1)
    W=next_power_of_two(w+kw-1)

    ip=np.zeros((H,W))
    kp=np.zeros((H,W))

    ip[:h,:w]=image
    kp[:kh,:kw]=kernel

    fft=FFTTransformer()
    full=inverse_2d(transform_2d(ip,fft)*transform_2d(kp,fft),fft).real

    return full[kh//2:kh//2+h,kw//2:kw//2+w]


