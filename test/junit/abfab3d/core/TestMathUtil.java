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

package abfab3d.core;

//External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Random;

// Internal Imports
import abfab3d.BaseTestCase;
import static java.lang.Math.*;
import static abfab3d.core.Output.printf;

// Internal Imports

/**
 * Tests the functionality of MahUtil 
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestMathUtil extends BaseTestCase  {
    
    static final boolean DEBUG = false;

    public void testNothing(){        
    }

    public void testSolveLinear2(){       
        Random rnd = new Random(121);
        int n = 100;
        double maxError = 0;
        for(int i = 0; i < n; i++){
            double m00 = rnd.nextDouble();
            double m01 = rnd.nextDouble();
            double m10 = rnd.nextDouble();
            double m11 = rnd.nextDouble();
            double c0 = rnd.nextDouble();
            double c1 = rnd.nextDouble();
            double x[] = new double[2];
            MathUtil.solveLinear2(m00, m01, m10, m11, c0, c1, x);
            double t0 = m00*x[0] + m01*x[1] - c0;
            double t1 = m10*x[0] + m11*x[1] - c1;
            double err = max(abs(t0), abs(t1));
            maxError = max(maxError, err);
        }
        if(DEBUG)printf("maxError: %10.3e\n", maxError);
        assertTrue("large error", maxError < 1.e-14);
    }

    public static void main(String arg[]){

        new TestMathUtil().testSolveLinear2();
        
    }
    
}