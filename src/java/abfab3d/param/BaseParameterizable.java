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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base code for all Parameterizable
 *
 * @author Alan Hudson
 */
public class BaseParameterizable implements Parameterizable, SNode {

    protected Map<String, Parameter> params = new LinkedHashMap<String,Parameter>();

    /**
     * Get the parameter definition and value.
     *
     * @param param The parameter name
     * @return The param or IllegalArgumentException if not found
     */
    public Parameter getParam(String param) {
        Parameter ret = params.get(param);
        if (ret == null) throw new IllegalArgumentException("Cannot find parameter: " + param);
        // we return actual param to be able to modify it VB 
        return ret;
    }

    /**
     * Get the parameters for the datasource.
     * @return The array of parameters
     */
    public Parameter[] getParams() {
        Parameter[] ret = new Parameter[params.size()];

        int len = params.size();

        int idx = 0;
        for(Parameter p : params.values()) {
            // we return actual param to be able to modify it VB 
            ret[idx++] = p;
        }

        return ret;
    }

    /**
       adds parameters from the array to the params table 
       @param aparam  - array of parameters to add 
     */
    public void addParams(Parameter aparam[]){
        for(int i = 0; i < aparam.length; i++){
            params.put(aparam[i].getName(),aparam[i]);
        }        
    }

    /**
     * Set the current value of a parameter.
     * @param param The name
     * @param value the value 
     */
    @Override
    public void set(String paramName, Object value) {
        Parameter par = getParam(paramName);
        par.setValue(value);
    }

    /**
     * Get the current value of a parameter.
     * @param param The name
     * @return The value or IllegalArgumentException if not found
     */
    public Object get(String paramName) {
        Parameter par = getParam(paramName);
        return par.getValue();
    }

    @Override
    public SNode[] getChildren() {
        return null;
    }
}
