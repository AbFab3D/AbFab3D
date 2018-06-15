cd ../../../..
rem call ant -emacs %1 %2 %3 %4 %5 %6 %7 %8 %9
rem call ant -emacs runMain compileTest
rem 
call ant -emacs runMain -Dclass=abfab3d.intersect.TestDataSourceIntersector;
