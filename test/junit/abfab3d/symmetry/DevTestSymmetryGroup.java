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
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

public class DevTestSymmetryGroup {
    
    static SymmetryGroup getTwoPlanes(double x1, double x2){
        
        return new SymmetryGroup(
                             new SPlane[] { 
                                 new Plane(new Vector3d(-1,0,0), -x1), // right of  plane 1
                                 new Plane(new Vector3d(1,0,0), x2),   // left of plane 2
                             }
                             );
        
    }

    static SymmetryGroup getCube(){
        
        return new SymmetryGroup(
                             new SPlane[] { 
                                 new Plane(new Vector3d(-1,0,0), 1), 
                                 new Plane(new Vector3d(1,0,0), 1),  
                                 new Plane(new Vector3d(0,1,0), 1),  
                                 new Plane(new Vector3d(0,-1,0), 1),  
                                 new Plane(new Vector3d(0,0,1), 1),  
                                 new Plane(new Vector3d(0,0,-1), 1),  
                             }
                             );
        
    }

    static SymmetryGroup getCubeE(){
        
        Plane sides[] = new Plane[] { 
            new Plane(new Vector3d(-1,0,0), 1), 
            new Plane(new Vector3d(1,0,0), 1),  
            new Plane(new Vector3d(0,1,0), 1),  
            new Plane(new Vector3d(0,-1,0), 1),  
            new Plane(new Vector3d(0,0,1), 1),  
            new Plane(new Vector3d(0,0,-1), 1),  
        };

        return new SymmetryGroup(sides, 
                                 new ETransform[]{
                                     new ETransform(sides[0]),
                                     new ETransform(sides[1]),
                                     new ETransform(sides[2]),
                                     new ETransform(sides[3]),
                                     new ETransform(sides[4]),
                                     new ETransform(sides[5])
                                 }
                                 );
        
    }

    static SymmetryGroup getSphere(){
        
        return new SymmetryGroup(
                             new SPlane[] { 
                                 new Sphere(new Vector3d(0,0,0),1), 
                             }
                             );
        
    }

    static SymmetryGroup getLens(){
        double x = 1 - Math.sqrt(3)/2;
        double r = 0.5;
        double C = (r*r - x*x)/(2*x);
        double R = Math.sqrt(C*C+r*r);
        printf("lens: x: %7.5f C:%7.5f R:%7.5f\n",x,C,R);
        return new SymmetryGroup(
                             new SPlane[] { 
                                 new Sphere(new Vector3d(C,0,0),R), 
                                 new Sphere(new Vector3d(-C,0,0),R), 
                             }
                             );
        
    }
    
    public void devTestPlanes(){

        //SymmetryGroup group = getTwoPlanes(1., 2.);
        SymmetryGroup group = getCubeE();
        //SymmetryGroup group = getSphere();
        //SymmetryGroup group = getLens();
        Vec pnt = new Vec(3);
        for(int i = 0; i < 100; i++){
        //for(int i = 0; i < 1; i++){
            double x = 0.1*i;
            double y = x;
            double z = y;
            pnt.v[0] = x;
            pnt.v[1] = y;
            pnt.v[2] = z;
            group.toFD(pnt);
            printf("(%7.3f, %7.3f, %7.3f) -> (%7.3f,%7.3f,%7.3f)\n", x,y,z,pnt.v[0],pnt.v[1],pnt.v[2]);
        }
    }

    public void devTestSphere(){

        Sphere s = new Sphere(new Vector3d(-0.8,0,0),1);
        Vec pnt = new Vec(3);
        //for(int i = 1; i <= 100; i++){
        for(int i = 0; i < 1; i++){
            double x = 0.1*i;
            double y = 0;
            double z = 0;
            pnt.v[0] = x;
            pnt.v[1] = y;
            pnt.v[2] = z;
            s.transform(pnt);
            printf("(%7.3f, %7.3f, %7.3f) -> (%7.3f,%7.3f,%7.3f)\n", x,y,z,pnt.v[0],pnt.v[1],pnt.v[2]);
        }
    }
    

    public static void main(String arg[]){
        new DevTestSymmetryGroup().devTestPlanes();
        //new DevTestSymmetryGroup().devTestSphere();
    }

}

