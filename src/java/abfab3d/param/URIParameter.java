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
import java.io.File;

import static abfab3d.core.Output.printf;

/**
 * A URI parameter
 *
 * @author Vladimir Bulatov
 */
public class URIParameter extends BaseParameter {
    private String[] validMimeTypes = new String[] {"*"};
    private final boolean DEBUG = false;


    public URIParameter(String name, String initialValue) {
        super(name, name);
        defaultValue = initialValue;
        setValue(initialValue);
    }

    public URIParameter(String name, String desc, String initialValue) {
        super(name, desc);
        defaultValue = initialValue;
        setValue(initialValue);
    }

    public URIParameter(String name, String desc, String initialValue, String[] validMimeTypes) {
        super(name, desc);
        defaultValue = initialValue;
        setValue(initialValue);

        if (validMimeTypes != null) {
            this.validMimeTypes = validMimeTypes.clone();
        }
    }

    @Override
    public String getValue() {
        return (String) value;
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.URI;
    }

    public void setValidMimeTypes(String[] val) {
        validMimeTypes = val.clone();
    }

    public String[] getValidMimeTypes() {
        return validMimeTypes;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (val == null) return;

        if (!(val instanceof String)) {
            throw new IllegalArgumentException("Unsupported type for String: " + val + " in param: " + getName());
        }
    }

    public URIParameter clone() {
        return (URIParameter) super.clone();
    }

    /**
       return param label used for caching
       @Override
     */
    public String getParamString() {
        StringBuilder sb = new StringBuilder();
        getParamString(sb);
        return sb.toString();
    }

    /**
       return param label used for caching
       @Override
     */
    public void getParamString(StringBuilder sb) {

        //TODO - deal with network URI
        String path = (String)value; 
        if(path == null) {
            sb.append("null");
            return;
        }
        if(path.startsWith("http:") || path.startsWith("https:")) {
            sb.append(path);
            return;            
        }

        long timeStamp = new File(path).lastModified();
        sb.append(path);
        sb.append(";");
        sb.append(timeStamp);
        if(DEBUG) printf("URIParameter:%s, paramString:%s\n",getName(), sb.toString());
        
    }

} //URIParameter
