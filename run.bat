
@echo off
REM ============================================================
REM  AI-Driven Closed-Loop Wireless Resource Management
REM  Run script for Windows
REM
REM  What this does:
REM    1. Compiles the Java backend (src/*.java) into an "out" folder
REM    2. Starts the backend, which trains the Q-Learning controller
REM       and then starts a small web server on http://localhost:8080
REM    3. Opens the dashboard in your default browser
REM ============================================================

echo Compiling Java backend...
if not exist out mkdir out
javac -d out src\*.java

if errorlevel 1 (
    echo.
    echo Compilation failed. Please check the error above.
    pause
    exit /b 1
)

echo Compilation successful.
echo Starting server... (this trains the AI controller, it may take a few seconds)

start "" http://localhost:8081

java -cp out Main

pause
