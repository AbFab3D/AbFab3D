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

 package abfab3d.grid.util;

// External Imports
import javax.vecmath.Vector3d;

import java.io.IOException;

import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;


// Internal Imports
import abfab3d.util.ColorMapper;

import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataChannel;
import abfab3d.core.Grid2D;


import static abfab3d.core.MathUtil.lerp2;
import static abfab3d.core.MathUtil.lerp3;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.Output.printf;

public class GridUtil  {

     public static void fill(Grid2D grid, long attribute){
         
         int nx = grid.getWidth();
         int ny = grid.getHeight();
         for(int y = 0; y < ny; y++){
             for(int x = 0; x < nx; x++){
                 grid.setAttribute(x,y,attribute);
             }
         }         
     }

     public static void fill(AttributeGrid grid, long attribute){
         
         int nx = grid.getWidth();
         int ny = grid.getHeight();
         int nz = grid.getDepth();
         for(int y = 0; y < ny; y++){
             for(int x = 0; x < nx; x++){
                 for(int z= 0; z < nz; z++){
                     grid.setAttribute(x,y,z,attribute);
                 }
             }
         }         
     }

     /**
        returns count of different voxels in the grids 
      */
     public static long compareGrids(AttributeGrid grid, AttributeGrid grid1){
         
         int nx = grid.getWidth();
         int ny = grid.getHeight();
         int nz = grid.getDepth();
         long count = 0;
         for(int y = 0; y < ny; y++){
             for(int x = 0; x < nx; x++){
                 for(int z = 0; z < nz; z++){
                     if(grid.getAttribute(x,y,z) != grid1.getAttribute(x,y,z))
                         count++;
                 }
             }
         }         
         return count;
     }

    public static void printGridAttribute(AttributeGrid grid, String format){
        
        int nz = grid.getDepth();
        for(int z = 0; z < nz; z++)
            printSliceAttribute(grid, z, format);
    }
    public static void printSliceAttribute(AttributeGrid grid, int z, String format){
         int nx = grid.getWidth();
         int ny = grid.getHeight();
         int nz = grid.getDepth();
         printf("grid slice z: %d\n", z);
         for(int y = 0; y < ny; y++){
             for(int x = 0; x < nx; x++){
                 printf(format, grid.getAttribute(x,y,z));
             }
             printf("\n");
         }                 
    }
    
    public static void printSliceAttribute(AttributeGrid grid, int z){
        
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        printf("grid slice z: %d\n", z);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                printf("%4d ", grid.getAttribute(x,y,z));
            }
            printf("\n");
        }         
    }

    
    /**
       writes grid slice slice into image file using given magnification and ColorMapper
       
     */
    public static void writeSlice(AttributeGrid grid, int magnification, int iz, GridDataChannel dataChannel, ColorMapper colorMapper, String path) throws IOException {

        int imgx = grid.getWidth()*magnification;
        int imgy = grid.getHeight()*magnification;

        BufferedImage image =  new BufferedImage(imgx, imgy, BufferedImage.TYPE_INT_ARGB);

        renderSlice(grid, magnification, iz, dataChannel, colorMapper, image);
        
        ImageIO.write(image, "png", new File(path));        

    }

    /**
     writes grid slice slice into image file using given magnification and ColorMapper

     */
    public static void renderSlice(AttributeGrid grid, int iz, GridDataChannel dataChannel, ColorMapper colorMapper, BufferedImage image){

        renderSlice(grid, 1, iz, dataChannel, colorMapper, image);
    }

    public static void renderSlice(AttributeGrid grid, int magnification, int iz, GridDataChannel dataChannel, ColorMapper colorMapper, BufferedImage image) {

        int gnx = grid.getWidth();
        int gny = grid.getHeight();
        int inx = gnx*magnification;
        int iny = gny*magnification;
        int nz = grid.getDepth();

        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        //if(DEBUG) printf("DataBuffer: %s\n", db);
        int[] sliceData = db.getData();

        double pix = 1./magnification;
        int debugSize= 30;

        for(int iy = 0; iy < iny; iy++){

            double y = (iy+0.5)*pix - 0.5;

            for(int ix = 0; ix < inx; ix++){

                double x = (ix+0.5)*pix-0.5;
                int gx = (int)Math.floor(x);
                int gy = (int)Math.floor(y);
                double dx = x - gx;
                double dy = y - gy;
                int gx1 = clamp(gx + 1,0, gnx-1);
                int gy1 = clamp(gy + 1,0, gny-1);
                gx = clamp(gx,0, gnx-1);
                gy = clamp(gy,0, gny-1);

                double v00 = dataChannel.getValue(grid.getAttribute(gx,gy, iz));
                double v10 = dataChannel.getValue(grid.getAttribute(gx1,gy, iz));
                double v01 = dataChannel.getValue(grid.getAttribute(gx,gy1, iz));
                double v11 = dataChannel.getValue(grid.getAttribute(gx1,gy1, iz));
                //if(a00 != 0 && debugCount-- > 0) {
                //    printf("[%3d %3d %3d]-> %3d %7.3f %x\n", ix, iy, iz, a00, v00, ac.getBits(a00));
                //}
                double v = lerp2(v00, v10, v01, v11,dx, dy);
                sliceData[ix + (iny-1-iy)*inx] = colorMapper.getColor(v);
            }
        }
    }

    /**
       writes image of grid slice into image file 
       slice is calculated using dataChannel in points in space calculated as 
       pnt = origin + eu * u, + ev*v; 
       0 <= u < nu
       0 <= v < nv
       values are lineraly interpolated 
       
       @param grid grid to make slice from 
       @param dataChannel data channel to use 
       @param colorMaper converter from channel data value into color 
       @param origin - physical location of slice corner (0,0)
       @param eu - basis vector in U direction 
       @param ev - basis vector in V direction
       @param nu dimension of slice in U direction 
       @param nv dimension of slice in V direction 
       @param path location of file to write to 
     */
    public static void writeSlice(AttributeGrid grid, GridDataChannel dataChannel, ColorMapper colorMapper, 
                                  Vector3d origin, Vector3d eu, Vector3d ev, int nu, int nv, String path) throws IOException{
        BufferedImage image =  new BufferedImage(nu, nv, BufferedImage.TYPE_INT_ARGB);

        renderSlice(grid,dataChannel,colorMapper, origin, eu, ev, nu, nv,image);
        ImageIO.write(image, "png", new File(path));        
        
    }


    /**
       render image of grid slice into buffered image 
       slice is calculated using dataChannel in points in space calculated as 
       pnt = origin + eu * u + ev*v; 
       0 <= u < nu
       0 <= v < nv
       values are lineraly interpolated 
       
       @param grid grid to make slice from 
       @param dataChannel data channel to use 
       @param colorMaper converter from channel data value into color 
       @param origin - physical location of slice corner (0,0)
       @param eu - basis vector in U direction 
       @param ev - basis vector in V direction
       @param nu dimension of slice in U direction 
       @param nv dimension of slice in Vdirection 
       @param image destination image to render to 
     */
    public static void renderSlice(AttributeGrid grid, GridDataChannel dataChannel, ColorMapper colorMapper, 
                                  Vector3d origin, Vector3d eu, Vector3d ev, int nu, int nv, BufferedImage image) throws IOException{
        
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] sliceData = db.getData();


        Vector3d 
            pnt = new Vector3d(),
            pu = new Vector3d(),
            pv = new Vector3d();
        int 
            nx = grid.getWidth(),
            ny = grid.getHeight(),
            nz = grid.getDepth();

        int nx1 = nx-1, 
            ny1 = ny-1, 
            nz1 = nz-1;

        Vector3d gcoord = new Vector3d();
        final double HALF = 0.5;

        for(int v = 0; v < nv; v++){
            for(int u = 0; u < nu; u++){
                pnt.set(origin);
                pu.set(eu);
                pu.scale(u+HALF);
                pnt.add(pu);
                pv.set(ev);
                pv.scale(v+HALF);
                pnt.add(pv);
                grid.getGridCoords(pnt.x, pnt.y, pnt.z, gcoord);
                double 
                    x = clamp(gcoord.x,0,nx),
                    y = clamp(gcoord.y,0,ny),
                    z = clamp(gcoord.z,0,nz);
                int 
                    gx = (int)(x),
                    gy = (int)(y),
                    gz = (int)(z);
                double 
                    dx = x - gx,
                    dy = y - gy,
                    dz = z - gz;

                gx = clamp(gx, 0, nx1);
                gy = clamp(gy, 0, ny1);
                gz = clamp(gz, 0, nz1);
                int gx1 = clamp(gx + 1,0, nx1);
                int gy1 = clamp(gy + 1,0, ny1);
                int gz1 = clamp(gz + 1,0, nz1);

                double 
                    v000 = dataChannel.getValue(grid.getAttribute(gx, gy, gz)),
                    v100 = dataChannel.getValue(grid.getAttribute(gx1,gy, gz)),
                    v110 = dataChannel.getValue(grid.getAttribute(gx1,gy1,gz)),
                    v010 = dataChannel.getValue(grid.getAttribute(gx, gy1,gz)),
                    v001 = dataChannel.getValue(grid.getAttribute(gx, gy, gz1)),
                    v101 = dataChannel.getValue(grid.getAttribute(gx1,gy, gz1)),
                    v111 = dataChannel.getValue(grid.getAttribute(gx1,gy1,gz1)),
                    v011 = dataChannel.getValue(grid.getAttribute(gx, gy1,gz1));
                
                double value = lerp3(v000, v100, v010, v110,  v001, v101, v011, v111, dx, dy, dz);
                sliceData[u + (nv-1-v)*nu] = colorMapper.getColor(value);                
            }
        }
        
    }

}