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
import abfab3d.util.LongConverter;
import abfab3d.util.Bounds;

import static java.lang.Math.sqrt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.MathUtil.extendTriangle;


/**
   class responsible for rendering triangles colored by data from 3D grid into 2D texture 
   original triangles are 3D 
   they are packed individiually onto 2D plane and rendered on 2D plane

   @author Vladimir Bulatov
 */
public class TextureRenderer {

    static final boolean DEBUG = true;
    static int debugCount = 100;
    
    static final double EPS = 1.e-9;

    static final int RES_OK = 1, RES_DEGENERATE_TRIANGLE = 2;
    
    AttributeGrid m_dataGrid;
    AttributeGrid m_textureGrid;
    // dimensions of data grid 
    int m_nx, m_ny, m_nz;
    // dimensions of texure grid 
    int m_nu, m_nv;
    
    TriangleInterpolator m_triInterpolator = new TriangleInterpolator();
    TriangleRenderer m_triRenderer = new TriangleRenderer();
    TexturePixelRenderer m_pixelRenderer;
    LongConverter m_colorMaker;

    /**
       @param dataGrid - grid which contains source data for rendering 
       @param colorMaker - converter from dataGrid attribute into RGB value
       @param textureGrid - grid where the texure is being rendered. The 2D texture is stored at single y-slice in the texture grid
       
     */    
    public TextureRenderer(AttributeGrid dataGrid, LongConverter colorMaker, AttributeGrid textureGrid){

        m_dataGrid = dataGrid;
        m_nx = m_dataGrid.getWidth();
        m_ny = m_dataGrid.getHeight();
        m_nz = m_dataGrid.getDepth();
        m_colorMaker = colorMaker;
        m_textureGrid = textureGrid;
        m_nu = m_textureGrid.getWidth();
        m_nv = m_textureGrid.getDepth();
        m_pixelRenderer = new TexturePixelRenderer();
    }

    /**
       renders three dimensional source triangle into two dimensional textured triangle. 
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
    public void renderTriangle(double tri[][], double texTri[][]){
        
        m_triInterpolator.init(texTri, tri);

        double tex[][] = texTri;

        m_triRenderer.fillTriangle(m_pixelRenderer, 
                                   tex[0][0],tex[0][1],
                                   tex[1][0],tex[1][1],
                                   tex[2][0],tex[2][1]);       
    }

    /**
       renders three dimensional source triangle into two dimensional textured triangle. 
       the same as renderTriangle() 
       with additional exentsion of 2D triangle by given extendWidth. 
       this is to make 2D triangle a little bigger to cover boundary effects on the edges of triagle 
       @param tri[3][3] 3D triangle coords
       @param texTri[3][2] 2D triangle coords
       @param extendWidth width of triangle extension 
       @param extTexTri[3][2] work array for extended triangles 
       @param lines[3][3] work array for triangle sides equations 
     */
    public void renderTriangleExtended(double tri[][], double texTri[][], double extWidth, double extTexTri[][], double lines[][]){
        
        m_triInterpolator.init(texTri, tri);

        extendTriangle(texTri, extWidth, lines, extTexTri);
        double tex[][] = extTexTri;        
        m_triRenderer.fillTriangle(m_pixelRenderer, 
                                   tex[0][0],tex[0][1],
                                   tex[1][0],tex[1][1],
                                   tex[2][0],tex[2][1]);       
    }

   
    /**
       renders individual pixels of texture 
     */
    class TexturePixelRenderer implements TriangleRenderer.PixelRenderer {

        double pnt[]= new double[3];
        double color[][] = new double[8][3];
        double icolor[] = new double[3];
        double colorChannel[] = new double[8];
        double m_scaleFactor = 1;
        double m_xmin, m_ymin, m_zmin;

        TexturePixelRenderer(){

            m_scaleFactor = 1./m_dataGrid.getVoxelSize();
            Bounds bounds = m_dataGrid.getGridBounds();
            m_xmin = bounds.xmin;
            m_ymin = bounds.ymin;
            m_zmin = bounds.zmin;
        }

        /**
           @override            
        */
        public void setPixel(int u, int v){

            // transform pixel into 3D space 
            if(u < 0 || u >= m_nu || v < 0 || v >= m_nv)
                return;

            m_triInterpolator.interpolate(u+0.5, v+0.5, pnt);
            // read voxel from dataGrid 

            // do half voxel shift to the center of voxels 
            double 
                x = m_scaleFactor*(pnt[0]-m_xmin)-0.5, 
                y = m_scaleFactor*(pnt[1]-m_ymin)-0.5, 
                z = m_scaleFactor*(pnt[2]-m_zmin)-0.5;           

            int 
                ix = (int)(x),
                iy = (int)(y),
                iz = (int)(z);
            double 
                dx = x - ix,
                dy = y - iy,
                dz = z - iz;

            //printf("%3d,%3d,%3d\n", iz, iy, iz);
            getPointColor(ix,   iy,   iz,color[0]);
            getPointColor(ix+1, iy,   iz,color[1]);
            getPointColor(ix,   iy+1, iz,color[2]);
            getPointColor(ix+1, iy+1, iz,color[3]);
            getPointColor(ix,   iy,   iz+1,color[4]);
            getPointColor(ix+1, iy,   iz+1,color[5]);
            getPointColor(ix,   iy+1, iz+1,color[6]);
            getPointColor(ix+1, iy+1, iz+1,color[7]);

            interpolateColors(dx, dy, dz, color, icolor);

            //if(DEBUG && color[0][0] != color[1][0] && debugCount-- > 0) 
            //    printf("u: %3d v: %3d (%7.5f %7.5f %7.5f): (%4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f): %4.2f\n", u, v, 
            //           dx, dy, dz, 
            //           color[0][0],color[1][0],color[2][0],color[3][0],color[4][0],color[5][0],color[6][0],color[7][0], icolor[0]);

            // write pixel into textureGrid 
            m_textureGrid.setAttribute(u, 0, v, makeAtt(icolor));   
        }

        // TODO - shall thid be done by output data channel ? 
        long makeAtt(double c[]){   
            
            return (((int)(c[0]*255))& 0xFF) | ((((int)(c[1]*255))&0xFF)<<8) | ((((int)(c[2]*255))&0xFF)<<16);
        }

        // get color of the given voxel 
        void getPointColor(int x, int y, int z, double color[]){

            if(x < 0) x = -x;
            if(y < 0) y = -y;
            if(z < 0) z = -z;
            if(x >= m_nx) x = x % m_nx;
            if(y >= m_ny) y = y % m_ny;
            if(z >= m_nz) z = z % m_ny;
            getColorComponents(m_colorMaker.get(m_dataGrid.getAttribute(x,y,z)), color); 
            
        }
        
        void getColorComponents(long c, double color[]){

            color[0] = ((c      ) & 0xFF)/255.;
            color[1] = ((c >>  8) & 0xFF)/255.;
            color[2] = ((c >> 16) & 0xFF)/255.;

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

    } //   class TexturePixelRenderer 

}//    class TextureRenderer 