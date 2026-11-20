package warungaju;

import java.util.ArrayList;
import java.util.List;

public class PrediksiArusKas implements Perhitungan {
    private final double saldoAwal;
    private final double pemasukanBulanan;
    private final double pengeluaranBulanan;
    private final int jumlahBulan;

    public PrediksiArusKas() {
        this(0, 0, 0, 0);
    }

    public PrediksiArusKas(double saldoAwal, double pemasukanBulanan,
                           double pengeluaranBulanan, int jumlahBulan) {
        this.saldoAwal = saldoAwal;
        this.pemasukanBulanan = pemasukanBulanan;
        this.pengeluaranBulanan = pengeluaranBulanan;
        this.jumlahBulan = jumlahBulan;
    }

    public List<Month> prediksi(double saldoAwal, double pemasukanBulanan,
                                double pengeluaranBulanan, int bulan) {
        List<Month> result = new ArrayList<>();
        double saldo = saldoAwal;
        for (int i = 1; i <= bulan; i++) {
            saldo += pemasukanBulanan - pengeluaranBulanan;
            result.add(new Month("Bulan " + i, pemasukanBulanan, pengeluaranBulanan, saldo));
        }
        return result;
    }

    public List<Month> prediksi() {
        return prediksi(saldoAwal, pemasukanBulanan, pengeluaranBulanan, jumlahBulan);
    }

    @Override
    public double hitung() {
        List<Month> months = prediksi();
        return months.isEmpty() ? saldoAwal : months.get(months.size() - 1).saldo;
    }

    @Override
    public String label() {
        return "Saldo akhir";
    }

    public static class Month {
        public final String nama;
        public final double pemasukan;
        public final double pengeluaran;
        public final double saldo;

        Month(String nama, double pemasukan, double pengeluaran, double saldo) {
            this.nama = nama;
            this.pemasukan = pemasukan;
            this.pengeluaran = pengeluaran;
            this.saldo = saldo;
        }
    }
}
