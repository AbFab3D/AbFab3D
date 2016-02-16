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

import javax.vecmath.Vector3d;

/**
 * A location parameter.  This is a point and a normal.
 *
 * @author Alan Hudson
 */
public class LocationParameter extends BaseParameter implements Cloneable {
    private static final Vector3d DEFAULT_POINT = null;
    private static final Vector3d DEFAULT_NORMAL = null;
    
    private static final Vector3d DEFAULT_MIN_POINT = new Vector3d(-10000d, -10000d, -10000d);
    private static final Vector3d DEFAULT_MAX_POINT = new Vector3d(10000d, 10000d, 10000d);
    
    private Vector3d minPoint;
    private Vector3d maxPoint;

    public LocationParameter(String name) {
        this(name, name, DEFAULT_POINT, DEFAULT_NORMAL);
    }

    public LocationParameter(String name, String desc) {
        this(name, desc, DEFAULT_POINT, DEFAULT_NORMAL);
    }
    
    public LocationParameter(String name, String desc, Vector3d point, Vector3d normal) {
        this(name, desc, point, normal, DEFAULT_MIN_POINT, DEFAULT_MAX_POINT);
    }

    public LocationParameter(String name, String desc, Vector3d point, Vector3d normal, Vector3d minPoint, Vector3d maxPoint) {
        super(name, desc);

        if (point == null) point = DEFAULT_POINT;
        if (normal == null) normal = DEFAULT_NORMAL;
    	if (minPoint == null) minPoint = DEFAULT_MIN_POINT;
    	if (maxPoint == null) maxPoint = DEFAULT_MAX_POINT;
    	
        setPointMin(minPoint);
        setPointMax(maxPoint);

        if (point != null && normal != null) {
        	setValue(new Vector3d[] {(Vector3d)point.clone(),(Vector3d)normal.clone()});
        	defaultValue = value;
        }
    }
    
    /**
     * Set the min location point
     */
    public void setPointMin(Vector3d minPoint) {
    	if (this.minPoint == null) this.minPoint = new Vector3d();
    	
        this.minPoint.x = minPoint.x;
        this.minPoint.y = minPoint.y;
        this.minPoint.z = minPoint.z;
    }

    public void setPointMin(double px,double py,double pz) {
        if (this.minPoint == null) this.minPoint = new Vector3d();

        this.minPoint.x = px;
        this.minPoint.y = py;
        this.minPoint.z = pz;
    }

    /**
     * Set the max location point
     */
    public void setPointMax(Vector3d maxPoint) {
    	if (this.maxPoint == null) this.maxPoint = new Vector3d();
    	
        this.maxPoint.x = maxPoint.x;
        this.maxPoint.y = maxPoint.y;
        this.maxPoint.z = maxPoint.z;
    }

    public void setPointMax(double px,double py,double pz) {
        if (this.maxPoint == null) this.maxPoint = new Vector3d();

        this.maxPoint.x = px;
        this.maxPoint.y = py;
        this.maxPoint.z = pz;
    }

    public Vector3d getPointMin() {
    	return minPoint;
    }
    
    
    public Vector3d getPointMax() {
    	return maxPoint;
    }
    
    @Override
    public void setValue(Object value) {
        validate(value);

        this.value = value;
        
    	if (defaultValue == null) {
    		defaultValue = value;
    	}
    }
    
    @Override
    public Vector3d[] getValue() {
        return (Vector3d[]) value;
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.LOCATION;
    }
    
    public void setPoint(Vector3d val) {
    	validatePoint(val);
    	
    	if (value == null) {
    		value = new Vector3d[2];
    		((Vector3d[])value)[0] = (Vector3d)val.clone();
    	} else {
    		Vector3d point = ((Vector3d[])value)[0];
    		if (point == null) {
    			point = new Vector3d();
    		}
            point.x = val.x;
            point.y = val.y;
            point.z = val.z;
    	}
    	
    	if (defaultValue == null) {
    		defaultValue = value;
    	}
    }

    public void setNormal(Vector3d val) {
    	if (value == null) {
    		value = new Vector3d[2];
    		((Vector3d[])value)[1] = (Vector3d)val.clone();
    	} else {
            Vector3d normal = ((Vector3d[])value)[1];
    		if (normal == null) {
    			normal = new Vector3d();
    		}
            normal.x = val.x;
            normal.y = val.y;
            normal.z = val.z;
    	}
    	
    	if (defaultValue == null) {
    		defaultValue = value;
    	}
    }

    public Vector3d getPoint() {
    	if (value == null) return null;
    	
        return ((Vector3d[])value)[0];
    }

    public Vector3d getNormal() {
    	if (value == null) return null;
    	
        return ((Vector3d[])value)[1];
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (!(val instanceof Vector3d[])) {
            throw new IllegalArgumentException("Unsupported type for Location: " + val + " in param: " + getName());
        }
        
        Vector3d p = ((Vector3d[]) val)[0];
        validatePoint(p);
    }

    public void validatePoint(Vector3d p)  {
    	boolean valid = true;
    	
        if (p.x < minPoint.x || p.y < minPoint.y || p.z < minPoint.z) {
        	valid = false;
        }
        if (p.x > maxPoint.x || p.y > maxPoint.y || p.z > maxPoint.z) {
        	valid = false;
        }
        
        if (!valid) {
        	throw new IllegalArgumentException("Invalid LocationParameter: point must be between min and max\n" +
        			"point: (" + p.x + ", " + p.y + ", " + p.z + ")\n" +
        			"min: (" + minPoint.x + ", " + minPoint.y + ", " + minPoint.z + ")\n" +
        			"max: (" + maxPoint.x + ", " + maxPoint.y + ", " + maxPoint.z + ")");
        }
    }

    public LocationParameter clone() {
        return (LocationParameter) super.clone();
    }
    
}
