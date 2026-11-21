package warungaju;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.Icon;
import org.junit.jupiter.api.Test;

class LogoAssetsTest {
    @Test
    void loadsTheProvidedBrandAndApplicationIcons() {
        assertNotNull(UI.logoImage());
        assertNotNull(UI.sideLogoImage());
        assertNotNull(UI.markImage());

        Icon bottomLogo = UI.logoIcon(120);
        Icon sideLogo = UI.sideLogoIcon(44);
        assertNotNull(bottomLogo);
        assertNotNull(sideLogo);
        assertEquals(120, bottomLogo.getIconHeight());
        assertTrue(sideLogo.getIconWidth() > sideLogo.getIconHeight());
        assertFalse(UI.windowIcons().isEmpty());
    }
}
