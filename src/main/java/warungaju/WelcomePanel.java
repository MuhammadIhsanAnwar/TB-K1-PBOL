package warungaju;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class WelcomePanel extends JPanel {
    private JButton dashboard;

    public WelcomePanel(final MainFrame frame, Runnable themeToggle) {
        setLayout(new BorderLayout());
        setBackground(UI.BG_DARK);
        add(brandStrip(), BorderLayout.WEST);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 22, 14));
        topBar.setOpaque(false);
        topBar.add(UI.themeToggle(themeToggle));
        add(topBar, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 20, 8, 20);
        g.anchor = GridBagConstraints.WEST;

        JLabel eyebrow = new JLabel("KEUANGAN USAHA / UMKM");
        eyebrow.setForeground(UI.PURPLE_TEXT);
        eyebrow.setFont(new Font(UI.FONT, Font.BOLD, 13));
        g.gridx = 0; g.gridy = 0; center.add(eyebrow, g);

        JLabel title = UI.htmlLabel("<html><div style='font-size:42px'>Selamat Datang di<br><font color='#DFFF00'>WARUNGAJU</font></div></html>");
        title.setForeground(UI.TEXT_LIGHT);
        title.setFont(new Font(UI.FONT, Font.BOLD, 42));
        g.gridy = 1; g.insets = new Insets(6, 20, 4, 20); center.add(title, g);

        JLabel subtitle = UI.htmlLabel("<html><div style='width:520px;color:#C8D0FF;font-size:15px'>Catat transaksi, hitung biaya produk, dan jaga arus kas usaha<br>agar keputusan harian terasa lebih terarah.</div></html>");
        subtitle.setFont(new Font(UI.FONT, Font.PLAIN, 16));
        g.gridy = 2; g.insets = new Insets(4, 20, 25, 20); center.add(subtitle, g);

        JPanel features = new JPanel(new GridLayout(1, 3, 16, 0));
        features.setOpaque(false);
        features.add(feature("01", "Rencanakan", "Susun target usaha dengan data yang mudah dipahami."));
        features.add(feature("02", "Simulasikan", "Uji skenario bisnis sebelum mengambil keputusan."));
        features.add(feature("03", "Pantau", "Tinjau perubahan kas dan laporan secara berkala."));
        g.gridy = 3; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; center.add(features, g);

        dashboard = UI.button("Buka ringkasan usaha");
        dashboard.setFont(new Font(UI.FONT, Font.BOLD, 15));
        dashboard.setPreferredSize(new Dimension(240, 52));
        g.gridy = 4; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(35, 20, 8, 20); center.add(dashboard, g);
        dashboard.addActionListener(e -> frame.showDashboard());

        JLabel note = new JLabel("Mulai dari transaksi atau produk yang ingin Anda kelola.");
        note.setForeground(UI.TEXT_MUTED);
        g.gridy = 5; g.insets = new Insets(0, 20, 8, 20); center.add(note, g);
        add(center, BorderLayout.CENTER);
        add(statusPanel(), BorderLayout.SOUTH);
    }

    private JPanel brandStrip() {
        JPanel strip = new JPanel(new GridBagLayout());
        strip.setPreferredSize(new Dimension(110, 0));
        strip.setBackground(UI.BG_DARK);
        strip.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UI.GLASS_BORDER));
        JLabel mark = new JLabel("W");
        mark.setForeground(UI.ACCENT);
        mark.setFont(new Font(UI.FONT, Font.BOLD, 54));
        strip.add(mark);
        return strip;
    }

    private JPanel feature(String number, String title, String description) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(UI.GLASS_LIGHT);
                g2.fill(shape);
                g2.setColor(UI.GLASS_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(shape);
                g2.dispose();
            }
        };
        panel.setLayout(new BorderLayout(4, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel n = new JLabel(number);
        n.setForeground(UI.ACCENT);
        n.setFont(new Font(UI.FONT, Font.BOLD, 14));
        JLabel t = new JLabel(title);
        t.setForeground(UI.TEXT_LIGHT);
        t.setFont(new Font(UI.FONT, Font.BOLD, 16));
        JLabel d = UI.htmlLabel("<html><div style='width:140px;color:#C8D0FF;font-size:12px'>" + description + "</div></html>");
        panel.add(n, BorderLayout.NORTH);
        panel.add(t, BorderLayout.CENTER);
        panel.add(d, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel statusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UI.GLASS_LIGHT);
        panel.setBorder(new EmptyBorder(14, 30, 14, 30));
        JLabel left = new JLabel("WARUNGAJU  /  PERENCANAAN KEUANGAN UMKM");
        left.setForeground(UI.TEXT_MUTED);
        left.setFont(new Font(UI.FONT, Font.BOLD, 10));
        JLabel right = new JLabel("Ruang kerja keuangan usaha");
        right.setForeground(UI.TEXT_MUTED);
        right.setFont(new Font(UI.FONT, Font.PLAIN, 10));
        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }
}