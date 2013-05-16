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

    private int INITIAL_SIZE = 10000;
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
        int expectedVertices = 2*expectedFaces;
//        printf("***expectedVertices: %d\n", expectedVertices);

        ps = new PointSet(expectedVertices, loadFactor, TOLERANCE);
    }

    public void clear(){

        if(faces != null)
            faces.clear();

        if(ps != null)
            ps.clear();
    }

    /**
     * Get the vertices.
     *
     * @return array x,y,z vertices
     */
    public double[] getVertices(){

        return ps.getPoints();

    }

    /**
       return vertices in the given array of x,y,z values or allocates new array if not enough space 
     */
    public double[] getVertices(double vert[]){

        return ps.getPoints(vert);

    }

    /**
       returns count of vertices 
     */
    public int getVertexCount(){

        return ps.getPointCount();

    }


    /**
       
     */
    public int[] getFaces(){
        return FaceList.toArray(faces);
    }

    public int[] getFaces(int farray[]){

        return FaceList.toArray(faces, farray);

    }

    /**
       
     */
    public int getFaceCount(){

        return FaceList.getCount(faces);

    }

    /**
       feeds traingles to TriangleCollector interface
     */
    public void getTriangles(TriangleCollector tcollector){
        
        Vector3d 
            v0 = new Vector3d(), 
            v1 = new Vector3d(), 
            v2 = new Vector3d();
        int face[] = new int[3];
        
        double pnt[] = getVertices();

        int fcount = FaceList.getCount(faces);
        //printf("fcount: %d \n", fcount);
        //printf("pnt count: %d \n", pnt.length);
        
        for(int i = 0; i < fcount; i++){

            FaceList.get(faces, i, face);
            int i0 = 3*face[0];
            int i1 = 3*face[1];
            int i2 = 3*face[2];
            //printf("face: %d %d %d\n", i0, i1, i2);

            v0.set(pnt[i0], pnt[i0 + 1], pnt[i0 + 2]);
            v1.set(pnt[i1], pnt[i1 + 1], pnt[i1 + 2]);
            v2.set(pnt[i2], pnt[i2 + 1], pnt[i2 + 2]);           
            tcollector.addTri(v0,v1,v2);
        }        
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

