/** 
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/


package abfab3d.geom;

import javax.vecmath.Vector3d;
import java.util.Vector;
import java.util.Arrays;
import java.util.HashMap;

import abfab3d.util.IPointMap;
//import abfab3d.util.PointMap;
import abfab3d.util.PointMap2;
import abfab3d.util.PointMap3;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.str;
import static java.lang.Math.*;


/**
   represents single slice 
   
   typical use - 
   1) create inastance 
   2) make many calls  addSegment(Vector3d p0, Vector3d p1)
   3) call buildContours()
   4) check if successful getSuccess()
   5) getClosedContourCount() return count of closed contoure 
   6) getClosedContourPoints(int index); return points of closed contours 

   normally for manifold mesh only closed contours will exists
 
   open contours may be presented for non-manifold mesh
   
   
 */
public interface Slice {
        
    /**
       add new segment to the slice 
     */
    public void addSegment(Vector3d p0, Vector3d p1);

    /**
       the call needed after all segments were added 
     */
    public void buildContours();

    /**
       return true or false 
     */
    public boolean getSuccess();

    public double getSliceDistance();

    public Vector3d getPointOnPlane();
    
    public int getClosedContourCount();

    public int getOpenContourCount();
    
    /**
       return contour with given index
    */
    public Contour getContour(int index);
    
    /**
       return closed contour with given index 
     */
    public Contour getClosedContour(int index);

    /**
       return open contour with given index 
     */
    public Contour getOpenContour(int index);
    
    public double[] getClosedContourPoints(int index);
        
    public double[] getOpenContourPoints(int index);
        
    public void printStat();

    public boolean testManifold();


} // interface Slice 