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

import abfab3d.param.BooleanParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeListParameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.AxisAngle4dParameter;

/**
 * Creates an editor for a parameter
 *
 * @author Alan Hudson
 */
public class EditorFactory {

    
    private static EditorFactory sm_factory;
    
    public Editor createEditor(Parameter param) {
        switch(param.getType()) {
        case INTEGER:
            return new IntEditor((IntParameter)param);
        case DOUBLE:
            //return new DoubleEditor((DoubleParameter)param);
            return new DoubleEditorScroll((DoubleParameter)param);
        case VECTOR_3D:
            return new Vector3dEditor((Vector3dParameter)param);
        case AXIS_ANGLE_4D:
            return new AxisAngle4dEditor((AxisAngle4dParameter)param);            
        case ENUM:
            return new EnumEditor((EnumParameter)param);
        case BOOLEAN:
            return new BooleanEditor((BooleanParameter)param);
        case SNODE_LIST:
            return new SNodeListEditor((SNodeListParameter)param);
            case SNODE:
                return new SNodeEditor((SNodeParameter)param);
        default:
            return new DefaultEditor(param);
        }
    }
    
    public static EditorFactory getInstance(){
        if(sm_factory == null)
            sm_factory = new EditorFactory();
        return sm_factory;
    }
    
}
