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

import abfab3d.core.TriangleCollector;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import static abfab3d.core.Output.printf;

/**
   Make point set from flat set of triangles

   @author Alan Hudson

 */
public class PointSetBuilder implements TriangleCollector {
    private int INITIAL_SIZE = 1000000;
    static final boolean DEBUG = false;
    static double TOLERANCE = 1.e-8; // vectors different less than tolerance are assumed to be equal

    PointSet ps;

    private int skip;
    private int cnt = 0;

    public PointSetBuilder(){
        this(0);
    }

    public PointSetBuilder(int skip) {
        this.skip = skip;
        ps = new PointSet(INITIAL_SIZE, 0.75f,TOLERANCE);
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
       add triangle 
       vertices are copied into internal structure and can be reused after return       

       returns true if success, false if faiure 
       
     */
    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
        cnt++;

        if (cnt < skip) {
            return true;
        }

        cnt = 0;

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

        return true;
    }

    protected int getIndex(Tuple3d t){
        return ps.add(t.x,t.y,t.z);
    }

}

