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

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

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
     * Return the underlying map.  This is a live map used for performance, be careful.
     * @return
     */
    public Map<String,Parameter> getParamMap() {
        return params;
    }

    /**
       adds parameters from the array to the params table 
       @param aparam  - array of parameters to add 
     */
    public void addParams(Parameter aparam[]){
        for(int i = 0; i < aparam.length; i++){
            String pname = aparam[i].getName();
            if(params.get(pname) != null){
                throw new RuntimeException(fmt("duplicate param name: %s",pname));
            }
            params.put(aparam[i].getName(),aparam[i]);
        }        
    }

    /**
     adds parameters from the array to the params table
     @param aparam  - array of parameters to add
     */
    public void addParams(Map<String,Parameter> aparam){
        params.putAll(aparam);
    }

    public void addParam(Parameter p) {
        params.put(p.getName(),p);
    }

    public void removeParam(String name) {
        params.remove(name);
    }

    /**
       remove all existng params 
     */
    public void clearParams(){
        params.clear();
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
     * Get the children of this node.
     *
     * @noRefGuide
     * @return A live array of children or null if no children
     */
    public SNode[] getChildren() {
        return null;
    }

    public String getParamString(Parameterizable pnode){
        return getParamString(getClass().getSimpleName(),pnode);
    }

    public String getParamString(Parameter[] aparam){
        return getParamString(getClass().getSimpleName(),aparam);
    }

    /**
     * saves array of parameters into a string
     * @noRefGuide
     */
    public static String getParamString(String name,Parameter aparam[]){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(":");
        for(int i = 0; i < aparam.length; i++){
            Parameter p = aparam[i];
            sb.append(p.getName());
            sb.append("=\"");
            p.getParamString(sb);
            sb.append("\";");
        }
        return sb.toString();
    }

    /**
     * saves array of parameters into a string
     *
     * @param add Additional string to add
     * @noRefGuide
     */
    public static String getParamString(String name,Parameter aparam[], String add){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(":");
        for(int i = 0; i < aparam.length; i++){
            Parameter p = aparam[i];
            sb.append(p.getName());
            sb.append("=\"");
            p.getParamString(sb);
            sb.append("\";");
        }

        sb.append(add);
        return sb.toString();
    }

    /**
     * saves array of parameters into a string
     * @noRefGuide
     */
    public static String getParamString(String name,Set<String> ignore,Parameterizable pnode){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(":");

        Map<String,Parameter> map = pnode.getParamMap();
        for(Parameter p : map.values()){
            if (ignore.contains(p.getName())) continue;
            sb.append(p.getName());
            sb.append("=\"");
            String ps = p.getParamString();
            sb.append(ps);
            sb.append("\";");
        }

        return sb.toString();
    }

    /**
     * saves array of parameters into a string
     * @noRefGuide
     */
    public static String getParamString(String name,Parameterizable pnode){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(":");

        Map<String,Parameter> map = pnode.getParamMap();
        for(Parameter p : map.values()){
            sb.append(p.getName());
            sb.append("=\"");
            String ps = p.getParamString();
            sb.append(ps);
            sb.append("\";");
        }
        return sb.toString();
    }

    /**
     * Saves array of parameters into a string
     * @noRefGuide
     */
    public static String getParamString(String name, Object src,Parameter aparam[]){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("[");

        String source = null;
        if (src instanceof SourceWrapper) {
            source = ((SourceWrapper)src).getParamString();
        } else {
            source = src.toString();
        }

        sb.append("source=\"");
        sb.append(source);
        sb.append("\";");
        for(int i = 0; i < aparam.length; i++){
            Parameter p = aparam[i];
            sb.append(p.getName());
            sb.append("=\"");
            sb.append(p.getParamString());
            sb.append("\";");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Saves array of parameters into a string
     * @noRefGuide
     */
    public static String getParamString(String name, Map<String,Parameter> params){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("[");
        for(Parameter p : params.values()) {
            sb.append(p.getName());
            sb.append("=\"");
            sb.append(p.getParamString());
            sb.append("\";");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Get the parameters as a string
     * @noRefGuide
     * @param name
     * @param params
     * @return
     */
    public static String getParamObjString(String name,Map<String,Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("[");
        for(Map.Entry<String,Object> p : params.entrySet()) {
            sb.append(p.getKey());
            sb.append("=\"");
            sb.append(p.getValue());
            sb.append("\";");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Output the hash and params for toString()
     * @return
     */
    /*
    // This messes up debug displays as its very large, moved functionality to getParamString
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(",");
        sb.append(getParamString(getClass().getSimpleName(),params));

        return sb.toString();
    }
    */
}
