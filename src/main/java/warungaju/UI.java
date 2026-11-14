package warungaju;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Pusat styling aplikasi. Tema finansial korporat: navy/charcoal + aksen emerald dan emas,
 * permukaan datar (tanpa gradient, tanpa glow), sudut kecil, garis tipis.
 *
 * Nama field lama (BG_DARK, GLASS_*, PURPLE_TEXT, dst.) sengaja dipertahankan
 * supaya panel lain yang belum diubah tetap bisa dikompilasi.
 */
class UI {
    static final String FONT = "Segoe UI";
    /** Radius sudut (px). ARC = diameter busur yang dipakai Graphics2D dan FlatLaf. */
    static final int RADIUS = 10;
    static final int ARC = RADIUS * 2;
    static boolean lightMode;

    // --- Nama lama (dipertahankan) ---
    static Color BG_DARK;        // latar halaman
    static Color ACCENT;         // emerald
    static Color ACCENT_HOVER;
    static Color PURPLE_TEXT;    // sekarang emas (aksen kedua)
    static Color PRIMARY_START;
    static Color PRIMARY_END;
    static Color PRIMARY_HOVER_START;
    static Color PRIMARY_HOVER_END;
    static Color TEXT_LIGHT;     // teks utama
    static Color TEXT_MUTED;
    static Color GLASS_LIGHT;    // permukaan kartu / panel
    static Color GLASS_BORDER;   // garis tipis
    static Color GLASS_ACTIVE;   // seleksi / hover
    static Color GLASS_TOP;
    static Color GLASS_BOTTOM;
    static Color GLOW_PURPLE;
    static Color GLOW_GREEN;

    // --- Warna baru ---
    static Color SIDEBAR_BG;
    static Color SIDEBAR_HOVER;
    static Color SIDEBAR_ACTIVE;
    static Color SIDEBAR_TEXT;
    static Color SIDEBAR_ACTIVE_TEXT;
    static Color BUTTON_TEXT;
    static Color INPUT_BG;
    static Color ROW_ALT;
    static Color HEADER_BG;
    static Color GREEN;
    static Color RED;
    static Color AMBER;

    static {
        initTheme();
    }

    static void initTheme() {
        if (lightMode) {
            BG_DARK = new Color(241, 244, 248);
            GLASS_LIGHT = new Color(255, 255, 255);
            GLASS_BORDER = new Color(214, 220, 229);
            GLASS_ACTIVE = new Color(226, 236, 240);
            TEXT_LIGHT = new Color(20, 33, 52);
            TEXT_MUTED = new Color(95, 108, 128);
            ACCENT = new Color(0, 128, 110);
            ACCENT_HOVER = new Color(0, 150, 128);
            PURPLE_TEXT = new Color(176, 132, 24);
            SIDEBAR_BG = new Color(15, 27, 45);
            SIDEBAR_HOVER = new Color(24, 40, 64);
            SIDEBAR_ACTIVE = new Color(30, 50, 80);
            SIDEBAR_TEXT = new Color(190, 203, 222);
            SIDEBAR_ACTIVE_TEXT = new Color(255, 255, 255);
            BUTTON_TEXT = Color.WHITE;
            INPUT_BG = Color.WHITE;
            ROW_ALT = new Color(247, 249, 252);
            HEADER_BG = new Color(226, 232, 240);
            GREEN = new Color(16, 138, 92);
            RED = new Color(200, 60, 60);
            AMBER = new Color(176, 132, 24);
        } else {
            BG_DARK = new Color(14, 22, 36);
            GLASS_LIGHT = new Color(22, 34, 54);
            GLASS_BORDER = new Color(44, 62, 90);
            GLASS_ACTIVE = new Color(32, 50, 78);
            TEXT_LIGHT = new Color(232, 238, 247);
            TEXT_MUTED = new Color(150, 165, 188);
            ACCENT = new Color(38, 198, 152);
            ACCENT_HOVER = new Color(74, 220, 176);
            PURPLE_TEXT = new Color(222, 184, 90);
            SIDEBAR_BG = new Color(9, 16, 28);
            SIDEBAR_HOVER = new Color(17, 28, 46);
            SIDEBAR_ACTIVE = new Color(26, 42, 66);
            SIDEBAR_TEXT = new Color(160, 176, 200);
            SIDEBAR_ACTIVE_TEXT = new Color(255, 255, 255);
            BUTTON_TEXT = new Color(8, 20, 30);
            INPUT_BG = new Color(28, 42, 66);
            ROW_ALT = new Color(26, 39, 60);
            HEADER_BG = new Color(34, 52, 80);
            GREEN = new Color(72, 200, 140);
            RED = new Color(240, 110, 110);
            AMBER = new Color(226, 178, 70);
        }
        // Sisa nama lama: semuanya warna solid, tidak ada gradient lagi.
        GLASS_TOP = GLASS_LIGHT;
        GLASS_BOTTOM = GLASS_LIGHT;
        GLOW_PURPLE = new Color(0, 0, 0, 0);
        GLOW_GREEN = new Color(0, 0, 0, 0);
        PRIMARY_START = ACCENT;
        PRIMARY_END = ACCENT;
        PRIMARY_HOVER_START = ACCENT_HOVER;
        PRIMARY_HOVER_END = ACCENT_HOVER;

        UIManager.put("defaultFont", new Font(FONT, Font.PLAIN, 13));
        UIManager.put("Component.arc", ARC);
        UIManager.put("Button.arc", ARC);
        UIManager.put("TextComponent.arc", ARC);
        UIManager.put("Component.borderColor", GLASS_BORDER);
        UIManager.put("Component.focusedBorderColor", ACCENT);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("Button.font", new Font(FONT, Font.BOLD, 13));
        UIManager.put("Button.background", ACCENT);
        UIManager.put("Button.foreground", BUTTON_TEXT);
        UIManager.put("Button.borderWidth", 0);
        UIManager.put("Table.font", new Font(FONT, Font.PLAIN, 13));
        UIManager.put("Table.foreground", TEXT_LIGHT);
        UIManager.put("Table.selectionBackground", GLASS_ACTIVE);
        UIManager.put("Table.selectionForeground", TEXT_LIGHT);
        UIManager.put("Table.rowHeight", 40);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("TabbedPane.selected", GLASS_LIGHT);
        UIManager.put("TabbedPane.tabHeight", 38);
        UIManager.put("TabbedPane.background", BG_DARK);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("TabbedPane.selectedForeground", ACCENT);
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("Label.foreground", TEXT_LIGHT);
        UIManager.put("TextField.background", INPUT_BG);
        UIManager.put("TextField.foreground", TEXT_LIGHT);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("ComboBox.background", INPUT_BG);
        UIManager.put("ComboBox.foreground", TEXT_LIGHT);
        UIManager.put("ScrollPane.background", BG_DARK);
        UIManager.put("Table.background", GLASS_LIGHT);
        UIManager.put("TableHeader.background", HEADER_BG);
        UIManager.put("TableHeader.foreground", TEXT_LIGHT);
    }

    static void toggleTheme() {
        lightMode = !lightMode;
        if (lightMode) FlatLightLaf.setup();
        else FlatDarkLaf.setup();
        initTheme();
    }

    // ---------- HTML helper (kode warna lama tetap dipetakan) ----------

    static String html(String content) {
        return content.replace("#FFFFF0", hex(TEXT_LIGHT))
            .replace("#C8D0FF", hex(TEXT_MUTED))
            .replace("#DFFF00", hex(ACCENT))
            .replace("#BFA7FF", hex(PURPLE_TEXT));
    }

    static JLabel htmlLabel(String content) {
        return new JLabel(html(content));
    }

    private static String hex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    // ---------- Panel / kartu ----------

    /** Panel datar dengan sudut membulat: isi solid + garis tipis 1px. (Nama lama dipertahankan.) */
    static JPanel glassPanel() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GLASS_LIGHT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
                boolean hot = Boolean.TRUE.equals(getClientProperty("warungaju.hover"));
                g2.setColor(hot ? ACCENT : GLASS_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    /** Tombol ganti tema: teks biasa, bukan toggle animasi. */
    static JButton themeToggle(final Runnable onToggle) {
        JButton b = secondaryButton(lightMode ? "Mode gelap" : "Mode terang");
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        b.addActionListener(e -> onToggle.run());
        return b;
    }

    static JPanel card(String title, String value, Color color) {
        return card(title, value, color, null);
    }

    /** Kartu ala nota: label kecil, garis putus-putus, angka rata kanan. */
    static JPanel card(String title, String value, Color color, final Runnable onClick) {
        JPanel p = glassPanel();
        p.setLayout(new BorderLayout(0, 10));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel a = new JLabel(title);
        a.setForeground(TEXT_MUTED);
        a.setFont(new Font(FONT, Font.PLAIN, 12));

        JPanel head = new JPanel(new BorderLayout(0, 8));
        head.setOpaque(false);
        head.add(a, BorderLayout.NORTH);
        head.add(new DashedLine(), BorderLayout.CENTER);

        JLabel b = new JLabel(value, SwingConstants.RIGHT);
        b.setFont(new Font(FONT, Font.BOLD, 24));
        b.setForeground(color);

        p.add(head, BorderLayout.NORTH);
        p.add(b, BorderLayout.CENTER);
        addCardHover(p, a, b, onClick);
        return p;
    }

    private static class DashedLine extends JComponent {
        DashedLine() {
            setPreferredSize(new Dimension(10, 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(GLASS_BORDER);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{4f, 3f}, 0f));
            g2.drawLine(0, 0, getWidth(), 0);
            g2.dispose();
        }
    }

    private static void addCardHover(final JPanel panel, final JLabel title, final JLabel value, final Runnable onClick) {
        MouseAdapter hover = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (onClick != null) panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                panel.putClientProperty("warungaju.hover", Boolean.TRUE);
                panel.repaint();
            }

            public void mouseExited(MouseEvent e) {
                Component source = e.getComponent();
                Point point = SwingUtilities.convertPoint(source, e.getPoint(), panel);
                if (!panel.contains(point)) {
                    panel.putClientProperty("warungaju.hover", Boolean.FALSE);
                    panel.repaint();
                }
            }

            public void mouseClicked(MouseEvent e) {
                if (onClick != null && SwingUtilities.isLeftMouseButton(e)) onClick.run();
            }
        };
        panel.addMouseListener(hover);
        title.addMouseListener(hover);
        value.addMouseListener(hover);
    }

    /** Format rupiah: "Rp 2.500.000" (dengan spasi, tanpa desimal). */
    static String money(double n) {
        long v = Math.round(Math.abs(n));
        String s = "Rp " + NumberFormat.getIntegerInstance(new Locale("id", "ID")).format(v);
        return (n < 0 && v != 0) ? "-" + s : s;
    }

    // ---------- Tombol ----------

    static JButton button(String text) {
        return new SolidButton(text);
    }

    /** Tombol utama: warna solid emerald, sudut membulat, tanpa kilau. */
    private static class SolidButton extends JButton {
        private float hover;
        private final Timer animation;

        SolidButton(String text) {
            super(text);
            setForeground(BUTTON_TEXT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font(FONT, Font.BOLD, 14));
            animation = new Timer(15, e -> {
                float target = getModel().isRollover() ? 1f : 0f;
                hover += (target - hover) * 0.3f;
                if (Math.abs(target - hover) < 0.03f) {
                    hover = target;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { animation.start(); }
                public void mouseExited(MouseEvent e) { animation.start(); }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(interpolate(ACCENT, ACCENT_HOVER, hover));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Menu sidebar: teks terang di atas navy gelap. */
    static JButton navButton(String text) {
        final JButton b = new JButton(text);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setForeground(SIDEBAR_TEXT);
        b.setFont(new Font(FONT, Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 20));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!Boolean.TRUE.equals(b.getClientProperty("warungaju.active"))) {
                    b.setOpaque(true);
                    b.setContentAreaFilled(true);
                    b.setBackground(SIDEBAR_HOVER);
                    b.setForeground(SIDEBAR_ACTIVE_TEXT);
                }
            }

            public void mouseExited(MouseEvent e) {
                if (!Boolean.TRUE.equals(b.getClientProperty("warungaju.active"))) {
                    b.setOpaque(false);
                    b.setContentAreaFilled(false);
                    b.setForeground(SIDEBAR_TEXT);
                }
            }
        });
        return b;
    }

    /** Menu aktif: garis tebal emerald di kiri + teks cerah, tanpa kotak menyala. */
    static void setNavActive(JButton b, boolean active) {
        b.putClientProperty("warungaju.active", active);
        b.setOpaque(active);
        b.setContentAreaFilled(active);
        b.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        b.setForeground(active ? SIDEBAR_ACTIVE_TEXT : SIDEBAR_TEXT);
        b.setFont(new Font(FONT, active ? Font.BOLD : Font.PLAIN, 14));
        b.setBorder(active
            ? BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, ACCENT),
                BorderFactory.createEmptyBorder(12, 20, 12, 20))
            : BorderFactory.createEmptyBorder(12, 24, 12, 20));
    }

    static JButton secondaryButton(String text) {
        JButton b = new OutlineButton(text);
        addSmoothHover(b, GLASS_LIGHT, GLASS_ACTIVE);
        return b;
    }

    /** Tombol sekunder: isi mengikuti getBackground() (dianimasikan addSmoothHover), garis tipis membulat. */
    private static class OutlineButton extends JButton {
        OutlineButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setFocusPainted(false);
            setBackground(GLASS_LIGHT);
            setForeground(TEXT_LIGHT);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.setColor(GLASS_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static void addSmoothHover(final JButton button, final Color normal, final Color hover) {
        final float[] progress = {0};
        final float[] target = {0};
        final Timer timer = new Timer(15, e -> {
            if (progress[0] < target[0]) progress[0] = Math.min(target[0], progress[0] + 0.12f);
            else if (progress[0] > target[0]) progress[0] = Math.max(target[0], progress[0] - 0.12f);
            button.setBackground(interpolate(normal, hover, progress[0]));
            if (progress[0] == target[0]) ((Timer) e.getSource()).stop();
        });

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                target[0] = 1;
                timer.start();
            }

            public void mouseExited(MouseEvent e) {
                target[0] = 0;
                timer.start();
            }
        });
    }

    /** Dipertahankan untuk kompatibilitas dengan panel lain. */
    static void addGlassHover(final JButton button) {
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setOpaque(true);
                button.setBackground(GLASS_ACTIVE);
            }

            public void mouseExited(MouseEvent e) {
                button.setOpaque(false);
                button.setContentAreaFilled(false);
            }
        });
    }

    static Color interpolate(Color c1, Color c2, float t) {
        return new Color(
            c1.getRed() + (int) ((c2.getRed() - c1.getRed()) * t),
            c1.getGreen() + (int) ((c2.getGreen() - c1.getGreen()) * t),
            c1.getBlue() + (int) ((c2.getBlue() - c1.getBlue()) * t),
            c1.getAlpha() + (int) ((c2.getAlpha() - c1.getAlpha()) * t)
        );
    }

    // ---------- Form ----------

    static JPanel formGrid() {
        JPanel p = glassPanel();
        p.setLayout(new GridBagLayout());
        return p;
    }

    static void field(JPanel p, GridBagConstraints g, String label, JComponent component, int row) {
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 10, 10, 16);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_LIGHT);
        p.add(lbl, g);
        g.gridx = 1;
        g.weightx = 1;
        g.insets = new Insets(10, 0, 10, 10);
        p.add(component, g);
    }

    static JTextField input(String value) {
        JTextField field = new JTextField(value);
        field.setPreferredSize(new Dimension(150, 38));
        field.setMargin(new Insets(6, 12, 6, 12));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(ACCENT);
        return field;
    }

    // ---------- Tabel ----------

    static void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(GLASS_BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(GLASS_ACTIVE);
        table.setSelectionForeground(TEXT_LIGHT);
        table.setFillsViewportHeight(true);
        table.setBackground(GLASS_LIGHT);
        table.setDefaultRenderer(Object.class, new StripeRenderer());
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        header.setFont(new Font(FONT, Font.BOLD, 12));
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_LIGHT);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
    }

    static JScrollPane scrollPane(JTable table) {
        return new RoundedScrollPane(table);
    }

    /** JScrollPane yang isinya dipotong mengikuti sudut membulat. */
    private static class RoundedScrollPane extends JScrollPane {
        RoundedScrollPane(Component view) {
            super(view);
            setOpaque(false);
            getViewport().setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            getVerticalScrollBar().setUnitIncrement(16);
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC, ARC));
            super.paint(g2);
            g2.dispose();
            Graphics2D b = (Graphics2D) g.create();
            b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            b.setColor(GLASS_BORDER);
            b.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            b.dispose();
        }
    }

    static JPanel searchBar(JTextField search, String hint) {
        JPanel p = glassPanel();
        p.setLayout(new BorderLayout(10, 0));
        p.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
        JLabel icon = new JLabel("Cari");
        icon.setFont(new Font(FONT, Font.BOLD, 12));
        icon.setForeground(TEXT_MUTED);
        search.putClientProperty("JTextField.placeholderText", hint);
        p.add(icon, BorderLayout.WEST);
        p.add(search, BorderLayout.CENTER);
        return p;
    }

    static class StripeRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            boolean right = value instanceof Number;
            if (value instanceof String) {
                String s = (String) value;
                right = s.startsWith("Rp") || s.startsWith("-Rp") || s.endsWith("%");
            }
            setHorizontalAlignment(right ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (!selected) {
                c.setBackground(row % 2 == 0 ? GLASS_LIGHT : ROW_ALT);
                c.setForeground(TEXT_LIGHT);
            }
            return c;
        }
    }
}