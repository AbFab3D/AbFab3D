/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
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

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;

import java.awt.geom.Path2D;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;

import java.io.File;


import abfab3d.core.Bounds;

import static java.lang.Math.*;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
   class to render 2d graphics into image using physical units 
 */
public class GraphicsCanvas2D {

    double m_xmin, m_xmax, m_ymin, m_ymax;

    BufferedImage m_img;
    String m_fileType = "png";
    Graphics2D m_graphics;
    int m_width, m_height;

    public GraphicsCanvas2D(int width, int height, Bounds bounds, Color background){
        this(width, height, bounds);
        clear(background);
    }

    public GraphicsCanvas2D(int width, int height, Bounds bounds){

        m_width = width;
        m_height = height;
        m_xmin = bounds.xmin;
        m_xmax = bounds.xmax;
        m_ymin = bounds.ymin;
        m_ymax = bounds.ymax;
        m_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        m_graphics = (Graphics2D)m_img.getGraphics();
        m_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void clear(Color color){
        
        m_graphics.setColor(color);
        m_graphics.fillRect(0,0, m_width, m_height);
    }

    public void write(String path)throws Exception{
        ImageIO.write(m_img, m_fileType, new File(path));
    }

    public void setColor(Color color){
        m_graphics.setColor(color);
    }

    public void drawLine(double x0, double y0, double x1, double y1, Color color){
        m_graphics.setColor(color);
        drawLine(x0, y0, x1, y1);
    }


    public void drawLine(double x0, double y0, double x1, double y1){


        m_graphics.draw(new Line2D.Double(imageX(x0),imageY(y0),imageX(x1),imageY(y1)));
        
    }

    public void drawCircle(double x, double y, double r, Color color){        
        m_graphics.setColor(color);
        drawCircle(x, y, r);
    }

    public void drawCircle(double x, double y, double r){        

        double cx = imageX(x);
        double cy = imageY(y);
        double w = abs(imageX(x+r) - imageX(x-r));
        double h = abs(imageY(y+r) - imageY(y-r));
        
        m_graphics.draw(new Ellipse2D.Double(cx - h/2,cy - h/2, w, h));        
    }

    public void fillCircle(double x, double y, double r, Color color){        
        m_graphics.setColor(color);
        fillCircle(x, y, r);
    }

    public void fillCircle(double x, double y, double r){        

        double cx = imageX(x);
        double cy = imageY(y);
        double w = abs(imageX(x+r) - imageX(x-r));
        double h = abs(imageY(y+r) - imageY(y-r));
        
        m_graphics.fill(new Ellipse2D.Double(cx - h/2,cy - h/2, w, h));        
    }


    public void fillPoly(PointSet poly, Color color){        

        Path2D.Double path = new Path2D.Double();
        Vector3d pnt = new Vector3d();
        poly.getPoint(0,pnt);        
        path.moveTo(imageX(pnt.x), imageY(pnt.y));
        for(int i = 1; i < poly.size(); i++){            
            poly.getPoint(i,pnt);        
            path.lineTo(imageX(pnt.x), imageY(pnt.y));
        }
        
        path.closePath();
        
        m_graphics.setColor(color);
        m_graphics.fill(path); 
                    
    }

    public void drawPoly(PointSet poly, Color color){        

        Path2D.Double path = new Path2D.Double();
        Vector3d pnt = new Vector3d();
        poly.getPoint(0,pnt);        
        path.moveTo(imageX(pnt.x), imageY(pnt.y));
        for(int i = 1; i < poly.size(); i++){            
            poly.getPoint(0,pnt);        
            path.lineTo(imageX(pnt.x), imageY(pnt.y));
        }
        
        m_graphics.setColor(color);
        m_graphics.draw(path); 
                    
    }

    
    public double imageX(double x){

        return m_width*(x - m_xmin)/(m_xmax-m_xmin);

    }

    public double imageY(double y){

        return m_height*(m_ymax - y)/(m_ymax-m_ymin);
    }

}