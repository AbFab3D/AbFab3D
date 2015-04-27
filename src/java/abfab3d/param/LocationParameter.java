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
public class LocationParameter extends Parameter implements Cloneable {
    private static final Vector3d DEFAULT_POINT = new Vector3d(0,0,0);
    private static final Vector3d DEFAULT_NORMAL = new Vector3d(0,0,0);
    
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
    	
        setMinPoint(minPoint);
        setMaxPoint(maxPoint);

        setValue(new Vector3d[] {(Vector3d)point.clone(),(Vector3d)normal.clone()});
        defaultValue = value;
    }
    
    /**
     * Set the min location point
     * @param value
     */
    public void setMinPoint(Vector3d minPoint) {
    	if (this.minPoint == null) this.minPoint = new Vector3d();
    	
        this.minPoint.x = minPoint.x;
        this.minPoint.y = minPoint.y;
        this.minPoint.z = minPoint.z;
    }
    
    /**
     * Set the max location point
     * @param value
     */
    public void setMaxPoint(Vector3d maxPoint) {
    	if (this.maxPoint == null) this.maxPoint = new Vector3d();
    	
        this.maxPoint.x = maxPoint.x;
        this.maxPoint.y = maxPoint.y;
        this.maxPoint.z = maxPoint.z;
    }
    
    public Vector3d getMinPoint() {
    	return minPoint;
    }
    
    
    public Vector3d getMaxPoint() {
    	return maxPoint;
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
        Vector3d point = ((Vector3d[])value)[0];
        validatePoint(val);
        point.x = val.x;
        point.y = val.y;
        point.z = val.z;
    }

    public void setNormal(Vector3d val) {
        Vector3d normal = ((Vector3d[])value)[1];
        normal.x = val.x;
        normal.y = val.y;
        normal.z = val.z;
    }

    public Vector3d getPoint() {
        return ((Vector3d[])value)[0];
    }

    public Vector3d getNormal() {
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
