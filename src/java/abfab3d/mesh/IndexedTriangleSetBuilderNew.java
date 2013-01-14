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
public class IndexedTriangleSetBuilderNew implements TriangleCollector {

    static final boolean DEBUG = false;
    static double TOLERANCE = 1.e-8; // vectors different less than tolerance are assumed to be equal

    ArrayList<int[]> m_faces = new ArrayList<int[]>();
    PointSet ps;


    public IndexedTriangleSetBuilderNew(){
        m_faces = new ArrayList<int[]>();

        // TODO: avoiding rehash for now, do something better
        ps = new PointSet(4000000, 0.75f,TOLERANCE);
    }

    public IndexedTriangleSetBuilderNew(int expectedVerts, int expectedFaces) {
        m_faces = new ArrayList<int[]>(expectedFaces);

        ps = new PointSet((int) (expectedVerts * 1.26f), 0.75f,TOLERANCE);
    }

    /**
     * Get the vertices.
     *
     * @return array x,y,z vertices
     */
    public double[] getVertices(){
        double[] ret_val = ps.getPoints();

        System.out.println("Verts tried: " + try_cnt + " actual: " + (ret_val.length / 3));
        return ret_val;
    }

    /**
       
     */
    public int[] getFaces(){
        int len = m_faces.size();
        int[] ret_val = new int[len * 3];
        int idx = 0;

        for(int i=0; i < len; i++) {
            int[] val = m_faces.get(i);

            ret_val[idx++] = val[0];
            ret_val[idx++] = val[1];
            ret_val[idx++] = val[2];
        }
        return ret_val;
    }

    int try_cnt = 0;

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

        if (add_cnt < 3) {
            add_cnt++;
            System.out.println("Adding v0: " + v0);
            System.out.println("Adding face: " + java.util.Arrays.toString(face));
        }
        m_faces.add(face);
        //printf("add face:[%3d, %3d, %3d]\n", f0,f1,f2);
        return true;
    }

    protected int getIndex(Tuple3d t){

        try_cnt++;

        return ps.add(t.x,t.y,t.z);
    }

}


