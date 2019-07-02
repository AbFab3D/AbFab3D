/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
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

package abfab3d.spatial;

import javax.vecmath.Vector3d;

public class Intersector {
    // Scratch Vars
    private Vector3d v0 = new Vector3d();
    private Vector3d v1 = new Vector3d();
    private Vector3d v2 = new Vector3d();
    private Vector3d normal = new Vector3d();
    private Vector3d e0 = new Vector3d();
    private Vector3d e1 = new Vector3d();
    private Vector3d e2 = new Vector3d();
    private Vector3d f = new Vector3d();
    private Vector3d vpos = new Vector3d();
    private Vector3d vmin = new Vector3d();
    private Vector3d vmax = new Vector3d();

    private Vector3d a = new Vector3d();
    private Vector3d b = new Vector3d();
    private Vector3d c = new Vector3d();


    public Intersector() {
    }

    /**
     * Does triangle overlap a voxel.
     * <p>
     * From paper: Fast 3D Triangle-Box Overlap Testing
     *
     * @param af   The first vertex
     * @param bf   The first vertex
     * @param cf  The first vertex
     * @param pos The voxel center position
     */
    public boolean intersectsTriangle(float[] af, float[] bf, float[] cf, double[] pos, double hs) {
        a.x = af[0];
        a.y = af[1];
        a.z = af[2];

        b.x = bf[0];
        b.y = bf[1];
        b.z = bf[2];

        c.x = cf[0];
        c.y = cf[1];
        c.z = cf[2];

        return intersectsTriangle(a,b,c,pos,hs);
    }

    /**
     * Does triangle overlap a voxel.
     * <p>
     * From paper: Fast 3D Triangle-Box Overlap Testing
     * TODO: this paper mentions having errors with long thin polygons
     *
     * @param a   The first vertex
     * @param b   The first vertex
     * @param c   The first vertex
     * @param pos The voxel center position
     */
    public boolean intersectsTriangle(Vector3d a, Vector3d b, Vector3d c, double[] pos, double hs) {
        // use separating axis theorem to test overlap between triangle and box
        // need to test for overlap in these directions:
        //
        // 1) the {x,y,z}-directions (actually, since we use the AABB of the
        // triangle
        // we do not even need to test these)
        // 2) normal of the triangle
        // 3) crossproduct(edge from tri, {x,y,z}-directin)
        // this gives 3x3=9 more tests

        // move everything so that the boxcenter is in (0,0,0)
        //vpos = new Vector3d(pos[0],pos[1],pos[2]);
        vpos.x = pos[0];
        vpos.y = pos[1];
        vpos.z = pos[2];
        v0.sub(a, vpos);
        v1.sub(b, vpos);
        v2.sub(c, vpos);

        // compute triangle edges
        e0.sub(v1, v0);

// TODO: Need to change y values to hHeight

        // test the 9 tests first (this was faster)
        f.absolute(e0);
        if (testAxis(e0.z, -e0.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, hs,
                hs)) {
            return false;
        }
        if (testAxis(-e0.z, e0.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, hs,
                hs)) {
            return false;
        }
        if (testAxis(e0.y, -e0.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, hs,
                hs)) {
            return false;
        }

        e1.sub(v2, v1);
        f.absolute(e1);
        if (testAxis(e1.z, -e1.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, hs,
                hs)) {
            return false;
        }
        if (testAxis(-e1.z, e1.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, hs,
                hs)) {
            return false;
        }
        if (testAxis(e1.y, -e1.x, f.y, f.x, v0.x, v0.y, v1.x, v1.y, hs,
                hs)) {
            return false;
        }

        e2.sub(v0, v2);
        f.absolute(e2);

        if (testAxis(e2.z, -e2.y, f.z, f.y, v0.y, v0.z, v1.y, v1.z, hs,
                hs)) {
            return false;
        }
        if (testAxis(-e2.z, e2.x, f.z, f.x, v0.x, v0.z, v1.x, v1.z, hs,
                hs)) {
            return false;
        }
        if (testAxis(e2.y, -e2.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, hs,
                hs)) {
            return false;
        }

        // first test overlap in the {x,y,z}-directions
        // find min, max of the triangle each direction, and test for overlap in
        // that direction -- this is equivalent to testing a minimal AABB around
        // the triangle against the AABB

        // test in X-direction
        if (min(v0.x, v1.x, v2.x) > hs
                || max(v0.x, v1.x, v2.x) < -hs) {
            return false;
        }

        // test in Y-direction
        if (min(v0.y, v1.y, v2.y) > hs
                || max(v0.y, v1.y, v2.y) < -hs) {
            return false;
        }

        // test in Z-direction
        if (min(v0.z, v1.z, v2.z) > hs
                || max(v0.z, v1.z, v2.z) < -hs) {
            return false;
        }

        // test if the box intersects the plane of the triangle
        // compute plane equation of triangle: normal*x+d=0
        normal.cross(e0, e1);
        double d = -normal.dot(v0);
        if (!planeBoxOverlap(normal, d, hs)) {
            return false;
        }

        return true;
    }

    /**
     * Test and axis intersection.
     */
    private boolean testAxis(double a, double b, double fa, double fb, double va,
                             double vb, double wa, double wb, double ea, double eb) {

        double p0 = a * va + b * vb;
        double p2 = a * wa + b * wb;
        double min, max;
        if (p0 < p2) {
            min = p0;
            max = p2;
        } else {
            min = p2;
            max = p0;
        }
        double rad = fa * ea + fb * eb;

        return (min > rad || max < -rad);
    }

    /**
     * Does a plane and box overlap.
     *
     * @param normal Normal to the plane
     * @param d      Distance
     * @param hv     Half voxel size
     */
    private boolean planeBoxOverlap(Vector3d normal, double d, double hv) {

// TODO: Need to change to include sheight
        //Vector3d vmin = new Vector3d();
        //Vector3d vmax = new Vector3d();

        if (normal.x > 0.0f) {
            vmin.x = -hv;
            vmax.x = hv;
        } else {
            vmin.x = hv;
            vmax.x = -hv;
        }

        if (normal.y > 0.0f) {
            vmin.y = -hv;
            vmax.y = hv;
        } else {
            vmin.y = hv;
            vmax.y = -hv;
        }

        if (normal.z > 0.0f) {
            vmin.z = -hv;
            vmax.z = hv;
        } else {
            vmin.z = hv;
            vmax.z = -hv;
        }
        if (normal.dot(vmin) + d > 0.0f) {
            return false;
        }
        if (normal.dot(vmax) + d >= 0.0f) {
            return true;
        }
        return false;
    }

    /**
     * Calculate the minimum of 3 values.
     *
     * @param a Value 1
     * @param b Value 2
     * @param c Value 3
     * @return The min value
     */
    private double min(double a, double b, double c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    /**
     * Calculate the maximum of 3 values.
     *
     * @param a Value 1
     * @param b Value 2
     * @param c Value 3
     * @return The min value
     */
    public double max(double a, double b, double c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

}
