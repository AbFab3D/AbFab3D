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

package abfab3d.datasources;

import java.awt.font.OpenType;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
//import sun.font.Font2D;
//import sun.font.ScriptRun;
//import sun.font.Script;
//import sun.font.FontUtilities;

// External Imports
import javax.imageio.ImageIO;

//import java.lang.reflect.Class;
import java.lang.reflect.Method;

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

import abfab3d.util.Insets2;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;

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
        //String str1 = "#W";
        String str1 = "$";
        //String str1 = "opqrstuvwxyz";
        //        String str1 = "ABCDEFGHIJKLMNOPQRSTUVWZYZ";
        //String str2 = "abcdefghijklmnopqrstuvwxyz";
        String str2 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        //String str2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int len1 = str1.length();
        int len2 = str2.length();
        int fontSize = 200;
        double strokeWidth = fontSize*0.09;
        double spacing = -0.0;      
        double cellX = fontSize*2.5;
        double cellY = fontSize*1.2;
        double x0 = fontSize*0.2;
        double y0 = fontSize*0.8;
        //String fontPath = "test/images/PinyonScript-Regular.ttf";
        //String fontPath = "test/images/PinyonScript-LatinOnly_v3.ttf";
        String fontPath = "test/images/PinyonScript-LatinOnly_v3a.ttf";
        //String fontName = "Pinyon Script LatinOnly";
        String fontName = "Pinyon Script LatinOnly v2";
        //String fontName = "Berkshire Swash v2";
        //String fontName = "Pinyon Script";
        //Font font = new Font(fontName, Font.PLAIN, fontSize);
        Font font =Font.createFont(Font.TRUETYPE_FONT,new File(fontPath));

        Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        map.put(TextAttribute.SIZE, new Double(fontSize));        
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

    public void devTestKerning3() throws Exception{
        
        String str1 = "+";
        // "#$@&*?!<>0123456789+ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        // %()/|\[]{}~^_-
        String str2 = "!\"#$%&'()*+,-./0123456789:;<>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        int len1 = str1.length();
        int len2 = str2.length();
        int fontSize = 200;
        double strokeWidth = fontSize*0.09;
        double spacing = 0.0;      
        double cellX = fontSize*4;
        double cellY = fontSize*1.2;
        double x0 = fontSize*1.0;
        double y0 = fontSize*0.8;
        String fontPath = "test/images/PinyonScript-LatinOnly_v3a.ttf";
        String fontName = "PinyonScript-LatinOnly_v3a";
        Font font =Font.createFont(Font.TRUETYPE_FONT,new File(fontPath));

        Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        map.put(TextAttribute.SIZE, new Double(fontSize));        
        font = font.deriveFont(map);

        for(int i = 0; i < len1; i++){
            
            int imageWidth = (int)(cellX);
            int imageHeight = (int)(str2.length()*cellY);
            
            BufferedImage image1 = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            BufferedImage image2 = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g1 = (Graphics2D)image1.getGraphics();
            g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g1.setColor(Color.WHITE);        
            g1.fillRect(0,0,imageWidth, imageHeight);
            Graphics2D g2 = (Graphics2D)image2.getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);        
            g2.fillRect(0,0,imageWidth, imageHeight);

            char c1 = str1.charAt(i);
            printf("%c \n",c1);                

            for(int j = 0; j < len2; j++){
                char c2 = str2.charAt(j);
                String text1 = fmt("%c%c", c1, c2);
                String text2 = fmt("%c%c", c2, c1);
                double x = x0;
                double y = y0 + j * cellY;
                TextUtil.renderText(g1, font, text1, x,y, spacing, strokeWidth);
                TextUtil.renderText(g2, font, text2, x,y, spacing, strokeWidth);
            }
            ImageIO.write(image1, "png", new File(fmt("/tmp/kerning/%s_(%2d)_1.png",fontName, (int)c1)));
            ImageIO.write(image2, "png", new File(fmt("/tmp/kerning/%s_(%2d)_2.png",fontName, (int)c1)));

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


    void devTestOpenType() throws Exception{

        //Font font = Font.createFont(Font.TRUETYPE_FONT, new File("test/images/PinyonScript-Regular.ttf"));
        Font font = Font.createFont(Font.TRUETYPE_FONT, new File("test/images/PinyonScript-LatinOnly_v2.otf"));

        //Font font = new Font("Pinyon Script LatinOnly", Font.PLAIN, 100);
        printf("font: %s\n",font);
        Class c = font.getClass();
        Method meth[] = c.getDeclaredMethods();
        for(int i = 0; i < meth.length; i++){
            printf("method: %s\n", meth[i]);
        }
        Class[] inter = c.getInterfaces();
        for(int i = 0; i < inter.length; i++){
            printf("inter: %s\n", inter[i]);
        }
        if (font instanceof OpenType) {
            printf("openType: %s\n", (OpenType)font);           
        }                

        //Font2D f2 = FontUtilities.getFont2D(font);
        
        //sun.font.StrikeMetrics


    }


    void devTestScriptRuns(){
        /*
        char[] text = "#@abcdABCD123".toCharArray();
        ScriptRun scriptRun = new ScriptRun(text, 0, text.length);
        
        while (scriptRun.next()) {
            int start  = scriptRun.getScriptStart();
            int limit  = scriptRun.getScriptLimit();
            int script = scriptRun.getScriptCode();
            
            System.out.println("Script \"" + script + "\" from " +
                               start + " to " + limit + ".");
        }
        */
    }

    static void printKerningPair(String glyphA[],String glyphB[]){

        for(int i = 0; i < glyphA.length; i++){
            for(int j = 0; j < glyphB.length; j++){
                printf(" pos %s %s <0> <0>;\n",glyphA[i],glyphB[j]);
            }            
        }
    }

    void devTestPrintKerningTable(){
    
        String symbols[] = {
            "exclam",
            "quotedbl",
            "numbersign",
            "dollar",
            "percent",
            "ampersand",
            "quotesingle",
            "parenleft",
            "parenright",
            "asterisk",
            "plus",
            "comma",
            "hyphen",
            "period",
            "slash",
            "zero",
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine",
            "colon",
            "semicolon",
            "less",
            "equal",
            "greater",
            "question",
            "at",
            "bracketleft",
            "backslash",
            "bracketright",
            "asciicircum",
            "underscore",
            "grave",
            "braceleft",
            "bar",
            "braceright",
            "asciitilde"};
        String letter1[] = {
            "@KernClass_A",
            "@KernClass_B",
            "@KernClass_C",
            "@KernClass_D",
            "@KernClass_E",
            "@KernClass_F",
            "@KernClass_G",
            "@KernClass_H",
            "@KernClass_I",
            "@KernClass_J",
            "@KernClass_K",
            "@KernClass_L",
            "@KernClass_M",
            "@KernClass_N",
            "@KernClass_O",
            "@KernClass_P",
            "@KernClass_Q",
            "@KernClass_R",
            "@KernClass_S",
            "@KernClass_T",
            "@KernClass_U",
            "@KernClass_V",
            "@KernClass_W",
            "@KernClass_X",
            "@KernClass_Y",
            "@KernClass_Z",
            "@KernClass_a1",
            "@KernClass_b",
            "@KernClass_c",
            "@KernClass_d",
            "@KernClass_e",
            "@KernClass_f",
            "@KernClass_g",
            "@KernClass_h1",
            "@KernClass_i",
            "@KernClass_j",
            "@KernClass_k",
            "@KernClass_l",
            "@KernClass_m",
            "@KernClass_o",
            "@KernClass_p",
            "@KernClass_q",
            "@KernClass_r",
            "@KernClass_s",
            "@KernClass_t",
            "@KernClass_u",
            "@KernClass_v",
            "@KernClass_w",
            "@KernClass_x",
            "@KernClass_y",
            "@KernClass_z"};            
        String letter2[] = {
            "@KernClass_A",
            "@KernClass_B",
            "@KernClass_C",
            "@KernClass_D",
            "@KernClass_E",
            "@KernClass_F",
            "@KernClass_G",
            "@KernClass_H",
            "@KernClass_I",
            "@KernClass_J",
            "@KernClass_K",
            "@KernClass_L",
            "@KernClass_M",
            "@KernClass_N",
            "@KernClass_O",
            "@KernClass_P",
            "@KernClass_Q",
            "@KernClass_R",
            "@KernClass_S",
            "@KernClass_T",
            "@KernClass_U",
            "@KernClass_V",
            "@KernClass_W",
            "@KernClass_X",
            "@KernClass_Y",
            "@KernClass_Z",
            "@KernClass_a",
            "@KernClass_b",
            "@KernClass_c",
            "@KernClass_d",
            "@KernClass_e",
            "@KernClass_f",
            "@KernClass_g",
            "@KernClass_h",
            "@KernClass_i",
            "@KernClass_j",
            "@KernClass_l",
            "@KernClass_m",
            "@KernClass_o",
            "@KernClass_p",
            "@KernClass_q",
            "@KernClass_r",
            "@KernClass_s",
            "@KernClass_t",
            "@KernClass_u",
            "@KernClass_v",
            "@KernClass_w",
            "@KernClass_x",
            "@KernClass_y",
            "@KernClass_z"};
            
        printKerningPair(symbols,symbols);
        printKerningPair(symbols,letter2);
        printKerningPair(letter1,symbols);

    }
        
    void devTestAutoKerning() throws Exception {

        //String text = "WW+1jJ1";
        //String text = "N\\-431415";
        //String text = "00110011";
        //String text = "34";
        //String text = "67"; // no intersection 
        //String text = "W..+=+!|7!Jy";
        //String text = ".-=`\"";
        //String text = "/Ja";
        //String text = "...Ja";
        String text = ".-..Ja";
        //String text = "ABC.\u0410\u0411\u0412\u0413\u0414\u0415\u0416"; 
        //String text = "W.W.Б";
        //String text = "/\\";
        double fontSize = 400;
        
        int imageWidth = (int)(fontSize*text.length()*1.5);
        int imageHeight = (int)(fontSize*1.2);
        
        double glyphSpacing = 0.*fontSize;
        double resolution = 1;
        double textX = 0.*fontSize;
        double textY = fontSize*0.8;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D textGraphics = (Graphics2D)image.getGraphics();
        textGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);        
        textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);        
        textGraphics.setColor(Color.white);
        textGraphics.fill(new Rectangle2D.Double(0,0,imageWidth, imageHeight));
        AffineTransform oldTransform = textGraphics.getTransform();
        
        String fontPath = "test/images/PinyonScript-LatinOnly_v3a.ttf";
        //String fontPath = "test/images/times.ttf";
        Font font = Font.createFont(Font.TRUETYPE_FONT,new File(fontPath));

        double glyphLocations[] = new double[text.length()];


        GlyphVector gv = new AutoKerning().layoutGlyphVector(textGraphics, font, text, fontSize, glyphSpacing, resolution);

       if (DEBUG) ImageIO.write(image, "png", new File("/tmp/03_originalText.png"));
       Rectangle2D grect = gv.getVisualBounds();       
       printf("grect: %s\n", grect);
       int twidth = (int)ceil(grect.getWidth());
       int theight = (int)ceil(grect.getHeight());

       BufferedImage timage = new BufferedImage(twidth, theight, BufferedImage.TYPE_INT_ARGB);
       Graphics2D tg = (Graphics2D)timage.getGraphics();
       tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);        
       tg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);        
       tg.setColor(Color.white);
       tg.fill(new Rectangle2D.Double(0,0,twidth, theight));
       
       tg.setColor(Color.black);
       tg.drawGlyphVector(gv, -(float)grect.getX(), -(float)grect.getY());
       
       if (DEBUG) ImageIO.write(timage, "png", new File("/tmp/04_packedText.png"));

    }

    static String getCyrillicString() {
        String s = "";
        for(char ch = 0x0410; ch<= 0x0416; ch++)
            s += ch;        
        return s;
        
    }
    void devTestCyrillic() throws Exception {


        String s = "";
        for(char ch=0x0410; ch<=0x0412; ch++)
            s += ch;
        s = new String(s.getBytes("UTF-8"), "UTF-8");
        //System.out.println(s); 
        
        String t = "\u0410\u0411\u0412";
        t = new String(t.getBytes("UTF-8"), "UTF-8");

        for(int i = 0; i < t.length(); i++){
            printf("char[%2d]: 0x%x 0x%x \n",i, (int)s.charAt(i), (int)t.charAt(i));
        }
    }

    public static void main(String arg[]) throws Exception {


        //new TestTextUtil().testRenderText();
        //new TestTextUtil().devTestRenderOutline();
        //new TestTextUtil().devTestKerning();
        //new TestTextUtil().devTestKerning2();
        //new TestTextUtil().devTestKerning3();
        //new TestTextUtil().devTestPrintKerningTable();
        //new TestTextUtil().devTestLineMetrics();
        //new TestTextUtil().devTestOpenType();
        //new TestTextUtil().devTestScriptRuns();
        new TestTextUtil().devTestAutoKerning();
        //new TestTextUtil().devTestCyrillic();
        
    }
    
}
