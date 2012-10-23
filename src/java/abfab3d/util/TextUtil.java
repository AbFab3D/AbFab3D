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
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Insets;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;


/**
 * 
 *
 * @author Vladimir Bulatov
 */
public class TextUtil {
  

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
        
        TextLayout layout = null;
        double fsize = font.getSize();
        int w = imageWidth - (insets.left + insets.right);
        int h = imageHeight - (insets.top + insets.bottom);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = (Graphics2D)image.getGraphics();

        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        Rectangle2D rect = null;
        double ascale = 1;
        layout = new TextLayout(text, font, g.getFontRenderContext());   
        rect = layout.getBounds();         

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
        AffineTransform at = getRectTransform(textRect, labelRect, true, true);
        //printf("at: scale: %7.3f\n", at.getScaleX());
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

}
