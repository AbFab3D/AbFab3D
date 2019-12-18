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

package abfab3d.util;

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;

import javax.vecmath.Vector3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple4d;

import java.util.Vector;

import static abfab3d.core.Units.MM;

/**
   creates a set of small objects (octahedra) to represent unstructured cloud of 3D points 

   @author Vladimir Bulatov
 */
public class PointSetArray implements TriangleProducer, PointSet  {
    
    public static final int SHAPE_OCTAHEDRON = 0; 
    public static final int SHAPE_SPHERE = 1; 

    protected int m_shapeType = SHAPE_OCTAHEDRON;
    protected int m_subdivisionLevel = 1;
    
    // points are represented via 3 coordinates 
    double coord[] = null;

    protected int m_size=0; // points count 
    protected int m_arrayCapacity = 0;
    protected int m_dataDimension =3;
    // size of geometrical shape to represent each point 
    protected double pointSize = 0.05*MM;
    
    /**
       makes empty point cloud.
       Pount can be added using add() method
     */
    public PointSetArray(){
        this(10);
    }

    public PointSetArray(int initialCapacity){

        this(initialCapacity,3);                

    }

    public PointSetArray(int initialCapacity, int dataDimension){

        // avoid wrong behavior 
        if(initialCapacity < 1) initialCapacity = 1;
        m_dataDimension = dataDimension;

        m_arrayCapacity = initialCapacity;
        coord = new double[m_dataDimension*m_arrayCapacity];
        
    }

    /**
       accept coordinates as flat array of double
     */
    public PointSetArray(double coord[]){
        this.coord = coord;
        m_arrayCapacity = coord.length/m_dataDimension;
        m_size = m_arrayCapacity;

    }
    
    /**
       accept coordinates as flat array of double
     */
    public PointSetArray(double coord[], int dataDimension){

        this.coord = coord;
        m_dataDimension = dataDimension;
        m_arrayCapacity = coord.length/m_dataDimension;
        m_size = m_arrayCapacity;

    }
    
    /**
       accept coordinates as vector of Vector3d
     */
    public PointSetArray(Vector<Vector3d> points){
        
        this.coord = getCoord(points);
        m_size = coord.length/m_dataDimension;
        m_arrayCapacity = m_size;
    }


    /**
     * Clear all point and triangle data.
     */
    public void clear() {
        m_size = 0;
        m_arrayCapacity = 10;
        coord = new double[m_dataDimension*m_arrayCapacity];
    }

    /**
       
     */
    public void setPointSize(double size){
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
       set subdivision level used for visiualization
     */
    public void setSubdivisionLevel(int level){
        m_subdivisionLevel = level;
    }

    
    public final void addPoint(double x, double y, double z){
        if(m_size >= m_arrayCapacity)
            reallocArray();

        int start = m_size*m_dataDimension;
        coord[start] = x;
        coord[start+1]= y;
        coord[start+2]= z;
        m_size++;
    }

    public final void addPoint(double x, double y, double z, double w){

        if(m_size >= m_arrayCapacity)
            reallocArray();

        int start = m_size*m_dataDimension;
        coord[start] = x;
        coord[start+1]= y;
        coord[start+2]= z;
        coord[start+3]= w;
        m_size++;
    }

    public final void addPoint(Tuple3d pnt){
        addPoint(pnt.x,pnt.y,pnt.z);
    }

    public final void addPoint(Tuple4d pnt){
        addPoint(pnt.x,pnt.y,pnt.z, pnt.w);
    }

    public final void addPoint(double pnt[]){
        if(m_dataDimension == 3){
            addPoint(pnt[0],pnt[1],pnt[2]); 
            return;
        }
        if(m_size >= m_arrayCapacity)
            reallocArray();

        int start = m_size*m_dataDimension;
        for(int i = 0; i < m_dataDimension; i++){            
            coord[start++] = pnt[i];
        }
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

    /**
       @param pnts set of points to add 
     */
    public final void addPoints(PointSet pnts){

        if(pnts instanceof PointSetArray){
            addPointSetArray((PointSetArray)pnts);
            return;
        }

        Vector3d p = new Vector3d();
        for(int k = 0; k < pnts.size(); k++){
            pnts.getPoint(k, p);
            addPoint(p.x,p.y,p.z);
        }
    }

    public final void addPointSetArray(PointSetArray pnts){

        double pnt[] = new double[m_dataDimension];
        for(int k = 0; k < pnts.size(); k++){
            pnts.getPoint(k, pnt);
            addPoint(pnt);
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
        Octa shape = new Octa(pointSize/2);
        
        for(int i = 0; i < count; i++){
            int start  = i*m_dataDimension;
            double 
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

            int start  = i*m_dataDimension;
            double 
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

    public void getPoint(int index, double point[]){

        int j = index*m_dataDimension;
        for(int i = 0; i < m_dataDimension; i++,j++){
            point[i] = coord[j];
        }
    }

    public void getPoint(int index, Tuple3d point){

        int start = index*m_dataDimension;
        point.x = coord[start];
        point.y = coord[start+1];
        point.z = coord[start+2];

    }

    public void getPoint(int index, Tuple4d point){

        int start = index*m_dataDimension;
        point.x = coord[start];
        point.y = coord[start+1];
        point.z = coord[start+2];
        point.w = coord[start+3];

    }

    public void getPoints(double pnts[]){

        System.arraycopy(this.coord, 0, pnts, 0, m_size*m_dataDimension);
        
    }

    public void setPoint(int index, Tuple3d point){

        int start = index*m_dataDimension;
        coord[start] = point.x;
        coord[start+1] = point.y;
        coord[start+2] = point.z;

    }

    public void setPoint(int index, double x, double y, double z){

        int start = index*m_dataDimension;
        coord[start] = x;
        coord[start+1] = y;
        coord[start+2] = z;

    }

    public void setPoint(int index, Tuple4d point){

        int start = index*m_dataDimension;
        coord[start] = point.x;
        coord[start+1] = point.y;
        coord[start+2] = point.z;
        coord[start+3] = point.w;

    }
    
    private void reallocArray(){

        int ncapacity = 2*m_arrayCapacity;
        double ncoord[] = new double[ncapacity*m_dataDimension];
        System.arraycopy(coord, 0, ncoord,0,coord.length);
        m_arrayCapacity = ncapacity;
        coord = ncoord;

    }


    private double [] getCoord(Vector<Vector3d> points){

        int count = points.size();
        double coord[] = new double[count*m_dataDimension];
        for(int i = 0; i < count; i++){
            Vector3d pnt = points.get(i);
            int i3 = i*m_dataDimension;
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
