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
 * A Double parameter to a service.
 *
 * @author Alan Hudson
 */
public class DoubleParameter extends NumberParameter {
    /** Min range for numeric values */
    private double minRange;

    /** Max range for numeric values */
    private double maxRange;

    public DoubleParameter(String name) {

        this(name, name);
    }

    public DoubleParameter(String name, String desc) {

        this(name, desc, 0,Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public DoubleParameter(String name, String desc, double initialValue,
                           double minRange, double maxRange) {

        super(name, desc);

        setMinRange(minRange);
        setMaxRange(maxRange);

        setValue(initialValue);
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.DOUBLE;
    }

    /**
     * @return the minRange
     */
    public double getMinRange() {
        return minRange;
    }

    /**
     * @param minRange the minRange to set
     */
    public void setMinRange(double minRange) {
        this.minRange = minRange;
    }

    /**
     * @return the maxRange
     */
    public double getMaxRange() {
        return maxRange;
    }

    /**
     * @param maxRange the maxRange to set
     */
    public void setMaxRange(double maxRange) {
        this.maxRange = maxRange;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (!(val instanceof Double)) {
            throw new IllegalArgumentException("Unsupported type for Double: " + val + " in param: " + getName());
        }
        
        double d = ((Double) val).doubleValue();
        
        if (d < minRange) {
            throw new IllegalArgumentException("Invalid double value, below minimum range: " + d + " in param: " + getName());

        }
        if (d > maxRange) {
            throw new IllegalArgumentException("Invalid double value, above maximum range: " + d + " in param: " + getName());

        }
    }
}
