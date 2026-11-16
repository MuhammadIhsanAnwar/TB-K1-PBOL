# Menjalankan WARUNGAJU

## Prasyarat

- JDK 8 atau lebih baru.
- Apache Maven 3.8 atau lebih baru.
- VS Code dengan Extension Pack for Java.

Jika muncul pesan `'mvn' is not recognized`, berarti Maven belum terpasang atau
belum masuk ke `PATH`.

### Instalasi Maven di Windows

1. Pastikan JDK sudah tersedia. Buka PowerShell dan jalankan:

   ```powershell
   java -version
   ```

2. Unduh file **Binary zip archive** Maven dari
   https://maven.apache.org/download.cgi.
3. Ekstrak file ZIP tersebut. Contoh hasil ekstraksi:

   ```text
   C:\Tools\apache-maven-3.9.x
   ```

   Di dalam folder tersebut harus terdapat subfolder `bin` yang berisi
   `mvn.cmd`.

4. Buka menu Windows **Edit the system environment variables**, lalu klik
   **Environment Variables**.
5. Pada bagian **User variables**, klik **New** dan isi:

   ```text
   Variable name: MAVEN_HOME
   Variable value: C:\Tools\apache-maven-3.9.x
   ```

   Sesuaikan nama folder dengan versi Maven yang Anda ekstrak.

6. Pada bagian **User variables**, pilih variable **Path**, klik **Edit**,
   klik **New**, lalu tambahkan:

   ```text
   %MAVEN_HOME%\bin
   ```

   Jangan menambahkan `%MAVEN_HOME%\bin` sebagai variable baru. Nilai tersebut
   harus menjadi salah satu baris di dalam variable **Path**.

7. Klik **OK** sampai semua jendela pengaturan tertutup. Tutup semua terminal
   PowerShell atau terminal VS Code yang sedang terbuka, lalu buka terminal
   baru agar perubahan `Path` terbaca.
8. Verifikasi instalasi:

```powershell
mvn -version
```

Jika perintah tersebut menampilkan versi Maven dan Java, instalasi berhasil dan
proyek siap dikompilasi.

### Jika dialog Path menampilkan "This environment variable is too large"

Dialog tersebut berarti nilai `Path` pengguna sudah melebihi batas yang dapat
ditampilkan editor Windows. Jangan menempelkan seluruh isi `Path` ke variable
baru dan jangan menghapus isi `Path` lama. Gunakan PowerShell untuk menambahkan
Maven tanpa dialog:

```powershell
$maven = "C:\Tools\apache-maven-3.10.0"
[Environment]::SetEnvironmentVariable("MAVEN_HOME", $maven, "User")
$path = [Environment]::GetEnvironmentVariable("Path", "User")
if ($path -notlike "*$maven\bin*") {
    [Environment]::SetEnvironmentVariable("Path", $path.TrimEnd(";") + ";" + $maven + "\bin", "User")
}
```

Sesuaikan nama folder jika versi Maven berbeda. Tutup semua terminal VS Code,
buka terminal baru, lalu jalankan `mvn -version`.

## Menjalankan dari terminal

Buka terminal pada folder `TB-K1-PBOL`, lalu jalankan:

```powershell
mvn compile
mvn exec:java
```

Maven akan mengambil FlatLaf dan Apache POI dari `pom.xml`. Class utama aplikasi
adalah `warungaju.Main`. FlatLaf digunakan sebagai Look and Feel modern untuk
seluruh komponen Swing, sedangkan animasi menu dibuat dengan `javax.swing.Timer`.

## Menjalankan dari VS Code

1. Buka folder `TB-K1-PBOL`.
2. Tunggu Java Language Server menyelesaikan resolusi dependensi Maven.
3. Buka `src/main/java/warungaju/Main.java`.
4. Tekan **Run Java** atau **F5**.

Aplikasi dimulai dengan data contoh. Tambahkan produk terlebih dahulu sebelum mengimpor penjualan Excel agar validasi nama produk dapat dilakukan. Template Excel dapat dibuat dari halaman **Transaksi**, lalu data aktual dapat diimpor dari file `.xlsx` yang sudah diisi.

## Modul

- `MainFrame` mengatur header, sidebar, dan `CardLayout`.
- Model `Produk` dan `Transaksi` menyimpan data aplikasi.
- `SimulasiBisnis`, `PrediksiArusKas`, dan `LaporanKeuangan` menangani perhitungan.
- `ExcelService` membuat template dan mengimpor data menggunakan Apache POI.

## Supabase dan akun online

Aplikasi memakai Supabase Auth untuk pendaftaran/login. Operasi database
dijalankan oleh layanan backend Java melalui PostgreSQL JDBC; aplikasi desktop
mengirim access token ke backend, dan backend memvalidasinya ke Supabase Auth
sebelum menjalankan transaksi dengan hak `authenticated` agar kebijakan RLS
tetap berlaku. Driver PostgreSQL JDBC dikelola Maven dari `pom.xml`.

URL Supabase dan publishable key dapat diatur melalui `SUPABASE_URL` dan
`SUPABASE_PUBLISHABLE_KEY`. Aplikasi desktop memakai backend lokal
`http://127.0.0.1:8081` secara default; untuk backend yang telah di-deploy,
atur `WARUNGAJU_API_URL` ke URL HTTPS layanan tersebut.

Sebelum aplikasi digunakan:

1. Buka `src/main/resources/db/supabase/online_schema.sql` dan jalankan seluruh
   isinya melalui **Supabase Dashboard → SQL Editor**. Ini membuat tabel
   `public.users`, role, tabel workspace, sinkronisasi tanggal aktivitas dan
   verifikasi, serta kebijakan Row Level Security.
2. Dari dashboard Supabase, ambil connection string PostgreSQL dan kredensial
   database untuk server tepercaya. Simpan hanya pada environment server, bukan
   pada komputer klien atau repository. Untuk Supavisor transaction pooler,
   gunakan URL seperti berikut dan sesuaikan host, project ref, serta password:

   ```text
   jdbc:postgresql://<pooler-host>:6543/postgres?sslmode=require&prepareThreshold=0
   ```

   Di PowerShell, atur variabel untuk proses backend (password dimasukkan
   secara tersembunyi):

   ```powershell
   $env:WARUNGAJU_DB_URL = "jdbc:postgresql://<pooler-host>:6543/postgres?sslmode=require&prepareThreshold=0"
   $env:WARUNGAJU_DB_USER = "postgres.<project-ref>"
   $securePassword = Read-Host "Supabase database password" -AsSecureString
   $env:WARUNGAJU_DB_PASSWORD = [Net.NetworkCredential]::new("", $securePassword).Password
   $env:WARUNGAJU_API_BIND = "127.0.0.1"
   mvn exec:java "-Dexec.mainClass=warungaju.SupabaseJdbcServer"
   ```

   Backend harus dijalankan pada server tepercaya. Jika diakses dari internet,
   pasang reverse proxy HTTPS dan firewall; jangan mengekspos listener HTTP
   langsung. Password database tidak boleh dibundel bersama aplikasi desktop.
3. Jalankan aplikasi desktop di terminal lain dengan `mvn exec:java`. Jika
   backend berjalan pada host lain, atur `WARUNGAJU_API_URL` ke alamat HTTPS
   backend sebelum menjalankan aplikasi.
4. Daftarkan akun. Pendaftaran dari aplikasi selalu
   membuat role `umkm`; verifikasi email bila Supabase memintanya.
5. Buat akun `warungaju@neoverse.my.id` di **Supabase Dashboard →
   Authentication → Users** (atau daftar lewat aplikasi), tetapkan password di
   Supabase Auth, lalu verifikasi emailnya. SQL tidak membuat akun Auth ataupun
   menyimpan password. Setelah akun terverifikasi, jalankan blok promosi Super
   Admin yang ada di `online_schema.sql`; jika migrasi dijalankan sebelum akun
   dibuat, jalankan ulang blok berikut:

   ```sql
   do $$
   declare
       admin_user_id uuid;
   begin
       select id into admin_user_id
       from auth.users
       where lower(email) = lower('warungaju@neoverse.my.id')
         and email_confirmed_at is not null;

       if admin_user_id is null then
           raise notice 'Buat dan verifikasi akun di Supabase Auth terlebih dahulu.';
       else
           update public.users
           set role = 'super_admin', is_active = true
           where id = admin_user_id;
       end if;
   end;
   $$;
   ```

6. Masuk kembali dengan akun tersebut untuk membuka panel Super Admin. Panel
   hanya menampilkan profil dengan `is_active = true`. Akun UMKM masuk ke
   dashboard UMKM.

Jika ingin membuat akun Super Admin baru, buat/invite akun terlebih dahulu
melalui **Supabase Dashboard → Authentication → Users** agar kredensial dikelola
oleh Supabase Auth, lalu jalankan SQL berikut dengan email akun tersebut:

```sql
update public.users
set role = 'super_admin'
where lower(email) = lower('admin@example.com')
returning id, email, nama, role;
```

Tabel `public.users` berisi `id`, `email`, `nama`, `role`, `is_active`,
`last_active_at`, `created_at`, dan `verified_at`. Kolom password sengaja tidak
ada: kata sandi disimpan dan diverifikasi oleh Supabase Auth di `auth.users`
dalam bentuk terproteksi, bukan diduplikasi ke tabel aplikasi. Jangan mencoba
membuat akun dengan `INSERT` langsung ke `auth.users`.

Transaksi, produk, pengaturan simulasi, dan input prediksi arus kas disimpan per
akun. Data yang sama akan dimuat saat akun masuk dari perangkat lain. Akses data
dibatasi oleh RLS berdasarkan user yang sedang login; role Super Admin tidak
memberi akses ke workspace keuangan UMKM.

Jangan pernah memasukkan Supabase **secret/service-role key** ke aplikasi,
repository, atau environment variable di komputer klien. Aplikasi hanya memakai
publishable key bersama access token hasil login.

### Pengirim email konfirmasi

Pengaturan SMTP Supabase harus dilakukan di **Supabase Dashboard → Project
Settings → Authentication → SMTP Settings** (nama menu dapat berbeda sedikit).
Aktifkan custom SMTP dan masukkan:

- Sender name: `WARUNGAJU`
- Sender email: `warungaju@neoverse.my.id`
- SMTP host: `mail.neoverse.my.id`
- SMTP port: `465` (SSL/TLS)
- SMTP username: `warungaju@neoverse.my.id`
- SMTP password: masukkan password mailbox di dashboard saja; jangan simpan di
  source code, SQL, atau repository.

Simpan pengaturan, pastikan domain email memiliki DNS/mail record yang valid,
lalu uji pendaftaran dan pengiriman ulang email konfirmasi. Jika provider SMTP
menolak koneksi port 465, periksa port submission TLS yang didukung provider
email dan setel port yang sesuai di dashboard.

Jika proses daftar menampilkan `Read timed out`, respons Supabase mungkin
terlambat saat memproses pengiriman email. Akun bisa saja sudah tercatat walaupun
aplikasi belum menerima respons. Periksa **Authentication → Users**, email
konfirmasi (termasuk folder spam), dan **Auth Logs** sebelum mencoba daftar lagi.
Jika muncul HTTP `504`, cek **Auth Logs** dan status project Supabase; periksa
juga konfigurasi, koneksi, serta limit pengiriman SMTP. Jangan langsung
mengulangi pendaftaran sebelum memastikan apakah akun sudah terbentuk.

Kredensial SMTP adalah rahasia server-side dan tidak boleh dimasukkan ke aplikasi
desktop. Karena password mailbox sempat dibagikan di chat, ganti/rotasi password
tersebut sebelum memasukkannya ke dashboard Supabase.
