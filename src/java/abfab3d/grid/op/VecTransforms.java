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

package abfab3d.grid.op;

import java.util.Vector;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;

import abfab3d.util.Vec;
import abfab3d.util.VecTransform;
import abfab3d.util.Initializable;

import static java.lang.Math.sqrt;

import static abfab3d.util.Output.printf;


/**
   a collection of various VecTransforms
   
 */
public class VecTransforms {
   
    /**

       identity transform, does nothing 
       only transfers data 
       
     */
    public static class Identity implements VecTransform {
        public int transform(Vec in, Vec out) {
            out.set(in);
            return RESULT_OK;
        }
        public int inverse_transform(Vec in, Vec out) {
            out.set(in);
            return RESULT_OK;
        }
        
    }

    /**

       wraps band in xy plane about a cylinder of given radius
       
     */
    public static class RingWrap implements VecTransform, Initializable {

        public double m_radius = 0.035; // units are meters       

        public int initialize(){
            // do sometithing? 
            return RESULT_OK;
        }
        /**
         * Calculate cartesian to polar coordinates
         *
         * @param in
         * @param out
         */
        public int transform(Vec in, Vec out) {
            
            double angle = in.v[0] / m_radius;
            double r = m_radius + in.v[2];
            double sina = Math.sin(angle);
            double cosa = Math.cos(angle);

            out.v[0] = r * sina;
            out.v[1] = in.v[1];
            out.v[2] = r * cosa;

            return RESULT_OK;
        }                

        /**
         * Calculate polar to cartesian coordinates
         * @param in
         * @param out
         */
        public int inverse_transform(Vec in, Vec out) {
            
            double wx = in.v[0] / m_radius;
            double wy = in.v[1];
            double wz = in.v[2] / m_radius;
            
            double dist = Math.sqrt(wx * wx + wz * wz);
            double angle = Math.atan2(wx, wz);
            
            wx = angle * m_radius;
            wz = (dist-1) * m_radius;
            
            out.v[0] = wx;
            out.v[1] = wy;
            out.v[2] = wz;

            return RESULT_OK;

        }
    }        

    /**
       performs rotation about given axis 
     */
    public static class Rotation implements VecTransform, Initializable {

        public Vector3d m_axis = new Vector3d(1,0,0); 
        public double m_angle = 0;

        private Matrix3d 
            mat = new Matrix3d(),
            mat_inv = new Matrix3d();

        public int initialize(){

            mat.set(new AxisAngle4d(m_axis.x,m_axis.y,m_axis.z,m_angle));
            mat_inv.set(new AxisAngle4d(m_axis.x,m_axis.y,m_axis.z,-m_angle));

            return RESULT_OK;
        }

        /**
         *
         */
        public int transform(Vec in, Vec out) {
            
            Vector3d v = new Vector3d(in.v[0],in.v[1],in.v[2]);

            mat.transform(v);

            out.v[0] = v.x;
            out.v[1] = v.y;
            out.v[2] = v.z;

            return RESULT_OK;
        }                

        /**
         *
         */
        public int inverse_transform(Vec in, Vec out) {
            
            Vector3d v = new Vector3d(in.v[0],in.v[1],in.v[2]);

            mat_inv.transform(v);

            out.v[0] = v.x;
            out.v[1] = v.y;
            out.v[2] = v.z;

            return RESULT_OK;

        }
    } // class Rotation 


    /**
       performs scaling by given factor
     */
    public static class Scale  implements VecTransform, Initializable  {

        public Vector3d m_scale = new Vector3d(1,1,1); 

        private Matrix3d 
            mat = new Matrix3d(),
            mat_inv = new Matrix3d();

        public int initialize(){

            mat.m00 = m_scale.x;
            mat.m11 = m_scale.y;
            mat.m22 = m_scale.z;

            mat_inv.m00 = 1/m_scale.x;
            mat_inv.m11 = 1/m_scale.y;
            mat_inv.m22 = 1/m_scale.z;

            return RESULT_OK;
        }

        /**
         *
         */
        public int transform(Vec in, Vec out) {
            
            Vector3d v = new Vector3d(in.v[0],in.v[1],in.v[2]);

            mat.transform(v);

            out.v[0] = v.x;
            out.v[1] = v.y;
            out.v[2] = v.z;

            return RESULT_OK;
        }                

        /**
         *
         */
        public int inverse_transform(Vec in, Vec out) {
            
            Vector3d v = new Vector3d(in.v[0],in.v[1],in.v[2]);

            mat_inv.transform(v);

            out.v[0] = v.x;
            out.v[1] = v.y;
            out.v[2] = v.z;

            return RESULT_OK;

        }
    } // class Scale



    /**
       performs inversion in a sphere of given center and radius 
     */
    public static class SphereInversion  implements VecTransform, Initializable  {

        public Vector3d m_center = new Vector3d(0,0,1); 
        public double m_radius = sqrt(2.); 

        private double radius2; 
        static double EPS = 1.e-20;

        public int initialize(){

            radius2 = m_radius*m_radius; 
            return RESULT_OK;

        }

                
        /**
         *
         */
        public int transform(Vec in, Vec out) {
            
            double x = in.v[0];
            double y = in.v[1];
            double z = in.v[2];
            
            // move center to origin 
            x -= m_center.x;
            y -= m_center.y;
            z -= m_center.z;
            double r2 = (x*x + y*y + z*z);
            if(r2 < EPS) r2 = EPS;

            double scale = radius2/r2;
            
            x *= scale;
            y *= scale;
            z *= scale;

            // move origin back to center
            x += m_center.x;
            y += m_center.y;
            z += m_center.z;
            
            out.v[0] = x;
            out.v[1] = y;
            out.v[2] = z;
            
            return RESULT_OK;
        }                

        /**
         *  composite transform is identical to direct transform
         */
        public int inverse_transform(Vec in, Vec out) {
            
            transform(in, out);

            return RESULT_OK;

        }
    } // class SphereInversion

    
    /**
       reflect in a given plane 
     */
    public static class PlaneReflection  implements VecTransform, Initializable  {

        public Vector3d m_pointOnPlane = new Vector3d(0,0,0); 
        public Vector3d m_planeNormal = new Vector3d(1,0,0); 


        public int initialize(){

            m_planeNormal.normalize();

            return RESULT_OK;

        }

                
        /**
         *
         */
        public int transform(Vec in, Vec out) {
            
            double x = in.v[0];
            double y = in.v[1];
            double z = in.v[2];
            
            // move center to origin 
            x -= m_pointOnPlane.x;
            y -= m_pointOnPlane.y;
            z -= m_pointOnPlane.z;

            double dot = 2*(x*m_planeNormal.x + y*m_planeNormal.y + z*m_planeNormal.z);
            
            x -= dot*m_planeNormal.x;
            y -= dot*m_planeNormal.y;
            z -= dot*m_planeNormal.z;

            // move origin back to center
            x += m_pointOnPlane.x;
            y += m_pointOnPlane.y;
            z += m_pointOnPlane.z;
            
            out.v[0] = x;
            out.v[1] = y;
            out.v[2] = z;
            
            return RESULT_OK;
        }                

        /**
         *  composite transform is identical to direct transform
         */
        public int inverse_transform(Vec in, Vec out) {
            
            transform(in, out);

            return RESULT_OK;

        }
    } // class PlaneReflection


    /**
       
     */
    public static class CompositeTransform implements VecTransform, Initializable {
        
        public Vector<VecTransform> m_transforms = new Vector<VecTransform>();

        
        public void add(VecTransform transform){

            m_transforms.add(transform);

        }

        public int initialize(){
            
            for(int i = 0; i < m_transforms.size(); i++){
                 VecTransform tr = m_transforms.get(i);
                if(tr instanceof Initializable){
                    int res = ((Initializable)tr).initialize();
                    if(res != RESULT_OK)
                        return res;
                }
            }
            
            return RESULT_OK;
        }

        /**
         *
         */
        public int transform(Vec in, Vec out) {
            
            if(m_transforms.size() < 1){
                // copy input to output                 
                out.set(in);
                return RESULT_OK;                
            }

            for(int i = 0; i < m_transforms.size(); i++){

                VecTransform tr = m_transforms.get(i);
                int res = tr.transform(in, out);
                if(res != RESULT_OK)
                    return res;

                in.set(out);
            }
            
            return RESULT_OK;
        }                

        /**
         *
         */
        public int inverse_transform(Vec in, Vec out) {
            
            if(m_transforms.size() < 1){
                // copy input to output                 
                out.set(in);
                return RESULT_OK;                
            }

            for(int i = m_transforms.size()-1; i >= 0; i--){

                VecTransform tr = m_transforms.get(i);
                int res = tr.inverse_transform(in, out);
                if(res != RESULT_OK)
                    return res;
                in.set(out);
            }
            
            return RESULT_OK;

        }
    }        

}
