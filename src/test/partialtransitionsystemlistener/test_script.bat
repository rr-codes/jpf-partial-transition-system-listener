@ECHO OFF
SET dot_file="test.partialtransitionsystemlistener.PartialTest.dot"
SET file_path="C:\Users\mattw\OneDrive - York University\Documents\GitHub\jpf-partial-transition-system-listener\src\test\partialtransitionsystemlistener\"
SET test_count=0
SET counter=1
SET test_name=PartialTest

for %%f in (*.jpf) do (
	SET /A test_count = test_count+1
)

:JPF
START jpf "%test_name%%counter%.jpf"

:CheckForFile
IF EXIST "%file_name%%dot_file%" GOTO FoundFile
TIMEOUT /T 1 > nul
GOTO CheckForFile

:FoundFile
dot -Tpdf "%dot_file%" -o "test%counter%.pdf"
SET /A counter = counter+1
TIMEOUT /T 1 > nul
DEL %file_path%%dot_file%

IF "%counter%" LEQ "%test_count%" GOTO JPF