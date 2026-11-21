package warungaju;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AuthFrame extends JFrame {
    private static final String EMAIL_PATTERN =
        "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$";
    private final SupabaseClient client = new SupabaseClient();
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards) {
        @Override
        public Dimension getPreferredSize() {
            for (Component card : getComponents()) {
                if (card.isVisible()) {
                    Dimension preferred = card.getPreferredSize();
                    return new Dimension(Math.min(440, preferred.width), preferred.height);
                }
            }
            return new Dimension(440, 300);
        }
    };
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
    private final JLabel passwordStrengthLabel = new JLabel(
        "Kekuatan kata sandi: Belum diisi");
    private final JProgressBar passwordStrengthBar = new JProgressBar(0, 5);
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
    private final JButton returnToLoginButton = UI.button("Kembali ke Masuk");
    private final JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
    private final JLabel verificationMessage = new JLabel("", JLabel.CENTER);
    private final JLabel resetPasswordMessage = new JLabel("", JLabel.CENTER);
    private String pendingVerificationEmail;
    private JPanel resetPasswordFields;
    private JPanel passwordResetForm;

    public AuthFrame() {
        setTitle("Warung Aju - Masuk");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(660, 680);
        setMinimumSize(new Dimension(580, 620));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(32, 42, 34, 42));
        root.setBackground(UI.BG_DARK);
        JLabel title = UI.htmlLabel("<html><div style='font-size:26px;font-weight:bold;"
            + "color:#DFFF00'>Warung Aju</div><font color='#C8D0FF'>Keuangan usaha, "
            + "lebih terarah.</font></html>");
        title.setFont(new Font(UI.FONT, Font.PLAIN, 14));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginEmail.putClientProperty("JTextField.placeholderText", "nama@usaha.id");
        loginEmail.setPreferredSize(new Dimension(320, 42));
        loginPassword.setPreferredSize(new Dimension(320, 42));
        resetPasswordEmail.putClientProperty("JTextField.placeholderText", "nama@usaha.id");
        resetPasswordCode.putClientProperty("JTextField.placeholderText", "Kode 6 angka");
        verificationCode.putClientProperty("JTextField.placeholderText", "Kode 6 angka");
        registerEmail.putClientProperty("JTextField.placeholderText", "nama@usaha.id");
        registerName.putClientProperty("JTextField.placeholderText", "Nama pemilik atau usaha");
        registerEmail.setPreferredSize(new Dimension(320, 42));
        registerPassword.setPreferredSize(new Dimension(320, 42));
        cardPanel.setOpaque(false);
        cardPanel.add(loginPanel(), "login");
        cardPanel.add(passwordResetPanel(), "password-reset");
        cardPanel.add(registerPanel(), "register");
        cardPanel.add(verificationPanel(), "verification");
        cards.show(cardPanel, "login");
        cardPanel.setMaximumSize(new Dimension(440, Integer.MAX_VALUE));
        cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        tabs.setOpaque(false);
        tabs.add(showLoginButton);
        tabs.add(showRegisterButton);
        tabs.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel content = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension preferred = super.getPreferredSize();
                preferred.width = 440;
                return preferred;
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setMaximumSize(new Dimension(440, Integer.MAX_VALUE));
        content.add(title);
        content.add(Box.createVerticalStrut(22));
        content.add(cardPanel);
        content.add(Box.createVerticalStrut(16));
        content.add(tabs);
        root.add(content);
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
        showRegisterButton.addActionListener(event -> showCard("register"));
        registerPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updatePasswordStrength();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updatePasswordStrength();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updatePasswordStrength();
            }
        });
    }

    private JPanel loginPanel() {
        JPanel panel = UI.glassPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 32, 30, 32));
        JPanel form = verticalForm();
        addFormItem(form, label("Masuk ke akun"), 8);
        addFormItem(form, label("Email"), 8);
        addFormItem(form, loginEmail, 8);
        addFormItem(form, label("Kata sandi"), 8);
        addFormItem(form, loginPassword, 8);
        JPanel actions = new JPanel(new GridLayout(2, 1, 0, 8));
        actions.setOpaque(false);
        actions.add(loginButton);
        actions.add(forgotPasswordButton);
        addFormItem(form, actions, 8);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel verificationPanel() {
        JPanel panel = UI.glassPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 32, 30, 32));
        JPanel content = verticalForm();
        addFormItem(content, label("Periksa kotak masuk Anda"), 8);
        verificationMessage.setForeground(UI.TEXT_MUTED);
        addFormItem(content, verificationMessage, 8);
        addFormItem(content, label("Masukkan kode 6 angka dari email verifikasi."), 8);
        addFormItem(content, verificationCode, 8);
        addFormItem(content, verificationActions(), 8);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel verificationActions() {
        JPanel actions = verticalForm();
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        verifySignupButton.setMaximumSize(new Dimension(Integer.MAX_VALUE,
            verifySignupButton.getPreferredSize().height));
        actions.add(verifySignupButton);
        actions.add(Box.createVerticalStrut(8));

        JPanel secondaryActions = new JPanel(new GridLayout(1, 2, 8, 0));
        secondaryActions.setOpaque(false);
        secondaryActions.add(resendVerificationButton);
        secondaryActions.add(returnToLoginButton);
        secondaryActions.setMaximumSize(new Dimension(Integer.MAX_VALUE,
            Math.max(resendVerificationButton.getPreferredSize().height,
                returnToLoginButton.getPreferredSize().height)));
        actions.add(secondaryActions);
        return actions;
    }

    private JPanel passwordResetPanel() {
        JPanel panel = UI.glassPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));
        JPanel form = verticalForm();
        resetPasswordMessage.setForeground(UI.TEXT_MUTED);
        addFormItem(form, label("Atur ulang kata sandi"), 8);
        addFormItem(form, wrappedLabel("Masukkan email akun untuk menerima kode 6 angka "
            + "dan mengatur kata sandi baru."), 8);
        addFormItem(form, label("Email"), 8);
        addFormItem(form, resetPasswordEmail, 8);
        addFormItem(form, sendPasswordResetButton, 8);
        resetPasswordFields = verticalForm();
        addFormItem(resetPasswordFields, label("Kode 6 angka dari email"), 6);
        addFormItem(resetPasswordFields, resetPasswordCode, 6);
        addFormItem(resetPasswordFields, wrappedLabel(
            "Kata sandi baru (8+ karakter, huruf besar-kecil, angka, simbol)"), 6);
        addFormItem(resetPasswordFields, resetPasswordNew, 6);
        addFormItem(resetPasswordFields, label("Ulangi kata sandi baru"), 6);
        addFormItem(resetPasswordFields, resetPasswordConfirm, 6);
        addFormItem(resetPasswordFields, completePasswordResetButton, 6);
        passwordResetForm = form;
        addFormItem(form, resetPasswordMessage, 8);
        addFormItem(form, cancelPasswordResetButton, 8);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel registerPanel() {
        JPanel panel = UI.glassPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(26, 30, 26, 30));
        JPanel form = verticalForm();
        addFormItem(form, label("Daftar akun UMKM"), 6);
        addFormItem(form, label("Nama"), 6);
        addFormItem(form, registerName, 6);
        addFormItem(form, label("Email"), 6);
        addFormItem(form, registerEmail, 6);
        addFormItem(form, wrappedLabel("Kata sandi (8+ karakter, huruf besar-kecil, angka, simbol)"),
            6);
        addFormItem(form, registerPassword, 6);
        addFormItem(form, passwordStrengthPanel(), 6);
        addFormItem(form, label("Ulangi kata sandi"), 6);
        addFormItem(form, confirmPassword, 6);
        addFormItem(form, registerButton, 6);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel verticalForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        return form;
    }

    private void addFormItem(JPanel form, JComponent component, int gap) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(component);
        form.add(Box.createVerticalStrut(gap));
    }

    private JPanel passwordStrengthPanel() {
        JPanel meter = new JPanel(new BorderLayout(0, 5));
        meter.setOpaque(false);
        passwordStrengthLabel.setForeground(UI.TEXT_MUTED);
        passwordStrengthLabel.setFont(new Font(UI.FONT, Font.BOLD, 12));
        passwordStrengthBar.setBorderPainted(false);
        passwordStrengthBar.setStringPainted(false);
        passwordStrengthBar.setPreferredSize(new Dimension(0, 9));
        passwordStrengthBar.setBackground(UI.GLASS_BORDER);
        passwordStrengthBar.setForeground(UI.ACCENT);
        JLabel requirements = label("Min. 8 karakter, huruf besar-kecil, angka, dan simbol.");
        requirements.setForeground(UI.TEXT_MUTED);
        requirements.setFont(new Font(UI.FONT, Font.PLAIN, 11));

        JPanel barAndRequirements = new JPanel(new BorderLayout(0, 5));
        barAndRequirements.setOpaque(false);
        barAndRequirements.add(passwordStrengthBar, BorderLayout.NORTH);
        barAndRequirements.add(requirements, BorderLayout.SOUTH);
        meter.add(passwordStrengthLabel, BorderLayout.NORTH);
        meter.add(barAndRequirements, BorderLayout.CENTER);
        return meter;
    }

    private void updatePasswordStrength() {
        char[] password = registerPassword.getPassword();
        int score = passwordCriteriaScore(password);
        java.util.Arrays.fill(password, '\0');

        passwordStrengthBar.setValue(score);
        if (score == 0) {
            passwordStrengthLabel.setText("Kekuatan kata sandi: Belum diisi");
            passwordStrengthBar.setForeground(UI.GLASS_BORDER);
        } else {
            String level = passwordStrengthLevel(score);
            passwordStrengthLabel.setText("Kekuatan kata sandi: " + level
                + " (" + score + "/5 syarat)");
            passwordStrengthBar.setForeground(score <= 2 ? UI.ACCENT
                : score == 5 ? UI.GREEN : UI.AMBER);
        }
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UI.TEXT_LIGHT);
        return label;
    }

    private JLabel wrappedLabel(String text) {
        JLabel label = UI.htmlLabel("<html><div style='width:340px'>" + text
            + "</div></html>");
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
                            "Email akun sudah terverifikasi. Silakan masuk.");
                    } else {
                        showVerification(email);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (registrationMayHaveCompleted(cause)) {
                        showVerification(email);
                        verificationMessage.setText("<html><div style='text-align:center'>"
                            + "Pendaftaran belum dapat dipastikan selesai. Akun mungkin sudah "
                            + "tercatat. Periksa email dan folder spam. Jika belum ada kode, gunakan "
                            + "Kirim ulang email.</div></html>");
                        JOptionPane.showMessageDialog(AuthFrame.this,
                            "Pendaftaran belum dapat dipastikan selesai. Jangan mendaftar ulang "
                                + "dulu karena akun mungkin sudah tercatat. Periksa email dan "
                                + "folder spam, atau kirim ulang kode verifikasi.",
                            "Pendaftaran belum terkonfirmasi", JOptionPane.WARNING_MESSAGE);
                    } else {
                        showError(cause);
                    }
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
                    if (resetPasswordFields.getParent() == null) {
                        passwordResetForm.add(resetPasswordFields, 5);
                    }
                    resetPasswordMessage.setText("<html><div style='text-align:center'>"
                        + "Jika alamat email terdaftar, periksa kotak masuk dan folder spam "
                        + "untuk kode pemulihan 6 angka. Jika belum diterima, coba lagi "
                        + "beberapa saat.</div></html>");
                    revalidate();
                    repaint();
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
                    passwordResetForm.remove(resetPasswordFields);
                    resetPasswordMessage.setText("Kata sandi berhasil diubah. Silakan masuk.");
                    revalidate();
                    repaint();
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
                        "Permintaan kode verifikasi telah diproses. Periksa kotak masuk dan "
                            + "folder spam. Jika belum diterima, coba lagi beberapa saat.");
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
        verificationMessage.setText("<html><div style='text-align:center'>"
            + "Periksa kotak masuk dan folder spam untuk kode verifikasi ke <b>" + email
            + "</b>. Jika belum diterima, pilih Kirim ulang email.</div></html>");
        showCard("verification");
        tabs.setVisible(false);
    }

    private void showLogin() {
        showCard("login");
        tabs.setVisible(true);
    }

    private void showPasswordReset() {
        resetPasswordEmail.setText("");
        resetPasswordCode.setText("");
        resetPasswordNew.setText("");
        resetPasswordConfirm.setText("");
        passwordResetForm.remove(resetPasswordFields);
        resetPasswordMessage.setText("");
        showCard("password-reset");
        tabs.setVisible(false);
    }

    private void showCard(String name) {
        cards.show(cardPanel, name);
        cardPanel.revalidate();
        cardPanel.repaint();
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
        return passwordCriteriaScore(password) == 5;
    }

    static int passwordCriteriaScore(char[] password) {
        boolean minimumLength = password.length >= 8;
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
        int score = 0;
        if (minimumLength) score++;
        if (uppercase) score++;
        if (lowercase) score++;
        if (digit) score++;
        if (symbol) score++;
        return score;
    }

    static String passwordStrengthLevel(int score) {
        if (score <= 1) return "Sangat lemah";
        if (score == 2) return "Lemah";
        if (score < 5) return "Kuat";
        return "Sangat kuat";
    }

    private static boolean isSixDigitCode(String code) {
        return code != null && code.matches("[0-9]{6}");
    }

    private static boolean registrationMayHaveCompleted(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.net.SocketTimeoutException) return true;
            String message = cause.getMessage();
            if (message != null && (message.contains("HTTP 504")
                    || message.toLowerCase(java.util.Locale.ROOT).contains("gateway timeout")
                    || message.toLowerCase(java.util.Locale.ROOT)
                        .contains("melewati batas waktu saat mendaftar"))) {
                return true;
            }
        }
        return false;
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
        JOptionPane.showMessageDialog(this, userFacingErrorMessage(error), "Permintaan gagal",
            JOptionPane.ERROR_MESSAGE);
    }

    static String userFacingErrorMessage(Throwable error) {
        if (error == null) {
            return "Terjadi kendala saat memproses permintaan. Coba lagi beberapa saat.";
        }
        StringBuilder details = new StringBuilder();
        boolean timedOut = false;
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException) timedOut = true;
            if (cause.getMessage() != null) {
                if (details.length() > 0) details.append(' ');
                details.append(cause.getMessage());
            }
        }
        String normalized = details.toString().toLowerCase(Locale.ROOT);
        if (normalized.contains("akun ini sedang dinonaktifkan")) {
            return "Akun ini sedang dinonaktifkan.";
        }
        if (normalized.contains("invalid login credentials")
                || normalized.contains("invalid_credentials")) {
            return "Email atau kata sandi tidak sesuai.";
        }
        if (normalized.contains("email not confirmed")
                || normalized.contains("email_not_confirmed")
                || normalized.contains("verifikasi email terlebih dahulu")) {
            return "Verifikasi email terlebih dahulu sebelum masuk.";
        }
        if (normalized.contains("already registered")
                || normalized.contains("user_already_exists")) {
            return "Email sudah terdaftar. Silakan masuk atau gunakan Lupa kata sandi.";
        }
        if ((normalized.contains("otp") || normalized.contains("token"))
                && (normalized.contains("invalid") || normalized.contains("expired"))) {
            return "Kode tidak valid atau sudah kedaluwarsa. Periksa kode dan coba lagi.";
        }
        if (normalized.contains("rate limit") || normalized.contains("too many requests")
                || normalized.contains("http 429")) {
            return "Terlalu banyak percobaan. Tunggu beberapa saat sebelum mencoba lagi.";
        }
        if (timedOut || normalized.contains("timeout") || normalized.contains("timed out")
                || normalized.contains("melewati batas waktu")) {
            return "Koneksi sedang lambat. Periksa internet dan coba lagi beberapa saat.";
        }
        return "Terjadi kendala saat memproses permintaan. Periksa koneksi internet dan "
            + "coba lagi. Jika masalah berlanjut, hubungi administrator.";
    }

    private static class AccountData {
        AppUser user;
        WorkspaceData workspace;
    }
}
