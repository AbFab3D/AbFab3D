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
 * A pointer to another node
 *
 * @author Alan Hudson
 */
public class SNodeParameter extends Parameter {
    public SNodeParameter(String name) {

        this(name, name, null);
    }

    public SNodeParameter(String name, String desc) {

        this(name, desc, null);
    }

    public SNodeParameter(String name, String desc, Object initialValue) {

        super(name, desc);

        setValue(initialValue);
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.SNODE;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
    }
}
