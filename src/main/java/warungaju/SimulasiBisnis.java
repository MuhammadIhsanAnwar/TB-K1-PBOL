package warungaju;

public class SimulasiBisnis implements Perhitungan {
    private static final double BIAYA_PER_JAM_OPERASIONAL = 25000;

    private final double bahanBaku;
    private final int karyawan;
    private final double gaji;
    private final double jam;
    private final double target;
    private final double harga;
    private Result hasilTerakhir;

    public SimulasiBisnis() {
        this(0, 0, 0, 0, 0, 0);
    }

    public SimulasiBisnis(double bahanBaku, int karyawan, double gaji, double jam,
                          double target, double harga) {
        this.bahanBaku = bahanBaku;
        this.karyawan = karyawan;
        this.gaji = gaji;
        this.jam = jam;
        this.target = target;
        this.harga = harga;
    }

    public Result hitung(double bahanBaku, int karyawan, double gaji,
                         double jam, double target, double harga) {
        double hpp = bahanBaku * target;
        double operasional = (karyawan * gaji) + (jam * BIAYA_PER_JAM_OPERASIONAL);
        double pendapatan = target * harga;
        double laba = pendapatan - hpp - operasional;
        hasilTerakhir = new Result(hpp, operasional, pendapatan, laba,
            pendapatan == 0 ? 0 : laba / pendapatan * 100);
        return hasilTerakhir;
    }

    @Override
    public double hitung() {
        return hitung(bahanBaku, karyawan, gaji, jam, target, harga).laba;
    }

    @Override
    public String label() {
        return "Laba bersih";
    }

    public Result getHasilTerakhir() {
        if (hasilTerakhir == null) {
            throw new IllegalStateException("Simulasi belum dihitung.");
        }
        return hasilTerakhir;
    }

    public static class Result {
        public final double hpp, operasional, pendapatan, laba, margin;

        Result(double hpp, double operasional, double pendapatan, double laba, double margin) {
            this.hpp = hpp;
            this.operasional = operasional;
            this.pendapatan = pendapatan;
            this.laba = laba;
            this.margin = margin;
        }
    }
}
