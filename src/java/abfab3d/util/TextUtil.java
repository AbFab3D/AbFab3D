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

import javax.imageio.ImageIO;
import java.io.File;

import java.awt.image.BufferedImage;
import java.awt.geom.Point2D;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.font.TextAttribute;
import java.awt.font.FontRenderContext;
import java.awt.BasicStroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;

import java.util.HashSet;
import java.util.Hashtable;

import static java.lang.Math.floor;
import static java.lang.Math.ceil;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;


/**
 * 
 *
 * @author Vladimir Bulatov
 */
public class TextUtil {

    static final boolean DEBUG = false;

    static public final int ALIGN_LEFT = 0, ALIGN_RIGHT = 1, ALIGN_CENTER = 2, ALIGN_TOP=3, ALIGN_BOTTOM=4;
    static public final int FIT_VERTICAL = 0, FIT_HORIZONTAL=1, FIT_BOTH=2, FIT_NONE=3;

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


    public static BufferedImage createTextImageOutline(int imageWidth, int imageHeight, 
                                                       String text, Font font,  
                                                       double spacing, Insets2 insets, boolean preserveAspect, int fitStyle, int halignment, int valignment,
                                                       double outlineWidth, boolean kerning) {
        
        
        if(DEBUG)printf("createText(%d, %d, %s) \n", imageWidth, imageHeight, text);
        if(kerning){
            Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
            map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
            font = font.deriveFont(map);
        }
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
        double scaleX = at.getScaleX();
        // printf("transform.scaleX: %f", scaleX);
        g.setTransform(at);
        
        outlineWidth /= scaleX;
        if(false){
            g.setColor(Color.LIGHT_GRAY);        
            g.fill(textRect);
            g.setColor(Color.RED);        
            g.draw(textRect);
        }

        g.setColor(Color.BLACK);
        BasicStroke stroke = new BasicStroke((float)outlineWidth, BasicStroke.CAP_ROUND,  	BasicStroke.JOIN_ROUND);
        g.setStroke(stroke);
        Shape textOutline = gv.getOutline();
        g.draw(textOutline);

        return image;
    }


    public static void renderText(Graphics2D g,  Font font, String text, double x, double y, double spacing,double outlineWidth) {
                
        if(DEBUG)printf("renderText(%d, %d, %s) \n", x,y, text);

        char ctext[] = text.toCharArray();
        GlyphVector gv = font.layoutGlyphVector(g.getFontRenderContext(), ctext, 0, ctext.length,  Font.LAYOUT_LEFT_TO_RIGHT);
        double space = spacing*gv.getGlyphPosition(ctext.length).getX()/ctext.length;        
        for(int i = 0; i < ctext.length; i++){
            Point2D pnt = gv.getGlyphPosition(i);
            Rectangle2D rect = gv.getGlyphVisualBounds(i).getBounds2D();
            gv.setGlyphPosition(i, new Point2D.Double(pnt.getX() + space*i, pnt.getY())); 
        }

        Rectangle2D textRect = gv.getVisualBounds(); 
        if(true)printf("textRect: [x:%5.1f y:%5.1f w:%5.1f h:%5.1f]\n ", textRect.getX(),textRect.getY(),textRect.getWidth(), textRect.getHeight());
                
        g.setColor(Color.BLACK);
        BasicStroke stroke = new BasicStroke((float)outlineWidth, BasicStroke.CAP_ROUND,  	BasicStroke.JOIN_ROUND);
        g.setStroke(stroke);
        Shape textOutline = gv.getOutline();
        AffineTransform trans = new AffineTransform();
        trans.translate(x,y);
        g.setTransform(trans);
        g.draw(textOutline);

    }


    
    public static void getKerning(Graphics2D g,  Font font, String text, double spacing, double resolution, double x, double y) {
                
        if(DEBUG)printf("getKerning(%s) \n", text);
        double halfWidth = spacing/2;
        char ctext[] = text.toCharArray();
        
        //GlyphVector gv = font.createGlyphVector(g.getFontRenderContext(), ctext);
        GlyphVector gv = font.layoutGlyphVector(g.getFontRenderContext(), ctext, 0, ctext.length, 0);

        Rectangle2D textRect = gv.getVisualBounds(); 
        if(true)printf("textRect: [x:%5.1f y:%5.1f w:%5.1f h:%5.1f]\n", 
                       textRect.getX(),textRect.getY(),textRect.getWidth(), textRect.getHeight());


        //double posx = textRect.getX(), posy = 0;
        double posx = 0, posy = y;

        for(int i = 0; i < ctext.length; i++){
            Point2D pnt = gv.getGlyphPosition(i);            
            Rectangle2D rect = gv.getGlyphVisualBounds(i).getBounds2D();            
            rect = getExpandedRect(rect, halfWidth);
            //g.draw(rect);
            double xr = posx + rect.getX();
            double yr = rect.getY();
            
            g.setColor(Color.blue);
            //g.draw(new Rectangle2D.Double(posx, rect.getY()+y, rect.getWidth(),rect.getHeight()));
            //g.draw(new Rectangle2D.Double(posx+x, y+rect.getY(), rect.getWidth(),rect.getHeight()));
            posy = rect.getY();
            g.setColor(Color.red);
            double r = 4;
            Point2D pos = gv.getGlyphPosition(i); 


            printf("%d(%c):[(%5.1f,%5.1f);[(%5.1f,%5.1f); %5.1f x %5.1f]\n",
                   i, ctext[i],xr,yr, rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight());   
            Point2D gp = new Point2D.Double(posx+(pos.getX() - rect.getX()),0);
            gv.setGlyphPosition(i, gp); 

            fillCircle(g, x+gp.getX(),y+gp.getY(), r);

            
            posx += rect.getWidth();
            
        }

        //AffineTransform trans = new AffineTransform();
        //trans.translate(x,y);
        //g.setTransform(trans);

                
        g.setColor(Color.BLACK);

        g.drawGlyphVector(gv, (float)x,(float)y);
        
        g.setColor(Color.blue);        
        drawCircle(g,x,y,8);
        
        BasicStroke outlineStroke = new BasicStroke((float)spacing, BasicStroke.CAP_ROUND,  	BasicStroke.JOIN_ROUND);
        g.setStroke(outlineStroke);
        g.setColor(Color.gray);
        Shape textOutline = gv.getOutline();
        AffineTransform at = new AffineTransform();
        at.translate(x,y);
        g.setTransform(at);
        g.draw(textOutline);
        
        g.setColor(Color.BLACK);
        g.drawGlyphVector(gv, 0,0);
        //g.setTransform(savedTransform);
        double margin = 1; // margin around glyphs 
        for(int i = 0; i < ctext.length; i++){
            Point2D gp = gv.getGlyphPosition(i);
            Shape gs = gv.getGlyphOutline(i);
            Rectangle2D srect = getExpandedRect(gs.getBounds(), halfWidth+margin);
            printf("srect:[%5.1f,%5.1f; %5.1f x %5.1f]\n",srect.getX(),srect.getY(),srect.getWidth(),srect.getHeight());
            Rectangle2D rect = gv.getGlyphVisualBounds(i).getBounds2D();            
            g.setColor(Color.blue);
            g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND,  	BasicStroke.JOIN_ROUND));
            g.draw(srect);
            g.setStroke(outlineStroke);  
            g.setColor(Color.gray);
            g.draw(gs);
            g.setColor(Color.black);
            g.fill(gs);            
            g.setColor(Color.red);
            fillCircle(g, gp.getX(),gp.getY(), 5);

            Grid2D gg = getShapeOutline(gs, srect, outlineStroke);
            
            if (true) {
                try {
                    ImageIO.write(Grid2DtoImage.getARGBImage(gg), "png", new File(fmt("/tmp/kerning_%02d.png",i)));
                } catch(Exception e){
                    e.printStackTrace();
                }           
                
            }
        }

    }

    static final void fillCircle(Graphics2D g, double x, double y, double r){
        g.fill(new Ellipse2D.Double(x-r,y-r,2*r,2*r));
    }

    static final void drawCircle(Graphics2D g, double x, double y, double r){
        g.draw(new Ellipse2D.Double(x-r,y-r,2*r,2*r));
    }

    static Rectangle2D getExpandedRect(Rectangle2D rect, double width){
        return new Rectangle2D.Double(rect.getX()-width,rect.getY()-width, rect.getWidth() + 2*width, rect.getHeight() + 2*width);
    }

    //
    // draw shape into given rectangle using stroke 
    //
    static Grid2D getShapeOutline(Shape shape, Rectangle2D rect, Stroke stroke){
        
        roundRect(rect, 1.);

        int width = (int)(rect.getWidth());
        int height = (int)(rect.getHeight());
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(Color.white);
        g.fill(new Rectangle2D.Double(0,0,width, height));
        AffineTransform at = new AffineTransform();
        at.translate(-rect.getX(),-rect.getY());
        g.setTransform(at);                
        g.setColor(Color.black);
        g.setStroke(stroke);
        g.draw(shape);
        
        double x0 = rect.getX();
        double x1 = rect.getX() + rect.getWidth();
        double y0 = rect.getY();
        double y1 = rect.getY() + rect.getHeight();
        double z0 = 0;
        double z1 = 1;

        return ImageToGrid2D.makeGrid(image, true, new Bounds(x0, x1, y0, y1, z0, z1));
        
    }

    /**
       expand rectangle to the nearest voxel boundary
       @return rounder rect 
     */
    public static void roundRect(Rectangle2D rect, double voxelSize){

        double x0 = voxelSize*floor(rect.getX()/voxelSize);
        double y0 = voxelSize*floor(rect.getY()/voxelSize);
        double x1 = voxelSize*ceil((rect.getX() + rect.getWidth())/voxelSize);
        double y1 = voxelSize*ceil((rect.getY() + rect.getHeight())/voxelSize);
        rect.setFrameFromDiagonal(x0,y0, x1,y1);
        
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
            case FIT_NONE:
                return at;
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
        if(DEBUG)printf("text bounds: [%5.1f %5.1f %5.1f %5.1f]\n ", rect.getX(),rect.getY(),rect.getWidth(), rect.getHeight());

        // rect size        
        double tx = rect.getWidth();
        double ty = rect.getHeight();
        double textHeight = (height-(insets.top + insets.bottom));
        if(textHeight <= 0)
            throw new IllegalArgumentException(fmt("negative textHeight: %7.3f  height: %7.3f insets.top:  %7.3f  insets.bottom: %7.3f", 
                                                   textHeight, height, insets.top, insets.bottom));
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