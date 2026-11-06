@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ==========================================
echo   Hospital Appointment Frontend
echo   Folder: %CD%
echo ==========================================
echo.

if not exist "node_modules" (
  echo Installing dependencies...
  call npm install
  if errorlevel 1 (
    echo npm install failed.
    pause
    exit /b 1
  )
)

echo Opening: http://localhost:3000/#/login
echo Keep this window open. Press Ctrl+C to stop.
echo.

start "" "http://localhost:3000/#/login"
call npm run dev

pause
