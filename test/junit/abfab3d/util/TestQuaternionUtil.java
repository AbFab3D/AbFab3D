/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2018
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
import static abfab3d.util.QuaternionUtil.r2q;
import static abfab3d.util.QuaternionUtil.q2s;
import static abfab3d.util.QuaternionUtil.s2q;
import static abfab3d.util.QuaternionUtil.r2s;
import static abfab3d.util.QuaternionUtil.qmul;
import static abfab3d.util.QuaternionUtil.q2positive;
import static abfab3d.util.QuaternionUtil.qlerp_s;

public class TestQuaternionUtil {
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestQuaternionUtil.class);
    }


    public void testNothing(){

    }


    static void devTestCubicSymmetry(){

        double rot[][] = new double[][]{
            
            // x-rotation 
            {1,0,0,-PI},
            {1,0,0,-PI/2},
            {1,0,0,0},
            {1,0,0,PI/2},
            {1,0,0,PI},
            
            
            // y-rotation 
            {0,1,0,-PI},
            {0,1,0,-PI/2},
            {0,1,0,0},
            {0,1,0,PI/2},
            {0,1,0,PI},
            
            
            //z-rotation              
            {0,0,1,-4*PI/4},
            {0,0,1,-2*PI/4},
            {0,0,1,0},
            {0,0,1,2*PI/4},
            {0,0,1,4*PI/4},
            /*
            // 111-rotation 
            {1,1,1,-3*PI/3},
            {1,1,1,-2*PI/3},
            {1,1,1,-1*PI/3},
            {1,1,1,0},
            {1,1,1,1*PI/3},
            {1,1,1,2*PI/3},
            {1,1,1,3*PI/3},
            */
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

        printf("x-rotation\n");
        for(int i = -4; i <= 4; i++){            
            double r[] = new double[]{1,0,0,i*PI/4};
            double q[] = r2q(r);
            double s[] = q2s(q);
            double q1[] = s2q(q);

            printf("r:[%6.3f,%6.3f,%6.3f;%6.1f]", r[0],r[1],r[2],r[3]*TODEGREE);
            printf("   q:[%6.3f,%6.3f,%6.3f;%6.3f]", q[0],q[1],q[2],q[3]);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);
        }

        printf("y-rotation\n");
        for(int i = -4; i <= 4; i++){            
            double r[] = new double[]{0,1,0,i*PI/4};
            double q[] = r2q(r);
            double s[] = q2s(q);
            double q1[] = s2q(q);

            printf("r:[%6.3f,%6.3f,%6.3f;%6.1f]", r[0],r[1],r[2],r[3]*TODEGREE);
            printf("   q:[%6.3f,%6.3f,%6.3f;%6.3f]", q[0],q[1],q[2],q[3]);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);
        }
        printf("z-rotation\n");
        for(int i = -4; i <= 4; i++){            
            double r[] = new double[]{0,0,1,i*PI/4};
            double q[] = r2q(r);
            double s[] = q2s(q);
            double q1[] = s2q(q);

            printf("r:[%6.3f,%6.3f,%6.3f;%6.1f]", r[0],r[1],r[2],r[3]*TODEGREE);
            printf("   q:[%6.3f,%6.3f,%6.3f;%6.3f]", q[0],q[1],q[2],q[3]);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);
        }
        printf("[111] rotation\n");
        for(int i = -3; i <= 3; i++){            
            double r[] = new double[]{1,1,1,i*PI/3};
            double q[] = r2q(r);
            double s[] = q2s(q);
            double q1[] = s2q(q);

            printf("r:[%6.3f,%6.3f,%6.3f;%6.1f]", r[0],r[1],r[2],r[3]*TODEGREE);
            printf("   q:[%6.3f,%6.3f,%6.3f;%6.3f]", q[0],q[1],q[2],q[3]);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);
        }
        printf("[-111] rotation\n");
        for(int i = -2; i <= 2; i++){            
            double r[] = new double[]{-1,1,1,i*PI/3};
            double q[] = r2q(r);
            double s[] = q2s(q);
            double q1[] = s2q(q);

            printf("r:[%6.3f,%6.3f,%6.3f;%6.1f]", r[0],r[1],r[2],r[3]*TODEGREE);
            printf("   q:[%6.3f,%6.3f,%6.3f;%6.3f]", q[0],q[1],q[2],q[3]);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);
        }
        printf("[1-11] rotation\n");
        for(int i = -2; i <= 2; i++){            
            double r[] = new double[]{1,-1,1,i*PI/3};
            double q[] = r2q(r);
            double s[] = q2s(q);
            double q1[] = s2q(q);

            printf("r:[%6.3f,%6.3f,%6.3f;%6.1f]", r[0],r[1],r[2],r[3]*TODEGREE);
            printf("   q:[%6.3f,%6.3f,%6.3f;%6.3f]", q[0],q[1],q[2],q[3]);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);
        }
        printf("[-1-11] rotation\n");
        for(int i = -2; i <= 2; i++){            
            double r[] = new double[]{-1,-1,1,i*PI/3};
            double q[] = r2q(r);
            double s[] = q2s(q);
            double q1[] = s2q(q);

            printf("r:[%6.3f,%6.3f,%6.3f;%6.1f]", r[0],r[1],r[2],r[3]*TODEGREE);
            printf("   q:[%6.3f,%6.3f,%6.3f;%6.3f]", q[0],q[1],q[2],q[3]);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);
        }

        printf("[110] rotation\n");
        for(int i = -4; i <= 4; i++){            
            double r[] = new double[]{1,1,0,i*PI/4};
            double q[] = r2q(r);
            double s[] = q2s(q);
            double q1[] = s2q(q);

            printf("r:[%6.3f,%6.3f,%6.3f;%6.1f]", r[0],r[1],r[2],r[3]*TODEGREE);
            printf("   q:[%6.3f,%6.3f,%6.3f;%6.3f]", q[0],q[1],q[2],q[3]);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);
        }

        double s0[] = new double[]{0.2,0,0};
        double s1[] = new double[]{0,0.2,0};
        printf("interpolation\n");
        for(int i = 0; i <=10; i++){
            double st[] = qlerp_s(s0,s1,i*0.1);
            printf("   s:[%6.3f,%6.3f,%6.3f]\n", st[0],st[1],st[2]);

        }        
    }


    static void devTestFundamentalDomain(){

        double rots[][] = new double[][]{
            //{1,0,0,PI/2}, //
            //{1,1,1,PI/3},  // s:[0.1547005383792515, 0.1547005383792515, 0.1547005383792515]
            //{1,0,0,PI/4},  // s:[0.1989123673796580, 0.0000000000000000, 0.0000000000000000]
            //{1,1,0,2*PI/4},  // s:[0.2928932188134524, 0.2928932188134524, 0.0000000000000000]
            {1,0,0,1*PI/4},  // 
            {1,0,0,2*PI/4},  // 
            {1,0,0,3*PI/4},  // 
            {1,0,0,4*PI/4},  // 
            {1,0,0,5*PI/4},  // 
            {1,0,0,6*PI/4},  // 
            {1,0,0,7*PI/4},  // 
        };
        printf("rot(100)\n");
        for(int i = 1; i < 8; i++){ 
            double rot[] = new double[]{1,0,0,i*PI/4};
            double s[] = r2s(rot);
            double p = s[0];
            double a = (1-p*p)/(2*p);
            double r = (1+p*p)/(2*p);

            printf("rot:[%6.3f,%6.3f,%6.3f;%6.1f] s:[%18.16f, %18.16f, %18.16f] a:%18.16f r:%18.16f\n", rot[0],rot[1],rot[2],rot[3]*TODEGREE, 
                   s[0], s[1], s[2], a, r);

        }

        printf("rot(111)\n");
        for(int i = 1; i < 6; i++){ 
            double rot[] = new double[]{1,1,1,i*PI/3};
            double s[] = r2s(rot);
            double p = sqrt(3.)*s[0];
            double a = (1-p*p)/(2*p)/sqrt(3.);
            double r = (1+p*p)/(2*p);

            printf("rot:[%6.3f,%6.3f,%6.3f;%6.1f] s:[%18.16f, %18.16f, %18.16f] a:%18.16f r:%18.16f\n", rot[0],rot[1],rot[2],rot[3]*TODEGREE, 
                   s[0], s[1], s[2], a, r);

        }

        printf("rot(110)\n");
        for(int i = 1; i < 4; i++){ 
            double rot[] = new double[]{1,1,0,i*PI/2};
            double s[] = r2s(rot);
            double p = sqrt(2.)*s[0];
            double a = (1-p*p)/(2*p)/sqrt(3.);
            double r = (1+p*p)/(2*p);

            printf("rot:[%6.3f,%6.3f,%6.3f;%6.1f] s:[%18.16f, %18.16f, %18.16f] a:%18.16f r:%18.16f\n", rot[0],rot[1],rot[2],rot[3]*TODEGREE, 
                   s[0], s[1], s[2], a, r);

        }


        //printf("a: %18.16f r:%18.16f  r1:%18.16f\n", a, r1, r2);

    }

    static void devTestMultiplication(){

        printf("devTestMultiplication()\n");
 
        double rot[][] = new double[][]{
            // identity 
            {1,0,0,0},

            //100-rotation 
            {1,0,0,PI/2},
            {1,0,0,-PI/2},
            {1,0,0,PI},
                        
            //010-rotation 
            {0,1,0,PI/2},
            {0,1,0,-PI/2},
            {0,1,0,-PI},
                        
            //001-rotation              
            {0,0,1,PI/2},
            {0,0,1,-PI/2},
            {0,0,1,-PI},            

            // 111-rotation 
            {1,1,1,2*PI/3},
            {1,1,1,-2*PI/3},
            // 
            {-1,1,1,2*PI/3},
            {-1,1,1,-2*PI/3},
            // 
            {1,-1,1,2*PI/3},
            {1,-1,1,-2*PI/3},
            // 
            {-1,-1,1,2*PI/3},
            {-1,-1,1,-2*PI/3},

            {1,1,0,PI},
            {1,0,1,PI},
            {0,1,1,PI},
            {-1,1,0,PI},
            {-1,0,1,PI},
            {0,-1,1,PI},
        };
        printf("cubic group:\n");
        for(int i = 0; i < rot.length; i++){
            double s[] = r2s(rot[i]);
            printf("[%6.3f,%6.3f,%6.3f]\n", s[0],s[1],s[2]);
        }

        printf("q[1]*q[i]:\n");
        double p[] = r2q(rot[1]);
        for(int i = 0; i < rot.length; i++){
            double qp[] = q2s(q2positive(qmul(r2q(rot[i]),p)));
            printf("[%6.3f,%6.3f,%6.3f]\n", qp[0],qp[1],qp[2]);
            
        }

    }


    public static void main(String arg[]){
 
        //devTestCubicSymmetry();
        //devTestFundamentalDomain();
        devTestMultiplication();
    }

}