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

import java.util.Arrays;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * A function parameter.
 * used to create a button. 
 * When the button is pressed 
 * paramChanged(Parameter param) is called 
 *
 * @author Vladimir Bulatov
 */
public class FunctionParameter extends BaseParameter {

    ParamChangedListener m_listener;
    
    public FunctionParameter(String name) {
        this(name, null);        
    }
    public FunctionParameter(String name, ParamChangedListener listener){
        super(name, name); 
        
        m_listener = listener;
        setLabel(name);
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.FUNCTION;
    }

    public void informListeners(){
        if(m_listener != null){
            m_listener.paramChanged(this);
        }
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
