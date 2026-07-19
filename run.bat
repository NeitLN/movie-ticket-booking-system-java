@echo off
chcp 65001 > nul
setlocal EnableExtensions
cd /d "%~dp0"

set "MAP_DRIVE="
set "SOURCE_LIST="
set "PUSHED="

echo [1/3] Cleaning old build...
if exist "out" rmdir /s /q "out"
mkdir "out"
if errorlevel 1 (
    echo.
    echo Build failed: could not create the output folder.
    pause
    exit /b 1
)

echo [2/3] Compiling Java Swing UI...
for %%D in (Z Y X W V U T S R Q P O N M L K J I H G F E D) do (
    if not exist "%%D:\" if not defined MAP_DRIVE set "MAP_DRIVE=%%D:"
)

if not defined MAP_DRIVE (
    echo.
    echo Build failed: no unused drive letter is available for temporary path mapping.
    pause
    exit /b 1
)

subst %MAP_DRIVE% "%CD%"
if errorlevel 1 (
    echo.
    echo Build failed: could not create a temporary drive mapping.
    pause
    exit /b 1
)

set "SOURCE_LIST=%MAP_DRIVE%\.movie_sources_%RANDOM%_%RANDOM%.txt"
pushd "%MAP_DRIVE%\"
if errorlevel 1 (
    call :cleanup
    echo.
    echo Build failed: could not access the temporary drive mapping.
    pause
    exit /b 1
)
set "PUSHED=1"

dir /s /b "src\*.java" > "%SOURCE_LIST%"
if errorlevel 1 (
    call :cleanup
    echo.
    echo Build failed: no Java source files were found.
    pause
    exit /b 1
)

javac -encoding UTF-8 -d out @"%SOURCE_LIST%"
set "COMPILE_EXIT=%ERRORLEVEL%"
call :cleanup

if not "%COMPILE_EXIT%"=="0" (
    echo.
    echo Compilation failed with exit code %COMPILE_EXIT%. Review the javac errors above.
    pause
    exit /b %COMPILE_EXIT%
)

echo [3/3] Opening Bittersweet Cinemas UI...
java -Dfile.encoding=UTF-8 -cp out movieticketbooking.Main
set "APP_EXIT=%ERRORLEVEL%"
pause
exit /b %APP_EXIT%

:cleanup
if defined SOURCE_LIST if exist "%SOURCE_LIST%" del /q "%SOURCE_LIST%" > nul 2>&1
if defined PUSHED popd
if defined MAP_DRIVE subst %MAP_DRIVE% /d > nul 2>&1
set "PUSHED="
set "SOURCE_LIST="
set "MAP_DRIVE="
exit /b 0
