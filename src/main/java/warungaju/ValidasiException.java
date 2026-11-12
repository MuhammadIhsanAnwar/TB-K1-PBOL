package warungaju;

/**
 * Dilempar ketika data yang dimasukkan pengguna tidak valid
 * (misalnya nama kosong, harga negatif, atau stok tidak cukup).
 *
 * Dibuat sebagai unchecked exception (RuntimeException) agar
 * kode lama yang memanggil constructor Produk tidak perlu diubah.
 * Pesan error-nya langsung ditampilkan ke pengguna oleh UI.
 */
public class ValidasiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidasiException(String pesan) {
        super(pesan);
    }
}