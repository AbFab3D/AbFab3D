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
package abfab3d.util;

//import java.util.Set;
//import java.util.HashSet;
import java.util.ArrayList;
import javax.vecmath.Point3d;

/**
   structure to describe edge collapse options
*/
public class EdgeCollapseParams {
    
    // if this is greater than zero, it is square of maximal length of edges 
    // allowed as result of edge collapse    
    public double maxEdgeLength2 = 0.; 
    public boolean testSurfacePinch = true;
    

}
