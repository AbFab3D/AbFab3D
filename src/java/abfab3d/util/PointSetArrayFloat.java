/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
 *                                Java Source
 *  
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *  
 * ***************************************************************************
 */

package abfab3d.util;

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.Vector;

import static abfab3d.core.Units.MM;

/**
   creates a set of small objects (octahedra) to represent unstructured cloud of 3D points 

   @author Vladimir Bulatov
 */
public class PointSetArrayFloat implements TriangleProducer, PointSet  {
    
    public static final int SHAPE_OCTAHEDRON = 0; 
    public static final int SHAPE_SPHERE = 1; 

    int m_shapeType = SHAPE_OCTAHEDRON;
    int m_subdivisionLevel = 1;
    
    // points are represented via 3 coordinates 
    float coord[] = null;

    int m_size=0; // points count 
    int m_arrayCapacity = 0;
    
    // size of geometrical shape to represent each point 
    double pointSize = 0.05*MM;
    
    /**
       makes empty point cloud.
       Pount can be added using add() method
     */
    public PointSetArrayFloat(){
        this(10);
    }

    public PointSetArrayFloat(int initialCapacity){

        // avoid wrong behavior 
        if(initialCapacity < 1) initialCapacity = 1;
            
        m_arrayCapacity = initialCapacity;
        coord = new float[3*m_arrayCapacity];
        
    }

    /**
       accept coordinates as flat array of float
     */
    public PointSetArrayFloat(float coord[]){
        this.coord = coord;
        m_arrayCapacity = coord.length/3;
        m_size = m_arrayCapacity;

    }
    
    /**
       accept coordinates as vector of Vector3d
     */
    public PointSetArrayFloat(Vector<Vector3f> points){
        
        this.coord = getCoord(points);
        m_size = coord.length/3;
        m_arrayCapacity = m_size;
    }


    /**
     * Clear all point and triangle data.
     */
    public void clear() {
        m_size = 0;
        m_arrayCapacity = 10;
        coord = new float[3*m_arrayCapacity];
    }

    /**
       
     */
    public void setPointSize(float size){
        pointSize = size;
    }

    /**
       set type of shape to represent exported points 
       possible values are 
       SHAPE_OCTAHEDRON (default) 
       SHAPE_SPHERE
       
     */
    public void setShapeType(int type){

        m_shapeType = type;

    }

    /**
       set subdivision level used to spheres 
     */
    public void setSubdivisionLevel(int level){
        m_subdivisionLevel = level;
    }

    
    public final void addPoint(double x, double y, double z){
        if(m_size >= m_arrayCapacity)
            reallocArray();

        int start = m_size*3;
        coord[start] = (float)x;
        coord[start+1]= (float)y;
        coord[start+2]= (float)z;
        m_size++;
    }

    /**
       @param pnts Vector of points (subclasses of Tuple3d) 
     */
    public final void addPoints(Vector pnts){

        for(int k = 0; k < pnts.size(); k++){
            Tuple3d p = (Tuple3d)pnts.get(k);
            addPoint(p.x,p.y,p.z);
        }
    }

    public boolean getTriangles(TriangleCollector collector){
        switch(m_shapeType){
        default: 
        case SHAPE_OCTAHEDRON: 
            return getTrianglesOcta(collector);
        case SHAPE_SPHERE: 
            return getTrianglesSphere(collector);
        }
    }

    public boolean getTrianglesOcta(TriangleCollector collector){
        
        int count = m_size;
        Octa shape = new Octa((float)pointSize/2);
        
        for(int i = 0; i < count; i++){
            int start  = i*3;
            float 
                x = coord[start],
                y = coord[start+1],
                z = coord[start+2];
            shape.makeShape(collector, x,y,z);
        }
        return true;
        
    }

    public boolean getTrianglesSphere(TriangleCollector collector){
        
        int count = m_size;
        
        for(int i = 0; i < count; i++){

            int start  = i*3;
            float 
                x = coord[start],
                y = coord[start+1],
                z = coord[start+2];
            TriangulatedSphere shape = new TriangulatedSphere(pointSize/2, new Vector3d(x,y,z), m_subdivisionLevel);
            shape.getTriangles(collector);
        }
        return true;
        
    }


    /**
       interface PointSet 
     */
    public int size(){

        return m_size;

    }

    public void getPoint(int index, Tuple3d point){

        int start = index*3;
        point.x = coord[start];
        point.y = coord[start+1];
        point.z = coord[start+2];

    }
    
    private void reallocArray(){

        int ncapacity = 2*m_arrayCapacity;
        float ncoord[] = new float[ncapacity*3];
        System.arraycopy(coord, 0, ncoord,0,coord.length);
        m_arrayCapacity = ncapacity;
        coord = ncoord;

    }


    private static float [] getCoord(Vector<Vector3f> points){

        int count = points.size();
        float coord[] = new float[count*3];
        for(int i = 0; i < count; i++){
            Vector3f pnt = points.get(i);
            int i3 = i*3;
            coord[i3] = pnt.x;
            coord[i3+1] = pnt.y;
            coord[i3+2] = pnt.z;
        }
        return coord;
    }

    static class Octa {
        
        float r;
        Vector3d 
            vx = new Vector3d(), 
            v_x  = new Vector3d(), 
            vy = new Vector3d(), 
            v_y = new Vector3d(), 
            vz = new Vector3d(), 
            v_z = new Vector3d();  
        
        Octa (float r){
            this.r = r;
        }
        // vertices to make 
        
        /**
           make octahedron of given radius and center 
        */
        protected void makeShape(TriangleCollector tc, float x, float y, float z){
            
            
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
