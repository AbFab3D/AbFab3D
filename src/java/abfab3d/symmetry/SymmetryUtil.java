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

package abfab3d.symmetry;
import javax.vecmath.Vector3d;

import abfab3d.util.Vec;
import abfab3d.util.ResultCodes;

import static abfab3d.util.Output.printf;

/**
 *  utils for symmetry package 
 *  
 *
 * 
 */
public class SymmetryUtil {

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

    /**
       angle is defined as angle between external normals 
     */
    public static double getCosAngle(Sphere s1, Sphere s2){

        double r1 = s1.getRadius();
        double r2 = s2.getRadius();
        Vector3d c1 = s1.getCenter();
        Vector3d c2 = s2.getCenter();
        double dx = c1.x - c2.x;
        double dy = c1.y - c2.y;
        double dz = c1.z - c2.z;
        
        double d = Math.sqrt(dx*dx + dy*dy + dz*dz);

        return -(d*d - r1*r1 - r2*r2)/(2*r1*r2);
                
    }

    /**
       angle is defined as angle between external normals 
     */
    public static double getCosAngle(Plane p1, Plane p2){

        Vector3d n1 = p1.getNormal();
        Vector3d n2 = p2.getNormal();
        return n1.dot(n2);

    }
    
    /**
       angle is defined as angle between external normals 
     */
    public static double getCosAngle(Sphere s1, Plane p2){

        Vector3d c1 = s1.getCenter();
        double r1 = s1.getRadius();
        Vector3d n2 = p2.getNormal();
        double d2 = p2.getDistance();
        return  -(c1.dot(n2) - d2)/r1;

    }


    /**
       transforms arbitrary 3D point into fundamental domain 
       FD is defined as array of splanes 
       pairing transforms are reflections in splanes 
     */
    static public int toFundamentalDomain(Vec pnt, SPlane splanes[], int maxIterations){
        
        int planeCount = splanes.length;
        int iter = maxIterations;
        while(iter-- > 0){

            boolean foundOutside = false; 

            for(int i =0; i < planeCount; i++){
                double d = splanes[i].distance(pnt);
                if(d > 0) {
                    foundOutside = true;
                    splanes[i].transform(pnt);
                    break; // out of planes cycle                     
                }                           
            }
            
            if(!foundOutside){
                // we are in FD
                return ResultCodes.RESULT_OK;
            }
        }        

        // we are here if we have reached maxIterations; 

        return ResultCodes.RESULT_OUTSIDE;
    }

    static public int toFundamentalDomain(Vec pnt, FDPlane planes[], PairingTransform  transforms[], int maxIterations){
        
        int planeCount = planes.length;
        int iter = maxIterations;
        while(iter-- > 0){

            boolean foundOutside = false; 

            for(int i =0; i < planeCount; i++){
                double d = planes[i].distance(pnt);
                
                if(d > 0) {
                    foundOutside = true;
                    transforms[i].transform(pnt);
                    //printf("i:%d (%7.5f,%7.5f,%7.5f)\n",i, pnt.v[0],pnt.v[1],pnt.v[2]);
                    break; // out of planes cycle                     
                }                           
            }
            
            if(!foundOutside){
                // we are in FD
                return ResultCodes.RESULT_OK;
            }
        }        

        // we are here if we have reached maxIterations; 

        return ResultCodes.RESULT_OUTSIDE;
    }  
}
