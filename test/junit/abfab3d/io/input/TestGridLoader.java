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

import abfab3d.grid.util.GridUtil;

import abfab3d.geom.TriangulatedModels;

import abfab3d.io.output.STLWriter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SlicesWriter;

import abfab3d.util.MathUtil;
import abfab3d.util.ColorMapper;
import abfab3d.util.ColorMapperDensity;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.CM3;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.lerp2;
import static abfab3d.util.ImageUtil.lerpColors;


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
       load STL file and write its density to a slice
     */
    public void devTestSTL_density() throws Exception {
        
        printf("devTestSTL()\n");
        int densityBitCount = 8;
        int distanceBitCount = 16;
        double voxelSize = 0.1*MM;
        int magnification = 4;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
        //int rasterAlgorithm = GridLoader.RASTERIZER_ZBUFFER;
        //int rasterAlgorithm = GridLoader.RASTERIZER_WAVELET;

        String path[] = new String[] {
            //"test/models/sphere_10cm_.4K_tri.stl",
            //"test/models/sphere_10cm_5K_tri.stl",
            //"test/models/sphere_10cm_32K_tri.stl",
            "test/models/gyrosphere.stl",
            //"test/models/sphere_10cm_400K_tri.stl"
        };

            
        GridLoader loader = new GridLoader();
        loader.setMaxGridSize(1000*1000*1000L);
        loader.setDensityBitCount(densityBitCount);
        loader.setDistanceBitCount(distanceBitCount);
        loader.setPreferredVoxelSize(voxelSize);
        loader.setDensityAlgorithm(rasterAlgorithm);
        
        for(int i = 0; i < path.length; i++){
            printf("voxelSize: %7.2f mm \n", voxelSize/MM);
            printf("rasterization algorithm: %s \n", GridLoader.getAlgorithmName(rasterAlgorithm));            
            printf("loading %s\n", path[i]);
            long t0 = time();
            AttributeGrid densGrid = loader.loadDensityGrid(path[i]);
            printf("grid %s loaded in %d ms\n", path[i], (time() - t0));
            t0 = time();
            double volume = getVolume(densGrid);
            printf("volume: %7.3f CM^3 in %d ms\n", volume/CM3, (time() - t0));
            for(int iz = densGrid.getDepth()/2; iz < densGrid.getDepth()/2+1; iz++){
            //for(int iz = 0; iz < densGrid.getDepth(); iz++){
                AttributeChannel dataChannel = densGrid.getAttributeDesc().getChannel(0);
                ColorMapper colorMapper = new ColorMapperDensity(0xFF000000, 0xFFFF0000, 1./2);
                GridUtil.writeSlice(densGrid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dens%03d.png", iz));
                //GridUtil.printSliceAttribute(densGrid, iz);
            }
        }
    }

    /**
       load STL file and write its density to a slice
     */
    public void devTestSTL_distance() throws Exception {
        
        printf("devTestSTL()\n");
        int densityBitCount = 8;
        int distanceBitCount = 16;
        double voxelSize = 0.1*MM;
        double bandWidth = 1*MM;
        double maxInDistance = 50*MM;
        double maxOutDistance = 50*MM;
        int magnification = 2;
        int threadCount = 4;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
        //int rasterAlgorithm = GridLoader.RASTERIZER_ZBUFFER;
        //int rasterAlgorithm = GridLoader.RASTERIZER_WAVELET;

        String path[] = new String[] {
            //"test/models/sphere_10cm_.4K_tri.stl",
            //"test/models/sphere_10cm_5K_tri.stl",
            "test/models/gyrosphere.stl",
            //"test/models/sphere_10cm_32K_tri.stl",
            //"test/models/sphere_10cm_400K_tri.stl"
        };

            
        GridLoader loader = new GridLoader();
        loader.setMaxGridSize(1000*1000*1000L);
        loader.setDensityBitCount(densityBitCount);
        loader.setDistanceBitCount(distanceBitCount);
        loader.setPreferredVoxelSize(voxelSize);
        loader.setDensityAlgorithm(rasterAlgorithm);
        loader.setMaxInDistance(maxInDistance);
        loader.setMaxOutDistance(maxOutDistance);
        loader.setShellHalfThickness(2);
        loader.setThreadCount(threadCount);
        
        for(int i = 0; i < path.length; i++){
            printf("voxelSize: %7.2f mm \n", voxelSize/MM);            
            printf("rasterization algorithm: %s \n", GridLoader.getAlgorithmName(rasterAlgorithm));            
            printf("loading %s\n", path[i]);
            long t0 = time();
            AttributeGrid grid = loader.loadDistanceGrid(path[i]);
            printf("grid %s loaded in %d ms\n", path[i], (time() - t0));
            t0 = time();
            for(int iz = grid.getDepth()/2; iz < grid.getDepth()/2+1; iz++){
                //for(int iz = 0; iz < grid.getDepth(); iz++){
                AttributeChannel dataChannel = grid.getAttributeDesc().getChannel(0);
                ColorMapper colorMapper = new ColorMapperDistance(0xFF00FF00,0xFFDDFFDD, 0xFF0000FF,0xFFDDDDFF, bandWidth);
                GridUtil.writeSlice(grid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dist%03d.png", iz));
            }
        }
    }

    /**
       calculates volume of the shape defined as voxels
     */
    double getVolume(AttributeGrid grid){
        
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        AttributeDesc ad = grid.getAttributeDesc();
        AttributeChannel ch = ad.getDensityChannel();
        double sum = 0;
        double voxelVolume = grid.getVoxelSize();
        voxelVolume = voxelVolume*voxelVolume*voxelVolume;

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    sum += ch.getValue(grid.getAttribute(x,y,z));                    
                }                
            }            
        }
        return sum*voxelVolume;
    }

    static int debugCount = 1000;

    public static void main(String[] args) throws Exception{
        for(int k = 0; k < 4; k++){
            //new TestGridLoader().devTestSTL_density();
            new TestGridLoader().devTestSTL_distance();
        }
    }
}
