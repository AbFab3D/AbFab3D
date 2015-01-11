package viewer;

import org.ietf.uri.ResourceConnection;
import org.ietf.uri.event.ProgressListener;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.xj3d.ui.awt.widgets.SwingProgressListener;

import javax.media.opengl.FPSCounter;
import javax.swing.*;
import java.awt.*;
import java.util.Properties;

import static abfab3d.util.Output.printf;


/**
 * A panel that implements a simple status bar capability with a
 * text readout and frames per second counter.
 * <p>
 *
 * @author Alan Hudson
 */
public class StatusBar extends JPanel implements Runnable  {

    /** Default properties object */
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    /** The label for status messages */
    private JLabel statusLabel;

    /** The last FPS, used to avoid garbage generation. */
    private float lastFPS;

    /** Label for frames per second. */
    private JLabel fpsLabel;

    /** A progress bar for main file loading */
    private JProgressBar progressBar;

    /** The periodic thread updating the status bar. */
    private Thread statusThread;

    /** The run state of the statusThread */
    private boolean runStatusThread;

    private FPSCounter counter;
    private RenderCanvas canvas;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * as described.
     *
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     */
    public StatusBar(RenderCanvas canvas,
                          boolean showStatusBar,
                          boolean showFPS
                          ) {
        super(new BorderLayout());

        JPanel rightPanel = new JPanel(new BorderLayout());

        add(rightPanel, BorderLayout.EAST);

        this.canvas = canvas;
        if(showFPS) {
            fpsLabel = new JLabel();
            rightPanel.add(fpsLabel, BorderLayout.EAST);
            statusThread = new Thread(this, "Xj3D FPS updates");
            runStatusThread = true;
            statusThread.start();
        }

        if(showStatusBar) {
            statusLabel = new JLabel();
            add(statusLabel, BorderLayout.WEST);

            progressBar = new JProgressBar();
        }
    }

    //---------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------

    /**
     * Thread to update frames per second and status bar.
     */
    public void run() {
        while(runStatusThread) {
            try {
                Thread.sleep(500);
            } catch(Exception e) {
            }

            if (counter == null) {
                counter = canvas.getCounter();
                if (counter == null) continue;
            }

            float fps = counter.getLastFPS();

            if(Math.abs(lastFPS - fps) > 0.1) {

                // TODO: Need todo this in a non-garbage generating way
                String txt = Float.toString(fps);
                if (txt.equals("Infinity")) {
                    lastFPS = 999.9f;
                    txt = "999.9";
                }

                int len = txt.length();

                txt = txt.substring(0, Math.min(5,len));
                if (len < 5) {
                    len = 5 - len;
                    for(int i=0; i < len; i++) {
                        txt += " ";
                    }
                }

                fpsLabel.setText("FPS: " + txt);
                lastFPS = fps;
            }
        }
    }

    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------

    /**
     * Update the status bar text message to say this.
     *
     * @param msg The message to display
     */
    public void setStatusText(String msg) {
        if(statusLabel != null)
            statusLabel.setText(msg);
    }
}
