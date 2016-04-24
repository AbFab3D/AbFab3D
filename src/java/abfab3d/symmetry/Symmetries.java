/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
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
import javax.vecmath.Matrix4d;

import abfab3d.param.Parameterizable;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeFactory;
import abfab3d.param.BaseSNodeFactory;

import static abfab3d.symmetry.ETransform.getTranslationMatrix;
import static abfab3d.symmetry.ETransform.getReflectionMatrix;



/**
   a collection of classes which generate specific symmetry groups
   each class implement SymmetryGenerator interface and can be used as parameter in the SymmetryTransform
   each class also impelements Parameterizable interface and can be controled via it's parameters  
 */

public class Symmetries {
    

    static String sm_symNames[] = new String[]{
        "frieze II", 
    };
    static String sm_symClasses[] = new String[]{
        "abfab3d.symmetry.Symmetries$FriezeII",
    };

    static SNodeFactory sm_factory;
    
    public static SNodeFactory getFactory(){
        if(sm_factory == null)
            sm_factory = new BaseSNodeFactory(sm_symNames, sm_symClasses);
        return sm_factory;
    }

    public static abstract class BaseFriezeSymmetry extends BaseParameterizable implements SymmetryGenerator {
        protected DoubleParameter m_domainWidth = new DoubleParameter("width", 1);
        Parameter[] aparam = new Parameter[]{
            m_domainWidth
        };
        
        BaseFriezeSymmetry(){
            super.addParams(aparam);
        }
        
    }


    /**
       frieze group (INF INF) consisting of pure translations
       domain has given width and is centered at origin 
     */
    public static class FriezeII extends BaseFriezeSymmetry {
        public FriezeII(){
        }
        public FriezeII(double domainWidth){
            m_domainWidth.setValue(domainWidth);
        }

        public SymmetryGroup getSymmetryGroup(){
            double width = m_domainWidth.getValue();
            SPlane planes[] = new SPlane[]{
                new EPlane(new Vector3d(-1,0,0),width/2),
                new EPlane(new Vector3d(1,0,0),width/2)
            };
            ETransform trans[] = new ETransform[]{
                new ETransform(ETransform.getTranslationMatrix(-width,0,0)),
                new ETransform(ETransform.getTranslationMatrix(width,0,0)),
            };
            return new SymmetryGroup(planes, trans);
        }
    }// class FriezeII

    /**
       frieze group (INF X) 
       domain has given width and is centered at origin 
     */
    public static class FriezeIX extends BaseFriezeSymmetry {
        public FriezeIX(){
        }
        public FriezeIX(double domainWidth){
            m_domainWidth.setValue(domainWidth);
        }

        public SymmetryGroup getSymmetryGroup(){
            
            double width = m_domainWidth.getValue();

            SPlane[] planes = new SPlane[]{
                new EPlane(new Vector3d(-1,0,0),width/2),
                new EPlane(new Vector3d(1,0,0),width/2)
            };            
                        
            Matrix4d t01 = ETransform.getTranslationMatrix(width, 0,0);
            Matrix4d t10 = ETransform.getTranslationMatrix(-width, 0,0);
            Matrix4d ry = ETransform.getReflectionMatrix(new Vector3d(0,1,0),0);

            Matrix4d pt0 = new Matrix4d();             
            Matrix4d pt1 = new Matrix4d();

            pt0.mul(t10, ry);
            pt1.mul(ry, t01);

            ETransform trans[] = new ETransform[]{
                new ETransform(pt0),
                new ETransform(pt1),
            };
            
            return new SymmetryGroup(planes, trans);
        }
    }// class FriezeIX

    /**
       frieze group (2 2 INF) 
       domain has given width and is centered at origin 
     */
    public static class Frieze22I extends BaseFriezeSymmetry {
        public Frieze22I(){
        }
        public Frieze22I(double domainWidth){
            m_domainWidth.setValue(domainWidth);
        }

        public SymmetryGroup getSymmetryGroup(){
            
            double width = m_domainWidth.getValue();

            SPlane[] planes = new SPlane[]{
                new EPlane(new Vector3d(-1,0,0),width/2), // p0
                new EPlane(new Vector3d(1,0,0),width/2)   // p1
            };            
            // reflection in p0
            Matrix4d r0 = ETransform.getReflectionMatrix(new Vector3d(-1,0,0),width/2);
            // reflection in p1
            Matrix4d r1 = ETransform.getReflectionMatrix(new Vector3d(1,0,0),width/2);
            // reflection in plane y
            Matrix4d ry = ETransform.getReflectionMatrix(new Vector3d(0,1,0),0);
            
            Matrix4d pt0 = new Matrix4d();             
            Matrix4d pt1 = new Matrix4d();

            pt0.mul(ry, r0); // half turn around P0
            pt1.mul(ry, r1); // half turn around P1

            ETransform trans[] = new ETransform[]{
                new ETransform(pt0),
                new ETransform(pt1),
            };
            
            return new SymmetryGroup(planes, trans);
        }
    }// class Frieze22I


    /**
       frieze group (* INF INF) 
     */
    public static class FriezeSII extends BaseFriezeSymmetry {
        public FriezeSII(){
        }
        public FriezeSII(double domainWidth){
            m_domainWidth.setValue(domainWidth);
        }

        public SymmetryGroup getSymmetryGroup(){
            
            double width = m_domainWidth.getValue();

            SPlane[] planes = new SPlane[]{
                new EPlane(new Vector3d(-1,0,0),width/2), // p0
                new EPlane(new Vector3d(1,0,0),width/2)   // p1
            };            
            // reflection in p0
            Matrix4d r0 = ETransform.getReflectionMatrix(new Vector3d(-1,0,0),width/2);
            // reflection in p1
            Matrix4d r1 = ETransform.getReflectionMatrix(new Vector3d(1,0,0),width/2);
            
            ETransform trans[] = new ETransform[]{
                new ETransform(r0),
                new ETransform(r1),
            };
            
            return new SymmetryGroup(planes, trans);
        }
    }// class FriezeSII

    /**
       frieze group (INF *) 
     */
    public static class FriezeIS extends BaseFriezeSymmetry {
        public FriezeIS(){
        }
        public FriezeIS(double domainWidth){
            m_domainWidth.setValue(domainWidth);
        }

        public SymmetryGroup getSymmetryGroup(){
            
            double width = m_domainWidth.getValue();

            SPlane[] planes = new SPlane[]{
                new EPlane(new Vector3d(-1,0,0),width/2), // p0
                new EPlane(new Vector3d(1,0,0),width/2),   // p1
                new EPlane(new Vector3d(0,-1,0),0),        // p2
            };            
            // reflection in p2
            Matrix4d r2 = ETransform.getReflectionMatrix(new Vector3d(0,1,0),0);
            // translation p0 p1 
            Matrix4d t01 = ETransform.getTranslationMatrix(width, 0,0);
            Matrix4d t10 = ETransform.getTranslationMatrix(-width, 0,0);

            
            ETransform trans[] = new ETransform[]{
                new ETransform(t10),
                new ETransform(t01),
                new ETransform(r2),
            };
            
            return new SymmetryGroup(planes, trans);
        }
    }// class FriezeIS

    /**
       frieze group (2*INF) 
     */
    public static class Frieze2SI extends BaseFriezeSymmetry {
        public Frieze2SI(){
        }
        public Frieze2SI(double domainWidth){
            m_domainWidth.setValue(domainWidth);
        }

        public SymmetryGroup getSymmetryGroup(){
            
            double width = m_domainWidth.getValue();

            SPlane[] planes = new SPlane[]{
                new EPlane(new Vector3d(-1,0,0),width/2), // p0
                new EPlane(new Vector3d(1,0,0),width/2),   // p1
                new EPlane(new Vector3d(0,-1,0),0),        // p2
            };            
            // reflection in p0
            Matrix4d r0 = ETransform.getReflectionMatrix(new Vector3d(-1,0,0),width/2);
            // reflection in p1
            Matrix4d r1 = ETransform.getReflectionMatrix(new Vector3d(1,0,0),width/2);
            // half turn in origin
            Matrix4d rx = ETransform.getReflectionMatrix(new Vector3d(1,0,0),0);
            Matrix4d ry = ETransform.getReflectionMatrix(new Vector3d(0,1,0),0);
            Matrix4d ht = new Matrix4d();
            ht.mul(rx, ry);
            
            ETransform trans[] = new ETransform[]{
                new ETransform(r0),
                new ETransform(r1),
                new ETransform(ht),
            };
            
            return new SymmetryGroup(planes, trans);
        }
    }// class Frieze2SI


    /**
       frieze group (S22I) 
     */
    public static class FriezeS22I extends BaseFriezeSymmetry {
        public FriezeS22I(){
        }
        public FriezeS22I(double domainWidth){
            m_domainWidth.setValue(domainWidth);
        }

        public SymmetryGroup getSymmetryGroup(){
            
            double width = m_domainWidth.getValue();

            SPlane[] planes = new SPlane[]{
                new EPlane(new Vector3d(-1,0,0),width/2), // p0
                new EPlane(new Vector3d(1,0,0),width/2),   // p1
                new EPlane(new Vector3d(0,-1,0),0),        // p2
            };            
            // reflection in p0
            Matrix4d r0 = ETransform.getReflectionMatrix(new Vector3d(-1,0,0),width/2);
            // reflection in p1
            Matrix4d r1 = ETransform.getReflectionMatrix(new Vector3d(1,0,0),width/2);
            // reflection in p2
            Matrix4d r2 = ETransform.getReflectionMatrix(new Vector3d(0,1,0),0);
            
            ETransform trans[] = new ETransform[]{
                new ETransform(r0),
                new ETransform(r1),
                new ETransform(r2),
            };
            
            return new SymmetryGroup(planes, trans);
        }
    }// class FriezeIS


    public static abstract class BaseWallpaperSymmetry extends BaseParameterizable implements SymmetryGenerator {
        protected DoubleParameter m_width = new DoubleParameter("width", 1);
        protected DoubleParameter m_height = new DoubleParameter("height", 1);
        protected DoubleParameter m_skew = new DoubleParameter("skew", 0.);

        Parameter[] aparam = new Parameter[]{
            m_width,
            m_height,
            m_skew,
        };
        
        BaseWallpaperSymmetry(){
            super.addParams(aparam);
        }
        
    }

     public static class WallpaperO extends BaseWallpaperSymmetry{
        public WallpaperO(){
        }
         public WallpaperO(double width,double height,double skew){
            m_width.setValue(width);
            m_height.setValue(height);
            m_skew.setValue(skew);
        }

        public SymmetryGroup getSymmetryGroup(){
            if(true) throw new RuntimeException("WallpaperO not implemented");
            double w = m_width.getValue();
            double h = m_height.getValue();
            double s = m_skew.getValue();
            double sx = w*s;

            double norm = Math.sqrt(sx*sx + h*h);
            double nx = h/norm;
            double ny = sx/norm;
            int sign = (sx >= 0.)? (1):(-1);
                        
            SPlane[] planes = new SPlane[]{
                new EPlane(0,-1,0,h/2),
                new EPlane(nx,-ny, 0,(nx*w - sign*ny*h)/2),
                new EPlane(0,1,0, h/2),
                new EPlane(-nx,ny, 0,(nx*w - sign*ny*h)/2)
            };
            
            ETransform trans[] = new ETransform[]{
                new ETransform(getTranslationMatrix(-sx, -h, 0)),
                new ETransform(getTranslationMatrix(w-sign*sx, 0, 0)),
                new ETransform(getTranslationMatrix(sx, h, 0)),
                new ETransform(getTranslationMatrix(-(w-sign*sx), 0, 0))};
            
            return new SymmetryGroup(planes, trans);
        }
    }// class WallpaperO 
   
}