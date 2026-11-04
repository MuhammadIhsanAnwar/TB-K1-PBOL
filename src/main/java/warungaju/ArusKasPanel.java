package warungaju;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ArusKasPanel extends JPanel {
    public ArusKasPanel(MainFrame f) {
        setLayout(new BorderLayout(16, 16));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 35, 35, 35));
        
        add(UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Prediksi Arus Kas</h1><font color='#C8D0FF'>Proyeksi saldo berdasarkan tren data contoh</font></html>"), BorderLayout.NORTH);
        
        JPanel input = UI.glassPanel();
        input.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField saldo = UI.input("3500000");
        JTextField in = UI.input("1250000");
        JTextField out = UI.input("650000");
        JButton run = UI.button("Prediksi 6 Bulan");

        input.setLayout(new GridBagLayout());
        GridBagConstraints fields = new GridBagConstraints();
        UI.field(input, fields, "Saldo awal", saldo, 0);
        UI.field(input, fields, "Pemasukan / bulan", in, 1);
        UI.field(input, fields, "Pengeluaran / bulan", out, 2);
        fields.gridx = 1;
        fields.gridy = 3;
        fields.weightx = 1;
        fields.fill = GridBagConstraints.HORIZONTAL;
        fields.insets = new Insets(10, 0, 0, 10);
        input.add(run, fields);
        
        DefaultTableModel m = new DefaultTableModel(new String[]{"Periode", "Prediksi Pemasukan", "Prediksi Pengeluaran", "Saldo Akhir", "Status"}, 0);
        JTable table = new JTable(m);
        UI.styleTable(table);
        
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        body.add(input, BorderLayout.NORTH);
        body.add(UI.scrollPane(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        
        run.addActionListener(e -> {
            m.setRowCount(0);
            try {
                for (PrediksiArusKas.Month x : new PrediksiArusKas().prediksi(Double.parseDouble(saldo.getText()), Double.parseDouble(in.getText()), Double.parseDouble(out.getText()), 6)) {
                    m.addRow(new Object[]{x.nama, UI.money(x.pemasukan), UI.money(x.pengeluaran), UI.money(x.saldo), x.saldo < 0 ? "DEFISIT" : "Aman"});
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Input harus angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}