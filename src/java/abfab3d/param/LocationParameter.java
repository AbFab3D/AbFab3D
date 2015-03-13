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

    public LocationParameter(String name) {
        this(name, name, DEFAULT_POINT, DEFAULT_NORMAL);
    }

    public LocationParameter(String name, String desc) {
        this(name, desc, DEFAULT_POINT, DEFAULT_NORMAL);
    }

    public LocationParameter(String name, String desc, Vector3d point, Vector3d normal) {
        super(name, desc);

        if (point == null) point = DEFAULT_POINT;
        if (normal == null) normal = DEFAULT_NORMAL;

        setValue(new Vector3d[] {(Vector3d)point.clone(),(Vector3d)normal.clone()});
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
    }

    public LocationParameter clone() {
        return (LocationParameter) super.clone();
    }
}
