package warungaju;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StartupFrameTest {
    @Test
    void classifiesConnectionLatency() {
        assertEquals("Koneksi lancar", StartupFrame.connectionQuality(300));
        assertEquals("Koneksi cukup lambat", StartupFrame.connectionQuality(900));
        assertEquals("Koneksi lambat", StartupFrame.connectionQuality(1800));
    }
}
