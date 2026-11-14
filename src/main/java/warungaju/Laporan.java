package warungaju;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dasar semua laporan keuangan.
 *
 * Satu class abstrak, tiga subclass (LabaRugi, ArusKas, Neraca). Panel cukup
 * menyimpan List<Laporan> lalu memanggil generate() dan baris() tanpa peduli
 * laporan jenis apa yang sedang dipegang.
 */
public abstract class Laporan {

    protected final List<Transaksi> data;

    protected Laporan(List<Transaksi> data) {
        this.data = data == null ? Collections.<Transaksi>emptyList() : data;
    }

    /** Nama laporan, dipakai sebagai judul tab. */
    public abstract String judul();

    /**
     * Isi laporan sebagai baris siap tampil: {label, nilai, jenis}.
     * jenis dipakai UI untuk mewarnai baris: "total" untuk baris hasil akhir.
     */
    public abstract List<Baris> baris();

    /** Teks laporan polos, berguna untuk ekspor atau log. */
    public String generate() {
        StringBuilder text = new StringBuilder(judul()).append('\n');
        for (Baris b : baris()) {
            text.append(b.label).append(": ").append(UI.money(b.nilai)).append('\n');
        }
        return text.toString();
    }

    // ---------- Helper yang dipakai semua subclass ----------

    protected double totalJenis(String jenis) {
        double total = 0;
        for (Transaksi t : data) {
            if (jenis.equals(t.getJenis())) total += t.getNominal();
        }
        return total;
    }

    protected double totalHpp() {
        double total = 0;
        for (Transaksi t : data) {
            total += t.getHpp();
        }
        return total;
    }

    protected List<Baris> list(Baris... items) {
        List<Baris> result = new ArrayList<>();
        Collections.addAll(result, items);
        return result;
    }

    /** Satu baris laporan. */
    public static class Baris {
        public final String label;
        public final double nilai;
        public final boolean penekanan;

        public Baris(String label, double nilai) {
            this(label, nilai, false);
        }

        public Baris(String label, double nilai, boolean penekanan) {
            this.label = label;
            this.nilai = nilai;
            this.penekanan = penekanan;
        }
    }
}
