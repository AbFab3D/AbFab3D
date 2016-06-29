/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2014
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

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;

/**
   class to represent Octahedron with given radius and center 
 */
    public class Octahedron implements TriangleProducer {
    
    double r, x, y,z;
        Vector3d 
            vx = new Vector3d(), 
            v_x  = new Vector3d(), 
            vy = new Vector3d(), 
            v_y = new Vector3d(), 
            vz = new Vector3d(), 
            v_z = new Vector3d();  

    public Octahedron (double r){
        this.r = r;
    }

    public void setCenter(double x,double y,double z){

        this.z = z;
        this.y = y;
        this.x = x;        


    }

    public void setSize(double r){
        this.r = r;
    }
    

    /**
       make octahedron of given radius and center 
    */
    public boolean getTriangles(TriangleCollector tc){ 
               
        vx.set (x+r,y,z);
        v_x.set(x-r,y,z);
        vy.set (x,y+r,z);
        v_y.set(x,y-r,z);
        vz.set (x,y,z+r);
        v_z.set(x,y,z-r);

        tc.addTri(vz, vx, vy);
        tc.addTri(vz ,vy,v_x);
        tc.addTri(vz,v_x,v_y);
        tc.addTri(vz,v_y, vx);
        
        tc.addTri(v_z, vy, vx);
        tc.addTri(v_z,v_x, vy);
        tc.addTri(v_z,v_y,v_x);
        tc.addTri(v_z, vx,v_y);

        return true;
        
    }

    /**
       make octahedron of given radius and center 
    */
    protected void makeShape(TriangleCollector tc, double x, double y, double z){
        
        
        vx.set (x+r,y,z);
        v_x.set(x-r,y,z);
        vy.set (x,y+r,z);
        v_y.set(x,y-r,z);
        vz.set (x,y,z+r);
        v_z.set(x,y,z-r);
        
        tc.addTri(vz, vx, vy);
        tc.addTri(vz ,vy,v_x);
        tc.addTri(vz,v_x,v_y);
        tc.addTri(vz,v_y, vx);
        
        tc.addTri(v_z, vy, vx);
        tc.addTri(v_z,v_x, vy);
        tc.addTri(v_z,v_y,v_x);
        tc.addTri(v_z, vx,v_y);
    }
}    
