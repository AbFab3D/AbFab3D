package abfab3d.param.editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

/**
 * Manage editor windows
 *
 * @author Alan Hudson
 */
public class WindowManager implements WindowListener {
    private static int lastY;
    private ArrayList<ParamPanel> panels;
    private static WindowManager manager;
    private Image icon;

    private WindowManager() {
        panels = new ArrayList<ParamPanel>();

        icon = new ImageIcon("classes/images/shapejs_icon_32.png").getImage();

    }

    public static WindowManager getInstance() {
        if (manager != null) return manager;

        manager = new WindowManager();

        return manager;
    }

    public void addPanel(ParamPanel p) {
        if (panels.size() == 0) {
            p.setCloseAllowed(false);
        } else {
            p.setCloseAllowed(true);
        }
        panels.add(p);

        lastY += p.getHeight();
        p.addWindowListener(this);
        p.setIconImage(icon);

    }

    public void closeAll() {
        for(ParamPanel p : panels) {
            p.setCloseAllowed(true);
            ((Frame)p).dispatchEvent(new WindowEvent(p, WindowEvent.WINDOW_CLOSING));
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
        ParamPanel panel = (ParamPanel) e.getWindow();

        if (panel.isCloseAllowed()) {
            e.getWindow().dispose();

            /*
            lastY = 0;
            for(ParamPanel p : panels) {
                if (p.getHeight() > lastY) {
                    lastY =
                }
            }
            */
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
