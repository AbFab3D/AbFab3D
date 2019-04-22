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


import static java.lang.Math.round;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static abfab3d.core.Output.printf;

/**
 * Test the Neigborhood class.
 *
 * @author Vladimir Bulatov
 */
public class TestNeighborhood extends TestCase {

    private static final boolean DEBUG = false;

    int subvoxelResolution = 100;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestNeighborhood.class);
    }

    public void testNothing() {
        // TODO: sad place holder
    }
    /**
       testing the properties of ball neigborhood
     */
    void makeBall(){
        //Vector nn = new Vector();
        for(int z = 0; z < 6; z++){
            for(int y = 0; y <= z; y++){
                for(int x = 0; x <= y; x++){
                    double r = sqrt(x*x + y*y + z*z);
                    int neig[] = Neighborhood.makeBall(r);                    
                    if(r <= 4.01) 
                        printf("%d %d %d : %5.2f %4d\n",x,y,z,r, neig.length/3);
                }
            }
        }        
        
    }
   

    public static void main(String arg[]){
        new TestNeighborhood().makeBall();
        
    }

}
