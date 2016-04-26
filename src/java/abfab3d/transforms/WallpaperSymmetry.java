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

import abfab3d.param.DoubleParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.Parameter;


import abfab3d.symmetry.SymmetryGroup;
import abfab3d.symmetry.WallpaperSymmetries;
import abfab3d.util.VecTransform;

import net.jafama.FastMath;

import static abfab3d.util.Output.printf;


/**
   <p>
   Makes transformations to reproduce 17 two dimensional wallpaper symmetry patterns. 
   See <a href="http://en.wikipedia.org/wiki/Wallpaper_group"> Wallpaper Group</a>. 
   Traditional wallpaper patterns are two dimensional. However these tranformations 
   are acting in three dimension. 
   </p>
   <p>
   The diagram below shows the shapes of fundamental domain used for each of 17 groups. 
   Fndamental domain is dark gray shape, which fills the whole plane using symmetry operations marked along the 
   boundary of fundamental domain
   </p>

   <p>
   <ul>
   the symmetry operations are the following.
   <li>Bold lines are mirror (or reflection) lines</li>
   <li>dotted lines - glide reflection</li>

   <li>Polygons reperesent rotation axes
      <ul> 
      <li> rhombus 2-fold rotation</li>
      <li> triangle 3-fold rotation</li>
      <li> square 4-fold rotation</li>
      <li> hexagon 6-fold rotation</li>
      </ul>
    </li>
   </ul>
   </p>
   <p>
   The WallpaperSymmetry fills the whole plane with copies of fundamental domain using transformation shown in the diagram.
   </p>
   <p>
   The notations used for wallpaper groups are orbifold notations. 
   </p>

   <embed src="doc-files/wallpaper_groups.svg" type="image/svg+xml"/> 
*/
public class WallpaperSymmetry  extends BaseTransform implements Initializable {
    


    protected DoubleParameter  mp_domainWidth = new DoubleParameter("domainWidth","width of fundamental domain",0.01);
    protected DoubleParameter  mp_domainHeight = new DoubleParameter("domainHeight","height of fundamental domain",0.01);
    protected DoubleParameter  mp_domainSkew = new DoubleParameter("domainSkew","skew of fundamental domain for symmetry O",0.0);
    protected IntParameter  mp_maxCount = new IntParameter("maxCount","max count of iteratioins to get to fundamental domain",100);
    protected EnumParameter  mp_symmetryType = new EnumParameter("symmetryType","type of walpaper symetry",SymmetryNames, SymmetryNames[WP_S2222]);

    Parameter aparam[] = new Parameter[]{
        mp_domainWidth,
        mp_domainHeight,
        mp_domainSkew,
        mp_maxCount, 
        mp_symmetryType,       
    };

    static final public String SymmetryNames[] = new String[]{
        "O",    //WM_O  = 0,    // O
        "XX",   //WP_XX = 1,   // xx
        "SX",   // WP_SX = 2,   // *x
        "SS",   //WP_SS = 3,   // **
        "632",  //WP_632 = 4,   // 632
        "S632", //WP_S632 = 5,   // *632
        "333",  //WP_333 = 6,   // 333
        "S333", //WP_S333 = 7,   // *333
        "3S3",  //WP_3S3 = 8,   // 3*3
        "442",  //WP_442 = 9,   // 442
        "S442", //WP_S442 = 10,   // *442
        "4S2",  //WP_4S2 = 11,   // 4*2
        "2222", //WP_2222 = 12,   // 2222
        "22x",  //WP_22X = 13,   // 22x
        "22S",  //WP_22S = 14,   // 22*
        "S2222",//WP_S2222 = 15,   // *2222
        "2S22", //WP_2S22 = 16;   // 2*22                
    };

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
    //protected int m_maxCount = 100; 
    // width of fundamental domain in meters
    //protected double m_domainWidth = 0.01; 
    // height of fundamental domain in meters (if used) 
    //protected double m_domainHeight = 0.01;
    //protected double m_domainSkew = 0.;    
    //protected int m_symmetryType; // one of WP_ constants                     
    // symmetry to be used 
    protected SymmetryGroup m_group;

    /**
       default constructor with default symmetry type WallpaperSymmetry.WP_S2222;
     */
    public WallpaperSymmetry(){
        setSymmetryType(WP_S2222);
    }

    
    /**
       constructor with given symmetry type
       @param symmetryType possible values are 
       <ul>
       <li>WallpaperSymmetry.WP_O</li>
       <li>WallpaperSymmetry.WP_XX</li>
       <li>WallpaperSymmetry.WP_SX</li>
       <li>WallpaperSymmetry.WP_SS</li>
       <li>WallpaperSymmetry.WP_632</li>
       <li>WallpaperSymmetry.WP_S632</li>
       <li>WallpaperSymmetry.WP_333</li>
       <li>WallpaperSymmetry.WP_S333</li>
       <li>WallpaperSymmetry.WP_3S3</li>
       <li>WallpaperSymmetry.WP_442</li>
       <li>WallpaperSymmetry.WP_S442</li>
       <li>WallpaperSymmetry.WP_4S2</li>
       <li>WallpaperSymmetry.WP_2222</li>
       <li>WallpaperSymmetry.WP_22X</li>
       <li>WallpaperSymmetry.WP_22S</li>
       <li>WallpaperSymmetry.WP_S2222</li>
       <li>WallpaperSymmetry.WP_2S22</li>
       </ul>
     */
    public WallpaperSymmetry(int symmetryType){
        super.addParams(aparam);
        setSymmetryType(symmetryType);
    }

    
    /**
       constructor with given symmetry type and size of fundamental domain 
       @param symmetryType possible values see above
       @param width width of fundamental domain 
     */
    public WallpaperSymmetry(int symmetryType, double width){
        super.addParams(aparam);
        setSymmetryType(symmetryType);
        setDomainWidth(width);
    }

    /**
       constructor with given symmetry type and size of fundamental domain 
       @param symmetryType possible values see above
       @param width width of fundamental domain 
       @param height height of fundamental domain 
     */
    public WallpaperSymmetry(int symmetryType, double width, double height){
        super.addParams(aparam);
        setSymmetryType(symmetryType);
        setDomainWidth(width);
        setDomainHeight(height);
    }

    /**
       constructor with given symmetry type and size of fundamental domain 
       @param symmetryType possible values see above
       @param width width of fundamental domain 
       @param height height of fundamental domain 
       @param skew in relative units (used only for symmetry type WP_O)
     */
    public WallpaperSymmetry(int symmetryType, double width, double height, double skew){
        super.addParams(aparam);
        setSymmetryType(symmetryType);
        setDomainWidth(width);
        setDomainHeight(height);
        setDomainSkew(skew);

    }


    /**
       @noRefGuide
     */
    public void setSymmetryType(int symmetryType){
        mp_symmetryType.setSelectedIndex(symmetryType);
    }

    public void setSymmetryType(String symmetryTypeName){
        mp_symmetryType.setValue(symmetryTypeName);
    }

    /**
       @param width width of fundamental domain. 
     */
    public void setDomainWidth(double width){
        mp_domainWidth.setValue(width);
    }

    /**
       @param height height of fundamental domain (if used). 
     */
    public void setDomainHeight(double height){
        mp_domainHeight.setValue(height);
    }
    /**
       @param maxCont maximal count of tranformations to use to generate patterns
       <p>
       if maxCount = 0 - no transformation is used and only the content of fundamntal domain will be shown 
       </p>
     */
    public void setMaxCount(int maxCount){

        mp_maxCount.setValue(maxCount);

    }

    /**
       
       @param skew skew parameter of fundamental domain for symmetry O
     */
    public void setDomainSkew(double skew){
        mp_domainSkew.setValue(skew);
    }

    /**
     @noRefGuide
     */
    public int getSymmetryType(String symmetryName){
        mp_symmetryType.setValue(symmetryName);
        return mp_symmetryType.getIndex();
    }
    
    /**
     @noRefGuide
     */
    public int initialize(){
        
        int symmetryType = mp_symmetryType.getIndex();
        double domainWidth = mp_domainWidth.getValue();
        double domainHeight = mp_domainHeight.getValue();
        double domainSkew = mp_domainSkew.getValue();
        int maxCount = mp_maxCount.getValue();

        switch(symmetryType){
        default: 
        case WP_O:    m_group = WallpaperSymmetries.getO(domainWidth,domainHeight, domainSkew); break;

        case WP_3S3:  m_group = WallpaperSymmetries.get3S3(domainWidth); break;
        case WP_4S2:  m_group = WallpaperSymmetries.get4S2(domainWidth); break;
        case WP_S442:  m_group = WallpaperSymmetries.getS442(domainWidth); break;
        case WP_442:   m_group = WallpaperSymmetries.get442(domainWidth); break;
        case WP_S632:  m_group = WallpaperSymmetries.getS632(domainWidth); break;
        case WP_632:   m_group = WallpaperSymmetries.get632(domainWidth); break;
        case WP_S333:  m_group = WallpaperSymmetries.getS333(domainWidth); break;
        case WP_333:   m_group = WallpaperSymmetries.get333(domainWidth); break;

        case WP_S2222: m_group = WallpaperSymmetries.getS2222(domainWidth,domainHeight); break;
        case WP_2222:  m_group = WallpaperSymmetries.get2222(domainWidth,domainHeight); break;
        case WP_2S22:  m_group = WallpaperSymmetries.get2S22(domainWidth,domainHeight); break;
        case WP_22S:   m_group = WallpaperSymmetries.get22S(domainWidth,domainHeight); break;
        case WP_SS:    m_group = WallpaperSymmetries.getSS(domainWidth,domainHeight); break;
        case WP_SX:    m_group = WallpaperSymmetries.getSX(domainWidth,domainHeight); break;
        case WP_22X:   m_group = WallpaperSymmetries.get22X(domainWidth,domainHeight); break;
        case WP_XX:    m_group = WallpaperSymmetries.getXX(domainWidth,domainHeight); break;

        }

        m_group.setMaxIterations(maxCount);


        return RESULT_OK;
        
    }
    
    /**
     @noRefGuide
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
     @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {

        out.set(in);

        return m_group.toFD(out);        
        
    }
} // class WallpaperSymmetry

