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
        if (timer != null) timer.stop();
        pages.clear();
        active = null;
        removeAll();
    }

    void showPage(final String name) {
        final JComponent next = pages.get(name);
        if (next == null || next == active) return;
        if (timer != null && timer.isRunning()) timer.stop();
        final JComponent previous = active;
        if (previous == null) {
            active = next;
            next.setBounds(0, 0, getWidth(), getHeight());
            next.setVisible(true);
            revalidate();
            repaint();
            return;
        }
        final int direction = 1;
        next.setVisible(true);
        next.setBounds(direction * getWidth(), 0, getWidth(), getHeight());
        final long started = System.currentTimeMillis();
        timer = new Timer(15, e -> {
            double progress = Math.min(1, (System.currentTimeMillis() - started) / 260.0);
            double eased = 1 - Math.pow(1 - progress, 3);
            int shift = (int) (getWidth() * eased);
            previous.setLocation(-direction * shift, 0);
            next.setLocation(direction * getWidth() - direction * shift, 0);
            if (progress >= 1) {
                previous.setVisible(false);
                next.setLocation(0, 0);
                active = next;
                timer.stop();
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Rectangle clip = g2.getClipBounds();
        if (clip == null) clip = new Rectangle(0, 0, getWidth(), getHeight());

        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(
            clip.x + 2, clip.y + 2, clip.width - 4, clip.height - 4, 24, 24
        );

        GradientPaint glass = new GradientPaint(
            clip.x, clip.y, new Color(255, 255, 255, UI.lightMode ? 42 : 10),
            clip.x, clip.y + clip.height, UI.GLASS_LIGHT
        );
        g2.setPaint(glass);
        g2.fill(shape);

        g2.setColor(UI.GLASS_BORDER);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(shape);

        g2.setColor(new Color(255, 255, 255, UI.lightMode ? 70 : 5));
        g2.setStroke(new BasicStroke(0.8f));
        g2.draw(new RoundRectangle2D.Float(
            clip.x + 4, clip.y + 4, clip.width - 8, clip.height - 8, 20, 20
        ));

        g2.dispose();
    }

    @Override
    public void doLayout() {
        if (active != null && (timer == null || !timer.isRunning())) {
            active.setBounds(0, 0, getWidth(), getHeight());
        }
    }
}        