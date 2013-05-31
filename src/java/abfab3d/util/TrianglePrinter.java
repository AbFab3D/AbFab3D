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
 * Counts triangles via TriangleCollector interface
 *
 * @author Vladimir Bulatov
 */
public class TrianglePrinter implements TriangleCollector {

    protected int count = 0;
    String f = "%10.5f"; // format for printing floating numbers 
    public TrianglePrinter(){
        
    }
   
    /**
       add triangle 
       vertices are copied into internal structure and can be reused after return       

       returns true if success, false if faiure 
       
     */
    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
        count++;
        printf("AddTri" + f + ","+ f + ","+ f + ","+ f + ","+ f + ","+ f + ","+ f + ","+ f + ","+ f + "):%d \n", 
               v0.x, v0.y, v0.z,
               v1.x, v1.y, v1.z,
               v2.x, v2.y, v2.z, count);
               
        return true;
    }

    public int getCount(){
        return count;
    }
}

