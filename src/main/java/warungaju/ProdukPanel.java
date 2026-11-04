package warungaju;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ProdukPanel extends JPanel {
    final MainFrame f;
    final DefaultTableModel model = new DefaultTableModel(new String[]{"Produk", "Biaya Produksi", "Harga Jual", "Stok", "HPP", "Margin"}, 0);

    public ProdukPanel(MainFrame f) {
        this.f = f;
        setLayout(new BorderLayout(16, 16));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 35, 35, 35));
        
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Produk dan HPP</h1><font color='#C8D0FF'>Kelola produk, biaya, dan margin keuntungan</font></html>"), BorderLayout.WEST);
        JButton add = UI.button("Tambah Produk");
        top.add(add, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        
        for (Produk p : f.produk) row(p);
        
        JTable table = new JTable(model);
        UI.styleTable(table);
        add(UI.scrollPane(table), BorderLayout.CENTER);
        
        add.addActionListener(e -> dialog());
    }

    void row(Produk p) {
        model.addRow(new Object[]{p.getNama(), UI.money(p.getBiayaProduksi()), UI.money(p.getHargaJual()), p.getStok(), UI.money(p.getHpp()), String.format("%.1f%%", p.getMargin())});
    }

    void dialog() {
        JTextField n = UI.input("");
        JTextField b = UI.input("");
        JTextField h = UI.input("");
        JTextField s = UI.input("");
        JPanel p = UI.formGrid();
        GridBagConstraints g = new GridBagConstraints();
        UI.field(p, g, "Nama produk", n, 0);
        UI.field(p, g, "Biaya produksi", b, 1);
        UI.field(p, g, "Harga jual", h, 2);
        UI.field(p, g, "Stok", s, 3);
        
        if (JOptionPane.showConfirmDialog(this, p, "Tambah produk", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Produk x = new Produk(n.getText(), Double.parseDouble(b.getText()), Double.parseDouble(h.getText()), Integer.parseInt(s.getText()));
                if (n.getText().trim().isEmpty()) throw new IllegalArgumentException();
                f.produk.add(x);
                row(x);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lengkapi data produk dengan benar.", "Validasi", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}