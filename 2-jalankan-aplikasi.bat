@echo off
REM ============================================================
REM  WARUNGAJU - jalankan aplikasi desktop
REM  Jalankan SETELAH 1-jalankan-backend.bat siap.
REM ============================================================
cd /d "%~dp0"

if not exist "target\classes\warungaju\Main.class" (
    echo Mengompilasi...
    call mvn -q compile
    if errorlevel 1 (
        echo GAGAL kompilasi.
        pause
        exit /b 1
    )
)

call mvn -q exec:java
pause
