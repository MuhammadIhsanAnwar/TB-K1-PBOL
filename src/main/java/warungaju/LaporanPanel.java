package warungaju;

import java.awt.*;
import java.util.List;
import javax.swing.*;

public class LaporanPanel extends JPanel {
    public LaporanPanel(MainFrame f) {
        setLayout(new BorderLayout(18, 18));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 30, 30, 30));

        add(UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Laporan Keuangan</h1><font color='#C8D0FF'>Ringkasan laba rugi, arus kas, dan posisi keuangan usaha</font></html>"), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UI.BG_DARK);
        tabs.setForeground(UI.TEXT_LIGHT);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        // Polimorfisme: semua laporan diperlakukan sama lewat tipe Laporan.
        for (Laporan laporan : f.laporan.semua()) {
            tabs.addTab(laporan.judul(), panelLaporan(laporan));
        }

        add(tabs, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.add(UI.secondaryButton("Ekspor PDF (segera hadir)"));
        actions.add(UI.secondaryButton("Ekspor Excel (segera hadir)"));
        add(actions, BorderLayout.SOUTH);
    }

    private JPanel panelLaporan(Laporan laporan) {
        List<Laporan.Baris> baris = laporan.baris();

        JPanel panel = UI.glassPanel();
        panel.setLayout(new GridLayout(baris.size(), 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        for (Laporan.Baris b : baris) {
            addLabelValuePair(panel, b.label, UI.money(b.nilai), b.penekanan);
        }
        return panel;
    }

    private void addLabelValuePair(JPanel panel, String label, String value, boolean penekanan) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(UI.TEXT_LIGHT);
        if (penekanan) lbl.setFont(new Font(UI.FONT, Font.BOLD, 14));
        panel.add(lbl);

        JLabel val = new JLabel(value);
        val.setForeground(penekanan ? UI.ACCENT : UI.TEXT_LIGHT);
        val.setFont(new Font(UI.FONT, Font.BOLD, penekanan ? 17 : 14));
        panel.add(val);
    }
}
