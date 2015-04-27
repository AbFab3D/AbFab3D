/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Point2D;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;

import static abfab3d.util.Output.printf;

/**
 * 
 *
 * @author Vladimir Bulatov
 */
public class TextUtil {

    static final boolean DEBUG = true;

    /** Cache of available fonts to speed testing */
    static HashSet<String> fontCache = null;

    /**
     * Create an image from a text string.
     *
     * @param imageWidth The width of the image
     * @param imageHeight The height of the image
     * @param text The text string
     * @param font The font to use
     * @param insets margins to leave blank around the text 
     */
    public static BufferedImage createTextImage(int imageWidth, int imageHeight, String text, Font font, Insets insets) {
        return createTextImage(imageWidth, imageHeight, text, font, insets, true, 0.);        
    }

    /**
     * makes text bitmap of given height and automaticaly set with to accomodate text and insets
       makes text bitmap of given height and automaticaly set width to accomodate text and insets
     */
    public static BufferedImage createTextImage(int imageHeight, String text, Font font, Insets insets) {
        return createTextImage(imageHeight, text, font, insets, 0);
    }
    public static BufferedImage createTextImage(int imageHeight, String text, Font font, Insets insets, double spacing) {

        if(DEBUG)printf("createText(%d, %s; insets(%d, %d, %d, %d)) \n", imageHeight, text, insets.left,insets.top,insets.right,insets.bottom);
        // we need to find width of the image for given text height
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = (Graphics2D) image.getGraphics();
        TextLayout layout = new TextLayout(text, font, g.getFontRenderContext());
        Rectangle2D rect = layout.getBounds();
        double twidth = rect.getWidth();
        double theight = rect.getHeight();
        if (DEBUG) printf("textRect: [%f x %f]\n", twidth, theight);
        // rect size        
        double tx = twidth;
        double ty = theight;
        int imageWidth = (int)((imageHeight-(insets.top + insets.bottom)) * tx / ty) + (insets.left + insets.right);
        if(DEBUG)printf("image: %d x %d \n", imageWidth, imageHeight);
        
        return createTextImage(imageWidth,imageHeight, text,font, insets, true, spacing);
        
    }

    public static BufferedImage createTextImage(int imageWidth, int imageHeight, String text, Font font, Insets insets, boolean shrinkText) {
        return createTextImage(imageWidth, imageHeight, text, font, insets, shrinkText, 0.);                
    }

    /**
     * Create an image from a text string.
     *
     * @param imageWidth The width of the image
     * @param imageHeight The height of the image
     * @param text The text string
     * @param font The font to use
     * @param insets margins to leave blank around the text 
     * @param shrinkText shrink text (if true) to fit the rectangle
     */
    public static BufferedImage createTextImage(int imageWidth, int imageHeight, String text, Font font, Insets insets, boolean shrinkText, double spacing) {
        
        
        if(DEBUG)printf("createText(%d, %d, %s) \n", imageWidth, imageHeight, text);
        double fsize = font.getSize();
        int w = imageWidth - (insets.left + insets.right);
        int h = imageHeight - (insets.top + insets.bottom);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = (Graphics2D)image.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        char ctext[] = text.toCharArray();
        GlyphVector gv = font.layoutGlyphVector(g.getFontRenderContext(), ctext, 0, ctext.length, 0);
        //float pos[] = gv.getGlyphPosition(ctext.length);0, ctext.length, new float[ctext.length]);
        double space = spacing*gv.getGlyphPosition(ctext.length).getX()/ctext.length;        
        for(int i = 0; i < ctext.length; i++){
            Point2D pnt = gv.getGlyphPosition(i);
            Rectangle2D rect = gv.getGlyphVisualBounds(i).getBounds2D();
            //printf("pnt: %7.1f,%7.1f rect: [%7.1f,%7.1f %7.1f,%7.1f]\n", pnt.getX(),pnt.getY(),rect.getX(), rect.getY(),rect.getWidth(),rect.getHeight());
            gv.setGlyphPosition(i, new Point2D.Double(pnt.getX() + space*i, pnt.getY())); 
            //AffineTransform trans = AffineTransform.getRotateInstance(-i*Math.PI/6);
            //AffineTransform trans = getItalicTransform(-0.1, 0.0);            
            //gv.setGlyphTransform(i, trans);             
        }

        Rectangle2D rect = gv.getVisualBounds(); 
        printf("text bounds: [%5.1f %5.1f %5.1f %5.1f]\n ", rect.getX(),rect.getY(),rect.getWidth(), rect.getHeight());

        double twidth = rect.getWidth();
        double theight = rect.getHeight();
        double tx = rect.getX();
        double ty = rect.getY();

        // center the text and move to make insets         
        int x = (int)((w - twidth)/2 - tx) + insets.left;
        int y = (int)((h - theight)/2 - ty) + insets.top;
        
        g.setColor(Color.WHITE);        
        g.fillRect(0,0,imageWidth, imageHeight);


        g.setColor(Color.LIGHT_GRAY);        
        Rectangle2D labelRect = new Rectangle2D.Double(insets.left,insets.top, w, h);
        //g.draw(labelRect);
        Rectangle2D textRect = new Rectangle2D.Double(x + tx, y + ty, twidth, theight);
        AffineTransform at;
        if(shrinkText) {
            at = getRectTransform(textRect, labelRect, true, true);
        } else {
            
            if(textRect.getWidth() / textRect.getHeight() > labelRect.getWidth() / labelRect.getHeight()){
                System.err.printf("EXTMSG: Text is too long. It is being truncated\n");
            }
            at = getRectTransform2(textRect, labelRect);
        }

        g.setTransform(at);
        
        g.setColor(Color.BLACK);        
        g.drawGlyphVector(gv, x,y);

        return image;
    }


    public static BufferedImage _createTextImage(int imageWidth, int imageHeight, String text, Font font, Insets insets, boolean shrinkText) {
        
        
        if(DEBUG)printf("createText(%d, %d, %s) \n", imageWidth, imageHeight, text);
        double fsize = font.getSize();
        int w = imageWidth - (insets.left + insets.right);
        int h = imageHeight - (insets.top + insets.bottom);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = (Graphics2D)image.getGraphics();

        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        TextLayout layout = new TextLayout(text, font, g.getFontRenderContext());   
        Rectangle2D rect = layout.getBounds();         

        double twidth = rect.getWidth();
        double theight = rect.getHeight();
        double tx = rect.getX();
        double ty = rect.getY();

        // center the text and move to make insets         
        int x = (int)((w - twidth)/2 - tx) + insets.left;
        int y = (int)((h - theight)/2 - ty) + insets.top;
        
        //printf("font size: %4.1f rect: (%4.1f, %4.1f) [%4.1f x %4.1f] x:%d, y:%d\n", 
        //       fsize, rect.getX(), rect.getY(), rect.getWidth(),rect.getHeight(), x, y);

        g.setColor(Color.WHITE);        
        g.fillRect(0,0,imageWidth, imageHeight);


        g.setColor(Color.LIGHT_GRAY);        
        Rectangle2D labelRect = new Rectangle2D.Double(insets.left,insets.top, w, h);
        //g.draw(labelRect);
        Rectangle2D textRect = new Rectangle2D.Double(x + tx, y + ty, twidth, theight);
        AffineTransform at;
        if(shrinkText) {
            at = getRectTransform(textRect, labelRect, true, true);
        } else {
            
            if(textRect.getWidth() / textRect.getHeight() > labelRect.getWidth() / labelRect.getHeight()){
                System.err.printf("EXTMSG: Text is too long. It is being truncated\n");
            }
            at = getRectTransform2(textRect, labelRect);
        }

        g.setTransform(at);

        //g.setColor(Color.RED);        
        //g.fill(new Rectangle2D.Double(x+tx,y,twidth, 1));        

        //g.setColor(Color.BLUE);         
        //g.draw(textRect);
        
        g.setColor(Color.BLACK);        
        layout.draw(g, x, y);

        return image;
    }


    /**
     *  returns transformation, which transforms rectIn into rectOut
     *  optionally it preserves aspect ratio 
     */
    public static AffineTransform getRectTransform(Rectangle2D rectIn, 
                                                   Rectangle2D rectOut, 
                                                   boolean preserveAspectRatio,
                                                   boolean centerImage 
                                                   ){
        
        AffineTransform at = new AffineTransform();
        
        double sx = rectOut.getWidth() / rectIn.getWidth();
        double sy = rectOut.getHeight() / rectIn.getHeight();
        
        if(centerImage){
            at.translate(rectOut.getX()+rectOut.getWidth()/2, rectOut.getY()+rectOut.getHeight()/2); 
        } else {
            at.translate(rectOut.getX(), rectOut.getY());     
        }
        if(preserveAspectRatio){            
            if(sx >= sy){                
                at.scale(sy,sy);                
            } else {                
                at.scale(sx,sx);                
            }                        
        } else {
            
            at.scale(sx,sy);
            
        }
        if(centerImage){
            at.translate(-(rectIn.getX()+rectIn.getWidth()/2), -(rectIn.getY()+rectIn.getHeight()/2));        
        } else {
            at.translate(-rectIn.getX(), -rectIn.getY());
        }        
        return at;         
    }    

    /**
     * calculates transform which scales rectIn to fit vertically into rectOut (possible makes it larger in width)
     */
    public static AffineTransform getRectTransform2(Rectangle2D rectIn, 
                                                   Rectangle2D rectOut
                                                   ){
        
        AffineTransform at = new AffineTransform();
        
        double sy = rectOut.getHeight() / rectIn.getHeight();
        
        at.translate(rectOut.getX(), rectOut.getY());         

        at.scale(sy,sy);                
        
        at.translate(-rectIn.getX(), -rectIn.getY());
                
        return at;         
    }    

    static AffineTransform getItalicTransform(double x, double y) {

        AffineTransform tr=new AffineTransform();
        //tr.translate(0, s);
        tr.shear(-x, y);
        //tr.translate(2,-s);
        return tr;
    }

    public static boolean fontExists(String fontName) {
        if (fontCache == null)  {
            GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fonts = g.getAvailableFontFamilyNames();
            HashSet<String> fc =  new HashSet<String>(fonts.length);

            System.out.println("Caching Fonts:");
            int len = fonts.length;
            for (int i = 0; i < len; i++) {
                System.out.println(fonts[i]);
                fc.add(fonts[i]);
            }

            fontCache = fc;
        }

        return fontCache.contains(fontName);
    }

    public static String[] getFontsInstalled() {
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = g.getAvailableFontFamilyNames();
        return fonts;
    }
}