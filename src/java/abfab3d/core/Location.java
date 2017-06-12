/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */
package abfab3d.core;

import javax.vecmath.Vector3d;

/**
 * Location data, contains a point and normal
 *
 * @author Alan Hudson
 */
public class Location implements Cloneable {
    private Vector3d point;
    private Vector3d normal;

    public Location(Vector3d point, Vector3d normal) {
        if (point != null) this.point = new Vector3d(point);
        if (normal != null) this.normal = new Vector3d(normal);
    }

    public Vector3d getPoint() {
        return point;
    }

    public void setPoint(Vector3d point) {
        this.point = point;
    }

    public Vector3d getNormal() {
        return normal;
    }

    public void setNormal(Vector3d normal) {
        this.normal = normal;
    }

    public Location clone() {
        try {
            return (Location) super.clone();
        } catch(CloneNotSupportedException cnse) {
            // Should never happen
            cnse.printStackTrace();
        }

        return null;
    }
}
