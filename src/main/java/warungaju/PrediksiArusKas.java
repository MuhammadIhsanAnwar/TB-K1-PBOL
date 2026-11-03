package warungaju;

import java.util.ArrayList;
import java.util.List;

public class PrediksiArusKas {
    public List<Month> prediksi(double saldoAwal, double pemasukanBulanan, double pengeluaranBulanan, int bulan) {
        List<Month> result = new ArrayList<Month>(); double saldo = saldoAwal;
        for (int i=1;i<=bulan;i++) { saldo += pemasukanBulanan - pengeluaranBulanan; result.add(new Month("Bulan "+i,pemasukanBulanan,pengeluaranBulanan,saldo)); }
        return result;
    }
    public static class Month {
        public final String nama; public final double pemasukan, pengeluaran, saldo;
        Month(String nama,double pemasukan,double pengeluaran,double saldo){this.nama=nama;this.pemasukan=pemasukan;this.pengeluaran=pengeluaran;this.saldo=saldo;}
    }
}
