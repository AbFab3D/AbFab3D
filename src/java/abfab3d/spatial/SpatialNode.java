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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

/**
 * An Octree node.  Contains a spatial bounds and a list of triangles inside
 *
 * @author Alan Hudson
 */
public class SpatialNode {
    private static final boolean DEBUG = false;

    private double[] center;
    private double size;
    private double hsize;
    private SpatialNode[] children;

    private ArrayList<Triangle> tris = new ArrayList<>();

    public SpatialNode(double cx, double cy, double cz, double size) {
        this.center = new double[]{cx,cy,cz};
        this.size = size;
        this.hsize = size / 2;
    }

    public SpatialNode(double[] center, double size) {
        this.center = new double[]{center[0], center[1], center[2]};
        this.size = size;
        this.hsize = size / 2;
    }

    public void addTriangle(Triangle tri, Intersector isect,int maxTris) {
        if (children == null) {
            tris.add(tri);
            if (tris.size() > maxTris) {
                split(isect,maxTris);
            }
        } else {
            // find the right child
            for(SpatialNode child : children) {
                if (child.contains(isect,tri)) {
                    child.addTriangle(tri,isect,maxTris);
                }
            }
        }
    }

    public List<Triangle> getTriangles() {
        return tris;
    }

    public double[] getCenter() {
        return center;
    }

    public double getSize() {
        return size;
    }

    /**
     * Does this cell contain a triangle
     *
     * @param tri
     * @return
     */
    public boolean contains(Intersector isect, Triangle tri) {
        float[] af = tri.getV0();
        float[] bf = tri.getV1();
        float[] cf = tri.getV2();

        if (isect.intersectsTriangle(af, bf, cf, center, hsize)) {
            return true;
        }

        return false;
    }
    
    // Splits this node into 8, redistributes its triangles
    private void split(Intersector isect, int maxTris) {
        double dim = size * 0.25;
        
        children = new SpatialNode[8];
        
        children[0] = new SpatialNode(center[0] - dim, center[1] - dim, center[2] + dim, 2*dim);
        children[1] = new SpatialNode(center[0] - dim, center[1] - dim, center[2] - dim, 2*dim);
        children[2] = new SpatialNode(center[0] - dim, center[1] + dim, center[2] + dim, 2*dim);
        children[3] = new SpatialNode(center[0] - dim, center[1] + dim, center[2] - dim, 2*dim);
        children[4] = new SpatialNode(center[0] + dim, center[1] - dim, center[2] + dim, 2*dim);
        children[5] = new SpatialNode(center[0] + dim, center[1] - dim, center[2] - dim, 2*dim);
        children[6] = new SpatialNode(center[0] + dim, center[1] + dim, center[2] + dim, 2*dim);
        children[7] = new SpatialNode(center[0] + dim, center[1] + dim, center[2] - dim, 2*dim);

        if (DEBUG) {
            printf("Splitting: center: %6.2f %6.2f %6.2f  size: %6.2f\n",center[0]/MM,center[1]/MM,center[2]/MM,
                    size/MM);
        }
        for(Triangle t : tris) {
            for(int i=0; i < 8; i++) {
                /*
                if (DEBUG) {
                    printf("Test: %s -> center: %6.2f %6.2f %6.2f  size: %6.2f  -->",t.toString(MM),
                            children[i].center[0]/MM,children[i].center[1]/MM,children[i].center[2]/MM,
                            children[i].getSize()/MM);
                }
                */
                if (children[i].contains(isect,t)) {
                    //if (DEBUG) printf(" IN\n");
                    children[i].addTriangle(t,isect,maxTris);
                } else {
                    //if (DEBUG) printf(" OUT\n");
                }
            }
        }

        tris.clear();
    }

    public SpatialNode[] getChildren() {
        return children;
    }
}
