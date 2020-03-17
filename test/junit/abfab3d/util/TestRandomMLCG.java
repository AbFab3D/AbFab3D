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
import abfab3d.io.output.STLWriter;

import static abfab3d.core.Output.printf;


/**


 */
public class TestRandomMLCG extends TestCase {

    static final boolean DEBUG = false;
    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestRandomMLCG.class);
    }


    public void testNothing(){

    }

    public void devTestPeriod(){

        long param[] = RandomMLCG.PARAM_32;
        long m = param[0], a = param[1];

        for(int k = 10; k < 12; k++){
            int seed = k;

            RandomMLCG rnd = new RandomMLCG(m, a, seed);       
            for(long i = 0; i < m; i++){
                int r = rnd.nextInt();
                if(r == seed){
                    printf("m:%d a:%d seed: %d period: %d\n", m, a, seed, (i+1));
                    break;
                }
            }
        }
    }


    public static void main(String arg[])throws Exception {

        new TestRandomMLCG().devTestPeriod();
                
    }
}
