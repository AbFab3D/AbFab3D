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
import java.awt.Dimension;


// Standard library imports
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;
import java.util.prefs.Preferences;


import java.util.prefs.Preferences;
import java.io.File;




import static abfab3d.core.Output.printf;

/**
 * An action to save design
 * <p>
 *
 * @author Vladimir Bulatov
 */
public class SaveDesignAction extends AbstractAction {

    private BaseVolumeViewer viewer;

    /**
     * Create an instance of the action class.
     */
    public SaveDesignAction(BaseVolumeViewer viewer) {
        super("Save");

        this.viewer = viewer;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(SHORT_DESCRIPTION, "Save Design into a File");

    }


    /**
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        try {
            viewer.saveDesign(); 
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
