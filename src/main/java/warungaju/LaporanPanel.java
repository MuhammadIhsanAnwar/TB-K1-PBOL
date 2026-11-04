package warungaju;

import java.awt.*;
import javax.swing.*;

public class LaporanPanel extends JPanel {
    public LaporanPanel(MainFrame f) {
        setLayout(new BorderLayout(15, 15));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 35, 35, 35));
        
        add(UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Laporan Keuangan</h1><font color='#C8D0FF'>Ringkasan laba rugi dan posisi keuangan usaha</font></html>"), BorderLayout.NORTH);
        
        double in = f.laporan.pemasukan(f.transaksi);
        double out = f.laporan.pengeluaran(f.transaksi);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UI.BG_DARK);
        tabs.setForeground(UI.TEXT_LIGHT);
        
        JPanel laba = UI.glassPanel();
        laba.setLayout(new GridLayout(5, 2, 10, 10));
        laba.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        addLabelValuePair(laba, "Total pendapatan", UI.money(in));
        addLabelValuePair(laba, "Harga pokok penjualan", UI.money(in * 0.45));
        addLabelValuePair(laba, "Laba kotor", UI.money(in * 0.55));
        addLabelValuePair(laba, "Biaya operasional", UI.money(out));
        addLabelValuePair(laba, "Laba bersih", UI.money(in * 0.55 - out));
        
        tabs.addTab("Laporan Laba Rugi", laba);
        
        JPanel neraca = UI.glassPanel();
        neraca.setLayout(new GridLayout(3, 2, 10, 10));
        neraca.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        addLabelValuePair(neraca, "Aset", UI.money(in - out + 3500000));
        addLabelValuePair(neraca, "Liabilitas", UI.money(0));
        addLabelValuePair(neraca, "Ekuitas", UI.money(in - out + 3500000));
        
        tabs.addTab("Neraca Sederhana", neraca);
        add(tabs, BorderLayout.CENTER);
        
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.add(UI.secondaryButton("Ekspor PDF (segera hadir)"));
        actions.add(UI.secondaryButton("Ekspor Excel (segera hadir)"));
        add(actions, BorderLayout.SOUTH);
    }
    
    private void addLabelValuePair(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(UI.TEXT_LIGHT);
        panel.add(lbl);
        JLabel val = new JLabel(value);
        val.setForeground(UI.ACCENT);
        val.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(val);
    }
}