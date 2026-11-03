package warungaju;

public class SimulasiBisnis {
    public Result hitung(double bahanBaku, int karyawan, double gaji, double jam, double target, double harga) {
        double hpp = bahanBaku * target, operasional = (karyawan * gaji) + (jam * 25000), pendapatan = target * harga;
        double laba = pendapatan - hpp - operasional;
        return new Result(hpp, operasional, pendapatan, laba, pendapatan == 0 ? 0 : laba / pendapatan * 100);
    }
    public static class Result {
        public final double hpp, operasional, pendapatan, laba, margin;
        Result(double hpp,double operasional,double pendapatan,double laba,double margin){this.hpp=hpp;this.operasional=operasional;this.pendapatan=pendapatan;this.laba=laba;this.margin=margin;}
    }
}
