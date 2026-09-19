import numpy as np
import matplotlib.pyplot as plt


def downsample(x,fs,component_freqs,M):
    y=np.asarray(x)[::int(M)]
    new_fs = fs/M
    f=np.asarray(component_freqs,dtype=float)
    aliases=np.abs(((f+new_fs/2)%new_fs)-new_fs/2)
    return new_fs,y,aliases

fs = 1200; duration = 1.0; M = 3
t = np.arange(int(fs*duration)) / fs
tones = np.array([80, 260, 430])
x = np.sum(np.sin(2*np.pi*tones[:, None]*t), axis=0)
new_fs, y, aliases = downsample(x, fs, tones, M)
fy = np.fft.rfftfreq(len(y), d=1/new_fs)
plt.plot(fy, np.abs(np.fft.rfft(y)))
plt.xlabel("Frequency (Hz)"); plt.ylabel("Magnitude"); plt.show()