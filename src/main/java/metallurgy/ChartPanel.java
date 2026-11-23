package metallurgy;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/** Точки: расчетный диаметр в зависимости от садки. */
public class ChartPanel extends JPanel {
    private List<Database.Entry> data=List.of();
    public void setData(List<Database.Entry> data){this.data=List.copyOf(data);repaint();}
    public static BufferedImage image(List<Database.Entry> data,int width,int height){BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();draw(g,data,width,height);g.dispose();return image;}
    @Override protected void paintComponent(Graphics g){super.paintComponent(g);draw((Graphics2D)g,data,getWidth(),getHeight());}
    private static void draw(Graphics2D g,List<Database.Entry> data,int w,int h){
        g.setColor(Color.WHITE);g.fillRect(0,0,w,h);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,18));g.drawString("Диаметр конвертера в зависимости от садки",75,35);
        int left=80,bottom=h-75,right=w-30,top=65;g.drawLine(left,bottom,right,bottom);g.drawLine(left,bottom,left,top);g.setFont(new Font("SansSerif",Font.PLAIN,13));g.drawString("Садка, т",Math.max(left,right-90),h-30);g.drawString("Диаметр, м",left+5,top-15);
        double maxX=1,maxY=1;for(var e:data){maxX=Math.max(maxX,e.input().mass());maxY=Math.max(maxY,ConverterCalculator.calculate(e.input()).diameter());}maxX*=1.1;maxY*=1.1;
        for(int i=1;i<=5;i++){int x=left+(right-left)*i/5,y=bottom-(bottom-top)*i/5;g.setColor(new Color(225,225,225));g.drawLine(x,top,x,bottom);g.drawLine(left,y,right,y);g.setColor(Color.DARK_GRAY);g.drawString(String.format(java.util.Locale.US,"%.0f",maxX*i/5),x-10,bottom+20);g.drawString(String.format(java.util.Locale.US,"%.1f",maxY*i/5),left-38,y+5);}g.setColor(new Color(30,110,180));for(var e:data){int x=left+(int)((right-left)*e.input().mass()/maxX);int y=bottom-(int)((bottom-top)*ConverterCalculator.calculate(e.input()).diameter()/maxY);g.fillOval(x-4,y-4,8,8);}
    }
}
