@echo off
setlocal
set BCP=c:\Progra~1\Java\PersonalJava\classes.zip
set CP=c:\progra~1\Java\PersonalJava\symbian.jar
set MISC=-g -source 1.3 -target 1.1 -deprecation
for %%f in (*.class) do del %%f
for %%f in (*.java) do javac -bootclasspath %BCP% -classpath .\;%CP% %MISC% %%f
endlocal
