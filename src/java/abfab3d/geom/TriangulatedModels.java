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

// External Imports
import java.util.*;
import java.io.*;


import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;

import abfab3d.util.TriangleCollector;
import abfab3d.util.ImageGray16;



// Internal Imports



// static imports 
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

/**
 * Creates a bunch of Models as triangle meshes
 *
 * @author Vladimir Bulatov
 */
public class TriangulatedModels {

    static final double MM = 0.001;


    // class to generate stars with parameters
    // parameters are illustrated in file docs/images/mesh_star.svg
    public static class Star {
        double armCount;
        double armBaseWidth;
        double armEndWidth;
        double spaceWidth;
        double armLength;
        double thickness;
        
        public Star(int armCount, 
                    double armBaseWidth, 
                    double armEndWidth, 
                    double spaceWidth, 
                    double armLenght,
                    double thickness){
            this.armCount = armCount;
            this.armBaseWidth = armBaseWidth;
            this.armEndWidth = armEndWidth;
            this.spaceWidth = spaceWidth;
            this.armLength = armLenght;
            this.thickness = thickness;
            
            
        }
        public void getTriangles(TriangleCollector tc){
            double a = PI/armCount;

            double Cy = armBaseWidth/2;
            double Cx = (Cy * cos(a) + spaceWidth/2 )/sin(a);
            double Dy = armEndWidth/2;
            double Dx = Cx + armLength;
            Matrix3d rot = new Matrix3d();
            rot.set(new AxisAngle4d(0,0,1,2*a));
                
            Vector3d 
                v0 = new Vector3d(0, 0, 0),
                v1 = new Vector3d(Cx, -Cy, 0),
                v2 = new Vector3d(Dx, -Dy, 0),
                v3 = new Vector3d(Dx, Dy, 0),
                v4 = new Vector3d(Cx, Cy, 0),
                v5 = new Vector3d(Cx, -Cy, 0);
            rot.transform(v5);
                
            Vector3d v0t = new Vector3d(),
                v1t = new Vector3d(), 
                v2t = new Vector3d(), 
                v3t = new Vector3d(), 
                v4t = new Vector3d(),
                v5t = new Vector3d();
            
            Vector3d zoffset = new Vector3d(0, 0, -thickness);
            for(int i = 0; i < armCount; i++){    
            //for(int i = 0; i < 1; i++){    
                //Matrix3d rot = new Matrix3d();
                rot.set(new AxisAngle4d(0,0,1,2*a*i));
                rot.transform(v0, v0t);
                rot.transform(v1, v1t);
                rot.transform(v2, v2t);
                rot.transform(v3, v3t);
                rot.transform(v4, v4t);
                rot.transform(v5, v5t);

                //tc.addTri(v0t, v1t, v4t);
                //tc.addTri(v1t, v2t, v3t);
                //tc.addTri(v1t, v3t, v4t);
                //tc.addTri(v0t, v4t, v5t);

                addBases(tc, new Vector3d[]{v0t, v1t, v4t}, zoffset);
                addBases(tc, new Vector3d[]{v1t, v2t, v3t}, zoffset);
                addBases(tc, new Vector3d[]{v1t, v3t, v4t}, zoffset);
                addBases(tc, new Vector3d[]{v0t, v4t, v5t}, zoffset);
                
                addBand(tc, new Vector3d[]{v1t, v2t, v3t, v4t, v5t}, zoffset);
            }
            
            
            
        }
    } // class Star  

    // adds band of triangles along path with given offset 
    public static void addBand(TriangleCollector tc, Vector3d[] path, Vector3d offset){

        for(int i = 0; i < path.length-1; i++){
            Vector3d v0 = path[i];
            Vector3d v1 = path[i+1];
            Vector3d v0t = new Vector3d(v0);
            Vector3d v1t = new Vector3d(v1);
            v0t.add(offset);
            v1t.add(offset);
            
            tc.addTri(v0, v0t, v1t);
            tc.addTri(v0, v1t, v1);
        }
    }
    // adds two base triangles, original and translated by offset 
    
    public static void addBases(TriangleCollector tc, Vector3d[] tri, Vector3d offset){
        
        
            Vector3d v0t = new Vector3d(tri[0]);
            Vector3d v1t = new Vector3d(tri[1]);
            Vector3d v2t = new Vector3d(tri[2]);
            
            v0t.add(offset);
            v1t.add(offset);
            v2t.add(offset);

            tc.addTri(tri[0],tri[1],tri[2]);
            tc.addTri(v1t, v0t, v2t);

        
    }

    // creates hight field of triangles from 2D grid 
    public static class HeightField {

        ImageGray16  image;
        int nx,  ny;
        int maxDataValue;
        double sx, sy, sz;

        public HeightField(ImageGray16 image, int maxDataValue, double sx, double sy, double sz){
            
            this.image = image;
            this.nx = image.getWidth();
            this.ny = image.getHeight();
            this.maxDataValue = maxDataValue;
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;          

        }
        
        public void getTriangles(TriangleCollector tc){
            
            double dx = sx / nx;
            double dy = sy / ny;
            
            
            for(int iy = 0; iy < ny-1; iy++){
                
                double y0 = iy * sy / ny;
                double y1 = y0 + dy;
                
                for(int ix = 0; ix < nx-1; ix++){
                    
                    double x0 = ix * sx / nx;
                    double x1 = x0 + dx;
                    double d00 = getHeight(ix,iy);
                    double d10 = getHeight(ix+1,iy);
                    double d01 = getHeight(ix,iy+1);
                    double d11 = getHeight(ix+1,iy+1);
                    
                    double dc = (d00 + d10 + d01 + d11)/4.;
                    Vector3d 
                        v00 = new Vector3d(x0, y0, d00),                    
                        v10 = new Vector3d(x1, y0, d10),
                        v01 = new Vector3d(x0, y1, d01),
                        v11 = new Vector3d(x1, y1, d11),
                        vc = new Vector3d((x0+x1)/2, (y0 + y1)/2, dc);
                    
                    tc.addTri(v00, v10, vc);
                    tc.addTri(v10, v11, vc);
                    tc.addTri(v11, v01, vc);
                    tc.addTri(v01, v00, vc);
                }            
            }
        }

        double getHeight(int ix, int iy){

            double d = image.getDataI(ix, iy);
            return (d * sz / maxDataValue);
        }

    } // class HeightField 
    
    
} // class TriangulatedModels


