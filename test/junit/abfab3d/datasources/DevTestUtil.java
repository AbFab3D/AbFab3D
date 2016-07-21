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

package abfab3d.datasources;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

// internal imports
import abfab3d.core.AttributeGrid;
import abfab3d.core.Bounds;
import abfab3d.core.GridDataDesc;
import abfab3d.core.GridDataChannel;

import abfab3d.util.ColorMapper;

import abfab3d.grid.ArrayAttributeGridShort;

import static abfab3d.core.Output.printf;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.lerp3;

public class DevTestUtil {
    public static AttributeGrid makeDistanceGrid(Bounds bounds, double vs, double maxDist){
        AttributeGrid grid = new ArrayAttributeGridShort(bounds, vs, vs);
        grid.setDataDesc(GridDataDesc.getDistance(16, maxDist));
        return grid;
    }

    static void printGridSliceY(AttributeGrid grid, int y){
        int nx = grid.getWidth();
        int nz = grid.getDepth();
        printf("y:%d\n",y);
        printf("=====\n");
        for(int z = 0; z < nz; z++){
            for(int x = 0; x < nx; x++){
                long att = grid.getAttribute(x,y,z);
                printf("%4x ", att);
            }
            printf("\n");
        }
        printf("=====\n");
    }
    
    static void printGridSliceValueY(AttributeGrid grid, int y, GridDataChannel channel, String format){
        int nx = grid.getWidth();
        int nz = grid.getDepth();
        printf("y:%d\n",y);
        printf("=====\n");
        for(int z = 0; z < nz; z++){
            for(int x = 0; x < nx; x++){
                double v = channel.getValue(grid.getAttribute(x,y,z));
                printf(format, v);
            }
            printf("\n");
        }
        printf("=====\n");
    }

    static void printGridSliceValueZ(AttributeGrid grid, int z, GridDataChannel channel, String format){
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        printf("z:%d\n",z);
        printf("=====\n");
        for(int x = 0; x < nx; x++){
            for(int y = 0; y < ny; y++){
                double v = channel.getValue(grid.getAttribute(x,y,z));
                printf(format, v);
            }
            printf("\n");
        }
        printf("=====\n");
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
       @param nu domension of slice in U direction 
       @param nv domension of slice in Vdirection 
       @param path locatrion of file to write to 
     */
    public static void writeSlice(AttributeGrid grid, GridDataChannel dataChannel, ColorMapper colorMapper, 
                                  Vector3d origin, Vector3d eu, Vector3d ev, int nu, int nv, String path) throws IOException{
        
        BufferedImage image =  new BufferedImage(nu, nv, BufferedImage.TYPE_INT_ARGB);
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

        for(int v = 0; v < nv; v++){
            for(int u = 0; u < nu; u++){
                pnt.set(origin);
                pu.set(eu);
                pu.scale(u);
                pnt.add(pu);
                pv.set(ev);
                pv.scale(v);
                pnt.add(pv);
                grid.getGridCoords(pnt.x, pnt.y, pnt.z, gcoord);

                int 
                    gx = (int)gcoord.x,
                    gy = (int)gcoord.y,
                    gz = (int)gcoord.z;
                double 
                    dx = gcoord.x - gx,
                    dy = gcoord.y - gy,
                    dz = gcoord.z - gz;

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
        ImageIO.write(image, "png", new File(path));        
        
    }

}