package warungaju;

import java.awt.*;
import javax.swing.*;

public class DashboardPanel extends JPanel {
    public DashboardPanel(MainFrame f) {
        setLayout(new BorderLayout(0, 20));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 35, 35, 35));
        
        JLabel header = UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Selamat datang di WARUNGAJU</h1><font color='#C8D0FF'>Ringkasan kondisi usaha Anda hari ini</font></html>");
        header.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(header, BorderLayout.NORTH);
        
        double in = f.laporan.pemasukan(f.transaksi);
        double out = f.laporan.pengeluaran(f.transaksi);
        
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(UI.card("Pemasukan bulan ini", UI.money(in), new Color(40, 190, 132), () -> f.showPage("Transaksi")));
        cards.add(UI.card("Pengeluaran bulan ini", UI.money(out), new Color(237, 94, 111), () -> f.showPage("Transaksi")));
        cards.add(UI.card("Keuntungan bersih", UI.money(in - out), new Color(245, 177, 66), () -> f.showPage("Simulasi Bisnis")));
        cards.add(UI.card("Saldo kas saat ini", UI.money(in - out + 3500000), new Color(111, 121, 232), () -> f.showPage("Prediksi Arus Kas")));
        
        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        
        JPanel info = UI.glassPanel();
        info.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        String status = f.actualDataImported ? "<font color='#DFFF00'><b>Tersedia</b></font>" : "<font color='#C8D0FF'>Belum tersedia</font>";
        JPanel activity = new JPanel(new BorderLayout(8, 8));
        activity.setOpaque(false);
        activity.add(UI.htmlLabel("<html><h2 style='color:#FFFFF0'>Ringkasan aktivitas</h2><font color='#C8D0FF'>Gunakan data aktual untuk hasil simulasi yang lebih akurat.</font></html>"), BorderLayout.NORTH);
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