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

package abfab3d.datasources;


import java.awt.image.BufferedImage;

import java.util.Vector;
import java.awt.Font;
import java.awt.Insets;


import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.param.Vector3dParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.StringParameter;
import abfab3d.param.Parameter;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;
import abfab3d.util.Units;
import abfab3d.util.TextUtil;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;

import static abfab3d.util.Units.MM;


/**

   makes 3D text shape from text string 

   <embed src="doc-files/Text.svg" type="image/svg+xml"/> 
    
   <p>
   Text is oriented parallel to xy plane. The bounding box is centered at origin 
   </p>

   @author Vladimir Bulatov

 */
public class Text extends TransformableDataSource {

    static public final int BOLD = Font.BOLD; 
    static public final int ITALIC = Font.ITALIC; 
    static public final int PLAIN = Font.PLAIN; 

    static final boolean DEBUG = false;
    static int debugCount = 1000;

    //private double m_sizeX=30*MM, m_sizeY=10*MM, m_sizeZ=2*MM;
    //private double m_voxelSize = 0.1*MM;

    private Image3D m_bitmap = null; 
    private int m_fontSize = 50; // arbitrary font size, text is scaled to fit box anyway
    //private double m_textBlurWidth = 1.;
    private int m_textScale = 5;

    // public params of the image 
    Vector3dParameter  mp_center = new Vector3dParameter("center","center of the text box",new Vector3d(0,0,0));
    Vector3dParameter  mp_size = new Vector3dParameter("size","size of the text box",new Vector3d(30*MM, 10*MM, 2*MM));
    DoubleParameter  mp_rounding = new DoubleParameter("rounding","rounding of the box edges", 0.);
    DoubleParameter  mp_voxelSize = new DoubleParameter("voxelSize","size of voxel for text rendering", 0.1*MM);
    DoubleParameter  mp_blurWidth = new DoubleParameter("blurWidth", "width of gaussian blur on the text bitmap", 0.025*MM);

    BooleanParameter  mp_useGrayscale = new BooleanParameter("useGrayscale","Use grayscale for text rendering", true);
    StringParameter  mp_fontName = new StringParameter("fontName","Name of the font", "Times New Roman");
    StringParameter  mp_text = new StringParameter("text","text to be created", "Text");
    IntParameter  mp_fontStyle = new IntParameter("fontStyle","style of font(BOLD ,ITALIC, PLAIN)", PLAIN);

    Parameter m_aparam[] = new Parameter[]{
        mp_center,
        mp_size,
        mp_voxelSize,
        mp_blurWidth,
        mp_rounding,
        mp_useGrayscale,
        mp_fontName,
        mp_text,
        mp_fontStyle,        
    };

    /**

       @param text the string to convert intoi 3D text 
       @param fontName name of font to be used for 3D text
       @param sx width of the 3D text bounding box
       @param sy height of the 3D text bounding box
       @param sz thickness of 3D text bounding box

       @param voxelSize size of voxel used for text rasterizetion
     */
    public Text(String text, String fontName, double sx, double sy, double sz, double voxelSize){
        initParams();
        mp_size.setValue(new Vector3d(sx, sy, sz));
        mp_voxelSize.setValue(new Double(voxelSize));        
        mp_text.setValue(text);
        mp_fontName.setValue(fontName);
        
    }

    public void setFontStyle(int fontStyle){
        mp_fontStyle.setValue(new Integer(fontStyle));
    }
        

    public Image3D getBitmap(){
        return m_bitmap;
    }

    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();
        
        // size of bitmap to render text 
        Vector3d size = mp_size.getValue();
        double voxelSize = mp_voxelSize.getValue();
        int nx = m_textScale*(int)Math.round(size.x/voxelSize);
        int ny = m_textScale*(int)Math.round(size.y/voxelSize); 
        String text = mp_text.getValue();
        String fontName = mp_fontName.getValue();
        int fontStyle = mp_fontStyle.getValue();


        BufferedImage img = TextUtil.createTextImage(nx, ny, text, new Font(fontName, fontStyle, m_fontSize), new Insets(0,0,0,0), true);
        
        printf("text bitmap: %d x %d\n", nx, ny);
        //Font font = new Font(m_fontName, m_fontStyle, m_fontSize);

        
        m_bitmap = new Image3D();

        m_bitmap.setImage(img);
        m_bitmap.setSize(size.x, size.y, size.z);
        m_bitmap.setBaseThickness(0.);
        m_bitmap.setImageType(Image3D.IMAGE_TYPE_EMBOSSED);
        m_bitmap.setTiles(1, 1);
        m_bitmap.setBlurWidth(mp_blurWidth.getValue());
        //m_bitmap.setUseGrayscale(false); // VB - temp fix for GPU 
        m_bitmap.setUseGrayscale(true);
        m_bitmap.setInterpolationType(Image3D.INTERPOLATION_LINEAR);
        m_bitmap.setTransform(getTransform());
        
        m_bitmap.initialize();

        
        return RESULT_OK;
        
    }
    
    /**
     * returns 1 if pnt is inside of block of given size and location
     * returns 0 otherwise
     @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        return m_bitmap.getDataValue(pnt, data);
        
    }
    
}  // class Text
