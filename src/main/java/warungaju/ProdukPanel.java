package warungaju;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ProdukPanel extends JPanel {

    private final MainFrame f;

    // Tabel tidak boleh diedit langsung; perubahan lewat tombol Edit
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Produk", "Biaya Produksi", "Jml Produksi", "Harga Jual", "Stok", "HPP", "Margin"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);


    public ProdukPanel(MainFrame f) {

        this.f = f;

        setLayout(new BorderLayout(16, 16));
        setBackground(UI.BG_DARK);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 35, 35, 35));


        // Bagian atas: judul + tombol aksi + kolom pencarian
        JPanel top = new JPanel(new BorderLayout(12, 12));
        top.setOpaque(false);

        top.add(
            UI.htmlLabel("<html><h1 style='color:#FFFFF0'>Produk dan HPP</h1><font color='#C8D0FF'>Kelola produk, biaya, dan margin keuntungan</font></html>"),
            BorderLayout.NORTH
        );

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton btnTambah = UI.button("Tambah Produk");
        JButton btnEdit = UI.secondaryButton("Edit");
        JButton btnHapus = UI.secondaryButton("Hapus");
        JButton btnTambahStok = UI.secondaryButton("Tambah Stok");
        JButton btnKurangiStok = UI.secondaryButton("Kurangi Stok");

        JTextField txtCari = UI.input("");

        actions.add(btnTambah);
        actions.add(btnEdit);
        actions.add(btnHapus);
        actions.add(btnTambahStok);
        actions.add(btnKurangiStok);
        actions.add(UI.searchBar(txtCari, "Cari nama produk"));

        top.add(actions, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);


        // Tabel + pencarian berdasarkan nama produk (kolom 0)
        UI.styleTable(table);

        final TableRowSorter<DefaultTableModel> sorter =
            new TableRowSorter<>(model);

        table.setRowSorter(sorter);

        txtCari.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }

            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }

            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {

                String kata = txtCari.getText().trim();

                if (kata.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(
                        RowFilter.regexFilter(
                            "(?i)" + java.util.regex.Pattern.quote(kata), 0
                        )
                    );
                }
            }
        });

        add(UI.scrollPane(table), BorderLayout.CENTER);

        muatUlang();


        // Event handling tombol
        btnTambah.addActionListener(e -> formProduk(null));
        btnEdit.addActionListener(e -> edit());
        btnHapus.addActionListener(e -> hapus());
        btnTambahStok.addActionListener(e -> ubahStok(true));
        btnKurangiStok.addActionListener(e -> ubahStok(false));
    }


    // ---------- Tabel ----------

    // Isi ulang tabel dari data di MainFrame (urutan baris tabel = urutan list)
    private void muatUlang() {

        model.setRowCount(0);

        for (Produk p : f.produk) {

            model.addRow(new Object[]{
                p.getNama(),
                UI.money(p.getBiayaProduksi()),
                p.getJumlahProduksi(),
                UI.money(p.getHargaJual()),
                p.getStok(),
                UI.money(p.getHpp()),
                String.format("%.1f%%", p.getMargin())
            });
        }
    }


    // Mengambil Produk dari baris yang sedang dipilih di tabel
    private Produk produkDipilih() {

        int baris = table.getSelectedRow();

        if (baris < 0) {
            throw new ValidasiException("Pilih produk di tabel terlebih dahulu.");
        }

        // Baris yang tampak bisa berbeda dari urutan data saat sedang dicari/diurutkan
        int indeks = table.convertRowIndexToModel(baris);

        return f.produk.get(indeks);
    }


    // ---------- Aksi tombol ----------

    private void edit() {

        try {
            formProduk(produkDipilih());
        } catch (ValidasiException ex) {
            peringatan(ex.getMessage());
        }
    }


    private void hapus() {

        try {

            Produk p = produkDipilih();

            int pilihan = JOptionPane.showConfirmDialog(
                this,
                "Hapus produk \"" + p.getNama() + "\"?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (pilihan == JOptionPane.YES_OPTION) {
                f.produk.remove(p);
                muatUlang();
                f.persistWorkspace();
            }

        } catch (ValidasiException ex) {
            peringatan(ex.getMessage());
        }
    }


    private void ubahStok(boolean tambah) {

        try {

            Produk p = produkDipilih();

            String aksi = tambah ? "ditambahkan" : "dikurangi";

            String input = JOptionPane.showInputDialog(
                this,
                "Jumlah stok yang akan " + aksi + " untuk " + p.getNama() +
                "\n(stok sekarang: " + p.getStok() + ")",
                tambah ? "Tambah Stok" : "Kurangi Stok",
                JOptionPane.QUESTION_MESSAGE
            );

            // Pengguna menekan Cancel
            if (input == null) {
                return;
            }

            int jumlah = parseBulat(input, "Jumlah stok");

            if (tambah) {
                p.tambahStok(jumlah);
            } else {
                p.kurangiStok(jumlah);
            }

            muatUlang();
            f.persistWorkspace();

        } catch (ValidasiException ex) {
            peringatan(ex.getMessage());
        }
    }


    // ---------- Form tambah / edit ----------

    // lama == null  -> tambah produk baru
    // lama != null  -> edit produk tersebut
    private void formProduk(Produk lama) {

        // Nilai awal form. Jika validasi gagal, form dibuka lagi
        // dengan isian terakhir agar pengguna tidak mengetik ulang.
        String nama = lama == null ? "" : lama.getNama();
        String biaya = lama == null ? "" : angka(lama.getBiayaProduksi());
        String jumlah = lama == null ? "1" : String.valueOf(lama.getJumlahProduksi());
        String harga = lama == null ? "" : angka(lama.getHargaJual());
        String stok = lama == null ? "" : String.valueOf(lama.getStok());

        String judul = lama == null ? "Tambah Produk" : "Edit Produk";

        while (true) {

            JTextField txtNama = UI.input(nama);
            JTextField txtBiaya = UI.input(biaya);
            JTextField txtJumlah = UI.input(jumlah);
            JTextField txtHarga = UI.input(harga);
            JTextField txtStok = UI.input(stok);

            JPanel panel = UI.formGrid();
            GridBagConstraints g = new GridBagConstraints();

            UI.field(panel, g, "Nama produk", txtNama, 0);
            UI.field(panel, g, "Total biaya produksi", txtBiaya, 1);
            UI.field(panel, g, "Jumlah diproduksi", txtJumlah, 2);
            UI.field(panel, g, "Harga jual / unit", txtHarga, 3);
            UI.field(panel, g, "Stok", txtStok, 4);

            int pilihan = JOptionPane.showConfirmDialog(
                this,
                panel,
                judul,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (pilihan != JOptionPane.OK_OPTION) {
                return;
            }

            nama = txtNama.getText();
            biaya = txtBiaya.getText();
            jumlah = txtJumlah.getText();
            harga = txtHarga.getText();
            stok = txtStok.getText();

            try {

                simpan(lama, nama, biaya, jumlah, harga, stok);
                return;

            } catch (ValidasiException ex) {

                peringatan(ex.getMessage());
            }
        }
    }


    private void simpan(Produk lama, String nama, String biaya,
                        String jumlah, String harga, String stok) {

        // 1. Ubah teks menjadi angka (error jika bukan angka)
        double biayaProduksi = parseUang(biaya, "Biaya produksi");
        int jumlahProduksi = parseBulat(jumlah, "Jumlah produksi");
        double hargaJual = parseUang(harga, "Harga jual");
        int stokProduk = parseBulat(stok, "Stok");

        // 2. Buat object sementara: constructor Produk memvalidasi semua nilai.
        //    Jika ada yang salah, ValidasiException dilempar dan data lama tidak berubah.
        Produk baru = new Produk(
            nama, biayaProduksi, hargaJual, stokProduk, jumlahProduksi
        );

        // 3. Nama produk tidak boleh kembar
        periksaDuplikat(baru.getNama(), lama);

        // 4. Simpan
        if (lama == null) {

            f.produk.add(baru);

        } else {

            lama.setNama(baru.getNama());
            lama.setBiayaProduksi(baru.getBiayaProduksi());
            lama.setJumlahProduksi(baru.getJumlahProduksi());
            lama.setHargaJual(baru.getHargaJual());
            lama.setStok(baru.getStok());
        }

        muatUlang();
        f.persistWorkspace();
    }


    // Nama dibandingkan tanpa membedakan huruf besar/kecil.
    // Produk yang sedang diedit (kecuali) tidak dianggap duplikat dirinya sendiri.
    private void periksaDuplikat(String nama, Produk kecuali) {

        for (Produk p : f.produk) {

            if (p != kecuali && p.getNama().equalsIgnoreCase(nama)) {

                throw new ValidasiException(
                    "Produk \"" + p.getNama() + "\" sudah ada."
                );
            }
        }
    }


    // ---------- Pembantu ----------

    private double parseUang(String teks, String label) {

        String bersih = teks.trim();

        if (bersih.isEmpty()) {
            throw new ValidasiException(label + " tidak boleh kosong.");
        }

        // "9.000" ditulis gaya Indonesia (pemisah ribuan) = sembilan ribu
        if (bersih.matches("\\d{1,3}(\\.\\d{3})+")) {
            bersih = bersih.replace(".", "");
        }

        // Hanya angka (boleh diawali minus dan memakai titik desimal)
        if (!bersih.matches("-?\\d+(\\.\\d+)?")) {
            throw new ValidasiException(
                label + " harus berupa angka (tanpa huruf atau simbol)."
            );
        }

        return Double.parseDouble(bersih);
    }


    private int parseBulat(String teks, String label) {

        String bersih = teks.trim();

        if (bersih.isEmpty()) {
            throw new ValidasiException(label + " tidak boleh kosong.");
        }

        try {

            return Integer.parseInt(bersih);

        } catch (NumberFormatException ex) {

            throw new ValidasiException(
                label + " harus berupa bilangan bulat."
            );
        }
    }


    // Tampilkan angka tanpa ".0" di belakang (9000.0 -> 9000)
    private String angka(double nilai) {

        if (nilai == Math.floor(nilai)) {
            return String.valueOf((long) nilai);
        }

        return String.valueOf(nilai);
    }


    private void peringatan(String pesan) {

        JOptionPane.showMessageDialog(
            this,
            pesan,
            "Validasi",
            JOptionPane.WARNING_MESSAGE
        );
    }
}