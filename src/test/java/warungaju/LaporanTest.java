package warungaju;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class LaporanTest {
    private final Transaksi penjualan = new Transaksi(LocalDate.of(2026, 1, 15),
        Transaksi.PEMASUKAN, "Penjualan", 1000, 400, "Pesanan", "Kopi");
    private final Transaksi biaya = new Transaksi(LocalDate.of(2026, 1, 15),
        Transaksi.PENGELUARAN, "Operasional", 200, "Listrik");

    @Test
    void laporanLabaRugiUsesTransactionCostAndExpense() {
        Laporan.Baris labaBersih = new LaporanLabaRugi(Arrays.asList(penjualan, biaya))
            .baris().get(4);

        assertEquals("Laba bersih", labaBersih.label);
        assertEquals(400, labaBersih.nilai, 0.001);
        assertTrue(labaBersih.penekanan);
    }

    @Test
    void laporanArusKasSubtractsCashOutflow() {
        Laporan.Baris arusBersih = new LaporanArusKas(Arrays.asList(penjualan, biaya))
            .baris().get(2);

        assertEquals("Arus kas bersih", arusBersih.label);
        assertEquals(800, arusBersih.nilai, 0.001);
    }

    @Test
    void laporanNeracaIncludesRemainingInventoryAtCost() {
        Produk produk = new Produk("Kopi", 400, 1000, 5, 1);
        Laporan.Baris totalAset = new LaporanNeraca(Arrays.asList(penjualan, biaya),
            Collections.singletonList(produk)).baris().get(2);

        assertEquals("Total aset", totalAset.label);
        assertEquals(2800, totalAset.nilai, 0.001);
    }
}
