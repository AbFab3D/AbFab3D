/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapejs.viewer;

import abfab3d.shapejs.RenderStat;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

import static abfab3d.core.Output.fmt;


/**
 * A panel that implements a simple status bar capability with a
 * text readout and frames per second counter.
 * <p>
 *
 * @author Alan Hudson
 */
public class StatusBar extends JPanel implements Runnable {

    /**
     * Default properties object
     */
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    /**
     * The label for status messages
     */
    private JLabel statusLabel;

    /**
     * The last FPS, used to avoid garbage generation.
     */
    private float lastFPS;

    /**
     * Label for frames per second.
     */
    private JLabel fpsLabel;

    /**
     * A progress bar for main file loading
     */
    private JProgressBar progressBar;

    /**
     * The periodic thread updating the status bar.
     */
    private Thread statusThread;

    /**
     * The run state of the statusThread
     */
    private boolean runStatusThread;

    private FPSCounter counter;
    private RenderCanvas canvas;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * as described.
     *
     * @param showStatusBar true to show a status bar
     * @param showFPS       true to show the current FPS
     */
    public StatusBar(RenderCanvas canvas,
                     boolean showStatusBar,
                     boolean showFPS
    ) {
        super(new BorderLayout());

        JPanel rightPanel = new JPanel(new BorderLayout());

        add(rightPanel, BorderLayout.EAST);

        this.canvas = canvas;
        if (showFPS) {
            fpsLabel = new JLabel();
            rightPanel.add(fpsLabel, BorderLayout.EAST);
            statusThread = new Thread(this, "AbFab3D FPS updates");
            runStatusThread = true;
            statusThread.start();
        }

        if (showStatusBar) {
            statusLabel = new JLabel();
            statusLabel.setText("System initializing");
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
        while (runStatusThread) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }


            if (counter == null) {
                counter = canvas.getCounter();
                if (counter == null) continue;
            }

            float fps = counter.getLastFPS();

            RenderStat stats = canvas.getRenderStat();
            if (Math.abs(lastFPS - fps) > 0.1) {

                if (Float.isInfinite(fps)) {
                    fps = 999f;
                }

//                String label = fmt("Kernel: %4d Render: %4d ms transmit: %9.2f recv: %5.1f  FPS: %5.0f", kernel, render, transmittedKBytes, recvKBytes, fps);
                String label = stats.toString(1e-6,"ms", 1e-4,"kb");
                fpsLabel.setText(label);
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
        if (statusLabel != null)
            statusLabel.setText(msg);
    }
}
