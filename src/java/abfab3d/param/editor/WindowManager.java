package abfab3d.param.editor;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * Manage editor windows
 *
 * @author Alan Hudson
 */
public class WindowManager {
    private static int lastY;
    private ArrayList<ParamPanel> panels;
    private static WindowManager manager;

    private WindowManager() {
        panels = new ArrayList<ParamPanel>();
    }

    public static WindowManager getInstance() {
        if (manager != null) return manager;

        manager = new WindowManager();

        return manager;
    }

    public void addPanel(ParamPanel p) {
        panels.add(p);

        lastY += p.getHeight();
        abfab3d.util.Output.printf("lastY: %d\n",lastY);
    }

    public void closeAll() {
        for(ParamPanel p : panels) {
            ((Frame)p).dispatchEvent(new WindowEvent(p, WindowEvent.WINDOW_CLOSING));
        }

        panels.clear();
    }

    public int getLastY() {
        return lastY;
    }
}
