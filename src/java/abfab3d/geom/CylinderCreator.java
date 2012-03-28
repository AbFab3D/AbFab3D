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

package abfab3d.geom;

// External Imports
import java.util.*;
import java.io.*;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.DualWrapper;
import org.web3d.vrml.sav.ContentHandler;

// Internal Imports
import abfab3d.grid.Grid;

/**
 * Creates a Cylinder
 *
 * @author Alan Hudson
 */
public class CylinderCreator extends GeometryCreator {
    protected double height;
    protected double radius;

    // The center position
    protected double x;
    protected double y;
    protected double z;

    // Rotation to apply
    protected double rx;
    protected double ry;
    protected double rz;
    protected double rangle;

    protected int materialID;

    boolean swapYZ = false;

    /**
     * Constructor.
     *
     */
    public CylinderCreator(
        double h, double r,
        double x, double y, double z,
        double rx, double ry, double rz, double ra,
        int material) {

        height = h;
        this.radius = r;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.rangle = ra;
        this.materialID = material;

        if (rx == 1 && (Math.abs(rangle - 1.57075) < 0.00001)) {
            System.out.println("x rotate set2");
            swapYZ = true;
            double t = y;
            this.y = z;
            this.z = t;
        }
    }

    /**
     * Generate the geometry and issue commands to the provided handler.
     *
     * @param grid The grid to update
     */
    public void generate(Grid grid) {
        AttributeGrid wrapper = null;

        if (grid instanceof AttributeGrid) {
            wrapper = (AttributeGrid) grid;
        } else {
            wrapper = new DualWrapper(grid);
        }
        
        int[] coords1 = new int[3];
        int[] coords2 = new int[3];
        int start,end, r;


        double h = height / 2.0;

        if (swapYZ) {
            //            wrapper.getGridCoords(x,z, y - h,coords1);
            wrapper.getGridCoords(x,z - h, y,coords1);
            wrapper.getGridCoords(radius,0,0,coords2);

            start = coords1[1];
            end = start + (int)(Math.floor(height / wrapper.getSliceHeight()));
            r = coords2[0];
            int xc = coords1[0];
            int zc = coords1[2];

            System.out.println("Generate grid from: " + start + " end: " + end);
            for(int y=start; y < end; y++) {
                rasterCircleSwapYZ(wrapper, xc, y, zc, r, materialID);
            }
        } else {
            wrapper.getGridCoords(x,y - h,z,coords1);
            wrapper.getGridCoords(radius,0,0,coords2);

            start = coords1[1];
            end = start + (int)(Math.floor(height / wrapper.getSliceHeight()));
            r = coords2[0];
            int xc = coords1[0];
            int zc = coords1[2];

            System.out.println("Generate grid from: " + start + " end: " + end + " at: " + xc + " " + zc + " r: " + r);
            for(int y=start; y < end; y++) {
                rasterCircle(wrapper, xc, y, zc, r, materialID);
            }
        }
    }

    /**
     * Create a circle on a grid, xz plane
     *
     * @param x0 The x center
     * @param y0 The y center
     * @param z0 The z center
     * @param radius The radius in voxels
     */
    private void rasterCircle(AttributeGrid grid, int x0, int y0, int z0, int radius, int mat) {
        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_z = -2 * radius;
        int x = 0;
        int z = radius;

        grid.setData(x0, y0, z0 + radius, Grid.EXTERIOR, mat);
        //setPixel(x0, z0 + radius);

        grid.setData(x0, y0, z0 - radius, Grid.EXTERIOR, mat);
        //setPixel(x0, z0 - radius);

        rasterLine(grid, x0 - radius, x0 + radius, y0, z0, z0, mat);
        //setPixel(x0 + radius, y0);
        //setPixel(x0 - radius, y0);

        while(x < z) {
            // ddF_x == 2 * x + 1;
            // ddF_z == -2 * z;
            // f == x*x + z*z - radius*radius + 2*x - z + 1;
            if(f >= 0) {
              z--;
              ddF_z += 2;
              f += ddF_z;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;

            rasterLine(grid, x0 - x, x0 + x, y0, z0 + z, z0 + z, mat);
            //setPixel(x0 + x, y0 + y);
            //setPixel(x0 - x, y0 + y);

            rasterLine(grid, x0 - x, x0 + x, y0, z0 - z, z0 - z, mat);
            //setPixel(x0 + x, z0 - z);
            //setPixel(x0 - x, z0 - z);

            rasterLine(grid, x0 - z, x0 + z, y0, z0 + x, z0 + x, mat);
            //setPixel(x0 + z, z0 + x);
            //setPixel(x0 - z, z0 + x);

            rasterLine(grid, x0 - z, x0 + z, y0, z0 - x, z0 - x, mat);
            //setPixel(x0 + z, z0 - x);
            //setPixel(x0 - z, z0 - x);
        }
    }

    /**
     * Create a circle on a grid, xz plane
     *
     * @param x0 The x center
     * @param y0 The y center
     * @param z0 The z center
     * @param radius The radius in voxels
     */
    private void rasterCircleSwapYZ(AttributeGrid grid, int x0, int y0, int z0, int radius, int mat) {
        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_z = -2 * radius;
        int x = 0;
        int z = radius;

//System.out.println("rcswap: " + x0 + " " + " " + y0 + " " + z0 + " " + radius);
        grid.setData(x0, z0 + radius, y0, Grid.EXTERIOR, mat);
        //setPixel(x0, z0 + radius);

        grid.setData(x0, z0 - radius,y0,Grid.EXTERIOR, mat);
        //setPixel(x0, z0 - radius);

        rasterLineSwapYZ(grid,x0 - radius, x0 + radius,y0,z0,z0,mat);
        //setPixel(x0 + radius, y0);
        //setPixel(x0 - radius, y0);

        while(x < z) {
            // ddF_x == 2 * x + 1;
            // ddF_z == -2 * z;
            // f == x*x + z*z - radius*radius + 2*x - z + 1;
            if(f >= 0) {
              z--;
              ddF_z += 2;
              f += ddF_z;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;

            rasterLineSwapYZ(grid,x0 - x, x0 + x, y0, z0 + z, z0 + z, mat);
            //setPixel(x0 + x, y0 + y);
            //setPixel(x0 - x, y0 + y);

            rasterLineSwapYZ(grid,x0 - x, x0 + x, y0, z0 - z, z0 - z, mat);
            //setPixel(x0 + x, z0 - z);
            //setPixel(x0 - x, z0 - z);

            rasterLineSwapYZ(grid,x0 - z, x0 + z, y0, z0 + x, z0 + x, mat);
            //setPixel(x0 + z, z0 + x);
            //setPixel(x0 - z, z0 + x);

            rasterLineSwapYZ(grid,x0 - z, x0 + z, y0, z0 - x, z0 - x, mat);
            //setPixel(x0 + z, z0 - x);
            //setPixel(x0 - z, z0 - x);
        }
    }

    private void rasterLine(AttributeGrid grid, int x, int x2, int y, int z, int z2, int mat) {
        int w = x2 - x;
        int h = z2 - z;
        int dx1 = 0, dz1 = 0, dx2 = 0, dz2 = 0;
        if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1;
        if (h<0) dz1 = -1 ; else if (h>0) dz1 = 1;
        if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1;

        int longest = Math.abs(w);
        int shortest = Math.abs(h);

        if (!(longest > shortest)) {
            longest = Math.abs(h) ;
            shortest = Math.abs(w) ;
            if (h<0)
                dz2 = -1;
            else if (h>0)
                dz2 = 1 ;
            dx2 = 0 ;
        }

        int numerator = longest >> 1 ;

        for (int i=0;i <= longest;i++) {
            grid.setData(x,y,z,Grid.EXTERIOR,mat);
            numerator += shortest ;
            if (!(numerator<longest)) {
                numerator -= longest ;
                x += dx1 ;
                z += dz1 ;
            } else {
                x += dx2 ;
                z += dz2 ;
            }
        }
    }
    
    private void rasterLineSwapYZ(AttributeGrid grid, int x, int x2, int y, int z, int z2, int mat) {
        int w = x2 - x;
        int h = z2 - z;
        int dx1 = 0, dz1 = 0, dx2 = 0, dz2 = 0;
        if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1;
        if (h<0) dz1 = -1 ; else if (h>0) dz1 = 1;
        if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1;

        int longest = Math.abs(w);
        int shortest = Math.abs(h);

        if (!(longest > shortest)) {
            longest = Math.abs(h) ;
            shortest = Math.abs(w) ;
            if (h<0)
                dz2 = -1;
            else if (h>0)
                dz2 = 1 ;
            dx2 = 0 ;
        }

        int numerator = longest >> 1 ;

        for (int i=0;i <= longest;i++) {
            grid.setData(x, z, y, Grid.EXTERIOR, mat);
            numerator += shortest ;
            if (!(numerator<longest)) {
                numerator -= longest ;
                x += dx1 ;
                z += dz1 ;
            } else {
                x += dx2 ;
                z += dz2 ;
            }
        }
    }
}
