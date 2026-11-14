package warungaju;

import java.util.ArrayList;
import java.util.List;

/**
 * Kumpulan laporan yang bisa dibuat dari data transaksi dan produk.
 *
 * Kelas ini yang memegang polymorphism: semua laporan disimpan sebagai
 * List<Laporan>, lalu UI memanggil judul() dan baris() tanpa instanceof.
 *
 * Dipertahankan juga pemasukan()/pengeluaran() karena DashboardPanel masih
 * memakainya.
 */
public class LaporanKeuangan {

    private final List<Transaksi> transaksi;
    private final List<Produk> produk;

    public LaporanKeuangan(List<Transaksi> transaksi, List<Produk> produk) {
        this.transaksi = transaksi == null ? new ArrayList<Transaksi>() : transaksi;
        this.produk = produk == null ? new ArrayList<Produk>() : produk;
    }

    /** Semua laporan yang tersedia, siap di-loop oleh UI. */
    public List<Laporan> semua() {
        List<Laporan> result = new ArrayList<>();
        result.add(new LaporanLabaRugi(transaksi));
        result.add(new LaporanArusKas(transaksi));
        result.add(new LaporanNeraca(transaksi, produk));
        return result;
    }

    public double pemasukan() {
        return new LaporanArusKas(transaksi).baris().get(0).nilai;
    }

    public double pengeluaran() {
        return new LaporanArusKas(transaksi).baris().get(1).nilai;
    }
}
