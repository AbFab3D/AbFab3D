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

import java.util.Set;
import java.util.HashSet;
import javax.vecmath.Point3d;

/**
   structure to describe edge collapse event 
*/
public class EdgeCollapseData {

    // input data 
    Edge edgeToCollapse; 
    Point3d point;       // location of new vertex 

    // return data 
    Set<Edge> removedEdges = new HashSet<Edge>(); 
    int faceCount;  // removed faces count
    int edgeCount;  // removed edges count
    int vertexCount; // removed vertex count        
}
