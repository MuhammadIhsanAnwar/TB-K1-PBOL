package warungaju;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;

class StartupFrame extends JFrame {
    private final JLabel connectionStatus = new JLabel(
        "Memeriksa koneksi internet dan layanan Warung Aju...", SwingConstants.CENTER);
    private final JProgressBar progress = new JProgressBar();

    StartupFrame() {
        setTitle("Warung Aju");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(640, 430);
        setLocationRelativeTo(null);
        setIconImages(UI.windowIcons());

        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        root.setBackground(UI.BG_DARK);

        JPanel card = UI.glassPanel();
        card.setPreferredSize(new Dimension(540, 340));
        card.setBorder(BorderFactory.createEmptyBorder(30, 38, 30, 38));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel logo = new JLabel(UI.logoIcon(116), SwingConstants.CENTER);
        logo.setAlignmentX(CENTER_ALIGNMENT);
        logo.setMaximumSize(new Dimension(116, 116));

        JLabel welcome = UI.htmlLabel("<html><div style='text-align:center;font-size:25px'>"
            + "Selamat datang</div></html>");
        welcome.setFont(new Font(UI.FONT, Font.BOLD, 24));
        welcome.setAlignmentX(CENTER_ALIGNMENT);
        welcome.setMaximumSize(new Dimension(460, 40));

        JLabel subtitle = new JLabel(
            "Menyiapkan ruang kerja keuangan usaha Anda", SwingConstants.CENTER);
        subtitle.setForeground(UI.TEXT_MUTED);
        subtitle.setFont(new Font(UI.FONT, Font.PLAIN, 14));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        subtitle.setMaximumSize(new Dimension(460, 24));

        connectionStatus.setForeground(UI.TEXT_MUTED);
        connectionStatus.setFont(new Font(UI.FONT, Font.BOLD, 13));
        connectionStatus.setAlignmentX(CENTER_ALIGNMENT);
        connectionStatus.setMaximumSize(new Dimension(460, 30));

        progress.setIndeterminate(true);
        progress.setBorderPainted(false);
        progress.setBackground(UI.GLASS_BORDER);
        progress.setForeground(UI.ACCENT);
        progress.setPreferredSize(new Dimension(420, 10));
        progress.setMaximumSize(new Dimension(420, 10));
        progress.setAlignmentX(CENTER_ALIGNMENT);

        content.add(logo);
        content.add(Box.createVerticalStrut(8));
        content.add(welcome);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(22));
        content.add(progress);
        content.add(Box.createVerticalStrut(10));
        content.add(connectionStatus);
        card.add(content, BorderLayout.CENTER);
        root.add(card);
        setContentPane(root);

        checkConnection();
    }

    private void checkConnection() {
        new SwingWorker<SupabaseClient.ConnectionCheck, Void>() {
            @Override
            protected SupabaseClient.ConnectionCheck doInBackground() throws Exception {
                return new SupabaseClient().checkConnection();
            }

            @Override
            protected void done() {
                try {
                    SupabaseClient.ConnectionCheck result = get();
                    if (result.httpStatus >= 400) {
                        connectionStatus.setForeground(UI.RED);
                        connectionStatus.setText("Internet tersambung, layanan tidak merespons normal "
                            + "(HTTP " + result.httpStatus + ").");
                    } else {
                        connectionStatus.setForeground(UI.TEXT_MUTED);
                        connectionStatus.setText(connectionQuality(result.elapsedMillis)
                            + " · " + result.elapsedMillis + " ms");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showConnectionUnavailable();
                } catch (ExecutionException ex) {
                    showConnectionUnavailable();
                    Throwable cause = ex.getCause();
                    if (cause != null) connectionStatus.setToolTipText(cause.getMessage());
                }

                progress.setIndeterminate(false);
                progress.setValue(100);
                Timer next = new Timer(900, event -> {
                    new AuthFrame().setVisible(true);
                    dispose();
                });
                next.setRepeats(false);
                next.start();
            }
        }.execute();
    }

    private void showConnectionUnavailable() {
        connectionStatus.setForeground(UI.RED);
        connectionStatus.setText("Koneksi belum tersedia · periksa internet Anda.");
    }

    static String connectionQuality(long elapsedMillis) {
        if (elapsedMillis <= 600) return "Koneksi lancar";
        if (elapsedMillis <= 1500) return "Koneksi cukup lambat";
        return "Koneksi lambat";
    }
}
