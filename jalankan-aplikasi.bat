@echo off
REM ============================================================
REM  Warung Aju - jalankan aplikasi desktop
REM  Akses Supabase langsung; backend JDBC lokal tidak diperlukan.
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
