import com.symbian.devnet.util.TaskSwitch;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Scrollbar;
import java.awt.SystemColor;
import java.awt.TextField;
import java.awt.Toolkit;

public class JFastReader extends Frame implements ActionListener {

  final static int WND_W=208, WND_H=276;  // initial window size
  final static String APPNAME="jFastReader";
  final static String APPVERSION="1.0";
    
  private static JFastReader jFastReader = null;
  
  static Font ftPlain8 = new Font("Dialog", Font.PLAIN, 8);
  static Font ftPlain10 = new Font("Dialog", Font.PLAIN, 10);
  static Font ftBold12 = new Font("Dialog", Font.BOLD, 12);
  
  static String curFile = "";
  
  static Panel pnMain = new Panel(new BorderLayout());

  static Panel pnTop = new Panel(new FlowLayout(FlowLayout.CENTER,1,1));
  static Panel pnBottom = new Panel(new BorderLayout());
  static Panel pnBottom2 = new Panel(new GridLayout(3,2,2,2));
  static Panel pnSpeed = new Panel(new BorderLayout());
  static Panel pnOptBook = new Panel(new GridLayout(1,2,1,1));
  static Panel pnSlowFast = new Panel(new GridLayout(1,2,1,1));
  static Panel pnBackForw = new Panel(new GridLayout(1,2,1,1));
  static Panel pnReadPaus = new Panel(new GridLayout(1,2,1,1));
  static Panel pnReptButt = new Panel(new GridLayout(1,2,1,1));
  static Panel pnButtons = new Panel(new GridLayout(2,1,1,1));

  static Label lbGo = new Label("Word:", Label.RIGHT);
  static TextField tfGo = new TextField("", 4);
  static Button btGo = new Button("Go");
  static Button btAbout = new Button("About");
  static Button btText = new Button("Text");
  
  static MyCanvas cvFR = new MyCanvas();
  
  static Scrollbar sbProgress = new Scrollbar(Scrollbar.HORIZONTAL, 0, 0, 0, 0);
  
  static Label lbSpeedTag = new Label("Speed (wpm.)", Label.LEFT);
  static MyProgBar cvSpeed = new MyProgBar();
  static Label lbSpeed = new Label("---", Label.LEFT);
  
  static Button btOptions = new Button("Options");
  static Button btBookmark = new Button("Bookmark");
  
  static Button btSlower = new Button("<<\nSlower");
  static Button btFaster = new Button(">>\nFaster");
  static Button btBack = new Button("<<\nBKWD");
  static Button btForw = new Button(">>\nFWD");
  
  static Button btRead = new Button("READ");
  static Button btPause = new Button("PAUSE");
  static Button btRepeat = new Button("REPEAT");
  
  static Button btLoad = new Button("Load File");
  static Button btQuit = new Button("Exit");


  transient Dialog diAbout = new Dialog(this, "About...", true);
  static Panel pnAboutText = new Panel(new GridLayout(0,1,0,0));
  static Panel pnAboutButt = new Panel(new FlowLayout(FlowLayout.RIGHT));
  static Label lbAbout1 = new Label(APPNAME, Label.CENTER);
  static Label lbAbout2 = new Label("by Markus Birth", Label.CENTER);
  static Label lbAbout3 = new Label("mbirth@webwriters.de", Label.CENTER);
  static Label lbAbout4 = new Label("http://www.webwriters.de/mbirth/", Label.CENTER);
  static Button btOk = new Button("OK");

  static Dimension dmScreen = Toolkit.getDefaultToolkit().getScreenSize();  // get Screen dimensions

  transient Dialog diInfo = new Dialog(this, "infoPrint", false);
  static Label lbInfo = new Label();
  transient InfoPrintThread thIPT = new InfoPrintThread();

  private class MainWindowAdapter extends WindowAdapter {
    public void windowClosing(WindowEvent we) {
      // TODO: Insert commands to execute upon shutdown
      System.out.println("Received windowClosing event... application shutting down.");
      infoPrint("Goodbye...");
      System.out.println("Initiated InfoPrint...");
      try {
        while (thIPT.isAlive()) {
          thIPT.setStopnow(true);
          Thread.sleep(100);
        }
      } catch (InterruptedException exIE) {
        System.out.println("Sleeping won't work. "+exIE.toString());
        exIE.printStackTrace();
      }
      System.out.println("InfoPrint stopped.");
      System.exit(0);
    }
  }
  
  private class DummyWindowAdapter extends WindowAdapter {
    public void windowActivated(WindowEvent we) {
    }
    
    public void windowClosing(WindowEvent we) {
      if (we.getSource().equals(diAbout)) {
        // System.out.println("About manually closed.");
        diAbout.setVisible(false);
        jFastReader.dispatchEvent(new WindowEvent(jFastReader, 201));
      }
    }
  }
  
  // handler for ActionListener
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource().equals(btOk)) {     // OK button on AboutScreen
      System.out.println("The user clicked OK. Trying to close the dialog.");
      diAbout.setVisible(false);
    } else if (ae.getSource().equals(btAbout)) {  // About-Button
      System.out.println("The user clicked 'About' button.");
      diAbout.setVisible(true);
      btOk.requestFocus();
    } else if (ae.getSource().equals(btQuit)) {          // Quit-Button
      System.out.println("Exit button clicked. Sending event...");
      dispatchEvent(new WindowEvent(this, 201));
    } else if (ae.getSource().equals(btLoad)) {           // Load-Button
      System.out.println("Show load-dialog...");
      FileDialog fdLoad = new FileDialog(jFastReader, "Load file", FileDialog.LOAD);
      fdLoad.show();
      System.out.println("Directory: "+fdLoad.getDirectory());
      System.out.println("File: "+fdLoad.getFile());
      curFile = fdLoad.getDirectory() + fdLoad.getFile();
    }
    // TODO: more events
  }

  public JFastReader() {                // Constructor
    addWindowListener(new MainWindowAdapter());
    setTitle(APPNAME+" "+APPVERSION+" by Markus Birth");  // set Frame title
    setResizable(false);
    setSize(WND_W, WND_H);   // set Frame size
    setLocation((dmScreen.width-WND_W)/2, (dmScreen.height-WND_H)/2); // center Frame
    infoPrint(APPNAME+" loading...");
    doAbout();
    
    btAbout.addActionListener(this);
    btLoad.addActionListener(this);
    btQuit.addActionListener(this);
    
    pnMain.setFont(ftPlain8);
    btGo.setFont(ftPlain8);
    btAbout.setFont(ftPlain8);
    btText.setFont(ftPlain8);
    
    pnTop.add(lbGo);
    pnTop.add(tfGo);
    pnTop.add(btGo);
    pnTop.add(btAbout);
    pnTop.add(btText);
    
    pnSpeed.add(lbSpeedTag, BorderLayout.NORTH);
    pnSpeed.add(cvSpeed, BorderLayout.CENTER);
    pnSpeed.add(lbSpeed, BorderLayout.EAST);
    
    pnOptBook.add(btOptions);
    pnOptBook.add(btBookmark);
    
    pnSlowFast.add(btSlower);
    pnSlowFast.add(btFaster);
    
    pnBackForw.add(btBack);
    pnBackForw.add(btForw);
    
    pnReadPaus.add(btRead);
    pnReadPaus.add(btPause);
    
    pnButtons.add(btLoad);
    pnButtons.add(btQuit);
    
    pnReptButt.add(btRepeat);
    pnReptButt.add(pnButtons);
    
    pnBottom2.add(pnSpeed);
    pnBottom2.add(pnOptBook);
    pnBottom2.add(pnSlowFast);
    pnBottom2.add(pnBackForw);
    pnBottom2.add(pnReadPaus);
    pnBottom2.add(pnReptButt);
    
    pnBottom.add(sbProgress, BorderLayout.CENTER);
    pnBottom.add(pnBottom2, BorderLayout.SOUTH);
    
    pnMain.add(pnTop, BorderLayout.NORTH);
    pnMain.add(cvFR, BorderLayout.CENTER);
    pnMain.add(pnBottom, BorderLayout.SOUTH);
    
    add(pnMain);

    btOk.addActionListener(this);
    
    // TODO: more initialization commands
    show(); // automagically calls paint(Graphics g)
    diInfo.setVisible(false); // init done, hide infoPrint
  }
  
  public static void main(String args[]) {
    try {
      jFastReader = new JFastReader();
    } catch (Exception ex) {
      System.out.println("Caught exception: "+ex.toString());
      ex.printStackTrace();
      System.exit(1);
    }
  }
  
  public final void doAbout() {
    pnAboutButt.add(btOk);
    lbAbout1.setFont(ftBold12);
    lbAbout2.setFont(ftPlain10);
    lbAbout3.setFont(ftPlain10);
    lbAbout4.setFont(ftPlain10);
    diAbout.setLayout(new BorderLayout());
    diAbout.setBackground(SystemColor.control);
    pnAboutText.add(lbAbout1);
    pnAboutText.add(lbAbout2);
    pnAboutText.add(lbAbout3);
    pnAboutText.add(lbAbout4);
    diAbout.add(pnAboutText, "North");
    diAbout.add(pnAboutButt, "South");
    diAbout.addWindowListener(new DummyWindowAdapter());
    diAbout.pack(); // without it, the Dialog won't get displayed!!
    Dimension dmAboutBox = diAbout.getSize();
    Dimension dmWindow = this.getSize();
    Point ptWindow = this.getLocation();
    diAbout.setLocation(ptWindow.x+(dmWindow.width-dmAboutBox.width)/2, ptWindow.y+dmWindow.height-dmAboutBox.height);
  }
  
  private static class MyCanvas extends Canvas {
    static Color bgColor = new Color(0,0,128);
    static Color fgColor = new Color(255,255,0);
    static String word = "";
    static int maxFontSize = 100;
    
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
          fs -= 2;
        } while ((fm.stringWidth(word)>getSize().width-2) || (fm.getHeight()>getSize().height-2));
        g.setFont(f);
        g.drawString(word, getSize().width/2-fm.stringWidth(word)/2, getSize().height/2-fm.getHeight()/2+fm.getAscent());
      }
    }
    
    public void setWord(String txt) {
      word = txt;
      repaint();
    }
  }
  
  private static class MyProgBar extends Canvas {
    static int minVal = 0;
    static int maxVal = 100;
    static int curVal = 50;
    static Color fgColor = new Color(0,0,128);
    static Color bgColor = Color.lightGray;
    static Color txColor = Color.white;
    
    public void paint(Graphics g) {
      double percentage = (double)(curVal-minVal)/(double)(maxVal-minVal);
      String percString = Double.toString(percentage*100) + "%";
      g.setFont(ftPlain10);
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
      repaint();
    }
  }
  
  // waits 3 seconds and then hides the diInfo-Dialog
  private class InfoPrintThread extends Thread {
    private transient boolean stopnow;
    
    public void run() {
      this.stopnow = false;
      try {
        // System.out.println("IPT: started.");
        long dtStart = System.currentTimeMillis();
        while (System.currentTimeMillis()<dtStart+3000 && !this.stopnow) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException exIE) {
            System.out.println("Caught "+exIE.toString());
            exIE.printStackTrace();
          }
        }
        diInfo.setVisible(false);
        // System.out.println("IPT: completed.");
        return;
      } catch (Exception ex) {
        System.out.println("Exception in IPT: "+ex.toString());
        ex.printStackTrace();
      }
    }
    
    public void setStopnow(boolean x) {
      this.stopnow = x;
    }
  }

  public final void infoPrint(String txt) {
    if (System.getProperty("os.name").equals("EPOC")) {
      TaskSwitch.infoPrint(txt);
    } else {
      while (thIPT.isAlive()) {
        thIPT.setStopnow(true);
        try {
          Thread.sleep(100);
        } catch (InterruptedException exIE) {
          System.out.println("Caught "+exIE.toString());
          exIE.printStackTrace();
        }
      }
      diInfo.add(lbInfo);
      lbInfo.setText(txt);
      diInfo.pack();
      Dimension dmWindow = this.getSize();
      Point ptWindow = this.getLocation();
      Dimension dmInfo = diInfo.getSize();
      // System.out.println("Window is at "+ptWindow.x+"|"+ptWindow.y+" and "+dmWindow.width+"x"+dmWindow.height);
      // System.out.println("InfoPrint is "+dmInfo.width+"x"+dmInfo.height);
      Point ptInfo = new Point();
      ptInfo.x = ptWindow.x+dmWindow.width-dmInfo.width;
      ptInfo.y = ptWindow.y;
      // System.out.println("InfoPrint will be positioned at "+ptInfo.x+"|"+ptInfo.y);
      diInfo.setLocation(ptInfo);
      diInfo.repaint();
      diInfo.show();
      // System.out.println("infoPrint: "+txt);
      thIPT = new InfoPrintThread();
      thIPT.start();
    }
  }
}
