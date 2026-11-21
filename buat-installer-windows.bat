@echo off
setlocal
cd /d "%~dp0"

where jpackage >nul 2>nul
if errorlevel 1 (
    echo jpackage tidak ditemukan. Pasang JDK 17 atau lebih baru dan tambahkan bin ke PATH.
    exit /b 1
)

set "WIX_BIN="
for /d %%D in ("%ProgramFiles(x86)%\WiX Toolset v3.*") do (
    if not defined WIX_BIN if exist "%%~fD\bin\candle.exe" if exist "%%~fD\bin\light.exe" set "WIX_BIN=%%~fD\bin"
)
if not defined WIX_BIN (
    for /d %%D in ("%ProgramFiles%\WiX Toolset v3.*") do (
        if not defined WIX_BIN if exist "%%~fD\bin\candle.exe" if exist "%%~fD\bin\light.exe" set "WIX_BIN=%%~fD\bin"
    )
)
if not defined WIX_BIN (
    echo WiX Toolset 3 tidak ditemukan di Program Files.
    echo Pastikan candle.exe dan light.exe ada di folder bin WiX Toolset v3.
    exit /b 1
)

REM Tambahkan WiX hanya ke PATH proses ini; tidak mengubah PATH permanen Windows.
set "PATH=%WIX_BIN%;%PATH%"

if exist "target\installer\Warung Aju-1.0.0.exe" (
    echo Installer lama ditemukan di target\installer.
    echo Pindahkan atau hapus installer lama sebelum membuat ulang.
    exit /b 1
)

call mvn -q package
if errorlevel 1 (
    echo Gagal membuat JAR aplikasi.
    exit /b 1
)

if not exist "target\jpackage-input" mkdir "target\jpackage-input"
if not exist "target\installer" mkdir "target\installer"
copy /Y "target\warungaju-1.0.0.jar" "target\jpackage-input\warungaju.jar" >nul
if errorlevel 1 (
    echo Gagal menyiapkan JAR untuk installer.
    exit /b 1
)

jpackage ^
    --type exe ^
    --name "Warung Aju" ^
    --app-version 1.0.0 ^
    --vendor "Warung Aju" ^
    --description "Aplikasi perencanaan keuangan UMKM" ^
    --input target\jpackage-input ^
    --main-jar warungaju.jar ^
    --main-class warungaju.Main ^
    --dest target\installer ^
    --win-menu ^
    --win-menu-group "Warung Aju" ^
    --win-shortcut ^
    --win-dir-chooser ^
    --win-per-user-install
if errorlevel 1 (
    echo Gagal membuat installer. Periksa versi WiX, JDK, dan output di atas.
    exit /b 1
)

echo Installer tersedia di target\installer\Warung Aju-1.0.0.exe
