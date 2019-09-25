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

import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;
import abfab3d.param.ParamChangedListener;
import abfab3d.param.editor.ParamPanel;
import abfab3d.param.editor.ParamFrame;
import abfab3d.shapejs.Scene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import static abfab3d.core.Output.printf;

/**
 * <p>
 *
 */
public class EditOptionsAction extends AbstractAction {

    private ParamFrame m_editor;

    /**
     * Create an instance of the action class.
     *
     *    Must be a full path.
     */
    public EditOptionsAction() {
        super("Edit Options");

        putValue(SHORT_DESCRIPTION, "Edit Options");
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

        m_editor = new ParamFrame(ViewerConfig.getInstance());
        //m_editor.getPanel().addParamChangedListener(m_listener);
        m_editor.setLocation(565, 50);
        m_editor.setVisible(true);
    }

    public void close() {
        if (m_editor == null) return;
        m_editor.getPanel().clearParamChangedListeners();
        m_editor.getPanel().closeWithChildren();
    }

}
