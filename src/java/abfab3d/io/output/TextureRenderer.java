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
package abfab3d.io.output;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector2d;

import abfab3d.grid.AttributeGrid;
import abfab3d.util.TriangleRenderer; 
import abfab3d.util.MathUtil;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;


/**
   class responsible for rendering triangles coored by 3D grid data into 2D texture 

   @author Vladimir Bulatov
 */
public class TextureRenderer {

    static final boolean DEBUG = true;
    static int debugCount = 100;
    
    static final double EPS = 1.e-9;

    static final int RES_OK = 1, RES_DEGENERATE_TRIANGLE = 2;
    
    AttributeGrid m_dataGrid;
    AttributeGrid m_textureGrid;

    TriangleInterpolator m_triInterpolator = new TriangleInterpolator();
    TriangleRenderer m_triRenderer = new TriangleRenderer();
    TexturePixelRenderer m_pixelRenderer = new TexturePixelRenderer();



    /**
       @param dataGrid - grid which contains source data for rendering 
       @param textureGrid - grid where the texure is being rendered. The texture is stored at single y-slice in the texture grid
       
     */    
    public TextureRenderer(AttributeGrid dataGrid, AttributeGrid textureGrid){
        m_dataGrid = dataGrid;
        m_textureGrid = textureGrid;
    }

    /**
       renders three dimensional source into two dimensional textured triangle. 
       @param tri vertices of source 3D triangle in the grid 
       @param tex  vertices of textured 2D triangle
       <p>
       all coordinates should be given in voxel units 
       the physical origin and size of the grid is ignored 
       </p>      
       <p>
       Algorithm works as follows 
       </p>
       <p>
       texTri is rasterized using TriangleRasterizer 
       each 2D pixel in texTri is mapped into 3D point in the plane of srcTri using linear transformation 
       the color of the 3D point is calculated from nearest voxels of the srcGrid via bilinear interpolation 
       that color is assigned to the 2D pixel of texture triangle. 
       </p>
     */
    public void renderTriangle(double tri[][], double tex[][]){
        
        m_triInterpolator.init(tex, tri);
        
        m_triRenderer.fillTriangle(m_pixelRenderer, 
                                   tex[0][0],tex[0][1],
                                   tex[1][0],tex[1][1],
                                   tex[2][0],tex[2][1]);       
    }

          


    class TexturePixelRenderer implements TriangleRenderer.PixelRenderer {

        double pnt[]= new double[3];
        double color[][] = new double[8][3];
        double icolor[] = new double[3];
        double colorChannel[] = new double[8];

        /**
           @override            
        */
        public void setPixel(int u, int v){

            // transform pixel into 3D space 
            m_triInterpolator.interpolate(u+0.5, v+0.5, pnt);
            // read voxel from dataGrid 

            // do half voxel shift to the center of voxels 
            double x = pnt[0]-0.5, y = pnt[1]-0.5, z = pnt[2]-0.5;           
            //double x = u/4., y = 25, z = v/4.;
            int 
                ix = (int)(x),
                iy = (int)(y),
                iz = (int)(z);
            double 
                dx = x - ix,
                dy = y - iy,
                dz = z - iz;

            
            getColor(m_dataGrid.getAttribute(ix,   iy,   iz),color[0]);
            getColor(m_dataGrid.getAttribute(ix+1, iy,   iz),color[1]);
            getColor(m_dataGrid.getAttribute(ix,   iy+1, iz),color[2]);
            getColor(m_dataGrid.getAttribute(ix+1, iy+1, iz),color[3]);
            getColor(m_dataGrid.getAttribute(ix,   iy,   iz+1),color[4]);
            getColor(m_dataGrid.getAttribute(ix+1, iy,   iz+1),color[5]);
            getColor(m_dataGrid.getAttribute(ix,   iy+1, iz+1),color[6]);
            getColor(m_dataGrid.getAttribute(ix+1, iy+1, iz+1),color[7]);

            interpolateColors(dx, dy, dz, color, icolor);

            //if(DEBUG && color[0][0] != color[1][0] && debugCount-- > 0) 
            //    printf("u: %3d v: %3d (%7.5f %7.5f %7.5f): (%4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f): %4.2f\n", u, v, 
            //           dx, dy, dz, 
            //           color[0][0],color[1][0],color[2][0],color[3][0],color[4][0],color[5][0],color[6][0],color[7][0], icolor[0]);

            // write pixel into textureGrid 
            m_textureGrid.setAttribute(u, 0, v, makeAtt(icolor));   
        }

        long makeAtt(double c[]){         
            return (((int)(c[0]*255))& 0xFF) | ((((int)(c[1]*255))&0xFF)<<8) | ((((int)(c[2]*255))&0xFF)<<16);
        }
        
        void getColor(long att, double color[]){

            color[0] = (att & 0xFF)/255.;
            color[1] = ((att >> 8) & 0xFF)/255.;
            color[2] = ((att >> 16) & 0xFF)/255.;

        }

        //
        // interpolate colors between vertices of a cube 
        //
        final void interpolateColors(double x, double y, double z, double c[][], double ic[]){
            
            //if(x < 0 || x > 1 || y < 0 || y > 1 || z < 0 || z > 1)
            //    throw new RuntimeException(fmt("bad params:%5.3f %5.3f %5.3f",x,y,z));

            double 
                x1 = 1-x,
                y1 = 1-y,
                z1 = 1-z;

            copyColor(c, 0, colorChannel);
            ic[0] = interpolateColor(x, y, z, x1, y1, z1,colorChannel);

            //if(DEBUG && colorChannel[0] != colorChannel[1] && debugCount-- > 0) 
            //    printf("(%7.5f %7.5f %7.5f): (%4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f): %4.2f\n", x, y, z, 
            //           colorChannel[0],colorChannel[1],colorChannel[2],colorChannel[3],colorChannel[4],colorChannel[5],colorChannel[6],colorChannel[7],ic[0]);
            copyColor(c, 1, colorChannel);
            ic[1] = interpolateColor(x, y, z, x1, y1, z1, colorChannel);
            copyColor(c, 2, colorChannel);
            ic[2] = interpolateColor(x, y, z, x1, y1, z1, colorChannel);            
        }

        final void copyColor(double c[][], int cindex, double channel[]){
            
            for(int k =0; k < channel.length; k++){
                channel[k] = c[k][cindex];
            }            
        }

        //
        // interpolate single color channel 
        //
        final double interpolateColor(double x, double y, double z, 
                                      double x1, double y1, double z1, double c[]){
            return 
                //x1 * c[0] + x * c[1];
                //z1 * c[0] + z * c[4];            
            x1 *(y1 * (z1 * c[0] + z  * c[4]) +  y*(z1 * c[2] + z * c[6])) +   
               x  *(y1 * (z1 * c[1] + z  * c[5]) +  y*(z1 * c[3] + z * c[7]));
        }
    }
}