@echo OFF
setlocal
set MYDIR=%~dp0
for %%I in (%MYDIR:~0,-1%) do set dirname=%%~nxI
if "%dirname%" EQU "scripts" (
  set MYDIR=%MYDIR:~0,-1%\..
)

SET MAINCLASS=de.denkunddachte.b2biutil.workflow.WorkflowUtil
SET REFRESH_BP=DD_REFRESH_PROPERTIES

set JAVA=java.exe
IF "%JRE_HOME%" NEQ "" (
    set JAVA="%JRE_HOME%\bin\java.exe"
) ELSE (
IF "%JAVA_HOME%" NEQ "" (
    set JAVA="%JAVA_HOME%\bin\java.exe"
))

setlocal ENABLEDELAYEDEXPANSION
set jar=
FOR /R %MYDIR% %%F IN (B2BUtils*.jar) DO set jar=%%F

%JAVA%  -Dddutils.debug=false -cp %jar% %MAINCLASS% --noutf8 -E %REFRESH_BP%
set RC=%ERRORLEVEL%

exit /B %RC%

