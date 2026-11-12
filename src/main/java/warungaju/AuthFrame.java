package warungaju;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

public class AuthFrame extends JFrame {
    private final SupabaseClient client = new SupabaseClient();
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final JTextField loginEmail = new JTextField();
    private final JPasswordField loginPassword = new JPasswordField();
    private final JTextField registerName = new JTextField();
    private final JTextField registerEmail = new JTextField();
    private final JPasswordField registerPassword = new JPasswordField();
    private final JPasswordField confirmPassword = new JPasswordField();
    private final JButton loginButton = UI.button("Masuk");
    private final JButton registerButton = UI.button("Buat Akun UMKM");
    private final JButton showLoginButton = UI.secondaryButton("Masuk");
    private final JButton showRegisterButton = UI.secondaryButton("Daftar");

    public AuthFrame() {
        setTitle("WARUNGAJU - Masuk");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(560, 590);
        setMinimumSize(new Dimension(500, 540));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBorder(BorderFactory.createEmptyBorder(28, 38, 30, 38));
        root.setBackground(UI.BG_DARK);
        JLabel title = UI.htmlLabel("<html><h1 style='color:#DFFF00'>WARUNGAJU</h1>"
            + "<font color='#C8D0FF'>Ruang kerja keuangan UMKM berbasis cloud</font></html>");
        title.setFont(new Font("SansSerif", Font.PLAIN, 15));
        root.add(title, BorderLayout.NORTH);

        cardPanel.setOpaque(false);
        cardPanel.add(loginPanel(), "login");
        cardPanel.add(registerPanel(), "register");
        root.add(cardPanel, BorderLayout.CENTER);

        JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
        tabs.setOpaque(false);
        tabs.add(showLoginButton);
        tabs.add(showRegisterButton);
        root.add(tabs, BorderLayout.SOUTH);
        setContentPane(root);

        loginButton.addActionListener(event -> signIn());
        registerButton.addActionListener(event -> signUp());
        showLoginButton.addActionListener(event -> cards.show(cardPanel, "login"));
        showRegisterButton.addActionListener(event -> cards.show(cardPanel, "register"));
    }

    private JPanel loginPanel() {
        JPanel panel = UI.glassPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(label("Masuk ke akun"));
        form.add(label("Email"));
        form.add(loginEmail);
        form.add(label("Kata sandi"));
        form.add(loginPassword);
        form.add(new JLabel());
        form.add(loginButton);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel registerPanel() {
        JPanel panel = UI.glassPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.add(label("Daftar akun UMKM"));
        form.add(label("Nama"));
        form.add(registerName);
        form.add(label("Email"));
        form.add(registerEmail);
        form.add(label("Kata sandi (minimal 8 karakter)"));
        form.add(registerPassword);
        form.add(label("Ulangi kata sandi"));
        form.add(confirmPassword);
        form.add(registerButton);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UI.TEXT_LIGHT);
        return label;
    }

    private void signIn() {
        final String email = loginEmail.getText().trim();
        final char[] enteredPassword = loginPassword.getPassword();
        if (email.isEmpty() || enteredPassword.length == 0) {
            JOptionPane.showMessageDialog(this, "Isi email dan kata sandi.");
            return;
        }
        setBusy(true);
        new SwingWorker<AuthSession, Void>() {
            @Override
            protected AuthSession doInBackground() throws Exception {
                return client.signIn(email, new String(enteredPassword));
            }

            @Override
            protected void done() {
                java.util.Arrays.fill(enteredPassword, '\0');
                try {
                    openAccount(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    private void signUp() {
        final String name = registerName.getText().trim();
        final String email = registerEmail.getText().trim();
        final char[] enteredPassword = registerPassword.getPassword();
        char[] repeatedPassword = confirmPassword.getPassword();
        boolean matches = java.util.Arrays.equals(enteredPassword, repeatedPassword);
        java.util.Arrays.fill(repeatedPassword, '\0');
        if (name.isEmpty() || email.isEmpty() || enteredPassword.length < 8 || !matches) {
            java.util.Arrays.fill(enteredPassword, '\0');
            JOptionPane.showMessageDialog(this,
                "Lengkapi nama dan email, gunakan kata sandi minimal 8 karakter, "
                    + "dan pastikan konfirmasi kata sandi sama.");
            return;
        }
        setBusy(true);
        new SwingWorker<AuthSession, Void>() {
            @Override
            protected AuthSession doInBackground() throws Exception {
                return client.signUp(name, email, new String(enteredPassword));
            }

            @Override
            protected void done() {
                java.util.Arrays.fill(enteredPassword, '\0');
                setBusy(false);
                try {
                    AuthSession session = get();
                    if (session == null) {
                        cards.show(cardPanel, "login");
                        loginEmail.setText(email);
                        JOptionPane.showMessageDialog(AuthFrame.this,
                            "Akun berhasil dibuat. Verifikasi email dari Supabase, lalu masuk.");
                    } else {
                        openAccount(session);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    private void openAccount(final AuthSession session) {
        setBusy(true);
        new SwingWorker<AccountData, Void>() {
            @Override
            protected AccountData doInBackground() throws Exception {
                AccountData account = new AccountData();
                account.user = client.getProfile(session);
                if (!account.user.active) {
                    throw new java.io.IOException("Akun ini sedang dinonaktifkan.");
                }
                if ("umkm".equals(account.user.role)) {
                    account.workspace = client.loadWorkspace(session);
                } else if (!"super_admin".equals(account.user.role)) {
                    throw new java.io.IOException("Role akun tidak dikenali.");
                }
                return account;
            }

            @Override
            protected void done() {
                try {
                    AccountData account = get();
                    JFrame next = "super_admin".equals(account.user.role)
                        ? new AdminFrame(client, session, account.user)
                        : new MainFrame(client, session, account.user, account.workspace);
                    next.setVisible(true);
                    dispose();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    setBusy(false);
                    showError(ex);
                } catch (ExecutionException ex) {
                    setBusy(false);
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        loginButton.setEnabled(!busy);
        registerButton.setEnabled(!busy);
        showLoginButton.setEnabled(!busy);
        showRegisterButton.setEnabled(!busy);
        setCursor(busy ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
            : java.awt.Cursor.getDefaultCursor());
    }

    private void showError(Throwable error) {
        setBusy(false);
        String message = error == null || error.getMessage() == null
            ? "Permintaan gagal." : error.getMessage();
        JOptionPane.showMessageDialog(this, message, "Koneksi / autentikasi gagal",
            JOptionPane.ERROR_MESSAGE);
    }

    private static class AccountData {
        AppUser user;
        WorkspaceData workspace;
    }
}
