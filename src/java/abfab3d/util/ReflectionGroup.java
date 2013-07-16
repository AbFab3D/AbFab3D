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

import javax.vecmath.Vector3d;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;


/**
   class to support calculations with reflection groups in 3D
   reflections include plane reflections and sphere inversions 


   @author Vladimir Bulatov
 */
public class ReflectionGroup {
    
    static final boolean DEBUG = true;
    
    public static final int RESULT_OK = ResultCodes.RESULT_OK;
    public static final int RESULT_ERROR = ResultCodes.RESULT_ERROR;
    
    double m_R2 = 1; // Radius^2 of Riemannn sphere 

    SPlane m_planes[];
    
    int m_maxIterations = 100;
    
    public ReflectionGroup(SPlane planes[]){
        
        m_planes = new SPlane[planes.length];
        System.arraycopy(planes, 0, m_planes, 0, planes.length);
        
    }
   
    public void setRiemannSphereRadius(double value){
        m_R2 = value*value;
        if(DEBUG){
            printf("setRiemannSphereRadius(%10.5f)\n", value);
        }
            
    }

    public void setMaxIterations(int value){

        m_maxIterations = value;
        
    }
    
    /**
       transforms arbitrary point in 3D into fundamental domain using reflections in planes 
     */
    public int toFundamentalDomain(Vec pnt){
        
        int currentPlane = 0;
        int insideCount = 0;
        int planeCount = m_planes.length;
        int maxIter = m_maxIterations;
        int iter = 0;
        // make initial scale of transformation which transforms from Riemann sphere to R^3
        //pnt.mulScale(1 + len2(pnt)/m_R2);
        while(iter < maxIter){
            if(false)
                printf("iter: %2d plane: %d pnt: (%10.5f %10.5f %10.5f) \n", iter, currentPlane, pnt.v[0],pnt.v[1],pnt.v[2]);
            SPlane plane = m_planes[currentPlane];            
            
            if(plane.distance(pnt) < 0.0 ){
                
                if(false)printf(" out\n"); 
                plane.reflect(pnt);
                // we are now inside of this plane, but this transform may move point outside of other planes 
                insideCount = 1; 
                
            } else {
                
                if(false)printf(" in\n"); 
                insideCount++;
                if(insideCount >= planeCount){

                    // point is inside of all planes - it is in the fundamental domain 
                    if(false) {
                        if(pnt.getScaleFactor() != 1)
                            printf(" pnt.scaleFactor():%10.5f pnt: (%10.5f %10.5f %10.5f)\n", pnt.getScaleFactor(),pnt.v[0],pnt.v[1],pnt.v[2]); 
                    }
                    // make final scale of transformation which transforms R^3 to Riemann sphere 
                    pnt.mulScale(1/(1 + len2(pnt)/m_R2));
                    return RESULT_OK;
                }
            }
            
            currentPlane = (currentPlane+1)%planeCount;
            iter++;
        }

        // we are here is we exceeded maxIterations; 

        return RESULT_ERROR;
    }
 
    
    //   class to represent sphere or plane 
    public static abstract class SPlane {
        
        // return signed distance to that objetc        
        public abstract double distance(Vec pnt);
        // reflect the point
        public abstract void reflect(Vec pnt);
    } // class SPlane 
    
    //
    // class to represent plane 
    //
    public static class Plane extends SPlane {
        
        double dist;
        // normal to the plane normalized to 1 
        double nx, ny, nz;
        public Plane(Vector3d normal, double distance){
            
            double len = normal.length();
            nx = normal.x / len;
            ny = normal.y / len;
            nz = normal.z / len;
            dist = distance; 
        }
        
        public double distance(Vec pnt){
            // dot(p,normal) - dist:
            return dot(pnt, nx, ny, nz) - dist; 

        }

        public void reflect(Vec p){

            //vn = dot( v - normal*dist, normal);             
            double vn = 2*((p.v[0] - nx*dist)*nx + (p.v[1] - ny*dist)*ny + (p.v[2] - nz*dist)*nz); 
            //p -= normal*vn
            subSet(p, nx * vn,ny * vn,nz * vn);

        }
    } // class Plane 


    //
    // class to represent sphere
    //
    public static class Sphere extends SPlane {
        
        // radius of sphere 
        protected double r; 
        protected double r2; 
        // center of sphere 
        protected double cx, cy, cz;
        // positive r gives positive distances inside of sphere 
        // negative r gives positive distances outside of sphere 
        // 
        public Sphere(Vector3d center, double radius){

            r = radius;
            r2 = r*r;
            cx = center.x;
            cy = center.y;
            cz = center.z;
            if(DEBUG)
                printf("Sphere: (%7.5f %7.5f %7.5f), %7.5f\n ", cx, cy, cz, r);
        }

        public double distance(Vec p){

            //v -= s.center; 
            double 
                x = p.v[0] - cx,
                y = p.v[1] - cy,
                z = p.v[2] - cz;
                        
            double dot = len2(x,y,z);
            
            return 0.5*(r - dot/r);
            
        }

        public void reflect(Vec pnt){
            
            //v = v - s.center;
            subSet(pnt, cx, cy, cz);
                
            //float len2 = dot(v,v);
            double len2 = len2(pnt);
            
            //float factor = (r2/len2);
            double factor = r2/len2;
            //v *= factor;
            mulSet(pnt, factor);
            double scaling = factor;
            pnt.mulScale(scaling);
            //v += s.center;
            addSet(pnt, cx, cy, cz);

        }
    } // class Sphere
    

    static final double dot(Vec pnt, double x, double y, double z ){

        return pnt.v[0] * x + pnt.v[1] * y + pnt.v[2] * z; 

    }

    static final double len2(double x, double y, double z){
        return x*x + y*y + z*z;
    }

    static final double len2(Vec pnt){
        return pnt.v[0] * pnt.v[0] + pnt.v[1] * pnt.v[1] + pnt.v[2] * pnt.v[2];
    }
    
    static final void subSet(Vec pnt, double x, double y, double z){
        pnt.v[0] -= x;
        pnt.v[1] -= y;
        pnt.v[2] -= z;
    }
    static final void addSet(Vec pnt, double x, double y, double z){
        pnt.v[0] += x;
        pnt.v[1] += y;
        pnt.v[2] += z;
    }
    static final void mulSet(Vec pnt, double f){
        pnt.v[0] *= f;
        pnt.v[1] *= f;
        pnt.v[2] *= f;
    }
}
