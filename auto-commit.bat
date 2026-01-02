@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo Starting auto-commit script...
echo.

powershell -NoExit -ExecutionPolicy Bypass -File ".\auto-commit.ps1" -interval 600

pause
