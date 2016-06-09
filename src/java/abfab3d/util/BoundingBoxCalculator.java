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


import static abfab3d.util.Output.printf;


/**
 * Counts bounding box of a set of triangles via TriangleCollector interface
 *
 * @author Vladimir Bulatov
 */
public class BoundingBoxCalculator implements TriangleCollector, TriangleCollector2 {

    static boolean DEBUG = false;
    int debugCount = 100;
    private int triCount;

    protected double bounds[] = new double[]{
        Double.MAX_VALUE,-Double.MAX_VALUE,
        Double.MAX_VALUE,-Double.MAX_VALUE,
        Double.MAX_VALUE,-Double.MAX_VALUE};
    
    public BoundingBoxCalculator(){
        
    }
   
    /**
       add triangle 
       vertices are copied into internal structure and can be reused after return       

       returns true if success, false if faiure 
       
     */
    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
        if(DEBUG && debugCount-- > 0)
            printf("bounds: %s, %s, %s, %s, %s, %s\n ", bounds[0],bounds[1],bounds[2],bounds[3],bounds[4],bounds[5]);

        triCount++;
        addVect(v0);
        addVect(v1);
        addVect(v2);

        if(DEBUG && debugCount > 0)
            printf(" -> bounds: %s, %s, %s, %s, %s, %s\n ", bounds[0],bounds[1],bounds[2],bounds[3],bounds[4],bounds[5]);
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

    @Override
    public boolean addTri2(Vec v0, Vec v1, Vec v2) {
        if(DEBUG && debugCount-- > 0)
            printf("bounds: %s, %s, %s, %s, %s, %s\n ", bounds[0],bounds[1],bounds[2],bounds[3],bounds[4],bounds[5]);

        triCount++;
        addVect(v0);
        addVect(v1);
        addVect(v2);

        if(DEBUG && debugCount > 0)
            printf(" -> bounds: %s, %s, %s, %s, %s, %s\n ", bounds[0],bounds[1],bounds[2],bounds[3],bounds[4],bounds[5]);
        return true;
    }

    private void addVect(Vec v){

        if(v.v[0] < bounds[0]) bounds[0] = v.v[0];
        if(v.v[0] > bounds[1]) bounds[1] = v.v[0];
        if(v.v[1] < bounds[2]) bounds[2] = v.v[1];
        if(v.v[1] > bounds[3]) bounds[3] = v.v[1];
        if(v.v[2] < bounds[4]) bounds[4] = v.v[2];
        if(v.v[2] > bounds[5]) bounds[5] = v.v[2];

    }

    public double [] getBounds(double bnds[]){
        if(bnds == null)
            bnds = new double[6];
        
        for(int i =0; i < 6; i++)
            bnds[i] = bounds[i];
        return bnds;
    }

    public Bounds getBounds(){

        return new Bounds(bounds);

    }
    
    public double[] getRoundedBounds(double voxelSize){
        double b[] = new double[6];
        System.arraycopy(bounds, 0, b, 0, 6);        
        b = MathUtil.roundBounds(b, voxelSize);
        return b;
    }

    /**
     * Get the triangle count.  Only available after getTriangles has been called on the source
     * @return
     */
    public int getTriangleCount() {
        return triCount;
    }
}

