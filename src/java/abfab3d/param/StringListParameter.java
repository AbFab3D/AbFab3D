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
 * A list of String
 *
 * @author Alan Hudson
 */
public class StringListParameter extends ListParameter {
    public StringListParameter(String name) {

        this(name, name);
    }

    public StringListParameter(String name, String desc) {

        this(name, desc, new ArrayList());
    }

    public StringListParameter(String name, String values[]) {
        super(name, name);
        List list = new ArrayList();
        for(int i = 0; i < values.length; i++){
            list.add(values[i]);
        }
        setValue(list);
        
    }

    public StringListParameter(String name, String desc, List initialValue) {

        super(name, desc);

        setValue(initialValue);
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.STRING_LIST;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (!(val instanceof List)) {
            throw new IllegalArgumentException("Unsupported type for StringList: " + val + " in param: " + getName());
        }
    }
}
