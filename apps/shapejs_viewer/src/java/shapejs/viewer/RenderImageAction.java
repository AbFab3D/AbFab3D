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

import org.apache.commons.io.FilenameUtils;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Dimension;
import java.awt.Point;

// Standard library imports
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;

import java.util.prefs.Preferences;
import java.io.File;

import abfab3d.param.ParamMap;
import abfab3d.param.IntParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.URIParameter;
import abfab3d.param.Parameter;
import abfab3d.param.editor.ParamDialog;


import static abfab3d.core.Output.printf;

/**
 * An action to render grid and save grid to a file
 * <p>
 *
 * @author Vladimir Bulatov
 */
public class RenderImageAction extends AbstractAction {


    static final String EXT_PNG = BaseVolumeViewer.EXT_PNG;

    URIParameter mp_path = new URIParameter("path", "");
    IntParameter mp_imageWidth = new IntParameter("width", 500);
    IntParameter mp_imageHeight = new IntParameter("height", 500);
    Parameter m_params[] = {
        mp_path,
        mp_imageWidth,
        mp_imageHeight
    };


    private BaseVolumeViewer viewer;

    /** The last directory property */
    private static final String LASTOUTDIR_PROPERTY = "OutputDirectory";


    /** The dialog used to select the file to open */
    private JFileChooser m_fc;

    /**
     * Create an instance of the action class.
     */
    public RenderImageAction(BaseVolumeViewer viewer) {
        super("Save Image");

        this.viewer = viewer;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_S,
                KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(SHORT_DESCRIPTION, "save image to a file");

        String dir = null;

        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        
        String last_dir = prefs.get(LASTOUTDIR_PROPERTY, null);
        String user_dir = System.getProperty("user.dir");
        
        if (last_dir != null)
            dir = last_dir;
        else
            dir = user_dir;
                
        m_fc = new JFileChooser(dir);

    }

    /**
     *
     * @param evt The event that caused this method to be called.
     */
    public void _actionPerformed(ActionEvent evt) {

        int imageWidth = 15000;
        int imageHeight = 15000;


        int returnVal = m_fc.showDialog(viewer,"Save Image to a File");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
            File file = m_fc.getSelectedFile();
            
            String dir = file.getPath();
            
            int idx = dir.lastIndexOf(File.separator);

            if (idx > 0) {
                dir = dir.substring(0, idx);

                Preferences prefs = Preferences.userNodeForPackage(this.getClass());

                prefs.put(LASTOUTDIR_PROPERTY, dir);
            }

            String path = file.getPath();
            if (FilenameUtils.getExtension(path).equals("")) {
                path += ".png";
            }
            viewer.onRenderImage(path, imageWidth, imageHeight);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        
        mp_path.setValue(viewer.getDesign().getPathNoExt()+EXT_PNG);

        ParamDialog dialog = new ParamDialog(viewer, "Save Image", m_params);
        
        dialog.setModal(true);
        
        Dimension dim = viewer.getSize();
        Point pnt = viewer.getLocationOnScreen();
        pnt.x += dim.width/2;
        dialog.setLocation(pnt.x, pnt.y);
        dialog.setVisible(true);
        
        if(dialog.getResult()){
            viewer.onRenderImage(mp_path.getValue(), mp_imageWidth.getValue(), mp_imageHeight.getValue());
        } 
        
    }

}
