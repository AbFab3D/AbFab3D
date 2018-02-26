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

import static abfab3d.symmetry.ETransform.getReflectionMatrix;
import static abfab3d.core.Output.printf;

/**
   contains code for 7 frieze groups

   group is described as fundamental domain and pairing transform for each side of fundamental domain 
   fundamental domain is set of planes represented as Vector4d (nx,ny,nz, distanceToOrigin);
   pairing transform is Matrix4d describing Eucliean motion of tile adjacent to fundamental domain along given side back 
   into fundamental domain 
   into adjacent tile 
   
   @author Vladimir Bulatov
 */
public class FriezeSymmetries {

    static final boolean DEBUG = false;
    
    public static SymmetryGroup getII(double domainWidth){
        
        Vector4d[] planes = new Vector4d[2]; 
        planes[0] = new Vector4d(-1,0,0,-domainWidth/2);
        planes[1] = new Vector4d(1,0,0,-domainWidth/2);
        Vector4d p2 = new Vector4d(1,0,0,0);
        
        Matrix4d m0 = getReflectionMatrix(planes[0]);
        Matrix4d m1 = getReflectionMatrix(p2);
        
        Matrix4d[] trans = new Matrix4d[2];

        trans[0] = new Matrix4d(); 
        trans[1] = new Matrix4d(); 
        
        trans[0].mul(m0,m1);
        trans[1].mul(m1,m0);
        return new SymmetryGroup(planes, trans);
    }
    

    public static SymmetryGroup getIX(double domainWidth){
        
        Vector4d[] planes = new Vector4d[2]; 
        planes[0] = new Vector4d(-1,0,0,-domainWidth/2);
        planes[1] = new Vector4d(1,0,0,-domainWidth/2);
        Vector4d p2 = new Vector4d(1,0,0,0);
        Vector4d p3 = new Vector4d(0,1,0,0);
        
        Matrix4d m0 = getReflectionMatrix(planes[0]);
        Matrix4d m1 = getReflectionMatrix(p2);
        Matrix4d t = new Matrix4d(); 
        t.mul(m0, m1);
        t.mul(getReflectionMatrix(p3));
        
        Matrix4d[] trans = new Matrix4d[2];
        
        trans[0] = t;
        trans[1] = new Matrix4d(); 
        trans[1].invert(t);

        return new SymmetryGroup(planes, trans);

    }
    
    public static SymmetryGroup get22I(double domainWidth){
        
        Vector4d[] planes = new Vector4d[3]; 
        planes[0] = new Vector4d(-1,0,0,-domainWidth/2);
        planes[1] = new Vector4d(1,0,0,-domainWidth/2);
        planes[2] = new Vector4d(0,-1,0,0);
        
        Vector4d p2 = new Vector4d(1,0,0,0);
        
        Matrix4d[] trans = new Matrix4d[3];
        
        trans[0] = new Matrix4d(); 
        trans[1] = new Matrix4d(); 
        trans[2] = new Matrix4d(); 
        
        trans[0].mul(getReflectionMatrix(planes[0]),getReflectionMatrix(p2));
        trans[1].mul(getReflectionMatrix(planes[1]),getReflectionMatrix(p2));
        trans[2].mul(getReflectionMatrix(planes[2]),getReflectionMatrix(p2)); // half turn 

        return new SymmetryGroup(planes, trans);
    }
    
    public static SymmetryGroup getSII(double domainWidth){
        
        Vector4d planes[] = new Vector4d[2]; 
        planes[0] = new Vector4d(-1,0,0,-domainWidth/2);
        planes[1] = new Vector4d(1,0,0,-domainWidth/2);
        
        Matrix4d[] trans = new Matrix4d[2];
        
        trans[0] = getReflectionMatrix(planes[0]);
        trans[1] = getReflectionMatrix(planes[1]);
        
        return new SymmetryGroup(planes, trans);

    }

    public static SymmetryGroup getIS(double domainWidth){
        
        Vector4d planes[] = new Vector4d[3]; 
        planes[0] = new Vector4d(-1,0,0,-domainWidth/2);
        planes[1] = new Vector4d(1,0,0,-domainWidth/2);
        planes[2] = new Vector4d(0,-1,0,0);
        
        Vector4d p2 = new Vector4d(1,0,0,0);
        
        Matrix4d m0 = getReflectionMatrix(planes[0]);
        Matrix4d m1 = getReflectionMatrix(p2);
        
        Matrix4d[] trans = new Matrix4d[3];
        
        trans[0] = new Matrix4d(); 
        trans[1] = new Matrix4d(); 
        
        trans[0].mul(m0,m1);
        trans[1].mul(m1,m0);
        trans[2] = getReflectionMatrix(planes[2]);
        return new SymmetryGroup(planes, trans);
        
    }

    public static SymmetryGroup getS22I(double domainWidth){
        
        Vector4d planes[] = new Vector4d[3]; 
        planes[0] = new Vector4d(-1,0,0,-domainWidth/2);
        planes[1] = new Vector4d(1,0,0,-domainWidth/2);
        planes[2] = new Vector4d(0,-1,0,0);
        
        Matrix4d[] trans = new Matrix4d[3];
        
        trans[0] = getReflectionMatrix(planes[0]);
        trans[1] = getReflectionMatrix(planes[1]);
        trans[2] = getReflectionMatrix(planes[2]);
        
        return new SymmetryGroup(planes, trans);
    }
    
    public static SymmetryGroup get2SI(double domainWidth){
        
        Vector4d planes[] = new Vector4d[3];
        
        planes[0] = new Vector4d(-1,0,0,-domainWidth/2);
        planes[1] = new Vector4d(1,0,0,-domainWidth/2);
        planes[2] = new Vector4d(0,-1,0,0);
        
        Matrix4d trans[] = new Matrix4d[3];
        
        trans[0] = getReflectionMatrix(planes[0]);
        trans[1] = getReflectionMatrix(planes[1]);
        
        trans[2] = new Matrix4d();
        trans[2].mul(getReflectionMatrix(planes[2]), getReflectionMatrix(new Vector4d(1,0,0,0))); // half turn 
        
        return new SymmetryGroup(planes, trans);

    }    
}
