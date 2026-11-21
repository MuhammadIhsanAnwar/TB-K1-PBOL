package warungaju;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransaksiTest {
    @Test
    void acceptsValidTransactionAndTrimsText() {
        Transaksi transaksi = new Transaksi(LocalDate.of(2026, 1, 15), " Pemasukan ",
            " Penjualan ", 1500, 250, " Pesanan pagi ", " Kopi ");

        assertEquals("Pemasukan", transaksi.getJenis());
        assertEquals("Penjualan", transaksi.getKategori());
        assertEquals(1500, transaksi.getNominal());
        assertEquals(250, transaksi.getHpp());
        assertEquals("Pesanan pagi", transaksi.getKeterangan());
        assertEquals("Kopi", transaksi.getProduk());
    }

    @Test
    void rejectsInvalidTransactionValues() {
        assertThrows(ValidasiException.class,
            () -> new Transaksi(null, Transaksi.PEMASUKAN, "Penjualan", 100, "catatan"));
        assertThrows(ValidasiException.class,
            () -> new Transaksi(LocalDate.now(), "Transfer", "Penjualan", 100, "catatan"));
        assertThrows(ValidasiException.class,
            () -> new Transaksi(LocalDate.now(), Transaksi.PEMASUKAN, " ", 100, "catatan"));
        assertThrows(ValidasiException.class,
            () -> new Transaksi(LocalDate.now(), Transaksi.PEMASUKAN, "Penjualan", 0, "catatan"));
        assertThrows(ValidasiException.class,
            () -> new Transaksi(LocalDate.now(), Transaksi.PEMASUKAN, "Penjualan",
                100, -1, "catatan", "Kopi"));
    }
}
