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

// External Imports


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;


// Internal Imports
import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataDesc;
import abfab3d.core.GridDataChannel;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.op.GridMaker;

import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapper;

import abfab3d.core.Bounds;

import abfab3d.io.input.ModelLoader;
import abfab3d.io.input.SVXReader;

import abfab3d.io.output.SVXWriter;
import abfab3d.io.output.GridSaver;



import static abfab3d.core.Output.printf;

import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;

import static abfab3d.grid.util.GridUtil.writeSlice;



/**
 * Tests the functionality of DataSourceGrid
 *
 * @version
 */
public class TestDataSourceGrid extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDataSourceGrid.class);
    }

    public void testNothing(){
    }


    public void devTestModelLoading()throws Exception {
        
        
        double vs = 0.2*MM;
        double maxOutDist = 2*MM + vs;
        double maxInDist = 1*MM + vs;
        double distanceBand = 1*MM;
        
        String filePath = "test/models/boxITS_4x4x0.4cm_slanted.x3d";
        String svxFile = "/tmp/00_boxITS_4x4x0.4cm_slanted.svx";
        String outPath = "/tmp/01_boxITS_4x4x0.4cm_slanted_out.stl";
        
        long t0 = time();
        long 
            lt=0, wt = 0, rt = 0;
        
        ModelLoader loader = new ModelLoader(filePath, vs, maxOutDist + vs);
        loader.setMaxOutDistance(maxOutDist);
        loader.setMaxInDistance(maxInDist);
        loader.setShellHalfThickness(1.);
        loader.setAttributeLoading(false);

        AttributeGrid modelGrid = loader.getGrid();
        lt = time() - t0;
        printf("model loading time: %d ms\n", lt);

        //DevTestUtil.printGridSliceValueX(modelGrid, modelGrid.getWidth()/2, modelGrid.getDataChannel(), "%3.0f ", 10000);        

        Bounds bounds = modelGrid.getGridBounds();
        GridSaver saver = new GridSaver();
        //saver.setMeshErrorFactor(0.3);
        //saver.setMeshSmoothingWidth(0.5);
        DataSourceGrid model = new DataSourceGrid(modelGrid);
        model.initialize();
        //DevTestUtil.printRay(model, new Vector3d(bounds.getCenterX(), bounds.getCenterY(), bounds.zmin),new Vector3d(0,0,vs/2), vs, 2*modelGrid.getDepth()+1);
        GridMaker gridMaker = new GridMaker();
        
        gridMaker.setSource(model);
        AttributeGrid outGrid = DevTestUtil.makeDistanceGrid(bounds, vs, maxOutDist);
        gridMaker.makeGrid(outGrid);
        saver.write(outGrid, "/tmp/01_model.stl");
        //DevTestUtil.printGridSliceValueX(outGrid, outGrid.getWidth()/2, outGrid.getDataChannel(), "%3.0f ", 10000);        
        
        int nv = 3000;
        int nu = (int)(nv * bounds.getSizeZ()/bounds.getSizeY());
        Vector3d eu = new Vector3d(0, 0, bounds.getSizeZ()/nu);
        Vector3d ev = new Vector3d(0, bounds.getSizeY()/nv, 0);
        Vector3d sliceOrigin = new Vector3d(bounds.getCenterX(), bounds.ymin, bounds.zmin);
        ColorMapper cm = new ColorMapperDistance(distanceBand);

        writeSlice(outGrid,outGrid.getDataChannel(), cm, sliceOrigin, eu, ev,nu, nv, "/tmp/00_outModelSliceX.png");

        Add dilatedModel = new Add(model, new Constant(-1.8*MM));
        gridMaker.setSource(dilatedModel);
        AttributeGrid dilatedGrid = DevTestUtil.makeDistanceGrid(bounds, vs, maxOutDist);
        gridMaker.makeGrid(dilatedGrid);
        saver.write(dilatedGrid, "/tmp/02_dilatedModel_2.10.stl");
        //DevTestUtil.printGridSliceValueX(dilatedGrid, dilatedGrid.getWidth()/2, dilatedGrid.getDataChannel(), "%3.0f ", 10000);        

    }
        
    public void devTestBytePacking(){
        
        testGrid( makeGridByte(2));
        testGrid( makeGridShort(2));
        testGrid( makeGridInt(2));

    }

    public void devTestPerf() {
        AttributeGrid grid = makeGridByte(400);
//        AttributeGrid grid = makeGridInt(400);

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    grid.setAttribute(x,y,z,(z+1) | ((x+1) << 4) | ((y+1) << 8));
                }
            }
        }

        DataSourceGrid dsg = new DataSourceGrid(grid);
        dsg.initialize();

        int data[] = new int[dsg.getBufferSize()];

        int TIMES = 5;

        for(int i=0; i < TIMES; i++) {
            long t0 = time();
            dsg.getBuffer(data);
            printf("time: %d ms\n", time() - t0);
        }
    }


    void testGrid(AttributeGrid grid){
        printf("test grid: %s\n", grid);
    
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    grid.setAttribute(x,y,z,(z+1) | ((x+1) << 4) | ((y+1) << 8));
                }
            }        
        }
        
        DataSourceGrid ds = new DataSourceGrid(grid);
        int data[] = new int[ds.getBufferSize()];
        ds.getBuffer(data);
        for(int i = 0; i < data.length; i++){
            printf("%08x\n", data[i]);
        }
    }

    AttributeGrid makeGridByte(int scale){

        double vs = 1*MM;
        double s = scale*vs;
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(new Bounds(-s,s,-s,s,-s,s), vs, vs);
        grid.setDataDesc(new GridDataDesc(new GridDataChannel(GridDataChannel.DISTANCE, "dist", 8,0,-1*MM, 1*MM)));
        return grid;

    }

    AttributeGrid makeGridShort(int scale){

        double vs = 1*MM;
        double s = scale*vs;
        ArrayAttributeGridShort grid = new ArrayAttributeGridShort(new Bounds(-s,s,-s,s,-s,s), vs, vs);
        grid.setDataDesc(new GridDataDesc(new GridDataChannel(GridDataChannel.DISTANCE, "dist", 16,0,-1*MM, 1*MM)));
        return grid;

    }

    AttributeGrid makeGridInt(int scale){

        double vs = 1*MM;
        double s = scale*vs;
        ArrayAttributeGridInt grid = new ArrayAttributeGridInt(new Bounds(-s,s,-s,s,-s,s), vs, vs);
        grid.setDataDesc(GridDataDesc.getDistBGR(2*vs));
        return grid;

    }

    public static void main(String[] args) throws Exception {
        new TestDataSourceGrid().devTestModelLoading();
    }
}