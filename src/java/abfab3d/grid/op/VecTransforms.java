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
import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;
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
       see RingSpaceWarp.svg 
       
     */
    public static class RingWrap implements VecTransform, Initializable {

        public double m_radius = 0.035; // units are meters       
        
        public void setRadius(double r){
            m_radius = r;
        }

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

        public void setRotation(Vector3d axis, double angle){

            m_axis = new Vector3d(axis); 
            m_angle = angle;
            
        }

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
    public static class Scale  implements VecTransform {

        protected double sx = 1, sy = 1, sz = 1; 

        public void setScale(double s){

            sx = s;
            sy = s;
            sz = s;

        }

        /**
         *
         */
        public int transform(Vec in, Vec out) {
            
            out.v[0] = in.v[0]*sx;
            out.v[1] = in.v[1]*sy;
            out.v[2] = in.v[2]*sz;

            return RESULT_OK;
        }                

        /**
         *
         */
        public int inverse_transform(Vec in, Vec out) {
            
            out.v[0] = in.v[0]/sx;
            out.v[1] = in.v[1]/sy;
            out.v[2] = in.v[2]/sz;

            return RESULT_OK;

        }
    } // class Scale


    /**
       
       perforsm tranaslation 
       
     */
    public static class Translation  implements VecTransform {

        protected double tx = 1, ty = 1, tz = 1; 

        public void setTranslation(double tx, double ty, double tz){

            this.tx = tx;
            this.ty = ty;
            this.tz = tz;

        }

        /**
         *
         */
        public int transform(Vec in, Vec out) {
            
            out.v[0] = in.v[0] + tx;
            out.v[1] = in.v[1] + ty;
            out.v[2] = in.v[2] + tz;

            return RESULT_OK;
        }                

        /**
         *
         */
        public int inverse_transform(Vec in, Vec out) {
            
            out.v[0] = in.v[0] - tx;
            out.v[1] = in.v[1] - ty;
            out.v[2] = in.v[2] - tz;

            return RESULT_OK;

        }
    } // class Translation


    /**
       performs inversion in a sphere of given center and radius 
     */
    public static class SphereInversion  implements VecTransform, Initializable  {

        public Vector3d m_center = new Vector3d(0,0,1); 
        public double m_radius = sqrt(2.); 

        private double radius2; 
        static double EPS = 1.e-20;

        public void setSphere(Vector3d center, double radius){

            m_center = new Vector3d(center);
            m_radius = radius;

        }

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


    /**
       makes transformations to reproduce one of frieze symmetry patterns
     */
    public static class FriezeSymmetry  implements VecTransform, Initializable  {

        public static final int     // orbifold notation
            FRIEZE_II = 0,   // oo oo
            FRIEZE_IX = 1,   // oo X
            FRIEZE_IS = 2,   // oo *
            FRIEZE_SII = 3,  // * oo oo
            FRIEZE_22I = 4,  // 2 2 oo
            FRIEZE_2SI = 5,  // 2 * oo
            FRIEZE_S22I = 6; // * 2 2 oo
        
        
        public int m_maxCount = 100; // maximal number of iterations to gett to FD 
        public double m_domainWidth = 0.01; // width of fundamental domain in meters
        public int m_friezeType; 

        // sides of fundamental domain (FD). 
        // Sides point outside of fundanetal domain. 
        // points inside of FD satisfy pnt.dot(sides[i]) < 0
        // points outside of FD satisfy pnt.dot(sides[i]) > 0        
        protected Vector4d planes[];
        
        protected Matrix4d trans[]; // pairng transformations of fundamental domain 
        // trans[i] transform FD into area adjacent to side i
        protected Matrix4d invtrans[]; // inverse transformations 
        // intrans[i] transforms area adjacent to side i into FD 
        // fundamental domain 

        public void setFriezeType(int friezeType){
            m_friezeType = friezeType;
        }
        public void setDomainWidth(double width){
            m_domainWidth = width;
        }

        public int initialize(){

            switch(m_friezeType){
            default: 
            case FRIEZE_II:  initII(); break;
            case FRIEZE_S22I:initS22I(); break;
            case FRIEZE_IS:  initIS(); break;
            case FRIEZE_SII: initSII(); break;
            case FRIEZE_2SI: init2SI(); break;
            case FRIEZE_22I: init22I(); break;
            case FRIEZE_IX:  initIX(); break;
            }

            return RESULT_OK;

        }

        protected void initII(){

            planes = new Vector4d[2]; 
            planes[0] = new Vector4d(-1,0,0,-m_domainWidth/2);
            planes[1] = new Vector4d(1,0,0,-m_domainWidth/2);
            Vector4d p2 = new Vector4d(1,0,0,0);

            Matrix4d m0 = getReflection(planes[0]);
            Matrix4d m1 = getReflection(p2);

            trans = new Matrix4d[2];

            trans[0] = new Matrix4d(); 
            trans[1] = new Matrix4d(); 

            trans[0].mul(m0,m1);
            trans[1].mul(m1,m0);
            
            invtrans = makeInversion(trans);
        }

        protected void initIX(){

            planes = new Vector4d[2]; 
            planes[0] = new Vector4d(-1,0,0,-m_domainWidth/2);
            planes[1] = new Vector4d(1,0,0,-m_domainWidth/2);
            Vector4d p2 = new Vector4d(1,0,0,0);
            Vector4d p3 = new Vector4d(0,1,0,0);

            Matrix4d m0 = getReflection(planes[0]);
            Matrix4d m1 = getReflection(p2);
            Matrix4d t = new Matrix4d(); 
            t.mul(m0, m1);
            t.mul(getReflection(p3));
            
            trans = new Matrix4d[2];

            trans[0] = t;
            trans[1] = new Matrix4d(); 
            trans[1].invert(t);
            
            invtrans = makeInversion(trans);
        }

        protected void init22I(){

            planes = new Vector4d[2]; 
            planes[0] = new Vector4d(-1,0,0,-m_domainWidth/2);
            planes[1] = new Vector4d(1,0,0,-m_domainWidth/2);
            Vector4d p2 = new Vector4d(0,1,0,0);

            trans = new Matrix4d[2];

            trans[0] = new Matrix4d(); 
            trans[1] = new Matrix4d(); 

            trans[0].mul(getReflection(planes[0]),getReflection(p2));
            trans[1].mul(getReflection(planes[1]),getReflection(p2));
            
            invtrans = makeInversion(trans);
        }

        protected void initSII(){

            planes = new Vector4d[2]; 
            planes[0] = new Vector4d(-1,0,0,-m_domainWidth/2);
            planes[1] = new Vector4d(1,0,0,-m_domainWidth/2);

            trans = new Matrix4d[2];

            trans[0] = getReflection(planes[0]);
            trans[1] = getReflection(planes[1]);
            
            invtrans = makeInversion(trans);
        }

        protected void initIS(){

            planes = new Vector4d[3]; 
            planes[0] = new Vector4d(-1,0,0,-m_domainWidth/2);
            planes[1] = new Vector4d(1,0,0,-m_domainWidth/2);
            planes[2] = new Vector4d(0,-1,0,0);

            Vector4d p2 = new Vector4d(1,0,0,0);

            Matrix4d m0 = getReflection(planes[0]);
            Matrix4d m1 = getReflection(p2);

            trans = new Matrix4d[3];

            trans[0] = new Matrix4d(); 
            trans[1] = new Matrix4d(); 

            trans[0].mul(m0,m1);
            trans[1].mul(m1,m0);
            trans[2] = getReflection(planes[2]);
            
            invtrans = makeInversion(trans);
        }

        protected void initS22I(){

            planes = new Vector4d[3]; 
            planes[0] = new Vector4d(-1,0,0,-m_domainWidth/2);
            planes[1] = new Vector4d(1,0,0,-m_domainWidth/2);
            planes[2] = new Vector4d(0,-1,0,0);

            trans = new Matrix4d[3];

            trans[0] = getReflection(planes[0]);
            trans[1] = getReflection(planes[1]);
            trans[2] = getReflection(planes[2]);
            
            invtrans = makeInversion(trans);
        }

        protected void init2SI(){

            planes = new Vector4d[3]; 
            planes[0] = new Vector4d(-1,0,0,-m_domainWidth/2);
            planes[1] = new Vector4d(1,0,0,-m_domainWidth/2);
            planes[2] = new Vector4d(0,-1,0,0);

            trans = new Matrix4d[3];

            trans[0] = getReflection(planes[0]);
            trans[1] = getReflection(planes[1]);
            
            trans[2] = new Matrix4d();
            trans[2].mul(getReflection(planes[2]), getReflection(new Vector4d(1,0,0,0))); // rotation pi
            
            invtrans = makeInversion(trans);

        }

                
        /**
         *
         */
        public int transform(Vec in, Vec out) {
            
            // this is one  to many transform 
            // it only makes sence for inverse transform 
            // so we apply only identity transform to the input 
            double x = in.v[0];
            double y = in.v[1];
            double z = in.v[2];
            
            out.v[0] = x;
            out.v[1] = y;
            out.v[2] = z;
            
            return RESULT_OK;
        }                

        /**
         *  composite transform is identical to direct transform
         */
        public int inverse_transform(Vec in, Vec out) {
            
            Vector4d vin = new Vector4d(in.v[0],in.v[1],in.v[2],1);

            toFundamentalDomain(vin, planes, invtrans, m_maxCount);
            
            // save result 
            out.v[0] = vin.x;
            out.v[1] = vin.y;
            out.v[2] = vin.z;
            
            return RESULT_OK;

        }
    } // class FriezeSymmetry




    /**
       return tranform corresponding to reflection in the given plane
       plane is expected to be normalized 
    */
    public static Matrix4d getReflection(Vector4d p){
        
        Matrix4d m = new Matrix4d();

        // transformation act on 4D vectors as followns 
        //  x-> x - 2*p*dot(p,x);

        m.m00 = 1-2*p.x*p.x;
        m.m01 =  -2*p.x*p.y;
        m.m02 =  -2*p.x*p.z;
        m.m03 =  -2*p.x*p.w;

        m.m10 =  -2*p.y*p.x;
        m.m11 = 1-2*p.y*p.y;
        m.m12 =  -2*p.y*p.z;
        m.m13 =  -2*p.y*p.w;

        m.m20 =  -2*p.z*p.x;
        m.m21 =  -2*p.z*p.y;
        m.m22 = 1-2*p.z*p.z;
        m.m23 =  -2*p.z*p.w;
       
        m.m30 =  0;
        m.m31 =  0;
        m.m32 =  0;
        m.m33 =  1;
       
        return m;
    }
    
    /**
       return array of inverse transforms 
    */
    public static Matrix4d[] makeInversion(Matrix4d trans[]){
        
        Matrix4d itr[] = new Matrix4d[trans.length];
        for(int i = 0; i < itr.length; i++){
            itr[i] = new Matrix4d();
            itr[i].invert(trans[i]);
        }
        
        return itr;
    }

    /**
       normalizes each vector of the array 
     */
    static public void normalize(Vector4d v[]){

        for(int i =0; i < v.length; i++){
            v[i].normalize();
        }        
    }

    static public void normalizePlane(Vector4d p){
        double norm = Math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z);
        p.scale(1./norm);
        
    }

    public static int toFundamentalDomain(Vector4d v, Vector4d planes[], Matrix4d[] trans, int maxCount ){
        
        // transform point to fundamental domain
        int count = maxCount;

        while(count-- > 0){

            boolean found = false; 

            for(int i =0; i < planes.length; i++){
                double d = planes[i].dot(v);
                //printf("d[%d]:%7.2f\n", i, d);

                if(d > 0) {
                    found = true;
                    trans[i].transform(v);
                    //printf("    v: (%5.2f,%5.2f,%5.2f,%5.2f)\n", v.x, v.y, v.z, v.w);
                    break; // out of planes cycle                     
                }                           
            }
            
            if(!found){
                // we are in FD
                return 0;
            }
        }        
        // if we are here - we haven't found FD 
        // do somthing 
        //printf("out of iterations\n");
        return -1;
    }
}
