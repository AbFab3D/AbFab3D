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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Random;

import static abfab3d.core.Output.printf;


/**


 */
public class TestRectPacking extends TestCase {

    static final boolean DEBUG = false;

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestRectPacking.class);
    }


    public void testFewRect(){

        int N = 10000000;
        
        double minS = 1; 
        double maxS = 10;

        double s = maxS - minS;

        
        RectPacking packer = new RectPacking();
        Random rnd = new Random(101);

        for(int i = 0; i < N; i++){
            packer.addRect(minS + s*rnd.nextDouble(),minS + s*rnd.nextDouble());
        }       
        packer.pack();
    }

    public static void main(String arg[]){

        for(int i = 0; i < 2; i++){
            new TestRectPacking().testFewRect();
        }        
    }
}
