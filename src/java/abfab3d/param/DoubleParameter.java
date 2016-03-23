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

import abfab3d.util.Unit;

// External Imports

/**
 * A Double parameter to a service.
 *
 * @author Alan Hudson
 */
public class DoubleParameter extends NumberParameter {
	/*public enum Unit {NONE, M, CM, MM, M3, CM3, MM3, FT, IN, UM, PT}; */
	
    public static final double DEFAULT_MIN_RANGE = -Double.MAX_VALUE;
    public static final double DEFAULT_MAX_RANGE = Double.MAX_VALUE;
    public static final double DEFAULT_STEP = 1;
    public static final Unit DEFAULT_UNIT = Unit.NONE;


    /** Min range for numeric values */
    private double minRange;

    /** Max range for numeric values */
    private double maxRange;

    /** The step size for changes */
    private double step;
    
    /** The parameters unit */
    private Unit unit;

    public DoubleParameter(String name) {
        this(name, name);
    }

    public DoubleParameter(String name, String desc) {

        this(name, desc, 0, DEFAULT_MIN_RANGE, DEFAULT_MAX_RANGE);
    }

    public DoubleParameter(String name, double initialValue) {

        this(name, name, initialValue, DEFAULT_MIN_RANGE, DEFAULT_MAX_RANGE);
    }

    public DoubleParameter(String name, String desc, double initialValue) {

        this(name, desc, initialValue, DEFAULT_MIN_RANGE, DEFAULT_MAX_RANGE);
    }

    public DoubleParameter(String name, String desc, double initialValue,
                           double minRange, double maxRange) {

        this(name,desc,initialValue,minRange,maxRange,DEFAULT_STEP);
    }

    public DoubleParameter(String name, String desc, double initialValue,
                           double minRange, double maxRange, double step) {

    	this(name,desc,initialValue,minRange,maxRange,step,DEFAULT_UNIT);
    }
    
    public DoubleParameter(String name, String desc, double initialValue,
            double minRange, double maxRange, double step, Unit unit) {

    	super(name, desc);

    	setMinRange(minRange);
    	setMaxRange(maxRange);

    	defaultValue = initialValue;
    	setValue(initialValue);
    	this.step = step;
    	this.unit = unit;
    }

    public DoubleParameter(DoubleParameter def, double initialValue) {

        super(def.getName(), def.getDesc());

        setMinRange(def.getMinRange());
        setMaxRange(def.getMaxRange());

        defaultValue = initialValue;
        setValue(initialValue);
        this.step = def.getStep();
        this.unit = def.getUnit();
    }

    @Override
    public Double getValue() {
        return (Double) value;
    }

    public Object getDefaultValue(Class hint) {
        return getValue();
    }

    @Override
    public void setValue(Object val) {
        if (val instanceof Double) {
            super.setValue(val);
        } else {
            Double dval;
            dval = ((Number)(val)).doubleValue();
            super.setValue(dval);
        }
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

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }
    
    public Unit getUnit() {
    	return unit;
    }
    
    public void setUnit(Unit unit) {
    	this.unit = unit;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (val == null) return;

        if (!(val instanceof Number)) {
            throw new IllegalArgumentException("Unsupported type for Double: " + val.getClass() + " in param: " + getName());
        }
        
        double d = ((Number) val).doubleValue();
        
        if (d < minRange) {
            throw new IllegalArgumentException("Invalid double value: " + val + ", below minimum: " + minRange + " in param: " + getName());

        }
        if (d > maxRange) {
            throw new IllegalArgumentException("Invalid double value: " + val + ", above maximum: " + maxRange + " in param: " + getName());

        }

        if (Double.isNaN(d)) {
            throw new IllegalArgumentException("Double cannot be NaN. In param: " + getName());
        }
    }
}
