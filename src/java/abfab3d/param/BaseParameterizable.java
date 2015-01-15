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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base code for all Parameterizable
 *
 * @author Alan Hudson
 */
public class BaseParameterizable implements Parametrizable, SNode {
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

        return ret.clone();
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
            ret[idx++] = p.clone();
        }

        return ret;
    }

    /**
     * Get the current value of a parameter.
     * @param param The name
     * @return The value or IllegalArgumentException if not found
     */
    public Object getParamValue(String param) {
        Parameter ret = params.get(param);
        if (ret == null) throw new IllegalArgumentException("Cannot find parameter: " + param);

        return ret.getValue();
    }

    @Override
    public SNode[] getChildren() {
        return null;
    }
}
