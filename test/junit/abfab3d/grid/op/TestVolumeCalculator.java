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

import abfab3d.datasources.TransformableDataSource;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;

import abfab3d.datasources.Sphere;


import static java.lang.Math.round;
import static java.lang.Math.abs;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.CM;

/**
 * Test the VolumeCalculatorclass.
 *
 * @author Vladimir Bulatov
 */
public class TestVolumeCalculator extends TestCase {

    private static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestVolumeCalculator.class);
    }

    public void testSphereVolume() {
        
        double r = 50*MM;
        double s = r+1*MM;
        double vs = 0.5*MM;
        Sphere sp = new Sphere(r);
        sp.setDataType(DataSource.DATA_TYPE_DENSITY);
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(new Bounds(-s,s,-s,s,-s,s), vs,vs);
        GridMaker gm = new GridMaker();
        gm.setThreadCount(8);
        gm.setSource(sp);
        long t0 = time();

        gm.makeGrid(grid);
        printf("grid %d x %d x %d generated in %d ms\n", grid.getWidth(),grid.getHeight(),grid.getDepth(),(time() - t0));
        
        VolumeCalculator vc = new VolumeCalculator();
        t0 = time();
        double v = vc.getVolume(grid);
        printf("volume calculated in %d ms\n", (time() - t0));

        double ve  = (4*Math.PI*r*r*r/3);                           
        double cm3 = CM*CM*CM;
        printf("calculated volume: %7.3f cm^3\n", v/(cm3));
        printf("     exact volume: %7.3f cm^3\n", ve/(cm3));
        assertTrue("calculated volume differs from exact", (Math.abs(ve - v) < 0.1*cm3) );
            
        
    }


    public static void main(String arg[]){
        new TestVolumeCalculator().testSphereVolume();
        
    }

}
