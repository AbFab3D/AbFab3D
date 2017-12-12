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

import java.util.Vector;
import java.awt.geom.Path2D;
import java.awt.geom.Area;
import java.awt.Shape;
import java.awt.geom.PathIterator;

import org.j3d.geom.TriangulationUtils;

import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;

import abfab3d.core.MathUtil;
import abfab3d.core.TriangleProducer;
import abfab3d.core.TriangleCollector;
import abfab3d.core.VecTransform;
import abfab3d.core.Vec;
import abfab3d.core.Transformer;
import abfab3d.core.Initializable;


import static abfab3d.core.Output.printf;
import static java.lang.Math.*;

/**
   class represents 3d polygon and converts it into triangles suitable for rendering 
   polygon can contain multiple parts and may have self intersections 
   
   @author Vladimir Bulatov
 */
public class Polygon implements TriangleProducer, Transformer {

    static final boolean DEBUG = false;
    // 
    // single subcycle of polygon 
    //
    PointSetArray m_part = null; 

    Vector<PointSet> m_polygon = new Vector<PointSet>();

    // optional transform to apply to generated triangles 
    VecTransform m_transform = null;
    PointSet m_triangles = null;
    TriangulationUtils m_triangulator = new TriangulationUtils();

    int m_windingRule = Path2D.WIND_EVEN_ODD;

    public static final int WIND_EVEN_ODD = Path2D.WIND_EVEN_ODD;
    public static final int WIND_NON_ZERO = Path2D.WIND_NON_ZERO;

    public Polygon(){
        this(WIND_EVEN_ODD);
    }

    public Polygon(int windingRule){
        m_windingRule = windingRule;
    }

    public Polygon(Shape shape){

        append(shape);

    }

    

    public void append(Shape shape) {

        m_triangles = null;
        // close current part
        close();
        Vector<PointSet> contours = getContours(shape, false);        
        
        m_polygon.addAll(contours);
    }

    

    public void moveTo(Vector3d pnt){
        moveTo(pnt.x, pnt.y, pnt.z);
    }

    
    /**
       start new polygon part 
     */
    public void moveTo(double x, double y, double z){

        m_triangles = null;

        if(m_part != null){
            m_polygon.add(m_part);            
        }
        m_part = new PointSetArray();
        m_part.addPoint(x,y,z);
    }

    /**
       convenience method 
     */
    public void lineTo(Vector3d pnt){

        lineTo(pnt.x, pnt.y, pnt.z);

    }


    /**
       add nend point for next edge 
     */
    public void lineTo(double x, double y, double z){

        m_triangles = null;
        if(m_part == null){
            throw new RuntimeException("Polygon has no initial point");
        }
        m_part.addPoint(x,y,z);
        
    }

    public void close(){

        m_triangles = null;

        if(m_part != null){
            m_polygon.add(m_part);            
            m_part = null;
        }        
    }

    /**
       retrurn triangulation of the polygon 
     */
    public boolean getTriangles(TriangleCollector triangleCollector){

        if(m_part != null){
            // store last unclosed part 
            m_polygon.add(m_part);
            m_part = null;
        }

        if(m_triangles == null)
            m_triangles = createTriangulatedPoly(m_polygon);

        int triCount = m_triangles.size()/3;

        Vector3d 
            p0 = new Vector3d(),
            p1 = new Vector3d(),
            p2 = new Vector3d();

        for(int k = 0; k < triCount; k++){
            
            m_triangles.getPoint(k*3  , p0);
            m_triangles.getPoint(k*3+1, p1);
            m_triangles.getPoint(k*3+2, p2);

            if(DEBUG) printf("  tri:(%7.5f, %7.5f,%7.5f)(%7.5f, %7.5f,%7.5f)(%7.5f, %7.5f,%7.5f)\n", 
                             p0.x,p0.y,p0.z,p1.x,p1.y,p1.z,p2.x,p2.y,p2.z);            
            if(m_transform != null) {
                transform(p0);
                transform(p1);
                transform(p2);
            }

            // transform here if needed 
            triangleCollector.addTri(p0, p1, p2);
        }
        return true;

    }

    protected void transform(Vector3d p){
        Vec v = new Vec(6);
        v.set(p);
        m_transform.transform(v, v);
        v.get(p);
    
    }

    protected PointSet createTriangulatedPoly(Vector<PointSet> polygon){

        //
        // outline of algorithm 
        //
        // find plane of the polygon 
        // transform polygin into plane 
        // regularize polygon (remove self intersections) make Path2D and tranform it into Area
        // triangulate 2d polygon 
        // transform triangles back into polygon plane 
        //

        // polygon in xy plane  
        Vector<PointSet> poly2 = new Vector<PointSet>();

        // transform polygon into plane 
        Vector3d normal = getNormal();
        Vector3d center = getCenter();
        printf("polygon normal: %s", normal);
        Vector3d axisZ = new Vector3d(0,0,1);
        AxisAngle4d aa = MathUtil.getAxisAngle(normal, axisZ);
        Matrix3d mat = new Matrix3d();
        Matrix3d mat_inv = new Matrix3d();
        mat.set(aa);
        mat_inv.set(new AxisAngle4d(aa.x,aa.y,aa.z,-aa.angle));

        Vector3d pnt = new Vector3d();

        for(int i = 0; i < polygon.size(); i++){

            PointSet part = polygon.get(i);
            PointSet part2 = new PointSetArray();
            for(int k = 0; k < part.size(); k++) {
                part.getPoint(k, pnt);
                pnt.sub(center);
                mat.transform(pnt);
                part2.addPoint(pnt.x,pnt.y,pnt.z);                
            }
            poly2.add(part2);
        }
        
        //
        // regularize polygon 
        //
        Path2D path = getPath(poly2);
        Area area = new Area(path);
        Vector<PointSet> contours = getContours(area, true);        

        // make 2D triangulation 
        PointSet triangles = triangulateContours(contours);
        
        // transform xy triangles back into polygon's plane         
        for(int k = 0; k < triangles.size(); k++) {
            triangles.getPoint(k, pnt);
            mat_inv.transform(pnt);
            pnt.add(center);
            triangles.setPoint(k, pnt);
        }
                
        return triangles;    
    }

    /**
       convert Vector<PointSet> into 2d shape (using only xy coord) 
     */
    Path2D getPath(Vector<PointSet> poly){
        
        printf("getPath() windRule: %s\n", getWindingRuleName(m_windingRule));
        Path2D.Double path = new Path2D.Double(m_windingRule);
        Vector3d pnt = new Vector3d();
        for(int k = 0; k < poly.size(); k++){
            PointSet part = poly.get(k);
            
            part.getPoint(0, pnt);
            path.moveTo(pnt.x, pnt.y);
            for(int i = 1; i < part.size(); i++){
                part.getPoint(i, pnt);
                path.lineTo(pnt.x, pnt.y);
            }            
            path.closePath();
        }

        return path;
        
    }

    /**
       splits shape into individual contours 
     */
    Vector<PointSet> getContours(Shape shape){

        return getContours(shape, false);

    }

    /**
       splits shape into individual contours with optiponal reversing of contours direction 
     */
    Vector<PointSet> getContours(Shape shape, boolean doReverse){
        
        PathIterator pi = shape.getPathIterator(null);
        Vector<PointSet> contours = new Vector<PointSet>();
        PointSet contour = null;

        double coord[] = new double[6];
        double lastX = Double.MAX_VALUE;
        double lastY = Double.MAX_VALUE;
        double EPS = 1.e-10;

        while(!pi.isDone()){
            int type = pi.currentSegment(coord);
            switch(type){
            default: 
                break;
            case PathIterator.SEG_CLOSE:  
                if(contours != null){
                    contours.add(contour); 
                }
                contour = null; 
                break;

            case PathIterator.SEG_LINETO: 
                if(contour == null) contour = new PointSetArray();
                if(abs(coord[0] - lastX) + abs(coord[1] - lastY) > EPS){
                    contour.addPoint(coord[0],coord[1], 0.);                          
                    lastX = coord[0];
                    lastY = coord[1];
                }
                break; 

            case PathIterator.SEG_MOVETO: 
                if(contour != null){ // add last contour 
                    contours.add(contour); 
                }
                contour = new PointSetArray();
                contour.addPoint(coord[0],coord[1], 0.);
                break; 
            }
            pi.next();
        }

        if(contour != null){ // add last contour 
            contours.add(contour); 
        }
        if(doReverse){ 
            for(int i = 0; i < contours.size(); i++){
                // area contoures are opposite oriented 
                PointSet ps = contours.get(i);
                reverse(ps);
            }
        }
        return contours;
    }
 


    /**
       reverse order of points 
     */
    public static void reverse(PointSet ps){

        int n = ps.size();
        Vector3d p0 = new Vector3d();
        Vector3d p1 = new Vector3d();

        for(int i = 0; i < n/2; i++){
            ps.getPoint(i, p0);
            ps.getPoint(n-i-1, p1);
            ps.setPoint(i, p1);
            ps.setPoint(n-i-1, p0);
        }
    }
 

    /**
       triangulate set of simple contour 
     */
    PointSet triangulateContours(Vector<PointSet> contours){

        PointSetArray tcontours = new PointSetArray();
        Vector3d pnt = new Vector3d();

        for(int i = 0; i < contours.size(); i++){
            PointSet contour = contours.get(i);
            PointSet triSet = triangulateContour(contour);
            for(int j = 0; j < triSet.size(); j++){
                triSet.getPoint(j, pnt);
                tcontours.addPoint(pnt.x, pnt.y, pnt.z);
            }     
        }

        return tcontours;
    }


    /**
       triangulae simple contour 
     */
    PointSet triangulateContour(PointSet contour){
        
        int vertCount = contour.size();
        float coord[] = new float[3*vertCount];    
        int coordIndex[] = new int[vertCount];
        if(vertCount < 3) return new PointSetArray();
        // output to hold triangle indices 
        int coordOutput[] = new int[(vertCount-2)*3];
        float normal[] = new float[]{0,0,1};
        Vector3d pnt = new Vector3d();

        for(int i = 0; i < vertCount; i++){

            contour.getPoint(i, pnt);
            coord[3*i] = (float)pnt.x;
            coord[3*i+1] = (float)pnt.y;
            coord[3*i+2] = 0;
            coordIndex[i] = i;

        }

        int triCount = m_triangulator.triangulateConcavePolygon(coord,0, vertCount, coordIndex,coordOutput, normal);
        printf("triCount: %d\n", triCount);
        
        
        PointSet triangles = new PointSetArray();

        triCount = abs(triCount);
        for(int i = 0; i < triCount; i++){

            contour.getPoint(coordOutput[3*i], pnt);
            triangles.addPoint(pnt.x, pnt.y, pnt.z);

            contour.getPoint(coordOutput[3*i+1], pnt);
            triangles.addPoint(pnt.x, pnt.y, pnt.z);

            contour.getPoint(coordOutput[3*i+2], pnt);
            triangles.addPoint(pnt.x, pnt.y, pnt.z);
        }
        return triangles;
    }

    public static final String getWindingRuleName(int windingRule){
        switch(windingRule){
        default: return "unknown";
        case WIND_EVEN_ODD: return "WIND_EVEN_ODD";
        case WIND_NON_ZERO: return "WIND_NON_ZERO";
        }

    }      

    public Vector3d getNormal(){
        return getNormal(m_polygon);
    }

    public Vector3d getCenter(){
        return getCenter(m_polygon);
    }

    /**
       calculates normal of 3d polygon 
       normal is calculated as average of notmals to each sequential triple of ponts 
       returned normal is normalized 
       or null 
     */
    static Vector3d getNormal(Vector<PointSet> poly){

        Vector3d p0 = new Vector3d();
        Vector3d p1 = new Vector3d();
        Vector3d p2 = new Vector3d();

        double nx = 0, ny = 0, nz = 0;
        
        int count = 0;
        Vector3d normal = new Vector3d();
        Vector3d nn = new Vector3d(); // normal of triple 

        for(int j = 0; j < poly.size(); j++){
            
            PointSet ps = poly.get(j);
            
            int size = ps.size();
            count += size;
            
            for(int i = 0; i < ps.size(); i++){
                
                ps.getPoint(i, p0);
                ps.getPoint((i+1) % size, p1);
                ps.getPoint((i+2) % size, p2);
                //printf("p0: (%7.5f %7.5f %7.5f)\n", p0.x, p0.y, p0.z);
                //printf("p1: (%7.5f %7.5f %7.5f)\n", p1.x, p1.y, p1.z);
                //printf("p2: (%7.5f %7.5f %7.5f)\n", p2.x, p2.y, p2.z);
                
                p2.sub(p1);
                p0.sub(p1);
                nn.cross(p2, p0);
                //double x = p2.y*p0.z - p2.z*p0.y;
                //double y = p2.z*p0.x - p2.x*p0.z;
                //double z = p2.x*p0.y - p2.y*p0.x;

                normal.add(nn);
                //printf("nxyz: (%7.5f %7.5f %7.5f)\n", x, y, z);
                
            }
        }
        
        //Vector3d normal = new Vector3d(nx, ny, nz);
        
        double length = normal.length();
        if(length == 0.) 
            return new Vector3d(0,0,1);

        normal.scale(1/length);
        
        return normal;

    }

    
    /**
       return center of mass of polygon 
     */
    static Vector3d getCenter(Vector<PointSet> contours){

        double x = 0, y = 0, z = 0; 
        int count = 0;
        Vector3d p = new Vector3d();
        for(int j = 0; j < contours.size(); j++){

            PointSet ps = contours.get(j);
            int size  = ps.size();
            count += size;
            
            for(int i = 0; i < size; i++){
                
                ps.getPoint(i, p);
                x += p.x;
                y += p.y;
                z += p.z;
                
            }        
        }
        if(count != 0){
            x /= count;
            y /= count;
            z /= count;
        }
        
        return new Vector3d(x,y,z);
    }

    /**

       @override 
     */
    public void setTransform(VecTransform trans) {

        m_transform = trans;
        if(m_transform instanceof Initializable)
            ((Initializable)m_transform).initialize();
    }

 
}
