package warungaju;

/**
 * Kontrak untuk semua objek yang bisa menghasilkan satu nilai angka bisnis.
 *
 * Dipakai oleh class perhitungan (HPP, target penjualan, prediksi, simulasi)
 * supaya panel cukup memanggil hitung() tanpa tahu rumus di belakangnya.
 * Inilah bentuk polymorphism: satu tipe rujukan, banyak implementasi.
 */
public interface Perhitungan {

    /** Nilai hasil perhitungan. */
    double hitung();

    /** Label singkat untuk ditampilkan di UI, misalnya "Laba bersih". */
    String label();
}
