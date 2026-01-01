@echo off
cd /d "%~dp0"
powershell -NoExit -Command ".\auto-commit.ps1 -interval 600"
pause
