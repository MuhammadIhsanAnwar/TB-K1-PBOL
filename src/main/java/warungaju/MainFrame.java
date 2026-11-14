package warungaju;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

/**
 * Versi gabungan: logika Supabase milik tim (login, workspace, persistWorkspace,
 * logout) + tampilan baru (header/sidebar datar, tanpa glow, Segoe UI).
 */
public class MainFrame extends JFrame {
    final CardLayout pages = new CardLayout();
    final JPanel pageContainer = new JPanel(pages);
    final AnimatedCardPanel content = new AnimatedCardPanel();
    final java.util.List<Transaksi> transaksi = new ArrayList<>();
    final java.util.List<Produk> produk = new ArrayList<>();
    final Set<String> importedFiles = new HashSet<>();
    boolean actualDataImported;
    final LaporanKeuangan laporan = new LaporanKeuangan(transaksi, produk);
    final SupabaseClient supabase;
    final AuthSession session;
    final AppUser user;
    final WorkspaceData workspace;
    private final ExecutorService workspaceSync = createWorkspaceSyncExecutor();
    private boolean welcomeVisible = false;
    private String currentPage = "Dashboard";
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private static ExecutorService createWorkspaceSyncExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), task ->
                new Thread(task, "warungaju-supabase-sync"));
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public MainFrame(SupabaseClient supabase, AuthSession session, AppUser user,
                     WorkspaceData workspace) {
        this.supabase = supabase;
        this.session = session;
        this.user = user;
        this.workspace = workspace;
        this.workspace.normalize();
        transaksi.addAll(workspace.toTransactions());
        produk.addAll(workspace.toProducts());
        actualDataImported = workspace.actualDataImported;
        setTitle("WARUNGAJU - Perencanaan dan Simulasi Keuangan UMKM");
        setSize(1280, 800);
        setMinimumSize(new Dimension(980, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        build();
    }

    private void build() {
        content.clearPages();
        pageContainer.removeAll();
        pageContainer.setBackground(UI.BG_DARK);
        navButtons.clear();

        // Latar polos: lingkaran glow di belakang sudah dihapus.
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(UI.BG_DARK);
        root.add(header(), BorderLayout.NORTH);
        root.add(sidebar(), BorderLayout.WEST);

        content.addPage("Dashboard", new DashboardPanel(this));
        content.addPage("Transaksi", new TransaksiPanel(this));
        content.addPage("Produk dan HPP", new ProdukPanel(this));
        content.addPage("Simulasi Bisnis", new SimulasiPanel(this));
        content.addPage("Prediksi Arus Kas", new ArusKasPanel(this));
        content.addPage("Laporan Keuangan", new LaporanPanel(this));

        root.add(content, BorderLayout.CENTER);
        pageContainer.add(new WelcomePanel(this, this::switchTheme), "Welcome");
        pageContainer.add(root, "Application");
        setContentPane(pageContainer);
        if (welcomeVisible) {
            pages.show(pageContainer, "Welcome");
        } else {
            pages.show(pageContainer, "Application");
            content.showPage(currentPage);
        }
    }

    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(UI.GLASS_LIGHT);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UI.GLASS_BORDER),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel("W", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UI.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UI.ARC, UI.ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mark.setPreferredSize(new Dimension(36, 36));
        mark.setOpaque(false);
        mark.setForeground(UI.BUTTON_TEXT);
        mark.setFont(new Font(UI.FONT, Font.BOLD, 20));
        JPanel name = new JPanel();
        name.setOpaque(false);
        name.setLayout(new BoxLayout(name, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("WARUNGAJU");
        title.setFont(new Font(UI.FONT, Font.BOLD, 17));
        title.setForeground(UI.TEXT_LIGHT);
        JLabel subtitle = new JLabel("Pembukuan warung");
        subtitle.setFont(new Font(UI.FONT, Font.PLAIN, 11));
        subtitle.setForeground(UI.TEXT_MUTED);
        name.add(title);
        name.add(subtitle);
        brand.add(mark);
        brand.add(name);

        JPanel account = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        account.setOpaque(false);
        JLabel accountName = new JLabel((user.fullName == null || user.fullName.trim().isEmpty()
            ? user.email : user.fullName) + "  /  UMKM");
        accountName.setForeground(UI.TEXT_MUTED);
        JButton logout = UI.secondaryButton("Keluar");
        logout.addActionListener(event -> logout());
        account.add(accountName);
        account.add(UI.themeToggle(this::switchTheme));
        account.add(logout);
        p.add(brand, BorderLayout.WEST);
        p.add(account, BorderLayout.EAST);
        return p;
    }

    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(230, 0));
        p.setOpaque(true);
        p.setBackground(UI.SIDEBAR_BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        String[] menus = {"Dashboard", "Transaksi", "Produk dan HPP", "Simulasi Bisnis", "Prediksi Arus Kas", "Laporan Keuangan"};

        for (final String menu : menus) {
            JButton b = UI.navButton(menu);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            b.addActionListener(e -> {
                showPage(menu);
            });
            navButtons.put(menu, b);
            UI.setNavActive(b, menu.equals(currentPage));
            p.add(b);
            p.add(Box.createVerticalStrut(2));
        }
        return p;
    }

    public void refresh() {
        content.clearPages();
        content.addPage("Dashboard", new DashboardPanel(this));
        content.addPage("Transaksi", new TransaksiPanel(this));
        content.addPage("Produk dan HPP", new ProdukPanel(this));
        content.addPage("Simulasi Bisnis", new SimulasiPanel(this));
        content.addPage("Prediksi Arus Kas", new ArusKasPanel(this));
        content.addPage("Laporan Keuangan", new LaporanPanel(this));
        content.revalidate();
        content.repaint();
    }

    void showDashboard() {
        welcomeVisible = false;
        pages.show(pageContainer, "Application");
        showPage("Dashboard");
    }

    void showPage(String page) {
        welcomeVisible = false;
        currentPage = page;
        pages.show(pageContainer, "Application");
        content.showPage(page);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            UI.setNavActive(entry.getValue(), entry.getKey().equals(page));
        }
    }

    void persistWorkspace() {
        persistWorkspace(null);
    }

    void persistWorkspace(final Runnable onSuccess) {
        final WorkspaceData snapshot = new WorkspaceData();
        for (Transaksi item : transaksi) {
            snapshot.transactions.add(new WorkspaceData.TransactionData(item));
        }
        for (Produk item : produk) {
            snapshot.products.add(new WorkspaceData.ProductData(item));
        }
        snapshot.actualDataImported = actualDataImported;
        snapshot.simulation.ingredientPrice = workspace.simulation.ingredientPrice;
        snapshot.simulation.employeeCount = workspace.simulation.employeeCount;
        snapshot.simulation.salaryPerEmployee = workspace.simulation.salaryPerEmployee;
        snapshot.simulation.operatingHours = workspace.simulation.operatingHours;
        snapshot.simulation.monthlySalesTarget = workspace.simulation.monthlySalesTarget;
        snapshot.simulation.sellingPrice = workspace.simulation.sellingPrice;
        snapshot.cashFlow.startingBalance = workspace.cashFlow.startingBalance;
        snapshot.cashFlow.monthlyIncome = workspace.cashFlow.monthlyIncome;
        snapshot.cashFlow.monthlyExpenses = workspace.cashFlow.monthlyExpenses;
        try {
            workspaceSync.execute(() -> {
                try {
                    supabase.saveWorkspace(session, snapshot);
                    if (onSuccess != null) {
                        SwingUtilities.invokeLater(() -> {
                            if (isDisplayable()) onSuccess.run();
                        });
                    }
                } catch (java.io.IOException ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Perubahan belum tersimpan ke cloud: " + ex.getMessage()
                            + ". Periksa koneksi lalu coba simpan kembali.",
                        "Sinkronisasi gagal", JOptionPane.ERROR_MESSAGE));
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ex) {
            JOptionPane.showMessageDialog(this,
                "Sinkronisasi cloud tidak dapat dijalankan: " + ex.getMessage(),
                "Sinkronisasi gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        try {
            supabase.signOut(session);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Sesi lokal ditutup, tetapi Supabase tidak dapat mengakhiri sesi: "
                    + ex.getMessage(),
                "Keluar", JOptionPane.WARNING_MESSAGE);
        }
        dispose();
        new AuthFrame().setVisible(true);
    }

    private void switchTheme() {
        UI.toggleTheme();
        build();
        SwingUtilities.updateComponentTreeUI(this);
    }
}