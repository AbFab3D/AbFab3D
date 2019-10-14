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

class ClearHistoryAction implements ActionListener {

    /** The handler for dealing with file open actions */
    private BaseVolumeViewer viewer;
    
    public ClearHistoryAction(BaseVolumeViewer viewer) {
        this.viewer = viewer;
    }
    
    /**
       @Override
    */
    public void actionPerformed(ActionEvent e) {

        History hist = ViewerConfig.getInstance().getOpenFileHistory();
        hist.clear();
        viewer.updateHistoryMenu();
    }
}


