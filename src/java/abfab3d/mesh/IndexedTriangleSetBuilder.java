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

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;


import abfab3d.util.TriangleCollector;



import static abfab3d.util.Output.printf;

/**
   class to make indexed triangle set from flat set of triangles

   @author Vladimir Bulatov

 */
public class IndexedTriangleSetBuilder implements TriangleCollector {

    static final boolean DEBUG = false;

    private ArrayList<int[]> m_faces = new ArrayList<int[]>();
    private HashMap<Point3dW,Integer> m_tvertices = new HashMap<Point3dW,Integer>();
    private ArrayList<Point3dW> m_vertices = new ArrayList<Point3dW>();
    

    public IndexedTriangleSetBuilder(){
        m_faces = new ArrayList<int[]>();
        m_tvertices = new HashMap<Point3dW,Integer>();
        m_vertices = new ArrayList<Point3dW>();
    }

    public IndexedTriangleSetBuilder(int expectedVerts, int expectedFaces) {
        m_faces = new ArrayList<int[]>(expectedFaces);
        m_tvertices = new HashMap<Point3dW,Integer>(expectedVerts);
        m_vertices = new ArrayList<Point3dW>(expectedVerts);
    }
    /**

     */
    public Point3d[] getVertices(){

        return m_vertices.toArray(new Point3d[0]);

    }

    /**
       
     */
    public int [][] getFaces(){
        return m_faces.toArray(new int[0][0]);
    }

    int add_cnt = 0;
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

        int[] face = new int[]{f0, f1, f2};

        m_faces.add(face);
/*
        if (add_cnt == 100000) {
            add_cnt++;
            System.out.println("Adding v0: " + v0);
            System.out.println("Adding face: " + java.util.Arrays.toString(face));

            int[][] findex = m_faces.toArray(new int[0][0]);
            System.out.println("Faces:" + findex.length);
            for(int i=0; i < findex.length; i++) {
                System.out.println(java.util.Arrays.toString(findex[i]));
            }


        }
*/
        //printf("add face:[%3d, %3d, %3d]\n", f0,f1,f2);
        return true;
    }

    protected int getIndex(Tuple3d t){
        
        Point3dW tw = new Point3dW(t.x, t.y, t.z);
        Integer ind = m_tvertices.get(tw);
        
        if(ind == null){
            ind = new Integer(m_vertices.size());
            m_tvertices.put(tw,ind);
            m_vertices.add(tw);
        }

        return ind.intValue();

    }

    /**
       
     */
    public static class Point3dW extends Point3d {
        static final double  // arbitrary constants for hashcode calculations
            CX = 14256.789,
            CY = 26367.891,
            CZ = 57672.981,
            CW = 35556.955;

        static double TOLERANCE = 1.e-8; // vectors different less than tolerance are assumed to be equal

        public Point3dW(double x, double y, double z){
            super(x,y,z);

        }

        public int hashCode(){

//            return (int) (31 * 31 * x + 31 * y + z);

            //hcCnt++;

            return (int)(CX*x + CY * y + CZ * z + CW);
            
        }

        public boolean equals(Object obj){

            //eqCnt++;

            Point3d p = (Point3d)obj;
            double d = distanceLinf(p);
            if(d < TOLERANCE)
                return true;
            else 
                return false;

        }

    }
}


