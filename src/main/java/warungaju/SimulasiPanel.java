package warungaju;

import java.awt.*;
import javax.swing.*;

public class SimulasiPanel extends JPanel {
    final SimulasiBisnis service = new SimulasiBisnis();
    final JLabel result = UI.htmlLabel("<html><h2 style='color:#FFFFF0'>Belum ada simulasi</h2><p style='color:#C8D0FF'>Pilih variabel lalu tekan Simulasikan.</p></html>");

    public SimulasiPanel(MainFrame f) {
        setLayout(new BorderLayout(15, 15));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 35, 35, 35));
        
        add(UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Simulasi Bisnis</h1><font color='#C8D0FF'>Uji perubahan strategi sebelum diterapkan di usaha</font></html>"), BorderLayout.NORTH);
        
        JPanel form = UI.formGrid();
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        
        JTextField bahan = UI.input("9000");
        JTextField karyawan = UI.input("2");
        JTextField gaji = UI.input("2500000");
        JTextField jam = UI.input("10");
        JTextField target = UI.input("100");
        JTextField harga = UI.input("18000");
        
        UI.field(form, g, "Harga bahan baku / unit", bahan, 0);
        UI.field(form, g, "Jumlah karyawan", karyawan, 1);
        UI.field(form, g, "Gaji / karyawan", gaji, 2);
        UI.field(form, g, "Jam operasional / hari", jam, 3);
        UI.field(form, g, "Target penjualan / bulan", target, 4);
        UI.field(form, g, "Harga jual / unit", harga, 5);
        
        JButton run = UI.button("Simulasikan");
        UI.field(form, g, "", run, 6);
        
        JPanel box = UI.glassPanel();
        box.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        box.add(UI.htmlLabel("<html><h2 style='color:#FFFFF0'>Input variabel</h2><p style='color:#C8D0FF'>Data contoh aktif. Ganti nilai untuk mencoba skenario.</p></html>"), BorderLayout.NORTH);
        box.add(form, BorderLayout.CENTER);
        
        JPanel output = UI.glassPanel();
        output.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        output.add(UI.htmlLabel("<html><h2 style='color:#FFFFF0'>Hasil simulasi</h2></html>"), BorderLayout.NORTH);
        output.add(result, BorderLayout.CENTER);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, box, output);
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        split.setDividerSize(8);
        split.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        add(split, BorderLayout.CENTER);
        
        run.addActionListener(e -> {
            try {
                SimulasiBisnis.Result r = service.hitung(
                    Double.parseDouble(bahan.getText()),
                    Integer.parseInt(karyawan.getText()),
                    Double.parseDouble(gaji.getText()),
                    Double.parseDouble(jam.getText()),
                    Double.parseDouble(target.getText()),
                    Double.parseDouble(harga.getText())
                );
                result.setText(UI.html("<html><h2 style='color:#DFFF00'>" + UI.money(r.laba) + "</h2><p style='color:#FFFFF0'>Pendapatan: " + UI.money(r.pendapatan) + "<br>HPP: " + UI.money(r.hpp) + "<br>Operasional: " + UI.money(r.operasional) + "<br>Margin: " + String.format("%.1f%%", r.margin) + "</p></html>"));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Semua variabel harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}