import com.symbian.devnet.util.TaskSwitch;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Point;

public class MyInfoPrint {
  Frame parent;
  Dialog diInfo;
  Label lbInfo = new Label();
  InfoPrintThread thIPT = new InfoPrintThread();
  
  public MyInfoPrint(Frame p) {
    parent = p;
    diInfo = new Dialog(parent, "infoPrint", false);
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
  
  private final void doBox(String txt) {
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
    Dimension dmWindow = parent.getSize();
    Point ptWindow = parent.getLocation();
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
  }
  
  public final void busy(String txt) {
    if (System.getProperty("os.name").equals("EPOC")) {
      TaskSwitch.infoPrint(txt);
    } else {
      doBox(txt);
    }
  }
  
  public final void hide() {
    while (thIPT.isAlive()) {
      thIPT.setStopnow(true);
      try {
        Thread.sleep(100);
      } catch (InterruptedException exIE) {
        System.out.println("Caught "+exIE.toString());
        exIE.printStackTrace();
      }
    }
    diInfo.setVisible(false);
  }

  public final void infoPrint(String txt) {
    if (System.getProperty("os.name").equals("EPOC")) {
      TaskSwitch.infoPrint(txt);
    } else {
      doBox(txt);
      // System.out.println("infoPrint: "+txt);
      thIPT = new InfoPrintThread();
      thIPT.start();
    }
  }
}
