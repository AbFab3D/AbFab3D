
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


import abfab3d.param.BaseParameterizable;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.StringParameter;
import abfab3d.param.Parameter;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;
import abfab3d.util.Units;
import abfab3d.util.TextUtil;
import abfab3d.util.Insets2;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Units.UM;


/**

   makes 3D text shape from text string 

   <embed src="doc-files/Text.svg" type="image/svg+xml"/> 
    
   <p>
   Text is oriented parallel to xy plane. The bounding box is centered at origin 
   </p>

   @author Vladimir Bulatov

 */
public class Text2D extends BaseParameterizable {

    static public final int BOLD = Font.BOLD; 
    static public final int ITALIC = Font.ITALIC; 
    static public final int PLAIN = Font.PLAIN; 

    static public final int 
        ALIGN_LEFT = TextUtil.ALIGN_LEFT,
        ALIGN_RIGHT = TextUtil.ALIGN_RIGHT, 
        ALIGN_CENTER = TextUtil.ALIGN_CENTER, 
        ALIGN_TOP = TextUtil.ALIGN_TOP, 
        ALIGN_BOTTOM = TextUtil.ALIGN_BOTTOM,
        FIT_VERTICAL = TextUtil.FIT_VERTICAL, 
        FIT_HORIZONTAL=TextUtil.FIT_HORIZONTAL, 
        FIT_BOTH = TextUtil.FIT_BOTH;
    static String m_fitNames[] = new String[]{"vertical","horizontal","both"};
    static int m_fitValues[] = new int[]{FIT_VERTICAL,FIT_HORIZONTAL, FIT_BOTH};

    static String m_hAlignNames[] = new String[]{"left","center","right"};
    static int m_hAlignValues[] = new int[]{ALIGN_LEFT,ALIGN_CENTER,ALIGN_RIGHT};
    static String m_vAlignNames[] = new String[]{"top","center","bottom"};
    static int m_vAlignValues[] = new int[]{ALIGN_TOP,ALIGN_CENTER,ALIGN_BOTTOM};

    static final boolean DEBUG = false;
    static int debugCount = 1000;

    // arbitrary font size, text is scaled to fit the box, but the size is affecting text rasterization somewhat 
    private int m_fontSize = 50; 

    BufferedImage m_bitmap = null;

    final int SCALING = 5;// scaling factor for text bitmap 

    // public params of the image 
    EnumParameter    mp_hAlign    = new EnumParameter("hAlign","horizontal text aligment (left, right, center)",m_hAlignNames, m_hAlignNames[0]);
    EnumParameter    mp_vAlign    = new EnumParameter("vAlign","vertical text aligment (top, bottom, center)",m_vAlignNames, m_vAlignNames[0]);
    EnumParameter    mp_fit       = new EnumParameter("fit","fitting of text (horizontal, vertical, both)", m_fitNames, m_fitNames[0]);
    BooleanParameter mp_aspect    = new BooleanParameter("preserveAspect","keep text aspect ratio",true);
    DoubleParameter  mp_fontSize  = new DoubleParameter("fontSize","size of text font to use in points",30);
    DoubleParameter  mp_height = new DoubleParameter("height","height of text",5*MM);
    DoubleParameter  mp_width = new DoubleParameter("width","width of text",20*MM);
    DoubleParameter  mp_voxelSize = new DoubleParameter("voxelSize","size of voxel for text rendering", 0.05*MM);
    StringParameter  mp_fontName  = new StringParameter("fontName","Name of the font", "Times New Roman");
    StringParameter  mp_text      = new StringParameter("text","text to be created", "Text");
    IntParameter     mp_fontStyle = new IntParameter("fontStyle","style of font (BOLD ,ITALIC, PLAIN)", PLAIN);
    DoubleParameter mp_inset      = new DoubleParameter("inset","white space around text", 0.5*MM);
    DoubleParameter mp_spacing    = new DoubleParameter("spacing","extra white space between characters in relative units", 0.);

    Parameter m_aparam[] = new Parameter[]{
        mp_vAlign,
        mp_hAlign,
        mp_fit,
        mp_aspect,
        mp_height,
        mp_width,
        mp_voxelSize,
        mp_fontName,
        mp_text,
        mp_fontStyle,  
        mp_inset,
        mp_spacing,
        mp_fontSize,
    };

    /**
     * Constructor
     @param text the string to convert into 3D text
     @param fontName name of font to be used for 3D text
     @param voxelSize size of voxel used for text rasterizetion
     */
    public Text2D(String text, String fontName, double voxelSize){
        super.addParams(m_aparam);
        mp_text.setValue(text);
        setFontName(fontName);
        setVoxelSize(voxelSize);
    }

    /**
     * Constructor
       @param text the string to convert into 3D text 
     */
    public Text2D(String text){
        super.addParams(m_aparam);
        mp_text.setValue(text);        
    }

    public void setFontName(String val) {
        m_bitmap = null;
        validateFontName(val);
        mp_fontName.setValue(val); 
    }

    public void setFontStyle(Integer val) {
        m_bitmap = null;
        mp_fontStyle.setValue(val);
    }

    public void setVoxelSize(double val) {
        m_bitmap = null;
        mp_voxelSize.setValue(val);
    }

    public void setSpacing(double val) {
        m_bitmap = null;
        mp_spacing.setValue(val);
    }

    public void setInset(double val) {
        m_bitmap = null;
        mp_inset.setValue(val);
    }

    public void set(String param, Object val) {
        // change of param causes change of bitmap
        m_bitmap = null;
        super.set(param, val);
    }


    public BufferedImage getImage(){
        if(m_bitmap == null)
            initialize();
        return m_bitmap;
    }

    protected void initParams(){
        super.addParams(m_aparam);
    }

    protected void validateFontName(String fontName){
        if (!TextUtil.fontExists(fontName)) {
            throw new IllegalArgumentException("Font not found:" + fontName);
        }
    }
    
    /**
       calculates preferred width of text box with for given font, insets and height
       the returned width includes width of text and insets on all sides 
       @return preferred text box width 
     */
    public double getPreferredWidth(){

        String fontName = mp_fontName.getValue();
        validateFontName(fontName);

        double voxelSize = mp_voxelSize.getValue();

        double inset = (mp_inset.getValue()/voxelSize);
        int fontStyle = mp_fontStyle.getValue();

        Font font = new Font(fontName, fontStyle, m_fontSize);
        String text = mp_text.getValue();

        double spacing = mp_spacing.getValue();
        Insets2 insets = new Insets2(inset,inset,inset,inset);

        int heightVoxels = (int)Math.round(mp_height.getValue()/voxelSize);         
        return voxelSize * TextUtil.getTextWidth(heightVoxels, text, font, spacing, insets);

    }

    /**
       @noRefGuide
     */
    public int initialize(){
        
        double voxelSize = mp_voxelSize.getValue();

        String fontName = mp_fontName.getValue();
        validateFontName(fontName);

        int height = (int)Math.round(mp_height.getValue()/voxelSize);         
        int width = (int)Math.round(mp_width.getValue()/voxelSize);         

        String text = mp_text.getValue();

        int fontStyle = mp_fontStyle.getValue();
        double inset = (mp_inset.getValue()/voxelSize);        
        Insets2 insets = new Insets2(inset,inset,inset,inset);
        boolean aspect = mp_aspect.getValue();

        int fit = m_fitValues[mp_fit.getIndex()];
        int halign = m_hAlignValues[mp_hAlign.getIndex()];
        int valign = m_vAlignValues[mp_vAlign.getIndex()];

        m_bitmap = TextUtil.createTextImage(width, height, text, new Font(fontName, fontStyle, m_fontSize), mp_spacing.getValue().doubleValue(),insets, aspect, fit, halign, valign);
        
        if(DEBUG)printf("Text2D bitmap height: %d x %d\n", m_bitmap.getWidth(), m_bitmap.getHeight());
        
        return DataSource.RESULT_OK;
        
    }
    
}  // class Text2D 
