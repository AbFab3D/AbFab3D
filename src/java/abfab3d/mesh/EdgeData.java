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

import javax.vecmath.Point3d;


/**
   class to keep info about an Edge 
*/    
public class EdgeData {
    
    int edge; // reference to Edge (to get to Vertices etc.)
    int index; // index in array of all edges for random access 
    double errorValue; // error calculated for this edge 
    Point3d point = new Point3d(); // place for candidate vertex
    int vertexUserData;
    
    public EdgeData() {
        // no user data is given 
        vertexUserData = -1;
    }
    
    public EdgeData(int userData) {
        vertexUserData = userData;
    }
    
    public void set(EdgeData ed){
        
        edge = ed.edge;
        index = ed.index;
        errorValue = ed.errorValue;
        vertexUserData = ed.vertexUserData;
        point.set(ed.point);
        
    }
    
}

