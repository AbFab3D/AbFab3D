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

import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.Shape;
import java.awt.Font;
import java.awt.Graphics2D;

import java.util.Hashtable;

import java.awt.font.TextAttribute;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;


import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.core.Bounds;
import abfab3d.core.ResultCodes;
import abfab3d.core.ImageProducer;
import abfab3d.core.FontProducer;

import abfab3d.param.ParamCache;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.ColorParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.StringParameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.Parameter;

import abfab3d.grid.op.SystemFontLoader;

import abfab3d.util.TextUtil;
import abfab3d.util.Insets2;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;

import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.PT;
import static abfab3d.core.Output.fmt;


/**

   makes 2D image of text 
    
   @author Vladimir Bulatov

 */
public class Text2D extends BaseParameterizable implements ImageProducer {

    
    static final boolean DEBUG = false;
    static final boolean CACHING_ENABLED = true;

    public enum Fit {VERTICAL, HORIZONTAL, BOTH, NONE}
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
        FIT_BOTH = TextUtil.FIT_BOTH,
        FIT_NONE = TextUtil.FIT_NONE;


    static int m_fitValues[] = new int[]{FIT_VERTICAL,FIT_HORIZONTAL, FIT_BOTH, FIT_NONE};
    static int m_hAlignValues[] = new int[]{ALIGN_LEFT,ALIGN_CENTER,ALIGN_RIGHT};
    static int m_vAlignValues[] = new int[]{ALIGN_TOP,ALIGN_CENTER,ALIGN_BOTTOM};

    static int debugCount = 1000;

    CachedTextData m_cachedData = null;

    StringParameter  mp_text      = new StringParameter("text","text to be created", "Text");
    EnumParameter mp_horizAlign = new EnumParameter("horizAlign","horizontal text alignment (left, right, center)",
                                                    EnumParameter.enumArray(HorizAlign.values()), HorizAlign.LEFT.toString());
    EnumParameter mp_vertAlign = new EnumParameter("vertAlign","vertical text alignment (top, bottom, center)",
                                                   EnumParameter.enumArray(VertAlign.values()), VertAlign.CENTER.toString());
    EnumParameter    mp_fit       = new EnumParameter("fit","fitting of text (horizontal, vertical, both, none)",
                                                      EnumParameter.enumArray(Fit.values()), Fit.VERTICAL.toString());
    BooleanParameter mp_aspect    = new BooleanParameter("preserveAspect","keep text aspect ratio",true);
    DoubleParameter  mp_fontSize  = new DoubleParameter("fontSize","size of text font to use in points",30);
    DoubleParameter  mp_height = new DoubleParameter("height","height of text",5*MM);
    DoubleParameter  mp_width = new DoubleParameter("width","width of text",0*MM); // width is initially undefined 
    DoubleParameter  mp_voxelSize = new DoubleParameter("voxelSize","size of voxel for text rendering", 0.05*MM);
    IntParameter     mp_fontStyle = new IntParameter("fontStyle","style of font (BOLD ,ITALIC, PLAIN)", PLAIN);
    DoubleParameter mp_inset      = new DoubleParameter("inset","white space around text", 0.5*MM);
    DoubleParameter mp_spacing    = new DoubleParameter("spacing","extra white space between characters in relative units", 0.);
    DoubleParameter mp_textOriginX = new DoubleParameter("textOriginX","text origin relative to left bounds", 0.);
    DoubleParameter mp_textOriginY = new DoubleParameter("textOriginY","text origin relative to bottom bounds", 0.);
    BooleanParameter mp_kerning = new BooleanParameter("kerning","layout with pair kerning", false);

    DoubleParameter mp_outlineWidth = new DoubleParameter("outlineWidth","width of text outline", 0.1*MM);

    BooleanParameter mp_outline = new BooleanParameter("outline","text outlne", false);
    BooleanParameter mp_fill = new BooleanParameter("fill","text fill", true);
    ColorParameter mp_fillColor = new ColorParameter("fillColor","fill color", new Color(0,0,0));
    ColorParameter mp_outlineColor = new ColorParameter("outlineColor","outline color", new Color(0,0,0));
    ColorParameter mp_backgroundColor = new ColorParameter("backgroundColor","outline color", new Color(1,1,1));
    SNodeParameter mp_fontSource = new SNodeParameter("font","Font source", null);

    Parameter m_param[] = new Parameter[]{
        mp_vertAlign,
        mp_horizAlign,
        mp_fit,
        mp_aspect,
        mp_height,
        mp_width,
        mp_voxelSize,
        mp_fontSource,
        mp_text,
        mp_fontStyle,  
        mp_inset,
        mp_spacing,
        mp_fontSize,
        mp_textOriginX,
        mp_textOriginY,
        mp_outlineWidth,
        mp_outline,
        mp_fill,
        mp_fillColor,
        mp_outlineColor,
        mp_backgroundColor,
        mp_kerning

    };


    /**
     * Constructor
     @param text the string to convert into 3D text
     @param fontName name of font to be used for 3D text
     @param voxelSize size of voxel used for text rasterizetion
     */
    public Text2D(String text, String fontName, double voxelSize){
        super.addParams(m_param);

        mp_text.setValue(text);
        mp_fontSource.setValue(new SystemFontLoader(fontName));
        mp_voxelSize.setValue(voxelSize);
    }

    /**
     * Constructor
     @param text the string to convert into 3D text
     @param font font to be used for 3D text
     @param voxelSize size of voxel used for text rasterizetion
     */
    public Text2D(String text, FontProducer fontProducer, double voxelSize){
        super.addParams(m_param);

        mp_text.setValue(text);
        mp_fontSource.setValue(fontProducer);
        mp_voxelSize.setValue(voxelSize);
    }

    /**
     * Constructor
       @param text the string to convert into 3D text 
     */
    public Text2D(String text){
        super.addParams(m_param);
        mp_text.setValue(text);
    }

    /**
     * Get the font name
     * @return
     */
    //public String getFontName() {
    //    return mp_fontName.getValue();
    //}

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
     * Set the specific font producer
     * @param fontProducer The font producer, or null to clear
     */
    public void setFont(FontProducer fontProducer) {
        mp_fontSource.setValue(fontProducer);
        
    }

    /**
     * Get the font used by this text
     */
    protected Font getFont(){
        FontProducer fp = (FontProducer)mp_fontSource.getValue();
        if(fp == null)
            return new Font("Tmes New Roman", PLAIN, 12);
        else
            return fp.getFont();
    }
    
    /**
     * Get the voxel size
     * @return
     */
    public double getVoxelSize() {
        return mp_voxelSize.getValue();
    }

    public void setText(String val) {
        mp_text.setValue(val);
    }

    /**
     * Set the font name.  The available fonts are server dependent.
     */
    public void setFontName(String fontName) {
        validateFontName(fontName);
        mp_fontSource.setValue(new SystemFontLoader(fontName)); 
    }

    public void setFontStyle(Integer val) {
        mp_fontStyle.setValue(val);
    }

    /**
     * Set the voxel size
     * @param val The size in meters
     */
    public void setVoxelSize(double val) {
        mp_voxelSize.setValue(val);
    }

    public void setSpacing(double val) {
        mp_spacing.setValue(val);
    }

    public double getSpacing() {
        return mp_spacing.getValue();
    }

    public void setInset(double val) {
        mp_inset.setValue(val);
    }

    public double getInset() {
        return mp_inset.getValue();
    }

    public void set(String param, Object val) {
        // change of param causes change of bitmap
        m_cachedData = null;
        super.set(param, val);
    }

    public void setWidth(double val) {
        if (val <= 0) {
            throw new IllegalArgumentException(fmt("illegal Text2D width:%11.9f", val));
        }
        mp_width.setValue(val);
    }

    public double getWidth() {
        return mp_width.getValue();
    }

    public void setHeight(double val) {
        if (val <= 0) {
            throw new IllegalArgumentException(fmt("illegal Text2D height:%11.9f", val));
        }
        mp_height.setValue(val);
    }

    public double getHeight() {
        return mp_height.getValue();
    }

    public void setHorizAlign(String val) {
        mp_horizAlign.setValue(val);
    }

    public String getHorizAlign() {
        return mp_horizAlign.getValue();
    }

    public void setVertAlign(String val) {
        mp_vertAlign.setValue(val);
    }

    public String getVertAlign() {
        return mp_vertAlign.getValue();
    }

    public void setFit(String val) {
        mp_fit.setValue(val);
    }

    public String getFit() {
        return mp_fit.getValue();
    }

    public void setPreserveAspect(boolean val) {
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

        initialize();        
        return m_cachedData.image;
    }

    /**
       @return physical bounds of the image
     */
    public Bounds getImageBounds(){

        initialize();        
        return m_cachedData.imageBounds;
    }

    /**
       @return physical bounds of the text 
     */
    public Bounds getTextBounds(){

        initialize();        
        return m_cachedData.textBounds;

    }

    /**
       @return position of glyph at the given index 
     */
    public Point2D getGlyphPosition(int index){
        initialize();        
        Point2D pnt = m_cachedData.gv.getGlyphPosition(index);
        
        return new Point2D.Double(pnt.getX()*PT, pnt.getY()*PT);

    }

    public Bounds getGlyphBounds(int index){
        initialize();        
        Shape shape = m_cachedData.gv.getGlyphVisualBounds(index);        
        Rectangle2D rect = shape.getBounds2D();
        return new Bounds(rect.getX()*PT, 
                          (rect.getX() + rect.getWidth())*PT, 
                          -(rect.getY() + rect.getHeight())*PT,
                          -rect.getY()*PT,0,0);
    }

    protected void validateFontName(String fontName){
        if (!TextUtil.fontExists(fontName)) {
            throw new IllegalArgumentException(fmt("Text2D: Font \"%s\" not found",fontName));
        }
    }
    
    /**
       calculates preferred width of text box for given font, insets and height
       the returned width includes width of text and insets on all sides 
       @return preferred text box width

     @noRefGuide
     */
    public double getPreferredWidth(){
        double voxelSize = mp_voxelSize.getValue();
        double inset = (mp_inset.getValue()/voxelSize);
        int fontStyle = mp_fontStyle.getValue();

        Font font = getFont();

        font = font.deriveFont(fontStyle,mp_fontSize.getValue().floatValue());
        
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

        if(DEBUG) printf("Text2D.initialize()\n");

        String label = getParamString(getClass().getSimpleName(), m_param);
        Object co = null;
        if(CACHING_ENABLED)co = ParamCache.getInstance().get(label);
        if (co == null) {
            m_cachedData = prepareData();
            if(CACHING_ENABLED)ParamCache.getInstance().put(label,m_cachedData);
            if (DEBUG) printf("Text2D: caching data: %s -> %s\n",label, m_cachedData);
        } else {
            m_cachedData = (CachedTextData) co;
            if (DEBUG) printf("Text2D: got cached data %s -> %s\n",label, m_cachedData);
        }
        return ResultCodes.RESULT_OK;
    }

    protected CachedTextData prepareData(){
        
        if(mp_fit.getIndex() == FIT_NONE){
            return prepareData_v2();
        } else {
            return prepareData_v1();            
        }
    }
        
    protected CachedTextData prepareData_v1(){
        
        double voxelSize = mp_voxelSize.getValue();
        if(DEBUG) printf("  voxelSize:%7.5f\n", voxelSize);
        
        int fontStyle = mp_fontStyle.getValue();
        double inset = (mp_inset.getValue()/voxelSize);        
        Insets2 insets = new Insets2(inset,inset,inset,inset);
        boolean aspect = mp_aspect.getValue();

        int fit = m_fitValues[mp_fit.getIndex()];
        int halign = m_hAlignValues[mp_horizAlign.getIndex()];
        int valign = m_vAlignValues[mp_vertAlign.getIndex()];
        
        // No need to validate font name if font is already set
        Font font = getFont();

        if(DEBUG) printf("  fontName:%s\n", font.getName());
        
        int height = (int)Math.round(mp_height.getValue()/voxelSize);
        if(DEBUG) printf("  text height:%d pixels\n", height);

        try {
	        if(mp_width.getValue() <= 0.) mp_width.setValue(getPreferredWidth());
        } catch (IllegalArgumentException iae) {
        	printf("%s\n", iae.getMessage());
        }

        int width = (int)Math.round(mp_width.getValue()/voxelSize);         
        if(DEBUG) printf("  text width:%d pixels\n", width);

        String text = mp_text.getValue();

        if(DEBUG) printf("  text:\"%s\"\n", text);

        font = font.deriveFont(fontStyle,mp_fontSize.getValue().floatValue());
        BufferedImage image = 
            TextUtil.createTextImage(width, height, text, font, mp_spacing.getValue().doubleValue(),insets, aspect, fit, halign, valign);
        
        if(DEBUG)printf("Text2D image [%d x %d] ready\n", image.getWidth(), image.getHeight());
        
        Bounds imageBounds = new Bounds(0.,mp_width.getValue(),0.,mp_height.getValue(),0.,0.,voxelSize);

        // this is not absolutely correct, the exact text bounds depend on the text fitting
        Bounds textBounds = new Bounds(inset,mp_width.getValue()-2*inset,inset,mp_height.getValue()-2*inset,0.,0.,voxelSize);
        
        return new CachedTextData(image, imageBounds, textBounds, null);

    }

    protected CachedTextData prepareData_v2(){

        
        double voxelSize = mp_voxelSize.getValue();
        double fontSize = mp_fontSize.getValue();
        int fontStyle = mp_fontStyle.getValue();
        double inset = mp_inset.getValue()/PT;        
        double spacing = mp_spacing.getValue();

        if(DEBUG)printf("Text2D fontSize: %5.1f\n", fontSize);
        
        Font font = getFont();
        
        Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();

        if(mp_kerning.getValue())
            map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        map.put(TextAttribute.SIZE, new Double(fontSize));        
        font = font.deriveFont(map);

        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics(); 
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        FontRenderContext frc = g.getFontRenderContext();

        String text = mp_text.getValue();        
        char ctext[] = text.toCharArray();
        GlyphVector gv = font.layoutGlyphVector(frc, ctext, 0, ctext.length, 0);

        // add extra spacing 
        // last position is position after last glyph
        double space = spacing*gv.getGlyphPosition(ctext.length).getX()/ctext.length; 
        for(int i = 0; i < ctext.length; i++){
            Point2D pnt = gv.getGlyphPosition(i);
            //Rectangle2D rect = gv.getGlyphVisualBounds(i).getBounds2D();
            gv.setGlyphPosition(i, new Point2D.Double(pnt.getX() + space*i, pnt.getY())); 
        }

        Rectangle2D textRect = gv.getVisualBounds(); 

        Bounds textBounds = new Bounds(textRect.getX()*PT,
                                       (textRect.getX() + textRect.getWidth())*PT,
                                       -(textRect.getY() + textRect.getHeight())*PT, // flip Y axis 
                                       -textRect.getY()*PT, 
                                       0,0, voxelSize);
        Bounds imageBounds = new Bounds((textRect.getX()-inset)*PT,
                                        (textRect.getX() + textRect.getWidth()+2*inset)*PT,
                                        -(textRect.getY() + textRect.getHeight()+2*inset)*PT, // flip Y axis 
                                        -(textRect.getY() -inset)*PT, 
                                        0,0, voxelSize);
        if(DEBUG)printf("Text2D image bounds: (%s)mm\n", imageBounds.toString(MM));
        if(DEBUG)printf("Text2D text bounds: (%s)mm\n", textBounds.toString(MM));

        // font units are PT 
        double scaleFactor = PT/voxelSize; // conversion from text units into image units
        // image is in voxels 
        int imageWidth =  imageBounds.getGridWidth();
        int imageHeight = imageBounds.getGridHeight();
        if(DEBUG)printf("Text2D image size: [%d x %d]\n", imageWidth,imageHeight);

        BufferedImage image2 = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);        
        Graphics2D g2 = (Graphics2D)image2.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(mp_backgroundColor.getValue().toAWT());
        g2.fill(new Rectangle2D.Double(0,0,imageWidth, imageHeight));
        g2.scale(scaleFactor,scaleFactor);
        g2.translate(-textRect.getX() +  + inset,-textRect.getY()+inset);

        if(mp_outline.getValue()){

            g2.setColor(mp_outlineColor.getValue().toAWT());
            //double outlineWidth = mp_outlineWidth.getValue()/voxelSize;
            double outlineWidth = (mp_outlineWidth.getValue()/PT); // width in points 
            BasicStroke stroke = new BasicStroke((float)outlineWidth, BasicStroke.CAP_ROUND,  	BasicStroke.JOIN_ROUND);
            g2.setStroke(stroke);
            Shape textOutline = gv.getOutline();
            g2.draw(textOutline);
            
            //g2.drawGlyphVector(gv, 0,0);
        }

        if(mp_fill.getValue()){
            g2.setColor(mp_fillColor.getValue().toAWT());
            g2.drawGlyphVector(gv, 0,0);
        }

        return new CachedTextData(image2, imageBounds, textBounds, gv);

    }

    /**
       cashed text data 
     */
    static class CachedTextData  {
        
        BufferedImage image;
        Bounds imageBounds;
        Bounds textBounds;
        GlyphVector gv;
        double voxelSize;
        CachedTextData(BufferedImage _image, Bounds _imageBounds, Bounds _textBounds, GlyphVector _gv){
            image = _image;
            imageBounds = _imageBounds;
            textBounds = _textBounds;
            gv = _gv;
            //voxelSize = _voxelSize;
        }
    }

}  // class Text2D
