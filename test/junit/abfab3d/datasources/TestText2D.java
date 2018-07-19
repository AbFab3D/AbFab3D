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

// External Imports


import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.imageio.ImageIO;


import java.io.File;

// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports

import abfab3d.core.Bounds;
import abfab3d.core.Color;

import abfab3d.grid.op.FontLoader;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.PT;

/**
 * Tests the functionality of Text2D
 *
 * @version
 */
public class TestText2D extends TestCase {

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestText2D.class);
    }

    int gridMaxAttributeValue = 127;

    public void testBitmapSize() {

        printf("testBitmapSize()\n");
        String text = "Shapeways";

        Text2D t = new Text2D(text);
        t.set("fontSize", 15);
        t.set("inset", 0.1*MM);

        t.initialize();
        BufferedImage image = t.getImage();
        try {
            ImageIO.write(image, "png", new File("/tmp/text.png"));
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void testTextWidth() {

        printf("testAlignment()\n");
        String text = "ySySySyS";
        Text2D t = new Text2D(text);
        t.set("height", 20*MM);
        t.set("inset", 0*MM);
        double w = t.getPreferredWidth();
        printf("calculated width: %7.2f mm\n", w/MM);

    }

    public void devTestTextAlign() throws Exception {

        printf("testAlignment()\n");
        //String text = "yShg";
        String text = "12yShg34";
        Text2D t = new Text2D(text);
        t.set("height", 10*MM);
        t.set("width", 30*MM);
        t.set("inset", 0*MM);
        t.set("text", text);
        t.set("voxelSize", 0.05*MM);
        BufferedImage img;
        

        //t.set("fit", "horizontal");
        t.set("fit", "vertical");
        //t.set("fit", "both");
        t.set("preserveAspect", true);
        //t.set("preserveAspect", false);

        t.set("horizAlign", "left");
        t.set("vertAlign", "top");
        img = t.getImage();
        ImageIO.write(img, "png", new File("/tmp/text_left_top.png"));

        t.set("horizAlign", "center");
        t.set("vertAlign", "center");
        img = t.getImage();
        ImageIO.write(img, "png", new File("/tmp/text_center_center.png"));

        t.set("horizAlign", "right");
        t.set("vertAlign", "bottom");
        img = t.getImage();
        ImageIO.write(img, "png", new File("/tmp/text_right_bottom.png"));
    }
 
    public void devTestTextNoFit() throws Exception {

        //String fontName = "Pinyon Script LatinOnly";
        String fontPath = "test/images/PinyonScript-LatinOnly_v3a.ttf";
        //String fontPath = "test/images/PinyonScript-Regular.ttf";
        //String fontPath = "test/images/berkshireswash-regular.ttf";
        //String fontName = "Times New Roman";

        double voxelSize = 0.05*MM;
        double fontSize = 10*MM;
        //String text = "CarrieJ";
        String text = "/\\.,-={}";
        double outlineWidth = 1*MM;
        double inset = 1*MM;

        //Text2D t = new Text2D(text, fontName, voxelSize);
        Text2D t = new Text2D(text, new FontLoader(fontPath), voxelSize);

        t.set("fit", "none");
        t.set("vertAlign", "center");
        t.set("fontSize", fontSize/PT);
        t.set("fillColor", new Color(0,0.3,0.));
        t.set("outlineColor", new Color(0.2,0.2,0.2));
        t.set("outline", new Boolean(true));
        t.set("outlineWidth", outlineWidth);
        t.set("kerning", true);
        t.set("inset", inset);
        t.setSpacing(0.03);
        BufferedImage img = t.getImage();
        ImageIO.write(img, "png", new File("/tmp/testTextNoFit.png")); 
        printf("textBounds (%s)mm\n", t.getTextBounds().toString(MM));
        printf("imageBounds (%s)mm\n", t.getImageBounds().toString(MM));
    }
 
    public void devTestAutoKerning() throws Exception {

        String fontPath = "test/images/PinyonScript-LatinOnly_v3a.ttf";

        double voxelSize = 0.05*MM;
        double fontSize = 10*MM;
        double outlineWidth = 1.*MM;
        double glyphDistance = 0.3*MM;
        double inset = 1*MM;
        //String text = "CarrieJ";
        String text = "/\\.,-={}";

        //Text2D t = new Text2D(text, fontName, voxelSize);
        Text2D t = new Text2D(text, new FontLoader(fontPath), voxelSize);

        t.set("inset", inset);
        t.set("fit", "none");
        t.set("autoKerning", true);
        t.set("vertAlign", "center");
        t.set("fontSize", fontSize/PT);
        t.set("fillColor", new Color(0,0,0.3));
        t.set("outlineColor", new Color(0.2,0.2,0.2));
        t.set("outline", new Boolean(true));
        t.set("outlineWidth", outlineWidth);
        t.set("kerning", true);
        t.set("autoKerningDistance",glyphDistance);
        BufferedImage img = t.getImage();
        ImageIO.write(img, "png", new File("/tmp/testAutoKernng.png")); 
        printf("textBounds (%s)mm\n", t.getTextBounds().toString(MM));
        printf("imageBounds (%s)mm\n", t.getImageBounds().toString(MM));
    }
 

    public void devTestGlyphPosition() throws Exception {

        String fontName = "Pinyon Script LatinOnly";
        //String fontPath = "test/images/PinyonScript-Regular.ttf";
        //String fontPath = "test/images/berkshireswash-regular.ttf";

        //String fontName = "Times New Roman";
        double voxelSize = 0.02*MM;
        double fontSize = 10*MM;
        String text = "Carrie";

        Text2D t = new Text2D(text, fontName, voxelSize);
        //Text2D t = new Text2D(text, new FontLoader(fontPath), voxelSize);

        t.set("fit", "none");
        t.set("vertAlign", "center");
        t.set("fontSize", fontSize/PT);
        t.set("fillColor", new Color(0,0,0,0.3));
        t.set("outlineColor", new Color(0.2,0.2,0.2));
        t.set("outline", new Boolean(true));
        t.set("outlineWidth", 0.6*MM);
        t.set("kerning", true);
        t.setSpacing(0.5);

        BufferedImage img = t.getImage();
        ImageIO.write(img, "png", new File("/tmp/testTextFit.png")); 
        Bounds imageBounds = t.getImageBounds();
        Bounds textBounds = t.getTextBounds();
        printf("text (%s)\n", text);
        printf("imageBounds:[%s]mm\n", imageBounds.toString(MM));
        printf(" textBounds:[%s]mm\n", textBounds.toString(MM));
        for(int i = 0; i < text.length(); i++){
            Point2D pnt = t.getGlyphPosition(i);
            Bounds gb = t.getGlyphBounds(i);
            printf("%c pos:[%5.1f,%5.1f]mm bounds:[%s]mm\n",text.charAt(i),pnt.getX()/MM,pnt.getY()/MM, gb.toString(MM));
             
        }        
   }
 
 
    public static void main(String[] args) throws Exception {
        //new TestText2D().testBitmapSize();
        //new TestText2D().testTextWidth();
        //new TestText2D().devTestTextAlign();
        new TestText2D().devTestTextNoFit();               
        new TestText2D().devTestAutoKerning();               
        // new TestText2D().devTestGlyphPosition();               
    }
}