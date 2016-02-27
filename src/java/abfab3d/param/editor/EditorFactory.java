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
import abfab3d.param.EnumParameter;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeListParameter;
import abfab3d.param.Vector3dParameter;

/**
 * Creates an editor for a parameter
 *
 * @author Alan Hudson
 */
public class EditorFactory {

    
    public Editor createEditor(Parameter param) {
        switch(param.getType()) {
            case DOUBLE:
                return new DoubleEditor((DoubleParameter)param);
            case VECTOR_3D:
                return new Vector3dEditor((Vector3dParameter)param);
            case ENUM:
                return new EnumEditor((EnumParameter)param);
            case SNODE_LIST:
                return new SNodeListEditor((SNodeListParameter)param);
        default:
            return new DefaultEditor(param);
        }
    }
}
