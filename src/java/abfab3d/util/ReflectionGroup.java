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
    
    static final boolean DEBUG = false;
    static int debugCount = 1000;
    
    public static final int RESULT_OK = ResultCodes.RESULT_OK;
    public static final int RESULT_ERROR = ResultCodes.RESULT_ERROR;
    public static final int RESULT_OUTSIDE = ResultCodes.RESULT_OUTSIDE;
    
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
                // we are outside of this plane - reflect in this plane 
                if(false)printf(" out\n"); 

                plane.reflect(pnt);
                // we are now inside of this plane, but this transform may move point outside of other planes 
                insideCount = 1; 
                
            } else {
                
                if(false)printf(" in\n"); 
                insideCount++;
                if(insideCount >= planeCount){

                    // point is inside of all planes => point is in the fundamental domain 
                    if(false) {
                        if(pnt.getScaleFactor() != 1)
                            printf(" pnt.scaleFactor():%10.5f pnt: (%10.5f %10.5f %10.5f)\n", pnt.getScaleFactor(),pnt.v[0],pnt.v[1],pnt.v[2]); 
                    }
                    // make final scaling of transformation which transforms R^3 to Riemann sphere 
                    if(m_R2 != 0.0)
                        pnt.mulScale(1/(1 + len2(pnt)/m_R2));

                    if(DEBUG && debugCount-- > 0)
                        printf("plane reflect vs: %5.3f mm\n", pnt.getScaledVoxelSize()*1000);

                    return RESULT_OK;
                }
            }
            
            currentPlane = (currentPlane+1)%planeCount;
            iter++;
        }

        // we are here is we exceeded maxIterations; 

        return RESULT_OUTSIDE;
    }
 


    //   class to represent sphere or plane 
    public static abstract class SPlane {
        
        // return signed distance to that objetc        
        public abstract double distance(Vec pnt);
        // reflect the point
        public abstract void reflect(Vec pnt);
        public abstract double getCosAngle(SPlane sp);
    } // class SPlane 
    

    //
    // class to represent half space plane with external normal and distance along normal to origin 
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
        public Vector3d getNormal(){
            return new Vector3d(nx, ny, nz);
        }

        public double getCosAngle(SPlane sp){

            if(sp instanceof Sphere){
                return ReflectionGroup.getCosAngle((Sphere)sp, this);
            } else {
                return ReflectionGroup.getCosAngle(this, (Plane)sp);
            }
        }

        public double getDistance(){
            return dist;
        }
        

        public double distance(Vec pnt){
            // dist - dot(p,normal) 
            return dist - dot(pnt, nx, ny, nz); 

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

        public Vector3d getCenter(){
            return new Vector3d(cx, cy, cz);
        }
        public double getRadius(){
            return r;
        }

        public double getCosAngle(SPlane sp){

            if(sp instanceof Sphere){
                return ReflectionGroup.getCosAngle(this, (Sphere)sp);
            } else {
                return ReflectionGroup.getCosAngle(this, (Plane)sp);
            }
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

    public static double getCosAngle(Sphere s1, Sphere s2){

        double r1 = s1.getRadius();
        double r2 = s2.getRadius();
        Vector3d c1 = s1.getCenter();
        Vector3d c2 = s2.getCenter();
        double dx = c1.x - c2.x;
        double dy = c1.y - c2.y;
        double dz = c1.z - c2.z;
        
        double d = Math.sqrt(dx*dx + dy*dy + dz*dz);

        return (d*d - r1*r1 - r2*r2)/(2*r1*r2);
                
    }

    public static double getCosAngle(Plane p1, Plane p2){

        Vector3d n1 = p1.getNormal();
        Vector3d n2 = p2.getNormal();
        return n1.dot(n2);

    }
    
    public static double getCosAngle(Sphere s1, Plane p2){

        Vector3d c1 = s1.getCenter();
        double r1 = s1.getRadius();
        Vector3d n2 = p2.getNormal();
        double r2 = p2.getDistance();
        if(true)
        throw new IllegalArgumentException("getCosAngle(Sphere s1, Plane p2) not implemented");
        //TODO 
        return 0;

    }
  
}
