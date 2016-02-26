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

import abfab3d.param.DoubleParameter;

import java.awt.*;

/**
 * Edits Double Parameter
 *
 * @author Alan Hudson
 */
public class DoubleEditor implements Editor {
    private DoubleParameter m_param;

    public DoubleEditor(DoubleParameter param) {
        m_param = param;
    }

    @Override
    public Component getComponent() {
        return null;
    }
}
