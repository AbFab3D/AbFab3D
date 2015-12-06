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
import java.io.IOException;
import java.util.*;

import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;


// Internal Imports
import abfab3d.util.ColorMapper;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeChannel;
import abfab3d.grid.Grid2D;


import static abfab3d.util.MathUtil.lerp2;
import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.Output.printf;

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
    public static void writeSlice(AttributeGrid grid, int magnification, int iz, AttributeChannel dataChannel, ColorMapper colorMapper, String path) throws IOException {

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        int imgx = nx*magnification;
        int imgy = ny*magnification;

        BufferedImage image =  new BufferedImage(imgx, imgy, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        //if(DEBUG) printf("DataBuffer: %s\n", db);        
        int[] sliceData = db.getData();

        double pix = 1./magnification;        
        int debugSize= 30;

        for(int iy = 0; iy < imgy; iy++){

            double y = (iy+0.5)*pix - 0.5;

            for(int ix = 0; ix < imgx; ix++){

                double x = (ix+0.5)*pix-0.5;
                int gx = (int)Math.floor(x);
                int gy = (int)Math.floor(y);
                double dx = x - gx;
                double dy = y - gy;
                //if(ix < magnification/2 && iy < magnification/2)
                //    printf("[%2d %2d](%4.2f %4.2f) ", gx, gy, dx, dy);
                int gx1 = clamp(gx + 1,0, nx-1);
                int gy1 = clamp(gy + 1,0, ny-1);
                gx = clamp(gx,0, nx-1);
                gy = clamp(gy,0, ny-1);
                long a00 = grid.getAttribute(gx,gy, iz);
                long a10 = grid.getAttribute(gx1,gy, iz);
                long a01 = grid.getAttribute(gx,gy1, iz);
                long a11 = grid.getAttribute(gx1,gy1, iz);

                double v00 = dataChannel.getValue(a00);
                double v10 = dataChannel.getValue(a10);
                double v01 = dataChannel.getValue(a01);
                double v11 = dataChannel.getValue(a11);
                //if(a00 != 0 && debugCount-- > 0) {
                //    printf("[%3d %3d %3d]-> %3d %7.3f %x\n", ix, iy, iz, a00, v00, ac.getBits(a00));
                //}
                double v = lerp2(v00, v10, v11, v01,dx, dy);
                sliceData[ix + (imgy-1-iy)*imgx] = colorMapper.getColor(v);
            }
        }
        
        ImageIO.write(image, "png", new File(path));        

    }

    /**
     writes grid slice slice into image file using given magnification and ColorMapper

     */
    public static void writeSlice(AttributeGrid grid, int iz, AttributeChannel dataChannel, ColorMapper colorMapper, BufferedImage image) throws IOException {

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        //if(DEBUG) printf("DataBuffer: %s\n", db);
        int[] sliceData = db.getData();

        double pix = 1.;
        int debugSize= 30;

        for(int iy = 0; iy < ny; iy++){

            double y = (iy+0.5)*pix - 0.5;

            for(int ix = 0; ix < nx; ix++){

                double x = (ix+0.5)*pix-0.5;
                int gx = (int)Math.floor(x);
                int gy = (int)Math.floor(y);
                double dx = x - gx;
                double dy = y - gy;
                //if(ix < magnification/2 && iy < magnification/2)
                //    printf("[%2d %2d](%4.2f %4.2f) ", gx, gy, dx, dy);
                int gx1 = clamp(gx + 1,0, nx-1);
                int gy1 = clamp(gy + 1,0, ny-1);
                gx = clamp(gx,0, nx-1);
                gy = clamp(gy,0, ny-1);
                long a00 = grid.getAttribute(gx,gy, iz);
                long a10 = grid.getAttribute(gx1,gy, iz);
                long a01 = grid.getAttribute(gx,gy1, iz);
                long a11 = grid.getAttribute(gx1,gy1, iz);

                double v00 = dataChannel.getValue(a00);
                double v10 = dataChannel.getValue(a10);
                double v01 = dataChannel.getValue(a01);
                double v11 = dataChannel.getValue(a11);
                //if(a00 != 0 && debugCount-- > 0) {
                //    printf("[%3d %3d %3d]-> %3d %7.3f %x\n", ix, iy, iz, a00, v00, ac.getBits(a00));
                //}
                double v = lerp2(v00, v10, v11, v01,dx, dy);
                sliceData[ix + (ny-1-iy)*nx] = colorMapper.getColor(v);
            }
        }
    }

}