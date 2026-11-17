@echo off
REM ============================================================
REM  WARUNGAJU - jalankan backend database (SupabaseJdbcServer)
REM  Buka terminal INI DULU, biarkan terbuka.
REM
REM  Kredensial database dibaca dari file .env (lihat .env.example).
REM  Jangan pernah commit .env ke git.
REM ============================================================
setlocal
cd /d "%~dp0"

if not exist ".env" (
    echo.
    echo  File .env belum ada. Buat file .env di folder ini berisi:
    echo.
    echo    WARUNGAJU_DB_URL=jdbc:postgresql://HOST:PORT/postgres?sslmode=require^&prepareThreshold=0
    echo    WARUNGAJU_DB_USER=postgres.PROJECT_REF
    echo    WARUNGAJU_DB_PASSWORD=PASSWORD_DATABASE
    echo    WARUNGAJU_API_BIND=127.0.0.1
    echo.
    pause
    exit /b 1
)

REM Muat isi .env baris per baris (format: NAMA=nilai).
for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do (
    if not "%%A"=="" set "%%A=%%B"
)

if "%WARUNGAJU_DB_URL%"=="" (
    echo  WARUNGAJU_DB_URL kosong di .env
    pause
    exit /b 1
)

if not exist "cp.txt" (
    echo Menyiapkan classpath...
    call mvn -q dependency:build-classpath "-Dmdep.outputFile=cp.txt"
    if errorlevel 1 (
        echo GAGAL menyiapkan classpath. Pastikan Maven dan internet tersedia.
        pause
        exit /b 1
    )
)

if not exist "target\classes\warungaju\SupabaseJdbcServer.class" (
    echo Mengompilasi...
    call mvn -q compile
    if errorlevel 1 (
        echo GAGAL kompilasi.
        pause
        exit /b 1
    )
)

set /p CP=<cp.txt

echo.
echo  ============================================
echo   WARUNGAJU backend
echo   Tunggu sampai muncul:
echo   "WARUNGAJU JDBC API listening on 127.0.0.1:8081"
echo   Jangan tutup jendela ini.
echo  ============================================
echo.

java -cp "target\classes;%CP%" warungaju.SupabaseJdbcServer
pause
