package warungaju;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ArusKasPanel extends JPanel {
    public ArusKasPanel(MainFrame f) {
        setLayout(new BorderLayout(18, 18));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 30, 30, 30));
        
        add(UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Prediksi Arus Kas</h1><font color='#C8D0FF'>Perkirakan posisi kas dari asumsi pemasukan dan pengeluaran.</font></html>"), BorderLayout.NORTH);
        
        JPanel input = UI.glassPanel();
        input.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        
        WorkspaceData.CashFlowSettings saved = f.workspace.cashFlow;
        JTextField saldo = UI.input(String.valueOf(saved.startingBalance));
        JTextField in = UI.input(String.valueOf(saved.monthlyIncome));
        JTextField out = UI.input(String.valueOf(saved.monthlyExpenses));
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
        
        JLabel saldoAkhir = new JLabel("Saldo akhir: -");
        saldoAkhir.setForeground(UI.TEXT_LIGHT);
        saldoAkhir.setFont(new Font(UI.FONT, Font.BOLD, 16));
        saldoAkhir.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        body.add(input, BorderLayout.NORTH);
        body.add(UI.scrollPane(table), BorderLayout.CENTER);
        body.add(saldoAkhir, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);
        
        run.addActionListener(e -> {
            m.setRowCount(0);
            try {
                saved.startingBalance = Double.parseDouble(saldo.getText());
                saved.monthlyIncome = Double.parseDouble(in.getText());
                saved.monthlyExpenses = Double.parseDouble(out.getText());
                PrediksiArusKas service = new PrediksiArusKas(saved.startingBalance,
                    saved.monthlyIncome, saved.monthlyExpenses, 6);
                Perhitungan perhitungan = service;
                double nilaiAkhir = perhitungan.hitung();
                for (PrediksiArusKas.Month x : service.prediksi()) {
                    m.addRow(new Object[]{x.nama, UI.money(x.pemasukan), UI.money(x.pengeluaran), UI.money(x.saldo), x.saldo < 0 ? "DEFISIT" : "Aman"});
                }
                saldoAkhir.setText(perhitungan.label() + ": " + UI.money(nilaiAkhir));
                f.persistWorkspace();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Input harus angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}