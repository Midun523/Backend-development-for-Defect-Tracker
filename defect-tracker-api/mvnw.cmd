@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Batch Script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%
if "%APP_HOME:~-1%"=="\" set APP_HOME=%APP_HOME:~0,-1%

@REM Resolve JAVA_EXE
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
goto error

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
if "%JAVA_HOME:~-1%"=="\" set JAVA_HOME=%JAVA_HOME:~0,-1%
set JAVA_EXE="%JAVA_HOME%\bin\java.exe"

if exist %JAVA_EXE% goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
goto error

:init
set WRAPPER_JAR="%APP_HOME%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%APP_HOME%\.mvn\wrapper\maven-wrapper.properties"

@REM Provide a simple fallback downloader using PowerShell if wrapper jar is missing
if not exist %WRAPPER_JAR% (
    echo Downloading Maven Wrapper...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar', '%APP_HOME%\.mvn\wrapper\maven-wrapper.jar')"
)

%JAVA_EXE% "-Dmaven.multiModuleProjectDirectory=%APP_HOME%" -cp %WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & exit /B %ERROR_CODE%
