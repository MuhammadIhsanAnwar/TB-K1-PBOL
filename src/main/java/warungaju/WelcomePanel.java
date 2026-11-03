package warungaju;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class WelcomePanel extends JPanel {
    private static final Color NAVY=new Color(12,35,43);
    private static final Color CYAN=new Color(78,211,177);
    private int animationTick;
    private Timer animationTimer;
    private JButton dashboard;

    public WelcomePanel(final MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(NAVY);
        add(glowStrip(),BorderLayout.WEST);

        JPanel center=new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(8,20,8,20);
        g.anchor=GridBagConstraints.WEST;

        JLabel eyebrow=new JLabel("UMKM FINANCIAL INTELLIGENCE");
        eyebrow.setForeground(CYAN);
        eyebrow.setFont(new Font("SansSerif",Font.BOLD,13));
        g.gridx=0; g.gridy=0; center.add(eyebrow,g);

        JLabel title=new JLabel("<html><div style='font-size:42px'>Selamat Datang di<br><font color='#4ed3b1'>WARUNGAJU</font></div></html>");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif",Font.BOLD,42));
        g.gridy=1; g.insets=new Insets(6,20,4,20); center.add(title,g);

        JLabel subtitle=new JLabel("<html><div style='width:520px;color:#c5d8d5;font-size:15px'>Rencanakan, simulasikan, dan pahami keuangan usaha Anda<br>melalui satu ruang kerja yang sederhana dan terarah.</div></html>");
        subtitle.setFont(new Font("SansSerif",Font.PLAIN,16));
        g.gridy=2; g.insets=new Insets(4,20,25,20); center.add(subtitle,g);

        JPanel features=new JPanel(new GridLayout(1,3,12,0));
        features.setOpaque(false);
        features.add(feature("01","Rencanakan","Susun target usaha dengan data yang mudah dipahami."));
        features.add(feature("02","Simulasikan","Uji skenario bisnis sebelum mengambil keputusan."));
        features.add(feature("03","Pantau","Lihat arus kas dan laporan dalam satu dashboard."));
        g.gridy=3; g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1; center.add(features,g);

        dashboard=UI.button("Halaman Dashboard  →");
        dashboard.setFont(new Font("SansSerif",Font.BOLD,15));
        dashboard.setPreferredSize(new Dimension(230,48));
        dashboard.setBorder(BorderFactory.createEmptyBorder(12,20,12,20));
        g.gridy=4; g.fill=GridBagConstraints.NONE; g.anchor=GridBagConstraints.WEST; g.insets=new Insets(30,20,8,20); center.add(dashboard,g);
        dashboard.addActionListener(e->frame.showDashboard());

        JLabel note=new JLabel("Data contoh tersedia untuk membantu Anda memulai.");
        note.setForeground(new Color(164,190,184));
        g.gridy=5; g.insets=new Insets(0,20,8,20); center.add(note,g);
        add(center,BorderLayout.CENTER);
        add(statusPanel(),BorderLayout.SOUTH);
        animationTimer=new Timer(45,e->{animationTick=(animationTick+1)%200; int pulse=(int)(Math.sin(animationTick*.08)*10); dashboard.setBackground(new Color(24,Math.max(70,92+pulse),Math.max(55,68+pulse))); dashboard.repaint(); repaint();});
        animationTimer.start();
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int x=70+(int)(Math.sin(animationTick*.04)*18);
        int y=90+(int)(Math.cos(animationTick*.035)*25);
        g.setColor(new Color(78,211,177,22));
        g.fillOval(x,y,190,190);
        g.setColor(new Color(78,211,177,10));
        g.fillOval(getWidth()-270,70+(int)(Math.sin(animationTick*.03)*30),230,230);
        g.dispose();
    }

    @Override public void removeNotify() {
        if(animationTimer!=null) animationTimer.stop();
        super.removeNotify();
    }

    private JPanel glowStrip() {
        JPanel strip=new JPanel(new GridBagLayout());
        strip.setPreferredSize(new Dimension(105,0));
        strip.setBackground(new Color(16,75,72));
        JLabel mark=new JLabel("W");
        mark.setForeground(CYAN);
        mark.setFont(new Font("SansSerif",Font.BOLD,54));
        strip.add(mark);
        return strip;
    }

    private JPanel feature(String number,String title,String description) {
        JPanel panel=new JPanel(new BorderLayout(4,5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(45,91,91)),new EmptyBorder(12,12,12,12)));
        JLabel n=new JLabel(number); n.setForeground(CYAN); n.setFont(new Font("SansSerif",Font.BOLD,12));
        JLabel t=new JLabel(title); t.setForeground(Color.WHITE); t.setFont(new Font("SansSerif",Font.BOLD,14));
        JLabel d=new JLabel("<html><div style='width:135px;color:#a8c3be;font-size:11px'>"+description+"</div></html>");
        panel.add(n,BorderLayout.NORTH); panel.add(t,BorderLayout.CENTER); panel.add(d,BorderLayout.SOUTH);
        return panel;
    }

    private JPanel statusPanel() {
        JPanel panel=new JPanel(new BorderLayout());
        panel.setBackground(new Color(9,27,34));
        panel.setBorder(new EmptyBorder(12,25,12,25));
        JLabel left=new JLabel("WARUNGAJU  /  PERENCANAAN KEUANGAN UMKM");
        left.setForeground(new Color(137,171,165));
        left.setFont(new Font("SansSerif",Font.BOLD,10));
        JLabel right=new JLabel("Versi awal  •  Siap membantu usaha Anda");
        right.setForeground(new Color(137,171,165));
        right.setFont(new Font("SansSerif",Font.PLAIN,10));
        panel.add(left,BorderLayout.WEST); panel.add(right,BorderLayout.EAST);
        return panel;
    }
}
