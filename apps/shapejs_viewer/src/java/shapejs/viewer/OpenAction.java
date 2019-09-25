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

import org.apache.commons.io.FilenameUtils;
import org.xj3d.ui.awt.widgets.VRMLFileFilter;
import org.xj3d.ui.awt.widgets.Web3DFileFilter;
import org.xj3d.ui.awt.widgets.X3DFileFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * An action that can be used to open a file.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class OpenAction extends AbstractAction {
    private static final int MAX_HISTORY = 5;

    /** The last directory property */
    private static final String LASTDIR_PROPERTY = "LastOpenDir";

    /** The handler for dealing with file open actions */
    private BaseVolumeViewer viewer;

    /** Parent frame used to handle the file dialog */
    private Component parent;

    /** The dialog used to select the file to open */
    private JFileChooser m_dialog;
    Dimension m_dialogSize = new Dimension(500,500);

    private JMenuItem menu;

    /**
     * Create an instance of the action class.
     *
     * @param contentDirectory initial directory to load content from.
     *    Must be a full path.
     */
    public OpenAction(BaseVolumeViewer viewer,
                      String contentDirectory, JMenuItem menu) {
        super("Open...");

        this.viewer = viewer;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_O,
                KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        putValue(SHORT_DESCRIPTION, "Open A file");

        String dir = null;
        String user_dir = System.getProperty("user.dir");

        if (contentDirectory == null) {
            Preferences prefs = Preferences.userNodeForPackage(OpenAction.class);

            String last_dir = prefs.get(LASTDIR_PROPERTY, null);

            if (last_dir != null)
                dir = last_dir;
            else
                dir = user_dir;

        } else
            dir = contentDirectory;

        m_dialog = new JFileChooser(dir);
        //m_dialog.addChoosableFileFilter(new X3DFileFilter());
        //m_dialog.addChoosableFileFilter(new VRMLFileFilter());
        //m_dialog.addChoosableFileFilter(new Web3DFileFilter());
        //m_dialog.addChoosableFileFilter(new SVXFileFilter());
        //m_dialog.addChoosableFileFilter(new ShapeJSFileFilter());
        m_dialog.setFileFilter(new ShapeJSFileFilter());
        Utils.setFont(m_dialog, viewer.m_font);

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

        try {
            m_dialog.setPreferredSize(m_dialogSize);

            int returnVal = m_dialog.showDialog(parent, "Open File");
            m_dialogSize = m_dialog.getSize();
        
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File file = m_dialog.getSelectedFile();

                String dir = m_dialog.getCurrentDirectory().getAbsolutePath();

                Preferences prefs = Preferences.userNodeForPackage(OpenAction.class);

                prefs.put(LASTDIR_PROPERTY, dir);                

                if (file.exists()) {
                    String f = file.getAbsolutePath();
                    viewer.loadFile(f, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

