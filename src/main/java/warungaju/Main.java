package warungaju;

import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatDarkLaf;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();
            UI.initTheme();
            new AuthFrame().setVisible(true);
        });
    }
}