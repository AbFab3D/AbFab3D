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

import abfab3d.param.Parameter;
import abfab3d.param.Editor;
import abfab3d.shapejs.Scene;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import static abfab3d.core.Output.printf;

/**
 * Custom editors
 *
 * @author Alan Hudson
 */
public class CustomEditors extends BaseCustomEditors implements ComponentListener {
    private Scene m_scene;

    // Editors which care about the current scene
    private ArrayList<Editor> sceneEditors = new ArrayList<Editor>();
    
    public CustomEditors(Component parent, Navigator examineNav, Navigator objNav) {
        super(parent, examineNav, objNav);
    }

    public void setScene(Scene scene) {
        m_scene = scene;

        for(Editor e: sceneEditors) {
            ((LocationEditor)e).setScene(scene);
        }
    }

    public void setWindowSize(int w, int h) {
        m_width = w;
        m_height = h;
        for(Editor e: sceneEditors) {
            ((LocationEditor)e).setWindowSize(w,h);
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        int w = m_parent.getWidth();
        int h = m_parent.getHeight();
        setWindowSize(w,h);
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public Editor createEditor(Parameter param) {
        switch(param.getType()) {
            case LOCATION:
                /*
                LocationEditor le = new LocationEditor(param, m_scene, m_parent, m_examineNav,m_objNav,m_width,m_height);

                // TODO: grows unbounded
                sceneEditors.add(le);
                return le;

                 */

                // TOOO: Reimplement

                printf("Need to implement location editors\n");
                return null;
        }

        return null;
    }
}
