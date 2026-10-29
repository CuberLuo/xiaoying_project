@echo off
REM ----------------------------------------------------------------------------
REM Maven Wrapper (简化但可用的自举版本，Windows)
REM 作用：若 .mvn\wrapper\maven-wrapper.jar 不存在则下载，再启动它。
REM 需要 JAVA_HOME 指向可用的 JDK（如你的 JDK 21）。
REM ----------------------------------------------------------------------------
setlocal

set "DIR=%~dp0"
set "WRAPPER_JAR=%DIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPS=%DIR%.mvn\wrapper\maven-wrapper.properties"

if not exist "%WRAPPER_PROPS%" (
  echo ERROR: %WRAPPER_PROPS% not found
  exit /b 1
)

REM 读取 wrapperUrl（取最后一行 wrapperUrl=）
for /f "tokens=*" %%i in ('findstr /b "wrapperUrl=" "%WRAPPER_PROPS%"') do set "WRAPPER_URL=%%i"
set "WRAPPER_URL=%WRAPPER_URL:wrapperUrl=%"

if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper jar from %WRAPPER_URL%
  if not exist "%DIR%.mvn\wrapper" mkdir "%DIR%.mvn\wrapper"
  powershell -Command "(New-Object System.Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"
)

if "%JAVA_HOME%"=="" (
  set "JAVA_CMD=java"
) else (
  set "JAVA_CMD=%JAVA_HOME%\bin\java"
)

"%JAVA_CMD%" -jar "%WRAPPER_JAR%" %*
endlocal
