RECOMMENDATION ALGORITHMS — MATCHED SOURCE PACKAGE

Main documents
  second_part.tex          All eleven supplied continuation slides.
  merged_presentation.tex  Sami's eight slides followed by those eleven slides.

Compiled previews
  second_part.pdf          11 frames / 45 overlay pages.
  merged_presentation.pdf  19 frames / 68 overlay pages.

COMPILING
Use XeLaTeX, not pdfLaTeX. Compile twice to resolve page counts and overlays.
Keep fonts/ next to either .tex file. The merged source also needs assets/.
Both sources contain their theme and slide definitions; theme.tex and other
.tex files are not required.

Overleaf: upload this ZIP as a project, select the desired .tex file as the
Main document, and select XeLaTeX as the compiler.

Local commands (run inside this extracted folder):
  xelatex second_part.tex
  xelatex second_part.tex
  xelatex merged_presentation.tex
  xelatex merged_presentation.tex

The embedded theme uses the original Nimbus Sans font files and P052 Italic.
Original font licensing is included in fonts/LICENSE.txt.
Original artwork attribution is included in assets/ARTWORK_PROMPTS.txt.

All supplied continuation content, including KNN, SVD, workflow, takeaways,
presenter IDs, equations, and overlay sequences, has been retained.
