@echo off
cd /d "%~dp0"
java -version >nul 2>&1
if errorlevel 1 (
  echo Java is not found. Install JDK 21 or newer and add it to PATH.
  pause
  exit /b 1
)
java -Dfile.encoding=UTF-8 -cp "build\metallurg.jar;lib\*" metallurgy.Main
if errorlevel 1 pause
