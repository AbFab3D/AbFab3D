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

import abfab3d.util.PointSet;
import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static abfab3d.core.Units.MM;

/**
 * A cloud of small objects to represent unstructured cloud of 3D points.
 * Optimized for dynamic usage.  This class is not thread safe.
 *
 * @author Alan Hudson
 */
public class PointCloudDynamic implements TriangleProducer, PointSet  {
    // points are represented via 3 coordinates
    private List<Vector3d> points = null;

    // size of geometrical shape to represent each point
    private double pointSize = 0.05*MM;

    public PointCloudDynamic() {
        points = new LinkedList<Vector3d>();
    }

    /**
       accept coordinates as vector of Vector3d
     */
    public PointCloudDynamic(List<Vector3d> points){
        this.points = new ArrayList<Vector3d>(points);
    }

    public void setPointSize(double size){
        pointSize = size;
    }


    public List<Vector3d> getPoints() {
        return points;
    }

    public void removePoint(Vector3d pnt) {
        if (!points.remove(pnt)) {
            throw new IllegalArgumentException("Cannot find point: " + pnt);
        }
    }

    public final void addPoint(double x, double y, double z){
        points.add(new Vector3d(x, y, z));
    }

    /**
     * Clear all point and triangle data.
     */
    public void clear() {
        points.clear();
    }

    public boolean getTriangles(TriangleCollector collector){
        
        Octa shape = new Octa(pointSize/2);

        for(Vector3d pnt : points) {
            shape.makeShape(collector, pnt.x,pnt.y,pnt.z);
        }
        return true;
    }


    /**
       interface PointSet 
     */
    public int size(){
        return points.size();
    }

    public void getPoint(int index, Tuple3d point){

        Vector3d p = points.get(index);
        point.x = p.x;
        point.y = p.y;
        point.z = p.z;
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
