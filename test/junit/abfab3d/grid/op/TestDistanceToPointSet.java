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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridShort;

import abfab3d.geom.PointCloud;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static java.lang.Math.round;

/**
 * Test the DistanceToPointSet class.
 *
 * @author Vladimir Bulatov
 */
public class TestDistanceToPointSet extends TestCase {

    private static final boolean DEBUG = true;

    int subvoxelResolution = 100;
    double voxelSize = 0.1*MM;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToPointSet.class);
    }

    public void testOnePoint(){

        int nx = 20, ny = 20, nz = 20; 
        double x0 = -1*MM,y0 = -1*MM, z0 = -1*MM;
        double 
            x1 = x0 + nx*voxelSize,
            y1 = y0 + nx*voxelSize,
            z1 = z0 + nx*voxelSize;
            

        PointCloud pnts = new PointCloud();
        pnts.addPoint(0.,0.,0.);
        
        DistanceToPointSet dps = new DistanceToPointSet(pnts, 0, 1*MM);
        AttributeGrid grid = new ArrayAttributeGridShort(nx, ny, nz, voxelSize, voxelSize);        
        grid.setGridBounds(new double[]{x0,x1, y0, y1, z0, z1});
        
        dps.execute(grid);
        
    }

    public static void main(String arg[]){


        new TestDistanceToPointSet().testOnePoint();
        
    }

}
