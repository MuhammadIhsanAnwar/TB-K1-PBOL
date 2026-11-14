package warungaju;

import java.time.LocalDate;

/**
 * Satu baris pemasukan atau pengeluaran usaha.
 *
 * Semua nilai divalidasi di constructor (encapsulation), jadi object Transaksi
 * tidak mungkin bernominal negatif atau berjenis asing, baik data dibuat dari
 * form, impor Excel, maupun muatan Supabase.
 */
public class Transaksi {

    /** Dua jenis yang dikenali laporan. */
    public static final String PEMASUKAN = "Pemasukan";
    public static final String PENGELUARAN = "Pengeluaran";

    private final LocalDate tanggal;
    private final String jenis, kategori, keterangan, produk;
    private final double nominal, hpp;

    public Transaksi(LocalDate tanggal, String jenis, String kategori, double nominal, String keterangan) {
        this(tanggal, jenis, kategori, nominal, 0, keterangan, "");
    }

    public Transaksi(LocalDate tanggal, String jenis, String kategori, double nominal, double hpp,
                     String keterangan, String produk) {
        this.tanggal = periksaTanggal(tanggal);
        this.jenis = periksaJenis(jenis);
        this.kategori = periksaTeks(kategori, "Kategori");
        this.nominal = periksaNominal(nominal);
        this.hpp = periksaHpp(hpp);
        this.keterangan = keterangan == null ? "" : keterangan.trim();
        this.produk = produk == null ? "" : produk.trim();
    }

    public LocalDate getTanggal(){return tanggal;} public String getJenis(){return jenis;} public String getKategori(){return kategori;}
    public double getNominal(){return nominal;} public double getHpp(){return hpp;} public String getKeterangan(){return keterangan;} public String getProduk(){return produk;}

    // ---------- Validasi ----------

    private static LocalDate periksaTanggal(LocalDate nilai) {
        if (nilai == null) {
            throw new ValidasiException("Tanggal transaksi tidak boleh kosong.");
        }
        return nilai;
    }

    private static String periksaJenis(String nilai) {
        if (nilai == null || nilai.trim().isEmpty()) {
            throw new ValidasiException("Jenis transaksi tidak boleh kosong.");
        }
        String bersih = nilai.trim();
        if (!PEMASUKAN.equals(bersih) && !PENGELUARAN.equals(bersih)) {
            throw new ValidasiException(
                "Jenis transaksi harus \"" + PEMASUKAN + "\" atau \"" + PENGELUARAN + "\".");
        }
        return bersih;
    }

    private static String periksaTeks(String nilai, String label) {
        if (nilai == null || nilai.trim().isEmpty()) {
            throw new ValidasiException(label + " tidak boleh kosong.");
        }
        return nilai.trim();
    }

    private static double periksaNominal(double nilai) {
        if (Double.isNaN(nilai) || Double.isInfinite(nilai)) {
            throw new ValidasiException("Nominal transaksi harus berupa angka yang valid.");
        }
        if (nilai <= 0) {
            throw new ValidasiException("Nominal transaksi harus lebih dari 0.");
        }
        return nilai;
    }

    private static double periksaHpp(double nilai) {
        if (Double.isNaN(nilai) || Double.isInfinite(nilai)) {
            throw new ValidasiException("HPP transaksi harus berupa angka yang valid.");
        }
        if (nilai < 0) {
            throw new ValidasiException("HPP transaksi tidak boleh negatif.");
        }
        return nilai;
    }
}
