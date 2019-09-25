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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

// Standard library imports
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import static abfab3d.core.Output.printf;

/**
 * An action that can be used to exit the system.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class ExitAction extends AbstractAction {
    private BaseVolumeViewer viewer;

    /**
     * Create an instance of the action class.
     */
    public ExitAction(BaseVolumeViewer viewer) {
        super("Exit");

        this.viewer = viewer;
        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_X,
                KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
        putValue(SHORT_DESCRIPTION, "Exit the viewer");
    }

    /**
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        viewer.shutdownApp();

        try { Thread.sleep(50); } catch(Exception e) {}
        System.exit(0);
    }
}
