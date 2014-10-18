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

import javax.vecmath.Point3d;

/**
   structure to describe edge collapse result 
*/
public class EdgeCollapseResult {

    public static final int 
        SUCCESS = 0, 
        FAILURE_SURFACE_PINCH = 1, 
        FAILURE_FACE_FLIP = 2,
        FAILURE_LONG_EDGE = 3;


    // Scratch vars for collapseEdge
    public Point3d p0 = new Point3d();
    public Point3d p1 = new Point3d();
    public Point3d pv1 = new Point3d();
    public Point3d pv0 = new Point3d();

    public FaceFlipChecker faceFlipChecker = new FaceFlipChecker();

    // work set for topology check 
    public StructSet v1set = new StructSet(new DefaultHashFunction());
    
    //edges removed during collapse 
    public int[] removedEdges = new int[] {-1,-1,-1};

    // new vertex created during collapse 
    public int insertedVertex;

    // removed faces count
    public int faceCount;  

    // removed edges count
    public int edgeCount;  
    
    // removed vertex count  
    public int vertexCount; 
    
    public int returnCode;

    public void reset() {
        removedEdges[0] = -1;
        removedEdges[1] = -1;
        removedEdges[2] = -1;
        insertedVertex = -1;
        edgeCount = 0;
        faceCount = 0;
        vertexCount = 0;

    }

    
}
