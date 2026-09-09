SAMI — RECOMMENDATION ALGORITHMS / SPEAKER 1

8 conceptual Beamer frames | 23 overlay pages | planned delivery: 3 minutes

QUICK START
1. Upload the entire source ZIP as a new Overleaf project.
2. Select XeLaTeX as the compiler and main.tex as the main document.
3. Recompile. All fonts and photographic assets are included.
4. Download the PDF and present it in full-screen / single-page mode.
5. Advance one page for each reveal. Use the guide's page map to rehearse.

Local build (inside this folder):
  xelatex -interaction=nonstopmode -halt-on-error main.tex
  xelatex -interaction=nonstopmode -halt-on-error main.tex

EDITABLE FILES
main.tex          Entry point.
theme.tex         Colors, typography, exact canvas and TikZ helper commands.
sami_frames.tex   All eight frames and their overlay specifications.
speaker_guide.tex Timed English script, click map, mathematics and references.
assets/           Generated Bob photograph and three-panel video artwork.
fonts/            Bundled fonts with their distribution license.

To compile the guide, set speaker_guide.tex as the main document and use
XeLaTeX, or run xelatex speaker_guide.tex twice locally.

DESIGN
Charcoal #17201D; warm ivory #F4F0E7; forest #245842; gold #D9B45E.
Nimbus Sans body text; P052 Italic editorial accents.
Native TikZ phone, stars, watch-progress signal, matrix, flow arrows,
ranking bars and exact cosine geometry. Photographs are imported assets.
All stage text remains selectable in the exported PDF.

PRESENTATION VERSUS REVIEW COPY
The presentation has 23 pages because Beamer overlays are separate PDF pages.
The review copy has the completed state of each frame: 8 pages.
For live delivery, use Sami_Recommendation_Presentation.pdf.
This deck uses manual, click-driven reveals. It contains no embedded movie
or continuous animation requiring a particular PDF viewer.

MERGING THE GROUP'S DECK
Teammates can reuse theme.tex and add their frames after
\input{sami_frames.tex} in main.tex. Avoid duplicating documentclass or
begin/end document. Change the footer denominator in theme.tex if the merged
deck should count every speaker's frames. Keep the fonts and assets folders
beside the group's main document, or adjust their relative paths.
Only Sami's part is authored here. Collaborative filtering is named as the
handoff, with its explanation left to the next speaker.

CONTENT NOTES
Bob and the video feed are fictional teaching examples. Watching one video
does not guarantee a specific next-day feed, and no cross-platform sharing
claim is made. The YouTube setting is illustrative; the toy content-based
algorithm is not asserted to be YouTube's production architecture.
Feedback is the correct term for explicit/implicit inputs; ranking is the
ordering of candidate outputs. The ratings matrix uses a 1–5 scale.
Feature vectors use separate illustrative strengths, not those star ratings.
Bob's profile (3,1) is chosen for explanation; it is not calculated from the
small ratings table. Cosine values are calculated exactly from the shown
vectors. See the guide for worked arithmetic and references.

ASSETS AND SOURCES
Bob's photograph and the three-panel fictional video stills were generated
for this presentation with the built-in image-generation tool. They do not
depict a real user or documentary evidence. The original artwork is retained;
TikZ clipping selects individual thumbnail panels at presentation time.
All technical figures were authored in TikZ, not generated as bitmap charts.
Fonts are from URW Base35; see fonts/LICENSE.txt.
Claims are traced by [1]–[6] in the guide. The supplied study deck and earlier
Beamer deck informed the topic sequence; their diagrams were not copied as
screenshots into the new slides.

Rubric reviewed on 2026-09-08:
Technical tools 8; general slide quality 5; contents 5; presentation 10;
WoW factor 2. Total 30, assessed individually.

Optional editable FigJam flow reference:
https://www.figma.com/board/jf0wu99bi2lEHuuL2iPeoO
