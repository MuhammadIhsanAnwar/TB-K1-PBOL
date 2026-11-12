package warungaju;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
            + "<font color='#C8D0FF'>Akun aktif yang terdaftar di WARUNGAJU</font></html>");
        title.setFont(new Font("SansSerif", Font.PLAIN, 14));
        header.add(title, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(new JLabel(user.email));
        JButton logout = UI.secondaryButton("Keluar");
        actions.add(refreshButton);
        actions.add(logout);
        header.add(actions, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JTable table = new JTable(model);
        UI.styleTable(table);
        root.add(UI.scrollPane(table), BorderLayout.CENTER);
        setContentPane(root);

        refreshButton.addActionListener(event -> loadUsers());
        logout.addActionListener(event -> logout());
        loadUsers();
    }

    private void loadUsers() {
        refreshButton.setEnabled(false);
        model.setRowCount(0);
        model.addRow(new Object[]{"Memuat data pengguna...", "", "", "", ""});
        new SwingWorker<List<AppUser>, Void>() {
            @Override
            protected List<AppUser> doInBackground() throws Exception {
                return client.getActiveProfiles(session);
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
                        model.addRow(new Object[]{"Belum ada pengguna aktif.", "", "", "", "", "", "", ""});
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                } finally {
                    refreshButton.setEnabled(true);
                }
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
