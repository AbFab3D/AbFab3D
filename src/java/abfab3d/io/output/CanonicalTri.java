/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011-2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.output;


import static abfab3d.util.Output.fmt;
import static abfab3d.util.MathUtil.extendTriangle;


/**
   
  class to represetn triangle in canonical orientation 
  vertex v0 - at origin 
  vertex v1 - at the x axis 
  vertex v2 - above x axis 
  

           2 
           *   
          / \  
         /   \ 
      0 *-----* 1

 side 01 is the longest side, thias makes projection of v2 to x axis is inside of (v0, v1)

 @author Vladimir Bulatov
*/
public class CanonicalTri{
    
    // coordinates of 2D triangle in canonocal orientation 
    private double x0; // offset of v0 (non zero if triangle is extended)
    private double y0;

    private double v1x;
    private double v2x;
    private double v2y;        
    private double ext; // extension of triangle size
    private double width;
    private double height;

    static double tri[][] = new double[3][2];
    static double etri[][] = new double[3][2];
    static double lines[][] = new double[3][3];
    
    public CanonicalTri(){

    }
    
    public CanonicalTri(double v1x, double v2x, double v2y, double ext){
        
        this.v1x = v1x;
        this.v2x = v2x;
        this.v2y = v2y;
        this.ext = ext;
        this.width = v1x;
        this.height = v2y;

        if(ext <= 0.)
            return;

        // extend triangle
        tri[0][0] = 0;
        tri[0][1] = 0;
        tri[1][0] = v1x;
        tri[1][1] = 0;
        tri[2][0] = v2x;
        tri[2][1] = v2y;
        
        extendTriangle(tri, ext, lines, etri);
        x0 = -etri[0][0];
        y0 = -etri[0][1];
        width = etri[1][0] - etri[0][0];
        height = etri[2][1] - etri[0][1];
        
    }

    /**
       assign other triangle data to this triangle
     */
    public void set(CanonicalTri tri){
        
        this.v1x = tri.v1x;
        this.v2x = tri.v2x;
        this.v2y = tri.v2y;            
        this.x0 = tri.x0;            
        this.y0 = tri.y0;            
        this.width = tri.width;            
        this.height = tri.height;            

        //this.ext = tri.ext;
        

    }

    /**
       return width of triangle 
     */
    public double getWidth(){
        return width;//v1x + 10*ext;
    } 

    /**
       return height of triangle 
     */
    public double getHeight() {
        return height;//v2y + 2*ext;
    }

    public String toString(){
        return fmt("[%7.2f; (%7.2f, %7.2f)]", v1x, v2x, v2y);
    }

    /**
       return coordinates of origianl triangle places inside of extended rect
     */
    public void getTexCoord(double x, double y, int texindex, double texCoord[]){
        
        x += x0;
        y += y0;

        texCoord[texindex]   = x;  // v0 
        texCoord[texindex+1] = y;
        texCoord[texindex+2] = x + v1x;  //v1 
        texCoord[texindex+3] = y;
        texCoord[texindex+4] = x + v2x;  //v2
        texCoord[texindex+5] = y + v2y;
    }

} // class CanonicalTri 

