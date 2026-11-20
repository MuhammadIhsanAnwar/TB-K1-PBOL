package warungaju;

import java.awt.*;
import javax.swing.*;

public class DashboardPanel extends JPanel {
    public DashboardPanel(MainFrame f) {
        setLayout(new BorderLayout(0, 20));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 35, 35, 35));
        
        JLabel header = UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Ringkasan usaha</h1><font color='#C8D0FF'>Pantau pemasukan, pengeluaran, dan posisi kas.</font></html>");
        header.setFont(new Font(UI.FONT, Font.PLAIN, 14));
        add(header, BorderLayout.NORTH);
        
        double in = f.laporan.pemasukan();
        double out = f.laporan.pengeluaran();
        
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(UI.card("Pemasukan tercatat", UI.money(in), UI.GREEN, () -> f.showPage("Transaksi")));
        cards.add(UI.card("Pengeluaran tercatat", UI.money(out), UI.RED, () -> f.showPage("Transaksi")));
        cards.add(UI.card("Selisih pemasukan", UI.money(in - out), UI.AMBER, () -> f.showPage("Simulasi Bisnis")));
        cards.add(UI.card("Saldo kas tercatat", UI.money(in - out + 3500000), UI.ACCENT, () -> f.showPage("Prediksi Arus Kas")));
        
        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        
        JPanel info = UI.glassPanel();
        info.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        String status = f.actualDataImported ? "<font color='#DFFF00'><b>Sudah diimpor</b></font>" : "<font color='#C8D0FF'>Belum ada impor</font>";
        JPanel activity = new JPanel(new BorderLayout(8, 8));
        activity.setOpaque(false);
        activity.add(UI.htmlLabel("<html><h2 style='color:#FFFFF0'>Aktivitas keuangan</h2><font color='#C8D0FF'>Data tersimpan pada ruang kerja akun ini.</font></html>"), BorderLayout.NORTH);
        activity.add(UI.htmlLabel("<html><p style='color:#FFFFF0'><b>" + f.transaksi.size()
            + "</b> transaksi tercatat &nbsp; | &nbsp; Data aktual: " + status
            + "</p><p style='color:#FFFFF0'>Prediksi arus kas 3 bulan: <font color='#DFFF00'><b>stabil</b></font></p></html>"), BorderLayout.CENTER);

        JPanel shortcuts = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        shortcuts.setOpaque(false);
        shortcuts.add(shortcut("Catat transaksi", "Transaksi", f));
        shortcuts.add(shortcut("Kelola produk", "Produk dan HPP", f));
        shortcuts.add(shortcut("Buat simulasi", "Simulasi Bisnis", f));
        activity.add(shortcuts, BorderLayout.SOUTH);
        info.add(activity, BorderLayout.CENTER);
        
        center.add(info, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JButton shortcut(String label, final String page, final MainFrame frame) {
        JButton button = UI.secondaryButton(label);
        button.addActionListener(e -> frame.showPage(page));
        return button;
    }
}