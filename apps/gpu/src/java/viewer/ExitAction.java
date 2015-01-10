/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

// Standard library imports
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

/**
 * An action that can be used to exit the system.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class ExitAction extends AbstractAction {

    /**
     * Create an instance of the action class.
     */
    public ExitAction() {
        super("Exit");

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_X,
                KeyEvent.ALT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
        putValue(SHORT_DESCRIPTION, "Exit the viewer");
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
        System.exit(0);
    }
}
