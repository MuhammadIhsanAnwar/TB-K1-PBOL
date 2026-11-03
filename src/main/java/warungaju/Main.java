package warungaju;

import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            FlatLightLaf.setup();
            UI.initTheme();
            new MainFrame().setVisible(true);
        }});
    }
}
