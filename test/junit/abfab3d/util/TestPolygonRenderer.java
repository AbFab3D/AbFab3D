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

package abfab3d.util;

// External Imports
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;

import java.util.Vector;


import org.j3d.geom.TriangulationUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;

// Internal Imports
import abfab3d.core.MathUtil;
import abfab3d.core.Bounds;
import abfab3d.core.TriangleCollector;

import abfab3d.transforms.Rotation;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.TODEGREE;
import static abfab3d.core.Output.fmt;
import static java.lang.Math.*;
import static abfab3d.core.MathUtil.getAxisAngle;


/**
 * Tests the functionality of PolygonRenderer
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestPolygonRenderer extends TestCase {

    /**+
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestPolygonRenderer.class);
    }


    public void testDumb(){
        //this test here is to make Test happy. 
    }

        
    public void devTestSeidelTriangulator(){
        
        // does't work 
        int[] contourCounts = new int[]{4};
        float vert[] = new float[]{0,0, 1,0, 0,1, -1, 0, 0,-1, 1,0};
        
        int triangles[] = new int[10];
        new TriangulationUtils().triangulatePolygon2D(1, contourCounts,vert,triangles);

            
    }

    public void devTestPolygon()throws Exception {

        //Polygon poly = new Polygon(Polygon.WIND_NON_ZERO);
        Polygon poly = new Polygon(Polygon.WIND_EVEN_ODD);
        double a = 0.5;
        poly.moveTo(a,0,0);
        poly.lineTo(0,a,0);
        poly.lineTo(0,0,a);

        double z = 0.1;
        
        /*
        poly.moveTo(1,0,z);
        poly.lineTo(0,1,z);
        poly.lineTo(-1,0,z);
        poly.lineTo(0,-1,z);
        poly.close();
        double a = 0.8;
        poly.moveTo(a,a,z);
        poly.lineTo(-a,a,z);
        poly.lineTo(-a,-a,z);
        poly.lineTo(a,-a,z);
        poly.close();
        */

        //poly.append(getStar(5, 2, PI/10, Path2D.WIND_EVEN_ODD, 1.e-10));


        TrianglesRenderer tr = new TrianglesRenderer(false, true);
        poly.getTriangles(tr);
        tr.write("/tmp/polygon.png");

    }

    public void devTestPolygon2()throws Exception {

        //Polygon poly = getRectY();
        //Polygon poly = getRectZ();
        Polygon poly = getTriangle();
        TrianglesRenderer tr = new TrianglesRenderer(true, true);
        poly.setTransform(new Rotation(0,0,1,PI/2));
        poly.getTriangles(tr);
        
        tr.write("/tmp/polygon_rectZ.png");

    }


    public void devTestTriangulator(){
        
        int numContour = 1;
        float coord[] = new float[]{1,1,0, -1,1,0, -1,-1,0, 1,-1,0};
        int vertCount = 4;
        int coordIndex[] = new int[]{0,1,2,3};
        int coordOutput[] = new int[(vertCount-2)*3];
        float normal[] = new float[]{0,0,1};
        
        int triCount = new TriangulationUtils().triangulateConcavePolygon(coord,0, 4, coordIndex,coordOutput, normal);
        printf("triCount: %d\n", triCount);

        for(int i = 0; i < triCount; i++){
            printf("%2d %2d %2d\n", coordOutput[3*i], coordOutput[3*i+1], coordOutput[3*i+2]);
        }
        
    }

    public void devTestArea(){
        
        //Path2D path = getCross(2, PI/10);
        //Path2D path = getRect();
        //Path2D path = getStar(4, 2, PI/10);
        //processShape(getCross(2, PI/10), "cross");
        
        processShape(getStar(4, 2, PI/10, Path2D.WIND_EVEN_ODD, 1.e-10), "star_EO");
        processShape(getStar(4, 2, PI/10, Path2D.WIND_NON_ZERO, 1.e-10), "star_NZ");

        //processShape(getHole(5, Path2D.WIND_EVEN_ODD), "hole_EO");
        //processShape(getHole(5, Path2D.WIND_NON_ZERO), "hole_NZ");

    }

    public void devTestNormal(){

        //Polygon poly = getBowTie();
        Polygon rectz = getRectZ();
        Polygon recty = getRectY();
        Polygon rectx = getRectX();

        Vector3d normal = recty.getNormal();
        Vector3d center = recty.getCenter();
        printf("normal: %7.3f %7.3f %7.3f\n",normal.x, normal.y, normal.z);
        printf("center: %7.3f %7.3f %7.3f\n",center.x, center.y, center.z);
    }

    
    /**
       splits shape into individual contours 
     */
    Vector<PointSet> getContours(Shape shape){
        
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
        
        return contours;
    }

    public void processShape_v0(Shape path, String name){

        printShape(path, name + "_path");
        renderShape(path, fmt("/tmp/polygon_%s_path.png", name));
        Area area = new Area(path);
        printShape(area, name+"_area");        
        renderShape(area, fmt("/tmp/polygon_%s_area.png", name));

    }

    public void processShape(Shape path, String name){

        Vector<PointSet> pathC = getContours(path);
        //printShape(path, name + "_path");
        printContours(pathC, name + "_path");        
        renderContours(pathC, fmt("/tmp/polygon_%s_path.png", name));

        Area area = new Area(path);
        //printShape(area, name + "_path");
        Vector<PointSet> areaC = getContours(area);
        for(int i = 0; i < areaC.size(); i++){
            // area are opposite oriented 
            PointSet ps = areaC.get(i);
            reverse(ps);
        }
        printContours(areaC, name+"_area");        
        renderContours(areaC, fmt("/tmp/polygon_%s_area.png", name));
        Vector<PointSet> areaT = triangulateContours(areaC);
        renderContours(areaT, fmt("/tmp/polygon_%s_triarea.png", name));

    }


    /**
       triangulae set of simple contour 
     */
    static Vector<PointSet> triangulateContours(Vector<PointSet> contours){

        Vector<PointSet> tcontours = new Vector<PointSet>();
        for(int i = 0; i < contours.size(); i++){
            PointSet contour = contours.get(i);
            Vector<PointSet> triSet = triangulate(contour);
            tcontours.addAll(triSet);
        }     
        return tcontours;
    }


    /**
       triangulae simple contour 
     */
    static Vector<PointSet> triangulate(PointSet contour){
        
        int vertCount = contour.size();
        float coord[] = new float[3*vertCount];    
        int coordIndex[] = new int[vertCount];

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

        int triCount = new TriangulationUtils().triangulateConcavePolygon(coord,0, vertCount, coordIndex,coordOutput, normal);
        printf("triCount: %d\n", triCount);
        
        
        Vector<PointSet> tcontours = new Vector<PointSet>();
        triCount = abs(triCount);
        for(int i = 0; i < triCount; i++){

            PointSet tri = new PointSetArray(3);

            contour.getPoint(coordOutput[3*i], pnt);
            printf("(%8.5f %8.5f) ", pnt.x, pnt.y);
            tri.addPoint(pnt.x, pnt.y, pnt.z);
            contour.getPoint(coordOutput[3*i+1], pnt);
            printf("(%8.5f %8.5f) ", pnt.x, pnt.y);
            tri.addPoint(pnt.x, pnt.y, pnt.z);
            contour.getPoint(coordOutput[3*i+2], pnt);
            printf("(%8.5f %8.5f)\n", pnt.x, pnt.y);
            tri.addPoint(pnt.x, pnt.y, pnt.z);

            tcontours.add(tri);
        }
        return tcontours;
    }

    static void printShape(Shape shape, String title){
        printf("shape: %s\n", title);
        PathIterator pi = shape.getPathIterator(null);

        double coord[] = new double[6];
        while(!pi.isDone()){
            int type = pi.currentSegment(coord);
            switch(type){
            default: printf("UNKNOWN\n"); break;
            case PathIterator.SEG_CLOSE:  printf("CLOSE\n",coord[0],coord[1]); break;
            case PathIterator.SEG_LINETO: printf("LINETO (%19.16f %19.16f)\n",coord[0],coord[1]); break; 
            case PathIterator.SEG_MOVETO:  printf("MOVETO (%19.16f %19.16f)\n",coord[0],coord[1]); break; 
            }
            pi.next();
        }
               

    }
    
    static void renderContours(Vector<PointSet> contours, String path){
        printf("renderContours(%s)\n", path);
        double a = 1.1;
        double pointSize = 0.005;
        GraphicsCanvas2D canvas = new GraphicsCanvas2D(1000, 1000, new Bounds(-a,a, -a,a, -a,a), Color.WHITE);

        Vector3d pnt = new Vector3d();
        for(int c = 0; c < contours.size(); c++){
            PointSet ps =  contours.get(c);
            ps.getPoint(ps.size()-1, pnt);
            double lastX = pnt.x;
            double lastY = pnt.y;
            for(int i = 0; i < ps.size(); i++){                
                ps.getPoint(i, pnt);
                double curX = pnt.x;
                double curY = pnt.y;
                canvas.drawLine(lastX, lastY, curX, curY,  getColor(c));
                canvas.fillCircle(curX, curY, pointSize,getColor(c)); 
                lastX = curX;
                lastY = curY;
            }
        }
        try {
            canvas.write(path);
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
       calculates area of 2D polygon 
     */
    static double getArea(PointSet ps){

        Vector3d p0 = new Vector3d();
        Vector3d p1 = new Vector3d();
        ps.getPoint(ps.size()-1, p0);
        double area = 0;
        for(int i = 0; i < ps.size(); i++){
            ps.getPoint(i, p1);
            area += p0.x*p1.y - p0.y*p1.x;
            p0.set(p1);
        }
        return 0.5*area;
    }

    static void printContours(Vector<PointSet> contours, String name){

        printf("contours: %s\n", name);
        Vector3d pnt = new Vector3d();
        Vector3d pnt1 = new Vector3d();
        for(int c = 0; c < contours.size(); c++){
            PointSet ps =  contours.get(c);
            printf("contour: %d area: %7.5f\n", c, getArea(ps));
            for(int i = 0; i < ps.size(); i++){                
                ps.getPoint(i, pnt);
                ps.getPoint((i+1)%ps.size(), pnt1);
                pnt1.sub(pnt);
                printf("%19.16f %19.16f %7.3f\n", pnt.x, pnt.y, pnt1.length());
            }
        }

    }

    static void renderShape(Shape area, String path){

        double a = 1.1;
        GraphicsCanvas2D canvas = new GraphicsCanvas2D(1000, 1000, new Bounds(-a,a, -a,a, -a,a), Color.WHITE);
        // xy axes 
        //canvas.drawLine(-a, 0, a, 0, Color.BLACK);
        //canvas.drawLine(0, -a, 0, a, Color.BLACK);

        PathIterator pi = area.getPathIterator(null);
        
        double coord[] = new double[6];
        double lastX=0, lastY=0, firstX=0, firstY=0, curX=0, curY=0;
        int colorIndex = 0;
        double pointSize = 0.005;
        while(!pi.isDone()){

            int type = pi.currentSegment(coord);
            
            switch(type){
            default: break;
            case PathIterator.SEG_CLOSE:  
                
                canvas.drawLine(curX, curY, firstX, firstY, colors[colorIndex]); 
                colorIndex  = (colorIndex+1)%colors.length;
                break;

            case PathIterator.SEG_LINETO: 

                curX = coord[0];
                curY = coord[1];
                canvas.drawLine(lastX, lastY, curX, curY,  colors[colorIndex]);
                canvas.fillCircle(curX, curY, pointSize,colors[colorIndex]);
                lastX = curX;
                lastY = curY;
                 break; 

            case PathIterator.SEG_MOVETO:  

                firstX = coord[0];
                firstY = coord[1];
                lastX = firstX;
                lastY = firstY;
                curX = firstX;
                curY = firstY;
                
                canvas.fillCircle(curX, curY, pointSize,colors[colorIndex]);

                break; 

            }
            
            pi.next();
        }
        
        try {
            canvas.write(path);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    static Path2D getBowTie2D(){

        double e = 0.1;
        
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        path.moveTo(1+e,1);
        path.lineTo(-1+e,1);
        path.lineTo(1+e,-1);
        path.lineTo(-1+e,-1);
        path.lineTo(1+e,1);
        
        return path;

    }

    static Polygon getBowTie(){

        double e = 0.1;
        
        Polygon poly = new Polygon(Polygon.WIND_EVEN_ODD);
        poly.moveTo(1+e,1,0);
        poly.lineTo(-1+e,1,0);
        poly.lineTo(1+e,-1,0);
        poly.lineTo(-1+e,-1,0);
        
        return poly;

    }

    static Path2D getRect2D(){
        
        
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        double a = 0.5; 
        double b = 1;
        path.moveTo( a,b);
        path.lineTo( a,-b);
        path.lineTo(-a,-b);
        path.lineTo(-a,b);
        path.lineTo( a,b);
        path.closePath();

        return path;

    }

    static Polygon getRectZ(){
        
        
        Polygon path = new Polygon(Polygon.WIND_EVEN_ODD);
        double a = 0.5; 
        double b = 1;
        double x = 0.1;
        double y = 0.2;
        double z = 0.3;
        path.moveTo(  a+x,  b+y, z);
        path.lineTo( -a+x,  b+y, z);
        path.lineTo( -a+x, -b+y, z);
        path.lineTo(  a+x, -b+y, z);
        path.close();
        return path;

    }

    static Polygon getRectY(){
        
        
        Polygon path = new Polygon(Polygon.WIND_EVEN_ODD);
        double a = 0.5; 
        double b = 1;
        double x = 0.1;
        double y = 0.2;
        double z = 0.3;
        path.moveTo( b+x, y, a+z);
        path.lineTo( b+x, y,-a+z);
        path.lineTo(-b+x, y,-a+z);
        path.lineTo(-b+x, y, a+z);
        path.close();
        return path;

    }

    static Polygon getRectX(){
        
        
        Polygon path = new Polygon(Polygon.WIND_EVEN_ODD);
        double a = 0.5; 
        double b = 1;
        double x = 0.5;
        path.moveTo(x, a, b);
        path.lineTo(x,-a, b );
        path.lineTo(x,-a, -b);
        path.lineTo(x, a, -b);
        path.close();
        return path;

    }

    static Polygon getTriangle(){
        
        
        Polygon path = new Polygon(Polygon.WIND_EVEN_ODD);
        double a = 0.5; 
        path.moveTo(a,0,0);
        path.lineTo(0,a,0);
        path.lineTo(0,0,a);
        return path;

    }

    /**
       union of rectrangles  making a ring with hole 
     */
    static Path2D getHole(int order, int type){
        Path2D path = new Path2D.Double(type);
        double a = 0.7;
        double w = 0.2;
        double b = a-w;
        double c = a;

        Vector3d vert[] = new Vector3d[]{new Vector3d(a, c, 0), new Vector3d(b, c, 0), new Vector3d(b, -c, 0), new Vector3d(a, -c, 0)};
        Matrix3d rot = new Matrix3d();

        rot.rotZ(2*PI/order);
        for(int k = 0; k < order; k++){
            path.moveTo(vert[vert.length-1].x,vert[vert.length-1].y); 
            for(int i = 0; i < vert.length; i++){
                path.lineTo(vert[i].x,vert[i].y);
            }
            for(int i = 0; i < vert.length; i++){
                rot.transform(vert[i]);
            }            
        }
        return path;
        
    }

    static Path2D getStar(int order, int factor, double pAngle, int windingType, double angle0){

        Path2D path = new Path2D.Double(windingType);
        
        double rotAngle = 2*PI/order;

        Vector3d p0 = new Vector3d(cos(pAngle), sin(pAngle), 0);
        Vector3d p1 = new Vector3d(cos(pAngle), -sin(pAngle), 0);
        int polyOrder = order;
        int polyCount = 1;
        if(order % factor == 0) {
            polyOrder = order/factor;
            polyCount = factor;
        }
        Vector3d vert[] = new Vector3d[2*polyOrder];

        Matrix3d rot = new Matrix3d();

        for(int i = 0; i < polyOrder; i++){

            Vector3d v0 = new Vector3d(p0);
            Vector3d v1 = new Vector3d(p1); 

            rot.rotZ(((factor*i)%order)*rotAngle);
            rot.transform(v1);
            rot.transform(v0);
            vert[2*i] = v1;
            vert[(2*i+1)] = v0;
            
        }
        
        Matrix3d rote = new Matrix3d();
        rote.rotZ(angle0);
        for(int i = 0; i < vert.length; i++){
            rote.transform(vert[i]);
        }
        
        //printf("polyOrder: %d\n", polyOrder);
        //printf("polyCount: %d\n", polyCount);
        for(int k = 0; k < polyCount; k++){
            path.moveTo(vert[vert.length-1].x,vert[vert.length-1].y); 
            //printf("moveTo(%5.3f %5.3f)\n",vert[vert.length-1].x,vert[vert.length-1].y);
            for(int i = 0; i < vert.length; i++){
                //printf("lineTo(%5.3f %5.3f)\n",vert[i].x,vert[i].y);
                path.lineTo(vert[i].x,vert[i].y);
            }
            //path.closePath(); 
            Matrix3d rr = new Matrix3d();
            rr.rotZ(rotAngle);
            for(int i = 0; i < vert.length; i++){
                rr.transform(vert[i]);
            }
            
        }

        return path;

    }

    static Path2D getSimpleCross(){
        
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        //Path2D path = new Path2D.Double(Path2D.WIND_NON_ZERO);
        double b = 1;
        double a = 0.5;
        path.moveTo( b,  a);
        path.lineTo(-b,  a);
        path.lineTo(-b, -a);
        path.lineTo( b, -a);
        path.lineTo( b,  a);

        path.moveTo( a, b);
        path.lineTo(-a, b);
        path.lineTo(-a,-b);
        path.lineTo( a,-b);
        path.lineTo( a, b);

        return path;

    }

    static Path2D getCross(int order, double pntAngle){
        
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);        
        //Path2D path = new Path2D.Double(Path2D.WIND_NON_ZERO);

        double angle = PI/order;
        double b = cos(pntAngle);
        double a = sin(pntAngle);
        Vector3d pnt[] = new Vector3d[]{new Vector3d(b,-a,0), new Vector3d(b,a,0),new Vector3d(-b,a,0), new Vector3d(-b,-a,0)};
        int n = pnt.length;

        Matrix3d rote = new Matrix3d();
        rote.rotZ(0.);  
        for(int i = 0; i < n; i++){
            rote.transform(pnt[i]);
        }
        //Vector3d pnt[] = new Vector3d[]{new Vector3d(b,a,0),new Vector3d(-b,-a,0),new Vector3d(-b,a,0),new Vector3d(b,-a,0)};

        path.moveTo(pnt[n-1].x, pnt[n-1].y);
        for(int i = 0; i < n; i++){
            path.lineTo(pnt[i].x, pnt[i].y); 
        }
        Matrix3d rot = new Matrix3d();
        rot.rotZ(angle);

        for(int j = 0; j < order-1; j++){
            for(int i = 0; i < n; i++){
                rot.transform(pnt[i]);
            }
            
            path.moveTo(pnt[n-1].x, pnt[n-1].y);
            for(int i = 0; i < n; i++){
                path.lineTo(pnt[i].x, pnt[i].y); 
            }
        }
        return path;

    }

    static String getSegmentName(int type) {
        switch(type){
        case PathIterator.SEG_CLOSE: return "CLOSE";
        case PathIterator.SEG_CUBICTO: return "CUBICTO";            
        case PathIterator.SEG_LINETO:  return "LINETO";
        case PathIterator.SEG_MOVETO:  return "MOVETO";
        case PathIterator.SEG_QUADTO:  return "QUADTO";
        default: return "UNKNOWN";
        }
    }

    static Color colors[] = new Color[]{Color.RED, Color.BLUE, Color.ORANGE, Color.GREEN, Color.PINK, Color.CYAN, Color.LIGHT_GRAY, 
                                     Color.GRAY, Color.BLACK,  Color.YELLOW,Color.MAGENTA};

    static Color getColor(int c){
        return colors[c % colors.length];
    }

    /**
       reverse order of points 
     */
    static void reverse(PointSet ps){

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

    static class TrianglesRenderer implements TriangleCollector {
        
        GraphicsCanvas2D m_canvas;
        
        boolean m_drawOutline = true;
        boolean m_fillTriangles = true;

        TrianglesRenderer(boolean drawOutline, boolean fillTriangles){

            m_drawOutline = drawOutline;
            m_fillTriangles = fillTriangles;


            double a = 1.1;
            m_canvas = new GraphicsCanvas2D(1000, 1000, new Bounds(-a,a, -a,a, -a,a), Color.WHITE);
        }

        public boolean addTri(Vector3d p0, Vector3d p1, Vector3d p2){
            if(m_fillTriangles){
                PointSet ps = new PointSetArray(3);
                ps.addPoint(p0.x,p0.y,p0.z);
                ps.addPoint(p1.x,p1.y,p1.z);
                ps.addPoint(p2.x,p2.y,p2.z);                
                m_canvas.fillPoly(ps, Color.YELLOW);
            }

            if(m_drawOutline){
                m_canvas.drawLine(p0.x,p0.y,p1.x,p1.y,Color.BLACK);
                m_canvas.drawLine(p1.x,p1.y,p2.x,p2.y,Color.BLACK);
                m_canvas.drawLine(p2.x,p2.y,p0.x,p0.y,Color.BLACK);
            }
            return true;

        }
        
        void write(String path){
            try {
                m_canvas.write(path);            
            } catch(Exception e){
                e.printStackTrace();
            }
        }

    }


    static void devTestVector(){
        Vector3d v = new Vector3d(0,0,0);
        v.normalize();
        printf("normalized V: (%7.5f %7.5f %7.5f)\n",v.x, v.y, v.z );
    }

    static void devTestAxisAngle(){
        Vector3d vx = new Vector3d(-1,0,0);
        Vector3d vy = new Vector3d(0,-1,0);
        Vector3d vz = new Vector3d(0,0,-1);

        Vector3d v = new Vector3d(0,0,1);
        AxisAngle4d ax = MathUtil.getAxisAngle(vx, v);
        AxisAngle4d ay = MathUtil.getAxisAngle(vy, v);
        AxisAngle4d az = MathUtil.getAxisAngle(vz, v);

        printf("ax (%7.5f %7.5f %7.5f; %7.5f)\n",ax.x, ax.y, ax.z, ax.angle*TODEGREE);
        printf("ay (%7.5f %7.5f %7.5f; %7.5f)\n",ay.x, ay.y, ay.z, ay.angle*TODEGREE);
        printf("az (%7.5f %7.5f %7.5f; %7.5f)\n",az.x, az.y, az.z, az.angle*TODEGREE);
    }

    public static void main(String[] arg) throws Exception {
        
        //new TestPolygonRenderer().devTestSeidelTriangulator();
        //new TestPolygonRenderer().devTestArea();        
        //new TestPolygonRenderer().devTestTriangulator();        
        //new TestPolygonRenderer().devTestPolygon();        
        new TestPolygonRenderer().devTestPolygon2();        
        //new TestPolygonRenderer().devTestVector();
        //new TestPolygonRenderer().devTestNormal();
        //new TestPolygonRenderer().devTestAxisAngle();
    }
}
