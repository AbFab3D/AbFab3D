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

import java.util.ArrayList;
import java.util.List;

/**
 * A pointer to a list of nodes
 *
 * @author Alan Hudson
 */
public class SNodeListParameter extends Parameter {
    public SNodeListParameter(String name) {

        this(name, name);
    }

    public SNodeListParameter(String name, String desc) {

        this(name, desc, new ArrayList());
    }

    public SNodeListParameter(String name, String desc, List initialValue) {

        super(name, desc);

        setValue(initialValue);
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.SNODE_LIST;
    }

    public void add(Parameterizable source){
        ((List) value).add(source);
    }

    public void set(int index, Parameterizable source){
        ((List) value).set(index, source);
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (!(val instanceof List)) {
            throw new IllegalArgumentException("Unsupported type for SNodeList: " + val + " in param: " + getName());
        }
    }

    public List getValue() {
        return (List) value;
    }
}
