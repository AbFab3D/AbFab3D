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

//import java.util.Set;
//import java.util.HashSet;
import java.util.ArrayList;
import javax.vecmath.Point3d;

/**
   structure to describe edge collapse result 
*/
public class EdgeCollapseResult {
    
    //edges removed during collapse 
    public ArrayList<Edge> removedEdges = new ArrayList<Edge>(); 

    // new vertex created during collapse 
    public Vertex insertedVertex;  

    // removed faces count
    public int faceCount;  

    // removed edges count
    public int edgeCount;  
    
    // removed vertex count  
    public int vertexCount; 


}
