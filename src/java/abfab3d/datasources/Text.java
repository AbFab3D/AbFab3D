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


import javax.vecmath.Vector3d;


import abfab3d.core.ResultCodes;
import abfab3d.param.*;


import abfab3d.core.Vec;
import abfab3d.util.TextUtil;
import abfab3d.util.Insets2;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;

import static abfab3d.core.Units.MM;


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
    ObjectParameter mp_font = new ObjectParameter("font","Specific font to use",null);
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

    
    /**
       @noRefGuide
     */
    public Text(String text, Font font, double sx, double sy, double sz, double voxelSize){
        initParams();
        mp_size.setValue(new Vector3d(sx, sy, sz));
        mp_voxelSize.setValue(new Double(voxelSize));
        mp_text.setValue(text);
        mp_font.setValue(font);
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
     * Set the size of the box.
     * @param val The size in meters
     */
    public void setSize(Vector3d val) {
        mp_size.setValue(val);
    }

    /**
     * Get the size
     */
    public Vector3d getSize() {
        return mp_size.getValue();
    }

    /**
     * Set the center of the coordinate system
     * @param val The center
     */
    public void setCenter(Vector3d val) {
        mp_center.setValue(val);
    }

    /**
     * Get the center of the coordinate system
     * @return
     */
    public Vector3d getCenter() {
        return mp_center.getValue();
    }

    /**
     * Set the amount of rounding of the edges
     * @param val The rounding.  Default is 0
     */
    public void setRounding(double val) {
        mp_rounding.setValue(val);
    }

    /**
     * Get the amount of rounding of the edges
     */
    public double getRounding() {
        return mp_rounding.getValue();
    }

    /**
     * Set the font name.  The available fonts are system dependent.
     * @param fontName
     */
    public void setFontName(String fontName){
        mp_fontName.setValue(fontName);
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
     * Set the voxel size
     * @param val The size in meters
     */
    public void setVoxelSize(double val) {
        mp_voxelSize.setValue(val);
    }

    /**
     * Get the voxel size
     * @return
     */
    public double getVoxelSize() {
        return mp_voxelSize.getValue();
    }

    /**
     * @noRefGuide
     */
    public Image3D getBitmap(){
        return m_bitmap;
    }

    /**
     * @noRefGuide
     */
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


        BufferedImage img = null;
        Font font = (Font) mp_font.getValue();
        if (font != null) {
            font = font.deriveFont(fontStyle,m_fontSize);
            img = TextUtil.createTextImage(nx, ny, text, font, new Insets2(0,0,0,0), true);
        } else {
            img = TextUtil.createTextImage(nx, ny, text, new Font(fontName, fontStyle, m_fontSize), new Insets2(0, 0, 0, 0), true);
        }

        printf("text bitmap: %d x %d\n", nx, ny);
        //Font font = new Font(m_fontName, m_fontStyle, m_fontSize);

        
        m_bitmap = new Image3D();

        m_bitmap.setImage(img);
        m_bitmap.setSize(size.x, size.y, size.z);
        m_bitmap.setBaseThickness(0.);
        m_bitmap.setImageType(Image3D.IMAGE_TYPE_EMBOSSED);
        m_bitmap.setTiles(1, 1);
        m_bitmap.set("blurWidth", mp_blurWidth.getValue());
        m_bitmap.set("useGrayscale",false); // text is always black and white
        m_bitmap.setInterpolationType(Image3D.INTERPOLATION_LINEAR);
        m_bitmap.setTransform(getTransform());
        
        m_bitmap.initialize();

        
        return ResultCodes.RESULT_OK;
        
    }
    
    /**
     * returns 1 if pnt is inside of block of given size and location
     * returns 0 otherwise
     * @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec data) {
        
        return m_bitmap.getDataValue(pnt, data);
        
    }
    
}  // class Text
