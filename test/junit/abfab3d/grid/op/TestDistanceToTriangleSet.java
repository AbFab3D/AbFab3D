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

import java.util.Random;

import abfab3d.grid.VectorIndexerStructMap;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridShort;

import abfab3d.util.Bounds;

import abfab3d.distance.DistanceData;
import abfab3d.distance.DistanceDataSphere;


import abfab3d.geom.PointCloud;

import static java.lang.Math.round;
import static java.lang.Math.ceil;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.L2S;

/**
 * Test the DistanceToTriangleSet class.
 *
 * @author Vladimir Bulatov
 */
public class TestDistanceToTriangleSet extends TestCase {

    private static final boolean DEBUG = true;

    int subvoxelResolution = 100;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToTriangleSet.class);
    }


    void makeTestOneTriangle(){
        
        if(DEBUG) printf("makeTestOneTriangle()\n");
        double xmin = 0*MM, xmax = 20*MM, ymin = 0*MM, ymax = 20*MM, zmin = 0*MM, zmax = 20*MM;
        double vs = 1*MM;
        int subvoxelResolution = 10;

        DistanceToTriangleSet dts = new DistanceToTriangleSet(new Bounds(xmin,xmax,ymin,ymax,zmin,zmax), vs, subvoxelResolution);
    
        dts.addTri(new Vector3d(5*MM, 5*MM, 10*MM), new Vector3d(15*MM, 5*MM, 10*MM), new Vector3d(15*MM, 15*MM, 10*MM));
        
    }

    public static void main(String arg[]){

        new TestDistanceToTriangleSet().makeTestOneTriangle();
        
    }

}
