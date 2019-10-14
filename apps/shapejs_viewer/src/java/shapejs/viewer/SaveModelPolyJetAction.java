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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.io.File;
import java.util.prefs.Preferences;

import abfab3d.param.ParamMap;
import abfab3d.param.IntParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.URIParameter;
import abfab3d.param.Parameter;
import abfab3d.param.editor.ParamDialog;
import abfab3d.param.editor.ParamPanel;

import abfab3d.io.output.PolyJetWriter;

import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;

// Standard library imports

/**
 * An action to render grid and save grid to a file
 * <p>
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class SaveModelPolyJetAction extends AbstractAction {


    static final boolean DEBUG = true;
    static String materialNames[] = PolyJetWriter.sm_smaterials;
    static final String mappingNames[] = PolyJetWriter.sm_mappingNames;


    URIParameter mp_path = new URIParameter("path", "");
    EnumParameter mp_material0 = new EnumParameter("material0", materialNames, PolyJetWriter.DEFAULT_MATERIAL0);
    EnumParameter mp_material1 = new EnumParameter("material1", materialNames, PolyJetWriter.DEFAULT_MATERIAL1);
    EnumParameter mp_material2 = new EnumParameter("material2", materialNames, PolyJetWriter.DEFAULT_MATERIAL2);
    EnumParameter mp_material3 = new EnumParameter("material3", materialNames, PolyJetWriter.DEFAULT_MATERIAL3);
    EnumParameter mp_material4 = new EnumParameter("material4", materialNames, PolyJetWriter.DEFAULT_MATERIAL4);
    EnumParameter mp_material5 = new EnumParameter("material5", materialNames, PolyJetWriter.DEFAULT_MATERIAL5);
    IntParameter mp_firstSlice = new IntParameter("firstSlice", -1);
    IntParameter mp_slicesCount = new IntParameter("slicesCount", -1);
    EnumParameter mp_materialMapping = new EnumParameter("materialMapping", mappingNames, mappingNames[0]);
    DoubleParameter mp_sliceThickness = new DoubleParameter("sliceThickness", 0.027*MM);

    Parameter m_params[] = {
        mp_path,
        mp_material0,
        mp_material1,
        mp_material2,
        mp_material3,
        mp_material4,
        mp_material5,
        mp_firstSlice, 
        mp_slicesCount,
        mp_sliceThickness,
        mp_materialMapping,
    };

    static final String POLYJET_EXT = ".pjs";

    private BaseVolumeViewer m_viewer;

    /** The last directory property */
    private static final String LASTOUTDIR_PROPERTY = "PolyjetOutputDirectory";


    /** The dialog used to select the file to open */
    private JFileChooser m_dialog;

    Dimension m_dialogSize = new Dimension(500, 500);
    /**
     * Create an instance of the action class.
     */
    public SaveModelPolyJetAction(BaseVolumeViewer viewer) {
        super("Export to PolyJet");

        m_viewer = viewer;

        //KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_MASK);
        //putValue(ACCELERATOR_KEY, acc_key);
        //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(SHORT_DESCRIPTION, "save model to a PolyJet slices");

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
    public void _actionPerformed(ActionEvent evt) {

        m_dialog.setPreferredSize(m_dialogSize);
        m_dialog.setSelectedFile(new File(m_viewer.getDesign().getPathNoExt()+POLYJET_EXT));
        int returnVal = m_dialog.showDialog(m_viewer,"Save Slices to a File");

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
            m_viewer.onSaveModelPolyJet(file.getPath(), null);
        }        
    }


    public void actionPerformed(ActionEvent evt) {

        if(DEBUG) printf("PolyJet action\n");
        
        mp_path.setValue(m_viewer.getDesign().getPathNoExt()+POLYJET_EXT);

        ParamDialog dialog = new ParamDialog(m_viewer, "PolyJet Export", m_params); 
        
        dialog.setModal(true);
        
        Dimension dim = m_viewer.getSize();
        Point pnt = m_viewer.getLocationOnScreen();
        pnt.x += dim.width/2;
        if(DEBUG) printf("dialog location:[%d,%d]\n", pnt.x, pnt.y);
        dialog.setLocation(pnt.x, pnt.y);
        dialog.setVisible(true);
        
        if(DEBUG) printf("PolyJet action exit, result: %s\n", dialog.getResult());
        if(dialog.getResult()){
            m_viewer.onSaveModelPolyJet(mp_path.getValue(), new ParamMap(m_params));
        } 
        
    }


} // class SaveModelPolyJetAction
