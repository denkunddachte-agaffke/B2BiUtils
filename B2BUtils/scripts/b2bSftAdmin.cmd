@echo OFF
setlocal
set MYDIR=%~dp0
for %%I in (%MYDIR:~0,-1%) do set dirname=%%~nxI
if "%dirname%" EQU "scripts" (
  set MYDIR=%MYDIR:~0,-1%\..
)

SET MAINCLASS=de.denkunddachte.b2biutil.api.AzSftFullUtil
SET JAVA_AGENT=org.eclipse.persistence.jpa-3.0.3.jar

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

set agentjar=
FOR /R %MYDIR% %%F IN (%JAVA_AGENT%) DO set agentjar=%%F

%JAVA%  -Dddutils.debug=false -javaagent:%agentjar% -cp %jar% %MAINCLASS% %*
set RC=%ERRORLEVEL%

exit /B %RC%

