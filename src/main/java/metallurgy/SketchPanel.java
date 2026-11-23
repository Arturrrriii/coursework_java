package metallurgy;
import javax.swing.*;
import java.awt.*;

/** Условное схематическое сечение, не чертеж в масштабе. */
public class SketchPanel extends JPanel {
    private ConverterCalculator.Result result;
    public SketchPanel(){setPreferredSize(new Dimension(330,360));}
    public void setResult(ConverterCalculator.Result r){result=r;repaint();}
    protected void paintComponent(Graphics gr){super.paintComponent(gr);Graphics2D g=(Graphics2D)gr.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(Color.WHITE);g.fillRect(0,0,getWidth(),getHeight());g.setColor(Color.DARK_GRAY);g.drawString("Схема внутреннего пространства",20,25);if(result==null){g.drawString("Выполните расчет",20,55);g.dispose();return;}
        int cx=getWidth()/2,top=60,bottom=getHeight()-50;double scale=Math.min((getWidth()-80)/result.diameter(),(bottom-top)/result.height());int half=(int)(result.diameter()*scale/2), neck=(int)(result.mouthDiameter()*scale/2), lower=(int)(result.bottomDiameter()*scale/2), cone=(int)(result.coneHeight()*scale), bath=(int)(result.height()*scale);int y0=top,y1=top+cone,y2=top+bath-(int)(result.heightRatio()*0);int yBath=top+bath-(int)((result.height()-result.freeHeight())*scale);g.setColor(new Color(235,242,248));Polygon p=new Polygon(new int[]{cx-neck,cx-half,cx-half,cx-lower,cx+lower,cx+half,cx+half,cx+neck},new int[]{y0,y1,yBath,y2,y2,yBath,y1,y0},8);g.fillPolygon(p);g.setColor(new Color(30,85,130));g.setStroke(new BasicStroke(2));g.drawPolygon(p);g.setColor(new Color(190,100,50));g.drawLine(cx-half,yBath,cx+half,yBath);g.setColor(Color.DARK_GRAY);g.drawString(String.format("D = %.2f м; H = %.2f м",result.diameter(),result.height()),20,getHeight()-15);g.dispose();}
}
