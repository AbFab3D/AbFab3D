/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.param.editor;

import abfab3d.param.Parameterizable;

import java.awt.*;

/**
 * Creates an editing panel for a parameterizable
 *
 * @author Alan Hudson
 */
public class ParamPanel extends Frame {
    private Parameterizable m_param;

    public ParamPanel(Parameterizable param) {
        m_param = param;
    }

    /**
     * Get notification of any parameter changes from this editor
     * @param l
     */
    public void addChangeListener(ParamChangedListener l) {
    }
}
