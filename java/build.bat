@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>&1
if errorlevel 1 (
  echo A JDK is required. Install a JDK and add its bin folder to PATH.
  pause
  exit /b 1
)
if not exist build mkdir build
javac -encoding UTF-8 -d build src\HospitalStore.java src\HospitalManagementSystemGUI.java
if errorlevel 1 (
  pause
  exit /b 1
)
jar cfe HospitalManagementSystem.jar HospitalManagementSystemGUI -C build .
if errorlevel 1 (
  pause
  exit /b 1
)
echo Build completed. Open run.bat to launch.
pause
