package warungaju;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Self-check rumus laporan. Jalankan: java warungaju.CekLaporan
 * (tanpa JUnit, cukup assert).
 */
public class CekLaporan {

    public static void main(String[] args) {
        List<Transaksi> data = new ArrayList<>();
        data.add(new Transaksi(LocalDate.now(), "Pemasukan", "Penjualan", 1000, 400, "jual", "A"));
        data.add(new Transaksi(LocalDate.now(), "Pengeluaran", "Operasional", 200, "listrik"));

        List<Produk> produk = new ArrayList<>();
        produk.add(new Produk("A", 400, 1000, 5, 1)); // stok 5 x HPP 400 = 2000

        LaporanKeuangan keuangan = new LaporanKeuangan(data, produk);

        Laporan.Baris labaBersih = new LaporanLabaRugi(data).baris().get(4);
        cek("laba bersih", 400, labaBersih.nilai); // 1000 - 400 - 200

        Laporan.Baris hpp = new LaporanLabaRugi(data).baris().get(1);
        cek("hpp dari transaksi", 400, hpp.nilai);

        Laporan.Baris arus = new LaporanArusKas(data).baris().get(2);
        cek("arus kas bersih", 800, arus.nilai);

        Laporan.Baris aset = new LaporanNeraca(data, produk).baris().get(2);
        cek("total aset", 2800, aset.nilai); // kas 800 + stok 2000

        cek("polimorfisme: 3 laporan", 3, keuangan.semua().size());
        cek("pemasukan()", 1000, keuangan.pemasukan());
        cek("pengeluaran()", 200, keuangan.pengeluaran());

        cekDitolak("nominal negatif", () ->
            new Transaksi(LocalDate.now(), "Pemasukan", "Penjualan", -5000, "salah"));
        cekDitolak("nominal nol", () ->
            new Transaksi(LocalDate.now(), "Pemasukan", "Penjualan", 0, "salah"));
        cekDitolak("jenis asing", () ->
            new Transaksi(LocalDate.now(), "Transfer", "Penjualan", 1000, "salah"));
        cekDitolak("kategori kosong", () ->
            new Transaksi(LocalDate.now(), "Pemasukan", "  ", 1000, "salah"));
        cekDitolak("tanggal null", () ->
            new Transaksi(null, "Pemasukan", "Penjualan", 1000, "salah"));
        cekDitolak("hpp negatif", () ->
            new Transaksi(LocalDate.now(), "Pemasukan", "Penjualan", 1000, -1, "salah", "A"));

        // Pembagian dengan nol dijaga di constructor, jadi getHpp()/getMargin() aman.
        cekDitolak("harga jual nol", () -> new Produk("B", 400, 0, 5, 10));
        cekDitolak("harga jual negatif", () -> new Produk("B", 400, -1000, 5, 10));
        cekDitolak("jumlah produksi nol", () -> new Produk("B", 400, 1000, 5, 0));
        cekDitolak("jumlah produksi negatif", () -> new Produk("B", 400, 1000, 5, -2));

        Produk p = new Produk("B", 4000, 1000, 5, 4);
        cek("hpp per unit", 1000, p.getHpp());
        cek("margin persen", 0, p.getMargin());

        System.out.println("Semua cek laporan lolos.");
    }

    private static void cekDitolak(String nama, Runnable aksi) {
        try {
            aksi.run();
        } catch (ValidasiException ex) {
            return;
        }
        throw new AssertionError(nama + ": seharusnya ditolak ValidasiException");
    }

    private static void cek(String nama, double harapan, double hasil) {
        if (Math.abs(harapan - hasil) > 0.001) {
            throw new AssertionError(nama + ": harapan " + harapan + " tapi " + hasil);
        }
    }
}
