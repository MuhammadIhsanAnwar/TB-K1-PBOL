package warungaju;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

class AnimatedCardPanel extends JPanel {
    private final Map<String, JComponent> pages=new LinkedHashMap<String, JComponent>();
    private JComponent active;
    private Timer timer;

    AnimatedCardPanel(){setLayout(null);setOpaque(false);}

    void addPage(String name,JComponent page){pages.put(name,page);page.setVisible(false);add(page);}
    void clearPages(){if(timer!=null)timer.stop();pages.clear();active=null;removeAll();}

    void showPage(final String name){
        final JComponent next=pages.get(name);
        if(next==null||next==active)return;
        if(timer!=null&&timer.isRunning())timer.stop();
        final JComponent previous=active;
        if(previous==null){active=next;next.setBounds(0,0,getWidth(),getHeight());next.setVisible(true);revalidate();repaint();return;}
        final int direction=1;
        next.setVisible(true); next.setBounds(direction*getWidth(),0,getWidth(),getHeight());
        final long started=System.currentTimeMillis();
        timer=new Timer(15,e->{
            double progress=Math.min(1,(System.currentTimeMillis()-started)/260.0);
            double eased=1-Math.pow(1-progress,3);
            int shift=(int)(getWidth()*eased);
            previous.setLocation(-direction*shift,0);
            next.setLocation(direction*getWidth()-direction*shift,0);
            if(progress>=1){previous.setVisible(false);next.setLocation(0,0);active=next;timer.stop();}
            repaint();
        });
        timer.start();
    }

    @Override public void doLayout(){if(active!=null&& (timer==null||!timer.isRunning()))active.setBounds(0,0,getWidth(),getHeight());}
}
