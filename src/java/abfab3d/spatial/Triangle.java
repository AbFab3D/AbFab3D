/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.spatial;

import javax.vecmath.Vector3d;

import static abfab3d.core.Output.fmt;

/**
 * Triangle storage for spatial usage.  Currently stores as floats to save storage.
 *
 * @author Alan Hudson
 */
public class Triangle {
    private float[] v0;
    private float[] v1;
    private float[] v2;
    private int id = -1;

    public Triangle(Vector3d iv0, Vector3d iv1, Vector3d iv2) {
        this(iv0, iv1, iv2, -1);
    }

    public Triangle(float[] v0, float[] v1, float[] v2) {
        this(v0, v1, v2, -1);
    }

    public Triangle(Vector3d iv0, Vector3d iv1, Vector3d iv2, int id) {
        v0 = new float[]{(float) iv0.x, (float) iv0.y, (float) iv0.z};
        v1 = new float[]{(float) iv1.x, (float) iv1.y, (float) iv1.z};
        v2 = new float[]{(float) iv2.x, (float) iv2.y, (float) iv2.z};
        this.id = id;
    }

    public Triangle(float[] v0, float[] v1, float[] v2, int id) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float[] getV0() {
        return v0;
    }

    public float[] getV1() {
        return v1;
    }

    public float[] getV2() {
        return v2;
    }

    public String toString(double unit) {
        return fmt("%6.2f %6.2f %6.2f -> %6.2f %6.2f %6.2f -> %6.2f %6.2f %6.2f",
                v0[0] / unit, v0[1] / unit, v0[2] / unit,
                v1[0] / unit, v1[1] / unit, v1[2] / unit,
                v2[0] / unit, v2[1] / unit, v2[2] / unit
        );
    }

    public boolean isDegenerateAreaMethod() {
        Vector3d a = new Vector3d(v0[0], v0[1], v0[2]);
        Vector3d b = new Vector3d(v1[0], v1[1], v1[2]);
        Vector3d c = new Vector3d(v2[0], v2[1], v2[2]);


        b.sub(a);
        c.sub(a);
        b.cross(b, c);

        double area = b.lengthSquared();

        double EPS = 1e-17f;

        if (area < EPS) {
            //System.out.println("Degenerate tri: " + f + " area: " + area);
            return true;
        }

        return false;
    }

    /**
     * Check for degenerate based on three edge length equalities
     * if any are false its degenerate:
     * a + b > c, a + c > b, b + c > a
     *
     * @return
     */
    public boolean isDegenerateLengthMethod() {

        // a = v0 - v1
        double x = v0[0] - v1[0];
        double y = v0[1] - v1[1];
        double z = v0[2] - v1[2];

        double a = Math.sqrt(x * x + y * y + z * z);

        // b is v0 - v2
        x = v0[0] - v2[0];
        y = v0[1] - v2[1];
        z = v0[2] - v2[2];

        double b = Math.sqrt(x * x + y * y + z * z);

        // c is v1 - v2
        x = v1[0] - v2[0];
        y = v1[1] - v2[1];
        z = v1[2] - v2[2];

        double c = Math.sqrt(x * x + y * y + z * z);

//        printf("a: %6.2f  b: %6.2f  c: %6.2f\n",a,b,c);
        if ((a + b > c) && (a + c > b) && (b + c > a)) return false;

        return true;
    }

    /**
     * Checks if its long and thin.  Generates garbage.
     *
     * @return
     */
    public boolean isLongThin() {
        double THIN_RATIO = 1e-7;

        Vector3d lv0 = new Vector3d(v0[0], v0[1], v0[2]);
        double a = lv0.length();

        Vector3d lv1 = new Vector3d(v1[0], v1[1], v1[2]);
        double b = lv1.length();

        Vector3d lv2 = new Vector3d(v2[0], v2[1], v2[2]);
        double c = lv2.length();


        lv1.sub(lv0);
        lv2.sub(lv0);

        Vector3d normal = new Vector3d();
        normal.cross(lv1, lv2);

        double area = normal.length();
        double perm = a * a + b * b + c * c;
        double ratio = area / perm;


        if (ratio < THIN_RATIO) {
            return true;
        }

        return false;
    }
}
