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
