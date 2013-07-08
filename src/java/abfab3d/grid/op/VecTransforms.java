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
import abfab3d.util.Symmetry;
import abfab3d.util.ReflectionGroup;

import net.jafama.FastMath;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Symmetry.getReflection;
import static abfab3d.util.Symmetry.toFundamentalDomain;


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


        public RingWrap(){
        };
        
        public RingWrap(double r){
            m_radius = r;            
        }
        
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
            
            out.set(in);

            double angle = in.v[0] / m_radius;
            double r = m_radius + in.v[2];
            double sina = FastMath.sin(angle);
            double cosa = FastMath.cos(angle);

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
            
            out.set(in);
            double wx = in.v[0] / m_radius;
            double wy = in.v[1];
            double wz = in.v[2] / m_radius;
            
            double dist = FastMath.sqrt(wx * wx + wz * wz);
            double angle = FastMath.atan2(wx, wz);


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

        private Vector3d m_axis = new Vector3d(1,0,0); 
        private double m_angle = 0;

        private Matrix3d 
            mat = new Matrix3d(),
            mat_inv = new Matrix3d();

        public Rotation(){
        }

        public Rotation(Vector3d axis, double angle){

            setRotation(axis, angle);

        }

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

            out.set(in);
            double x,y,z;

            x = in.v[0];
            y = in.v[1];
            z = in.v[2];
            
            out.v[0] = mat.m00* x + mat.m01*y + mat.m02*z;
            out.v[1] = mat.m10* x + mat.m11*y + mat.m12*z;
            out.v[2] = mat.m20* x + mat.m21*y + mat.m22*z;

            return RESULT_OK;
        }

        /**
         *
         */
        public int inverse_transform(Vec in, Vec out) {

            out.set(in);

            double x,y,z;

            x = in.v[0];
            y = in.v[1];
            z = in.v[2];

            out.v[0] = mat_inv.m00* x + mat_inv.m01*y + mat_inv.m02*z;
            out.v[1] = mat_inv.m10* x + mat_inv.m11*y + mat_inv.m12*z;
            out.v[2] = mat_inv.m20* x + mat_inv.m21*y + mat_inv.m22*z;

            return RESULT_OK;

        }

    } // class Rotation


    /**
       performs scaling by given factor
     */
    public static class Scale  implements VecTransform {

        protected double sx = 1., sy = 1., sz = 1.; 
        protected double averageScale = 1.;

        public Scale(){
        }

        public Scale(double s){
            setScale(s);
        }

        public Scale(double sx, double sy, double sz){
            setScale(sx,sy,sz);
        }
        
        public void setScale(double s){

            sx = s;
            sy = s;
            sz = s;
            this.averageScale = s;

        }

        public void setScale(double sx, double sy, double sz){

            this.sx = sx;
            this.sy = sy;
            this.sz = sz;

            this.averageScale = Math.pow(sz*sy*sz, 1./3);
        }

        /**
         *
         */
        public int transform(Vec in, Vec out) {
            
            out.set(in);
            
            out.v[0] = in.v[0]*sx;
            out.v[1] = in.v[1]*sy;
            out.v[2] = in.v[2]*sz;

            out.mulScale(averageScale);

            return RESULT_OK;
        }                

        /**
         *
         */
        public int inverse_transform(Vec in, Vec out) {

            out.set(in);
            out.v[0] = in.v[0]/sx;
            out.v[1] = in.v[1]/sy;
            out.v[2] = in.v[2]/sz;

            out.mulScale(1/averageScale);
            
            return RESULT_OK;

        }
    } // class Scale


    /**
       
       performs translation
       
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
            
            out.set(in);
            out.v[0] = in.v[0] + tx;
            out.v[1] = in.v[1] + ty;
            out.v[2] = in.v[2] + tz;


            return RESULT_OK;
        }                

        /**
         *
         */
        public int inverse_transform(Vec in, Vec out) {
            out.set(in);
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
        public double m_radius = FastMath.sqrt(2.);

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
            
            out.set(in);

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

            out.mulScale(scale);

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
            out.set(in);
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
            
            return transform(in, out);

        }
    } // class PlaneReflection


    /**
       
     */
    public static class CompositeTransform implements VecTransform, Initializable {
        
        private Vector<VecTransform> vTransforms = new Vector<VecTransform>();
        
        private VecTransform aTransforms[]; // array used in calculations 

        public void add(VecTransform transform){

            vTransforms.add(transform);

        }

        public int initialize(){
            
            int len = vTransforms.size();

            aTransforms = (VecTransform[])vTransforms.toArray(new VecTransform[len]);

            for(int i = 0; i < len; i++){
                VecTransform tr = aTransforms[i];
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

            int len = aTransforms.length;
            if(len < 1){
                // copy input to output                 
                out.set(in);
                return RESULT_OK;                
            }

            //TODO garbage generation 
            Vec vin = new Vec(in);

            for(int i = 0; i < len; i++){

                VecTransform tr = aTransforms[i];
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
            
            int len = aTransforms.length;
            if(len < 1){
                // copy input to output                 
                out.set(in);
                return RESULT_OK;                
            }

            //TODO garbage generation 
            Vec vin = new Vec(in);

            for(int i = aTransforms.length-1; i >= 0; i--){

                VecTransform tr = aTransforms[i];
                int res = tr.inverse_transform(vin, out);
                
                if(res != RESULT_OK)
                    return res;
                vin.set(out);
            }
            
            return RESULT_OK;

        }
    }  // class CompositeTransform


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

        // symmettry group to use 
        Symmetry m_sym;

        public void setFriezeType(int friezeType){
            m_friezeType = friezeType;
        }
        public void setDomainWidth(double width){
            m_domainWidth = width;
        }

        public int initialize(){

            switch(m_friezeType){
            default: 
            case FRIEZE_II:  m_sym = Symmetry.getII(m_domainWidth); break;
            case FRIEZE_S22I:m_sym = Symmetry.getS22I(m_domainWidth); break;
            case FRIEZE_IS:  m_sym = Symmetry.getIS(m_domainWidth); break;
            case FRIEZE_SII: m_sym = Symmetry.getSII(m_domainWidth); break;
            case FRIEZE_2SI: m_sym = Symmetry.get2SI(m_domainWidth); break;
            case FRIEZE_22I: m_sym = Symmetry.get22I(m_domainWidth); break;
            case FRIEZE_IX:  m_sym = Symmetry.getIX(m_domainWidth); break;
            }

            return RESULT_OK;

        }

                
        /**
         *
         */
        public int transform(Vec in, Vec out) {
            out.set(in);
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
            out.set(in);
            // TODO - garbage generation 
            Vector4d vin = new Vector4d(in.v[0],in.v[1],in.v[2],1);

            toFundamentalDomain(vin, m_sym, m_maxCount);
            
            // save result 
            out.v[0] = vin.x;
            out.v[1] = vin.y;
            out.v[2] = vin.z;

            return RESULT_OK;

        }
    } // class FriezeSymmetry


    /**
       makes transformations to reproduce wallpaper symmetry patterns
     */
    public static class WallpaperSymmetry  implements VecTransform, Initializable  {

        public static final int     // orbifold notation
            WP_O  = 0,    // O
            WP_XX = 1,   // xx
            WP_SX = 2,   // *x
            WP_SS = 3,   // **
            WP_632 = 4,   // 632
            WP_S632 = 5,   // *632
            WP_333 = 6,   // 333
            WP_S333 = 7,   // *333
            WP_3S3 = 8,   // 3*3
            WP_442 = 9,   // 442
            WP_S442 = 10,   // *442
            WP_4S2 = 11,   // 4*2
            WP_2222 = 12,   // 2222
            WP_22X = 13,   // 22x
            WP_22S = 14,   // 22*
            WP_S2222 = 15,   // *2222
            WP_2S22 = 16;   // 2*22        

        // maximal number of iterations to get to FD 
        protected int m_maxCount = 100; 
        // width of fundamental domain in meters
        protected double m_domainWidth = 0.01; 
         // height of fundamental domain in meters (if used) 
        protected double m_domainHeight = 0.01;
        protected double m_domainSkew = 0.;

        protected int m_symmetryType; // one of WP_ constants                 
        
        // symmetry to be used 
        protected Symmetry m_sym;

        public void setSymmetryType(int symmetryType){
            m_symmetryType = symmetryType;
        }
        public void setDomainWidth(double width){
            m_domainWidth = width;
        }
        public void setDomainHeight(double height){
            m_domainHeight = height;
        }
        public void setDomainSkew(double skew){
            m_domainSkew = skew;
        }

        public int initialize(){

            switch(m_symmetryType){
            default: 
            case WP_S442:  m_sym = Symmetry.getS442(m_domainWidth); break;
            case WP_442:  m_sym = Symmetry.get442(m_domainWidth); break;
            case WP_S632:  m_sym = Symmetry.getS632(m_domainWidth); break;
            case WP_S333:  m_sym = Symmetry.getS333(m_domainWidth); break;
            case WP_333:   m_sym = Symmetry.getS333(m_domainWidth); break;
            case WP_S2222: m_sym = Symmetry.getS2222(m_domainWidth,m_domainHeight); break;
            case WP_2222:  m_sym = Symmetry.get2222(m_domainWidth,m_domainHeight); break;
            case WP_2S22:  m_sym = Symmetry.get2S22(m_domainWidth,m_domainHeight); break;
            case WP_22S:   m_sym = Symmetry.get22S(m_domainWidth,m_domainHeight); break;
            case WP_SS:    m_sym = Symmetry.getSS(m_domainWidth,m_domainHeight); break;
            case WP_SX:    m_sym = Symmetry.getSX(m_domainWidth,m_domainHeight); break;
            case WP_22X:   m_sym = Symmetry.get22X(m_domainWidth,m_domainHeight); break;
            case WP_XX:    m_sym = Symmetry.getXX(m_domainWidth,m_domainHeight); break;
            case WP_O:    m_sym = Symmetry.getO(m_domainWidth,m_domainHeight, m_domainSkew); break;
            }

            return RESULT_OK;

        }
                
        /**
         *
         */
        public int transform(Vec in, Vec out) {
            out.set(in);
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
            out.set(in);
            // TODO garbage generation 
            Vector4d vin = new Vector4d(in.v[0],in.v[1],in.v[2],1);

            toFundamentalDomain(vin, m_sym, m_maxCount);
            
            // save result 
            out.v[0] = vin.x;
            out.v[1] = vin.y;
            out.v[2] = vin.z;
            
            return RESULT_OK;

        }
    } // class WallpaperSymmetry


    /**
       makes transformations for reflection symetry groups
     */
    public static class ReflectionSymmetry  implements VecTransform, Initializable  {
        

        ReflectionGroup group;
        double riemannSphereRadius;

        public ReflectionSymmetry(){            

        }
        
        public void setGroup(ReflectionGroup group){
            this.group = group;
        }

        public void setRiemannSphereRadius(double value){
            this.riemannSphereRadius = value;
        }
        
        public int initialize(){
            
            if(group == null){               
                // init to simple one generator group 
                group = new ReflectionGroup(new ReflectionGroup.SPlane[]{new ReflectionGroup.Plane(new Vector3d(1,0,0),0)});
            }
                
            group.setRiemannSphereRadius(riemannSphereRadius);
            return RESULT_OK;
        }

        public int transform(Vec in, Vec out) {
            // direct transform is identity transform 
            out.set(in);
            // TODO we use custom combination of reflectins from the group 
            return RESULT_OK;
            
        }
        public int inverse_transform(Vec in, Vec out) {
            out.set(in);
            return group.toFundamentalDomain(out);
            
        }
        
        
    } // class ReflectionSymmetry 

   
    static public final void normalizePlane(Vector4d p){
        double norm = FastMath.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
        p.scale(1./norm);
        
    }

}
