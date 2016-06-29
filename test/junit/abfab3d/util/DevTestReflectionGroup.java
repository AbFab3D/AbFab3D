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

import abfab3d.core.Vec;

import javax.vecmath.Vector3d;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

public class DevTestReflectionGroup {
    
    static void testInvesion(){


        //ReflectionGroup.SPlane planes[] = getTwoSpheres(10*MM, 11*MM);
        ReflectionGroup.SPlane planes[] = getPlaneAndSphere(10*MM, 7*MM);
        //ReflectionGroup.SPlane planes[] = getTwoPlanes(0*MM, 1*MM);
        
        ReflectionGroup group = new ReflectionGroup(planes);
        
        Vec pnt = new Vec(3);
        
        for(int i = -80; i <= 80; i++){
            
            double x = i*0.1*MM;
            pnt.v[0] = x;
            pnt.v[1] = 0.1*MM;
            pnt.v[2] = 0;
            pnt.setVoxelSize(0.1*MM);
            
            group.toFundamentalDomain(pnt);
            printf("x: %7.2f -> (%7.2f %7.2f %7.2f) vs: %7.4f\n", x/MM, pnt.v[0]/MM,pnt.v[1]/MM,pnt.v[2]/MM,pnt.getVoxelSize()/MM);
        }

    }

    static ReflectionGroup.SPlane[] getTwoPlanes(double x1, double x2){
        
        return new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Plane(new Vector3d(1,0,0), x1), // right of  plane 1
            new ReflectionGroup.Plane(new Vector3d(-1,0,0), -x2), // left of plane 2
        };

    }
    
    static ReflectionGroup.SPlane[] getPlaneAndSphere(double x, double r){

        
        return new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Plane(new Vector3d(1,0,0), 0.), // right of yz plane
            new ReflectionGroup.Sphere(new Vector3d(x,0,0), -r), // outside of sphere  
        };
        
    }
    
    static ReflectionGroup.SPlane[] getTwoSpheres(double r1, double r2){
        
        if(r1 > r2) {
            double t = r2;
            r2 = r1;
            r1 = t;
        }

        return new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Sphere(new Vector3d(0,0,0), -r1), // outside of smaller sphere 
            new ReflectionGroup.Sphere(new Vector3d(0,0,0), r2), // inside of larger sphere  
        };
    }

    static ReflectionGroup.SPlane[] getCube(double r1, double r2){
        
        if(r1 > r2) {
            double t = r2;
            r2 = r1;
            r1 = t;
        }

        return new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Sphere(new Vector3d(0,0,0), -r1), // outside of smaller sphere 
            new ReflectionGroup.Sphere(new Vector3d(0,0,0), r2) // inside of larger sphere  
                };
    }


    static void testReminder(){
        int n = 2;
        for(int k = -20; k <= 20; k++){

            //int i = (k < 0) ? ((k % n) + n): k % n;
            int i =                 k % n;
            printf("k: %3d -> i: %2d\n", k,i);
        }
    }

    public static void main(String arg[]){
        //testInvesion();
        testReminder();
    }

}

