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
