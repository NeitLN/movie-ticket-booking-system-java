@echo off
chcp 65001 > nul
setlocal

echo [1/3] Cleaning old build...
if exist out rmdir /s /q out
mkdir out

echo [2/3] Compiling Java Swing UI...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    echo.
    echo Compile failed. Please check your JDK installation.
    pause
    exit /b 1
)

echo [3/3] Opening Bittersweet Cinemas UI...
java -Dfile.encoding=UTF-8 -cp out movieticketbooking.Main
pause
