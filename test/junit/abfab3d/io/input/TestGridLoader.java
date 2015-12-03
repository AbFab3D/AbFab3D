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

import abfab3d.util.ColorMapper;
import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapperDensity;

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

import abfab3d.distance.DistanceData;
import abfab3d.distance.DistanceDataSphere;

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

    public void testDistancePrecision() throws Exception {

        int densityBitCount = 8;
        int distanceBitCount = 16;
        double voxelSize = 2*MM;
        int magnification = 20;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
        double maxInDistance = 5*MM;
        double maxOutDistance = 5*MM;

        
        String path  = "test/models/sphere_10cm_5K_tri.stl"; //10cm diameter sphere
        //String path  = "test/models/sphere_10cm_32K_tri.stl";
        //String path  = "test/models/sphere_10cm_400K_tri.stl";
        

        printf("voxelSize: %5.2 mm\n", voxelSize/MM);
        printf("path: %s\n", path);
        
        GridLoader loader = new GridLoader();
        loader.setDensityBitCount(densityBitCount);
        loader.setDistanceBitCount(distanceBitCount);
        loader.setPreferredVoxelSize(voxelSize);
        loader.setDensityAlgorithm(rasterAlgorithm);
        loader.setMaxInDistance(maxInDistance);
        loader.setMaxOutDistance(maxOutDistance);
        loader.setShellHalfThickness(2.5);
        
        AttributeGrid grid = loader.loadDistanceGrid(path);
        
        Difference diff = getDifference(grid, new DistanceDataSphere(50*MM));
        
        printf("diffMax: %7.3f voxels\n", diff.diffmax/voxelSize);
        printf("  diff1: %7.3f voxels\n", diff.diff1/voxelSize);
        printf("  diff2: %7.3f voxels\n", diff.diff2/voxelSize);

        assertTrue("((maxdiff < 0.19) != true)\n", (diff.diffmax/voxelSize < 0.19));
        assertTrue("((diff1 < 0.036) != true)\n", (diff.diff1/voxelSize < 0.036));
        assertTrue("((diff2 < 0.044) != true)\n", (diff.diff2/voxelSize < 0.044));
        
        if(false){
            AttributeChannel dataChannel = grid.getAttributeDesc().getChannel(0);
            ColorMapper colorMapper = new ColorMapperDistance(0xFF00FF00,0xFFDDFFDD, 0xFF0000FF,0xFFDDDDFF, 2*MM);
            int iz = grid.getDepth()/2;
            GridUtil.writeSlice(grid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dist%03d.png", iz));
        }   
    }
    

    /**
       load STL file and write its density to a slice
     */
    public void devTestSTL_density() throws Exception {
        
        printf("devTestSTL()\n");
        int densityBitCount = 8;
        int distanceBitCount = 16;
        double voxelSize = 2*MM;
        int magnification = 4;
        //int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE2;
        //int rasterAlgorithm = GridLoader.RASTERIZER_ZBUFFER;
        //int rasterAlgorithm = GridLoader.RASTERIZER_WAVELET;

        String path[] = new String[] {
            "test/models/sphere_10cm_.4K_tri.stl",
            //"test/models/sphere_10cm_5K_tri.stl",
            //            "test/models/sphere_10cm_32K_tri.stl",
            //"test/models/gyrosphere.stl",
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
            //for(int iz = densGrid.getDepth()/2; iz < densGrid.getDepth()/2+1; iz++){
            for(int iz = 0; iz < densGrid.getDepth(); iz++){
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
        double voxelSize = 0.5*MM;
        double bandWidth = 2*MM;
        double maxInDistance = 50*MM;
        double maxOutDistance = 50*MM;
        int magnification = 1;
        int threadCount = 4;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE2;
        //int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
        //int rasterAlgorithm = GridLoader.RASTERIZER_ZBUFFER;
        //int rasterAlgorithm = GridLoader.RASTERIZER_WAVELET;

        String path[] = new String[] {
            //"test/models/sphere_10cm_.4K_tri.stl",
            //"test/models/sphere_10cm_5K_tri.stl",
            //"test/models/gyrosphere.stl",
            //"test/models/sphere_10cm_32K_tri.stl",
            //"test/models/sphere_10cm_400K_tri.stl"
            "test/models/deer.stl"

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
            //for(int iz = grid.getDepth()/2; iz < grid.getDepth()/2+1; iz++){
            for(int iz = 0; iz < grid.getDepth(); iz += 1){
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
    
    static Difference getDifference(AttributeGrid grid, DistanceData distData){
        
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double maxDiff = 0;
        double diffSum = 0;
        double diff2Sum = 0;
        int count = 0;

        AttributeChannel dataChannel = grid.getAttributeDesc().getChannel(0);
        double pnt[] = new double[3];
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    grid.getWorldCoords(x, y, z, pnt);
                    double exactDist = distData.getDistance(pnt[0],pnt[1],pnt[2]);
                    double gridDist = dataChannel.getValue(grid.getAttribute(x,y,z));
                    double diff = Math.abs(gridDist - exactDist);
                    if(diff > maxDiff)
                        maxDiff = diff;
                    diffSum += diff;
                    diff2Sum += diff*diff;
                    count++;
                }
            }
        }
        return new Difference(maxDiff, diffSum/count, Math.sqrt(diff2Sum/count));
    }


    static class Difference {
        double diffmax;
        double diff1;
        double diff2;
        
        Difference(double diffmax, double diff1, double diff2){
            this.diffmax = diffmax;
            this.diff1 = diff1;
            this.diff2 = diff2;
        }

    }

    static int debugCount = 1000;

    public static void main(String[] args) throws Exception{

        for(int k = 0; k < 1; k++){
            //new TestGridLoader().devTestSTL_density();
            new TestGridLoader().devTestSTL_distance();
            //new TestGridLoader().testDistancePrecision();
        }
    }
}
