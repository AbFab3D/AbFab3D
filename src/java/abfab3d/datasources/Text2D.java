
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

import java.awt.Font;


import abfab3d.param.*;


import abfab3d.util.DataSource;
import abfab3d.util.TextUtil;
import abfab3d.util.Insets2;

import javax.vecmath.Vector3d;

import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
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
public class Text2D extends BaseParameterizable {
    static final boolean DEBUG = true;

    public enum Fit {VERTICAL, HORIZONTAL, BOTH}
    public enum HorizAlign {LEFT, CENTER, RIGHT}
    public enum VertAlign {TOP, CENTER, BOTTOM}

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

    static int m_fitValues[] = new int[]{FIT_VERTICAL,FIT_HORIZONTAL, FIT_BOTH};
    static int m_hAlignValues[] = new int[]{ALIGN_LEFT,ALIGN_CENTER,ALIGN_RIGHT};
    static int m_vAlignValues[] = new int[]{ALIGN_TOP,ALIGN_CENTER,ALIGN_BOTTOM};

    static int debugCount = 1000;

    // arbitrary font size, text is scaled to fit the box, but the size is affecting text rasterization somewhat 
    private int m_fontSize = 50; 

    BufferedImage m_bitmap = null;

    final int SCALING = 5;// scaling factor for text bitmap 

    // public params of the image 
    EnumParameter mp_horizAlign = new EnumParameter("horizAlign","horizontal text alignment (left, right, center)",
            EnumParameter.enumArray(HorizAlign.values()), HorizAlign.LEFT.toString());
    EnumParameter mp_vertAlign = new EnumParameter("vertAlign","vertical text alignment (top, bottom, center)",
            EnumParameter.enumArray(VertAlign.values()), VertAlign.CENTER.toString());
    EnumParameter    mp_fit       = new EnumParameter("fit","fitting of text (horizontal, vertical, both)",
            EnumParameter.enumArray(Fit.values()), Fit.VERTICAL.toString());
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
    ObjectParameter mp_font = new ObjectParameter("font","Specific font to use",null);

    Parameter m_aparam[] = new Parameter[]{
            mp_vertAlign,
            mp_horizAlign,
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
     @param font font to be used for 3D text
     @param voxelSize size of voxel used for text rasterizetion
     */
    public Text2D(String text, Font font, double voxelSize){
        super.addParams(m_aparam);
        mp_text.setValue(text);
        setFont(font);
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

    /**
     * Get the font name
     * @return
     */
    public String getFontName() {
        return mp_fontName.getValue();
    }

    /**
     * Set the font style
     * @param fontStyle
     */
    public void setFontStyle(int fontStyle){
        mp_fontStyle.setValue(new Integer(fontStyle));
    }

    /**
     * Get the font style
     * @return
     */
    public int getFontStyle() {
        return mp_fontStyle.getValue();
    }


    /**
     * Set the specific font.  This overrides any value set in fontName
     * @param font The font, or null to clear
     */
    public void setFont(Font font) {
        mp_font.setValue(font);
    }

    /**
     * Get the font set by setFont.  This will not return a font for Text created via the fontName route.
     */
    public Font getFont() {
        return (Font) mp_font.getValue();
    }
    /**
     * Get the voxel size
     * @return
     */
    public double getVoxelSize() {
        return mp_voxelSize.getValue();
    }

    public void setText(String val) {
        m_bitmap = null;
        mp_text.setValue(val);
    }

    /**
     * Set the font name.  The available fonts are server dependent.
     */
    public void setFontName(String val) {
        m_bitmap = null;
        validateFontName(val);
        mp_fontName.setValue(val); 
    }

    public void setFontStyle(Integer val) {
        m_bitmap = null;
        mp_fontStyle.setValue(val);
    }

    /**
     * Set the voxel size
     * @param val The size in meters
     */
    public void setVoxelSize(double val) {
        m_bitmap = null;
        mp_voxelSize.setValue(val);
    }

    public void setSpacing(double val) {
        m_bitmap = null;
        mp_spacing.setValue(val);
    }

    public double getSpacing() {
        return mp_spacing.getValue();
    }

    public void setInset(double val) {
        m_bitmap = null;
        mp_inset.setValue(val);
    }

    public double getInset() {
        return mp_inset.getValue();
    }

    public void set(String param, Object val) {
        // change of param causes change of bitmap
        m_bitmap = null;
        super.set(param, val);
    }

    public void setWidth(double val) {
        if (val <= 0) {
            throw new IllegalArgumentException("Text2D width cannot be <= 0");
        }

        m_bitmap = null;
        mp_width.setValue(val);
    }

    public double getWidth() {
        return mp_width.getValue();
    }

    public void setHeight(double val) {
        if (val <= 0) {
            throw new IllegalArgumentException("Text2D height cannot be <= 0");
        }

        m_bitmap = null;
        mp_height.setValue(val);
    }

    public double getHeight() {
        return mp_height.getValue();
    }

    public void setHorizAlign(String val) {
        m_bitmap = null;
        mp_horizAlign.setValue(val.toUpperCase());
    }

    public String getHorizAlign() {
        return mp_horizAlign.getValue();
    }

    public void setVertAlign(String val) {
        m_bitmap = null;
        mp_vertAlign.setValue(val.toUpperCase());
    }

    public String getVertAlign() {
        return mp_vertAlign.getValue();
    }

    public void setFit(String val) {
        m_bitmap = null;
        mp_fit.setValue(val.toUpperCase());
    }

    public String getFit() {
        return mp_fit.getValue();
    }

    public void setPreserveAspect(boolean val) {
        m_bitmap = null;
        mp_aspect.setValue(val);
    }

    public boolean getPreserveAspect() {
        return mp_aspect.getValue();
    }

    /**
     * @noRefGuide
     * @return
     */
    public BufferedImage getImage(){

        if(m_bitmap == null)
            initialize();

        return m_bitmap;
    }

    /**
     * @noRefGuide
     */
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

     @noRefGuide
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
        int halign = m_hAlignValues[mp_horizAlign.getIndex()];
        int valign = m_vAlignValues[mp_vertAlign.getIndex()];

        Font font = (Font) mp_font.getValue();
        if (font != null) {
            font = font.deriveFont(fontStyle,m_fontSize);
            m_bitmap = TextUtil.createTextImage(width, height, text, font, mp_spacing.getValue().doubleValue(),insets, aspect, fit, halign, valign);
        } else {
            m_bitmap = TextUtil.createTextImage(width, height, text, new Font(fontName, fontStyle, m_fontSize),mp_spacing.getValue().doubleValue(),insets, aspect, fit, halign, valign);
        }

        if(DEBUG)printf("Text2D bitmap height: %d x %d\n", m_bitmap.getWidth(), m_bitmap.getHeight());
        
        return DataSource.RESULT_OK;
        
    }
    
}  // class Text2D 
