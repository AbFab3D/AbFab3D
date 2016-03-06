/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.param;

// External Imports

/**
 * A Empty parameter 
 *
 * @author Vladimir Bulatov
 */
public class UndefinedParameter extends BaseParameter implements Cloneable {

    public UndefinedParameter(String name, String desc) {
        super(name,desc);
    }

    public UndefinedParameter() {
        this("undefined");
    }

    public UndefinedParameter(String name) {
        super(name, name);
    }

    public UndefinedParameter clone() {
        return (UndefinedParameter) super.clone();
    }

    public ParameterType getType(){
        return ParameterType.UNDEFINED;
    }

    public void validate(Object val) {
    }
}
