/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

// External Imports

import abfab3d.core.MathUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Random;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.TODEGREE;

/**
 */
public class TestColorMapperIndex extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestColorMapperIndex.class);
    }

    public void testNothing() {

                
    }

    public void devTestColors() {

        ColorMapperIndex cm = new ColorMapperIndex();
        for(int i = 0; i < 100; i++){
            double v = -5. + 0.2*i;
            int c = cm.getColor(v);
            printf("%5.1f -> 0x%08X\n", v, c);
        }

    }


    public static void main(String arg[]){

        new TestColorMapperIndex().devTestColors();
        
    }
}
