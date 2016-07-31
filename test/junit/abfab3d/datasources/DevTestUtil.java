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
import abfab3d.core.DataSource;
import abfab3d.core.Vec;

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
    
    static void printGridSliceValueX(AttributeGrid grid, int x, GridDataChannel channel, String format, double factor){
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        printf("x:%d\n",x);
        printf("=====\n");
        for(int y = 0; y < ny; y++){
            for(int z = 0; z < nz; z++){
                double v = factor*channel.getValue(grid.getAttribute(x,y,z));
                printf(format, v);
            }
            printf("\n");
        }
        printf("=====\n");
    }

    static void printGridSliceValueY(AttributeGrid grid, int y, GridDataChannel channel, String format){
        printGridSliceValueY(grid, y, channel, format, 1.);
    }

    static void printGridSliceValueY(AttributeGrid grid, int y, GridDataChannel channel, String format, double factor){
        int nx = grid.getWidth();
        int nz = grid.getDepth();
        printf("y:%d\n",y);
        printf("=====\n");
        for(int z = 0; z < nz; z++){
            for(int x = 0; x < nx; x++){
                double v = factor*channel.getValue(grid.getAttribute(x,y,z));
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


    static void printRay(DataSource data, Vector3d origin, Vector3d e, double voxelSize, int count){

        Vec p = new Vec(3);
        Vec d = new Vec(3);
        p.setVoxelSize(voxelSize);

        for(int i = 0; i < count; i++){
            p.v[0] = origin.x + e.x*i;
            p.v[1] = origin.y + e.y*i;
            p.v[2] = origin.z + e.z*i;
            data.getDataValue(p, d);
            printf("(%8.5f, %8.5f, %8.5f) -> (%8.5f, %8.5f, %8.5f)\n", p.v[0],p.v[1],p.v[2],d.v[0],d.v[1],d.v[2]);
        }
    }
}