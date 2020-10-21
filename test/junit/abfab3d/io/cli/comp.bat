cd ../../../../../
rem call ant -emacs %1 %2 %3 %4 %5 %6 %7 %8 %9
rem call ant -emacs build
rem call ant -emacs testSpecific -Dtest=TestCLISliceWriter
rem call ant -emacs test
rem call ant -emacs runMain -Dclass=abfab3d.io.cli.TestCLISliceWriter
rem call ant -emacs runMain -Dclass=abfab3d.io.cli.TestSLISliceReader
rem call ant -emacs runMain -Dclass=abfab3d.io.cli.TestSLISliceWriter
rem 
call ant -emacs runMain -Dclass=abfab3d.io.cli.TestSliceRenderer

