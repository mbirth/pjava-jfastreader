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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class JFastReader extends Frame implements ActionListener {

  final int WND_W=208, WND_H=276;  // initial window size
  final String APPNAME="jFastReader";
  final String APPVERSION="1.0";
    
  private static JFastReader jFastReader = null;
  
  Font ftPlain8 = new Font("Dialog", Font.PLAIN, 8);
  Font ftPlain10 = new Font("Dialog", Font.PLAIN, 10);
  Font ftBold12 = new Font("Dialog", Font.BOLD, 12);
  
  File curFile;
  int curWord = 1;
  int maxWord = 1;
  MySeeker seeker = new MySeeker();
  
  Panel pnMain = new Panel(new BorderLayout());

  Panel pnTop = new Panel(new BorderLayout());
  Panel pnTopLeft = new Panel(new FlowLayout(FlowLayout.LEFT,1,1));
  Panel pnTopRight = new Panel(new FlowLayout(FlowLayout.RIGHT,1,1));
  Panel pnBottom = new Panel(new BorderLayout());
  Panel pnBottom2 = new Panel(new GridLayout(3,2,2,1));
  Panel pnSpeed = new Panel(new BorderLayout());
  Panel pnOptBook = new Panel(new GridLayout(1,2,1,1));
  Panel pnSlowFast = new Panel(new GridLayout(1,2,1,1));
  Panel pnBackForw = new Panel(new GridLayout(1,2,1,1));
  Panel pnReadPaus = new Panel(new GridLayout(1,2,1,1));
  Panel pnReptButt = new Panel(new GridLayout(1,2,1,1));
  Panel pnButtons = new Panel(new GridLayout(2,1,1,1));

  Label lbGo = new Label("Word:", Label.RIGHT);
  TextField tfGo = new TextField("", 4);
  Button btGo = new Button("Go");
  Button btAbout = new Button("?");
  Button btText = new Button("Go Text");
  
  MyCanvas cvFR = new MyCanvas();
  
  Scrollbar sbProgress = new Scrollbar(Scrollbar.HORIZONTAL, 0, 0, 0, 0);
  
  Label lbSpeedTag = new Label("Speed (wpm.)", Label.LEFT);
  MyProgBar cvSpeed = new MyProgBar();
  Label lbSpeed = new Label("---", Label.LEFT);
  
  Button btOptions = new Button("Opt.");
  Button btBookmark = new Button("Bkmk");
  
  Button btSlower = new Button("<<\nSlwr");
  Button btFaster = new Button(">>\nFstr");
  Button btBack = new Button("<\nBKW");
  Button btForw = new Button(">\nFWD");
  
  Button btRead = new Button(">\nREAD");
  Button btPause = new Button("||\nPAUS");
  Button btRepeat = new Button("REPT");
  
  Button btLoad = new Button("Load");
  Button btQuit = new Button("Exit");


  Dialog diAbout = new Dialog(this, "About...", true);
  Panel pnAboutText = new Panel(new GridLayout(0,1,0,0));
  Panel pnAboutButt = new Panel(new FlowLayout(FlowLayout.RIGHT));
  Label lbAbout1 = new Label(APPNAME, Label.CENTER);
  Label lbAbout2 = new Label("by Markus Birth", Label.CENTER);
  Label lbAbout3 = new Label("mbirth@webwriters.de", Label.CENTER);
  Label lbAbout4 = new Label("http://www.webwriters.de/mbirth/", Label.CENTER);
  Button btOk = new Button("OK");

  Dimension dmScreen = Toolkit.getDefaultToolkit().getScreenSize();  // get Screen dimensions

  MyInfoPrint MIP = new MyInfoPrint(this);

  private class MainWindowAdapter extends WindowAdapter {
    public void windowClosing(WindowEvent we) {
      // TODO: Insert commands to execute upon shutdown
      MIP.infoPrint("Goodbye...");
      MIP.hide();
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
      FileDialog fdLoad = new FileDialog(jFastReader, "Load file", FileDialog.LOAD);
      fdLoad.show();
      if (fdLoad.getFile() != null) {
        System.out.println("Directory: "+fdLoad.getDirectory());
        System.out.println("File: "+fdLoad.getFile());
        curFile = new File(fdLoad.getDirectory() + fdLoad.getFile());
        seeker = new MySeeker();
        curWord = 1;
        maxWord = 1;
        getWord(1);
        showWord(curWord);
      } else {
        MIP.infoPrint("Cancelled.");
      }
    } else if (ae.getSource().equals(btBack)) {
      System.out.println("<<< BACK");
      if (curWord > 1) {
        showWord(--curWord);
      }
    } else if (ae.getSource().equals(btForw)) {
      System.out.println(">>> FORW");
      if (curWord < maxWord) {
        showWord(++curWord);
      }
    }
    // TODO: more events
  }
  
  private String[] splitString(String str, String delim) {
    str = str.trim();
    int i = 1;
    int lastIndex = 0;
    int idx;
    // System.out.println("sS: Input string is >"+str+"<, delimiter >"+delim+"<");
    // System.out.println("sS: Counting words...");
    while ((idx=str.indexOf(delim, lastIndex)) >= 0) {
      i++;
      lastIndex = idx+1;
    }
    // System.out.println("sS: Counted "+i+" words.");
    String[] result = new String[i];
    lastIndex = 0;
    i = 0;
    while ((idx=str.indexOf(delim, lastIndex)) >= 0) {
      result[i] = str.substring(lastIndex, idx);
      // System.out.println("sS: Word #"+i+" >"+result[i]+"<");
      lastIndex = idx+1;
      i++;
    }
    result[i] = str.substring(lastIndex);
    // System.out.println("sS: Word #"+i+" >"+result[i]+"<");
    return result;
  }
  
  public String getWord(int w) {
    String result = "";
    if (curFile != null && curFile.exists() && curFile.isFile()) {
      try {
        RandomAccessFile f = new RandomAccessFile(curFile, "r");
        String tmp;
        long lastPos = 0;
        int wordcount = 1;
        if (seeker.len <= 0) {
          // get wordcounts
          MIP.infoPrint("Indexing...");
          while ((tmp = f.readLine()) != null) {
            // System.out.println("Read line: >" + tmp + "<");
            String[] words = splitString(tmp, " ");
            // System.out.println("Setting index for word #"+wordcount+" to pos. "+lastPos);
            seeker.add(wordcount, lastPos);
            wordcount += words.length;
            // System.out.println("New wordcount is now "+wordcount);
            lastPos = f.getFilePointer();
          }
          maxWord = --wordcount;
          // System.out.println("Words at all: "+maxWord);
        }
        long[] seekpos = seeker.getSeekForWord(w);
        f.seek(seekpos[1]);
        String myLine = f.readLine();
        int indexl = 0;
        while (seekpos[0]<w && myLine.indexOf(" ", indexl)>0) {
          // System.out.println("Need word: "+w+"; cur: "+seekpos[0]);
          indexl = myLine.indexOf(" ", indexl+1)+1;
          seekpos[0]++;
        }
        // System.out.println("Need word: "+w+"; got: "+seekpos[0]);
        int indexr = myLine.indexOf(" ", indexl+1);
        if (indexr <= 0) { indexr = myLine.length(); }
        result = myLine.substring(indexl, indexr);
        f.close();
      } catch (IOException ioe) {
        System.out.println("IOException while reading file: "+ioe.toString());
        MIP.infoPrint("File IO error");
      }
    }
    return result;
  }
  
  private void showWord(int w) {
    String wrd = getWord(w);
    cvFR.setWord(wrd);
  }

  public JFastReader() {                // Constructor
    addWindowListener(new MainWindowAdapter());
    setTitle(APPNAME+" "+APPVERSION+" by Markus Birth");  // set Frame title
    //setResizable(false);
    setSize(WND_W, WND_H);   // set Frame size
    setLocation((dmScreen.width-WND_W)/2, (dmScreen.height-WND_H)/2); // center Frame
    MIP.infoPrint(APPNAME+" loading...");
    doAbout();
    
    btAbout.addActionListener(this);
    btLoad.addActionListener(this);
    btQuit.addActionListener(this);
    btBack.addActionListener(this);
    btForw.addActionListener(this);
    
    pnMain.setFont(ftPlain8);
    
    pnTopLeft.add(lbGo);
    pnTopLeft.add(tfGo);
    pnTopLeft.add(btGo);
    pnTopRight.add(btAbout);
    pnTopRight.add(btText);
    pnTop.add(pnTopLeft, BorderLayout.WEST);
    pnTop.add(pnTopRight, BorderLayout.EAST);
    
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
    MIP.hide(); // init done, hide infoPrint
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
  
  private class MySeeker {
    int[] poses = new int[0];
    long[] seeker = new long[0];
    int len = 0;
    
    public void add(int word, long pos) {
      int[] newp = new int[len+1];
      long[] news = new long[len+1];
      for (int i=0;i<len;i++) {
        newp[i] = poses[i];
        news[i] = seeker[i];
      }
      newp[len] = word;
      news[len] = pos;
      poses = newp;
      seeker = news;
      len++;
    }
    
    public long[] getSeekForWord(int word) {
      // System.out.println("gSFW: Seek word #"+word);
      long[] result = new long[2];
      if (len>0) {
        int i=0;
        while (i<len && poses[i]<=word) {
          // System.out.println("gSFW: "+i+" Curwordindex: "+poses[i]+" / Seek: "+seeker[i]);
          i++;
        }
        if (i>0) { i--; }
        // System.out.println("gSFW: Nearest word is #"+poses[i]+" at pos. "+seeker[i]);
        result[0] = poses[i];
        result[1] = seeker[i];
      } else {
        System.out.println("gSFW: No data.");
        result[0] = 0;
        result[1] = 0;
      }
      return result;
    }
  }

}
