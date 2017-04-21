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


import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
/**
 * A Int parameter 
 *
 * @author Vladimir Bulatov
 */
public class IntParameter extends NumberParameter {
    
    static final boolean DEBUG = false;
    public static final int DEFAULT_MIN_RANGE = Integer.MIN_VALUE;
    public static final int DEFAULT_MAX_RANGE = Integer.MAX_VALUE;

    /** Min range for numeric values */
    private int minRange;
    /** Max range for numeric values */
    private int maxRange;
    // increent step
    private int m_step = 1;

    public IntParameter(String name, int initialValue) {

        this(name, name, initialValue);
    }

    public IntParameter(String name, String desc, int initialValue) {

        this(name, desc, initialValue, DEFAULT_MIN_RANGE, DEFAULT_MAX_RANGE);
    }

    public IntParameter(String name, String desc, int initialValue, int minRange, int maxRange) {

        super(name, desc);
        
        setMinRange(minRange);
        setMaxRange(maxRange);
        defaultValue = initialValue;
        setValue(initialValue);
    }

    @Override
    public Integer getValue() {
        return (Integer) value;
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.INTEGER;
    }

    /**
     * @return the minRange
     */
    public int getMinRange() {
        return minRange;
    }

    /**
     * @param minRange the minRange to set
     */
    public void setMinRange(int minRange) {
        this.minRange = minRange;
    }

    /**
     * @return the maxRange
     */
    public int getMaxRange() {
        return maxRange;
    }

    /**
     * @param maxRange the maxRange to set
     */
    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }
    
    @Override
    public void setValue(Object val) {

        if(DEBUG) printf("IntParameter(%s).setValue(%s:%s)\n",getName(),val.getClass().getName(), val);
        // Integer keeps data as Double (for compatibility with some UI)
        if(val instanceof Double){
            val = new Integer((int)Math.round(((Double)val).doubleValue()));
        } else if(val instanceof Integer){
            // do nothing 
        } else if(val instanceof String){
            val = new Integer((int)Math.round(Double.parseDouble((String)val)));            
        }
        validate(val);

        this.value = val;
        changed = true;
        updateUI();

    }


    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (val == null) return;

        int d = 0;
        if (val instanceof Integer) {
            d = ((Integer) val).intValue();
            this.value = val;        
        }  else if (val instanceof Double) {
            d = (int)Math.round(((Double) val).doubleValue());
            this.value = new Integer(d);        
        } else {
            throw new IllegalArgumentException(fmt("Unsupported type for Integer: %s, %s in param: %s", val, val.getClass().getName(),getName()));
        }
        
        if (d < minRange) {
            throw new IllegalArgumentException("Invalid int value, below minimum range: " + d + " in param: " + getName());

        }
        if (d > maxRange) {
            throw new IllegalArgumentException("Invalid int value, above maximum range: " + d + " in param: " + getName());
        }        
    }

    /**
       @Override
    */
    public String getStringValue(){
        return ((Integer)value).toString();
    }
    
    /**
       @Override
    */
    public void setStringValue(String str){
        
        value = new Integer(str);

    }

}
