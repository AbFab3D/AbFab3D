/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2015
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


import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;

/**
   rasterises filled triangle using custom interface TriangleRenderer.PixelRenderer
   
   triangle with floating point coordinates is rendered as pixels whose centers are are inside of the triangle 
   
   the pixel with integer coordinates (x, y) has center at (x+0.5, y+0.5) 
   
   The renderer is not thread safe. 
   Different threads have to use separate instances of TriangleRenderer
   
	@author Vladimir Bulatov
	
 */
public class TriangleRenderer {
        

    static final boolean DEBUG = false;

    // working place to hold triangle edges 
    private Edge edges[] = new Edge[]{new Edge(),new Edge(),new Edge()};

    public TriangleRenderer(){        
    }
    
    /**
       
       renders filled triangle using PixelRenderer
       for each traingle pixel it calls renderer.setPixle(x,y) 
       
       @param renderer custom pixel renderer 
       @param x1  triangle vertices coordinates 
       @param y1 
       @param x2 
       @param y2 
       @param x3 
       @param y3 
     */
    public final void fillTriangle(PixelRenderer renderer, 
                                   double x1, double y1,
                                   double x2, double y2, 
                                   double x3, double y3){        
        if(DEBUG)
            printf("Triangle.fill(%7.3f,%7.3f,; %7.3f,%7.3f; %7.3f,%7.3f)\n", x1, y1, x2, y2, x3, y3);
        
        // create edges for the triangle
        edges[0].init(x1,y1, x2,y2);
        edges[1].init(x2,y2, x3,y3);
        edges[2].init(x3,y3, x1,y1);
        
        double maxLength = 0;
        int longEdge = 0;
        
        // find edge with the greatest length in the y axis
        for(int i = 0; i < 3; i++) {
            double length = edges[i].y2 - edges[i].y1;
            if(length > maxLength) {
                maxLength = length;
                longEdge = i;
            }
        } 
        int shortEdge1 = (longEdge + 1) % 3;
        int shortEdge2 = (longEdge + 2) % 3;
        // draw spans between edges; the long edge can be drawn
        // with the shorter edges to draw the full triangle
        fillSpans(renderer, edges[longEdge], edges[shortEdge1]);
        fillSpans(renderer, edges[longEdge], edges[shortEdge2]);        
        
    }
    
    /**
       we assume, that 
       e1 is y-longest edge of triangle 
       e2 - one of 2 shorter edges 
       
    */
    final void fillSpans(PixelRenderer renderer, Edge e1, Edge e2){
        if(DEBUG)printf("fillSpans(%s, %s)\n", e1, e2);
        
        // calculate difference between the y coordinates
        // of the first edge and return if 0
        double e1ydiff = (e1.y2 - e1.y1);
        if(e1ydiff == 0.0)
            return;
        
        // calculate difference between the y coordinates
        // of the second edge and return if 0
        double e2ydiff = (e2.y2 - e2.y1);
        if(e2ydiff == 0.0)
            return;
        
        // calculate differences between the x coordinates
        // and colors of the points of the edges
        double e1xdiff = (e1.x2 - e1.x1);
        double e2xdiff = (e2.x2 - e2.x1);
        
        // we use e2, because it is shorter than e1 and one point is shared
        
        int ystart = (int)(e2.y1+0.5);
        int yend = (int)(e2.y2+0.5);
        
        if(DEBUG)printf("ystart: %d yend: %d\n", ystart, yend);
        if(DEBUG)printf("e1xdiff: %7.3f e2xdiff: %7.3f\n", e1xdiff, e2xdiff);
        
        double e1x1 = e1.x1;
        double e2x1 = e2.x1;
        
        // calculate factors to use for interpolation
        // with the edges and the step values to increase
        // them by after drawing each span
        double factor1 = (ystart + 0.5 - e1.y1) / e1ydiff;
        double factorStep1 = 1.0 / e1ydiff;
        double factor2 = (ystart + 0.5 - e2.y1) / e2ydiff;
        double factorStep2 = 1.0 / e2ydiff;

        // loop through the lines between ystart and yend
        for(int iy = ystart; iy < yend; iy++) {
            
            double y = iy + 0.5;
            if(DEBUG)printf("y: %7.3f\n ", y);
            double xs = e1x1 + (e1xdiff * factor1);
            double xe = e2x1 + (e2xdiff * factor2);
            factor1 += factorStep1;
            factor2 += factorStep2;
            
            if(DEBUG)printf("xs: %7.3f, xe: %7.3f\n ", xs, xe);
            
            drawSpan(renderer, iy, xs, xe);
            
        }
        
    }
    
    /**
       fill interval of voxels (x1, x2) at given y coordinate
    */
    final void drawSpan(PixelRenderer renderer, int iy, double x1, double x2){
        
        if(DEBUG)printf("drawSpan(%d, %7.3f, %7.3f)\n", iy, x1, x2);            
        double xdiff = x2 - x1;
        if(xdiff == 0.0)
            return;
        
        if( xdiff < 0){
            double t = x1; 
            
            x1 = x2; 
            x2 = t;
            
            xdiff = -xdiff;
        }
                        
        // draw each pixel in the span
        int xstart = (int)(x1+0.5);
        int xend = (int)(x2+0.5);
                
        double factor = (xstart + 0.5 - x1)/xdiff; // offset of center of first voxel from start of interval 
        double factorStep = 1.0 / xdiff;
        
        //printf("zdiff: %7.3f factor: %7.3f, factorStep: %7.3f\n", zdiff, factor, factorStep);
        
        //loop through each x position in the span 
        for(int ix = xstart; ix < xend; ix++) {
            renderer.setPixel(ix, iy);
            factor += factorStep;
        }                
    }
    
    //
    // representation of one triangle edge 
    //
    static class Edge {
        
        double x1, y1, x2, y2;
        
        void init(double _x1, double _y1, double _x2, double _y2){ 
            
            if(_y1 < _y2) {
                x1 = _x1;
                y1 = _y1;
                x2 = _x2;
                y2 = _y2;
            } else {
                x1 = _x2;
                y1 = _y2;
                x2 = _x1;
                y2 = _y1;
            }
            
        }
        
        public String toString(){
            return fmt("[(%7.3f,%7.3f),(%7.3f,%7.3f)]", x1, y1, x2, y2);
        }        
    } // class Edge 


    /**
       interface used by TriangleRenderer to render individual pixels 
       the classes implementing this interface are supposed to know where the pixels has to be rendetred and the pixel attributes
    */
    public interface PixelRenderer {
        
        /**
           set pixel with given coordinates 
         */
        public void setPixel(int x, int y);
        
    }

}    
