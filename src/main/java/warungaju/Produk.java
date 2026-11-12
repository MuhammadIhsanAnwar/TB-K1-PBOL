package warungaju;

/**
 * Representasi satu produk UMKM.
 *
 * Semua atribut private (encapsulation). Validasi dilakukan di dalam
 * class ini, sehingga object Produk tidak mungkin berada dalam keadaan
 * tidak valid (nama kosong, harga negatif, stok negatif, dan sebagainya).
 */
public class Produk {

    private String nama;
    private double biayaProduksi;
    private double hargaJual;
    private int stok;
    private int jumlahProduksi;


    // Constructor lama tetap tersedia: jumlah produksi dianggap 1
    public Produk(String nama, double biayaProduksi, double hargaJual, int stok) {

        this(nama, biayaProduksi, hargaJual, stok, 1);
    }


    // biayaProduksi adalah total biaya untuk membuat sebanyak jumlahProduksi unit
    public Produk(String nama, double biayaProduksi, double hargaJual,
                  int stok, int jumlahProduksi) {

        this.nama = periksaNama(nama);
        this.biayaProduksi = periksaBiaya(biayaProduksi);
        this.hargaJual = periksaHargaJual(hargaJual);
        this.stok = periksaStok(stok);
        this.jumlahProduksi = periksaJumlahProduksi(jumlahProduksi);
    }


    // ---------- Getter ----------

    public String getNama() {
        return nama;
    }

    public double getBiayaProduksi() {
        return biayaProduksi;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public int getStok() {
        return stok;
    }

    public int getJumlahProduksi() {
        return jumlahProduksi;
    }


    // ---------- Perhitungan ----------

    // HPP per unit = total biaya produksi / jumlah unit yang diproduksi
    public double getHpp() {

        return biayaProduksi / jumlahProduksi;
    }


    // Margin dalam persen terhadap harga jual
    public double getMargin() {

        return (hargaJual - getHpp()) / hargaJual * 100;
    }


    // ---------- Setter (semuanya divalidasi) ----------

    public void setNama(String nama) {
        this.nama = periksaNama(nama);
    }

    public void setBiayaProduksi(double value) {
        this.biayaProduksi = periksaBiaya(value);
    }

    public void setHargaJual(double value) {
        this.hargaJual = periksaHargaJual(value);
    }

    public void setStok(int value) {
        this.stok = periksaStok(value);
    }

    public void setJumlahProduksi(int value) {
        this.jumlahProduksi = periksaJumlahProduksi(value);
    }


    // ---------- Kelola stok ----------

    public void tambahStok(int jumlah) {

        if (jumlah <= 0) {
            throw new ValidasiException("Jumlah stok yang ditambahkan harus lebih dari 0.");
        }

        stok += jumlah;
    }


    public void kurangiStok(int jumlah) {

        if (jumlah <= 0) {
            throw new ValidasiException("Jumlah stok yang dikurangi harus lebih dari 0.");
        }

        if (jumlah > stok) {
            throw new ValidasiException(
                "Stok tidak cukup. Stok " + nama + " saat ini: " + stok + "."
            );
        }

        stok -= jumlah;
    }


    // ---------- Validasi (dipakai constructor dan setter) ----------

    private static String periksaNama(String nama) {

        if (nama == null || nama.trim().isEmpty()) {
            throw new ValidasiException("Nama produk tidak boleh kosong.");
        }

        return nama.trim();
    }


    private static double periksaBiaya(double nilai) {

        if (Double.isNaN(nilai) || Double.isInfinite(nilai)) {
            throw new ValidasiException("Biaya produksi harus berupa angka yang valid.");
        }

        if (nilai < 0) {
            throw new ValidasiException("Biaya produksi tidak boleh negatif.");
        }

        return nilai;
    }


    private static double periksaHargaJual(double nilai) {

        if (Double.isNaN(nilai) || Double.isInfinite(nilai)) {
            throw new ValidasiException("Harga jual harus berupa angka yang valid.");
        }

        if (nilai <= 0) {
            throw new ValidasiException("Harga jual harus lebih dari 0.");
        }

        return nilai;
    }


    private static int periksaStok(int nilai) {

        if (nilai < 0) {
            throw new ValidasiException("Stok tidak boleh negatif.");
        }

        return nilai;
    }


    private static int periksaJumlahProduksi(int nilai) {

        if (nilai < 1) {
            throw new ValidasiException("Jumlah produksi minimal 1.");
        }

        return nilai;
    }
}