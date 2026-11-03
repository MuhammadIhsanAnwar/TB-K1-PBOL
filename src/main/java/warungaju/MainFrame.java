package warungaju;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    static final Color GREEN=new Color(24,92,68), LIGHT=new Color(245,248,247), TEXT=new Color(38,52,48);
    final CardLayout pages=new CardLayout(); final JPanel pageContainer=new JPanel(pages);
    final AnimatedCardPanel content=new AnimatedCardPanel();
    final java.util.List<Transaksi> transaksi=new ArrayList<Transaksi>(); final java.util.List<Produk> produk=new ArrayList<Produk>();
    final Set<String> importedFiles=new HashSet<String>();
    boolean actualDataImported;
    final LaporanKeuangan laporan=new LaporanKeuangan();
    public MainFrame() {
        setTitle("WARUNGAJU - Perencanaan dan Simulasi Keuangan UMKM"); setSize(1280,800); setMinimumSize(new Dimension(980,650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLocationRelativeTo(null);
        transaksi.add(new Transaksi(java.time.LocalDate.now(),"Pemasukan","Penjualan",1250000,"Penjualan harian"));
        transaksi.add(new Transaksi(java.time.LocalDate.now().minusDays(1),"Pengeluaran","Bahan baku",450000,"Belanja bahan"));
        produk.add(new Produk("Nasi Goreng",9000,18000,45)); produk.add(new Produk("Es Teh Manis",2500,6000,80));
        build();
    }
    private void build() {
        JPanel root=new JPanel(new BorderLayout()); root.setBackground(LIGHT); root.add(header(),BorderLayout.NORTH); root.add(sidebar(),BorderLayout.WEST);
        content.addPage("Dashboard",new DashboardPanel(this)); content.addPage("Transaksi",new TransaksiPanel(this)); content.addPage("Produk dan HPP",new ProdukPanel(this));
        content.addPage("Simulasi Bisnis",new SimulasiPanel(this)); content.addPage("Prediksi Arus Kas",new ArusKasPanel(this)); content.addPage("Laporan Keuangan",new LaporanPanel(this));
        root.add(content,BorderLayout.CENTER);
        pageContainer.add(new WelcomePanel(this),"Welcome"); pageContainer.add(root,"Application");
        setContentPane(pageContainer); pages.show(pageContainer,"Welcome");
    }
    private JPanel header(){JPanel p=new JPanel(new BorderLayout()); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createEmptyBorder(18,28,18,28));
        JLabel title=new JLabel("WARUNGAJU"); title.setFont(new Font("SansSerif",Font.BOLD,25)); title.setForeground(GREEN);
        JLabel user=new JLabel("Warung Maju Jaya  |  Admin"); user.setForeground(new Color(90,105,100)); p.add(title,BorderLayout.WEST); p.add(user,BorderLayout.EAST); return p;}
    private JPanel sidebar(){JPanel p=new JPanel(); p.setPreferredSize(new Dimension(220,0)); p.setBackground(GREEN); p.setBorder(BorderFactory.createEmptyBorder(25,14,20,14)); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        JLabel brand=new JLabel("  FINANCIAL PLANNER"); brand.setForeground(new Color(190,225,210)); brand.setFont(new Font("SansSerif",Font.BOLD,11)); p.add(brand); p.add(Box.createVerticalStrut(25));
        String[] menus={"Dashboard","Transaksi","Produk dan HPP","Simulasi Bisnis","Prediksi Arus Kas","Laporan Keuangan"};
        final JButton[] active={null}; for(final String menu:menus){JButton b=UI.navButton(menu); b.setAlignmentX(Component.LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(192,44)); b.addActionListener(e->{if(active[0]!=null){active[0].putClientProperty("warungaju.active",false);active[0].setBackground(GREEN);}b.putClientProperty("warungaju.active",true);b.setBackground(new Color(35,119,89));active[0]=b;content.showPage(menu);}); p.add(b); p.add(Box.createVerticalStrut(5));} return p;}
    public void refresh(){content.clearPages(); content.addPage("Dashboard",new DashboardPanel(this)); content.addPage("Transaksi",new TransaksiPanel(this)); content.addPage("Produk dan HPP",new ProdukPanel(this)); content.addPage("Simulasi Bisnis",new SimulasiPanel(this)); content.addPage("Prediksi Arus Kas",new ArusKasPanel(this)); content.addPage("Laporan Keuangan",new LaporanPanel(this)); content.revalidate(); content.repaint();}
    void showDashboard(){pages.show(pageContainer,"Application"); content.showPage("Dashboard");}
}
