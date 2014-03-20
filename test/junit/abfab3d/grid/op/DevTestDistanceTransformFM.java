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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

// Internal Imports
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.grid.SliceExporter;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.io.output.SlicesWriter;

import abfab3d.datasources.Sphere;
import abfab3d.datasources.Cone;
import abfab3d.datasources.Cylinder;
import abfab3d.datasources.Box;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Subtraction;
import abfab3d.datasources.Union;
import abfab3d.datasources.Plane;
import abfab3d.transforms.Rotation;

import abfab3d.util.DataSource;
import abfab3d.util.Vec;
import abfab3d.util.VecTransform;
import abfab3d.util.Long2Short;
import abfab3d.util.LongConverter;


import abfab3d.datasources.GridMipMap;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Units.MM;
import static abfab3d.util.ImageUtil.makeRGB;
import static abfab3d.util.ImageUtil.makeRGBA;
import static abfab3d.util.ImageUtil.MAXC;

import static java.lang.Math.sin;
import static java.lang.Math.round;
import static java.lang.Math.PI;

/**
 *
 * @author Vladimir Bulatov
 * @version
 */
public class DevTestDistanceTransformFM {
    
    double surfaceThickness = 0.5;//Math.sqrt(3)/2;
    int maxAttribute = 100;
    double voxelSize = 0.1*MM;


    void testUpwindSolver1(){
        
        double a[] = new double[]{175,212,235};
        double h = 100;
        double f = DistanceTransformFM.getUpwindSolution(a,h);
        printf("(%5f %5f %5f )h:%5f -> %5f \n",a[0],a[1], a[2], h, f);
        
        
    }

    void testUpwindSolver(){
        int ia[] = new int[3];
        double a[] = new double[3];

        int mm = 100;        
        Random rnd = new Random(101);
        int h = 100;
        double maxError = 0;
        double a0=0, a1=0, a2=0,ff = 0;
        long t0 = time();
        for(int i = 0; i < 10000000; i++){            

            ia[0] = rnd.nextInt(mm); 
            ia[1] = ia[0] + rnd.nextInt(4*h)-2*h;
            ia[2] = ia[0] + rnd.nextInt(4*h)-2*h;       
            a[0] = ia[0];
            a[1] = ia[1];
            a[2] = ia[2];


            //double f = DistanceTransformFM.getUpwindSolution(a,h);
            int iff = DistanceTransformFM.getUpwindSolutionInt(ia,h);
            //double ee = DistanceTransformFM.checkUpwindSolution(f, a,h);
            //int iee = DistanceTransformFM.checkUpwindSolution(iff, ia,h);
            int iee = 0;
            if(iee > maxError){                
                maxError = iee;
                a0 = ia[0];
                a1 = ia[1];
                a2 = ia[2];
                ff = iff;
            }   
         
            
            //printf("a: %3d %3d %3d  -> %3d err: %3d \n", a[0],a[1],a[2],f, ee);
            //printf("a: %5.1f %5.1f %5.1f  -> %5.1f err: %10.3e (%3d  %3d)\n", a[0],a[1],a[2],f, ee, iff, iee);
        }
        printf("maxError: %g for(%5f %5f %5f ): %5f \n",maxError, a0, a1, a2,ff);
        printf("time: %d ms\n", time() - t0);
            
    }

    void testBoxExact(){

        int nx = 200;
        AttributeGrid grid = makeBox(nx, 4.0*MM);
        MyGridWriter gw = new MyGridWriter();
        double maxInDistance = 1*MM;
        double maxOutDistance = 0*MM;

        gw.writeSlices(grid, maxAttribute, "/tmp/slices/ex_grid_%03d.png",nx/2, nx/2+1, null);
        DistanceTransformExact dt = new DistanceTransformExact(maxAttribute, maxInDistance, maxOutDistance);
        long t0 = time();
        AttributeGrid dg = dt.execute(grid);        
        printf("DistanceTransformExact done: %d ms\n", time() - t0); 
        int norm = (int)round(maxAttribute*maxInDistance/voxelSize);
        gw.writeSlices(dg, norm, "/tmp/slices/ex_dist_%03d.png",nx/2, nx/2+1, new DistanceColorizer(norm));
        printRow(dg, 0, nx, nx/2, nx/2);

        
    }

    void testBoxFM(){

        int nx = 200;
        AttributeGrid grid = makeBox(nx, 4.0*MM);

        MyGridWriter gw = new MyGridWriter();
        gw.writeSlices(grid, maxAttribute, "/tmp/slices/fm_grid_%03d.png",nx/2, nx/2+1, null);
        
        double maxInDistance = 1*MM;
        double maxOutDistance = 0*MM;
        
        printf("grid: [%d x %d x %d]\n", grid.getWidth(), grid.getHeight(), grid.getDepth()); 
        printf("starting DistanceTransformFM (%d %7.3f mm %7.3f mm)\n", maxAttribute, maxInDistance/MM, maxOutDistance/MM); 

        long t0 = time();

        DistanceTransformFM dt = new DistanceTransformFM(maxAttribute, maxInDistance, maxOutDistance);
        //dt.setSliceExporter(new MyGridWriter());

        AttributeGrid dg = dt.execute(grid);
        
        printf("DistanceTransformFM done: %d ms\n", time() - t0); 
        int norm = (int)round(maxAttribute*maxInDistance/voxelSize);
        gw.writeSlices(dg,norm , "/tmp/slices/fm_dist_%03d.png",nx/2, nx/2+1, new DistanceColorizer(norm));
        printRow(dg, 0, nx, nx/2, nx/2);
    }


    AttributeGrid makeBox(int gridSize, double boxWidth){
        
        double width = gridSize*voxelSize;
        
        int nx = gridSize;
        int ny = nx;
        int nz = nx;

        double sx = nx*voxelSize;
        double sy = ny*voxelSize;
        double sz = nz*voxelSize;

        double xoff = 0;//voxelSize/2;
        double yoff = 0;
        double zoff = 0;

        double bounds[] = new double[]{-sx/2, sx/2, -sy/2, sy/2, -sz/2, sz/2};        
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);        
        grid.setGridBounds(bounds);

        //Box box = new Box((nx - 4)*voxelSize, (ny - 4) * voxelSize, (nz-30)*voxelSize);
        Box box = new Box(xoff,yoff,zoff,boxWidth, 0.8*ny * voxelSize, (nz-30)*voxelSize);
        box.setTransform(new Rotation(0,0,1,Math.PI*0.1));
        //box.setTransform(new Rotation(0,0,1,Math.PI/4+0.01));
        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(maxAttribute); 
        gm.setVoxelScale(surfaceThickness);

        gm.setSource(box);
        gm.makeGrid(grid);
        return grid;
    }

    // special slice writer 
    static class MyGridWriter implements SliceExporter {

        int cellSize = 8;
        int voxelSize = 8;
        MyGridWriter(){
        }
        public void writeSlices(Grid grid, long maxAttribute, String filePattern, int start, int end, LongConverter colorMaker ){
            printf("%s.writeSlices(%s, %d %d %d)\n", this, filePattern, start, end, maxAttribute);
            SlicesWriter slicer = new SlicesWriter();
            slicer.setDataConverter(new Long2Short());
            slicer.setCellSize(cellSize);
            slicer.setVoxelSize(voxelSize);
            
            slicer.setMaxAttributeValue((int)maxAttribute);
            if(colorMaker != null){
                slicer.setColorMaker(colorMaker);
            }
            int sliceMin = grid.getDepth()/2;
            int sliceMax = sliceMin+1;
            //int sliceMax = grid.getDepth() -1;
            
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
                printf("  + ");
            else if(v == -Short.MAX_VALUE)
                printf("  . ");
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


    public static void main(String[] args) throws Exception{
        
        DevTestDistanceTransformFM dt = new DevTestDistanceTransformFM();
        //dt.testUpwindSolver();

        //dt.testBoxFM();
        dt.testBoxExact();
    }
}
