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
import javax.swing.JFileChooser;

import java.util.prefs.Preferences;
import java.io.File;


import static abfab3d.core.Output.printf;

/**
 * An action to clear cache 
 * <p>
 *
 * @author Vladimir Bulatov
 */
public class ClearCacheAction extends AbstractAction {

    private BaseVolumeViewer viewer;

    /**
     * Create an instance of the action class.
     */
    public ClearCacheAction(BaseVolumeViewer viewer) {
        super("Clear Cache");

        this.viewer = viewer;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        putValue(SHORT_DESCRIPTION, "Clear Cache");

    }

    /**
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {

        viewer.onClearCache(); 

    }
}
