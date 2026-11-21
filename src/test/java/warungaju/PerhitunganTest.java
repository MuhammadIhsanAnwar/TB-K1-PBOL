package warungaju;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PerhitunganTest {
    @Test
    void simulationCalculatesProfitAndHourlyCostThroughInterface() {
        Perhitungan perhitungan = new SimulasiBisnis(100, 2, 500, 2, 10, 7000);

        assertEquals(18000, perhitungan.hitung(), 0.001);
        assertEquals("Laba bersih", perhitungan.label());
    }

    @Test
    void cashFlowPredictionReturnsFinalBalanceThroughInterface() {
        Perhitungan perhitungan = new PrediksiArusKas(1000, 800, 500, 3);

        assertEquals(1900, perhitungan.hitung(), 0.001);
        assertEquals("Saldo akhir", perhitungan.label());
    }
}
