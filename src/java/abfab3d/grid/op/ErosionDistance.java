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

package abfab3d.grid.op;

// External Imports

// Internal Imports

import abfab3d.datasources.DataSourceGrid;
import abfab3d.datasources.Plane;
import abfab3d.datasources.Subtraction;
import abfab3d.grid.*;
import abfab3d.util.Long2Short;
import abfab3d.util.LongConverter;
import abfab3d.util.Units;

import javax.vecmath.Vector3d;

import static abfab3d.util.ImageUtil.MAXC;
import static abfab3d.util.ImageUtil.makeRGB;
import static abfab3d.util.Output.time;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;

/**
 * Erode an object based on using a DistanceTransform.
 *
 * Alternate idea, calculate distance transform from -voxelSize to distance
 *
 * This version is density aware.
 *
 * @author Alan Hudson
 */
public class ErosionDistance implements Operation, AttributeOperation {
    private static final boolean DEBUG = false;

    /** The erosion distance in meters */
    private double distance;
    private int subvoxelResolution;

    public ErosionDistance(double distance, int subvoxelResolution) {
        this.distance = distance;
        this.subvoxelResolution = subvoxelResolution;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        throw new IllegalArgumentException("Not implemented.");
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for dest
     * @return The new grid
     */
    public AttributeGrid executeDirect(AttributeGrid dest) {

        // TODO: This is the way I want to write it but its not working right

        // Nothing to do if distance is 0
        if (distance <= 0) {
            return dest;
        }

        // Calculate DistanceTransform

        double maxInDistance = distance;
        double maxOutDistance = 5 * dest.getVoxelSize();  // TODO: make sure surface gets eroded

        printf("outDistance: %f\n",maxOutDistance);
//        double maxOutDistance = 0;

        long t0 = time();

        // TODO:  allow user specified DistanceTransform class
        // TODO: change back to use multistep
        DistanceTransformMultiStep dt = new DistanceTransformMultiStep(subvoxelResolution, maxInDistance, maxOutDistance);
//        DistanceTransformExact dt = new DistanceTransformExact(subvoxelResolution, maxInDistance, maxOutDistance);
        AttributeGrid dg = dt.execute(dest);

        /*
        if (DEBUG) {
            MyGridWriter gw = new MyGridWriter(8,8);
            DistanceColorizer colorizer =new DistanceColorizer(subvoxelResolution,0,0,0);
            gw.writeSlices(dg, subvoxelResolution, "/tmp/slices/ed_%03d.png",0, dg.getWidth(), colorizer);
        }
        */

        int nx = dest.getWidth();
        int ny = dest.getHeight();
        int nz = dest.getDepth();

        // Erode grid by removing surface voxels

        double vs = dg.getVoxelSize();

        nx = dg.getWidth();
        ny = dg.getHeight();
        nz = dg.getDepth();

        // 5 intervals for distance values

        int inDistanceMinus = (int) (-maxInDistance * subvoxelResolution / vs - subvoxelResolution / 2);
        int inDistancePlus = (int) (-maxInDistance * subvoxelResolution / vs + subvoxelResolution / 2);
        int outDistanceMinus = (int) (maxOutDistance * subvoxelResolution / vs - subvoxelResolution / 2);
        int outDistancePlus = (int) (maxOutDistance * subvoxelResolution / vs + subvoxelResolution / 2);

        // TODO: remove me
        long out_cnt = 0;

        for(int y=0; y < ny; y++) {
            for(int x=0; x < nx; x++) {
                for(int z=0; z < nz; z++) {
                    long att = (long) (short) dg.getAttribute(x,y,z);

                    if (att == -Short.MAX_VALUE) {
                        int i=1;
                    }
                    short dest_att;

                    if (att == -Short.MAX_VALUE) {
                        dest_att = (short) subvoxelResolution;
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                    } else if (att < inDistanceMinus) {
                        dest.setData(x,y,z,Grid.OUTSIDE,0);
                        out_cnt++;
                    } else if (att >= inDistanceMinus && att < inDistancePlus) {
                        dest_att = (short) (att - inDistanceMinus);
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                    } else if (att >= inDistancePlus && att < outDistanceMinus) {
//                        dest.setData(x,y,z,Grid.OUTSIDE,0);
//                        out_cnt++;
                        dest_att = (short) subvoxelResolution;
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                    } else if (att >= outDistanceMinus && att <= outDistancePlus) {
                        dest_att = (short) (outDistancePlus - att);
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);

                        // TODO: make sure surface voxels go away
//                        dest.setData(x,y,z,Grid.OUTSIDE,0);
//                        out_cnt++;
                    } else {
                       dest.setData(x,y,z,Grid.OUTSIDE,0);
                       out_cnt++;
                    }
                }
            }
        }

        printf("Out cnt: %d\n",out_cnt);
        return dest;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for dest
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {

        // Nothing to do if distance is 0
        if (distance <= 0) {
            return dest;
        }

        // Calculate DistanceTransform

        long t0 = time();

        // TODO:  allow user specified DistanceTransform class

        double maxInDistance = distance + dest.getVoxelSize();
        double maxOutDistance = dest.getVoxelSize();

        DistanceTransformMultiStep dt_exact = new DistanceTransformMultiStep(subvoxelResolution, maxInDistance, maxOutDistance);
        AttributeGrid dg = dt_exact.execute(dest);
        printf("DistanceTransformMultiStep done: %d ms\n", time() - t0);

        /*
        if (DEBUG) {
            MyGridWriter gw = new MyGridWriter(8,8);
            DistanceColorizer colorizer =new DistanceColorizer(subvoxelResolution,0,0,0);
            gw.writeSlices(dg, subvoxelResolution, "/tmp/slices/ed_%03d.png",0, dg.getWidth(), colorizer);
        }
        */

        double[] bounds = new double[6];
        dest.getGridBounds(bounds);
        DensityGridExtractor dge = new DensityGridExtractor(-distance, 0*MM,dg,maxInDistance,maxOutDistance, subvoxelResolution);
        AttributeGrid subsurface = (AttributeGrid) dest.createEmpty(dest.getWidth(), dest.getHeight(), dest.getDepth(), dest.getVoxelSize(), dest.getSliceHeight());
        subsurface.setGridBounds(bounds);

        subsurface = dge.execute(subsurface);

        printf("Done extracting subsurface");
        AttributeGrid new_dest = (AttributeGrid) dest.createEmpty(dest.getWidth(), dest.getHeight(), dest.getDepth(), dest.getVoxelSize(), dest.getSliceHeight());
        new_dest.setGridBounds(bounds);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(subvoxelResolution);

        DataSourceGrid dsg1 = new DataSourceGrid(dest,subvoxelResolution);
        DataSourceGrid dsg2 = new DataSourceGrid(subsurface,subvoxelResolution);

        Subtraction result = new Subtraction(dsg1,dsg2);

        gm.setSource(result);
        gm.makeGrid(new_dest);

        printf("Done making grid");
        return new_dest;
    }
/*
    static class MyGridWriter implements SliceExporter {

        int cellSize = 1;
        int voxelSize = 1;
        int modSkip = 0;

        MyGridWriter(int cellSize, int voxelSize){
            this.cellSize = cellSize;
            this.voxelSize = voxelSize;
        }
        MyGridWriter(int cellSize, int voxelSize, int modSkip){
            this.cellSize = cellSize;
            this.voxelSize = voxelSize;
            this.modSkip = modSkip;
        }

        public void writeSlices(Grid grid, long maxAttribute, String filePattern, int start, int end, LongConverter colorMaker ){

            SlicesWriter slicer = new SlicesWriter();
            slicer.setDataConverter(new Long2Short());
            slicer.setCellSize(cellSize);
            slicer.setVoxelSize(voxelSize);
            slicer.setModSkip(modSkip);

            slicer.setMaxAttributeValue((int)maxAttribute);
            if(colorMaker != null){
                slicer.setColorMaker(colorMaker);
            }
            int sliceMin = grid.getDepth()/2;
            //int sliceMax = sliceMin+1;
            int sliceMax = grid.getDepth() -1;

            slicer.setFilePattern(filePattern);
            slicer.setBounds(0, grid.getWidth(), 0, grid.getHeight(), start, end);
            // make transparent background
            //slicer.setBackgroundColor(0xFFFFFF);  // transparent
            //slicer.setBackgroundColor(0xFFFFFFFF);  // solid white
            //slicer.setForegroundColor(0xFF0000FF); // solid blue

            try {
                slicer.writeSlices(grid);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

    }
*/

    /**
     * Color attributes based on a grayscale distance
     */
    static class DistanceColorizer implements LongConverter {

        int maxvalue = 100;
        int undefined = Short.MAX_VALUE;
        byte zr = 0;
        byte zg = 0;
        byte zb = 0;

        DistanceColorizer(int maxvalue,int zr, int zg, int zb){
            this.maxvalue = maxvalue;
            this.zr = (byte) zr;
            this.zg = (byte) zg;
            this.zb = (byte) zb;
        }

        public long get(long value){

            if(value == -undefined) {
                return makeRGB(MAXC, MAXC,0);
            } else if(value == undefined) {
                return makeRGB(0, MAXC,MAXC);
            }

            byte r = 0;
            byte g = 0;
            byte b = 0;

            if (value == 0) {

                r = zr;
                g = zg;
                b = zb;

            } else {

                //int v = (int)(MAXC  - (value * MAXC / maxvalue) & MAXC);
                byte v = (byte) (map((int)value, -maxvalue,maxvalue,-MAXC,MAXC));
                if (value < 0) {
                    r = (byte) (-v);
                } else {
                    b = (byte) (v);
                }
            }


            return makeRGB(r, g, b);
        }
    }

    /**
     * Map one range of numbers to another range.
     *
     * @param x
     * @param inMin
     * @param inMax
     * @param outMin
     * @param outMax
     * @return
     */
    private static int map(int x, int inMin, int inMax, int outMin, int outMax) {
        int ret_val = (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
        return  ret_val;
    }


}
