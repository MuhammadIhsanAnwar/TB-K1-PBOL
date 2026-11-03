package warungaju;

import java.time.LocalDate;

public class Transaksi {
    private final LocalDate tanggal;
    private final String jenis, kategori, keterangan, produk;
    private final double nominal, hpp;
    public Transaksi(LocalDate tanggal, String jenis, String kategori, double nominal, String keterangan) {
        this(tanggal, jenis, kategori, nominal, 0, keterangan, "");
    }
    public Transaksi(LocalDate tanggal, String jenis, String kategori, double nominal, double hpp, String keterangan, String produk) {
        this.tanggal=tanggal; this.jenis=jenis; this.kategori=kategori; this.nominal=nominal; this.hpp=hpp; this.keterangan=keterangan; this.produk=produk;
    }
    public LocalDate getTanggal(){return tanggal;} public String getJenis(){return jenis;} public String getKategori(){return kategori;}
    public double getNominal(){return nominal;} public double getHpp(){return hpp;} public String getKeterangan(){return keterangan;} public String getProduk(){return produk;}
}
