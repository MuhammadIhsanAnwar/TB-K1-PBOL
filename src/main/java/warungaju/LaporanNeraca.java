package warungaju;

import java.util.List;

/**
 * Neraca sederhana: aset = liabilitas + ekuitas.
 *
 * Aset dihitung dari kas (pemasukan - pengeluaran) ditambah nilai stok produk
 * pada harga beli. Liabilitas dianggap 0 karena aplikasi belum mencatat utang,
 * sehingga ekuitas = aset.
 */
public class LaporanNeraca extends Laporan {

    private final List<Produk> produk;

    public LaporanNeraca(List<Transaksi> data, List<Produk> produk) {
        super(data);
        this.produk = produk;
    }

    @Override
    public String judul() {
        return "Neraca Sederhana";
    }

    @Override
    public List<Baris> baris() {
        double kas = totalJenis("Pemasukan") - totalJenis("Pengeluaran");
        double stok = nilaiStok();
        double aset = kas + stok;
        double liabilitas = 0;

        return list(
            new Baris("Kas", kas),
            new Baris("Nilai stok produk", stok),
            new Baris("Total aset", aset, true),
            new Baris("Liabilitas", liabilitas),
            new Baris("Ekuitas", aset - liabilitas, true)
        );
    }

    /** Nilai stok = sisa stok x HPP per unit. */
    private double nilaiStok() {
        if (produk == null) return 0;
        double total = 0;
        for (Produk p : produk) {
            total += p.getStok() * p.getHpp();
        }
        return total;
    }
}
