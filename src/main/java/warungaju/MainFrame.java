package warungaju;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    final CardLayout pages = new CardLayout();
    final JPanel pageContainer = new JPanel(pages);
    final AnimatedCardPanel content = new AnimatedCardPanel();
    final java.util.List<Transaksi> transaksi = new ArrayList<>();
    final java.util.List<Produk> produk = new ArrayList<>();
    final Set<String> importedFiles = new HashSet<>();
    boolean actualDataImported;
    final LaporanKeuangan laporan = new LaporanKeuangan();
    final SupabaseClient supabase;
    final AuthSession session;
    final AppUser user;
    final WorkspaceData workspace;
    private boolean welcomeVisible = false;
    private String currentPage = "Dashboard";
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

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
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new RadialGradientPaint(
                    new Point(getWidth() - 130, 120), 420,
                    new float[]{0f, 1f},
                    new Color[]{UI.GLOW_PURPLE, new Color(UI.GLOW_PURPLE.getRed(),
                        UI.GLOW_PURPLE.getGreen(), UI.GLOW_PURPLE.getBlue(), 0)}
                ));
                g.fillOval(getWidth() - 550, -300, 840, 840);
                g.setPaint(new RadialGradientPaint(
                    new Point(330, getHeight() - 60), 360,
                    new float[]{0f, 1f},
                    new Color[]{UI.GLOW_GREEN, new Color(UI.GLOW_GREEN.getRed(),
                        UI.GLOW_GREEN.getGreen(), UI.GLOW_GREEN.getBlue(), 0)}
                ));
                g.fillOval(-140, getHeight() - 430, 720, 720);
                Color goldGlow = new Color(UI.PRIMARY_START.getRed(),
                    UI.PRIMARY_START.getGreen(), UI.PRIMARY_START.getBlue(),
                    UI.lightMode ? 30 : 24);
                g.setPaint(new RadialGradientPaint(
                    new Point(getWidth() / 2, 0), 470,
                    new float[]{0f, 1f},
                    new Color[]{goldGlow, new Color(goldGlow.getRed(), goldGlow.getGreen(),
                        goldGlow.getBlue(), 0)}
                ));
                g.fillOval(getWidth() / 2 - 470, -470, 940, 940);
                g.dispose();
            }
        };
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
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UI.GLASS_BORDER),
            BorderFactory.createEmptyBorder(13, 26, 13, 30)
        ));
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel("W", SwingConstants.CENTER);
        mark.setPreferredSize(new Dimension(38, 38));
        mark.setOpaque(true);
        mark.setBackground(new Color(UI.ACCENT.getRed(), UI.ACCENT.getGreen(), UI.ACCENT.getBlue(), 30));
        mark.setForeground(UI.ACCENT);
        mark.setFont(new Font("SansSerif", Font.BOLD, 22));
        mark.setBorder(BorderFactory.createLineBorder(new Color(UI.ACCENT.getRed(),
            UI.ACCENT.getGreen(), UI.ACCENT.getBlue(), 110), 1, true));
        JPanel name = new JPanel();
        name.setOpaque(false);
        name.setLayout(new BoxLayout(name, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("WARUNGAJU");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(UI.ACCENT);
        JLabel subtitle = new JLabel("FINANCIAL WORKSPACE");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 9));
        subtitle.setForeground(UI.TEXT_MUTED);
        name.add(title);
        name.add(subtitle);
        brand.add(mark);
        brand.add(name);

        JPanel account = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        account.setOpaque(false);
        JLabel status = new JLabel("●");
        status.setForeground(new Color(114, 226, 164));
        JLabel accountName = new JLabel((user.fullName == null || user.fullName.trim().isEmpty()
            ? user.email : user.fullName) + "  /  UMKM");
        accountName.setForeground(UI.TEXT_LIGHT);
        JButton logout = UI.secondaryButton("Keluar");
        logout.addActionListener(event -> logout());
        account.add(status);
        account.add(accountName);
        account.add(UI.themeToggle(this::switchTheme));
        account.add(logout);
        p.add(brand, BorderLayout.WEST);
        p.add(account, BorderLayout.EAST);
        return p;
    }

    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(240, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, UI.GLASS_BORDER),
            BorderFactory.createEmptyBorder(26, 14, 20, 14)
        ));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        JLabel brand = new JLabel("  YOUR WORKSPACE");
        brand.setForeground(UI.TEXT_MUTED);
        brand.setFont(new Font("SansSerif", Font.BOLD, 10));
        p.add(brand);
        p.add(Box.createVerticalStrut(18));
        
        String[] menus = {"Dashboard", "Transaksi", "Produk dan HPP", "Simulasi Bisnis", "Prediksi Arus Kas", "Laporan Keuangan"};
        
        for (final String menu : menus) {
            JButton b = UI.navButton(menu);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            b.addActionListener(e -> {
                showPage(menu);
            });
            navButtons.put(menu, b);
            setNavActive(b, menu.equals(currentPage));
            p.add(b);
            p.add(Box.createVerticalStrut(6));
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
            setNavActive(entry.getValue(), entry.getKey().equals(page));
        }
    }

    boolean persistWorkspace() {
        workspace.transactions = new ArrayList<>();
        for (Transaksi item : transaksi) {
            workspace.transactions.add(new WorkspaceData.TransactionData(item));
        }
        workspace.products = new ArrayList<>();
        for (Produk item : produk) {
            workspace.products.add(new WorkspaceData.ProductData(item));
        }
        workspace.actualDataImported = actualDataImported;
        try {
            supabase.saveWorkspace(session, workspace);
            return true;
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Perubahan belum tersimpan ke cloud: " + ex.getMessage(),
                "Sinkronisasi gagal", JOptionPane.ERROR_MESSAGE);
            return false;
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

    private void setNavActive(JButton button, boolean active) {
        button.putClientProperty("warungaju.active", active);
        button.setOpaque(active);
        button.setContentAreaFilled(active);
        button.setBackground(active ? UI.GLASS_ACTIVE : UI.BG_DARK);
        button.setForeground(active ? UI.ACCENT : UI.TEXT_LIGHT);
    }

    private void switchTheme() {
        UI.toggleTheme();
        build();
        SwingUtilities.updateComponentTreeUI(this);
    }
}