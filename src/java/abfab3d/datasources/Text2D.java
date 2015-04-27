
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
import static abfab3d.util.Units.UM;
import static abfab3d.util.Units.PT;


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

    static final boolean DEBUG = false;
    static int debugCount = 1000;

    // arbitrary font size, text is scaled to fit the box, but the size is affecting text rasterization somewhat 
    private int m_fontSize = 50; 

    BufferedImage m_bitmap = null;

    final int SCALING = 5;// scaling factor for text bitmap 

    // public params of the image 
    DoubleParameter  mp_fontSize = new DoubleParameter("fontSize","size of text font in points",10);
    DoubleParameter  mp_voxelSize = new DoubleParameter("voxelSize","size of voxel for text rendering", 0.05*MM);
    StringParameter  mp_fontName = new StringParameter("fontName","Name of the font", "Times New Roman");
    StringParameter  mp_text = new StringParameter("text","text to be created", "Text");
    IntParameter  mp_fontStyle = new IntParameter("fontStyle","style of font(BOLD ,ITALIC, PLAIN)", PLAIN);
    DoubleParameter mp_inset = new DoubleParameter("inset","white space around text in points", 1.);
    DoubleParameter mp_spacing = new DoubleParameter("spacing","extra white space between characters in relative units", 0.);

    Parameter m_aparam[] = new Parameter[]{
        mp_fontSize,
        mp_voxelSize,
        mp_fontName,
        mp_text,
        mp_fontStyle,  
        mp_inset,
        mp_spacing,
    };

    /**

       @param text the string to convert into 3D text 
       @param fontName name of font to be used for 3D text
       @param sx width of the 3D text bounding box
       @param sy height of the 3D text bounding box
       @param sz thickness of 3D text bounding box

       @param voxelSize size of voxel used for text rasterizetion
     */
    public Text2D(String text){
        super.addParams(m_aparam);
        mp_text.setValue(text);        
    }

    public void setFontName(String val) {
        if (!TextUtil.fontExists(val)) {
            throw new IllegalArgumentException("Font not found:" + val);
        }

        mp_fontStyle.setValue(val);
    }

    public void setFontStyle(String val) {
        mp_fontStyle.setValue(val);
    }


    public BufferedImage getImage(){
        if(m_bitmap == null)
            initialize();
        return m_bitmap;
    }

    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
       @noRefGuide
     */
    public int initialize(){
        
        // size of bitmap to render text 
        double size = PT*mp_fontSize.getValue();
        double voxelSize = mp_voxelSize.getValue();
        //int nx = SCALING*(int)Math.round(size.x/voxelSize);
        int ny = (int)Math.round(size/voxelSize); 
        int nx = ny;// TODO - how to get ny 
        String text = mp_text.getValue();
        String fontName = mp_fontName.getValue();
        int fontStyle = mp_fontStyle.getValue();
        int inset = (int)(PT*mp_inset.getValue()/voxelSize);

        m_bitmap = TextUtil.createTextImage(ny, text, new Font(fontName, fontStyle, m_fontSize), new Insets(inset,inset,inset,inset), mp_spacing.getValue().doubleValue());
        
        printf("Text2D bitmap height: %d x %d\n", m_bitmap.getWidth(), m_bitmap.getHeight());
        
        return DataSource.RESULT_OK;
        
    }
    
}  // class Text2D 
