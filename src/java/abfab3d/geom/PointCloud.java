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

package abfab3d.geom;

import javax.vecmath.Vector3d;

import java.util.Vector;

import abfab3d.util.TriangleProducer;
import abfab3d.util.TriangleCollector;

import static abfab3d.util.Units.MM;

/**
   generates a cloud of small objects to represent unstructured cloud of 3D points 

 */
public class PointCloud implements TriangleProducer {
    
    
    // points are represented via 3 coordinates 
    double coord[];
    double pointSize = 0.05*MM;

    // vector to accumulate points 
    Vector<Vector3d> vpoints;

    /**
       makes empty point cloud.
       Pount can be added using add() method
     */
    public PointCloud(){
        vpoints = new Vector<Vector3d>();
    }
    /**
       accept coordinates as flat array of double
     */
    public PointCloud(double coord[]){
        this.coord = coord;
    }
    
    /**
       accept coordinates as vector of Vector3d
     */
    public PointCloud(Vector<Vector3d> points){
        
        this.coord = getCoord(points);
    }


    /**
       
     */
    public void setPointSize(double size){
        pointSize = size;
    }


    
    public void addPoint(double x, double y, double z){
        if(vpoints == null){
            vpoints = new Vector<Vector3d>();
        }
        vpoints.add(new Vector3d(x,y,z));
    }

    public boolean getTriangles(TriangleCollector collector){
        
        if(vpoints != null){
            this.coord = getCoord(vpoints);
        }

        int count = coord.length/3;
        Octa shape = new Octa(pointSize/2);
        
        for(int i = 0; i < count; i++){
            double 
                x = coord[3*i],
                y = coord[3*i+1],
                z = coord[3*i+2];
            shape.makeShape(collector, x,y,z);
        }
        return true;
        
    }

    private double [] getCoord(Vector<Vector3d> points){

        int count = points.size();
        double coord[] = new double[count*3];
        for(int i = 0; i < count; i++){
            Vector3d pnt = points.get(i);
            int i3 = i*3;
            coord[i3] = pnt.x;
            coord[i3+1] = pnt.y;
            coord[i3+2] = pnt.z;
        }
        return coord;
    }

    static class Octa {
        
        double r;
        Vector3d 
            vx = new Vector3d(), 
            v_x  = new Vector3d(), 
            vy = new Vector3d(), 
            v_y = new Vector3d(), 
            vz = new Vector3d(), 
            v_z = new Vector3d();  
        
        Octa (double r){
            this.r = r;
        }
        // vertices to make 
        
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
}
