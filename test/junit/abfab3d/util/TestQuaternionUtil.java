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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.TODEGREE;
import static java.lang.Math.*;


public class TestQuaternionUtil {
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestQuaternionUtil.class);
    }


    public void testNothing(){

    }

    /**
       convert point (x,y,z) in stereographic projection into unit quaternion 
     */
    static double[] stereographic2quaternion(double s[]){        

        double a2 = s[0]*s[0] + s[1]*s[1] + s[2]*s[2];
        double b = 1/(1+a2);
        return new double[]{2*b*s[0],2*b*s[1],2*b*s[2],(1-a2)*b};

    }

    static double[] quaternion2stereographic(double q[]){ 
        double a = 1+q[3];
        return new double[]{q[0]/a,q[1]/a,q[2]/a};
    }

    //
    //  rotation is represented as axis, angle
    //
    static double[] rotation2quaternion(double r[]){ 
        
        double a = r[3];
        double ca = cos(a/2);
        double sa = sin(a/2);
        
        double n = sqrt(r[0]*r[0] + r[1]*r[1] + r[2]*r[2]);

        return new double[]{ca, sa*r[0]/n,sa*r[1]/n,sa*r[2]/n};

    }


    static void devTestCubicSymmetry(){

        double rot[][] = new double[][]{
            /*
            // x-rotation 
            {1,0,0,-PI},
            {1,0,0,-PI/2},
            {1,0,0,0},
            {1,0,0,PI/2},
            {1,0,0,PI},
            */
            /*
            // y-rotation 
            {0,1,0,-PI},
            {0,1,0,-PI/2},
            {0,1,0,0},
            {0,1,0,PI/2},
            {0,1,0,PI},
            */
            /*
            //z-rotation              
            {0,0,1,-4*PI/4},
            {0,0,1,-3*PI/4},
            {0,0,1,-2*PI/4},
            {0,0,1,-1*PI/4},
            {0,0,1,0},
            {0,0,1,1*PI/4},
            {0,0,1,2*PI/4},
            {0,0,1,3*PI/4},
            {0,0,1,4*PI/4},
            */
            // 111-rotation 
            {1,1,1,-3*PI/3},
            {1,1,1,-2*PI/3},
            {1,1,1,-1*PI/3},
            {1,1,1,0},
            {1,1,1,1*PI/3},
            {1,1,1,2*PI/3},
            {1,1,1,3*PI/3},
            
            /*
            {0,1,0,PI/2},
            {0,1,0,-PI/2},
            {0,1,0,PI},
            {0,0,1,PI/2},
            {0,0,1,-PI/2},
            {0,0,1,PI},

            {1,1,1,2*PI/3},
            {1,1,1,-2*PI/3},
            {-1,1,1,2*PI/3},
            {-1,1,1,-2*PI/3},
            {-1,-1,1,2*PI/3},
            {-1,-1,1,-2*PI/3},
            {1,-1,1,2*PI/3},
            {1,-1,1,-2*PI/3},

            {1,1,0,PI},
            {1,0,1,PI},
            {0,1,1,PI},
            {-1,1,0,PI},
            {-1,0,1,PI},
            {0,-1,1,PI},
            */
        };

        for(int i = 0; i < rot.length; i++){
            double r[] = rot[i];
            double q[] = rotation2quaternion(r);
            double s[] = quaternion2stereographic(q);
            double q1[] = stereographic2quaternion(q);

            printf("r:[%6.3f,%6.3f,%6.3f;%6.1f]", r[0],r[1],r[2],r[3]*TODEGREE);
            printf("   q:[%6.3f,%6.3f,%6.3f;%6.3f]", q[0],q[1],q[2],q[3]);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);

        }
    }

    public static void main(String arg[]){
 
        devTestCubicSymmetry();
    }

}