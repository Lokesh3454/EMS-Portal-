@echo off
title EMS Portal - Git Auto-Commit Watcher
echo Starting Git Auto-Commit Watcher...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0auto-commit.ps1"
pause
