package abfab3d.param;

import java.util.Map;

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
     *
     * @return The array of parameters
     */
    public Parameter[] getParams();

    /**
       return string representation of this parametrizable
     */
    public String getParamString();


    /**
     * Returns the underlying parameter list, this is a live list.
     *
     * @return
     */
    public Map<String, Parameter> getParamMap();

    /**
     * Get the current value of a parameter.
     *
     * @param param The name
     * @return The value or IllegalArgumentException if not found
     */
    public Object get(String param);


    /**
     * Set the current value of a parameter.
     *
     * @param param The name
     * @param value the value to set to param
     */
    public void set(String param, Object value);


}
