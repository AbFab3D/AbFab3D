/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

import javax.vecmath.Vector3d;

/**
 * Counts triangles via TriangleCollector interface
 *
 * @author Vladimir Bulatov
 */
public class BoundingBoxCalculator implements TriangleCollector {

    protected double bounds[] = new double[]{
        Double.MAX_VALUE,Double.MIN_VALUE,
        Double.MAX_VALUE,Double.MIN_VALUE,
        Double.MAX_VALUE,Double.MIN_VALUE
    };

    public BoundingBoxCalculator(){
        
    }
   
    /**
       add triangle 
       vertices are copied into internal structure and can be reused after return       

       returns true if success, false if faiure 
       
     */
    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){

        addVect(v0);
        addVect(v1);
        addVect(v2);

        return true;
    }

    private void addVect(Vector3d v){

        if(v.x < bounds[0]) bounds[0] = v.x;
        if(v.x > bounds[1]) bounds[1] = v.x;
        if(v.y < bounds[2]) bounds[2] = v.y;
        if(v.y > bounds[3]) bounds[3] = v.y;
        if(v.z < bounds[4]) bounds[4] = v.z;
        if(v.z > bounds[5]) bounds[5] = v.z;

    }

    public void getBounds(double bnds[]){
        for(int i =0; i < 6; i++)
            bnds[i] = bounds[i];
    }
}

