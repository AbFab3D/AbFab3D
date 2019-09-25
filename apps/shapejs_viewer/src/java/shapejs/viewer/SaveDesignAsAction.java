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
public class SaveDesignAsAction extends AbstractAction {

    private static final String LAST_SAVE_DIR = "LastSaveDir";

    private BaseVolumeViewer viewer;

    JFileChooser m_fileDialog;
    Dimension m_fileDialogSize = new Dimension(500, 500);


    /**
     * Create an instance of the action class.
     */
    public SaveDesignAsAction(BaseVolumeViewer viewer) {
        super("Save As...");

        this.viewer = viewer;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        putValue(SHORT_DESCRIPTION, "Save Design into a File");

    }


    /**
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {

        if(m_fileDialog == null){

            Preferences prefs = Preferences.userNodeForPackage(BaseVolumeViewer.class);
            String lastDir = prefs.get(LAST_SAVE_DIR, null);            
            if(lastDir == null) lastDir = "";
            m_fileDialog = new JFileChooser(lastDir);            
            m_fileDialog.setFileFilter(new ShapeJSFileFilter());
        }

        m_fileDialog.setPreferredSize(m_fileDialogSize);
        int returnVal = m_fileDialog.showSaveDialog(viewer);
        m_fileDialogSize = m_fileDialog.getSize();
        String dir = m_fileDialog.getCurrentDirectory().getAbsolutePath();
        Preferences.userNodeForPackage(BaseVolumeViewer.class).put(LAST_SAVE_DIR, dir);
        
        if (returnVal != JFileChooser.APPROVE_OPTION) 
            return;
            
        File file = m_fileDialog.getSelectedFile();            
        try {
            viewer.onSaveDesignAs(file.getPath());                     
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
