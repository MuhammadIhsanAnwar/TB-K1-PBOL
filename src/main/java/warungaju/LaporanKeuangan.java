package warungaju;

import java.util.List;

public class LaporanKeuangan {
    public double pemasukan(List<Transaksi> data){return total(data,"Pemasukan");}
    public double pengeluaran(List<Transaksi> data){return total(data,"Pengeluaran");}
    private double total(List<Transaksi> data,String jenis){double n=0; for(Transaksi t:data)if(jenis.equals(t.getJenis()))n+=t.getNominal(); return n;}
}
