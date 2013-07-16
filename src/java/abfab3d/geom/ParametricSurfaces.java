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

import javax.vecmath.Vector3d;

import abfab3d.util.Vec; 

import abfab3d.util.TriangleCollector;

import static java.lang.Math.sqrt;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.PI;

import static abfab3d.util.Output.printf;


/**
 *  various parametric surfaces 
 * @author Vladimir Bulatov
 */
public class ParametricSurfaces { 

    /**
       torus of given small and large radiuses, center, and axis
     */
    public static class Torus implements ParametricSurface {

        double rin, rout;
        Vector3d center = new Vector3d();
        Vector3d axis = new Vector3d(0,0,1);
        
        public Torus(double rin, double rout){
            this.rin = rin;
            this.rout = rout;
        }

        public Torus(double ri, double r0, Vector3d center, Vector3d axis){

            this.center.set(center);
            this.axis.set(axis);
            //TODO - init rotation matrix 

        }
    
        public double[] getDomainBounds(){
            // the domain is unit square
            return new double[]{0,1,0,1};            
        }

        public int[] getGridSize(){
            return new int[]{4,4};
        }

        public Vector3d getPoint(Vector3d in, Vector3d out){
            
            double u = in.x;
            double v = in.y;
            
            double phi = 2*PI*u;
            double theta = 2*PI*v;

            double rxy = rout + rin*cos(phi);
            

            out.x = rxy * cos(theta);
            out.y = rxy * sin(theta);            
            out.z = -rin*sin(phi);
            //printf("(%10.5f,%10.5f) ->(%10.5f,%10.5f,%10.5f)\n",in.x, in.y, out.x,out.y,out.z);
            // TODO rotate out if necessary 
            // shift the center 
            out.x += center.x;
            out.y += center.y;
            out.z += center.z;
            
            return out;
        }
        
        public double getVolume(){
            return (PI*rin*rin)*(2*PI*rout);
        }

        public double getArea(){
            return (2*PI*rin)*(2*PI*rout);
        }

    } // Torus 


    /**
       sphere of given radius and center 
     */
    public static class Sphere implements ParametricSurface {

        double rad;
        Vector3d center = new Vector3d();
        
        public Sphere(double rad){
            this.rad = rad;
        }
        public Sphere(double rad, Vector3d center){
            this.rad = rad;
            this.center.set(center);
        }
    
        public double[] getDomainBounds(){
            // the domain is unit square
            return new double[]{0,1,0,1};            
        }

        public int[] getGridSize(){
            return new int[]{4,2};
        }

        public Vector3d getPoint(Vector3d in, Vector3d out){
            
            double u = in.x;
            double v = in.y;
            
            double phi = 2*PI*u;
            double theta = PI*v;

            double z = -rad*cos(theta);
            double x = rad*sin(theta)*cos(phi);
            double y = rad*sin(theta)*sin(phi);
            
            out.x = x;
            out.y = y;
            out.z = z;
            
            // shift the center 
            out.x += center.x;
            out.y += center.y;
            out.z += center.z;
            
            return out;
        }
        
        public double getVolume(){
            return (4*PI/3)*rad*rad*rad;
        }

        public double getArea(){
            return (4*PI*rad*rad);
        }

    } // Sphere


}
