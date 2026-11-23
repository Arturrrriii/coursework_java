@echo off
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
if not exist build\tests mkdir build\tests
javac -encoding UTF-8 -cp "lib\pdfbox.jar;lib\fontbox.jar;lib\commons-logging.jar" -d build\classes src\main\java\metallurgy\*.java
if errorlevel 1 goto error
jar --create --file build\metallurg.jar -C build\classes .
if errorlevel 1 goto error
javac -encoding UTF-8 -cp "build\metallurg.jar;lib\*" -d build\tests src\test\java\metallurgy\*.java
if errorlevel 1 goto error
echo Build complete.
pause
exit /b 0
:error
echo Build failed. Check JDK 21+ and PATH.
pause
exit /b 1
