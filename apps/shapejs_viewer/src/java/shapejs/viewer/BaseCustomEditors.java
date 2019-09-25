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

import abfab3d.param.Editor;
import abfab3d.param.editor.EditorCreator;
import abfab3d.shapejs.Scene;

import java.awt.*;
import java.util.ArrayList;

/**
 * Custom editors
 *
 * @author Tony Wong
 */
public abstract class BaseCustomEditors implements EditorCreator {
    protected Component m_parent;
    protected Navigator m_examineNav;
    protected Navigator m_objNav;
    protected int m_width = 512;
    protected int m_height = 512;

    // Editors which care about the current scene
    protected ArrayList<Editor> sceneEditors = new ArrayList<Editor>();
    
    
    public BaseCustomEditors(Component parent, Navigator examineNav, Navigator objNav) {
        m_parent = parent;
        m_examineNav = examineNav;
        m_objNav = objNav;
    }

    public abstract void setScene(Scene scene);

    public abstract void setWindowSize(int w, int h) ;

}
