package warungaju;
import java.awt.*; import javax.swing.*;

public class DashboardPanel extends JPanel {
    public DashboardPanel(MainFrame f){setLayout(new BorderLayout(0,18));setBackground(MainFrame.LIGHT);setBorder(BorderFactory.createEmptyBorder(25,30,30,30));
        add(new JLabel("<html><h1>Selamat datang di WARUNGAJU</h1><font color='#60736b'>Ringkasan kondisi usaha Anda hari ini</font></html>"),BorderLayout.NORTH);
        double in=f.laporan.pemasukan(f.transaksi), out=f.laporan.pengeluaran(f.transaksi); JPanel cards=new JPanel(new GridLayout(1,4,14,0));cards.setOpaque(false);cards.add(UI.card("Pemasukan bulan ini",UI.money(in),new Color(24,130,87)));cards.add(UI.card("Pengeluaran bulan ini",UI.money(out),new Color(210,102,57)));cards.add(UI.card("Keuntungan bersih",UI.money(in-out),MainFrame.GREEN));cards.add(UI.card("Saldo kas saat ini",UI.money(in-out+3500000),new Color(58,92,150)));
        JPanel center=new JPanel(new BorderLayout(15,15));center.setOpaque(false);center.add(cards,BorderLayout.NORTH); JPanel info=new JPanel(new BorderLayout(10,10));info.setBackground(Color.WHITE);info.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));String status=f.actualDataImported?"<font color='#188257'><b>Tersedia</b></font>":"<font color='#b47700'>Belum tersedia</font>";info.add(new JLabel("<html><h2>Ringkasan aktivitas</h2><p>Gunakan data aktual untuk hasil simulasi yang lebih akurat.</p><p><b>"+f.transaksi.size()+"</b> transaksi tercatat &nbsp; | &nbsp; Data aktual: "+status+"</p><p>Prediksi arus kas 3 bulan: <font color='#188257'><b>stabil</b></font></p></html>"),BorderLayout.NORTH);center.add(info,BorderLayout.CENTER);add(center,BorderLayout.CENTER);}
}
