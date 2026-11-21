package warungaju;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

class AnimatedCardPanel extends JPanel {
    private final Map<String, JComponent> pages = new LinkedHashMap<>();
    private JComponent active;
    private Timer timer;

    AnimatedCardPanel() {
        setLayout(null);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
    }

    void addPage(String name, JComponent page) {
        pages.put(name, page);
        page.setVisible(false);
        add(page);
    }

    void clearPages() {
        if (timer != null)
            timer.stop();
        pages.clear();
        active = null;
        removeAll();
    }

    void showPage(final String name) {
        final JComponent next = pages.get(name);
        if (next == null) return;
        if (timer != null && timer.isRunning())
            timer.stop();

        for (JComponent page : pages.values()) {
            if (page != active) {
                page.setVisible(false);
                page.setLocation(0, 0);
            } else if (active != null) {
                page.setVisible(true);
                page.setBounds(0, 0, getWidth(), getHeight());
            }
        }
        if (next == active) {
            revalidate();
            repaint();
            return;
        }

        final JComponent previous = active;
        active = next;
        if (previous == null) {
            active = next;
            next.setBounds(0, 0, getWidth(), getHeight());
            next.setVisible(true);
            revalidate();
            repaint();
            return;
        }
        if (getWidth() <= 0 || getHeight() <= 0) {
            previous.setVisible(false);
            next.setBounds(0, 0, getWidth(), getHeight());
            next.setVisible(true);
            revalidate();
            repaint();
            return;
        }
        final int direction = 1;
        final int width = getWidth();
        final int height = getHeight();
        next.setVisible(true);
        next.setBounds(direction * width, 0, width, height);
        final long started = System.currentTimeMillis();
        final Timer transition = new Timer(15, e -> {
            if (active != next) {
                ((Timer) e.getSource()).stop();
                return;
            }
            double progress = Math.min(1, (System.currentTimeMillis() - started) / 260.0);
            double eased = 1 - Math.pow(1 - progress, 3);
            int shift = (int) (width * eased);
            previous.setLocation(-direction * shift, 0);
            next.setLocation(direction * width - direction * shift, 0);
            if (progress >= 1) {
                previous.setVisible(false);
                next.setLocation(0, 0);
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        timer = transition;
        transition.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Pakai ukuran seluruh panel, bukan area yang sedang di-repaint
        int w = getWidth();
        int h = getHeight();

        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(1, 1, w - 2, h - 2,
            UI.ARC, UI.ARC);
        g2.setColor(UI.GLASS_LIGHT);
        g2.fill(shape);
        g2.setColor(UI.GLASS_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(shape);
        g2.dispose();
    }

    @Override
    public void doLayout() {
        if (active != null && (timer == null || !timer.isRunning())) {
            active.setBounds(0, 0, getWidth(), getHeight());
        }
    }
}