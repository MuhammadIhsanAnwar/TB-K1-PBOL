package warungaju;

import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatDarkLaf;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
                FlatDarkLaf.setup();
                UI.initTheme();
                new MainFrame().setVisible(true);
            }
        });
    }
}