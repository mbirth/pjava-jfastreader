import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class MyProgBar extends Component {
  final static int WINDOWS = 1;
  final static int PQ1 = 2;
  final static int PQ2 = 3;
  long minVal;
  long maxVal;
  long curVal;
  private int systemDelta = 0;
  Color fgColor = new Color(0,0,128);
  Color bgColor = new Color(220,220,220);
  Color txColor = Color.white;
  int Height = 18;
  int Width = 40;
  private int mode;
  
  public MyProgBar(long min, long max, long pos, int mode) {
    this.minVal = min;
    this.maxVal = max;
    this.curVal = pos;
    this.mode = mode;
    if (System.getProperty("os.name").equals("EPOC")) {
      this.systemDelta = -1;
    }
  }
    
  public MyProgBar(long min, long max, long pos) {
    this(min, max, pos, MyProgBar.WINDOWS);
  }
  
  public MyProgBar(long min, long max) {
    this(min, max, min, MyProgBar.WINDOWS);
  }
  
  public MyProgBar() {
    this(0, 100, 0, MyProgBar.WINDOWS);
  }
    
  public void paint(Graphics g) {
    if (g != null) {
      double percentage = (double)(curVal-minVal)/(double)(maxVal-minVal);
      String percString = String.valueOf((int)(percentage*100)) + "%";
      g.setFont(new Font("Dialog", Font.PLAIN, 10));
      FontMetrics fm = getFontMetrics(g.getFont());
      int pright = (int)((double)(getSize().width-2)/(maxVal-minVal)*(curVal-minVal));
      g.setColor(bgColor);
      g.fill3DRect(pright+1,1,getSize().width-2-pright+systemDelta,getSize().height-2+systemDelta, false);
      g.setColor(fgColor);
      g.fill3DRect(1,1,pright+systemDelta,getSize().height-2+systemDelta, true);
      g.setXORMode(txColor);
      int tleft;
      switch (mode) {
        default:
        case MyProgBar.WINDOWS:
          g.drawString(percString, getSize().width/2-fm.stringWidth(percString)/2, getSize().height/2-fm.getHeight()/2+fm.getAscent());
          break;
  
        case MyProgBar.PQ1:
          tleft = pright+2;
          if (tleft+fm.stringWidth(percString)>=getSize().width-1) tleft = getSize().width-1-fm.stringWidth(percString);
          g.drawString(percString, tleft, getSize().height/2-fm.getHeight()/2+fm.getAscent());
          break;
  
        case MyProgBar.PQ2:
          tleft = (pright-1)/2-fm.stringWidth(percString)/2+1;
          if (tleft <= 2) tleft = 2;
          g.drawString(percString, tleft, getSize().height/2-fm.getHeight()/2+fm.getAscent());
          break;
      }
      g.setPaintMode();
      g.setColor(Color.black);
      g.drawRect(0,0,getSize().width-1,getSize().height-1);
    }
  }
  
  public void setMinValue(long v) {
    minVal = v;
  }
    
  public void setMaxValue(long v) {
    maxVal = v;
  }
  
  public void setBgColor(Color c) {
    bgColor = c;
  }
  
  public void setFgColor(Color c) {
    fgColor = c;
  }
    
  public void setPos(long p) {
    curVal = p;
    if (curVal > maxVal) curVal = maxVal;
      else if (curVal < minVal ) curVal = minVal;
    paint(getGraphics());
  }
  
  public void setMode(int m) {
    mode = m;
    paint(getGraphics());
  }
  
  public Dimension getPreferredSize() {
    return new Dimension(Width, Height);
  }
  
  public Dimension getMinimumSize() {
    return new Dimension(Width, Height);
  }
  
}