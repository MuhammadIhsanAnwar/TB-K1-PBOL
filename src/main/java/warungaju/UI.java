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

class UI {
    static boolean lightMode;
    static Color BG_DARK;
    static Color ACCENT;
    static Color ACCENT_HOVER;
    static Color PURPLE_TEXT;
    static Color PRIMARY_START;
    static Color PRIMARY_END;
    static Color PRIMARY_HOVER_START;
    static Color PRIMARY_HOVER_END;
    static Color TEXT_LIGHT;
    static Color TEXT_MUTED;
    static Color GLASS_LIGHT;
    static Color GLASS_BORDER;
    static Color GLASS_ACTIVE;
    static Color GLASS_TOP;
    static Color GLASS_BOTTOM;
    static Color GLOW_PURPLE;
    static Color GLOW_GREEN;

    static {
        initTheme();
    }

    static void initTheme() {
        if (lightMode) {
            BG_DARK = new Color(238, 242, 252);
            ACCENT = new Color(104, 78, 218);
            ACCENT_HOVER = new Color(127, 105, 232);
            PURPLE_TEXT = new Color(92, 63, 177);
            TEXT_LIGHT = new Color(31, 39, 67);
            TEXT_MUTED = new Color(91, 103, 134);
            GLASS_LIGHT = new Color(255, 255, 255, 138);
            GLASS_BORDER = new Color(117, 132, 178, 100);
            GLASS_ACTIVE = new Color(112, 90, 222, 52);
            GLASS_TOP = new Color(255, 255, 255, 205);
            GLASS_BOTTOM = new Color(175, 190, 232, 120);
            GLOW_PURPLE = new Color(133, 100, 235, 35);
            GLOW_GREEN = new Color(37, 166, 132, 30);
        } else {
            BG_DARK = new Color(13, 18, 35);
            ACCENT = new Color(223, 255, 0);
            ACCENT_HOVER = new Color(255, 255, 100);
            PURPLE_TEXT = new Color(204, 184, 255);
            TEXT_LIGHT = new Color(255, 255, 240);
            TEXT_MUTED = new Color(190, 200, 229);
            GLASS_LIGHT = new Color(146, 164, 210, 24);
            GLASS_BORDER = new Color(200, 216, 255, 58);
            GLASS_ACTIVE = new Color(139, 149, 240, 52);
            GLASS_TOP = new Color(84, 125, 205, 62);
            GLASS_BOTTOM = new Color(111, 77, 176, 38);
            GLOW_PURPLE = new Color(91, 87, 173, 45);
            GLOW_GREEN = new Color(37, 150, 135, 23);
        }
        PRIMARY_START = new Color(255, 224, 90);
        PRIMARY_END = new Color(255, 174, 66);
        PRIMARY_HOVER_START = new Color(255, 237, 130);
        PRIMARY_HOVER_END = new Color(255, 192, 87);
        UIManager.put("Component.arc", 16);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 13));
        UIManager.put("Button.background", ACCENT);
        UIManager.put("Button.foreground", new Color(42, 34, 73));
        UIManager.put("Button.borderWidth", 0);
        UIManager.put("Table.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("Table.foreground", TEXT_LIGHT);
        UIManager.put("Table.selectionBackground", GLASS_ACTIVE);
        UIManager.put("Table.selectionForeground", TEXT_LIGHT);
        UIManager.put("Table.rowHeight", 36);
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("TabbedPane.selected", GLASS_ACTIVE);
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.background", BG_DARK);
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("Label.foreground", TEXT_LIGHT);
        UIManager.put("TextField.background", GLASS_LIGHT);
        UIManager.put("TextField.foreground", TEXT_LIGHT);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("ComboBox.background", BG_DARK);
        UIManager.put("ComboBox.foreground", TEXT_LIGHT);
        UIManager.put("ScrollPane.background", BG_DARK);
        UIManager.put("Table.background", BG_DARK);
        UIManager.put("TableHeader.background", GLASS_ACTIVE);
        UIManager.put("TableHeader.foreground", TEXT_LIGHT);
    }

    static void toggleTheme() {
        lightMode = !lightMode;
        if (lightMode) FlatLightLaf.setup();
        else FlatDarkLaf.setup();
        initTheme();
    }

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

    static JPanel glassPanel() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setPaint(new GradientPaint(0, 0, GLASS_TOP, getWidth(), getHeight(), GLASS_BOTTOM));
                g2.fill(shape);
                g2.setPaint(new GradientPaint(0, 2,
                    new Color(255, 255, 255, lightMode ? 150 : 34),
                    getWidth(), 5,
                    new Color(PRIMARY_START.getRed(), PRIMARY_START.getGreen(), PRIMARY_START.getBlue(),
                        lightMode ? 48 : 30)));
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, 5, 5, 5));
                g2.setColor(GLASS_BORDER);
                g2.setStroke(new BasicStroke(0.8f));
                g2.draw(shape);
                g2.setColor(lightMode ? new Color(255, 255, 255, 85) : new Color(255, 255, 255, 18));
                g2.draw(new RoundRectangle2D.Float(2, 2, getWidth() - 5, getHeight() - 5, 14, 14));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    static JButton themeToggle(final Runnable onToggle) {
        return new ThemeToggle(onToggle);
    }

    private static class ThemeToggle extends JButton {
        private final Runnable onToggle;
        private float position;
        private float target;
        private Timer animation;
        private boolean hovering;

        ThemeToggle(final Runnable onToggle) {
            this.onToggle = onToggle;
            position = lightMode ? 1f : 0f;
            setPreferredSize(new Dimension(126, 42));
            setMinimumSize(new Dimension(126, 42));
            setMaximumSize(new Dimension(126, 42));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setFocusable(true);
            setToolTipText(lightMode ? "Switch to dark mode" : "Switch to light mode");
            getAccessibleContext().setAccessibleName("Toggle light and dark mode");
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hovering = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hovering = false;
                    repaint();
                }
            });
            addActionListener(e -> animateToggle());
        }

        private void animateToggle() {
            if (animation != null && animation.isRunning()) return;
            target = lightMode ? 0f : 1f;
            animation = new Timer(15, e -> {
                position += (target - position) * 0.24f;
                if (Math.abs(target - position) < 0.025f) {
                    position = target;
                    ((Timer) e.getSource()).stop();
                    onToggle.run();
                }
                repaint();
            });
            animation.start();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color track = lightMode ? new Color(105, 82, 205, hovering ? 220 : 185)
                : new Color(70, 81, 119, hovering ? 230 : 190);
            g.setColor(track);
            g.fillRoundRect(2, 4, 76, 34, 17, 17);
            g.setColor(GLASS_BORDER);
            g.drawRoundRect(2, 4, 76, 34, 17, 17);
            int knobX = 5 + Math.round(position * 40);
            g.setColor(new Color(255, 255, 255, 238));
            g.fillOval(knobX, 7, 28, 28);
            if (position < 0.5f) {
                g.setColor(new Color(79, 91, 133));
                g.fillOval(knobX + 8, 12, 12, 12);
                g.setColor(new Color(255, 255, 255, 238));
                g.fillOval(knobX + 12, 9, 12, 12);
            } else {
                g.setColor(new Color(245, 174, 45));
                g.fillOval(knobX + 8, 15, 12, 12);
                g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    int x1 = knobX + 14 + (int) (Math.cos(angle) * 8);
                    int y1 = 21 + (int) (Math.sin(angle) * 8);
                    int x2 = knobX + 14 + (int) (Math.cos(angle) * 10);
                    int y2 = 21 + (int) (Math.sin(angle) * 10);
                    g.drawLine(x1, y1, x2, y2);
                }
            }
            g.setColor(TEXT_LIGHT);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString(position >= 0.5f ? "Light" : "Dark", 85, 26);
            if (hasFocus()) {
                g.setColor(ACCENT);
                g.drawRoundRect(0, 2, 82, 38, 19, 19);
            }
            g.dispose();
        }
    }

    static JPanel card(String title, String value, Color color) {
        return card(title, value, color, null);
    }

    static JPanel card(String title, String value, Color color, final Runnable onClick) {
        JPanel p = glassPanel();
        p.setLayout(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        JLabel a = new JLabel(title.toUpperCase());
        a.setForeground(TEXT_MUTED);
        a.setFont(new Font("SansSerif", Font.BOLD, 11));
        JLabel b = new JLabel(value);
        b.setFont(new Font("SansSerif", Font.BOLD, 24));
        b.setForeground(color);
        p.add(a, BorderLayout.NORTH);
        p.add(b, BorderLayout.CENTER);
        addCardHover(p, a, b, onClick);
        return p;
    }

    private static void addCardHover(final JPanel panel, final JLabel title, final JLabel value, final Runnable onClick) {
        MouseAdapter hover = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (onClick != null) panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 130), 1, true),
                    new EmptyBorder(19, 23, 19, 23)
                ));
            }

            public void mouseExited(MouseEvent e) {
                Component source = e.getComponent();
                Point point = SwingUtilities.convertPoint(source, e.getPoint(), panel);
                if (!panel.contains(point)) {
                    panel.setBorder(new EmptyBorder(20, 24, 20, 24));
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

    static String money(double n) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(n).replace(",00", "");
    }

    static JButton button(String text) {
        return new GradientButton(text);
    }

    private static class GradientButton extends JButton {
        private float hover;
        private final Timer animation;

        GradientButton(String text) {
            super(text);
            setForeground(new Color(42, 34, 73));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("SansSerif", Font.BOLD, 14));
            animation = new Timer(15, e -> {
                float target = getModel().isRollover() ? 1f : 0f;
                hover += (target - hover) * 0.28f;
                if (Math.abs(target - hover) < 0.025f) {
                    hover = target;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { animation.start(); }
                public void mouseExited(MouseEvent e) { animation.start(); }
            });
            getModel().addChangeListener(e -> {
                if (getModel().isPressed()) {
                    hover = 1f;
                    repaint();
                } else if (!getModel().isRollover()) {
                    animation.start();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color start = interpolate(PRIMARY_START, PRIMARY_HOVER_START, hover);
            Color end = interpolate(PRIMARY_END, PRIMARY_HOVER_END, hover);
            g.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g.setColor(new Color(255, 255, 255, 100));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    static JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setForeground(TEXT_LIGHT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addGlassHover(b);
        return b;
    }

    static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(GLASS_LIGHT);
        b.setForeground(TEXT_LIGHT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GLASS_BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addSmoothHover(b, GLASS_LIGHT, GLASS_ACTIVE);
        return b;
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

    static void addGlassHover(final JButton button) {
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setOpaque(true);
                button.setBackground(GLASS_ACTIVE);
                button.setForeground(ACCENT);
            }
            public void mouseExited(MouseEvent e) {
                if (!Boolean.TRUE.equals(button.getClientProperty("warungaju.active"))) {
                    button.setOpaque(false);
                    button.setContentAreaFilled(false);
                    button.setForeground(TEXT_LIGHT);
                }
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
        field.setPreferredSize(new Dimension(150, 40));
        field.setBackground(GLASS_LIGHT);
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GLASS_BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    static void styleTable(JTable table) {
        table.setRowHeight(44);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setGridColor(GLASS_BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(GLASS_ACTIVE);
        table.setSelectionForeground(TEXT_LIGHT);
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(0, 0, 0, 0));
        table.setDefaultRenderer(Object.class, new StripeRenderer());
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(GLASS_ACTIVE);
        header.setForeground(TEXT_LIGHT);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
    }

    static JScrollPane scrollPane(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setOpaque(false);
        pane.getViewport().setOpaque(false);
        pane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GLASS_BORDER, 1, true),
            BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));
        pane.getVerticalScrollBar().setUnitIncrement(16);
        return pane;
    }

    static JPanel searchBar(JTextField search, String hint) {
        JPanel p = glassPanel();
        p.setLayout(new BorderLayout(10, 0));
        p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JLabel icon = new JLabel("CARI");
        icon.setFont(new Font("SansSerif", Font.BOLD, 12));
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
            setHorizontalAlignment(value instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (!selected) {
                c.setBackground(row % 2 == 0 ? BG_DARK : GLASS_LIGHT);
                c.setForeground(TEXT_LIGHT);
            }
            return c;
        }
    }
}