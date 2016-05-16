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

package abfab3d.io.output;

// External Imports


import javax.vecmath.Vector3d;


import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.AttributeMakerGeneral;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridLong;
import abfab3d.grid.GridBitIntervals;
import abfab3d.grid.GridDataDesc;
import abfab3d.grid.GridDataChannel;

import abfab3d.util.Bounds;


import abfab3d.geom.TriangulatedModels;

import abfab3d.util.MathUtil;
import abfab3d.util.ImageGray16;
import abfab3d.util.LongConverter;

import abfab3d.distance.DistanceDataSphere;
import abfab3d.distance.DistanceDataSegment;
import abfab3d.distance.DistanceDataUnion;
import abfab3d.distance.DensityFromDistance;
import abfab3d.distance.DataSourceFromDistance;

import abfab3d.datasources.DataSourceMixer;
import abfab3d.datasources.SolidColor;
import abfab3d.datasources.Box;
import abfab3d.datasources.Noise;
import abfab3d.datasources.Cone;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Torus;
import abfab3d.datasources.Composition;
import abfab3d.datasources.VolumePatterns;

import abfab3d.transforms.Rotation;

import abfab3d.grid.op.GridMaker;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

import static java.lang.Math.sqrt;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

/**
 * Tests the functionality of GridSaver
 *
 * @version
 */
public class TestGridSaver extends TestCase {

    static final double CM = 0.01; // cm -> meters
    static final double MM = 0.001; // mm -> meters

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGridSaver.class);
    }

    public void testNothing(){
        //this test here is to make Test happy. 
    }

    void devTestDistanceGrid() throws IOException{

        printf("devTestGray()\n");  
        double voxelSize = 0.2*MM;
        double margin = 2*MM;
        double sizex = 10*MM; 
        double sizey = 10*MM; 
        double sizez = 10*MM;
        double ballRadius = 10.0*MM;
        
        double w = 30*MM;
        
        int threadsCount = 1;
        Bounds bounds = new Bounds(-w/2,w/2,-w/2,w/2,-w/2,w/2);

        DistanceDataSphere s0 = new DistanceDataSphere(7*MM, new Vector3d(0,0,0));
        DistanceDataSphere s1 = new DistanceDataSphere(7*MM, new Vector3d(5*MM,0,0));
        DistanceDataSphere s2 = new DistanceDataSphere(7*MM, new Vector3d(-5*MM,0,0));
        DistanceDataUnion s12 = new DistanceDataUnion(s1, s2);
        double blend = 1*MM;
        s12.setBlendWidth(blend);
        GridMaker gm = new GridMaker();

 
        gm.setSource(new DataSourceFromDistance(s0));  
        //gm.setSource(new DataSourceFromDistance(s12));  
        AttributeGrid grid = new ArrayAttributeGridByte(bounds, voxelSize, voxelSize);
        GridDataChannel distChannel = new GridDataChannel(GridDataChannel.DISTANCE, "dist", 8, 0, 2*voxelSize,-2*voxelSize);
        grid.setDataDesc(new GridDataDesc(distChannel));
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        GridSaver gridSaver = new GridSaver();
        double ds = 0.99*voxelSize;
        for(int i = 0; i < 5; i++){
            double surface = (i-2)*ds;
            gridSaver.setSurfaceLevel(surface);
            gridSaver.write(grid,fmt("/tmp/grid_distance_%d.stl",i));
            printf("writing mesh done\n");
        }
    }
   
    public static void main(String[] args) throws IOException {
        new TestGridSaver().devTestDistanceGrid();
    }
    
}
