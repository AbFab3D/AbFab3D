/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
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

import abfab3d.datasources.Sphere;
import abfab3d.datasources.Torus;
import abfab3d.datasources.VolumePatterns;
import abfab3d.grid.*;
import abfab3d.grid.query.RegionFinder;
import abfab3d.io.output.SlicesWriter;
import abfab3d.transforms.Rotation;
import abfab3d.util.Units;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;

import javax.vecmath.Vector3d;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static abfab3d.util.Output.printf;

// Internal Imports

/**
 * Tests the functionality of the DownsampleAlpha operation
 *
 * @author Alan Hudson
 * @version
 */
public class TestDownsampleAlpha extends BaseTestAttributeGrid {
    private static final boolean DEBUG = false;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDownsampleAlpha.class);
    }

    /**
     * Simple average of all maxes
     */
    public void testSimpleAverageMaxed() {
        int size = 4;
        int factor = 2;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        int max = 127;

        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                for(int z=0; z < size; z++) {
                    grid.setData(x,y,z,Grid.INSIDE,max);
                }
            }
        }
        DownsampleAlpha ds = new DownsampleAlpha(0,2,max);
        AttributeGrid dest = ds.execute(grid);

        int new_size = dest.getWidth();
        for(int y=0; y < new_size; y++) {
            for(int x=0; x < new_size; x++) {
                for(int z=0; z < new_size; z++) {
                    assertEquals("Dest value",max, dest.getAttribute(x,y,z));
                }
            }
        }

        // Test grid is factor sized

        assertEquals("Grid factor w", size / factor, dest.getWidth());
        assertEquals("Grid factor h", size / factor, dest.getHeight());
        assertEquals("Grid factor d", size / factor, dest.getDepth());

    }

    /**
     * Binary coeff of all inside
     */
    public void testBinaryCoeffMaxed() {
        int size = 4;
        int factor = 2;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        int max = 127;

        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                for(int z=0; z < size; z++) {
                    grid.setState(x,y,z,Grid.INSIDE);
                }
            }
        }
        DownsampleAlpha ds = new DownsampleAlpha(true,1,2,max);
        AttributeGrid dest = ds.execute(grid);

        int new_size = dest.getWidth();
        for(int y=0; y < new_size; y++) {
            for(int x=0; x < new_size; x++) {
                for(int z=0; z < new_size; z++) {
                    assertEquals("Dest value",max, dest.getAttribute(x,y,z));
                }
            }
        }

        // Test grid is factor sized

        assertEquals("Grid factor w", size / factor, dest.getWidth());
        assertEquals("Grid factor h", size / factor, dest.getHeight());
        assertEquals("Grid factor d", size / factor, dest.getDepth());

    }

    /**
     * Simple average of all zeros
     */
    public void testSimpleAverageZero() {
        int size = 4;
        int factor = 2;
        int max = 127;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                for(int z=0; z < size; z++) {
                    grid.setData(x,y,z,Grid.OUTSIDE,0);
                }
            }
        }
        DownsampleAlpha ds = new DownsampleAlpha(0,factor,max);
        AttributeGrid dest = ds.execute(grid);

        int new_size = dest.getWidth();
        for(int y=0; y < new_size; y++) {
            for(int x=0; x < new_size; x++) {
                for(int z=0; z < new_size; z++) {
                    assertEquals("Dest value",0, dest.getAttribute(x,y,z));
                }
            }
        }

        assertEquals("Grid factor w", size / factor, dest.getWidth());
        assertEquals("Grid factor h", size / factor, dest.getHeight());
        assertEquals("Grid factor d", size / factor, dest.getDepth());

    }

    /**
     * Simple average
     *
     * xz plane, copied in y.  a = 64   b = 6/8 * 64 = 48
     *
     * a000
     * aa00 ==> b0
     * a000     b0
     * aa00
     *
     * Because of rounding b can be 47 or 49.
     */
    public void testSimpleAverage1() {
        int size = 8;
        int max = 127;
        int factor = 2;
        double coeff = 0;
        int a = 64;
        int b = 48;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int i=0; i < size; i++) {
            grid.setData(0,i,0,Grid.INSIDE, a);
            grid.setData(0,i,1,Grid.INSIDE, a);
            grid.setData(1,i,1,Grid.INSIDE, a);
            grid.setData(0,i,2,Grid.INSIDE, a);
            grid.setData(0,i,3,Grid.INSIDE, a);
            grid.setData(1,i,3,Grid.INSIDE, a);
        }


        //writeSlices("/tmp/slices_in",grid,max);
        DownsampleAlpha ds = new DownsampleAlpha(coeff,factor,max);
        AttributeGrid dest = ds.execute(grid);
        //writeSlices("/tmp/slices_out",dest,max);

        double EPS = 1;
        assertTrue("Dest value000", (Math.abs(dest.getAttribute(0,0,0) - b) < EPS));
        assertTrue("Dest value100", (Math.abs(dest.getAttribute(1,0,0)) < EPS));
        assertTrue("Dest value001", (Math.abs(dest.getAttribute(0,0,1) - b) < EPS));
        assertTrue("Dest value101", (Math.abs(dest.getAttribute(1,0,1)) < EPS));

        // Test bounds are within a factor * voxel size

        EPS = grid.getVoxelSize() * factor;

        double[] orig_bounds = new double[6];
        grid.getGridBounds(orig_bounds);

        double[] new_bounds = new double[6];
        dest.getGridBounds(new_bounds);

        for(int i=0; i < orig_bounds.length; i++) {
            assertTrue("Grid bounds", Math.abs(orig_bounds[i] - new_bounds[i]) <= EPS);
        }

        // Test grid is factor sized

        assertEquals("Grid factor w", 4, dest.getWidth());
        assertEquals("Grid factor h", 4, dest.getHeight());
        assertEquals("Grid factor d", 4, dest.getDepth());
    }

    /**
     * Bias towards keeping marked voxels
     *
     * xz plane, copied in y
     *
     * 8000
     * 8800 ==> 80
     * 8000     80
     * 8800
     */
    public void testWeightedAverage() {
        int size = 8;
        int max = 63;
        int factor = 2;
        double coeff = 1.0;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int i=0; i < 2; i++) {
            grid.setData(0,i,0,Grid.INSIDE, 8);
            grid.setData(0,i,1,Grid.INSIDE, 8);
            grid.setData(1,i,1,Grid.INSIDE, 8);
            grid.setData(0,i,2,Grid.INSIDE, 8);
            grid.setData(0,i,3,Grid.INSIDE, 8);
            grid.setData(1,i,3,Grid.INSIDE, 8);
        }

        DownsampleAlpha ds = new DownsampleAlpha(coeff,factor,max);
        AttributeGrid dest = ds.execute(grid);

        assertEquals("Dest value000", 8, dest.getAttribute(0, 0, 0));
        assertEquals("Dest value100", 0, dest.getAttribute(1,0,0));
        assertEquals("Dest value001", 8, dest.getAttribute(0,0,1));
        assertEquals("Dest value101", 0, dest.getAttribute(1,0,1));

        // Test bounds are within a factor * voxel size

        double EPS = grid.getVoxelSize() * factor;

        double[] orig_bounds = new double[6];
        grid.getGridBounds(orig_bounds);

        double[] new_bounds = new double[6];
        dest.getGridBounds(new_bounds);

        for(int i=0; i < orig_bounds.length; i++) {
            assertTrue("Grid bounds", Math.abs(orig_bounds[i] - new_bounds[i]) <= EPS);
        }

        // Test grid is factor sized

        assertEquals("Grid factor w", 4, dest.getWidth());
        assertEquals("Grid factor h", 4, dest.getHeight());
        assertEquals("Grid factor d", 4, dest.getDepth());
    }

    /**
     * Test that larger factors work on even multiple grids
     */
    public void testFactorMaxDivisible() {
        int size = 9;
        int factor = 3;
        int max = 127;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                for(int z=0; z < size; z++) {
                    grid.setData(x,y,z,Grid.INSIDE,max);
                }
            }
        }

        DownsampleAlpha ds = new DownsampleAlpha(0,factor,max);
        AttributeGrid dest = ds.execute(grid);

        int new_size = dest.getWidth();
        for(int y=0; y < new_size; y++) {
            for(int x=0; x < new_size; x++) {
                for(int z=0; z < new_size; z++) {
                    assertEquals("Dest value", max, dest.getAttribute(x, y, z));
                }
            }
        }

        // Test bounds are within a factor * voxel size

        double EPS = grid.getVoxelSize() * factor;

        double[] orig_bounds = new double[6];
        grid.getGridBounds(orig_bounds);

        double[] new_bounds = new double[6];
        dest.getGridBounds(new_bounds);

        for(int i=0; i < orig_bounds.length; i++) {
            assertTrue("Grid bounds", Math.abs(orig_bounds[i] - new_bounds[i]) <= EPS);
        }

        // Test grid is factor sized

        assertEquals("Grid factor w", size / factor, dest.getWidth());
        assertEquals("Grid factor h", size / factor, dest.getHeight());
        assertEquals("Grid factor d", size / factor, dest.getDepth());
    }

    /**
     * Test that larger factors work on odd multiple grids
     */
    public void testFactorMaxIndivisible() {
        int size = 11;
        int factor = 3;
        int max = 127;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                for(int z=0; z < size; z++) {
                    grid.setData(x,y,z,Grid.INSIDE,max);
                }
            }
        }

        DownsampleAlpha ds = new DownsampleAlpha(0,factor,max);
        AttributeGrid dest = ds.execute(grid);

        int new_size = dest.getWidth();
        for(int y=0; y < new_size; y++) {
            for(int x=0; x < new_size; x++) {
                for(int z=0; z < new_size; z++) {
                    assertEquals("Dest value",max, dest.getAttribute(x,y,z));
                }
            }
        }

        // Test bounds are within a factor * voxel size

        double EPS = grid.getVoxelSize() * factor;

        double[] orig_bounds = new double[6];
        grid.getGridBounds(orig_bounds);

        double[] new_bounds = new double[6];
        dest.getGridBounds(new_bounds);

        for(int i=0; i < orig_bounds.length; i++) {
            assertTrue("Grid bounds", Math.abs(orig_bounds[i] - new_bounds[i]) <= EPS);
        }

        // Test grid is factor sized

        assertEquals("Grid factor w", size / factor, dest.getWidth());
        assertEquals("Grid factor h", size / factor, dest.getHeight());
        assertEquals("Grid factor d", size / factor, dest.getDepth());
    }

    /**
     * Test downsampling on a sphere.
     *
     * Mostly visually compared using SliceWriter.  Analytical would check that the
     * edges were not totally black.
     */
    public void testBall() {

        double voxelSize = 0.1*Units.MM;
        double margin = 0*voxelSize;

        double sizex = 1*Units.CM;
        double sizey = 1*Units.CM;
        double sizez = 1*Units.CM;
        double ballRadius = 4.5*Units.MM;
        double surfaceThickness = Math.sqrt(3)/2;

        double gridWidth = sizex + 2*margin;
        double gridHeight = sizey + 2*margin;
        double gridDepth = sizez + 2*margin;
        int maxAttributeValue = 127;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        Sphere sphere = new Sphere(0,0,0,ballRadius);

        GridMaker gm = new GridMaker();
        gm.setBounds(bounds);
        gm.setSource(sphere);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfaceThickness);


        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        gm.makeGrid(grid);

        //writeSlices("/tmp/slices_in", grid, maxAttributeValue);

        int factor = 2;
        DownsampleAlpha ds = new DownsampleAlpha(0,factor,maxAttributeValue);
        AttributeGrid dest = ds.execute(grid);

        //writeSlices("/tmp/slices_out", dest, maxAttributeValue);
    }

    /**
     * Simple average of all maxes using MT code
     */
    public void testSimpleAverageMaxedMT() {
        int size = 4;
        int factor = 2;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        int max = 127;

        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                for(int z=0; z < size; z++) {
                    grid.setData(x,y,z,Grid.INSIDE,max);
                }
            }
        }
        DownsampleAlphaMT ds = new DownsampleAlphaMT(0,2,max);
        AttributeGrid dest = ds.execute(grid);

        int new_size = dest.getWidth();
        for(int y=0; y < new_size; y++) {
            for(int x=0; x < new_size; x++) {
                for(int z=0; z < new_size; z++) {
                    assertEquals("Dest value",max, dest.getAttribute(x,y,z));
                }
            }
        }

        assertEquals("Grid factor w", size / factor, dest.getWidth());
        assertEquals("Grid factor h", size / factor, dest.getHeight());
        assertEquals("Grid factor d", size / factor, dest.getDepth());

    }

    /**
     * Simple average of all zeros
     */
    public void testSimpleAverageZeroMT() {
        int size = 4;
        int factor = 2;
        int max = 127;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                for(int z=0; z < size; z++) {
                    grid.setData(x,y,z,Grid.OUTSIDE,0);
                }
            }
        }
        DownsampleAlphaMT ds = new DownsampleAlphaMT(0,factor,max);
        AttributeGrid dest = ds.execute(grid);

        int new_size = dest.getWidth();
        for(int y=0; y < new_size; y++) {
            for(int x=0; x < new_size; x++) {
                for(int z=0; z < new_size; z++) {
                    assertEquals("Dest value",0, dest.getAttribute(x,y,z));
                }
            }
        }

        assertEquals("Grid factor w", size / factor, dest.getWidth());
        assertEquals("Grid factor h", size / factor, dest.getHeight());
        assertEquals("Grid factor d", size / factor, dest.getDepth());

    }

    /**
     * Test downsampling on a sphere.
     *
     * Mostly visually compared using SliceWriter.  Analytical would check that the
     * edges were not totally black.
     */
    public void testBallMT() {

        double voxelSize = 0.1*Units.MM;
        double margin = 0*voxelSize;

        double sizex = 1*Units.CM;
        double sizey = 1*Units.CM;
        double sizez = 1*Units.CM;
        double ballRadius = 4.5*Units.MM;
        double surfaceThickness = Math.sqrt(3)/2;

        double gridWidth = sizex + 2*margin;
        double gridHeight = sizey + 2*margin;
        double gridDepth = sizez + 2*margin;
        int maxAttributeValue = 127;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        Sphere sphere = new Sphere(0,0,0,ballRadius);

        GridMaker gm = new GridMaker();
        gm.setBounds(bounds);
        gm.setSource(sphere);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfaceThickness);


        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        gm.makeGrid(grid);

        writeSlices("/tmp/slices_in", grid, maxAttributeValue);

        int factor = 2;
        DownsampleAlphaMT ds = new DownsampleAlphaMT(0,factor,maxAttributeValue);
        AttributeGrid dest = ds.execute(grid);

        writeSlices("/tmp/slices_out", dest, maxAttributeValue);
    }

    /**
     * Test that MT is faster then ST.  Assumes we always run on a MT box
     */
    public void testMTFasterCoeffZero() {
        double voxelSize = 0.02*Units.MM;
        int maxAttributeValue = 127;
        AttributeGrid grid = getSphereGrid(voxelSize);

        int WARMUP = 3;
        int factor = 2;

        for(int i=0; i < WARMUP; i++) {
            long t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            DownsampleAlphaMT ds = new DownsampleAlphaMT(0,factor,maxAttributeValue);
            AttributeGrid dest = ds.execute(grid);

            long mt_time = System.currentTimeMillis() - t0;
            t0 = System.currentTimeMillis();
            DownsampleAlpha op2 = new DownsampleAlpha(0,factor,maxAttributeValue);
            Grid dest2 = op2.execute(grid);
            long st_time = System.currentTimeMillis() - t0;

            printf("MT time: %6d  ST time: %6d  SpeedUp: %6.2f\n",mt_time,st_time,(float)st_time / mt_time);
        }

        int TIMES = 1;

        int cores = Runtime.getRuntime().availableProcessors();
        cores = Math.min(cores,8); // I expect after 8 for it to level off
        float expected_speedup = 0.5f * cores;

        long t0 = System.currentTimeMillis();
        DownsampleAlpha op2 = new DownsampleAlpha(0,factor,maxAttributeValue);
        Grid dest2 = op2.execute(grid);
        long st_time = System.currentTimeMillis() - t0;

        for(int i=0; i < TIMES; i++) {
            t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            DownsampleAlphaMT ds = new DownsampleAlphaMT(0,factor,maxAttributeValue);
            ds.setThreadCount(cores);
            AttributeGrid dest = ds.execute(grid);

            long mt_time = System.currentTimeMillis() - t0;

            float speedup = (float)st_time / mt_time;
            printf("MT time: %6d  ST time: %6d  SpeedUp: %6.2f\n",mt_time,st_time,speedup);

            assertTrue("Speedup factor > " + expected_speedup, speedup >= expected_speedup);
        }
    }

    /**
     * Test that MT is faster then ST.  Assumes we always run on a MT box
     */
    public void testMTFasterCoeffNonZero() {
        double voxelSize = 0.02*Units.MM;
        int maxAttributeValue = 127;
        double coeff = 1;
        AttributeGrid grid = getSphereGrid(voxelSize);

        int WARMUP = 3;
        int factor = 2;

        for(int i=0; i < WARMUP; i++) {
            long t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            DownsampleAlphaMT ds = new DownsampleAlphaMT(coeff,factor,maxAttributeValue);
            AttributeGrid dest = ds.execute(grid);

            long mt_time = System.currentTimeMillis() - t0;
            t0 = System.currentTimeMillis();
            DownsampleAlpha op2 = new DownsampleAlpha(coeff,factor,maxAttributeValue);
            Grid dest2 = op2.execute(grid);
            long st_time = System.currentTimeMillis() - t0;

            printf("MT time: %6d  ST time: %6d  SpeedUp: %6.2f\n",mt_time,st_time,(float)st_time / mt_time);
        }

        int TIMES = 1;

        int cores = Runtime.getRuntime().availableProcessors();
        cores = Math.min(cores,8); // I expect after 8 for it to level off
        float expected_speedup = 0.5f * cores;
        long t0 = System.currentTimeMillis();
        DownsampleAlpha op2 = new DownsampleAlpha(coeff,factor,maxAttributeValue);
        Grid dest2 = op2.execute(grid);
        long st_time = System.currentTimeMillis() - t0;

        for(int i=0; i < TIMES; i++) {
            t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            DownsampleAlphaMT ds = new DownsampleAlphaMT(coeff,factor,maxAttributeValue);
            ds.setThreadCount(cores);

            AttributeGrid dest = ds.execute(grid);

            long mt_time = System.currentTimeMillis() - t0;

            float speedup = (float)st_time / mt_time;
            printf("MT time: %6d  ST time: %6d  SpeedUp: %6.2f\n",mt_time,st_time,speedup);

            assertTrue("Speedup factor > " + expected_speedup, speedup >= expected_speedup);
        }
    }

    /**
     * Test that MT is faster then ST.  Assumes we always run on a MT box
     */
    public void testMTFasterIntervalCoeffZero() {
        double voxelSize = 0.02*Units.MM;
        int maxAttributeValue = 127;
        AttributeGrid grid = getSphereGrid(voxelSize);

        int WARMUP = 3;
        int factor = 2;

        for(int i=0; i < WARMUP; i++) {
            long t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            DownsampleAlphaMT ds = new DownsampleAlphaMT(0,factor,maxAttributeValue);
            AttributeGrid dest = ds.execute(grid);

            long mt_time = System.currentTimeMillis() - t0;
            t0 = System.currentTimeMillis();
            DownsampleAlpha op2 = new DownsampleAlpha(0,factor,maxAttributeValue);
            Grid dest2 = op2.execute(grid);
            long st_time = System.currentTimeMillis() - t0;

            printf("MT time: %6d  ST time: %6d  SpeedUp: %6.2f\n",mt_time,st_time,(float)st_time / mt_time);
        }

        int TIMES = 1;

        int cores = Runtime.getRuntime().availableProcessors();
        cores = Math.min(cores,8); // I expect after 8 for it to level off
        float expected_speedup = 0.5f * cores;

        long t0 = System.currentTimeMillis();
        DownsampleAlpha op2 = new DownsampleAlpha(0,factor,maxAttributeValue);
        Grid dest2 = op2.execute(grid);
        long st_time = System.currentTimeMillis() - t0;

        for(int i=0; i < TIMES; i++) {
            t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            DownsampleAlphaMT ds = new DownsampleAlphaMT(0,factor,maxAttributeValue);
            ds.setThreadCount(cores);
            AttributeGrid dest = ds.execute(grid);

            long mt_time = System.currentTimeMillis() - t0;

            float speedup = (float)st_time / mt_time;
            printf("MT time: %6d  ST time: %6d  SpeedUp: %6.2f\n",mt_time,st_time,speedup);

            assertTrue("Speedup factor > " + expected_speedup, speedup >= expected_speedup);
        }
    }

    /**
     * Bias towards keeping marked voxels
     *
     * xz plane, copied in y
     *
     * 8000
     * 8800 ==> 80
     * 8000     80
     * 8800
     */
    public void testWeightedAverageMT() {
        int size = 8;
        int max = 63;
        int factor = 2;
        double coeff = 1.0;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int i=0; i < 2; i++) {
            grid.setData(0,i,0,Grid.INSIDE, 8);
            grid.setData(0,i,1,Grid.INSIDE, 8);
            grid.setData(1,i,1,Grid.INSIDE, 8);
            grid.setData(0,i,2,Grid.INSIDE, 8);
            grid.setData(0,i,3,Grid.INSIDE, 8);
            grid.setData(1,i,3,Grid.INSIDE, 8);
        }

        DownsampleAlphaMT ds = new DownsampleAlphaMT(coeff,factor,max);
        AttributeGrid dest = ds.execute(grid);

        assertEquals("Dest value000", 8, dest.getAttribute(0, 0, 0));
        assertEquals("Dest value100", 0, dest.getAttribute(1,0,0));
        assertEquals("Dest value001", 8, dest.getAttribute(0,0,1));
        assertEquals("Dest value101", 0, dest.getAttribute(1,0,1));

        // Test bounds are within a factor * voxel size

        double EPS = grid.getVoxelSize() * factor;

        double[] orig_bounds = new double[6];
        grid.getGridBounds(orig_bounds);

        double[] new_bounds = new double[6];
        dest.getGridBounds(new_bounds);

        for(int i=0; i < orig_bounds.length; i++) {
            assertTrue("Grid bounds", Math.abs(orig_bounds[i] - new_bounds[i]) <= EPS);
        }

        // Test grid is factor sized

        assertEquals("Grid factor w", 4, dest.getWidth());
        assertEquals("Grid factor h", 4, dest.getHeight());
        assertEquals("Grid factor d", 4, dest.getDepth());
    }

    private AttributeGrid getSphereGrid(double voxelSize) {
        double margin = 0*voxelSize;

        double sizex = 1*Units.CM;
        double sizey = 1*Units.CM;
        double sizez = 1*Units.CM;
        double ballRadius = 4.5*Units.MM;
        double surfaceThickness = Math.sqrt(3)/2;

        double gridWidth = sizex + 2*margin;
        double gridHeight = sizey + 2*margin;
        double gridDepth = sizez + 2*margin;
        int maxAttributeValue = 127;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        Sphere sphere = new Sphere(0,0,0,ballRadius);

        GridMaker gm = new GridMaker();
        gm.setBounds(bounds);
        gm.setSource(sphere);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfaceThickness);


        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        gm.makeGrid(grid);

        return grid;

    }

    private AttributeGrid getSphereIntervalGrid(double voxelSize) {
        double margin = 0*voxelSize;

        double sizex = 1*Units.CM;
        double sizey = 1*Units.CM;
        double sizez = 1*Units.CM;
        double ballRadius = 4.5*Units.MM;
        double surfaceThickness = Math.sqrt(3)/2;

        double gridWidth = sizex + 2*margin;
        double gridHeight = sizey + 2*margin;
        double gridDepth = sizez + 2*margin;
        int maxAttributeValue = 127;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        Sphere sphere = new Sphere(0,0,0,ballRadius);

        GridMaker gm = new GridMaker();
        gm.setBounds(bounds);
        gm.setSource(sphere);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfaceThickness);


        AttributeGrid grid = new GridShortIntervals(nx, ny, nz, voxelSize, voxelSize);

        gm.makeGrid(grid);

        return grid;

    }

    /**
     * Test that the downsample of ST and MT are the same
     */
    public void testSTAndMTEqualsBall() {
        int factor = 2;
        double coeff = 1.5;

        double voxelSize = 0.1*Units.MM;
        double margin = 0*voxelSize;

        double sizex = 1*Units.CM;
        double sizey = 1*Units.CM;
        double sizez = 1*Units.CM;
        double ballRadius = 4.5*Units.MM;
        double surfaceThickness = Math.sqrt(3)/2;

        double gridWidth = sizex + 2*margin;
        double gridHeight = sizey + 2*margin;
        double gridDepth = sizez + 2*margin;

        if (gridWidth % factor == 0) gridWidth++;
        if (gridHeight % factor == 0) gridHeight++;
        if (gridDepth % factor == 0) gridDepth++;

        int maxAttributeValue = 127;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        Sphere sphere = new Sphere(0,0,0,ballRadius);

        GridMaker gm = new GridMaker();
        gm.setBounds(bounds);
        gm.setSource(sphere);
        gm.setMaxAttributeValue(1);
        gm.setVoxelSize(voxelSize*surfaceThickness);


        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        gm.makeGrid(grid);

        //writeSlices("/tmp/slices_in", grid, maxAttributeValue);

        DownsampleAlpha ds = new DownsampleAlpha(true,coeff,factor,maxAttributeValue);
        AttributeGrid dest1 = ds.execute(grid);

        DownsampleAlphaMT ds2 = new DownsampleAlphaMT(true,coeff,factor,maxAttributeValue);
        AttributeGrid dest2 = ds2.execute(grid);

        int w = dest1.getWidth();
        int h = dest1.getHeight();
        int d = dest1.getDepth();

        for(int y=0; y < h; y++) {
            for(int x=0; x < w; x++) {
                for(int z=0; z < d; z++) {
                    long mat1 = dest1.getAttribute(x,y,z);
                    long mat2 = dest2.getAttribute(x,y,z);
                    assertEquals("Mat compare",mat1,mat2);

                    byte state1 = dest1.getState(x,y,z);
                    byte state2 = dest2.getState(x,y,z);

                    assertEquals("State compare", state1,state2);
                }
            }
        }

        double EPS = grid.getVoxelSize() * factor;

        System.out.println("EPS: " + EPS);
        double[] orig_bounds = new double[6];
        dest1.getGridBounds(orig_bounds);

        double[] new_bounds = new double[6];
        dest2.getGridBounds(new_bounds);

        System.out.println("ST bounds: " + java.util.Arrays.toString(orig_bounds));
        System.out.println("MT bounds: " + java.util.Arrays.toString(new_bounds));

        for(int i=0; i < orig_bounds.length; i++) {
            assertTrue("Grid bounds", Math.abs(orig_bounds[i] - new_bounds[i]) <= EPS);
        }

    }

    /**
     * Test that the downsample of ST and MT are the same
     */
    public void testSTAndMTEqualsBallFactor3() {
        int factor = 3;
        double coeff = 1.5;

        double voxelSize = 0.1*Units.MM;
        double margin = 0*voxelSize;

        double sizex = 1*Units.CM;
        double sizey = 1*Units.CM;
        double sizez = 1*Units.CM;
        double ballRadius = 4.5*Units.MM;
        double surfaceThickness = Math.sqrt(3)/2;

        double gridWidth = sizex + 2*margin;
        double gridHeight = sizey + 2*margin;
        double gridDepth = sizez + 2*margin;

        if (gridWidth % factor == 0) gridWidth++;
        if (gridHeight % factor == 0) gridHeight++;
        if (gridDepth % factor == 0) gridDepth++;

        int maxAttributeValue = 127;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        Sphere sphere = new Sphere(0,0,0,ballRadius);

        GridMaker gm = new GridMaker();
        gm.setBounds(bounds);
        gm.setSource(sphere);
        gm.setMaxAttributeValue(1);
        gm.setVoxelSize(voxelSize*surfaceThickness);


        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        gm.makeGrid(grid);

        //writeSlices("/tmp/slices_in", grid, maxAttributeValue);

        DownsampleAlpha ds = new DownsampleAlpha(true,coeff,factor,maxAttributeValue);
        AttributeGrid dest1 = ds.execute(grid);

        DownsampleAlphaMT ds2 = new DownsampleAlphaMT(true,coeff,factor,maxAttributeValue);
        AttributeGrid dest2 = ds2.execute(grid);

        int w = dest1.getWidth();
        int h = dest1.getHeight();
        int d = dest1.getDepth();

        for(int y=0; y < h; y++) {
            for(int x=0; x < w; x++) {
                for(int z=0; z < d; z++) {
                    long mat1 = dest1.getAttribute(x,y,z);
                    long mat2 = dest2.getAttribute(x,y,z);
                    assertEquals("Mat compare",mat1,mat2);

                    byte state1 = dest1.getState(x,y,z);
                    byte state2 = dest2.getState(x,y,z);

                    assertEquals("State compare", state1,state2);
                }
            }
        }

        double EPS = grid.getVoxelSize() * factor;

        System.out.println("EPS: " + EPS);
        double[] orig_bounds = new double[6];
        dest1.getGridBounds(orig_bounds);

        double[] new_bounds = new double[6];
        dest2.getGridBounds(new_bounds);

        System.out.println("ST bounds: " + java.util.Arrays.toString(orig_bounds));
        System.out.println("MT bounds: " + java.util.Arrays.toString(new_bounds));

        for(int i=0; i < orig_bounds.length; i++) {
            assertTrue("Grid bounds", Math.abs(orig_bounds[i] - new_bounds[i]) <= EPS);
        }

    }

    /**
     * Write out slices for debugging.  Ignore if debug if FALSE
     * @param dir
     * @param grid
     * @param maxAttributeValue
     */
    private void writeSlices(String dir, Grid grid, int maxAttributeValue) {
        if (!DEBUG) return;

        try {
            File f = new File(dir);
            f.mkdirs();
            FileUtils.cleanDirectory(f);
            SlicesWriter slicer = new SlicesWriter();
            slicer.setFilePattern(dir + "/slice_%03d.png");
            slicer.setCellSize(12);
            slicer.setVoxelSize(9);
            slicer.setWriteLevels(false);
            slicer.setWriteVoxels(true);
            slicer.setLevels(new double[]{0.1, 0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9});
            slicer.setMaxAttributeValue(maxAttributeValue);

            slicer.writeSlices(grid);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

    }
}
