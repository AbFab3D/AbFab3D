package abfab3d.grid.op;

import abfab3d.datasources.*;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.grid.SliceExporter;
import abfab3d.io.output.SlicesWriter;
import abfab3d.transforms.Rotation;
import abfab3d.util.ImageUtil;
import abfab3d.util.Long2Short;
import abfab3d.util.LongConverter;
import junit.framework.TestCase;

import static abfab3d.util.ImageUtil.MAXC;
import static abfab3d.util.ImageUtil.makeRGB;
import static abfab3d.util.ImageUtil.makeRGBA;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.L2S;

/**
 * Common code for testing distance transforms
 *
 * @author Alan Hudson
 */
public class BaseTestDistanceTransform extends TestCase {
    AttributeGrid makeBox(int gridSize, double boxWidth, double voxelSize, int maxAttribute, double surfaceThickness){

        double width = gridSize*voxelSize;

        int nx = gridSize;
        int ny = nx;
        int nz = nx;
        double sx = nx*voxelSize;
        double sy = ny*voxelSize;
        double sz = nz*voxelSize;
        double xoff = voxelSize/2;
        double yoff = 0;
        double zoff = 0;

        double bounds[] = new double[]{-sx/2, sx/2, -sy/2, sy/2, -sz/2, sz/2};
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        //Box box = new Box((nx - 4)*voxelSize, (ny - 4) * voxelSize, (nz-30)*voxelSize);
        Box box = new Box(xoff,yoff,zoff,boxWidth, 0.8*ny * voxelSize, (nz-30)*voxelSize);
        box.setTransform(new Rotation(0,0,1,Math.PI/20));
        //box.setTransform(new Rotation(0,0,1,Math.PI/4+0.01));
        GridMaker gm = new GridMaker();

        // TODO:  not sure why but changing this to maxAttribute raises the error rate from 0.56 to 2
//        gm.setMaxAttributeValue(2*maxAttribute);
        gm.setMaxAttributeValue(maxAttribute);
        gm.setVoxelScale(surfaceThickness);

        gm.setSource(box);
        gm.makeGrid(grid);
        return grid;
    }

    AttributeGrid makeSphere(int gridSize, double radius, double voxelSize, int maxAttribute, double surfaceThickness){

        int nx = gridSize;
        int ny = nx;
        int nz = nx;
        double sx = nx*voxelSize;
        double sy = ny*voxelSize;
        double sz = nz*voxelSize;
        double xoff = voxelSize / 2;
        double yoff = 0;
        double zoff = 0;

        double bounds[] = new double[]{-sx/2, sx/2, -sy/2, sy/2, -sz/2, sz/2};
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        Sphere sphere = new Sphere(xoff,yoff,zoff,radius);
        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(maxAttribute);
        gm.setVoxelScale(surfaceThickness);

        gm.setSource(sphere);
        gm.makeGrid(grid);
        return grid;
    }

    AttributeGrid makeGyroid(int gridSize, double radius, double voxelSize, int maxAttribute, double surfaceThickness, double period, double density){

        int nx = gridSize;
        int ny = nx;
        int nz = nx;
        double sx = nx*voxelSize;
        double sy = ny*voxelSize;
        double sz = nz*voxelSize;
        double xoff = voxelSize / 2;
        double yoff = 0;
        double zoff = 0;

        double bounds[] = new double[]{-sx/2, sx/2, -sy/2, sy/2, -sz/2, sz/2};
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        double s = 2.0 * Math.max(grid.getWidth(),grid.getDepth()) * grid.getVoxelSize();
        Box box = new Box(s,s,s);

        //double density = 0.05;
        double t = 8.85744622e-5 * (density * density * density) - 2.011785553e-5 * (density * density) + 2.313078492e-3 * density - 3.174920916e-5;

//        VolumePatterns.Gyroid pattern = new VolumePatterns.Gyroid(10*MM, t);
        VolumePatterns.Gyroid pattern = new VolumePatterns.Gyroid(period, t);

        Intersection combined = new Intersection(box,pattern);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(maxAttribute);
        gm.setVoxelScale(surfaceThickness);
        gm.setVoxelSize(voxelSize);

        gm.setSource(combined);
        gm.makeGrid(grid);
        return grid;
    }

    AttributeGrid makeTorus(int gridSize, double rout, double rin, double voxelSize, int maxAttribute, double surfaceThickness){

        int nx = gridSize;
        int ny = nx;
        int nz = nx;
        double sx = nx*voxelSize;
        double sy = ny*voxelSize;
        double sz = nz*voxelSize;
//        double xoff = voxelSize / 2;
        double xoff = 0;
        double yoff = 0;
        double zoff = 0;

        double bounds[] = new double[]{-sx/2, sx/2, -sy/2, sy/2, -sz/2, sz/2};
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        Torus torus = new Torus(xoff,yoff,zoff,rout,rin);
        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(maxAttribute);
        //gm.setVoxelScale(surfaceThickness);

        gm.setSource(torus);
        gm.makeGrid(grid);
        return grid;
    }

    /**
     * Test that no attribute value exceeds the maximum expected
     * @param val
     */
    public void checkMaxValue(long val, long notCalcedInside, long notCalcedOutside, AttributeGrid grid) {

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long att = (long) (short) grid.getAttribute(x,y,z);
                    if (att != notCalcedInside && att != notCalcedOutside && att > val) {
                        fail("Max Value exceeded.  Max: " + val + " val: " + att);
                    }
                }
            }
        }
    }

    /**
     * Test that no attribute value exceeds the maximum expected
     * @param val
     */
    public void checkMinValue(long val, long notCalcedInside, long notCalcedOutside, AttributeGrid grid) {

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long att = (long) (short) grid.getAttribute(x,y,z);
                    if (att != notCalcedInside && att != notCalcedOutside && att < val) {
                        fail("Min Value exceeded.  Min: " + val + " val: " + att);
                    }
                }
            }
        }
    }

    /**
     * Check that attribute values go from low to high to low without changing direction
     * @param val
     * @param notCalcedInside
     * @param notCalcedOutside
     * @param grid
     */
    public void checkLowToHighToLow(long val, int y, int z, long notCalcedInside, long notCalcedOutside, AttributeGrid grid) {
        int dir = 1;
        long curr = -Short.MAX_VALUE;
        int nx = grid.getWidth();
        boolean changed = false;

        for(int x=1; x < nx; x++) {
            long att = (long) (short) grid.getAttribute(x,y,z);

            if (dir == 1) {
                if (att < curr) {
                    if (!changed) {
                        dir = -1;
                        changed = true;
                        printf("Changed dir at: " + att);
                    } else {
                        fail("Non monotonic change.  Curr: " + curr + " new: " + att + " dir: " + dir);
                    }
                }
            } else if (dir == -1) {
                if (att > curr) {
                    fail("Non monotonic change.  Curr: " + curr + " new: " + att + " dir: " + dir);
                }
            }

            curr = att;
        }
    }

    /**
     * Check that attribute values go from high to low to high without changing direction
     * @param val
     * @param notCalcedInside
     * @param notCalcedOutside
     * @param grid
     */
    public void checkHightToLowToHigh(long val, int y, int z, long notCalcedInside, long notCalcedOutside, AttributeGrid grid) {
        int dir = -1;
        long curr = Short.MAX_VALUE;
        int nx = grid.getWidth();
        boolean changed = false;

        for(int x=1; x < nx; x++) {
            long att = (long) (short) grid.getAttribute(x,y,z);

            if (dir == 1) {
                if (att < curr) {
                    fail("Non monotonic change.  Curr: " + curr + " new: " + att + " dir: " + dir);
                }
            } else if (dir == -1) {
                if (att > curr) {
                    if (!changed) {
                        dir = 1;
                        changed = true;
                    } else {
                        fail("Non monotonic change.  Curr: " + curr + " new: " + att + " dir: " + dir);
                    }
                }
            }

            curr = att;
        }
    }

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


    static void printRow(AttributeGrid grid, int xmin, int xmax, int y, int z){
        printRow(grid,xmin,xmax,y,z,true,25);
    }

    static void printRow(AttributeGrid grid, int xmin, int xmax, int y, int z, boolean header, int width){
        if (header) printf("printRow(%d %d %d %d)\n",xmin, xmax, y,z);
        for(int x = xmin; x < xmax; x++){
            short v = (short)(grid.getAttribute(x,y,z));
            if(v == Short.MAX_VALUE)
                printf("   +");
            else if(v == -Short.MAX_VALUE)
                printf("   .");
            else
                printf("%4d",v);
            if((x-xmin+1)%width == 0)
                printf("\n");
        }

    }


    static void printSlice(AttributeGrid grid, int xmin, int xmax, int ymin, int ymax, int z){

        for(int y = ymin; y < ymax; y++){
            for(int x = xmin; x < xmax; x++){
                int v = L2S(grid.getAttribute(x,y,z));
                if(v == Short.MAX_VALUE)
                    printf("  +  ");
                else if(v == -Short.MAX_VALUE)
                    printf("  -  ");
                else
                    printf("%4d ",v);
            }
            printf("\n");
        }        
    }

    static long[] getDiffHistogram(AttributeGrid grid, AttributeGrid grid1){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();
        int maxDiff = 0;
        int diff = 0;
        long hist[] = new long[100];
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    int d = L2S(grid.getAttribute(x,y,z));
                    int d1 = L2S(grid1.getAttribute(x,y,z));
                    if(d != d1){
                        diff = Math.abs(d - d1);
                        if(diff >= hist.length)
                            hist[hist.length-1]++;
                        else 
                            hist[diff]++;
                        if(diff > maxDiff)
                            maxDiff = diff;
                    } else {
                        hist[0]++;
                    }
                }
            }
        }
        printf("maxDiff: %3d\n", maxDiff);
        return hist;
    }

    public void showColorMapping(LongConverter conv) {
        for(int i=-512; i < 12; i++) {
            int color = (int) conv.get(i);
            printf("%d --> %d,%d,%d\n", i,ImageUtil.getRed(color),ImageUtil.getGreen(color),ImageUtil.getBlue(color));
        }
    }
    /**
     * Color attributes based on a grayscale distance
     */
    static class DistanceColorizer implements LongConverter {

        int maxvalue = 100;
        int undefined = Short.MAX_VALUE;
        int zr = 0;
        int zg = 0;
        int zb = 0;

        DistanceColorizer(int maxvalue,int zr, int zg, int zb){
            this.maxvalue = maxvalue;
            this.zr = (byte) zr;
            this.zg = (byte) zg;
            this.zb = (byte) zb;
        }

        DistanceColorizer(int maxvalue){
            this.maxvalue = maxvalue;
        }

        public long get(long value){

            if(value == -undefined) {
                return makeRGB(MAXC, MAXC,0);
            } else if(value == undefined) {
                return makeRGB(0, MAXC,MAXC);
            }

            int r = 0;
            int g = 0;
            int b = 0;

            if (value == 0) {

                r = zr;
                g = zg;
                b = zb;

            } else {

                //int v = (int)(MAXC  - (value * MAXC / maxvalue) & MAXC);
                int v = (map((int)value, -maxvalue,maxvalue,-MAXC,MAXC));
                if (value < 0) {
                    r =  (-v);
                } else {
                    b =  (v);
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

    /**

     */
    static class ErrorColorizer implements LongConverter {

        int maxvalue = 100 * 2;  // compress range
        int min = 128;
        int undefined = Short.MAX_VALUE;

        ErrorColorizer(int maxvalue){

            this.maxvalue = maxvalue;
        }

        public long get(long value){
            if(value == undefined) {
                return makeRGB(MAXC, 0,0);
            } else if(value == -undefined) {
                return makeRGB(0,0,MAXC);
            }

            if( value > 0){
                int v = (int)(min + (MAXC - value * MAXC / maxvalue) & MAXC);
                return makeRGB(v, v, v);
            } else {
                return makeRGB(MAXC, MAXC, MAXC);
            }
        }
    }
    /**

     */
    static class DensityColorizer implements LongConverter {

        int maxvalue = 100;
        DensityColorizer(int maxvalue){

            this.maxvalue = maxvalue;
        }

        public long get(long value){

            int v = (int)((MAXC -  MAXC*value/ maxvalue) & MAXC);
            return makeRGB(v, v, v);
            
        }
    }
}
