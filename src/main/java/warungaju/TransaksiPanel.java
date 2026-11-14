package warungaju;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TransaksiPanel extends JPanel {
    final MainFrame f;

    // Tabel dibuat READ-ONLY. Sebelumnya sel bisa diedit langsung (klik dua kali),
    // dan itu hanya mengubah isi tabel di layar, bukan f.transaksi, sehingga
    // tidak pernah sampai ke Supabase.
    final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Tanggal", "Jenis", "Kategori", "Nominal", "Keterangan"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table;

    public TransaksiPanel(MainFrame f) {
        this.f = f;
        setLayout(new BorderLayout(16, 16));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 35, 35, 35));

        JPanel top = new JPanel(new BorderLayout(12, 12));
        top.setOpaque(false);
        top.add(UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Transaksi</h1><font color='#C8D0FF'>Catat dan kelola arus uang usaha</font></html>"), BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton add = UI.button("Tambah Transaksi");
        JButton edit = UI.secondaryButton("Ubah");
        JButton delete = UI.secondaryButton("Hapus");
        JButton template = UI.secondaryButton("Unduh Template Excel");
        JButton imp = UI.secondaryButton("Impor Excel");
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(template);
        actions.add(imp);

        JTextField search = UI.input("");
        actions.add(UI.searchBar(search, "Cari keterangan atau kategori"));
        top.add(actions, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        for (Transaksi t : f.transaksi) row(t);

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.styleTable(table);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            void filter() {
                sorter.setRowFilter(search.getText().trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(search.getText()), 1, 2, 4));
            }
        });

        add(UI.scrollPane(table), BorderLayout.CENTER);
        add.addActionListener(e -> dialog(null, -1));
        edit.addActionListener(e -> editSelected());
        delete.addActionListener(e -> deleteSelected());
        template.addActionListener(e -> download());
        imp.addActionListener(e -> importExcel());
    }

    private Object[] rowData(Transaksi t) {
        return new Object[]{t.getTanggal(), t.getJenis(), t.getKategori(), UI.money(t.getNominal()), t.getKeterangan()};
    }

    private void row(Transaksi t) {
        model.addRow(rowData(t));
    }

    /**
     * Mengembalikan indeks di f.transaksi untuk baris yang dipilih, atau -1.
     * Tabel punya sorter/filter, jadi indeks tampilan HARUS diubah ke indeks model.
     * Urutan baris model sama dengan urutan f.transaksi.
     */
    private int selectedIndex() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih satu baris transaksi dulu.",
                "Belum ada yang dipilih", JOptionPane.INFORMATION_MESSAGE);
            return -1;
        }
        int index = table.convertRowIndexToModel(viewRow);
        if (index < 0 || index >= f.transaksi.size()) return -1;
        return index;
    }

    private void editSelected() {
        int index = selectedIndex();
        if (index < 0) return;
        Transaksi t = f.transaksi.get(index);
        Object produk = t.getProduk();
        if (produk != null && !produk.toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Transaksi hasil impor Excel berisi data produk dan HPP, jadi tidak bisa diubah di sini.\n"
                    + "Hapus baris ini lalu tambahkan transaksi baru.",
                "Tidak bisa diubah", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        dialog(t, index);
    }

    private void deleteSelected() {
        int index = selectedIndex();
        if (index < 0) return;
        Transaksi t = f.transaksi.get(index);
        int ok = JOptionPane.showConfirmDialog(this,
            "Hapus transaksi ini?\n" + t.getKategori() + "  -  " + UI.money(t.getNominal())
                + "\n" + t.getKeterangan(),
            "Konfirmasi hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        f.transaksi.remove(index);   // 1) data utama
        model.removeRow(index);      // 2) tampilan
        f.persistWorkspace(() ->     // 3) kirim ke Supabase
            JOptionPane.showMessageDialog(this, "Transaksi dihapus dan disinkronkan."));
    }

    /** existing == null berarti tambah baru; selain itu ubah data pada indeks tertentu. */
    private void dialog(Transaksi existing, int index) {
        boolean edit = existing != null;

        java.util.List<String> cats = new ArrayList<>(Arrays.asList("Penjualan", "Bahan baku", "Operasional", "Lainnya"));
        if (edit && existing.getKategori() != null && !cats.contains(existing.getKategori())) {
            cats.add(existing.getKategori());
        }

        JTextField nominal = new JTextField(edit ? plain(existing.getNominal()) : "");
        JTextField ket = new JTextField(edit && existing.getKeterangan() != null ? existing.getKeterangan() : "");
        JComboBox<String> jenis = new JComboBox<>(new String[]{"Pemasukan", "Pengeluaran"});
        JComboBox<String> kat = new JComboBox<>(cats.toArray(new String[0]));
        if (edit) {
            jenis.setSelectedItem(existing.getJenis());
            kat.setSelectedItem(existing.getKategori());
        }

        JPanel p = UI.formGrid();
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        UI.field(p, g, "Jenis", jenis, 0);
        UI.field(p, g, "Kategori", kat, 1);
        UI.field(p, g, "Nominal", nominal, 2);
        UI.field(p, g, "Keterangan", ket, 3);

        int ok = JOptionPane.showConfirmDialog(this, p, edit ? "Ubah transaksi" : "Tambah transaksi",
            JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            double value = parseNominal(nominal.getText());
            if (value <= 0) {
                JOptionPane.showMessageDialog(this, "Nominal harus lebih dari 0.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LocalDate tanggal = edit ? existing.getTanggal() : LocalDate.now();
            Transaksi t = new Transaksi(tanggal, (String) jenis.getSelectedItem(),
                (String) kat.getSelectedItem(), value, ket.getText());

            if (edit) {
                f.transaksi.set(index, t);                       // 1) data utama
                Object[] data = rowData(t);                      // 2) tampilan
                for (int c = 0; c < data.length; c++) model.setValueAt(data[c], index, c);
            } else {
                f.transaksi.add(t);
                row(t);
            }
            f.persistWorkspace(() ->                             // 3) kirim ke Supabase
                JOptionPane.showMessageDialog(this, edit
                    ? "Transaksi berhasil diubah dan disinkronkan."
                    : "Transaksi berhasil disimpan dan disinkronkan."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nominal harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Menerima "125000", "125.000", atau "Rp 125.000". Titik dianggap pemisah ribuan. */
    private static double parseNominal(String text) {
        String s = text.trim().replace("Rp", "").replace("rp", "").replace(" ", "")
            .replace(".", "").replace(",", ".");
        return Double.parseDouble(s);
    }

    /** 125000.0 -> "125000" (tanpa ".0" dan tanpa notasi ilmiah). */
    private static String plain(Number n) {
        return new BigDecimal(String.valueOf(n)).stripTrailingZeros().toPlainString();
    }

    private void download() {
        JFileChooser c = new JFileChooser();
        if (c.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                new ExcelService().buatTemplate(new File(c.getSelectedFile().getAbsolutePath() + ".xlsx"));
                JOptionPane.showMessageDialog(this, "Template berhasil dibuat.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importExcel() {
        JFileChooser c = new JFileChooser();
        if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String path = c.getSelectedFile().getCanonicalPath();
                if (f.importedFiles.contains(path)) {
                    JOptionPane.showMessageDialog(this, "File ini sudah pernah diimpor.", "Duplikasi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                java.util.Set<String> names = new java.util.HashSet<>();
                for (Produk p : f.produk) names.add(p.getNama());
                java.util.List<Transaksi> imported = new ExcelService().impor(c.getSelectedFile(), names);

                DefaultTableModel preview = new DefaultTableModel(new String[]{"Tanggal", "Produk", "Penjualan", "HPP", "Keterangan"}, 0);
                for (Transaksi t : imported) preview.addRow(new Object[]{t.getTanggal(), t.getProduk(), UI.money(t.getNominal()), UI.money(t.getHpp()), t.getKeterangan()});

                JTable previewTable = new JTable(preview);
                UI.styleTable(previewTable);
                previewTable.setPreferredScrollableViewportSize(new Dimension(620, 160));

                int ok = JOptionPane.showConfirmDialog(this, new JScrollPane(previewTable), "Preview data aktual (" + imported.size() + " baris)", JOptionPane.OK_CANCEL_OPTION);
                if (ok == JOptionPane.OK_OPTION) {
                    for (Transaksi t : imported) {
                        f.transaksi.add(t);
                        row(t);
                    }
                    f.importedFiles.add(path);
                    f.actualDataImported = true;
                    f.persistWorkspace(() -> {
                        JOptionPane.showMessageDialog(this, "Data aktual berhasil diimpor dan disinkronkan.");
                    });
                }
            } catch (IOException | RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Impor gagal: " + ex.getMessage(), "Validasi Excel", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}