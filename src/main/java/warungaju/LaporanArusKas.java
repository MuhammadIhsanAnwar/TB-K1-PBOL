package warungaju;

import java.util.List;

/**
 * Laporan arus kas sederhana: kas masuk, kas keluar, dan arus kas bersih.
 *
 * Nilai HPP tidak dikurangkan di sini karena pembelian bahan sudah tercatat
 * sebagai transaksi "Pengeluaran".
 */
public class LaporanArusKas extends Laporan {

    public LaporanArusKas(List<Transaksi> data) {
        super(data);
    }

    @Override
    public String judul() {
        return "Laporan Arus Kas";
    }

    @Override
    public List<Baris> baris() {
        double masuk = totalJenis("Pemasukan");
        double keluar = totalJenis("Pengeluaran");

        return list(
            new Baris("Kas masuk", masuk),
            new Baris("Kas keluar", keluar),
            new Baris("Arus kas bersih", masuk - keluar, true)
        );
    }
}
