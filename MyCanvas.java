import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

class MyCanvas extends Canvas {
  Color bgColor = new Color(0,0,128);
  Color fgColor = new Color(255,255,0);
  String word = "";
  int maxFontSize = 40;
    
  public void paint(Graphics g) {
    g.setColor(bgColor);
    g.fillRect(0,0,getSize().width-1,getSize().height-2);
    g.setColor(Color.black);
    g.drawRect(0,0,getSize().width-1,getSize().height-2);
    if (word.length()>0) {
      g.setColor(fgColor);
      // do auto-downsizing of font
      int fs = maxFontSize;
      Font f;
      FontMetrics fm;
      do {
        f = new Font("Dialog", Font.BOLD, fs);
        fm = g.getFontMetrics(f);
        // System.out.println("MyC: Font-size now "+fs+"pt.");
        fs -= 4;
      } while (((fm.stringWidth(word)>getSize().width-2) || (fm.getHeight()>getSize().height-2)) && fs > 0);
      g.setFont(f);
      g.drawString(word, getSize().width/2-fm.stringWidth(word)/2, getSize().height/2-fm.getHeight()/2+fm.getAscent());
    }
  }
  
  public void setWord(String txt) {
    word = txt.trim();
    paint(getGraphics());
  }
}
