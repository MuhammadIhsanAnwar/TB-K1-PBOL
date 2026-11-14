package warungaju;

import java.util.List;

/**
 * Laporan laba rugi.
 *
 * HPP diambil dari field hpp tiap transaksi (hasil impor Excel atau
 * produk terjual), bukan persentase karangan.
 */
public class LaporanLabaRugi extends Laporan {

    public LaporanLabaRugi(List<Transaksi> data) {
        super(data);
    }

    @Override
    public String judul() {
        return "Laporan Laba Rugi";
    }

    @Override
    public List<Baris> baris() {
        double pendapatan = totalJenis("Pemasukan");
        double hpp = totalHpp();
        double labaKotor = pendapatan - hpp;
        double beban = totalJenis("Pengeluaran");
        double labaBersih = labaKotor - beban;

        return list(
            new Baris("Total pendapatan", pendapatan),
            new Baris("Harga pokok penjualan", hpp),
            new Baris("Laba kotor", labaKotor, true),
            new Baris("Biaya operasional", beban),
            new Baris("Laba bersih", labaBersih, true)
        );
    }
}
