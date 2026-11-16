@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -STA -File "%~dp0Instalar.ps1" -Launch
if errorlevel 1 pause
endlocal
