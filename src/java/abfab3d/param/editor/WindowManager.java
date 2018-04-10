package abfab3d.param.editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import static abfab3d.core.Output.printf;


/**
 * Manage editor windows
 *
 * @author Alan Hudson
 */
public class WindowManager implements WindowListener {

    static final boolean DEBUG = true;

    private static int lastY;
    private ArrayList<Component> panels;
    private static WindowManager manager;
    private Image icon;

    private WindowManager() {
        panels = new ArrayList<Component>();

        icon = new ImageIcon("classes/images/shapejs_icon_32.png").getImage();

    }

    public static WindowManager getInstance() {
        if (manager != null) return manager;

        manager = new WindowManager();

        return manager;
    }

    public void addPanel(Component p) {
        
        if(DEBUG)printf("WindwoManager.addPanel(%s)\n", p);
            
        ParamPanel panel = null;
        ParamFrame frame = null;
        
        if (p instanceof ParamFrame) {
            panel = ((ParamFrame)p).getPanel();
            frame = ((ParamFrame)p);
        } else if (p instanceof ParamPanel) {
            panel = (ParamPanel) p;
        } else {
            throw new IllegalArgumentException("WindowManager  Invalid type, not a ParamPanel or ParamFrame");
        }

        if (panels.size() == 0) {
            panel.setCloseAllowed(false);
        } else {
            panel.setCloseAllowed(true);
        }
        if(frame != null){
            if(DEBUG)printf("WindowManager adding frame(%s)\n", frame);
            panels.add(frame);
        } else {            
            if(DEBUG)printf("WindoManager adding panel(%s)\n", panel);
            panels.add(panel);
        }

        lastY += p.getHeight();

        if (frame != null) {
            frame.addWindowListener(this);
            frame.setIconImage(icon);
        }
    }

    public void closeAll() {
        if(DEBUG) printf("WindowManager.closeAll()\n");
            
        for(Component p : panels) {

            ParamPanel panel = null;
            ParamFrame frame = null;

            if(DEBUG) printf("WindowManager  component (%s)\n", p);
            if (p instanceof ParamFrame) {
                
                frame = (ParamFrame)p;
                panel = frame.getPanel();
            } else if (p instanceof ParamPanel) {
                panel = (ParamPanel) p;
            }

            if(DEBUG) printf("WindowManager  frame(%s) panel(%s)\n", frame, panel);
            panel.setCloseAllowed(true);
            if (frame != null) {
                if(DEBUG) printf("WindowManager  closing frame (%s)\n", frame);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        }

        panels.clear();
        lastY = 0;
    }

    public int getLastY() {
        return lastY;
    }

    // WindowListener methods

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        ParamFrame panel = (ParamFrame) e.getWindow();

        if (panel.getPanel().isCloseAllowed()) {
            e.getWindow().dispose();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    // end WindowListener methods
}
