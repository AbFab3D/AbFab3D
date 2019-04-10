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
import java.util.Set;

import abfab3d.core.Initializable;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * Base code for all Parameterizable
 *
 * @author Alan Hudson
 */
public class BaseParameterizable implements Parameterizable, SNode {

    protected Map<String, Parameter> m_paramMap = new LinkedHashMap<String,Parameter>();
    protected Parameter m_paramArray[];
    /**
     * Get the parameter definition and value.
     *
     * @param param The parameter name
     * @return The param or IllegalArgumentException if not found
     */
    public Parameter getParam(String param) {
        Parameter ret = m_paramMap.get(param);
        if (ret == null) throw new IllegalArgumentException("Cannot find parameter: " + param);
        // we return actual param to be able to modify it VB 
        return ret;
    }

    /**
     * Get the parameters for the datasource.
     * @return The array of parameters
     */
    public Parameter[] getParams() {
        initParamArray();
        return m_paramArray;
    }

    void initParamArray(){
        
        if(m_paramArray == null || m_paramArray.length != m_paramMap.size()){

            m_paramArray = new Parameter[m_paramMap.size()];        
            
            int len = m_paramMap.size();
            
            int idx = 0;
            for(Parameter p : m_paramMap.values()) {
                // we return actual param to be able to modify it VB 
                m_paramArray[idx++] = p;
            }
        }
    }

    /**
     * Return the underlying map.  This is a live map used for performance, be careful.
     * @return
     */
    public Map<String,Parameter> getParamMap() {
        return m_paramMap;
    }

    /**
       adds parameters from the array to the params table 
       @param aparam  - array of parameters to add 
     */
    public void addParams(Parameter aparam[]){
        for(int i = 0; i < aparam.length; i++){
            String pname = aparam[i].getName();
            if(m_paramMap.get(pname) != null){
                throw new RuntimeException(fmt("duplicate param name: %s",pname));
            }
            m_paramMap.put(aparam[i].getName(),aparam[i]);            
        }        
        m_paramArray = null;
        initParamArray();
    }

    /**
     adds parameters from the array to the params table
     @param mparam  - map of parameters to add
     */
    public void addParams(Map<String,Parameter> mparam){
        m_paramMap.putAll(mparam);
        m_paramArray = null;
    }

    public void addParam(Parameter p) {
        m_paramMap.put(p.getName(),p);
        m_paramArray = null;
    }

    public void removeParam(String name) {
        m_paramMap.remove(name);
        m_paramArray = null;
    }

    /**
       remove all existng params 
     */
    public void clearParams(){
        m_paramMap.clear();
        m_paramArray = null;
    }

    /**
     * Set the current value of a parameter.
     * @param paramName The name
     * @param value the value 
     */
    @Override
    public void set(String paramName, Object value) {
        Parameter par = getParam(paramName);
        par.setValue(value);
    }

    /**
     * Get the current value of a parameter.
     * @param paramName The name
     * @return The value or IllegalArgumentException if not found
     */
    public Object get(String paramName) {
        Parameter par = getParam(paramName);
        return par.getValue();
    }

    /**
       return parameter value if parameter exists or default value otherwise
     */
    public Object get(String paramName, Object defaultValue){

        Parameter par = m_paramMap.get(paramName);
        if(par == null)
            return defaultValue;
        else 
            return par.getValue();
    }

    /**
     * Get the children of this node.
     *
     * @noRefGuide
     * @return A live array of children or null if no children
     */
    public SNode[] getChildren() {
        return null;
    }

    public String getParamString(){
        StringBuilder sb = new StringBuilder();
        getParamString(sb);

        return sb.toString();
    }

    public void getParamString(StringBuilder sb){
        getParamString(getClass().getSimpleName(),m_paramArray,sb);
    }

    public static void getParamString(String name,Parameter[] params,StringBuilder sb) {
        sb.append("{");
        sb.append(name);
        sb.append(":{");
        for(int i = 0; i < params.length; i++){
            Parameter p = params[i];
            sb.append(p.getName());
            sb.append(":");
            p.getParamString(sb);
            if(i < params.length-1)
                sb.append(",");
            else
                sb.append("");
        }
        sb.append("}}");
    }

    public static String getParamString(String name,Parameter[] params) {
        StringBuilder sb = new StringBuilder();
        getParamString(name,params,sb);

        return sb.toString();
    }

    public void getDataLabel(StringBuilder sb){
        getParamString(getClass().getSimpleName(),m_paramArray,sb);
    }

    public String getDataLabel() {
        return getParamString(getClass().getSimpleName(),m_paramArray);
    }


    public final static void initialize(Object obj){
        if(obj instanceof Initializable){
            ((Initializable)obj).initialize();
        }
    }


}
