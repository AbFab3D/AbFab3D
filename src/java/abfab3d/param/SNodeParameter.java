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

import static abfab3d.core.Output.printf;

/**
 * A pointer to another node
 *
 * @author Alan Hudson
 */
public class SNodeParameter extends BaseParameter {

    SNodeFactory m_nodeFactory;

    public SNodeParameter(String name) {

        this(name, name, new UndefinedParameter(), new BaseSNodeFactory());
    }

    public SNodeParameter(String name,  SNodeFactory nodeFactory) {

        this(name, name, new UndefinedParameter("undefined"), nodeFactory);
    }

    public SNodeParameter(String name, String desc) {

        this(name, desc, new UndefinedParameter(), new BaseSNodeFactory());
    }

    public SNodeParameter(String name, String desc, Object initialValue, SNodeFactory nodeFactory) {

        super(name, desc);
        m_nodeFactory = nodeFactory;
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
       retursn factory to create new nodes 
     */
    public SNodeFactory getSNodeFactory(){
        return m_nodeFactory;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
    }

    /**
     * Has the value changed since the last call.  This method will clear the changed state.
     * @return
     */
    public boolean hasChanged() {
        boolean ret_val = changed;
        if (value instanceof Parameterizable) {
            Parameter[] cp = ((Parameterizable)value).getParams();
            int len2 = cp.length;
            for(int j=0; j < len2; j++) {
                if (cp[j].hasChanged()) {
                    ret_val = true;
                }
            }
        }

        if (value instanceof Parameter) {
            Parameter val = (Parameter) value;
            if (val.hasChanged()) {
                ret_val = true;
            }
        }
        changed = false;
        return ret_val;
    }
}
