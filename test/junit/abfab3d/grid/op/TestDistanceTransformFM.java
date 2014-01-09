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

import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.grid.*;
import abfab3d.io.output.SlicesWriter;
import abfab3d.transforms.Rotation;
import abfab3d.util.Long2Short;
import abfab3d.util.LongConverter;
import junit.framework.TestCase;

import java.util.Random;

import static abfab3d.util.ImageUtil.*;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static java.lang.Math.round;

// Internal Imports

/**
 *
 * @author Alan Hudson
 * @version
 */
public class TestDistanceTransformFM extends TestCase {
    
    double surfareThickness = Math.sqrt(3)/2;
    int maxAttribute = 100;
    double voxelSize = 0.1*MM;

    public void testBox(){

        int max_attribute = 128;
        int nx = 100;
//        AttributeGrid grid = makeBox(nx, 4.0 * MM);
        AttributeGrid grid = makeSphere(nx, 2.0 * MM);
        double maxInDistance = 1*MM;
        double maxOutDistance = 0;

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        t0 = time();
        DistanceTransformFM dt_fm = new DistanceTransformFM(max_attribute, maxInDistance, maxOutDistance);

        AttributeGrid dg_fm = dt_fm.execute(grid);
        printf("DistanceTransformFM done: %d ms\n", time() - t0);

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        int diffs_in_exact = 0;
        int diffs_in_fm = 0;

//        long not_calced_inside = dt_exact.getInsideDefault();
//        long not_calced_outside = dt_exact.getOutsideDefault();

        long not_calced_inside = Short.MAX_VALUE;
        long not_calced_outside = -Short.MAX_VALUE;

        long total_error = 0;
        long max_error = 0;
        long err_cnt = 0;

        long ma = (long) ((double) max_attribute * maxInDistance / grid.getVoxelSize());

        AttributeGrid diff_grid = (AttributeGrid) dg_exact.createEmpty(dg_exact.getWidth(),dg_exact.getHeight(),dg_exact.getDepth(),
                dg_exact.getVoxelSize(), dg_exact.getSliceHeight());

        printf("Max attribute: %d\n",ma);
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    // TODO: do we want caller to have to know this?

                    long att_exact = (short) dg_exact.getAttribute(x,y,z);
                    long att_fm = (short) dg_fm.getAttribute(x,y,z);

                    if ((att_exact == not_calced_outside || att_exact == not_calced_inside) &&
                        att_fm != not_calced_outside && att_fm != not_calced_inside) {
                        diffs_in_fm++;
                        // view as blue
                        diff_grid.setAttribute(x,y,z,-Short.MAX_VALUE);
                    } else if ((att_fm == not_calced_outside || att_fm == not_calced_inside) &&
                                att_exact != not_calced_outside && att_exact != not_calced_inside) {
                        diffs_in_exact++;
                        // view as red
                        diff_grid.setAttribute(x,y,z,Short.MAX_VALUE);
                    } else {
                        long diff = Math.abs(att_exact - att_fm);

                        diff_grid.setAttribute(x,y,z,diff);

                        if (diff > max_error) {
                            printf("new max error: %d  exact: %d  fm: %d\n", diff, att_exact, att_fm);
                            max_error = diff;
                        }
                        total_error += diff;
                        err_cnt++;
                    }
                }
            }
        }

        printf("Total error: %d  Per Error: %f  Max error: %d  Init Diffs  in exact: %d  in_fm: %d\n",total_error,((double) total_error / ma / err_cnt * 100.0),max_error,diffs_in_exact, diffs_in_fm);

        // viz:  difference greyscale.  blue for not in exact but in compare, red for in exact but not in compare

        int norm = (int)round(maxAttribute*maxInDistance/voxelSize);

        MyGridWriter gw = new MyGridWriter(8,8);
        gw.writeSlices(diff_grid,norm , "/tmp/slices/distance/exact_%03d.png",0, nx, new ErrorColorizer(norm));
        printRow(diff_grid, 0, nx, nx/2, nx/2);


    }

    AttributeGrid makeBox(int gridSize, double boxWidth){
        
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
        gm.setVoxelScale(surfareThickness);

        gm.setSource(box);
        gm.makeGrid(grid);
        return grid;
    }

    AttributeGrid makeSphere(int gridSize, double radius){

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
        gm.setMaxAttributeValue(maxAttribute); // correct normalization is 2*
        gm.setVoxelScale(surfareThickness);

        gm.setSource(sphere);
        gm.makeGrid(grid);
        return grid;
    }

    // makeCylinder --> Vlad said Cylinder would be the worst case

    static class MyGridWriter implements SliceExporter {

        int cellSize = 1;
        int voxelSize = 1;
        MyGridWriter(int cellSize, int voxelSize){
            this.cellSize = cellSize;
            this.voxelSize = voxelSize;
        }
        public void writeSlices(Grid grid, long maxAttribute, String filePattern, int start, int end, LongConverter colorMaker ){
            
            SlicesWriter slicer = new SlicesWriter();
            slicer.setDataConverter(new Long2Short());
            slicer.setCellSize(cellSize);
            slicer.setVoxelSize(voxelSize);
            
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
        printf("printRow(%d %d %d %d)\n",xmin, xmax, y,z);
        for(int x = xmin; x < xmax; x++){
            short v = (short)(grid.getAttribute(x,y,z));
            if(v == Short.MAX_VALUE)
                printf("   +");
            else if(v == -Short.MAX_VALUE)
                printf("   .");
            else
                printf("%4d",v);
            if((x+1)%25 == 0)
                printf("\n");
        }

    }



    /**

     */
    static class DistanceColorizer implements LongConverter {

        int maxvalue = 100;
        int undefined = Short.MAX_VALUE;

        DistanceColorizer(int maxvalue){

            this.maxvalue = maxvalue;
        }

        public long get(long value){
            if(value == undefined) {
                return makeRGB(MAXC, 0,0);
            } else if(value == -undefined) {
                return makeRGB(0,0,MAXC);
            }

            if( value >= 0){
                int v = (int)(MAXC  - (value * MAXC / maxvalue) & MAXC);
                return makeRGB(v, v, v);
            } else {
                return makeRGBA(0,0,0,0);
            }
        }
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
                return makeRGB(MAXC,MAXC,MAXC);
            }
        }
    }
}

