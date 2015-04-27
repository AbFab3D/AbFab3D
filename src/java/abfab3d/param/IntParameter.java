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

/**
 * A Int parameter 
 *
 * @author Vladimir Bulatov
 */
public class IntParameter extends NumberParameter {
    /** Min range for numeric values */
    private int minRange;

    /** Max range for numeric values */
    private int maxRange;

    public IntParameter(String name, String desc, int initialValue) {

        this(name, desc, initialValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntParameter(String name, String desc, int initialValue,
                           int minRange, int maxRange) {

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
        if(val instanceof Double)
            val = new Integer((int)Math.round(((Double)val).doubleValue()));
        validate(val);

        this.value = val;

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
        } else {
            throw new IllegalArgumentException("Unsupported type for Integer: " + val + " in param: " + getName());
        }
        
        if (d < minRange) {
            throw new IllegalArgumentException("Invalid int value, below minimum range: " + d + " in param: " + getName());

        }
        if (d > maxRange) {
            throw new IllegalArgumentException("Invalid int value, above maximum range: " + d + " in param: " + getName());
        }        
    }

}
