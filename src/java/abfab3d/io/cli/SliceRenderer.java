/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.cli;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import abfab3d.core.Bounds;


import static abfab3d.core.Units.CM;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.fmt;



/**
   renders given slice into image 
 */
public class SliceRenderer {
    
    public static final double DEFAULT_PIXEL_SIZE = 0.1*MM;


    // pixel size in meters 
    double m_pixelSizeX = DEFAULT_PIXEL_SIZE;
    double m_pixelSizeY = DEFAULT_PIXEL_SIZE;  

    double m_originX=0;
    double m_originY = 0; // slice origin 
    double a = 10*CM;
    Color m_fillColor = new Color(0, 0, 0);
    Color m_outlineColor = null;//new Color(0, 0, 128);

    Bounds m_bounds = new Bounds(0,a, 0,a,0,a); // physical bounds to fit into the image 

    public SliceRenderer(Bounds bounds){

        m_bounds = bounds.clone();

    }

    public void setPixelSize(double pixelSizeX, double pixelSizeY){
        m_pixelSizeX = pixelSizeX;
        m_pixelSizeY = pixelSizeY;
    }

    public void setColors(Color fillColor, Color outlineColor){

        m_fillColor = fillColor;
        m_outlineColor = outlineColor;

    }

    public BufferedImage createImage(){

        int height = m_bounds.getGridHeight(m_pixelSizeY);
        int width = m_bounds.getGridWidth(m_pixelSizeX);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        //ImageIO.write(image, "png", new File(tpath));

       return image;
    }

    public void renderSlice(Graphics2D graphics, SliceLayer slice){

        int count = slice.getPolyLineCount();
        Path2D.Double path = new Path2D.Double();
        Point2D.Double pnt = new Point2D.Double();

        for(int i = 0; i < count; i++){

            PolyLine line = slice.getPolyLine(i);
            double pnts[] = line.getPoints();
            int kmax = pnts.length/2;
            pnt.setLocation(pnts[0], pnts[1]);
            w2s(pnt);
            path.moveTo(pnt.x, pnt.y);

            for(int k = 1; k < kmax; k++){
                pnt.setLocation(pnts[2*k], pnts[2*k+1]);
                w2s(pnt);
                path.lineTo(pnt.x, pnt.y);                
            }
            path.closePath();            
        }
        if(m_fillColor != null) {
            graphics.setPaint(m_fillColor);
            graphics.fill(path);
        }
        if(m_outlineColor != null){
            graphics.setPaint(m_outlineColor);
            graphics.draw(path);
        }
    }

    void w2s(Point2D.Double p){

        p.setLocation((p.x - m_bounds.xmin)/m_pixelSizeX, (m_bounds.ymax - p.y)/m_pixelSizeY);
        
    }

   

}