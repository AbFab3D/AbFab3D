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

import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;

// Standard library imports

/**
 * An action to render grid and save grid to a file
 * <p>
 *
 * @author Alan Hudson
 */
public class SaveModelAction extends AbstractAction {

    private BaseVolumeViewer viewer;

    /** The last directory property */
    private static final String LASTOUTDIR_PROPERTY = "OutputDirectory";


    /** The dialog used to select the file to open */
    private JFileChooser m_dialog;

    Dimension m_dialogSize = new Dimension(500, 500);
    /**
     * Create an instance of the action class.
     */
    public SaveModelAction(BaseVolumeViewer viewer) {
        super("Save Model");

        this.viewer = viewer;

        //KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_MASK);

        //putValue(ACCELERATOR_KEY, acc_key);
        //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(SHORT_DESCRIPTION, "save model to a file");

        String dir = null;

        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        
        String last_dir = prefs.get(LASTOUTDIR_PROPERTY, null);
        String user_dir = System.getProperty("user.dir");
        
        if (last_dir != null)
            dir = last_dir;
        else
            dir = user_dir;
        
        m_dialog = new JFileChooser(dir);

    }

    /**
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {

        m_dialog.setPreferredSize(m_dialogSize);
        m_dialog.setSelectedFile(new File(viewer.getDesign().getPathNoExt()+".stl"));
        int returnVal = m_dialog.showDialog(viewer,"Save Model to a File");

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
            File file = m_dialog.getSelectedFile();

            m_dialogSize = m_dialog.getSize();
            
            String dir = file.getPath();
            
            int idx = dir.lastIndexOf(File.separator);

                if (idx > 0) {
                    dir = dir.substring(0, idx);

                    Preferences prefs = Preferences.userNodeForPackage(this.getClass());

                    prefs.put(LASTOUTDIR_PROPERTY, dir);
                }
                viewer.onSaveModel(file.getPath());
            }


    }
}
