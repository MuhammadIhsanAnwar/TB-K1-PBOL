package warungaju;

import java.awt.*;
import javax.swing.*;

public class SimulasiPanel extends JPanel {
    final JLabel result = UI.htmlLabel("<html><h2 style='color:#FFFFF0'>Belum ada simulasi</h2><p style='color:#C8D0FF'>Atur asumsi usaha, lalu hitung perkiraan laba.</p></html>");

    public SimulasiPanel(MainFrame f) {
        setLayout(new BorderLayout(18, 18));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 30, 30, 30));
        
        add(UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Simulasi Bisnis</h1><font color='#C8D0FF'>Ukur dampak biaya, kapasitas, dan harga terhadap laba usaha.</font></html>"), BorderLayout.NORTH);
        
        JPanel form = UI.formGrid();
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(9, 10, 9, 10);
        
        WorkspaceData.SimulationSettings saved = f.workspace.simulation;
        JTextField bahan = UI.input(String.valueOf(saved.ingredientPrice));
        JTextField karyawan = UI.input(String.valueOf(saved.employeeCount));
        JTextField gaji = UI.input(String.valueOf(saved.salaryPerEmployee));
        JTextField jam = UI.input(String.valueOf(saved.operatingHours));
        JTextField target = UI.input(String.valueOf(saved.monthlySalesTarget));
        JTextField harga = UI.input(String.valueOf(saved.sellingPrice));
        
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
        box.add(UI.htmlLabel("<html><h2 style='color:#FFFFF0'>Asumsi usaha</h2><p style='color:#C8D0FF'>Sesuaikan nilai dengan rencana produksi dan penjualan Anda.</p></html>"), BorderLayout.NORTH);
        box.add(form, BorderLayout.CENTER);
        
        JPanel output = UI.glassPanel();
        output.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        output.add(UI.htmlLabel("<html><h2 style='color:#FFFFF0'>Hasil simulasi</h2></html>"), BorderLayout.NORTH);
        output.add(result, BorderLayout.CENTER);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, box, output);
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        split.setDividerSize(1);
        split.setContinuousLayout(true);
        split.setBackground(UI.BG_DARK);
        split.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        add(split, BorderLayout.CENTER);
        
        run.addActionListener(e -> {
            try {
                double hargaBahan = Double.parseDouble(bahan.getText());
                int jumlahKaryawan = Integer.parseInt(karyawan.getText());
                double gajiKaryawan = Double.parseDouble(gaji.getText());
                double jamOperasional = Double.parseDouble(jam.getText());
                double targetPenjualan = Double.parseDouble(target.getText());
                double hargaJual = Double.parseDouble(harga.getText());
                SimulasiBisnis service = new SimulasiBisnis(hargaBahan, jumlahKaryawan,
                    gajiKaryawan, jamOperasional, targetPenjualan, hargaJual);
                Perhitungan perhitungan = service;
                double labaBersih = perhitungan.hitung();
                SimulasiBisnis.Result r = service.getHasilTerakhir();
                saved.ingredientPrice = Double.parseDouble(bahan.getText());
                saved.employeeCount = Integer.parseInt(karyawan.getText());
                saved.salaryPerEmployee = Double.parseDouble(gaji.getText());
                saved.operatingHours = Double.parseDouble(jam.getText());
                saved.monthlySalesTarget = Double.parseDouble(target.getText());
                saved.sellingPrice = Double.parseDouble(harga.getText());
                f.persistWorkspace();
                result.setText(UI.html("<html><h2 style='color:#DFFF00'>" + perhitungan.label()
                    + ": " + UI.money(labaBersih) + "</h2><p style='color:#FFFFF0'>Pendapatan: "
                    + UI.money(r.pendapatan) + "<br>HPP: " + UI.money(r.hpp)
                    + "<br>Operasional: " + UI.money(r.operasional) + "<br>Margin: "
                    + String.format("%.1f%%", r.margin) + "</p></html>"));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Semua variabel harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}