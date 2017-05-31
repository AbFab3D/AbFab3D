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
 * A Long parameter 
 *
 * @author Alan Hudson
 */
public class LongParameter extends NumberParameter {
    /** Min range for numeric values */
    private long minRange;

    /** Max range for numeric values */
    private long maxRange;

    public LongParameter(String name, long initialValue) {
        this(name, name, initialValue);
    }
    public LongParameter(String name, String desc, long initialValue) {

        this(name, desc, initialValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public LongParameter(String name, String desc, long initialValue,
                         long minRange, long maxRange) {

        super(name, desc);

        setMinRange(minRange);
        setMaxRange(maxRange);

        defaultValue = initialValue;
        setValue(initialValue);
    }

    @Override
    public Long getValue() {
        return (Long) value;
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
    public long getMinRange() {
        return minRange;
    }

    /**
     * @param minRange the minRange to set
     */
    public void setMinRange(long minRange) {
        this.minRange = minRange;
    }

    /**
     * @return the maxRange
     */
    public long getMaxRange() {
        return maxRange;
    }

    /**
     * @param maxRange the maxRange to set
     */
    public void setMaxRange(long maxRange) {
        this.maxRange = maxRange;
    }
    
    @Override
    public void setValue(Object val) {
        if(val instanceof Double)
            val = new Long((long)Math.round(((Double)val).doubleValue()));
        validate(val);

        this.value = val;
        changed = true;

    }


    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (val == null) return;

        long d = 0;
        if (val instanceof Number) {
            d = ((Number) val).longValue();
            this.value = val;        
        } else {
            throw new IllegalArgumentException("Unsupported type for Long: " + val + " in param: " + getName());
        }
        
        if (d < minRange) {
            throw new IllegalArgumentException("Invalid long value, below minimum range: " + d + " in param: " + getName());

        }
        if (d > maxRange) {
            throw new IllegalArgumentException("Invalid long value, above maximum range: " + d + " in param: " + getName());
        }        
    }

}
