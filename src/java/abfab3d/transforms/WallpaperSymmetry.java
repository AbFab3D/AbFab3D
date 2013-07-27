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

package abfab3d.transforms;

import java.util.Vector;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.AxisAngle4d;

import abfab3d.util.Vec;
import abfab3d.util.Initializable;
import abfab3d.util.Symmetry;
import abfab3d.util.ReflectionGroup;
import abfab3d.util.VecTransform;

import net.jafama.FastMath;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Symmetry.getReflection;
import static abfab3d.util.Symmetry.toFundamentalDomain;


/**
   makes transformations to reproduce wallpaper symmetry patterns
*/
public class WallpaperSymmetry  implements VecTransform, Initializable  {
    
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

    public WallpaperSymmetry() {

    }

    public WallpaperSymmetry(int symmetryType){
        setSymmetryType(symmetryType);
    }
    public WallpaperSymmetry(int symmetryType, double width, double height){
        setSymmetryType(symmetryType);
        setDomainWidth(width);
        setDomainHeight(height);
    }

    public WallpaperSymmetry(String symmetryName, double width, double height){
        setSymmetryType(getSymmetryType(symmetryName));
        setDomainWidth(width);
        setDomainHeight(height);
    }

    public void setSymmetryType(int symmetryType){
        m_symmetryType = symmetryType;
    }
    public void setDomainWidth(double width){
        m_domainWidth = width;
    }
    public void setDomainHeight(double height){
        m_domainHeight = height;
    }
    public void setMaxCount(int maxCount){

        m_maxCount = maxCount;

    }
    public void setDomainSkew(double skew){
        m_domainSkew = skew;
    }

    public static int getSymmetryType(String symmetryName){
        //TODO 
        return 0;
    }
    
    public int initialize(){
        
        switch(m_symmetryType){
        default: 
        case WP_S442:  m_sym = Symmetry.getS442(m_domainWidth); break;
        case WP_442:  m_sym = Symmetry.get442(m_domainWidth); break;
        case WP_S632:  m_sym = Symmetry.getS632(m_domainWidth); break;
        case WP_S333:  m_sym = Symmetry.getS333(m_domainWidth); break;

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

        // missing groups 
        //WP_632 = 4,   // 632
        //WP_3S3 = 8,   // 3*3
        //WP_4S2 = 11,   // 4*2
        //WP_333

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
     *  
     */
    public int inverse_transform(Vec in, Vec out) {
        out.set(in);
        // TODO garbage generation 
        Vector4d vin = new Vector4d(in.v[0],in.v[1],in.v[2],1);
        
        int res = toFundamentalDomain(vin, m_sym, m_maxCount);
        
        // save result 
        out.v[0] = vin.x;
        out.v[1] = vin.y;
        out.v[2] = vin.z;
        
        return res;
        
    }
} // class WallpaperSymmetry

