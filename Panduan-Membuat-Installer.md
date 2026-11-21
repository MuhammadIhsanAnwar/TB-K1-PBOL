# Koneksi Supabase dan rilis Windows

## Cara aplikasi terhubung

Untuk pemakaian desktop, alurnya langsung ke Supabase melalui HTTPS:

```text
WARUNGAJU.exe
    |-- /auth/v1/*  (masuk, daftar, verifikasi, reset password)
    |-- /rest/v1/*  (profil dan workspace)
    |-- /rest/v1/rpc/* (perubahan nama dan operasi akun)
    v
Supabase Auth + Data API
    |-- access token pengguna
    |-- Row Level Security (RLS)
    v
Database Supabase
```

Karena itu, aplikasi desktop tidak membutuhkan backend WARUNGAJU terpisah,
port database, JDBC connection string, atau file `.env` untuk mengakses data.
Komputer pengguna hanya membutuhkan koneksi internet dan installer aplikasi.

URL project Supabase dan publishable key adalah konfigurasi publik aplikasi.
Keduanya sudah menjadi nilai bawaan di `SupabaseClient`; aplikasi terpasang
mengirim publishable key bersama access token dari sesi pengguna. Permintaan
database berjalan sebagai pengguna yang sedang login sehingga kebijakan RLS
Supabase tetap membatasi data.

`SUPABASE_JWKS_URL` tidak diperlukan oleh aplikasi desktop untuk alur ini.
`SUPABASE_SECRET_KEY` **tidak boleh** dimasukkan ke source code, EXE, atau
installer karena memberikan akses istimewa. Secret key yang sempat dibagikan
harus dirotasi dari Supabase Dashboard. Hanya publishable key yang dipakai
aplikasi klien.

> Konsekuensi: akses data desktop menggunakan HTTPS/PostgREST, bukan JDBC.
> PostgreSQL JDBC driver dan `SupabaseJdbcServer` masih ada untuk mode backend
> manual/legacy, tetapi bukan bagian dari alur aplikasi desktop ini. Jika
> backend JDBC itu dijalankan secara manual, ia tetap membutuhkan environment
> database sendiri dan tidak diperlukan untuk EXE.

## Menyiapkan Supabase satu kali

1. Dari **Supabase Dashboard → SQL Editor**, jalankan
   `src/main/resources/db/supabase/online_schema.sql`. Skrip ini membuat tabel,
   kebijakan RLS, trigger, dan fungsi RPC yang digunakan aplikasi.
2. Pastikan **Data API** aktif dan schema `public` tersedia untuk Data API.
   Tabel `users` dan `umkm_workspaces` harus tetap memiliki grants serta
   kebijakan RLS dari skrip schema. Fungsi RPC untuk operasi profil/akun juga
   harus tersedia dan hanya diberikan kepada role `authenticated`.
3. Aktifkan **Confirm email** di Supabase Auth dan konfigurasi SMTP di dashboard
   jika pendaftaran harus mewajibkan verifikasi email. Kredensial SMTP hanya
   disimpan di dashboard Supabase, bukan di EXE.
4. Buat/verifikasi akun Super Admin sesuai petunjuk di `RUNNING.md`.

Semua pengguna membutuhkan internet dan project Supabase harus aktif. Tidak
dibuat database lokal; data dibaca dan disimpan pada project Supabase tersebut.

## Membuat installer Windows

Installer dibuat sebagai file setup Windows `.exe` menggunakan `jpackage`.
Installer menyertakan runtime Java, sehingga pengguna tidak perlu memasang JDK,
Maven, menjalankan backend lokal, atau membuat file `.env`. Aplikasi tetap
membutuhkan koneksi internet untuk mengakses Supabase.

### Persiapan di komputer pembuat

Pasang kebutuhan berikut pada komputer Windows yang akan membuat installer:

1. **JDK 17 atau lebih baru.** Tambahkan folder `bin` JDK ke `PATH`, lalu buka
   terminal baru dan pastikan `java`, `jpackage`, dan `javac` dapat ditemukan:

   ```powershell
   java -version
   jpackage --version
   javac -version
   ```

2. **Apache Maven.** Pastikan perintah berikut berhasil:

   ```powershell
   mvn -version
   ```

3. **WiX Toolset 3.** `jpackage` memerlukannya untuk menghasilkan installer
   `.exe`. Pasang dengan WinGet:

```powershell
winget install --source winget --id WiXToolset.WiXToolset --exact
```

Installer script mencari `candle.exe` dan `light.exe` langsung di folder
`Program Files\WiX Toolset v3.*\bin` atau
`Program Files (x86)\WiX Toolset v3.*\bin`, termasuk lokasi WiX v3.14. Script
menambahkan folder itu hanya ke PATH proses build yang sedang berjalan; PATH
Windows tidak perlu diubah secara permanen. Jadi tidak perlu mengetik lokasi
WiX atau menambahkannya ke PATH secara manual.

### Langkah membuat installer

Buka PowerShell di folder proyek yang berisi `pom.xml`, kemudian jalankan:

```powershell
mvn test
.\buat-installer-windows.bat
```

Skrip akan membuat JAR aplikasi, menyiapkannya untuk `jpackage`, lalu
menghasilkan installer. File hasilnya:

```text
target\installer\WARUNGAJU-1.0.0.exe
```

Jika proses berhenti karena file installer tersebut sudah ada, pindahkan atau
hapus file lama itu, lalu jalankan kembali `.\buat-installer-windows.bat`.

### Memasang dan membagikan aplikasi

Uji installer dengan menjalankan file `.exe` dan ikuti wizard pemasangan.
Setelah terpasang, aplikasi dapat dibuka dari shortcut Start Menu atau desktop.
Untuk komputer pengguna tidak perlu menyalin seluruh folder proyek, memasang
Java/Maven/WiX, mengisi URL, atau menjalankan server lokal. Pengguna hanya perlu
internet agar aplikasi terhubung ke Supabase.

Installer tidak menyertakan `.env` maupun kredensial database. Jangan pernah
memasukkan Supabase secret/service-role key atau password database ke dalam
source code, JAR, maupun installer.
