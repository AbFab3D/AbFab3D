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

package abfab3d.io.input;

// External Imports


import java.util.Map;


// external imports
import abfab3d.grid.query.CountMaterials;
import abfab3d.grid.query.CountStates;
import abfab3d.util.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.AttributeDesc;
import abfab3d.grid.AttributeChannel;

import abfab3d.geom.TriangulatedModels;

import abfab3d.io.output.STLWriter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SlicesWriter;

import abfab3d.util.MathUtil;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.CM3;
import static abfab3d.util.Units.MM;


/**
 * Tests the functionality of GridLoader
 *
 * @version
 */
public class TestGridLoader extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGridLoader.class);
    }

    public void testNothing() throws Exception {
        
    }

    /**
       makes a set of boxes and checks volume 
     */
    public void devTestSTL() throws Exception {
        
        printf("devTestSTL()\n");
        int densityBitCount = 3;
        double voxelSize = 0.2*MM;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
        //int rasterAlgorithm = GridLoader.RASTERIZER_ZBUFFER;
        //int rasterAlgorithm = GridLoader.RASTERIZER_WAVELET;

        String path[] = new String[] {
            "test/models/sphere_10cm_.4K_tri.stl",
            "test/models/sphere_10cm_5K_tri.stl",
            "test/models/sphere_10cm_32K_tri.stl",
            "test/models/sphere_10cm_400K_tri.stl"
        };

            
        GridLoader loader = new GridLoader();
        loader.setMaxGridSize(1000000000L);
        loader.setDensityBitCount(densityBitCount);
        loader.setPreferredVoxelSize(voxelSize);
        loader.setDensityAlgorithm(rasterAlgorithm);
        
        for(int i = 0; i < path.length; i++){
            printf("voxelSize: %7.2f mm \n", voxelSize/MM);
            printf("densityBitCount:%d\n", densityBitCount);
            printf("loading %s ms\n", path[i]);
            long t0 = time();
            AttributeGrid densGrid = loader.loadDensityGrid(path[i]);
            printf("grid %s loaded in %d ms\n", path[i], (time() - t0));
            t0 = time();
            double volume = getVolume(densGrid);
            printf("volume: %7.3f CM^3 in %d ms\n", volume/CM3, (time() - t0));
        }
    }

    /**
       calculates volume of the shape defined as voxels
     */
    static double getVolume(AttributeGrid grid){

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        AttributeDesc ad = grid.getAttributeDesc();
        AttributeChannel ch = ad.getDensityChannel();
        double sum = 0;
        double voxelVolume = grid.getVoxelSize();
        voxelVolume  = voxelVolume*voxelVolume*voxelVolume;

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    sum += ch.getValue(grid.getAttribute(x,y,z));                    
                }                
            }            
        }
        return sum*voxelVolume;
    }

    public static void main(String[] args) throws Exception{
        for(int k = 0; k < 2; k++){
            new TestGridLoader().devTestSTL();
        }
    }
}
