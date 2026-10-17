@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>&1
if errorlevel 1 (
  echo Java was not found. Install Java 8 or later and reopen this file.
  pause
  exit /b 1
)
java -jar HospitalManagementSystem.jar
if errorlevel 1 pause
