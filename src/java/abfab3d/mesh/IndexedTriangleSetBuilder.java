/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.mesh;

import abfab3d.util.StructDataDefinition;
import abfab3d.util.StructMixedData;
import abfab3d.util.TriangleCollector;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.HashMap;

import static abfab3d.util.Output.printf;

/**
   class to make indexed triangle set from flat set of triangles

   @author Vladimir Bulatov

 */
public class IndexedTriangleSetBuilder implements TriangleCollector {
    private int INITIAL_SIZE = 1000000;
    static final boolean DEBUG = false;
    static double TOLERANCE = 1.e-8; // vectors different less than tolerance are assumed to be equal

    StructMixedData faces = null;
    PointSet ps;


    public IndexedTriangleSetBuilder(){
        faces = new StructMixedData(FaceList.DEFINITION, INITIAL_SIZE);
        ps = new PointSet(INITIAL_SIZE, 0.75f,TOLERANCE);
    }

    public IndexedTriangleSetBuilder(int expectedFaces) {
        faces = new StructMixedData(FaceList.DEFINITION, expectedFaces);
        // from Euler formula V-E+F=2 for simple surfaces 
        float loadFactor = 0.75f;
        int estimatedFaces = (int)((expectedFaces/2 + 2)/loadFactor);
        System.out.println("Estimated Faces: " + estimatedFaces);
        ps = new PointSet(estimatedFaces, loadFactor, TOLERANCE);
    }

    /**
     * Get the vertices.
     *
     * @return array x,y,z vertices
     */
    public double[] getVertices(){
        double[] ret_val = ps.getPoints();

        return ret_val;
    }

    /**
       
     */
    public int[] getFaces(){
        return FaceList.toArray(faces);
    }

    /**
       add triangle 
       vertices are copied into internal structure and can be reused after return       

       returns true if success, false if faiure 
       
     */
    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
        if(DEBUG){
            Vector3d p1 = new Vector3d();
            Vector3d p2 = new Vector3d();
            Vector3d cp = new Vector3d();
            p1.sub(v1, v0);
            p2.sub(v2, v0);
            cp.cross(p1, p2);
            if(cp.dot(cp) < 1.e-18){
                printf("degenerate triangle (%s, %s, %s)\n", v0, v1, v2);            
                return false;
            }
        }

/*
        System.out.println("v0 = new Vector3d(" + v0.x + "," + v0.y + "," + v0.z + ");");
        System.out.println("v1 = new Vector3d(" + v1.x + "," + v1.y + "," + v1.z + ");");
        System.out.println("v2 = new Vector3d(" + v2.x + "," + v2.y + "," + v2.z + ");");
        System.out.println("its1.addTri(v0,v1,v2);");
        System.out.println("its2.addTri(v0,v1,v2);");
*/
        int f0 = getIndex(v0);
        int f1 = getIndex(v1);
        int f2 = getIndex(v2);

        if(f0 == f1 ||
           f1 == f2 || 
           f2 == f0) {
            if(DEBUG)
                printf("BAD FACE [%d, %d, %d] (%s, %s, %s)\n", f0, f1, f2, v0, v1, v2);
            return false;
        } 

        FaceList.set(f0,f1,f2,faces,faces.addItem());
        //printf("add face:[%3d, %3d, %3d]\n", f0,f1,f2);
        return true;
    }

    protected int getIndex(Tuple3d t){
        return ps.add(t.x,t.y,t.z);
    }

}

class FaceList extends StructDataDefinition {
    public static final StructDataDefinition DEFINITION = new FaceList();

    public static final int INT_DATA_SIZE = 3;

    // int positions
    public static final int POS_X = 0;
    public static final int POS_Y = 1;
    public static final int POS_Z = 2;

    public static int createEntry(int x, int y, int z, StructMixedData dest) {
        int destIdx = dest.addItem();
        set(x,y,z, dest, destIdx);

        return destIdx;
    }

    public static void set(int x, int y, int z, StructMixedData dest, int destIdx) {
        int int_pos = destIdx * INT_DATA_SIZE;
        int[] int_data = dest.getIntData();

        int_data[int_pos + POS_X] = x;
        int_data[int_pos + POS_Y] = y;
        int_data[int_pos + POS_Z] = z;
    }

    public static void get(StructMixedData src, int srcIdx, int[] face) {
        int int_pos = srcIdx * INT_DATA_SIZE;
        int[] int_data = src.getIntData();

        face[0] = int_data[int_pos +  + POS_X];
        face[1] = int_data[int_pos +  + POS_Y];
        face[2] = int_data[int_pos +  + POS_Z];
    }

    public static int[] toArray(StructMixedData src) {
        int[] ret_val = src.getIntData();

        // need to trim to count
        int len = src.getLength() * INT_DATA_SIZE;
        if (ret_val.length != len) {
            int[] vals = ret_val;
            ret_val = new int[len];

            System.arraycopy(vals,0,ret_val,0,len);
        }

        return ret_val;
    }

    public int getIntDataSize() {
        return INT_DATA_SIZE;
    }
}

