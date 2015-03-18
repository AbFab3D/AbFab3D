package abfab3d.param;

import abfab3d.param.Parameter;

/**
 * Can have parameters.
 */
public interface Parameterizable {
    /**
     * Get the parameter definition and value.
     *
     * @param param The parameter name
     * @return The param or IllegalArgumentException if not found
     */
    public Parameter getParam(String param);

    /**
     * Get the parameters for the datasource.
     * @return The array of parameters
     */
    public Parameter[] getParams();

    /**
     * Get the current value of a parameter.
     * @param param The name
     * @return The value or IllegalArgumentException if not found
     */
    public Object get(String param);


    /**
     * Set the current value of a parameter.
     * @param param The name
     * @param value the value to set to param
     */
    public void set(String param, Object value);


}
