package warungaju;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

public class AdminFrame extends JFrame {
    private final SupabaseClient client;
    private final AuthSession session;
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Email", "Nama", "Role", "Status", "Terakhir aktif",
            "Dibuat", "Diverifikasi"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    private final JButton refreshButton = UI.button("Muat Ulang");
    private final JButton changePasswordButton = UI.secondaryButton("Ubah Kata Sandi");
    private final JButton toggleButton = UI.secondaryButton("Aktifkan / Nonaktifkan");
    private final JButton deleteButton = UI.secondaryButton("Hapus Akun");
    private JTable table;
    private boolean actionsEnabled = true;

    public AdminFrame(SupabaseClient client, AuthSession session, AppUser user) {
        this.client = client;
        this.session = session;
        setTitle("WARUNGAJU - Super Admin");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        root.setBackground(UI.BG_DARK);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = UI.htmlLabel("<html><h1 style='color:#DFFF00'>Panel Super Admin</h1>"
            + "<font color='#C8D0FF'>Semua akun terdaftar di WARUNGAJU</font></html>");
        title.setFont(new Font(UI.FONT, Font.PLAIN, 14));
        header.add(title, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(new JLabel(user.email));
        JButton logout = UI.secondaryButton("Keluar");
        actions.add(changePasswordButton);
        actions.add(toggleButton);
        actions.add(deleteButton);
        actions.add(refreshButton);
        actions.add(logout);
        header.add(actions, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        table = new JTable(model);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> updateSelectionActions());
        UI.styleTable(table);
        root.add(UI.scrollPane(table), BorderLayout.CENTER);
        setContentPane(root);

        refreshButton.addActionListener(event -> loadUsers());
        changePasswordButton.addActionListener(event -> changePassword());
        toggleButton.addActionListener(event -> toggleActive());
        deleteButton.addActionListener(event -> deleteSelectedUser());
        logout.addActionListener(event -> logout());
        loadUsers();
    }

    private void loadUsers() {
        setActionsEnabled(false);
        model.setRowCount(0);
        model.addRow(new Object[]{"Memuat data pengguna...", "", "", "", ""});
        new SwingWorker<List<AppUser>, Void>() {
            @Override
            protected List<AppUser> doInBackground() throws Exception {
                return client.getAllProfiles(session);
            }

            @Override
            protected void done() {
                try {
                    List<AppUser> users = get();
                    model.setRowCount(0);
                    for (AppUser user : users) {
                        model.addRow(new Object[]{user.id, user.email, user.fullName, user.role,
                            user.active ? "Aktif" : "Nonaktif", user.lastActiveAt,
                            user.createdAt, user.verifiedAt});
                    }
                    if (users.isEmpty()) {
                        model.addRow(new Object[]{"Belum ada pengguna.", "", "", "", "", "", "", ""});
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                } finally {
                    setActionsEnabled(true);
                }
            }
        }.execute();
    }

    private interface IoAction {
        void run() throws java.io.IOException;
    }

    private void setActionsEnabled(boolean enabled) {
        actionsEnabled = enabled;
        refreshButton.setEnabled(enabled);
        changePasswordButton.setEnabled(enabled);
        updateSelectionActions();
    }

    private void updateSelectionActions() {
        if (table == null) return;
        int row = table.getSelectedRow();
        boolean validSelection = row >= 0;
        boolean superAdmin = validSelection
            && "super_admin".equals(model.getValueAt(table.convertRowIndexToModel(row), 3));
        toggleButton.setEnabled(actionsEnabled && validSelection && !superAdmin);
        deleteButton.setEnabled(actionsEnabled && validSelection && !superAdmin);
    }

    /** Mengembalikan {id, email, role, status} baris terpilih, atau null bila tidak valid. */
    private String[] selectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih satu akun pada tabel terlebih dahulu.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = String.valueOf(model.getValueAt(modelRow, 0));
        try {
            java.util.UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Baris yang dipilih bukan data akun.");
            return null;
        }
        return new String[]{id, String.valueOf(model.getValueAt(modelRow, 1)),
            String.valueOf(model.getValueAt(modelRow, 3)),
            String.valueOf(model.getValueAt(modelRow, 4))};
    }

    private void toggleActive() {
        final String[] selected = selectedUser();
        if (selected == null) return;
        if ("super_admin".equals(selected[2])) {
            JOptionPane.showMessageDialog(this, "Akun Super Admin tidak dapat diubah dari panel ini.");
            return;
        }
        final boolean activate = "Nonaktif".equals(selected[3]);
        int answer = JOptionPane.showConfirmDialog(this,
            (activate ? "Aktifkan kembali akun " : "Nonaktifkan akun ") + selected[1] + "?",
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        runAction(() -> client.setUserActive(session, selected[0], activate));
    }

    private void deleteSelectedUser() {
        final String[] selected = selectedUser();
        if (selected == null) return;
        if ("super_admin".equals(selected[2])) {
            JOptionPane.showMessageDialog(this, "Akun Super Admin tidak dapat dihapus dari panel ini.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
            "Hapus PERMANEN akun " + selected[1] + " beserta seluruh data keuangannya?\n"
                + "Tindakan ini tidak dapat dibatalkan.",
            "Hapus akun", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        runAction(() -> client.deleteUser(session, selected[0]));
    }

    private void changePassword() {
        final JPasswordField newPasswordField = new JPasswordField();
        final JPasswordField confirmPasswordField = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Kata sandi baru (minimal 8 karakter)"));
        form.add(newPasswordField);
        form.add(new JLabel("Ulangi kata sandi baru"));
        form.add(confirmPasswordField);
        int choice = JOptionPane.showConfirmDialog(this, form, "Ubah Kata Sandi",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        char[] entered = newPasswordField.getPassword();
        char[] repeated = confirmPasswordField.getPassword();
        boolean valid = entered.length >= 8 && java.util.Arrays.equals(entered, repeated);
        java.util.Arrays.fill(repeated, '\0');
        if (!valid) {
            java.util.Arrays.fill(entered, '\0');
            JOptionPane.showMessageDialog(this,
                "Kata sandi minimal 8 karakter dan konfirmasi harus sama.");
            return;
        }
        final String password = new String(entered);
        java.util.Arrays.fill(entered, '\0');

        setActionsEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                client.changePassword(session, password);
                return null;
            }

            @Override
            protected void done() {
                setActionsEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(AdminFrame.this,
                        "Kata sandi Super Admin berhasil diubah.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showActionError(ex);
                } catch (ExecutionException ex) {
                    showActionError(ex.getCause());
                }
            }
        }.execute();
    }

    private void showActionError(Throwable error) {
        JOptionPane.showMessageDialog(this,
            error == null || error.getMessage() == null
                ? "Aksi gagal." : error.getMessage(),
            "Aksi gagal", JOptionPane.ERROR_MESSAGE);
    }

    private void runAction(final IoAction action) {
        setActionsEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                action.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    JOptionPane.showMessageDialog(AdminFrame.this,
                        cause == null || cause.getMessage() == null
                            ? "Aksi gagal." : cause.getMessage(),
                        "Aksi gagal", JOptionPane.ERROR_MESSAGE);
                }
                loadUsers();
            }
        }.execute();
    }

    private void logout() {
        try {
            client.signOut(session);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Sesi lokal ditutup, tetapi Supabase tidak dapat mengakhiri sesi: "
                    + ex.getMessage(),
                "Keluar", JOptionPane.WARNING_MESSAGE);
        }
        dispose();
        new AuthFrame().setVisible(true);
    }

    private void showError(Throwable error) {
        model.setRowCount(0);
        JOptionPane.showMessageDialog(this,
            error == null || error.getMessage() == null
                ? "Gagal memuat pengguna." : error.getMessage(),
            "Gagal memuat pengguna", JOptionPane.ERROR_MESSAGE);
    }
}
