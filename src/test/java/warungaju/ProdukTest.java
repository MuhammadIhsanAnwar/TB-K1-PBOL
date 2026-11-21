package warungaju;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProdukTest {
    @Test
    void calculatesCostAndMarginThroughPerhitungan() {
        Produk produk = new Produk("Kopi", 4000, 10000, 5, 2);
        Perhitungan perhitungan = produk;

        assertEquals(2000, produk.getHpp(), 0.001);
        assertEquals(80, produk.getMargin(), 0.001);
        assertEquals(2000, perhitungan.hitung(), 0.001);
        assertEquals("HPP per unit", perhitungan.label());
    }

    @Test
    void rejectsZeroProductionAndSellingPrice() {
        assertThrows(ValidasiException.class, () -> new Produk("Kopi", 100, 100, 0, 0));
        assertThrows(ValidasiException.class, () -> new Produk("Kopi", 100, 0, 1, 1));
    }
}
