package warungaju;

public class Produk {
    private String nama;
    private double biayaProduksi;
    private double hargaJual;
    private int stok;
    private int jumlahProduksi;

    public Produk(String nama, double biayaProduksi, double hargaJual, int stok) {
        this.nama = nama; this.biayaProduksi = biayaProduksi; this.hargaJual = hargaJual; this.stok = stok; this.jumlahProduksi = 1;
    }
    public String getNama() { return nama; }
    public double getBiayaProduksi() { return biayaProduksi; }
    public double getHargaJual() { return hargaJual; }
    public int getStok() { return stok; }
    public double getHpp() { return biayaProduksi / Math.max(1, jumlahProduksi); }
    public double getMargin() { return hargaJual == 0 ? 0 : (hargaJual - getHpp()) / hargaJual * 100; }
    public void setNama(String nama) { this.nama = nama; }
    public void setBiayaProduksi(double value) { biayaProduksi = value; }
    public void setHargaJual(double value) { hargaJual = value; }
    public void setStok(int value) { stok = value; }
}
