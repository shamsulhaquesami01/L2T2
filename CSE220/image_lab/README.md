# Image Lab

A Django GUI for 2D DSP experiments: convolution filtering, resampling with and
without anti-aliasing, and noise injection / restoration.

Built for CSE 220 (Signals & Linear Systems). Every algorithm is implemented
from its defining equation in `image_lab/dsp_utils.py` using NumPy only — there
is no OpenCV, and `scipy.ndimage` is deliberately **not** a dependency.

---

## Running it

```bash
cd CSE220/image_lab
python -m pip install -r requirements.txt
python manage.py runserver
```

Open <http://127.0.0.1:8000/>. There is no database and no migration step.

```bash
python manage.py test image_lab     # 23 tests
python manage.py check
```

Two test images ship in `samples/`:

| File | What it is for |
|---|---|
| `zoneplate.png` | Radially increasing frequency. The canonical aliasing demo. |
| `testchart.png` | Fine grating + smooth gradient + hard edges, in colour. |

### A note on this machine's Python

`D:\LocalDev\Python` is an **embeddable** CPython distribution. Its
`python313._pth` pins `sys.path` and, as a side effect, CPython neither prepends
the running script's directory nor honours `PYTHONPATH`. Django cannot import
`config` / `image_lab` without help, so `manage.py`, `wsgi.py` and `asgi.py`
each begin with a two-line `sys.path.insert(0, BASE_DIR)` shim.

The same limitation is why `CSE220/CSE-220_Offline_Convolution/main.py` fails
with `ModuleNotFoundError: No module named 'signal_lti'` — that script has no
such shim. If you install a full CPython and use a virtualenv, delete the shims.

---

## Project structure

```
CSE220/image_lab/
├── manage.py                  # entry point (+ sys.path shim)
├── requirements.txt
├── samples/                   # generated test patterns
│
├── config/                    # project package — wiring only, no logic
│   ├── settings.py            # no DB, no auth, no admin
│   ├── urls.py                # root routes + dev-only media serving
│   ├── wsgi.py / asgi.py
│
├── image_lab/                 # the application
│   ├── dsp_utils.py           # ← ALL the mathematics. numpy only.
│   ├── imaging.py             # ← the only module that touches Pillow / disk
│   ├── operations.py          # ← the experiment registry
│   ├── views.py               # 3 thin endpoints
│   ├── urls.py
│   ├── tests.py
│   └── templates/image_lab/
│       ├── base.html
│       └── index.html
│
├── static/image_lab/
│   ├── css/style.css
│   └── js/app.js              # live preview, debounced
│
└── media/                     # runtime, gitignored
    ├── uploads/               # ingest-normalised originals
    └── results/               # generated panels, content-addressed
```

### The layering rule

The one structural decision worth defending:

```
dsp_utils.py     pure NumPy      knows nothing about images-as-files, Django, or HTTP
     ↑
imaging.py       + Pillow        the only place that decodes, encodes, or touches disk
     ↑
operations.py    + registry      composes DSP calls into displayable experiments
     ↑
views.py         + Django        HTTP, validation, JSON
```

Each layer imports only downward. The payoff is that `dsp_utils.py` can be
imported into a Jupyter notebook or graded in isolation with no web stack
present — which is what makes the tests in `tests.py` able to compare against a
naive reference implementation directly.

### Adding a new experiment

`operations.py` is a registry. A new experiment is one function:

```python
@register("my_filter", "My Filter", "What it demonstrates.")
def op_my_filter(image, params):
    strength = _as_float(params, "strength", 1.0, 0.0, 5.0)
    output = dsp.some_new_routine(image, strength)
    return OpResult(
        panels=[Panel("original", "Original", image),
                Panel("result", "Result", output)],
        metrics=[Metric("PSNR", f"{dsp.psnr(image, output):.2f} dB")],
    )
```

The tab, the panel grid and the metrics table are all driven off the registry,
so nothing else needs editing except the control markup in `index.html`.

---

## The mathematics

### 1. 2D convolution

The 2D convolution of image `f` with kernel `h` is

```
g[m,n] = Σ_i Σ_j  f[i,j] · h[m-i, n-j]
```

Substituting `u = m-i`, `v = n-j` gives the form that can actually be evaluated
by array slicing:

```
g[m,n] = Σ_u Σ_v  h[u,v] · f[m-u, n-v]
```

Those minus signs are the whole difference between **convolution** and
**correlation**. Reversing the kernel on both axes once, up front, converts the
expression into a plain correlation:

```
h_flipped[u,v] = h[kh-1-u, kw-1-v]
g[m,n] = Σ_u Σ_v  h_flipped[u,v] · f[m+u-ay, n+v-ax]
```

where `(ay, ax) = (kh//2, kw//2)` is the anchor keeping output aligned with
input. For symmetric kernels (box, Gaussian) the flip changes nothing; for
asymmetric ones (Sobel, emboss, first difference) it rotates the response 180°.
`tests.py::test_convolution_is_not_correlation` fails if the flip is removed.

The implementation uses `sliding_window_view`, a stride trick producing an
`(H, W, kh, kw)` *view* of every neighbourhood with no data copied, then
contracts each window against the flipped kernel with `einsum`.

**Border handling.** The sum reads outside the image near the edges. Four
policies are exposed: `reflect` (default, no edge duplication), `edge` (clamp),
`wrap` (makes it circular convolution), and `zero` (true linear convolution,
but darkens borders).

**Reading the kernel sum.** The sum of the coefficients is the DC gain — the
filter's response to a constant region:

- **sum = 1** → brightness preserved. All blurs.
- **sum = 0** → DC removed. Pure high-pass; flat areas go to zero and only
  edges survive (Laplacian, Sobel).
- **sum > 1** → image brightens overall.

**Sharpening** is high-pass. The cheapest high-pass is *signal minus its own
low-pass*, and adding a multiple of that detail layer back is the unsharp mask:

```
g = f + amount · (f − blur(f))
```

The familiar `[[0,-1,0],[-1,5,-1],[0,-1,0]]` is exactly this: `5 = 1 + 4` at
the centre against `-1` neighbours, i.e. identity plus a Laplacian. It sums to
1, so brightness holds. Sharpening overshoots at edges and pushes samples
outside `[0,1]`; the UI reports what fraction got clipped, which is the halo
you see around high-contrast boundaries.

**Separability.** When `h[u,v] = ky[u]·kx[v]` the 2D sum factors into a pass
along x then a pass along y, dropping cost per output pixel from `O(kh·kw)` to
`O(kh+kw)`. The Gaussian is separable, which is why the anti-aliasing prefilter
uses `convolve_separable`.

### 2. Resampling and anti-aliasing

**The coordinate convention.** Pixel `k` of an `N`-wide row covers `[k, k+1)`
with centre `k+0.5`. Matching normalised centres between grids:

```
(d + 0.5)/out = (s + 0.5)/in     →     s = (d + 0.5)·(in/out) − 0.5
```

This *half-pixel centre* mapping keeps the image geometrically centred. The
naive `s = d·(in/out)` shifts everything by half a pixel — a classic
"my resized image drifted" bug. `test_bilinear_upsample_does_not_shift_the_image`
guards against it.

**Nearest neighbour** rounds `s` to the closest integer and copies that sample.
No arithmetic on values, so it is exact but blocky.

**Bilinear** takes the four surrounding samples and blends by fractional
distance `a = s_y − ⌊s_y⌋`, `b = s_x − ⌊s_x⌋`:

```
g = (1−a)(1−b)·f[y0,x0] + (1−a)b·f[y0,x1] + a(1−b)·f[y1,x0] + ab·f[y1,x1]
```

Implemented as interpolation along x for both rows, then a blend along y.
Indices are clamped *after* the weights are computed, so edges extend rather
than wrap.

#### Why anti-aliasing is mandatory before downsampling

Let `D = in/out` be the decimation factor. Keeping every `D`-th sample moves
the Nyquist limit from `π` down to `π/D` on the original sample grid.

Content above `π/D` **does not disappear**. It folds back into the baseband and
reappears as false low-frequency structure. That fold-back is aliasing: it is
why a downscaled brick wall grows moiré bands, and why a striped shirt strobes
on video.

The sampling theorem's remedy is to band-limit *before* discarding samples, so
nothing above the new Nyquist limit survives to fold. A Gaussian is the usual
practical choice — strictly positive, separable, and free of the ringing an
ideal brick-wall (sinc) filter produces. Matching its width to the decimation
factor:

```
σ = (D − 1)/2 ,   and σ = 0 when D ≤ 1
```

`σ = 0` when upsampling is not an oversight: upsampling introduces no new
Nyquist limit, so there is nothing to protect against, and prefiltering would
only throw away detail. The UI says as much when you push the scale above 1.

**Measured on `samples/zoneplate.png`, downsampled 6×:**

| | variance |
|---|---|
| original | 0.1250 |
| without anti-aliasing | 0.1185 |
| with anti-aliasing | 0.0249 |

The no-AA result retains almost the original variance — but that energy is
*fake*, folded-down content masquerading as real structure. Visually the outer
fine rings become ghost rings that were never in the original.

Each axis gets its own σ, so non-uniform scaling is filtered correctly. Both
results are magnified back to the original footprint with **nearest neighbour**
for display, so you compare the same sample set at a size where moiré is
visible, with no extra smoothing sneaking in.

### 3. Noise and restoration

Two noise models:

- **Additive white Gaussian**, `f + N(0, σ²)` — models sensor and thermal
  noise. Every pixel is perturbed slightly.
- **Salt & pepper (impulse)**, a fraction of pixels forced to 0.0 or 1.0 —
  models dead pixels and transmission errors. Applied to whole pixels rather
  than per channel, which is how it actually behaves on a sensor.

The seed is exposed and fixed by default, so when you tune a filter the noise
does not change underneath you — any difference you see is the filter's doing.

Three filters, and the point is that **matching the filter to the noise
matters more than filter strength**:

| Filter | Type | Good against |
|---|---|---|
| Moving average | linear (box convolution) | Gaussian |
| Gaussian low-pass | linear, separable | Gaussian |
| Median | non-linear, order statistic | impulse |

A moving average cannot discard an outlier — it only spreads each spike across
the window, turning one bad pixel into `n²` slightly-bad ones. The median ranks
an impulse out entirely, because an extreme value is never the middle value.
This is `test_median_beats_mean_on_impulse_noise`, and on the zone plate at 8%
corruption the median scores **+10.43 dB** PSNR against the mean filter's much
weaker recovery.

Quality is reported three ways, because they disagree in useful ways:

```
MSE  = mean((a−b)²)
PSNR = 10·log₁₀(peak² / MSE)            peak = 1.0        higher is better
SSIM = Gaussian-windowed luminance/contrast/structure agreement, Wang et al. 2004
```

PSNR is a pure per-pixel error measure and is blind to structure — a slightly
blurred image can out-score one with a few sharp defects, even though the eye
prefers the latter. SSIM uses local statistics under a Gaussian window and
tracks perceived quality more closely. `ssim` is computed with this project's
own `convolve_separable`, so it exercises the same convolution code path.

---

## Verification

`tests.py` checks the vectorised implementation against a literal, deliberately
slow transcription of the convolution sum (`naive_convolve2d`) — so the fast
path is validated against the *definition*, not against another library.

Also asserted: the kernel flip is real, the identity kernel is a no-op,
separable equals full 2D, a unity-sum blur preserves mean brightness, the
half-pixel convention does not shift the image, σ grows correctly with
decimation, anti-aliasing cuts alias energy by at least 5×, PSNR matches its
closed form for a known error, and the median beats the mean on impulse noise.
