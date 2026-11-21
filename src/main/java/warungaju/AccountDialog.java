package warungaju;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** Dialog akun UMKM: ubah nama, ubah kata sandi, dan hapus akun sendiri. */
public class AccountDialog extends JDialog {
    private interface Task {
        void run() throws java.io.IOException;
    }

    private final MainFrame owner;
    private final Runnable onProfileChanged;
    private final JTextField nameField = UI.input("");
    private final JPasswordField newPassword = new JPasswordField();
    private final JPasswordField confirmPassword = new JPasswordField();
    private final JButton saveNameButton = UI.button("Simpan Nama");
    private final JButton changePasswordButton = UI.button("Ubah Kata Sandi");
    private final JButton deleteButton = UI.secondaryButton("Hapus Akun Saya");

    public AccountDialog(MainFrame owner, Runnable onProfileChanged) {
        super(owner, "Akun Saya", true);
        this.owner = owner;
        this.onProfileChanged = onProfileChanged;
        nameField.setText(owner.user.fullName == null ? "" : owner.user.fullName);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));
        root.setBackground(UI.BG_DARK);
        JPanel form = new JPanel(new GridLayout(0, 1, 9, 9));
        form.setOpaque(false);
        form.add(label("Email: " + owner.user.email));
        form.add(label("Nama"));
        form.add(nameField);
        form.add(saveNameButton);
        form.add(label("Kata sandi baru (minimal 8 karakter)"));
        form.add(newPassword);
        form.add(label("Ulangi kata sandi baru"));
        form.add(confirmPassword);
        form.add(changePasswordButton);
        form.add(new JLabel());
        form.add(deleteButton);
        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
        setSize(480, 600);
        setMinimumSize(new java.awt.Dimension(440, 560));
        setLocationRelativeTo(owner);

        saveNameButton.addActionListener(event -> saveName());
        changePasswordButton.addActionListener(event -> changePassword());
        deleteButton.addActionListener(event -> deleteAccount());
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UI.TEXT_LIGHT);
        return label;
    }

    private void saveName() {
        final String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong.");
            return;
        }
        execute(() -> owner.supabase.updateOwnName(owner.session, name), () -> {
            owner.user.fullName = name;
            onProfileChanged.run();
            JOptionPane.showMessageDialog(this, "Nama berhasil diperbarui.");
        });
    }

    private void changePassword() {
        char[] first = newPassword.getPassword();
        char[] second = confirmPassword.getPassword();
        boolean valid = first.length >= 8 && Arrays.equals(first, second);
        final String password = new String(first);
        Arrays.fill(first, '\0');
        Arrays.fill(second, '\0');
        if (!valid) {
            JOptionPane.showMessageDialog(this,
                "Kata sandi minimal 8 karakter dan konfirmasi harus sama.");
            return;
        }
        execute(() -> owner.supabase.changePassword(owner.session, password), () -> {
            newPassword.setText("");
            confirmPassword.setText("");
            JOptionPane.showMessageDialog(this, "Kata sandi berhasil diubah.");
        });
    }

    private void deleteAccount() {
        int answer = JOptionPane.showConfirmDialog(this,
            "Hapus PERMANEN akun ini beserta seluruh data keuangan?\n"
                + "Tindakan ini tidak dapat dibatalkan.",
            "Hapus akun", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        execute(() -> owner.supabase.deleteOwnAccount(owner.session), () -> {
            owner.dispose();
            dispose();
            new AuthFrame().setVisible(true);
        });
    }

    private void execute(final Task task, final Runnable onSuccess) {
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    get();
                    onSuccess.run();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    JOptionPane.showMessageDialog(AccountDialog.this,
                        cause == null || cause.getMessage() == null
                            ? "Permintaan gagal." : cause.getMessage(),
                        "Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        saveNameButton.setEnabled(!busy);
        changePasswordButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
    }
}
