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
import static abfab3d.util.Output.fmt;

/**
 * 
 *
 * @author Vladimir Bulatov
 */
public class TextUtil {

    static final boolean DEBUG = false;

    static public final int ALIGN_LEFT = 0, ALIGN_RIGHT = 1, ALIGN_CENTER = 2, ALIGN_TOP=3, ALIGN_BOTTOM=4;
    static public final int FIT_VERTICAL = 0, FIT_HORIZONTAL=1, FIT_BOTH=2;

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
    public static BufferedImage createTextImage(int imageWidth, int imageHeight, String text, Font font, Insets2 insets) {
        return createTextImage(imageWidth, imageHeight, text, font, 0., insets, true, FIT_BOTH, ALIGN_LEFT, ALIGN_TOP);        
    }

    public static BufferedImage createTextImage(int imageWidth, int imageHeight, String text, Font font, Insets2 insets, boolean shrinkText) {
        return createTextImage(imageWidth, imageHeight, text, font, 0., insets, true, FIT_BOTH, ALIGN_LEFT, ALIGN_TOP);                
    }

    /**
     * makes text bitmap of given height and automaticaly set with to accomodate text and insets
       makes text bitmap of given height and automaticaly set width to accomodate text and insets
     */
    public static BufferedImage createTextImage(int imageHeight, String text, Font font, double spacing, Insets2 insets) {
        int imageWidth = (int)getTextWidth(imageHeight, text, font, spacing, insets);
        return createTextImage(imageWidth, imageHeight, text, font, spacing, insets, true, FIT_BOTH, ALIGN_LEFT, ALIGN_TOP);
    }
    /*
    public static BufferedImage createTextImage(int imageHeight, String text, Font font, Insets2 insets, double spacing) {

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
        double imageWidth = ((imageHeight-(insets.top + insets.bottom)) * tx / ty) + (insets.left + insets.right);
        if(DEBUG)printf("image: %d x %d \n", imageWidth, imageHeight);
        
        return createTextImage(imageWidth,imageHeight, text,font, spacing, insets, true, FIT_BOTH, ALIGN_LEFT,ALIGN_TOP);
        
    }
    */
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
    public static BufferedImage createTextImage(int imageWidth, int imageHeight, String text, Font font, Insets2 insets, boolean shrinkText, double spacing) {
        return createTextImage(imageWidth, imageHeight, text, font, spacing, insets, true, FIT_BOTH, ALIGN_LEFT,ALIGN_TOP); 
    }

    /**
     * Create an image from a text string.
     *
     * @param imageWidth The width of the image
     * @param imageHeight The height of the image
     * @param text The text string
     * @param font The font to use
     * @param spacing extra spacing between characters (in relative units)
     * @param insets margins to leave blank around the text 
     * @param preserveAspect  if true preserves text aspect ratio 
     
     * @param fitStyle fitting text to into rectangle (FIT_VERTICAL, FIT_HORIZONTAL, FIT_BOTH)
     * @param halignment horizontal text alignment (ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT) 
     * @param valignment horizontal text alignment (ALIGN_TOP, ALIGN_CENTER, ALIGN_BOTTOM) 
     */
    public static BufferedImage createTextImage(int imageWidth, int imageHeight, String text, Font font,  double spacing, Insets2 insets, boolean preserveAspect, int fitStyle, int halignment, int valignment) {
        
        
        if(DEBUG)printf("createText(%d, %d, %s) \n", imageWidth, imageHeight, text);
        double fsize = font.getSize();
        int renderWidth = (int)(imageWidth - (insets.left + insets.right));
        int renderHeight = (int)(imageHeight - (insets.top + insets.bottom));

        if (renderWidth < 0) {
            throw new IllegalArgumentException("TextUtil: renderWidth < 0");
        }

        if (renderHeight < 0) {
            throw new IllegalArgumentException("TextUtils: renderHeight < 0");
        }

        //BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        char ctext[] = text.toCharArray();
        GlyphVector gv = font.layoutGlyphVector(g.getFontRenderContext(), ctext, 0, ctext.length, 0);
        double space = spacing*gv.getGlyphPosition(ctext.length).getX()/ctext.length;        
        for(int i = 0; i < ctext.length; i++){
            Point2D pnt = gv.getGlyphPosition(i);
            Rectangle2D rect = gv.getGlyphVisualBounds(i).getBounds2D();
            gv.setGlyphPosition(i, new Point2D.Double(pnt.getX() + space*i, pnt.getY())); 
        }

        Rectangle2D textRect = gv.getVisualBounds(); 
        if(DEBUG)printf("textRect: [x:%5.1f y:%5.1f w:%5.1f h:%5.1f]\n ", textRect.getX(),textRect.getY(),textRect.getWidth(), textRect.getHeight());
        
        g.setColor(Color.WHITE);        
        g.fillRect(0,0,imageWidth, imageHeight);


        g.setColor(Color.LIGHT_GRAY);        
        Rectangle2D labelRect = new Rectangle2D.Double(insets.left,insets.top, renderWidth, renderHeight);
        AffineTransform at;
        
        at = getRectTransformAligned(textRect, labelRect, preserveAspect, fitStyle, halignment, valignment);

        g.setTransform(at);
        
        if(false){
            g.setColor(Color.LIGHT_GRAY);        
            g.fill(textRect);
            g.setColor(Color.RED);        
            g.draw(textRect);
        }

        g.setColor(Color.BLACK);        
        g.drawGlyphVector(gv, 0,0);

        return image;
    }


    /**
     *  returns transformation, which transforms rectIn into rectOut
     *  optionally it preserves aspect ratio 
     */
    /*
    public static AffineTransform getRectTransform(Rectangle2D rectIn, 
                                                   Rectangle2D rectOut, 
                                                   boolean preserveAspectRatio,
                                                   int alignment
                                                   ){
        
        AffineTransform at = new AffineTransform();
        
        double sx = rectOut.getWidth() / rectIn.getWidth();
        double sy = rectOut.getHeight() / rectIn.getHeight();
        double scale = (sx >= sy)? sx : sy;

        switch(alingment){
        case ALIGN_CENTER:
            at.translate(rectOut.getX()+rectOut.getWidth()/2, rectOut.getY()+rectOut.getHeight()/2); 
            break;
        case ALIGN_LEFT:
            at.translate(rectOut.getX(), rectOut.getY());     
            break;
        case ALIGN_RIGHT:
            at.translate(rectOut.getX()+(wout - win1), rectOut.getY());     
            break;
        }

        if(preserveAspectRatio){            
            at.scale(scale, scale);                
        } else {            
            at.scale(sx,sy);            
        }

        at.translate(-rectIn.getX(), -rectIn.getY());
                
        return at;         
    }    
    */

    /**
     * calculates transform which scales rectIn to fit vertically into rectOut possible makes it larger in width with given aligment 
     */
    public static AffineTransform getRectTransformAligned(Rectangle2D rectIn, 
                                                          Rectangle2D rectOut,
                                                          boolean preserveAspect, 
                                                          int fitStyle, 
                                                          int halignment, 
                                                          int valignment
                                                   ){
        
        AffineTransform at = new AffineTransform();
        // scale factor to fit vertically
        double sx = rectOut.getWidth() / rectIn.getWidth();
        double sy = rectOut.getHeight() / rectIn.getHeight();

        double scalex, scaley; // actual scaling to be used 
        if(preserveAspect) {
            // uniform scale to be used, we scale to fit one dimension only 
            switch(fitStyle){
            default:
            case FIT_BOTH:
                if(sx >= sy){
                    // select smaller scale factor
                    scalex = sy;
                    scaley = sy;
                } else {
                    scalex = sx;
                    scaley = sx;
                }
                break;
            case FIT_VERTICAL:
                scalex = sy;
                scaley = sy;                
                break;
            case FIT_HORIZONTAL:
                scalex = sx;
                scaley = sx;                
                break;
            } 
        } else { // non uniform scaling - fits both dimensions 
            scalex = sx;
            scaley = sy;                            
        }

        
        double wout = rectOut.getWidth();
        double win1 = rectIn.getWidth() * scalex; // width of scaled rectIn
        
        double hout = rectOut.getHeight();
        double hin1 = rectIn.getHeight() * scaley; // height of scaled rectIn
        
        double dx, dy;
        switch(halignment){
        default:
        case ALIGN_LEFT:
            dx = 0; break;
        case ALIGN_RIGHT:
            dx = (wout - win1); break;
        case ALIGN_CENTER:
            dx = (wout - win1)/2; break;
        }

        switch(valignment){
        default:
        case ALIGN_TOP:
            dy = 0; break;
        case ALIGN_BOTTOM:
            dy = (hout - hin1); break;
        case ALIGN_CENTER:
            dy = (hout - hin1)/2; break;
        }

        at.translate(rectOut.getX()+dx, rectOut.getY()+dy);         

        at.scale(scalex,scaley);          
        
        at.translate(-rectIn.getX(), -rectIn.getY());
                
        return at;         
    }    

    /**
       calculates text box width for given height
     */
    public static double getTextWidth(double height, String text, Font font, double spacing, Insets2 insets) {

        // we need to find width of the image for given text height
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = (Graphics2D) image.getGraphics();
        TextLayout layout = new TextLayout(text, font, g.getFontRenderContext());
 
        char ctext[] = text.toCharArray();
        GlyphVector gv = font.layoutGlyphVector(g.getFontRenderContext(), ctext, 0, ctext.length, 0);
        double space = spacing*gv.getGlyphPosition(ctext.length).getX()/ctext.length;        
        for(int i = 0; i < ctext.length; i++){
            Point2D pnt = gv.getGlyphPosition(i);
            Rectangle2D rect = gv.getGlyphVisualBounds(i).getBounds2D();
            gv.setGlyphPosition(i, new Point2D.Double(pnt.getX() + space*i, pnt.getY())); 
        }

        Rectangle2D rect = gv.getVisualBounds(); 
        printf("text bounds: [%5.1f %5.1f %5.1f %5.1f]\n ", rect.getX(),rect.getY(),rect.getWidth(), rect.getHeight());

        // rect size        
        double tx = rect.getWidth();
        double ty = rect.getHeight();
        double textHeight = (height-(insets.top + insets.bottom));
        if(textHeight <= 0)
            throw new IllegalArgumentException(fmt("negative textHeight: %7.3f  height: %7.3f insets.top:  %7.3f  insets.bottom: %7.3f", textHeight, height, insets.top, insets.bottom));
        double textWidth = textHeight * tx / ty + (insets.left + insets.right);

        return textWidth;
   }
    
    static AffineTransform getItalicTransform(double x, double y) {

        AffineTransform tr = new AffineTransform();
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

            if(DEBUG)System.out.println("Caching Fonts:");
            int len = fonts.length;
            for (int i = 0; i < len; i++) {
                if(DEBUG)System.out.println(fonts[i]);
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