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

import javax.vecmath.Vector3d;

import java.util.Arrays;

import static java.lang.Math.round;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;


/**
   class to render rasterised triangles into 

   represent [Nx x Ny] grid of rays parallel to z-axis 
   z - coordinates are in the range [0, Nz] (it is to be used for conversio to voxels)
   
   each ray is variable length array of double - points, where rendered triangle intersect the ray via center of xy square

   x,y,z triangles cordinates are double 

   grid occupies the space with  0.0 x < 
   pixel with integer coordinates ix,iy is represented by square (ix < x < ix+1, iy < y < iy+1)
   z-ray pass via center of square.  

   @author Vladimir Bulatov
 */
public class ZBuffer {
    
    float zdata[][];
    int zcount[]; 
    int Nx, Ny, Nz, Nxy;

    Triangle m_tri = new Triangle(); // triangle renderer     

    public ZBuffer(int nx, int ny, int nz){
        Nx = nx;
        Ny = ny;
        Nz = nz;
        Nxy = Nx*Ny;

        zdata = new float[Nxy][];
        zcount = new int[Nxy];

    }

    public void clear() {
        System.out.println("Clearing zdata");
        int len = zdata.length;
        for(int i=0; i < len; i++) {
            zdata[i] = null;
        }
        Arrays.fill(zcount, 0);
    }

    public void sort(){
        
        for(int i = 0; i < Nxy; i++){
            int c = zcount[i];
            if(c > 1){
                java.util.Arrays.sort(zdata[i], 0, c);
            }            
        }
    }

    public void printStats(){
        int hmax = 40;
        int hist[] = new int[hmax+1];
        for(int i = 0; i < Nxy; i++){
            int c = zcount[i];
            if( c < hmax)
                hist[c]++;
            else 
                hist[hmax]++;
        }
        
        printf("Zbuffer histogram max crossing: %d\n", hmax);
        for(int k = 0; k < hmax; k++){
            if((k & 1) != 0){
                // this should be normaly 0 
                if(hist[k] > 0)
                    printf("crossings: %d, count: %d ****BAD CROSSING COUNT****\n", k, hist[k]);                
            } else {
                if(hist[k] > 0)
                    printf("crossings: %d, count: %d\n", k, hist[k]);
            }                
        }
        if(hist[hmax] > 0)
            printf("crossing: %d and more, count: %d\n", hmax, hist[hmax]);

    }

    /**
       
       
     */
    public void setPixel(int x, int y, double z){

        if(x < 0 || x >= Nx || y < 0 || y >= Ny)  return;            

        int c = x + Nx*y;
        
        int cz = zcount[c];
        float zray[] = zdata[c];

        if(zray == null){
            // new array
            if (cz < 2) {
                zray = new float[2];
            } else {
                zray = new float[cz+1];
            }
            zdata[c] = zray;
        } else if(cz >= zray.length){

            zray = reallocArray(zray, zray.length*2);
            zdata[c] = zray;
        }

        zray[cz] = (float)z;

        zcount[c]++;

            
    }


    public int getWidth(){
        return Nx;
    }

    public int getHeight(){
        return Ny;
    }

    public int getDepth(){
        return Nz;
    }

    public float[] getRay(int x, int y){
        return zdata[x + Nx*y];
    }

    public void setRay(int x, int y, float ray[]){
        zdata[x + Nx*y] = ray;
    }

    public int getCount(int x, int y){
        return zcount[x + Nx*y];
    }

    
    public void fillTriangle(Vector3d v1, Vector3d v2, Vector3d v3){

        fillTriangle(v1.x,v1.y,v1.z,v2.x,v2.y,v2.z,v3.x,v3.y,v3.z);

    }
    
    public void fillTriangle(double x1, double y1, double z1, 
                            double x2, double y2, double z2, 
                            double x3, double y3, double z3
                            ){

        //setPixel((int)floor(x1), (int)floor(y1), z1);
        //setPixel((int)floor(x2), (int)floor(y2), z2);
        //setPixel((int)floor(x3), (int)floor(y3), z3);
        
        m_tri.fill(this, x1, y1, z1, x2, y2, z2, x3, y3, z3); 

    }

    public static float[] reallocArray(float array[], int newsize){

        //printf("reallocArray(%d)\n", newsize);
        float newarray[] = new float[newsize];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }


    // representation of one 3D edge
    static class Edge {

        double x1, y1, z1, x2, y2, z2;
        
        void init(double _x1, double _y1, double _z1, 
                  double _x2, double _y2, double _z2){ 

            if(_y1 < _y2) {
                x1 = _x1;
                y1 = _y1;
                z1 = _z1;
                x2 = _x2;
                y2 = _y2;
                z2 = _z2;
            } else {
                x1 = _x2;
                y1 = _y2;
                z1 = _z2;
                x2 = _x1;
                y2 = _y1;
                z2 = _z1;
            }
            
        }
        
        public String toString(){
            return fmt("[(%7.3f,%7.3f,%7.3f),(%7.3f,%7.3f,%7.3f)]", x1, y1, z1, x2, y2, z2);
        }

    }

    // representation of one 3D triangle
    static class Triangle {
        
        Edge edges[] = new Edge[]{new Edge(),new Edge(),new Edge()};
        
        ZBuffer m_zb;

        Triangle(){
        }

        public void fill(ZBuffer _zb, 
                         double x1, double y1, double z1, 
                         double x2, double y2, double z2, 
                         double x3, double y3, double z3){
            m_zb = _zb;

            //printf("Triangle.fill(%7.3f,%7.3f,%7.3f, %7.3f,%7.3f,%7.3f, %7.3f,%7.3f,%7.3f )\n", x1, y1, z1, x2, y2, z2, x3, y3, z3);
            
            // create edges for the triangle
            edges[0].init(x1,y1,z1, x2,y2,z2);
            edges[1].init(x2,y2,z2, x3,y3,z3);
            edges[2].init(x3,y3,z3, x1,y1,z1);
            
            double maxLength = 0;
            int longEdge = 0;
            
            // find edge with the greatest length in the y axis
            for(int i = 0; i < 3; i++) {
                double length = edges[i].y2 - edges[i].y1;
                if(length > maxLength) {
                    maxLength = length;
                    longEdge = i;
                }
            } 
            int shortEdge1 = (longEdge + 1) % 3;
            int shortEdge2 = (longEdge + 2) % 3;
            // draw spans between edges; the long edge can be drawn
            // with the shorter edges to draw the full triangle
            fillSpans(edges[longEdge], edges[shortEdge1]);
            fillSpans(edges[longEdge], edges[shortEdge2]);        
            
        }

        /**
           we assume, that 
           e1 is y-longest edge of triangle 
           e2 - one of 2 shorter edges 
           
         */
        void fillSpans(Edge e1, Edge e2){
            //printf("fillSpans(%s, %s)\n", e1, e2);

            // calculate difference between the y coordinates
            // of the first edge and return if 0
            double e1ydiff = (e1.y2 - e1.y1);
            if(e1ydiff == 0.0)
                return;
            
            // calculate difference between the y coordinates
            // of the second edge and return if 0
            double e2ydiff = (e2.y2 - e2.y1);
            if(e2ydiff == 0.0)
                return;

            // calculate differences between the x coordinates
            // and colors of the points of the edges
            double e1xdiff = (e1.x2 - e1.x1);
            double e2xdiff = (e2.x2 - e2.x1);
            double e1zdiff = (e1.z2 - e1.z1);
            double e2zdiff = (e2.z2 - e2.z1);
            
            // we use e2, because it is shorter than e1 and one point is shared

            int ystart = (int)(e2.y1+0.5);
            int yend = (int)(e2.y2+0.5);

            //printf("ystart: %d yend: %d\n", ystart, yend);
            //printf("e1xdiff: %7.3f e2xdiff: %7.3f\n", e1xdiff, e2xdiff);
            
            double e1x1 = e1.x1;
            double e2x1 = e2.x1;
            double e1z1 = e1.z1;
            double e2z1 = e2.z1;

            // calculate factors to use for interpolation
            // with the edges and the step values to increase
            // them by after drawing each span
            double factor1 = (ystart + 0.5 - e1.y1) / e1ydiff;
            double factorStep1 = 1.0 / e1ydiff;
            double factor2 = (ystart + 0.5 - e2.y1) / e2ydiff;
            double factorStep2 = 1.0 / e2ydiff;
/*
            // TODO: Decide whether to have this clamping.
            if (ystart < 0) {
                System.out.println("Outside grid-, clamping");
                ystart = 0;
            }
            if (yend > m_zb.Ny) {
                System.out.println("Outside grid+, clamping");
                yend = m_zb.Ny;
            }
 */
            // loop through the lines between ystart and yend
            for(int iy = ystart; iy < yend; iy++) {
                
                double y = iy + 0.5;
                //printf("y: %7.3f\n ", y);
                double xs = e1x1 + (e1xdiff * factor1);//e1xdiff * factorStep1*(y-e1.y1);
                double xe = e2x1 + (e2xdiff * factor2);//e2xdiff * factorStep2*(y-e2.y1);
                double zs = e1z1 + (e1zdiff * factor1);//e1zdiff * factorStep1*(y-e1.y1);
                double ze = e2z1 + (e2zdiff * factor2);//e2zdiff * factorStep2*(y-e2.y1);
                factor1 += factorStep1;
                factor2 += factorStep2;

                //printf("xs: %7.3f, xe: %7.3f\n ", xs, xe);

                drawSpan(iy, xs, zs, xe, ze);

                /*
                // create and draw span
                double xs = e1.x1 + (e1xdiff * factor1);
                double xe = e2.x1 + (e2xdiff * factor2);

                double zs = e1.z1 + (e1zdiff * factor1);
                double ze = e2.z1 + (e2zdiff * factor2);
                                
                drawSpan(y, xs, zs, xe, ze);
                
                // increase factors
                factor1 += factorStep1;
                factor2 += factorStep2;
                */
            }
        
        }

        /**
           fill interval of voxels (x1, x2) at given y coordinate
         */
        void drawSpan(int iy, double x1, double z1, double x2, double z2){
            
            //printf("drawSpan(%d, %7.3f,%7.3f,%7.3f,%7.3f)\n", iy, x1, z1, x2, z2);            
            double xdiff = x2 - x1;
            if(xdiff == 0.0)
                return;

            if( xdiff < 0){
                double t = x1; 
                
                x1 = x2; 
                x2 = t;

                t = z1; 
                z1 = z2; 
                z2 = t; 
                xdiff = -xdiff;
            }

            
            
            // draw each pixel in the span
            int xstart = (int)(x1+0.5);
            int xend = (int)(x2+0.5);

            double zdiff = z2 - z1;
            
            double factor = (xstart + 0.5 - x1)/xdiff; // offset of center of first voxel from start of interval 
            double factorStep = 1.0 / xdiff;

            //printf("zdiff: %7.3f factor: %7.3f, factorStep: %7.3f\n", zdiff, factor, factorStep);
            
            //loop through each x position in the span 
            for(int ix = xstart; ix < xend; ix++) {
                double z = z1 + zdiff*factor;                
                //printf("ix: %d, z: %7.3f\n", ix, z);
                m_zb.setPixel(ix, iy, z);
                factor += factorStep;
            }                
        }
    }    
}
