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
// External imports
import javax.swing.Icon;
import javax.swing.Action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

// Local imports

/**
 * An action that changes to a specific navigation mode.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class ToolbarAction extends AbstractAction {

    /** The mode */
    private String mode;
    private BaseVolumeViewer viewer;

    /**
     * Create an instance of the action class.
     *
     * @param icon The Icon
     * @param modeString The mode string
     * @param accelKey The accelerator key
     */
    public ToolbarAction(Icon icon, String modeString, KeyStroke accelKey, BaseVolumeViewer viewer) {
        this.viewer = viewer;
        if (icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, modeString);
        }

        this.mode = modeString;

        putValue(ACCELERATOR_KEY, accelKey);
        //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_W));
        putValue(SHORT_DESCRIPTION, modeString + " mode");
    }

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        viewer.setToolbarMode(mode);
    }
}
