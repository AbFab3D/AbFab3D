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

import static abfab3d.core.Output.fmt;

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

    public StringListParameter(String name, String values[]){
        super(name, name);
        setValue(values);
        
    }

    public StringListParameter(String name, String desc, List initialValue) {

        super(name, desc);

        setValue(initialValue);
    }

    public void add(String str){
        getList().add(str);
    }

    public void clear(){
        getList().clear();
    }

    public int size(){
        return getList().size();
    }

    public final ArrayList getList(){
        return (ArrayList)value;
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
        if (!(val instanceof ArrayList)) {
            throw new IllegalArgumentException("Unsupported type for StringList: " + val + " in param: " + getName());
        }
    }


    /**
     * Set the parameters value
     * @param value
     */
    public void setValue(Object value) {
        
        if(value instanceof List){

            ArrayList list = new ArrayList();
            list.addAll((List)value); 
            this.value = list;

        } else if(value instanceof String[]){

            ArrayList list = new ArrayList();
            String values[] = (String[])value;
            for(int i = 0; i < values.length; i++){
                list.add(values[i]);
            }
            this.value = list;

        } else {
            throw new RuntimeException(fmt("illegal parameter:%s",value));
        }
        
        changed = true;
        updateUI();
    }

}
