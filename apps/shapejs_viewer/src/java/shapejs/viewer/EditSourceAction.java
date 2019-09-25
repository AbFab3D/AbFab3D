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

import java.util.ArrayList;

import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;
import abfab3d.param.ParamChangedListener;
import abfab3d.param.editor.ParamFrame;
import abfab3d.param.editor.ParamPanel;
import abfab3d.shapejs.Scene;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static abfab3d.core.Output.printf;

/**
 * An action that can be used to edit Scene params
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class EditSourceAction extends AbstractAction {
    private Scene m_scene;
    private ParamFrame m_editor;
    private SourcesEditedListener m_listener;
    private RenderCanvas m_canvas;

    /**
     * Create an instance of the action class.
     *
     *    Must be a full path.
     */
    public EditSourceAction(Scene scene, RenderCanvas canvas) {
        super("Edit Source");

        putValue(SHORT_DESCRIPTION, "Edit Source");

        m_canvas = canvas;
        setScene(scene);
        m_listener = new SourcesEditedListener(m_canvas);
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

        if (m_scene == null) return;
        ArrayList<Parameterizable> source =  (ArrayList<Parameterizable>)m_scene.getSource();
        for(int i = 0; i < source.size(); i++){
            printf("source:%s\n",source.get(i));
        }
        m_editor = new ParamFrame(source.get(0));
        m_editor.getPanel().addParamChangedListener(m_listener);
        m_editor.setLocation(565, 0);
        m_editor.setVisible(true);

    }

    public void close() {
        if (m_editor == null) return;
        m_editor.getPanel().clearParamChangedListeners();
        m_editor.getPanel().closeWithChildren();
    }

    public void setScene(Scene scene) {
        m_scene = scene;

        if (m_scene != null) {
            close();
            return;
        }

    }

    static class SourcesEditedListener implements ParamChangedListener {
        private RenderCanvas m_canvas;

        public SourcesEditedListener(RenderCanvas canvas) {
            m_canvas = canvas;
        }

        @Override
        public void paramChanged(Parameter parameter) {
            m_canvas.forceRender();
        }
    }


}
