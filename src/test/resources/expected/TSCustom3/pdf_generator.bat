@ECHO OFF
for %%f in (*.dot) do (
	dot -Tpdf %%f -o %%~nf.pdf
)