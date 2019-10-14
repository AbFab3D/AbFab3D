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

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action that can be used to change whether to render HQ after a delay
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class HQRenderAction extends AbstractAction {
    /** The status bar */
    protected StatusBar statusBar;
    protected RenderCanvas canvas;

    /**
     * Create an instance of the action class.
     *
     */
    public HQRenderAction(RenderCanvas canvas, StatusBar statusBar) {
        super("");

        this.canvas = canvas;
        this.statusBar = statusBar;

        putValue(SHORT_DESCRIPTION, "HQ Render");
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

        if (val.equalsIgnoreCase("TRUE")) {
            canvas.setHQRendering(true);
        } else {
            canvas.setHQRendering(false);
        }

        canvas.forceRender();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

}
