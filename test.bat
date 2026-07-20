@echo off
chcp 65001 > nul
setlocal EnableExtensions
cd /d "%~dp0"

set "MAP_DRIVE="
set "SOURCE_LIST="
set "TEST_SOURCE_LIST="
set "PUSHED="

echo [1/4] Cleaning old test build...
if exist "out" rmdir /s /q "out"
if exist "test-out" rmdir /s /q "test-out"
mkdir "out"
mkdir "test-out"
if errorlevel 1 goto :build_failed

echo [2/4] Preparing source files...
for %%D in (Z Y X W V U T S R Q P O N M L K J I H G F E D) do (
    if not exist "%%D:\" if not defined MAP_DRIVE set "MAP_DRIVE=%%D:"
)
if not defined MAP_DRIVE goto :mapping_failed

subst %MAP_DRIVE% "%CD%"
if errorlevel 1 goto :mapping_failed

set "SOURCE_LIST=%MAP_DRIVE%\.movie_sources_%RANDOM%_%RANDOM%.txt"
set "TEST_SOURCE_LIST=%MAP_DRIVE%\.movie_test_sources_%RANDOM%_%RANDOM%.txt"
pushd "%MAP_DRIVE%\"
if errorlevel 1 goto :mapping_failed
set "PUSHED=1"

dir /s /b "src\*.java" > "%SOURCE_LIST%"
dir /s /b "tests\*.java" > "%TEST_SOURCE_LIST%"

javac -encoding UTF-8 -d out @"%SOURCE_LIST%"
if errorlevel 1 goto :compile_failed

javac -encoding UTF-8 -cp out -d test-out @"%TEST_SOURCE_LIST%"
if errorlevel 1 goto :compile_failed

echo [3/4] Running business-rule regression tests...
java -Dfile.encoding=UTF-8 -cp "out;test-out" movieticketbooking.tests.BookingSystemRegressionTest
if errorlevel 1 goto :test_failed

echo [4/4] Running Swing integration smoke test...
java -Dfile.encoding=UTF-8 -cp "out;test-out" movieticketbooking.tests.GuiSmokeTest
if errorlevel 1 goto :test_failed

call :cleanup
echo.
echo All tests passed.
pause
exit /b 0

:build_failed
echo.
echo Test build failed: could not create output folders.
goto :failed

:mapping_failed
echo.
echo Test build failed: could not create a temporary drive mapping.
goto :failed

:compile_failed
echo.
echo Test compilation failed. Review the javac errors above.
goto :failed

:test_failed
echo.
echo One or more tests failed. Review the output above.

:failed
call :cleanup
pause
exit /b 1

:cleanup
if defined SOURCE_LIST if exist "%SOURCE_LIST%" del /q "%SOURCE_LIST%" > nul 2>&1
if defined TEST_SOURCE_LIST if exist "%TEST_SOURCE_LIST%" del /q "%TEST_SOURCE_LIST%" > nul 2>&1
if defined PUSHED popd
if defined MAP_DRIVE subst %MAP_DRIVE% /d > nul 2>&1
set "PUSHED="
set "SOURCE_LIST="
set "TEST_SOURCE_LIST="
set "MAP_DRIVE="
exit /b 0
