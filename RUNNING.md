# Menjalankan Warung Aju

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

## Cara cepat menjalankan di Windows

Double-click `2-jalankan-aplikasi.bat`. Aplikasi mengakses Supabase Auth dan
Data API langsung melalui HTTPS, jadi tidak perlu `.env` atau backend lokal.
Komputer harus terhubung ke internet.

## Menjalankan lewat Maven

Jalankan aplikasi desktop dengan satu perintah:

```powershell
mvn exec:java
```

## Supabase dan akun online

Pendaftaran, login, reset password, profil, workspace, dan RPC akun menggunakan
Supabase Auth/Data API secara langsung melalui HTTPS. Setiap permintaan data
membawa publishable key dan access token pengguna; Supabase menerapkan Row
Level Security (RLS). Tidak ada JDBC connection string atau alamat backend yang
perlu diatur pada komputer pengguna.

Alamat URL dan publishable key Supabase tertanam sebagai konfigurasi publik
aplikasi; tidak perlu diisi melalui environment variable.
`SupabaseJdbcServer` dan PostgreSQL JDBC driver masih tersedia untuk penggunaan
backend manual/legacy, tetapi bukan bagian dari alur aplikasi desktop ini.

Sebelum aplikasi digunakan:

1. Buka `src/main/resources/db/supabase/online_schema.sql` dan jalankan seluruh
   isinya melalui **Supabase Dashboard → SQL Editor**. Skrip ini membuat tabel,
   kebijakan RLS, trigger, dan fungsi RPC yang digunakan aplikasi.
2. Pastikan **Data API** aktif dan schema `public` tersedia untuk Data API.
   Jangan menonaktifkan grants atau kebijakan RLS yang dibuat oleh schema.
3. Di pengaturan **Authentication → Providers → Email**, aktifkan **Confirm
   email** agar Supabase mengirim OTP verifikasi. Pastikan **Token length** 6.
   Jika `mailer_autoconfirm` bernilai `true` pada `/auth/v1/settings`, akun
   otomatis dikonfirmasi dan email verifikasi tidak dikirim. Konfigurasikan
   SMTP Supabase; password SMTP hanya disimpan di dashboard.
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
   dashboard UMKM. Super Admin dapat mengganti kata sandinya melalui tombol
   **Ubah Kata Sandi**; akun ber-role Super Admin tidak bisa dinonaktifkan atau
   dihapus dari panel.

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

OTP email tidak memerlukan tabel OTP di `public`. Supabase Auth yang membuat,
menyimpan sementara, mengirim, memeriksa masa berlaku, dan memvalidasi OTP.
Trigger `handle_new_user` pada `online_schema.sql` hanya menyinkronkan profil
ke `public.users`; Java meminta Supabase mengirim OTP dan meneruskan OTP yang
dimasukkan pengguna ke endpoint verifikasi Supabase Auth.

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
Konfirmasi bahwa **Confirm email** aktif di pengaturan provider Email, kemudian
aktifkan custom SMTP dan masukkan. Pengaturan SMTP yang sama dipakai untuk email
verifikasi dan email reset kata sandi:

- Sender name: `Warung Aju`
- Sender email: `warungaju@neoverse.my.id`
- SMTP host: `mail.neoverse.my.id`
- SMTP port: `465` (SSL/TLS)
- SMTP username: `warungaju@neoverse.my.id`
- SMTP password: masukkan password mailbox di dashboard saja; jangan simpan di
  source code, SQL, atau repository.

Simpan pengaturan, pastikan domain email memiliki DNS/mail record yang valid,
lalu uji pendaftaran, pengiriman ulang email konfirmasi, dan lupa kata sandi.
Jika provider SMTP
menolak koneksi port 465, periksa port submission TLS yang didukung provider
email dan setel port yang sesuai di dashboard.

Respons HTTP 2xx dari endpoint `signup`, `resend`, atau `recover` hanya berarti
permintaan diproses oleh Supabase Auth; respons tersebut tidak membuktikan email
sampai ke mailbox. Periksa **Authentication → Logs** untuk hasil pengiriman dan
pesan SMTP/provider. Aplikasi menampilkan kegagalan HTTP beserta status dan kode
error Supabase yang tersedia, tetapi tidak mencatat key, password, OTP, atau
token sesi.

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

Di layar login, **Lupa kata sandi?** membuka form tersendiri untuk memasukkan
email akun. Aplikasi meminta Supabase mengirim OTP pemulihan; pengguna
memasukkan OTP 6 angka dan kata sandi baru, lalu Supabase memvalidasi kode dan
memperbarui kata sandi.

### Template email OTP

Di **Authentication → Email Templates**, pastikan template **Confirm signup**
dan **Reset Password** menampilkan `{{ .Token }}` (kode OTP), bukan hanya
`{{ .ConfirmationURL }}` (tautan). Simpan template, lalu uji dengan alamat email
yang dapat diakses. Pengiriman tetap dilakukan oleh Supabase melalui SMTP yang
dikonfigurasi.

Jika pendaftaran menampilkan HTTP `504` atau gateway timeout, Supabase mungkin
sudah membuat akun sebelum pengiriman SMTP timeout. Jangan langsung mendaftar
ulang dengan email yang sama. Periksa **Authentication → Users**, email/spam,
dan **Auth Logs**. Layar OTP aplikasi menyediakan **Kirim ulang email** agar
permintaan OTP baru tetap dilakukan oleh Supabase.
