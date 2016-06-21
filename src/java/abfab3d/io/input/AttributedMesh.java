/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.io.input;

import abfab3d.util.AttributedTriangleCollector;
import abfab3d.util.AttributedTriangleProducer;
import abfab3d.util.Bounds;
import abfab3d.util.DataSource;
import abfab3d.util.TriangleCollector;
import abfab3d.util.TriangleProducer;
import abfab3d.util.Vec;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import static abfab3d.util.Output.printf;

/**
 * Container for attributed triangular mesh data
 *
 * @author Alan Hudson
 */
public class AttributedMesh implements AttributedTriangleProducer, TriangleProducer, AttributedTriangleCollector, TriangleCollector {
    private ArrayList<Triangle> tris;
    private Bounds bounds;
    private DataSource attributeCalculator;
    private int dim=3; 

    public AttributedMesh() {
        this(3);
    }

    public AttributedMesh(int dataDimension) {
        this(dataDimension, 10);
    }

    /**
     * Constructor
     *
     * @param ntris - Approximate number of triangles
     */
    public AttributedMesh(int dataDimension, int ntris) {
        this.dim = dataDimension;
        tris = new ArrayList<Triangle>();
        tris = new ArrayList<Triangle>(ntris);
    }

    public boolean addAttTri(Vec v0, Vec v1, Vec v2) {
        Triangle t = new Triangle(v0,v1,v2);

        tris.add(t);
        bounds = null;

        return true;
    }

    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2) {
        Triangle t = new Triangle(v0,v1,v2);

        tris.add(t);
        bounds = null;

        return true;
    }

    public Bounds getBounds() {
        if (bounds != null) {
            return bounds;
        }

        BoundsCalculator bc = new BoundsCalculator();

        for(Triangle t : tris) {
            bc.addAttTri(t.v0,t.v1,t.v2);
        }

        double[] bounds = new double[6];
        bc.getBounds(bounds);

        return new Bounds(bounds);
    }

    public int getTriCount() {
        return tris.size();
    }

    public DataSource getAttributeCalculator() {
        return attributeCalculator;
    }

    public void setAttributeCalculator(DataSource ac) {
        attributeCalculator = ac;
    }

    public void setDataDimension(int dd) {
        this.dim = dd;
    }

    public boolean getAttTriangles(AttributedTriangleCollector tc) {
        printf("Getting att triangles\n");
        for(Triangle t : tris) {
            tc.addAttTri(t.v0, t.v1, t.v2);
        }

        return true;
    }

    public int getDataDimension() {
        return dim;
    }

    public boolean getTriangles(TriangleCollector tc) {
        printf("Getting regular triangles\n");

        Vector3d v0 = new Vector3d();
        Vector3d v1 = new Vector3d();
        Vector3d v2 = new Vector3d();

        for(Triangle t : tris) {
            v0.x = t.v0.v[0];
            v0.y = t.v0.v[1];
            v0.z = t.v0.v[2];

            v1.x = t.v1.v[0];
            v1.y = t.v1.v[1];
            v1.z = t.v1.v[2];

            v2.x = t.v2.v[0];
            v2.y = t.v2.v[1];
            v2.z = t.v2.v[2];

            tc.addTri(v0, v1, v2);
        }

        return true;
    }

    static class Triangle {
        public Vec v0;
        public Vec v1;
        public Vec v2;

        public Triangle(Vec v0, Vec v1, Vec v2) {
            this.v0 = new Vec(v0);
            this.v1 = new Vec(v1);
            this.v2 = new Vec(v2);
        }

        public Triangle(Vector3d v0, Vector3d v1, Vector3d v2) {
            this.v0 = new Vec(3);
            this.v0.v[0] = v0.x;
            this.v0.v[1] = v0.y;
            this.v0.v[2] = v0.z;

            this.v1 = new Vec(3);
            this.v1.v[0] = v1.x;
            this.v1.v[1] = v1.y;
            this.v1.v[2] = v1.z;

            this.v2 = new Vec(3);
            this.v2.v[0] = v2.x;
            this.v2.v[1] = v2.y;
            this.v2.v[2] = v2.z;
        }
    }
}
