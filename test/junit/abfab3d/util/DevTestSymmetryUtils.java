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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;

import java.io.File;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import static java.lang.Math.PI;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;


public class DevTestSymmetryUtils {

    
    /**
       draw array of images of inversive triangles 
       n0 - angle at origin 
       n1, n2 other two angles 
       filePattern  path pattern to save the png files to 
       width - width of the images 
     */
    static void drawInversiveTrianges(int n0, int n1, int n2, String filePattern, int width) throws Exception { 

        BufferedImage outImage = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
        float lineWidth = 1;
        Graphics2D graphics = outImage.createGraphics();
        
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND,  BasicStroke.JOIN_ROUND));

        graphics.translate(0,width);
        graphics.scale(1,-1);

        //graphics.scale(0.5, 0.5);        graphics.translate(width/2, width/2);

        
        for(int k1 = 2; k1 <= n1; k1++){
            for(int k2 = 2; k2 <= n2; k2++){

                String fname = fmt(filePattern,n0, k1, k2);                
                printf("%s\n",fname);
                SymmetryUtils.InversiveTriangle tri = new SymmetryUtils.InversiveTriangle(PI/n0, PI/k1,PI/k2, width);
                //graphics.setPaint(new Color(240, 240, 240));
                graphics.setClip(null);
                //graphics.setColor(new Color(255,255,255,0));
                //graphics.fillRect(0,0, width, width);
                graphics.setBackground(new Color(255,255,255,0));
                graphics.clearRect(0,0, width, width);

                graphics.setPaint(new Color(200, 200, 200));
                tri.draw(graphics);
                //graphics.draw(new Rectangle(0,0,width-1, width-1));
                
                ImageIO.write(outImage, "png", new File(fname));

            }
        }
    }

    
    public static void main(String arg[]) throws Exception {

        drawInversiveTrianges(7, 7,7, "/tmp/tri/tri_%d_%d_%d.png", 1000);
    }
}
