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
    private static final String EMAIL_PATTERN =
        "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$";
    private final SupabaseClient client = new SupabaseClient();
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final JTextField loginEmail = UI.input("");
    private final JPasswordField loginPassword = new JPasswordField();
    private final JTextField resetPasswordEmail = UI.input("");
    private final JTextField resetPasswordCode = UI.input("");
    private final JPasswordField resetPasswordNew = new JPasswordField();
    private final JPasswordField resetPasswordConfirm = new JPasswordField();
    private final JTextField verificationCode = UI.input("");
    private final JTextField registerName = UI.input("");
    private final JTextField registerEmail = UI.input("");
    private final JPasswordField registerPassword = new JPasswordField();
    private final JPasswordField confirmPassword = new JPasswordField();
    private final JButton loginButton = UI.button("Masuk");
    private final JButton registerButton = UI.button("Buat Akun UMKM");
    private final JButton showLoginButton = UI.secondaryButton("Masuk");
    private final JButton showRegisterButton = UI.secondaryButton("Daftar");
    private final JButton forgotPasswordButton = UI.secondaryButton("Lupa kata sandi?");
    private final JButton sendPasswordResetButton = UI.button("Kirim Kode Reset");
    private final JButton completePasswordResetButton =
        UI.button("Verifikasi Kode dan Ubah Kata Sandi");
    private final JButton cancelPasswordResetButton = UI.secondaryButton("Kembali ke Masuk");
    private final JButton verifySignupButton = UI.button("Verifikasi Email");
    private final JButton resendVerificationButton = UI.secondaryButton("Kirim ulang email");
    private final JButton returnToLoginButton = UI.button("Kembali ke masuk");
    private final JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
    private final JLabel verificationMessage = new JLabel("", JLabel.CENTER);
    private final JLabel resetPasswordMessage = new JLabel("", JLabel.CENTER);
    private String pendingVerificationEmail;
    private JPanel resetPasswordFields;

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
            + "<font color='#C8D0FF'>Catat hari ini, rencanakan langkah berikutnya.</font></html>");
        title.setFont(new Font(UI.FONT, Font.PLAIN, 15));
        root.add(title, BorderLayout.NORTH);

        loginEmail.putClientProperty("JTextField.placeholderText", "nama@usaha.id");
        resetPasswordEmail.putClientProperty("JTextField.placeholderText", "nama@usaha.id");
        resetPasswordCode.putClientProperty("JTextField.placeholderText", "Kode 6 angka");
        verificationCode.putClientProperty("JTextField.placeholderText", "Kode 6 angka");
        registerEmail.putClientProperty("JTextField.placeholderText", "nama@usaha.id");
        registerName.putClientProperty("JTextField.placeholderText", "Nama pemilik atau usaha");
        cardPanel.setOpaque(false);
        cardPanel.add(loginPanel(), "login");
        cardPanel.add(passwordResetPanel(), "password-reset");
        cardPanel.add(registerPanel(), "register");
        cardPanel.add(verificationPanel(), "verification");
        root.add(cardPanel, BorderLayout.CENTER);

        tabs.setOpaque(false);
        tabs.add(showLoginButton);
        tabs.add(showRegisterButton);
        root.add(tabs, BorderLayout.SOUTH);
        setContentPane(root);

        loginButton.addActionListener(event -> signIn());
        registerButton.addActionListener(event -> signUp());
        forgotPasswordButton.addActionListener(event -> showPasswordReset());
        sendPasswordResetButton.addActionListener(event -> sendPasswordReset());
        completePasswordResetButton.addActionListener(event -> completePasswordReset());
        cancelPasswordResetButton.addActionListener(event -> showLogin());
        verifySignupButton.addActionListener(event -> verifySignup());
        resendVerificationButton.addActionListener(event -> resendVerification());
        returnToLoginButton.addActionListener(event -> showLogin());
        showLoginButton.addActionListener(event -> showLogin());
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
        JPanel actions = new JPanel(new GridLayout(2, 1, 0, 8));
        actions.setOpaque(false);
        actions.add(loginButton);
        actions.add(forgotPasswordButton);
        form.add(actions);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel verificationPanel() {
        JPanel panel = UI.glassPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(28, 24, 28, 24));
        JPanel content = new JPanel(new GridLayout(0, 1, 10, 10));
        content.setOpaque(false);
        content.add(label("Periksa kotak masuk Anda"));
        verificationMessage.setForeground(UI.TEXT_MUTED);
        content.add(verificationMessage);
        content.add(label("Masukkan kode 6 angka dari email verifikasi."));
        content.add(verificationCode);
        content.add(verifySignupButton);
        content.add(resendVerificationButton);
        content.add(returnToLoginButton);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel passwordResetPanel() {
        JPanel panel = UI.glassPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        resetPasswordMessage.setForeground(UI.TEXT_MUTED);
        form.add(label("Atur ulang kata sandi"));
        form.add(label("Masukkan email akun. Kami akan mengirimkan tautan untuk "
            + "membuat kata sandi baru."));
        form.add(label("Email"));
        form.add(resetPasswordEmail);
        form.add(sendPasswordResetButton);
        resetPasswordFields = new JPanel(new GridLayout(0, 1, 8, 8));
        resetPasswordFields.setOpaque(false);
        resetPasswordFields.add(label("Kode 6 angka dari email"));
        resetPasswordFields.add(resetPasswordCode);
        resetPasswordFields.add(label("Kata sandi baru (8+ karakter, huruf besar-kecil, angka, simbol)"));
        resetPasswordFields.add(resetPasswordNew);
        resetPasswordFields.add(label("Ulangi kata sandi baru"));
        resetPasswordFields.add(resetPasswordConfirm);
        resetPasswordFields.add(completePasswordResetButton);
        resetPasswordFields.setVisible(false);
        form.add(resetPasswordFields);
        form.add(resetPasswordMessage);
        form.add(cancelPasswordResetButton);
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
        form.add(label("Kata sandi (8+ karakter, huruf besar-kecil, angka, simbol)"));
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
        if (!isValidEmail(email) || enteredPassword.length == 0) {
            java.util.Arrays.fill(enteredPassword, '\0');
            JOptionPane.showMessageDialog(this, "Masukkan format email yang valid dan kata sandi.");
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
        if (name.isEmpty() || !isValidEmail(email) || !isStrongPassword(enteredPassword)
                || !matches) {
            java.util.Arrays.fill(enteredPassword, '\0');
            JOptionPane.showMessageDialog(this,
                "Periksa nama dan format email. Kata sandi harus memiliki minimal 8 karakter, "
                    + "huruf besar, huruf kecil, angka, dan simbol; konfirmasi juga harus sama.");
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
                    if (session != null && session.emailVerified) {
                        loginEmail.setText(email);
                        showLogin();
                        JOptionPane.showMessageDialog(AuthFrame.this,
                            "Akun sudah dikonfirmasi oleh Supabase. Silakan masuk.");
                    } else {
                        showVerification(email);
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

    private void sendPasswordReset() {
        final String email = resetPasswordEmail.getText().trim();
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Masukkan format email akun yang valid.",
                "Format email", JOptionPane.WARNING_MESSAGE);
            return;
        }
        resetPasswordMessage.setText("");
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                client.sendPasswordReset(email);
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    get();
                    resetPasswordFields.setVisible(true);
                    resetPasswordMessage.setText("<html><div style='text-align:center'>"
                        + "Jika email terdaftar, kode 6 angka akan dikirim. Periksa juga "
                        + "folder spam.</div></html>");
                    pack();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    private void completePasswordReset() {
        final String email = resetPasswordEmail.getText().trim();
        final String token = resetPasswordCode.getText().trim();
        char[] enteredPassword = resetPasswordNew.getPassword();
        char[] repeatedPassword = resetPasswordConfirm.getPassword();
        boolean matches = java.util.Arrays.equals(enteredPassword, repeatedPassword);
        java.util.Arrays.fill(repeatedPassword, '\0');
        if (!isValidEmail(email) || !isSixDigitCode(token)
                || !isStrongPassword(enteredPassword) || !matches) {
            java.util.Arrays.fill(enteredPassword, '\0');
            JOptionPane.showMessageDialog(this,
                "Masukkan email valid, kode 6 angka yang benar, dan kata sandi kuat "
                    + "yang sama pada kedua kolom.");
            return;
        }
        final String newPassword = new String(enteredPassword);
        java.util.Arrays.fill(enteredPassword, '\0');
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                AuthSession session = client.verifyEmailOtp(email, token, "recovery");
                client.changePassword(session, newPassword);
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    get();
                    resetPasswordCode.setText("");
                    resetPasswordNew.setText("");
                    resetPasswordConfirm.setText("");
                    resetPasswordFields.setVisible(false);
                    resetPasswordMessage.setText("Kata sandi berhasil diubah. Silakan masuk.");
                    showLogin();
                    JOptionPane.showMessageDialog(AuthFrame.this,
                        "Kata sandi berhasil diubah. Silakan masuk dengan kata sandi baru.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    private void verifySignup() {
        final String token = verificationCode.getText().trim();
        if (pendingVerificationEmail == null || !isSixDigitCode(token)) {
            JOptionPane.showMessageDialog(this, "Masukkan kode verifikasi 6 angka dari email.");
            return;
        }
        setBusy(true);
        new SwingWorker<AuthSession, Void>() {
            @Override
            protected AuthSession doInBackground() throws Exception {
                return client.verifyEmailOtp(pendingVerificationEmail, token, "signup");
            }

            @Override
            protected void done() {
                setBusy(false);
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

    private void resendVerification() {
        if (pendingVerificationEmail == null) return;
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                client.resendSignupVerification(pendingVerificationEmail);
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AuthFrame.this,
                        "Jika akun menunggu verifikasi, email baru akan segera dikirim.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    private void showVerification(String email) {
        pendingVerificationEmail = email;
        verificationCode.setText("");
        verificationMessage.setText("<html><div style='text-align:center'>Email verifikasi "
            + "berisi kode 6 angka telah diminta untuk <b>" + email + "</b>.</div></html>");
        cards.show(cardPanel, "verification");
        tabs.setVisible(false);
    }

    private void showLogin() {
        cards.show(cardPanel, "login");
        tabs.setVisible(true);
    }

    private void showPasswordReset() {
        resetPasswordEmail.setText("");
        resetPasswordCode.setText("");
        resetPasswordNew.setText("");
        resetPasswordConfirm.setText("");
        resetPasswordFields.setVisible(false);
        resetPasswordMessage.setText("");
        cards.show(cardPanel, "password-reset");
        tabs.setVisible(false);
    }

    private static boolean isValidEmail(String email) {
        if (email == null || email.length() > 254 || !email.matches(EMAIL_PATTERN)
                || email.indexOf("..") >= 0) {
            return false;
        }
        int at = email.indexOf('@');
        return at > 0 && email.charAt(0) != '.' && email.charAt(at - 1) != '.';
    }

    private static boolean isStrongPassword(char[] password) {
        if (password.length < 8) return false;
        boolean uppercase = false;
        boolean lowercase = false;
        boolean digit = false;
        boolean symbol = false;
        for (char character : password) {
            uppercase |= Character.isUpperCase(character);
            lowercase |= Character.isLowerCase(character);
            digit |= Character.isDigit(character);
            symbol |= !Character.isLetterOrDigit(character) && !Character.isWhitespace(character);
        }
        return uppercase && lowercase && digit && symbol;
    }

    private static boolean isSixDigitCode(String code) {
        return code != null && code.matches("[0-9]{6}");
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
        forgotPasswordButton.setEnabled(!busy);
        sendPasswordResetButton.setEnabled(!busy);
        completePasswordResetButton.setEnabled(!busy);
        cancelPasswordResetButton.setEnabled(!busy);
        verifySignupButton.setEnabled(!busy);
        resendVerificationButton.setEnabled(!busy);
        returnToLoginButton.setEnabled(!busy);
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
