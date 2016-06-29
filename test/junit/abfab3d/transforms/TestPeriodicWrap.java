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

package abfab3d.transforms;

// External Imports


import javax.vecmath.Vector3d;

// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import abfab3d.core.Vec;

import static abfab3d.core.Output.printf;

/**
 * Tests the functionality of PeriodicWrap
 *
 * @version
 */
public class TestPeriodicWrap extends TestCase {

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestPeriodicWrap.class);
    }

    public void testSimple() {

        //PeriodicWrap pw = new PeriodicWrap(new Vector3d(1,0,0),new Vector3d(0,2,0),new Vector3d(0,0,3));
        PeriodicWrap pw = new PeriodicWrap(new Vector3d(1,1,0),new Vector3d(-1,1,0),new Vector3d(0,0,1));
        pw.initialize();

        Vec v = new Vec(0.1,2.,7.3);
        Vec out = new Vec(3);

        pw.inverse_transform(v, out);

        printf("(%7.5f,%7.5f,%7.5f) ->  (%7.5f,%7.5f,%7.5f)\n", v.v[0],v.v[1],v.v[2], out.v[0],out.v[1],out.v[2]);

    }
   
    public static void main(String[] args) {
        new TestPeriodicWrap().testSimple();
    }
}