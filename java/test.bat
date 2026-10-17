@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 -d build src\HospitalStore.java src\HospitalManagementSystemGUI.java src\HospitalSystemTest.java
if errorlevel 1 (
  pause
  exit /b 1
)
java -Djava.awt.headless=true -cp build HospitalSystemTest
if errorlevel 1 (
  echo Tests failed.
  pause
  exit /b 1
)
pause
