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
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


/**
 * Image utilities.
 *
 * @author Vladimir Bulatov
 */
public class ImageUtil {

    public static final double CNORM = 255.; // maximal value of color component
    public static final double CNORM1 = 1./255.;
    // indices of component in the array of colors
    public static final int RED = 0, GREEN  = 1, BLUE = 2, ALPHA = 3;
    
    static public int getAlpha(int rgb){
        return ((rgb >> 24) & 0xFF);
    }
    
    static public int getRed(int rgb){
        return ((rgb >> 16) & 0xFF);
    }
    
    static public int getGreen(int rgb){
        return ((rgb >> 8) & 0xFF);
    }
    
    static public int getBlue(int rgb){
        return ((rgb) & 0xFF);
    }
    
    static final int makeRGB(int r, int g, int b, int a ){
        return 
            ((a & 0xFF) << 24 )|
            ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8) |
            ((b & 0xFF) );    
    }
    
    public static void lerpColors(double c1[], double c2[], double x, double cout[]){
        
        for(int i = 0; i < 4; i++){
            cout[i] = c1[i] + x * (c2[i] - c1[i]);
        }
        
    }
    
    
    /**
       do multiplication of colors normalized to [0, 255]
    */
    public static final double mul255(double a, double b){
        return a*b * CNORM1;
    }

    public static final int mul255(int a, int b){
        return (a*b)/255;
    }
    
    /*
      combine two premultiplied colors with given alpha
      colors are normalized to [0, 255]
    */
    public static final double combinePremultDouble(double c1, double c2, double alpha){
        
        return c1 + c2 - alpha * c1 * CNORM1;
        
    }

    public static final int combinePremultInt(int c1, int c2, int alpha){
        
        return c1 + c2 - alpha * c1 / 255;
        
    }

    // combination of non-premult color components 
    public static final int combineInt(int c1, int c2, int alpha){
        
        return c1 + (c2 - c1)* alpha / 255;
        
    }
    
    //
    // formula for alpha composition works for PREMULTIPLIED colors (colors are premultiplied by alpha value) 
    //
    public static void combinePremultColors(double c1[], double c2[], double c3[], double alpha){
        
        for(int i = 0; i < 4; i++){
            c3[i] = combinePremultDouble(c1[i], c2[i], alpha);
        }
    }

    public static int combinePremultColorsInt(int c1, int c2){

        int a = getAlpha(c2);
        int r = combinePremultInt(getRed(c1),   getRed(c2), a);
        int g = combinePremultInt(getGreen(c1), getGreen(c2), a);
        int b = combinePremultInt(getBlue(c1),  getBlue(c2), a);
        int a1 = combinePremultInt(getAlpha(c1),  a, a);

        return makeRGB(r,g,b,a1);

    }
        
    public static double[] getPremultColor(Color c, double outColor[]){
        double alpha = c.getAlpha();
        outColor[ALPHA] = alpha;
        outColor[RED] = mul255(c.getRed(),alpha);
        outColor[GREEN] = mul255(c.getGreen(),alpha);
        outColor[BLUE] = mul255(c.getBlue(),alpha);
        return outColor;
    }

    public static double[] getPremultColor(int rgba, double outColor[]){
        double alpha = getAlpha(rgba);
        outColor[ALPHA] = alpha;
        outColor[RED] = mul255(getRed(rgba),alpha);
        outColor[GREEN] = mul255(getGreen(rgba),alpha);
        outColor[BLUE] = mul255(getBlue(rgba),alpha);
        return outColor;
    }

    public static final int getPremultColorInt(int c){
        int a = getAlpha(c);
        int r = mul255(getRed(c),a);
        int g = mul255(getGreen(c),a);
        int b = mul255(getBlue(c),a);
        return makeRGB(r, g, b, a );
    }

    public static final int getGray(int c){
        return (getRed(c) + getGreen(c) + getBlue(c))/3;
    }

    /**
       calculates gray level intensity of two colors 
       
       c1 - background Color  RGBA (completely opaque) 
       c2 - overlay Color     RGBA (may have transparency) 
       
       input colors are not premultiplied. 
       
     */
    public static final int getCombinedGray(int c1, int c2){

        int r1 = getRed(c1);
        int g1 = getGreen(c1);
        int b1 = getBlue(c1);

        int r2 = getRed(c2);
        int g2 = getGreen(c2);
        int b2 = getBlue(c2);
        int a2 = getAlpha(c2);
        
        int r3 = combineInt(r1,r2,a2);
        int g3 = combineInt(g1,g2,a2);
        int b3 = combineInt(b1,b2,a2);

        return (r3 + g3 + b3)/3;
    }
    
    /*
      return scaled down image using pixel weight function 
    */
    public static BufferedImage getScaledImage(BufferedImage image, int width, int height, PixelWeight pixelWeight){
        
        int w0 = image.getWidth();
        int h0 = image.getHeight();
        
        BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for(int y = 0; y < height; y++){
            
            int y0 = (y * h0)/height;
            int y1 = ((y + 1) * h0)/height;
            //printf("y0: %d, y1: %d\n", y0, y1);
            
            for(int x = 0; x < width; x++){
                
                int x0 = (x * w0)/width;
                int x1 = ((x + 1) * w0)/width;
                
                //printf("x0: %d, x1: %d\n", x0, x1);
                
                int rs = 0, gs = 0, bs = 0, as = 0, ws = 0;
                
                for(int yy = y0; yy < y1; yy++){
                    
                    for(int xx = x0; xx < x1; xx++){
                        
                        int pixel = image.getRGB(xx,yy);
                        int r = getRed(pixel);
                        int g = getGreen(pixel);
                        int b = getBlue(pixel);
                        int a = getAlpha(pixel);
                        int weight = pixelWeight.getWeight(r,g,b,a);
                        rs += r*weight;
                        gs += g*weight;
                        bs += b*weight;
                        as += a*weight;
                        ws += weight;
                    }
                }
                
                rs /= ws;
                gs /= ws;
                bs /= ws;
                as /= ws;
                outImage.setRGB(x,y, makeRGB(rs,gs,bs,as));
                
            }
        }
        
        return outImage;
        
    }
    
    
    /**
       return scaled instance of the image 
       returned image has the same type as source image
    */
    public static BufferedImage getScaledImage(BufferedImage image, int width, int height){
        
        try {
            
            BufferedImage scaledImage = new BufferedImage(width, height, image.getType());
            //BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            // Paint scaled version of image to new image
            Graphics2D graphics2D = scaledImage.createGraphics();
            //graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            //graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            //graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            //Image scaled = image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
            
            graphics2D.drawImage(scaled, 0, 0, width, height, null);
            
            return scaledImage;      
            
        } catch(Exception e){
            e.printStackTrace(Output.out);
        }
        return null;
    }  
    
}
