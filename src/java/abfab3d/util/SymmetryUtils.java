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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;


import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static abfab3d.core.Output.printf;

/**
   misc methods for symmetries in inversive geometry 
   
 */
public class SymmetryUtils {
    
    static final double EPS = 1.e-10;

    /**
       represents inversive triangle (v0, v1, v2) with angles (a0, a1, a2)
       v0 in origin, 
       side (v0, v1) - line at x-axis
       side (v0, v2) - line at angle pi/k to x-axis 
       side (v1, v2) - segment of line or cirle 
       len2 - length of side (v0,v1)(opposite to v2 
     */
    public static class InversiveTriangle {
        
        static final int GEOM_EUCLIDEAN = 0, GEOM_SPHERICAL = 1, GEOM_HYPERBOLIC = 2;
        
        int m_geometry =-1;
        double a0, a1, a2, a3, len2;

        public InversiveTriangle( double a0, double a1, double a2, double len2){
            
            this.a0 = a0;
            this.a1 = a1;
            this.a2 = a2;
            this.len2 = len2;

            double factor = (a0 + a1 + a2);
            if(factor < PI - EPS){ // hyperbolic geometry 
                m_geometry = GEOM_HYPERBOLIC;
            } else if(factor < PI + EPS){
                m_geometry = GEOM_EUCLIDEAN;
            } else { 
                m_geometry = GEOM_SPHERICAL;
            }
            
        }

        /**
           draw the triangle onto the canvas 
           the canvas size is assumed to be (len2 x len2)
           and triangle is scaled to fit the square 
         */
        public void draw(Graphics2D g){

            double s1 = 1; // lengthg of side (v0, v2)
            if(m_geometry == GEOM_EUCLIDEAN){
		s1 = sin(a1)/sin(a2);
            }	else {
		s1 = (cos(a1) + cos(a2+a0))/(cos(a2) + cos(a1+a0));
            }	

            double v2x = s1*cos(a0);
            double v2y = s1*sin(a0);
            // scale to fit triangle into square 
            double smax = max(1., v2x);
            smax = max(smax, v2y);
            
            double s2 = len2/smax; // len(v0, v1)

            v2x *= s2;
            v2y *= s2;

            Path2D clip = new Path2D.Double();
            clip.moveTo(0,0);
            clip.lineTo(s2*2,0);
            clip.lineTo(2*v2x, 2*v2y);
            clip.lineTo(0, 0);
            g.setClip(clip);

            g.draw(new Line2D.Double(0, 0, s2, 0 ));
            g.draw(new Line2D.Double(0, 0, v2x, v2y));
            
            if(m_geometry == GEOM_EUCLIDEAN){
                // draw line (v1, v2)

                Path2D tri = new Path2D.Double();
                tri.moveTo(0,0);
                tri.lineTo(s2,0);
                tri.lineTo(v2x, v2y);
                tri.lineTo(0, 0);
                //g.draw(new Line2D.Double(s2, 0, v2x, v2y ));
                g.fill(tri);

            } else {
                double r = s2*sin(a0)/(cos(a2) + cos(a1+a0));
                double cx = r*(cos(a2) + cos(a1)*cos(a0))/sin(a0);
                double cy = r*cos(a1);
                r = abs(r);
                if(m_geometry == GEOM_HYPERBOLIC) { 
                    
                    printf("outiside cx: %5.3f cy: %5.3f r: %5.3f \n", cx, cy, r );
                    //fill outside of ellipse 
                    Path2D out = new Path2D.Double();
                    out.setWindingRule(Path2D.WIND_EVEN_ODD);                    

                    //out.append(new Rectangle2D.Double(-len2, -len2, 4*len2,4*len2),false);
                    out.append(new Rectangle2D.Double(cy-4*r, cy-4*r, 8*r, 8*r), false); // larger rect
                    out.append(new Ellipse2D.Double(cx-r, cy-r, 2*r, 2*r), false);

                    g.fill(out);                    
                    
                } else {
                    printf("inside cx: %5.3f cy: %5.3f r: %5.3f \n", cx, cy, r );
                    g.fill(new Ellipse2D.Double(cx-r, cy-r, 2*r, 2*r));                    
                }

            }              
        }
    }    
}
