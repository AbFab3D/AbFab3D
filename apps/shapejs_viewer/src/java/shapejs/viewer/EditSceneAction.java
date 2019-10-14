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
 * An action that can be used to edit Scene params
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class EditSceneAction extends AbstractAction {
    private Scene m_scene;
    private ParamFrame m_editor;
    private SceneEditedListener m_listener;
    private RenderCanvas m_canvas;

    /**
     * Create an instance of the action class.
     *
     *    Must be a full path.
     */
    public EditSceneAction(Scene scene, RenderCanvas canvas) {
        super("Edit Scene");

        putValue(SHORT_DESCRIPTION, "Edit Scene");

        m_canvas = canvas;
        setScene(scene);
        m_listener = new SceneEditedListener(m_canvas);
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

        m_editor = new ParamFrame(m_scene);
        m_editor.getPanel().addParamChangedListener(m_listener);
        m_editor.setLocation(565, 500);
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

    static class SceneEditedListener implements ParamChangedListener {
        private RenderCanvas m_canvas;

        public SceneEditedListener(RenderCanvas canvas) {
            m_canvas = canvas;
        }

        @Override
        public void paramChanged(Parameter parameter) {
            Scene scene = m_canvas.getScene();
            scene.reinitialize();

            m_canvas.forceRender();
        }
    }


}
