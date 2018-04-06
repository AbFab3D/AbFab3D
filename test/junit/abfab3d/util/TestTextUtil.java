/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

// External Imports
import javax.imageio.ImageIO;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Hashtable;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.AffineTransform;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.font.TextAttribute;
import java.awt.font.GlyphVector;
import java.awt.Graphics2D;

import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 */
public class TestTextUtil extends TestCase {
    public static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTextUtil.class);
    }

    public void testRenderText() throws Exception{

        String text = "0jlg";
        int width = 500;
        int height = 100;
        Font font = new Font("Times New Roman", Font.PLAIN, 10);
        Insets2 insets = new Insets2(1,1,1,1);
        double spacing = -0.1;
        boolean aspect = true;
        //int fitStyle = TextUtil.FIT_HORIZONTAL;
        //int fitStyle = TextUtil.FIT_VERTICAL;
        int fitStyle = TextUtil.FIT_BOTH;
        
        int valign[] = new int[]{TextUtil.ALIGN_TOP,TextUtil.ALIGN_CENTER,TextUtil.ALIGN_BOTTOM};
        int halign[] = new int[]{TextUtil.ALIGN_LEFT,TextUtil.ALIGN_CENTER,TextUtil.ALIGN_RIGHT};
        String haligns[] = new String[]{"left","center", "right"};
        String valigns[] = new String[]{"top","center", "bottom"};

        BufferedImage img;
        for(int m = 0; m < 3; m++){
            for(int k = 0;k < 3; k++){
                img = TextUtil.createTextImage(width, height, text, font, spacing, insets,  aspect, fitStyle, halign[k], valign[m]);
                if (DEBUG) ImageIO.write(img, "png", new File("/tmp/text_"+valigns[m] + "_" + haligns[k] + ".png"));
            }
        }
    } 

    public void devTestRenderOutline() throws Exception{
        
        //String text = "Carriej";
        //String text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String text = "ABCDE";
        //String text = "Carrie";
        int width = 1800;
        int height = 400;
        //Font font = new Font("Times New Roman", Font.PLAIN, 10);
        Font font = new Font("Pinyon Script LatinOnly", Font.PLAIN, 100);
        //Font font = new Font("Arial", Font.PLAIN, 100);

        double strokeWidth = 30;
        //double strokeWidth = 30;
        double ins = strokeWidth/2 + 1;
        Insets2 insets = new Insets2(ins,ins,ins,ins);
        double spacing = -0.01;
        boolean aspect = true;
        int fitStyle = TextUtil.FIT_BOTH;
        
        int valign = TextUtil.ALIGN_CENTER;
        int halign = TextUtil.ALIGN_CENTER;

        BufferedImage img;
        //img = TextUtil.createTextImage(width, height, text, font, spacing, insets,  aspect, fitStyle, halign, valign);
        img = TextUtil.createTextImageOutline(width, height, text, font, spacing, insets,  aspect, fitStyle, halign, valign, strokeWidth, false);
        ImageIO.write(img, "png", new File("/tmp/text_outline_kerning_off.png"));
        img = TextUtil.createTextImageOutline(width, height, text, font, spacing, insets,  aspect, fitStyle, halign, valign, strokeWidth, true);
        ImageIO.write(img, "png", new File("/tmp/text_outline_kerning_on.png"));
        
    }

    public void devTestKerning() throws Exception{
        
        String str1 = "A";
        String str2 = "a";
        int len1 = str1.length();
        int len2 = str2.length();
        Font font = new Font("Pinyon Script LatinOnly", Font.PLAIN, 100);
        for(int i = 0; i < len1; i++){
            for(int j = 0; j < len2; j++){
                char c1 = str1.charAt(i);
                char c2 = str2.charAt(j);
                String text = fmt("%c%c", c1, c2);
                printf("(%d, %d): %s \n",i,j, text);
                
                //String text = "Carrie";
                int width = 1000;
                int height = 400;
                double strokeWidth = 30;
                //double strokeWidth = 30;
                double ins = strokeWidth/2 + 1;
                Insets2 insets = new Insets2(ins,ins,ins,ins);
                double spacing = -0.01;
                boolean aspect = true;
                int fitStyle = TextUtil.FIT_BOTH;
                
                int valign = TextUtil.ALIGN_CENTER;
                int halign = TextUtil.ALIGN_CENTER;
                
                BufferedImage img;
                img = TextUtil.createTextImageOutline(width, height, text, font, spacing, insets,  aspect, fitStyle, halign, valign, strokeWidth, true);
                ImageIO.write(img, "png", new File(fmt("/tmp/kerning/%s_%s.png",getName(c1),getName(c2))));
            }
        }
    }

    public void devTestKerning2() throws Exception{
        
        //String str1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn";
        String str1 = "opqrstuvwxyz";
        //        String str1 = "ABCDEFGHIJKLMNOPQRSTUVWZYZ";
        //String str2 = "abcdefghijklmnopqrstuvwxyz";
        String str2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int len1 = str1.length();
        int len2 = str2.length();
        int fontSize = 200;
        double strokeWidth = fontSize*0.09;
        double spacing = -0.0;      
        double cellX = fontSize*2.5;
        double cellY = fontSize*1.2;
        double x0 = fontSize*0.2;
        double y0 = fontSize*0.8;
        
        String fontName = "Pinyon Script LatinOnly";
        //String fontName = "Pinyon Script";
        Font font = new Font(fontName, Font.PLAIN, fontSize);
        Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        font = font.deriveFont(map);

        


        for(int i = 0; i < len1; i++){
            
            int imageWidth = (int)(cellX);
            int imageHeight = (int)(str2.length()*cellY);
            
            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D)image.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);        
            g.fillRect(0,0,imageWidth, imageHeight);

            char c1 = str1.charAt(i);
            printf("%c \n",c1);                

            for(int j = 0; j < len2; j++){
                char c2 = str2.charAt(j);
                String text = fmt("%c%c", c1, c2);
                double x = x0;
                double y = y0 + j * cellY;
                TextUtil.renderText(g, font, text, x,y, spacing, strokeWidth);
            }
            ImageIO.write(image, "png", new File(fmt("/tmp/kerning/%s_(%2d).png",fontName, (int)c1)));
        }
    }
    
    static String getName(char c){
        if(c >= 'A' && c <= 'Z') return "c"+c+"";
        if(c >= 'a' && c <= 'z') return "s"+c+"";
        return "" + c;
    }

    
    public void devTestLineMetrics() throws Exception{
        
        
        String fontName = "Pinyon Script LatinOnly";
        String fontFile = "test/images/PinyonScript-Regular.ttf";
        //String fontName = "Times New Roman";
        //String fontName = "Arial";
        //String text = "Carrje";
        String text = "first line";
        String text2 = "Second line";
        
        Font font;
        
        font = Font.createFont(Font.TRUETYPE_FONT,new File(fontFile));
        //font = new Font(fontName, Font.PLAIN, 10);
                
        double fontSize = 100.;
        double lineSpacing = 1.5*fontSize;

        //font = font.deriveFont((float)fontSize);

        Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        map.put(TextAttribute.SIZE, new Double(fontSize));        
        font = font.deriveFont(map);
        printf("font:%s\n", font.toString());
        int imageWidth = 800;
        int imageHeight = (int)(fontSize*4);

        double textX = fontSize;
        double textY = fontSize*1.5;
        
        
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        FontRenderContext frc = g.getFontRenderContext();
        char ctext[] = text.toCharArray();
        GlyphVector gv = font.layoutGlyphVector(frc, ctext, 0, ctext.length, 0);
        
        Rectangle2D textRect = gv.getVisualBounds(); 
        printf("text bounds: [x:%5.1f y:%5.1f w:%5.1f h:%5.1f]\n ", textRect.getX(),textRect.getY(),textRect.getWidth(), textRect.getHeight());

        LineMetrics metrics = font.getLineMetrics(text, frc);
        printf("text: %s\n",text);

        double ascent = metrics.getAscent();
        double descent = metrics.getDescent();
        double textHeight = metrics.getHeight();

        printf(" ascent: %5.1f\n",ascent);
        printf("descent: %5.1f\n", descent);
        printf(" height: %5.1f\n", textHeight);

        
        //printf("leading: %5.1f\n", metrics.getLeading());
        //printf("StrikethroughOffset: %5.1f\n", metrics.getStrikethroughOffset());
        //printf("StrikethroughThickness: %5.1f\n", metrics.getStrikethroughThickness());
        //printf("UnderlineOffset: %5.1f\n", metrics.getUnderlineOffset());
        //printf("UnderlineThickness: %5.1f\n", metrics.getUnderlineThickness());
        //printf("baselineOffsets[CENTER]: %5.1f\n", metrics.getBaselineOffsets()[Font.CENTER_BASELINE]);
        //printf("baselineOffsets[ROMAN]: %5.1f\n", metrics.getBaselineOffsets()[Font.ROMAN_BASELINE]);
        //printf("baselineOffsets[HANGING]: %5.1f\n", metrics.getBaselineOffsets()[Font.HANGING_BASELINE]);
        //printf("baselineIndex: %d\n", metrics.getBaselineIndex());
        Shape baseLine = new Line2D.Double(-imageWidth,0,imageWidth,0);
        Shape ascentLine = new Line2D.Double(-imageWidth,-ascent,imageWidth,-ascent);
        Shape descentLine = new Line2D.Double(-imageWidth,descent,imageWidth,descent);
        double dotS = 5;
        Shape textOrigin = new Ellipse2D.Double(-dotS,-dotS,2*dotS,2*dotS);

        AffineTransform at2 = AffineTransform.getTranslateInstance(0,lineSpacing);

        g.setColor(Color.WHITE);        
        g.fill(new Rectangle2D.Double(0,0, imageWidth, imageHeight));
        g.translate(textX, textY);

        g.setColor(Color.ORANGE);                
        g.draw(baseLine);
        g.draw(at2.createTransformedShape(baseLine));

        g.setColor(Color.LIGHT_GRAY);                
        g.draw(ascentLine);
        g.draw(at2.createTransformedShape(ascentLine));

        g.setColor(Color.GREEN);                
        g.draw(descentLine);
        g.draw(at2.createTransformedShape(descentLine));

        g.setColor(Color.BLUE);
        g.draw(textRect);
        
        g.setColor(Color.RED);        
        g.draw(textOrigin);
        g.draw(at2.createTransformedShape(textOrigin));

        g.setColor(Color.BLACK);        
        g.drawGlyphVector(gv, 0,0);

        GlyphVector gv2 = font.layoutGlyphVector(frc, text2.toCharArray(), 0, text2.length(), 0);
        Rectangle2D textRect2 = gv2.getVisualBounds(); 

        g.drawGlyphVector(gv2, 0,(float)lineSpacing);

        g.setColor(Color.BLUE);        
        g.draw(at2.createTransformedShape(textRect2));
        
        ImageIO.write(image, "png", new File("/tmp/testTextMetrix.png"));

    }

    public static void main(String arg[]) throws Exception {


        //new TestTextUtil().testRenderText();
        //new TestTextUtil().devTestRenderOutline();
        //new TestTextUtil().devTestKerning();
        //new TestTextUtil().devTestKerning2();
        new TestTextUtil().devTestLineMetrics();
        
    }
    
}
