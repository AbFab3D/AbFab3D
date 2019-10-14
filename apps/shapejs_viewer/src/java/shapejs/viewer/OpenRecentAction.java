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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

class OpenRecentAction implements ActionListener {

    /** The handler for dealing with file open actions */
    private FileHandler fileHandler;
    private String file;
    
    public OpenRecentAction(FileHandler fileHandler, String file) {
        this.fileHandler = fileHandler;
        this.file = file;
    }
    
    /**
       @Override
    */
    public void actionPerformed(ActionEvent e) {
        try {
            fileHandler.loadFile(file, true);
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
}


