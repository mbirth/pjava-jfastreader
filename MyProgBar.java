import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

class MyProgBar extends Canvas {
  int minVal = 0;
  int maxVal = 100;
  int curVal = 50;
  Color fgColor = new Color(0,0,128);
  Color bgColor = Color.lightGray;
  Color txColor = Color.white;
  
  public MyProgBar(int min, int max, int pos) {
    super();
    this.minVal = min;
    this.maxVal = max;
    this.curVal = pos;
  }
    
  public MyProgBar(int min, int max) {
    super();
    this.minVal = min;
    this.maxVal = max;
  }
    
  public void paint(Graphics g) {
    double percentage = (double)(curVal-minVal)/(double)(maxVal-minVal);
    String percString = String.valueOf((int)(percentage*100)) + "%";
    g.setFont(new Font("Dialog", Font.PLAIN, 10));
    FontMetrics fm = getFontMetrics(g.getFont());
    g.setColor(bgColor);
    g.fillRect(0,0,getSize().width-1,getSize().height-1);
    g.setColor(fgColor);
    g.fillRect(0,0,(int)((double)(getSize().width-1)/(maxVal-minVal)*(curVal-minVal)),getSize().height-1);
    g.setXORMode(txColor);
    g.drawString(percString, getSize().width/2-fm.stringWidth(percString)/2, getSize().height/2-fm.getHeight()/2+fm.getAscent());
    g.setPaintMode();
    g.setColor(Color.black);
    g.drawRect(0,0,getSize().width-1,getSize().height-1);
  }
    
  public void setMinValue(int v) {
    minVal = v;
  }
    
  public void setMaxValue(int v) {
    maxVal = v;
  }
    
  public void setPos(int p) {
    curVal = p;
    paint(getGraphics());
  }
}
