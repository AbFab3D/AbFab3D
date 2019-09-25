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

import abfab3d.shapejs.Quality;
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
public class ShadowQualityAction extends AbstractAction {
    /** The status bar */
    protected StatusBar statusBar;

    protected RenderCanvas canvas;

    /**
     * Create an instance of the action class.
     *
     */
    public ShadowQualityAction(RenderCanvas canvas, StatusBar statusBar, Quality defVal) {
        super("");

        this.canvas = canvas;
        this.statusBar = statusBar;

        putValue(SHORT_DESCRIPTION, "Rendering quality");
        RenderOptions ro = canvas.getStillRenderOptions();
        ro.shadowQuality = defVal;
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
        Quality quality = Quality.valueOf(val);

        RenderOptions ro = canvas.getStillRenderOptions();
        ro.shadowQuality = quality;

        statusBar.setStatusText("Shadow quality set to: " + val);
        canvas.forceRender();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

}
