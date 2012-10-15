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
}
