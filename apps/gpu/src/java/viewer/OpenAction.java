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


// External imports
import org.xj3d.ui.awt.widgets.VRMLFileFilter;
import org.xj3d.ui.awt.widgets.Web3DFileFilter;
import org.xj3d.ui.awt.widgets.X3DFileFilter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import java.util.prefs.Preferences;

import java.net.MalformedURLException;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;

/**
 * An action that can be used to open a file.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class OpenAction extends AbstractAction {

    /** The last directory property */
    private static final String LASTDIR_PROPERTY = "History_";

    /** The handler for dealing with file open actions */
    private FileHandler fileHandler;

    /** Parent frame used to handle the file dialog */
    private Component parent;

    /** The dialog used to select the file to open */
    private JFileChooser fc;

    /**
     * Create an instance of the action class.
     *
     * @param parent The parent component
     * @param handler A handler for opening files
     * @param contentDirectory initial directory to load content from.
     *    Must be a full path.
     */
    public OpenAction(Component parent,
                      FileHandler handler,
                      String contentDirectory) {
        super("Open");

        this.parent = parent;
        fileHandler = handler;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_O,
                KeyEvent.CTRL_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        putValue(SHORT_DESCRIPTION, "Open A file");

        String dir = null;

        if (contentDirectory == null) {
            Preferences prefs = Preferences.userNodeForPackage(OpenAction.class);

            String last_dir = prefs.get(LASTDIR_PROPERTY, null);
            String user_dir = System.getProperty("user.dir");

            if (last_dir != null)
                dir = last_dir;
            else
                dir = user_dir;

        } else
            dir = contentDirectory;

        fc = new JFileChooser(dir);
        fc.addChoosableFileFilter(new X3DFileFilter());
        fc.addChoosableFileFilter(new VRMLFileFilter());
        fc.addChoosableFileFilter(new Web3DFileFilter());
        fc.addChoosableFileFilter(new SVXFileFilter());
        fc.addChoosableFileFilter(new ShapeJSFileFilter());
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
            int returnVal = fc.showDialog(parent,"Open File");
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File file = fc.getSelectedFile();

                String dir = file.getPath();

                int idx = dir.lastIndexOf(File.separator);

                if (idx > 0) {
                    dir = dir.substring(0, idx);

                    Preferences prefs = Preferences.userNodeForPackage(OpenAction.class);

                    prefs.put(LASTDIR_PROPERTY, dir);
                }

                if (file.exists())
                    fileHandler.loadURL(file.getAbsolutePath());
            }
        } catch(MalformedURLException mue) {
            mue.printStackTrace();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
