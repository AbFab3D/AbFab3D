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

import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;

import static java.lang.Math.sqrt;
import static abfab3d.core.Output.printf;
import static abfab3d.symmetry.ETransform.getReflectionMatrix;
import static abfab3d.symmetry.ETransform.getTranslationMatrix;

/**
   contais code to create wallpaper symmetries

   @author Vladimir Bulatov
 */
public class WallpaperSymmetries {

    static final boolean DEBUG = false;
    static final double 
        SQRT2 = sqrt(2),
        SQRT3 = sqrt(3);
       
    /**
       wallpaper group *442
       
     */
    public static SymmetryGroup getS442(double domainWidth){
        
        Vector4d planes[] = new Vector4d[3]; 
        planes[0] = new Vector4d(0,-1,0,0);
        planes[1] = new Vector4d(1/SQRT2,1/SQRT2,0,-domainWidth/SQRT2);
        planes[2] = new Vector4d(-1,0,0,0);

        Matrix4d trans[] = new Matrix4d[3];
        
        for(int i=0; i < 3; i++){
            trans[i] = getReflectionMatrix(planes[i]);
        }
        
        return new SymmetryGroup(planes, trans);
        
    }

    
    /**
       wallpaper group 442
       
     */
    public static SymmetryGroup get442(double domainWidth){
        

        Vector4d planes[] = new Vector4d[3]; 
        
        planes[0] = new Vector4d(1./SQRT2,-1./SQRT2, 0., -domainWidth/SQRT2);
        planes[1] = new Vector4d(1./SQRT2,1./SQRT2, 0., -domainWidth/SQRT2);
        planes[2] = new Vector4d(-1, 0,0, 0);

        Matrix4d trans[] = new Matrix4d[3];
        
        Vector4d py = new Vector4d(0,1,0,0);
        for(int i = 0; i < 3; i++){
            trans[i] = new Matrix4d();
            trans[i].mul(getReflectionMatrix(planes[i]), getReflectionMatrix(py));
        }
        
        return new SymmetryGroup(planes, trans);
        
    }

    /**
       wallpaper group 4S2
       
     */
    public static SymmetryGroup get4S2(double domainWidth){
        

        Vector4d planes[] = new Vector4d[3]; 
        
        planes[0] = new Vector4d(1./SQRT2,-1./SQRT2, 0., -domainWidth/SQRT2);
        planes[1] = new Vector4d(1./SQRT2,1./SQRT2, 0., -domainWidth/SQRT2);
        planes[2] = new Vector4d(-1, 0,0, 0);

        Matrix4d trans[] = new Matrix4d[3];
        
        Vector4d py = new Vector4d(0,1,0,0);
        for(int i = 0; i < 2; i++){
            trans[i] = new Matrix4d();
            trans[i].mul(getReflectionMatrix(planes[i]), getReflectionMatrix(py));
        }
        trans[2] = getReflectionMatrix(planes[2]);
        
        return new SymmetryGroup(planes, trans);
        
    }

    /**
       wallpaper group 3S3
       
     */
    public static SymmetryGroup get3S3(double domainWidth){
        

        Vector4d planes[] = new Vector4d[3]; 
        
        planes[0] = new Vector4d(SQRT3/2,-0.5,0., -domainWidth*(SQRT3/2.));
        planes[1] = new Vector4d(SQRT3/2, 0.5,0., -domainWidth*(SQRT3/2.));
        planes[2] = new Vector4d(-1,0,0,0);

        Matrix4d trans[] = new Matrix4d[3];
        
        Vector4d py = new Vector4d(0,1,0,0);
        for(int i = 0; i < 2; i++){
            trans[i] = new Matrix4d();
            trans[i].mul(getReflectionMatrix(planes[i]), getReflectionMatrix(py));
        }
        trans[2] = getReflectionMatrix(planes[2]);
        
        return new SymmetryGroup(planes, trans);
        
    }

    /**
       wallpaper group *632
       
     */
    public static SymmetryGroup getS632(double domainWidth){
        
        Vector4d planes[] = new Vector4d[3]; 
        planes[0] = new Vector4d(0,-1,0,0);
        planes[1] = new Vector4d(SQRT3/2,0.5,0., -domainWidth*(SQRT3/2.));
        planes[2] = new Vector4d(-1,0,0,0);

        Matrix4d trans[] = new Matrix4d[3];
        
        for(int i=0; i < 3; i++){
            trans[i] = getReflectionMatrix(planes[i]);
        }
        
        return new SymmetryGroup(planes, trans);
        
    }

    /**
       wallpaper group *333
       
     */
    public static SymmetryGroup getS333(double domainWidth){
        
        Vector4d planes[] = new Vector4d[3]; 
        planes[0] = new Vector4d(0, -1, 0, 0);
        planes[1] = new Vector4d(SQRT3/2,0.5,0., -domainWidth/2*(SQRT3/2.));
        planes[2] = new Vector4d(-SQRT3/2,0.5,0., -domainWidth/2*(SQRT3/2.));

        Matrix4d trans[] = new Matrix4d[3];
        
        for(int i=0; i < 3; i++){
            trans[i] = getReflectionMatrix(planes[i]);
        }
        
        return new SymmetryGroup(planes, trans);
        
    }

    /**
       wallpaper group 333
       
     */
    public static SymmetryGroup get333(double domainWidth){
        
        Vector4d planes[] = new Vector4d[4]; 
        
        planes[0] = new Vector4d(SQRT3/2,-0.5,0., -domainWidth/2*(SQRT3/2.));
        planes[1] = new Vector4d(SQRT3/2, 0.5,0., -domainWidth/2*(SQRT3/2.));
        planes[2] = new Vector4d(-SQRT3/2,0.5,0., -domainWidth/2*(SQRT3/2.));
        planes[3] = new Vector4d(-SQRT3/2,-0.5,0., -domainWidth/2*(SQRT3/2.));

        Matrix4d trans[] = new Matrix4d[4];
        
        Vector4d py = new Vector4d(0,1,0,0);
        for(int i = 0; i < 4; i++){
            trans[i] = new Matrix4d();
            trans[i].mul(getReflectionMatrix(planes[i]), getReflectionMatrix(py));
        }
        
        return new SymmetryGroup(planes, trans);
        
    }


    /**
       wallpaper group 632
       
     */
    public static SymmetryGroup get632(double domainWidth){
        
        Vector4d planes[] = new Vector4d[3]; 
        
        planes[0] = new Vector4d(SQRT3/2,-0.5,0., -domainWidth*(SQRT3/2.));
        planes[1] = new Vector4d(SQRT3/2, 0.5,0., -domainWidth*(SQRT3/2.));
        planes[2] = new Vector4d(-1, 0,0, 0);

        Matrix4d trans[] = new Matrix4d[3];
        
        Vector4d py = new Vector4d(0,1,0,0);
        for(int i = 0; i < 3; i++){
            trans[i] = new Matrix4d();
            trans[i].mul(getReflectionMatrix(planes[i]), getReflectionMatrix(py));
        }
        
        return new SymmetryGroup(planes, trans);
        
    }


    
    /**
       wallpaper group *2222
       
     */
    public static SymmetryGroup getS2222(double domainWidth, double domainHeight){
        
        Vector4d planes[] = new Vector4d[4]; 
        planes[0] = new Vector4d(1,0,0,-domainWidth);
        planes[1] = new Vector4d(0,1,0,-domainHeight);
        planes[2] = new Vector4d(-1,0,0,0);
        planes[3] = new Vector4d(0,-1,0,0);

        Matrix4d trans[] = new Matrix4d[4];
        
        for(int i=0; i < 4; i++){
            trans[i] = getReflectionMatrix(planes[i]);
        }
        
        return new SymmetryGroup(planes, trans);
        
    }

    /**
       wallpaper group 2222
       
     */
    public static SymmetryGroup get2222(double domainWidth, double domainHeight){
        
        Vector4d p[] = new Vector4d[4]; 
        p[0] = new Vector4d(0,-1,0,-domainHeight/2);
        p[1] = new Vector4d(1, 0,0,-domainWidth);
        p[2] = new Vector4d(0, 1,0,-domainHeight/2);
        p[3] = new Vector4d(-1,0,0,0);
        
        Vector4d p01 = new Vector4d(0,1,0,0);

        Matrix4d trans[] = new Matrix4d[4];
        
        for(int i=0; i < 4; i++){
            trans[i] = new Matrix4d();
            trans[i].mul(getReflectionMatrix(p[i]),getReflectionMatrix(p01));
        }
        
        return new SymmetryGroup(p, trans);
        
    }

    /**
       wallpaper group 2*22
       
     */
    public static SymmetryGroup get2S22(double domainWidth, double domainHeight){
        
        Vector4d p[] = new Vector4d[4]; 
        p[0] = new Vector4d(0,-1,0,-domainHeight/2);
        p[1] = new Vector4d(1,0,0,-domainWidth);
        p[2] = new Vector4d(0,1,0,-domainHeight/2);
        p[3] = new Vector4d(-1,0,0,0);
        
        Vector4d p01 = new Vector4d(0,1,0,0);

        Matrix4d trans[] = new Matrix4d[4];
        
        trans[0] = getReflectionMatrix(p[0]);
        trans[1] = getReflectionMatrix(p[1]);
        trans[2] = getReflectionMatrix(p[2]);
        trans[3] = new Matrix4d();
        trans[3].mul(getReflectionMatrix(p[3]),getReflectionMatrix(p01));
        
        return new SymmetryGroup(p, trans);
        
    }

    /**
       wallpaper group 22*
       
     */
    public static SymmetryGroup get22S(double domainWidth, double domainHeight){
        
        Vector4d p[] = new Vector4d[4]; 
        p[0] = new Vector4d(0,-1,0,-domainHeight/2);
        p[1] = new Vector4d(1,0,0, -domainWidth);
        p[2] = new Vector4d(0,1,0, -domainHeight/2);
        p[3] = new Vector4d(-1,0,0,0);
        
        Vector4d p01 = new Vector4d(0,1,0,0);

        Matrix4d trans[] = new Matrix4d[4];
        
        trans[0] = getReflectionMatrix(p[0]);

        trans[1] = new Matrix4d();
        trans[1].mul(getReflectionMatrix(p[1]),getReflectionMatrix(p01));

        trans[2] = getReflectionMatrix(p[2]);

        trans[3] = new Matrix4d();
        trans[3].mul(getReflectionMatrix(p[3]),getReflectionMatrix(p01));
        
        return new SymmetryGroup(p, trans);
        
    }

    /**
       wallpaper group **
       
     */
    public static SymmetryGroup getSS(double domainWidth, double domainHeight){
        
        Vector4d p[] = new Vector4d[4]; 
        p[0] = new Vector4d(0,-1,0,-domainHeight/2);
        p[1] = new Vector4d(1,0,0, -domainWidth);
        p[2] = new Vector4d(0,1,0, -domainHeight/2);
        p[3] = new Vector4d(-1,0,0,0);
        
        Vector4d p01 = new Vector4d(0,1,0,0);

        Matrix4d trans[] = new Matrix4d[4];
        

        trans[0] = new Matrix4d();
        trans[0].mul(getReflectionMatrix(p[0]),getReflectionMatrix(p01));

        trans[1] = getReflectionMatrix(p[1]);

        trans[2] = new Matrix4d();
        trans[2].mul(getReflectionMatrix(p[2]),getReflectionMatrix(p01));

        trans[3] = getReflectionMatrix(p[3]);
        
        return new SymmetryGroup(p, trans);
        
    }
 
    /**
       wallpaper group *X
       
     */
    public static SymmetryGroup getSX(double domainWidth, double domainHeight){
        
        Vector4d p[] = new Vector4d[4]; 
        p[0] = new Vector4d(0,-1,0,-domainHeight/2);
        p[1] = new Vector4d(1,0,0, -domainWidth/2);
        p[2] = new Vector4d(0,1,0, -domainHeight/2);
        p[3] = new Vector4d(-1,0,0,-domainWidth/2);
        
        Vector4d py = new Vector4d(0,1,0,0);
        Vector4d px = new Vector4d(1,0,0,0);
        
        Matrix4d trans[] = new Matrix4d[4];
        
        
        trans[0] = getReflectionMatrix(p[0]);
        
        trans[2] = getReflectionMatrix(p[2]);
        
        trans[1] = new Matrix4d();
        trans[1].mul(getReflectionMatrix(p[1]),getReflectionMatrix(py));
        trans[1].mul(getReflectionMatrix(px));
        
        trans[2] = getReflectionMatrix(p[2]);
        
        trans[3] = new Matrix4d();
        trans[3].mul(getReflectionMatrix(p[3]),getReflectionMatrix(py));
        trans[3].mul(getReflectionMatrix(px));
        
        return new SymmetryGroup(p, trans);
        
    }

    /**
       wallpaper group 22X
       
     */
    public static SymmetryGroup get22X(double domainWidth, double domainHeight){
        
        Vector4d p[] = new Vector4d[4]; 
        p[0] = new Vector4d(0,-1,0,-domainHeight/2);
        p[1] = new Vector4d(1,0,0, -domainWidth/2);
        p[2] = new Vector4d(0,1,0, -domainHeight/2);
        p[3] = new Vector4d(-1,0,0,-domainWidth/2);
        
        Vector4d py = new Vector4d(0,1,0,0);
        Vector4d px = new Vector4d(1,0,0,0);

        Matrix4d trans[] = new Matrix4d[4];
        

        trans[0] = new Matrix4d();
        trans[0].mul(getReflectionMatrix(p[0]),getReflectionMatrix(px));
        trans[0].mul(getReflectionMatrix(py));

        trans[1] = new Matrix4d();
        trans[1].mul(getReflectionMatrix(p[1]),getReflectionMatrix(py));
        trans[1].mul(getReflectionMatrix(px));

        trans[2] = new Matrix4d();
        trans[2].mul(getReflectionMatrix(p[2]),getReflectionMatrix(px));
        trans[2].mul(getReflectionMatrix(py));

        trans[3] = new Matrix4d();
        trans[3].mul(getReflectionMatrix(p[3]),getReflectionMatrix(py));
        trans[3].mul(getReflectionMatrix(px));
        
        return new SymmetryGroup(p, trans);
        
    }

    /**
       wallpaper group XX
       
     */
    public static SymmetryGroup getXX(double domainWidth, double domainHeight){
        
        Vector4d p[] = new Vector4d[4]; 
        p[0] = new Vector4d(0,-1,0,-domainHeight/2);
        p[1] = new Vector4d(1,0,0, -domainWidth/2);
        p[2] = new Vector4d(0,1,0, -domainHeight/2);
        p[3] = new Vector4d(-1,0,0,-domainWidth/2);
        
        Vector4d py = new Vector4d(0,1,0,0);
        Vector4d px = new Vector4d(1,0,0,0);

        Matrix4d trans[] = new Matrix4d[4];
        

        trans[0] = new Matrix4d();
        trans[0].mul(getReflectionMatrix(p[0]),getReflectionMatrix(py));

        trans[1] = new Matrix4d();
        trans[1].mul(getReflectionMatrix(p[1]),getReflectionMatrix(py));
        trans[1].mul(getReflectionMatrix(px));

        trans[2] = new Matrix4d();
        trans[2].mul(getReflectionMatrix(p[2]),getReflectionMatrix(py));

        trans[3] = new Matrix4d();
        trans[3].mul(getReflectionMatrix(p[3]),getReflectionMatrix(py));
        trans[3].mul(getReflectionMatrix(px));
        
        return new SymmetryGroup(p, trans);
        
    }

    /**
       wallpaper group o
       
     */
    public static SymmetryGroup getO(double domainWidth, double domainHeight, double skew){
       

        double w = domainWidth;
        double h = domainHeight;
        double sx = domainWidth*skew; 
        double norm = sqrt(sx*sx + h*h);
        double nx = h/norm;
        double ny = sx/norm;
        int sign = (sx >= 0.)? (1):(-1);

        Vector4d p[] = new Vector4d[4]; 
               
        p[0] = new Vector4d(0,-1,0,-h/2);
        p[1] = new Vector4d(nx,-ny, 0,-(nx*w - sign*ny*h)/2);
        p[2] = new Vector4d(0,1,0, -h/2);
        p[3] = new Vector4d(-nx,ny, 0,-(nx*w - sign*ny*h)/2);

        Matrix4d trans[] = new Matrix4d[4];        

        trans[0] = getTranslationMatrix(-sx, -h, 0);
        trans[1] = getTranslationMatrix(w-sign*sx, 0, 0);
        trans[2] = getTranslationMatrix(sx, h, 0);
        trans[3] = getTranslationMatrix(-(w-sign*sx), 0, 0);
        
        return new SymmetryGroup(p, trans);
        
    }  
}
