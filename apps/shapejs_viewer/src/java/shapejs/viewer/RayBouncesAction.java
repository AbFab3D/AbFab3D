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

// Standard library imports

import abfab3d.shapejs.RenderOptions;

import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 * An action that can be used to change the number of evaluation steps
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class RayBouncesAction extends AbstractAction {
    /** The status bar */
    protected StatusBar statusBar;

    protected RenderCanvas canvas;
    private int numBounces;
    private static final int maxBounces = 8;

    /**
     * Create an instance of the action class.
     *
     */
    public RayBouncesAction(RenderCanvas canvas, StatusBar statusBar) {
        super("");

        this.canvas = canvas;
        this.statusBar = statusBar;

        putValue(SHORT_DESCRIPTION, "Cycles the eval steps");

        numBounces = getDefaultNumberOfBounces();
    }

    //---------------------------------------------------------------
    // Methods defined by ActionListener
    //---------------------------------------------------------------

    /**
     * An action has been performed. This is the result of ALT-A being
     * selected to cycle through antialiasing sample rates that the
     * graphics card can support through cycling AA off.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        String val = evt.getActionCommand();

        if (val.equals("Disabled")) {
            numBounces = 0;
        } else if (val.equals("Cycle")) {
            cycleSteps();
            return;
        } else {
            numBounces = Integer.parseInt(val);
        }

        changeBounces();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    public static int getDefaultNumberOfBounces() {
        return 0;
    }

    /**
     * Get the maximum number of samples.
     *
     * @return the maximum number of samples
     */
    public static int getMaximumNumberOfBounces() {
        return maxBounces;
    }

    /**
     * Set the desired steps.  This will be capped at the current system maximum.
     *
     * @param desired The desired number of samples
     */
    public void setDesiredSteps(int desired) {
        if (desired > maxBounces)
            numBounces = maxBounces;
        else
            numBounces = desired;

        changeBounces();
    }

    /**
     * Cycle through antialiasing options.
     *
     */
    private void cycleSteps() {
        numBounces++;
        if (numBounces > maxBounces)
            numBounces = 1;

        changeBounces();
    }

    /**
     * Change to the current numSteps.
     */
    private void changeBounces() {
        statusBar.setStatusText("MaxRayBounces: " + numBounces + " out of max: " + maxBounces);

        RenderOptions ro = canvas.getStillRenderOptions();
        ro.rayBounces = numBounces;
        canvas.forceRender();
    }
}
