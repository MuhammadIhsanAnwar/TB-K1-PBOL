package warungaju;

import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            UI.initTheme();
            new StartupFrame().setVisible(true);
        });
    }
}