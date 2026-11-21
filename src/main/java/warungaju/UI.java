package warungaju;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Pusat styling aplikasi dengan palet putih, cokelat hangat, dan merah bata.
 *
 * Nama field lama (BG_DARK, GLASS_*, PURPLE_TEXT, dst.) sengaja dipertahankan
 * supaya panel lain yang belum diubah tetap bisa dikompilasi.
 */
class UI {
    static final String FONT = "Segoe UI";
    /** Radius sudut (px). ARC = diameter busur yang dipakai Graphics2D dan FlatLaf. */
    static final int RADIUS = 10;
    static final int ARC = RADIUS * 2;
    static boolean lightMode = true;

    // --- Nama lama (dipertahankan) ---
    static Color BG_DARK;        // latar halaman
    static Color ACCENT;         // merah bata
    static Color ACCENT_HOVER;
    static Color PURPLE_TEXT;    // nama lama, sekarang cokelat hangat
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
            BG_DARK = new Color(250, 247, 243);
            GLASS_LIGHT = Color.WHITE;
            GLASS_BORDER = new Color(234, 224, 216);
            GLASS_ACTIVE = new Color(250, 232, 229);
            TEXT_LIGHT = new Color(55, 42, 36);
            TEXT_MUTED = new Color(126, 108, 99);
            ACCENT = new Color(180, 55, 48);
            ACCENT_HOVER = new Color(203, 68, 58);
            PURPLE_TEXT = new Color(125, 79, 59);
            SIDEBAR_BG = new Color(255, 253, 251);
            SIDEBAR_HOVER = new Color(248, 241, 236);
            SIDEBAR_ACTIVE = new Color(250, 232, 229);
            SIDEBAR_TEXT = new Color(98, 79, 70);
            SIDEBAR_ACTIVE_TEXT = new Color(137, 48, 42);
            BUTTON_TEXT = Color.WHITE;
            INPUT_BG = new Color(255, 255, 255);
            ROW_ALT = new Color(251, 248, 245);
            HEADER_BG = new Color(246, 239, 234);
            GREEN = new Color(125, 79, 59);
            RED = new Color(180, 55, 48);
            AMBER = new Color(166, 96, 59);
        } else {
            BG_DARK = new Color(45, 34, 30);
            GLASS_LIGHT = new Color(60, 46, 41);
            GLASS_BORDER = new Color(91, 70, 63);
            GLASS_ACTIVE = new Color(89, 55, 51);
            TEXT_LIGHT = new Color(249, 242, 237);
            TEXT_MUTED = new Color(202, 180, 168);
            ACCENT = new Color(220, 96, 83);
            ACCENT_HOVER = new Color(237, 117, 101);
            PURPLE_TEXT = new Color(220, 158, 125);
            SIDEBAR_BG = new Color(38, 29, 26);
            SIDEBAR_HOVER = new Color(58, 42, 38);
            SIDEBAR_ACTIVE = new Color(79, 48, 44);
            SIDEBAR_TEXT = new Color(215, 197, 187);
            SIDEBAR_ACTIVE_TEXT = new Color(255, 242, 237);
            BUTTON_TEXT = Color.WHITE;
            INPUT_BG = new Color(69, 51, 45);
            ROW_ALT = new Color(54, 42, 38);
            HEADER_BG = new Color(72, 53, 47);
            GREEN = new Color(220, 158, 125);
            RED = new Color(239, 126, 113);
            AMBER = new Color(231, 173, 123);
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

        UIManager.put("defaultFont", new Font(FONT, Font.PLAIN, 14));
        UIManager.put("Component.arc", ARC);
        UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", ARC);
        UIManager.put("Component.minimumHeight", 40);
        UIManager.put("Button.minimumHeight", 42);
        UIManager.put("TextField.minimumHeight", 42);
        UIManager.put("PasswordField.minimumHeight", 42);
        UIManager.put("ComboBox.minimumHeight", 42);
        UIManager.put("Component.focusColor", new Color(ACCENT.getRed(),
            ACCENT.getGreen(), ACCENT.getBlue(), 55));
        UIManager.put("Component.borderColor", GLASS_BORDER);
        UIManager.put("Component.focusedBorderColor", ACCENT);
        UIManager.put("Component.focusWidth", 1.5f);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Button.font", new Font(FONT, Font.BOLD, 14));
        UIManager.put("Button.background", ACCENT);
        UIManager.put("Button.foreground", BUTTON_TEXT);
        UIManager.put("Button.borderWidth", 0);
        UIManager.put("Table.font", new Font(FONT, Font.PLAIN, 14));
        UIManager.put("Table.foreground", TEXT_LIGHT);
        UIManager.put("Table.selectionBackground", GLASS_ACTIVE);
        UIManager.put("Table.selectionForeground", TEXT_LIGHT);
        UIManager.put("Table.rowHeight", 44);
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
        UIManager.put("OptionPane.background", BG_DARK);
        UIManager.put("OptionPane.messageForeground", TEXT_LIGHT);
        UIManager.put("PopupMenu.background", GLASS_LIGHT);
        UIManager.put("ToolTip.background", lightMode ? TEXT_LIGHT : GLASS_LIGHT);
        UIManager.put("ToolTip.foreground", lightMode ? Color.WHITE : TEXT_LIGHT);
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

    /**
     * Logo untuk layar. Gambar sumber (besar) dikecilkan bertahap lalu digambar
     * langsung dengan kualitas tinggi, jadi tetap tajam di layar dengan skala
     * Windows 125% / 150%.
     */
    static Icon logoIcon(int height) {
        return makeIcon(logoImage(), height);
    }

    /** Logo tanpa tulisan untuk header kecil dan ikon aplikasi. */
    static Icon markIcon(int height) {
        return makeIcon(markImage(), height);
    }

    /** Logo horizontal untuk navbar. */
    static Icon sideLogoIcon(int height) {
        return makeIcon(sideLogoImage(), height);
    }

    private static Icon makeIcon(BufferedImage source, int height) {
        if (source == null) {
            return null;
        }
        final int w = Math.round(source.getWidth() * (height / (float) source.getHeight()));
        final int h = height;
        final BufferedImage hi = shrink(source, w * 3, h * 3);
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(hi, x, y, w, h, null);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return w;
            }

            @Override
            public int getIconHeight() {
                return h;
            }
        };
    }

    /** Kecilkan dengan membagi dua berulang (rata-rata piksel), tanpa memperbesar. */
    private static BufferedImage shrink(BufferedImage src, int targetW, int targetH) {
        BufferedImage cur = src;
        while (cur.getWidth() / 2 >= targetW && cur.getHeight() / 2 >= targetH) {
            cur = resize(cur, cur.getWidth() / 2, cur.getHeight() / 2,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        if (cur.getWidth() > targetW && cur.getHeight() > targetH) {
            cur = resize(cur, targetW, targetH, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }
        return cur;
    }

    private static BufferedImage resize(BufferedImage src, int w, int h, Object interpolation) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private static BufferedImage logoCache;
    private static BufferedImage sideLogoCache;
    private static java.util.List<Image> iconCache;

    private static BufferedImage loadTrimmed(String path) {
        try (InputStream in = UI.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            BufferedImage image = ImageIO.read(in);
            return image == null ? null : trimTransparent(image);
        } catch (IOException ex) {
            return null;
        }
    }

    private static BufferedImage markCache;

    /** Logo ikon tanpa tulisan. */
    static BufferedImage markImage() {
        if (markCache == null) {
            markCache = loadTrimmed("/images/warung-aju-ikon.png");
        }
        return markCache;
    }

    /** Logo utama (dengan tulisan) untuk halaman login. */
    static BufferedImage logoImage() {
        if (logoCache == null) {
            logoCache = loadTrimmed("/images/warung-aju-bawah.png");
        }
        return logoCache;
    }

    /** Logo horizontal untuk navbar. */
    static BufferedImage sideLogoImage() {
        if (sideLogoCache == null) {
            sideLogoCache = loadTrimmed("/images/warung-aju-samping.png");
        }
        return sideLogoCache;
    }

    /**
     * Ikon jendela (title bar dan taskbar) memakai logo ikon dalam beberapa ukuran.
     */
    static java.util.List<Image> windowIcons() {
        if (iconCache != null) {
            return iconCache;
        }
        java.util.List<Image> list = new java.util.ArrayList<>();
        BufferedImage src = markImage();
        if (src != null) {
            int[] sizes = {16, 20, 24, 32, 48, 64, 128, 256};
            for (int size : sizes) {
                float scale = Math.min(size / (float) src.getWidth(), size / (float) src.getHeight());
                int w = Math.max(1, Math.round(src.getWidth() * scale));
                int h = Math.max(1, Math.round(src.getHeight() * scale));
                BufferedImage small = shrink(src, w, h);
                BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = canvas.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(small, (size - w) / 2, (size - h) / 2, w, h, null);
                g.dispose();
                list.add(canvas);
            }
        }
        iconCache = list;
        return list;
    }

    private static BufferedImage trimTransparent(BufferedImage img) {
        int minX = img.getWidth();
        int minY = img.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int alpha = (img.getRGB(x, y) >>> 24) & 0xFF;
                if (alpha > 10) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < 0) {
            return img;
        }
        return img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
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

    /** Kartu ringkas dengan label, nilai utama, dan aksen warna di sisi kiri. */
    static JPanel card(String title, String value, Color color, final Runnable onClick) {
        JPanel p = glassPanel();
        p.setLayout(new BorderLayout(14, 0));
        p.setBorder(new EmptyBorder(0, 18, 0, 20));

        JPanel accent = new JPanel();
        accent.setBackground(color);
        accent.setPreferredSize(new Dimension(4, 1));

        JLabel a = new JLabel(title);
        a.setForeground(TEXT_MUTED);
        a.setFont(new Font(FONT, Font.BOLD, 13));

        JLabel b = new JLabel(value);
        b.setFont(new Font(FONT, Font.BOLD, 25));
        b.setForeground(color);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(18, 0, 18, 0));
        content.add(a, BorderLayout.NORTH);
        content.add(b, BorderLayout.CENTER);
        p.add(accent, BorderLayout.WEST);
        p.add(content, BorderLayout.CENTER);
        addCardHover(p, a, b, onClick);
        return p;
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

    /** Tombol utama merah bata dengan sudut membulat. */
    private static class SolidButton extends JButton {
        private float hover;
        private final Timer animation;

        SolidButton(String text) {
            super(text);
            setForeground(BUTTON_TEXT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(11, 20, 11, 20));
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
            g.setColor(isEnabled()
                ? interpolate(ACCENT, ACCENT_HOVER, hover) : GLASS_BORDER);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class NavIcon implements Icon {
        private final String label;

        NavIcon(String label) {
            this.label = label;
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(component.getForeground());
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (label.startsWith("Dashboard")) {
                g.drawRoundRect(x + 2, y + 2, 14, 14, 4, 4);
                g.drawLine(x + 9, y + 3, x + 9, y + 15);
                g.drawLine(x + 3, y + 9, x + 15, y + 9);
            } else if (label.startsWith("Transaksi")) {
                g.drawRoundRect(x + 3, y + 2, 12, 14, 3, 3);
                g.drawLine(x + 6, y + 6, x + 12, y + 6);
                g.drawLine(x + 6, y + 9, x + 12, y + 9);
                g.drawLine(x + 6, y + 12, x + 10, y + 12);
            } else if (label.startsWith("Produk")) {
                g.drawRoundRect(x + 2, y + 4, 14, 11, 3, 3);
                g.drawLine(x + 2, y + 8, x + 16, y + 8);
                g.drawLine(x + 6, y + 4, x + 6, y + 8);
                g.drawLine(x + 12, y + 4, x + 12, y + 8);
            } else if (label.startsWith("Simulasi")) {
                g.drawLine(x + 3, y + 15, x + 3, y + 3);
                g.drawLine(x + 3, y + 15, x + 16, y + 15);
                g.drawLine(x + 5, y + 12, x + 8, y + 9);
                g.drawLine(x + 8, y + 9, x + 11, y + 11);
                g.drawLine(x + 11, y + 11, x + 15, y + 5);
            } else if (label.startsWith("Prediksi")) {
                g.drawRoundRect(x + 2, y + 3, 14, 12, 3, 3);
                g.drawLine(x + 2, y + 7, x + 16, y + 7);
                g.drawLine(x + 6, y + 10, x + 8, y + 10);
                g.drawLine(x + 11, y + 10, x + 13, y + 10);
                g.drawLine(x + 6, y + 13, x + 8, y + 13);
            } else {
                g.drawRoundRect(x + 3, y + 2, 12, 14, 3, 3);
                g.drawLine(x + 6, y + 6, x + 12, y + 6);
                g.drawLine(x + 6, y + 9, x + 12, y + 9);
                g.drawLine(x + 6, y + 12, x + 10, y + 12);
            }
            g.dispose();
        }
    }

    /** Sidebar navigation with active and hover states. */
    static JButton navButton(String text) {
        final JButton b = new JButton(text);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setForeground(SIDEBAR_TEXT);
        b.setFont(new Font(FONT, Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 20));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setIcon(new NavIcon(text));
        b.setIconTextGap(14);
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

    /** Menu aktif memakai garis merah bata dan latar hangat. */
    static void setNavActive(JButton b, boolean active) {
        b.putClientProperty("warungaju.active", active);
        b.setOpaque(active);
        b.setContentAreaFilled(active);
        b.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        b.setForeground(active ? SIDEBAR_ACTIVE_TEXT : SIDEBAR_TEXT);
        b.setFont(new Font(FONT, active ? Font.BOLD : Font.PLAIN, 14));
        b.setBorder(active
            ? BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT),
                BorderFactory.createEmptyBorder(12, 17, 12, 20))
            : BorderFactory.createEmptyBorder(12, 20, 12, 20));
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
        field.setPreferredSize(new Dimension(180, 42));
        field.setMargin(new Insets(8, 12, 8, 12));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(ACCENT);
        field.setFont(new Font(FONT, Font.PLAIN, 14));
        return field;
    }

    // ---------- Tabel ----------

    static void styleTable(JTable table) {
        table.setRowHeight(44);
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
        JLabel icon = new JLabel(new SearchIcon());
        icon.setPreferredSize(new Dimension(18, 18));
        icon.setForeground(TEXT_MUTED);
        search.putClientProperty("JTextField.placeholderText", hint);
        p.add(icon, BorderLayout.WEST);
        p.add(search, BorderLayout.CENTER);
        return p;
    }

    private static class SearchIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 15;
        }

        @Override
        public int getIconHeight() {
            return 15;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TEXT_MUTED);
            g2.setStroke(new BasicStroke(1.7f));
            g2.drawOval(x + 1, y + 1, 8, 8);
            g2.drawLine(x + 9, y + 9, x + 13, y + 13);
            g2.dispose();
        }
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
