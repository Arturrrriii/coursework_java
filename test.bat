@echo off
cd /d "%~dp0"
java -Dfile.encoding=UTF-8 -cp "build\metallurg.jar;build\tests;lib\*" metallurgy.Tests
pause
