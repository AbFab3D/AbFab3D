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
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

/**
 */
public class TestPerlinNoise3D extends TestCase {

    static final double EPS = 1.e-9;
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestPerlinNoise3D.class);
    }

    public void testRemainder(){
        double x1 = 11.5;
        double x2 = -11.5;
        int n = 10;
        double r1 = MathUtil.toInterval(x1,n);
        double r2 = MathUtil.toInterval(x2,n);

        printf("%f %% %d = %f\n", x1,n,r1);
        printf("%f %% %d = %f\n", x2,n,r2);
    }

    public void test1(){

        int nx = 2, ny = 2, nz = 2;

        //double grad[] = new double[]{1.,1,0,-1,0,0};
        
        PerlinNoise3D pn = new PerlinNoise3D(nx, ny, nz, 11);
        
        int N = 20;
        for(int iy = 0; iy <= N; iy++){
            for(int ix = 0; ix <= N; ix++){

                double x = ix * 0.1+0.5;
                double y = iy * 0.1+0.5;
                double z = 0.5;
                
                double v = pn.get(x,y,z);
                //printf("(%5.2f,%5.2f,%5.2f): %6.3f\n", x,y,z,v);
                printf("%5.2f ", v);
            }        
            printf("\n");
        }
    }

    public void test2(){

        int nx = 50, ny = 50, nz = 50;
        int seed = 11;
        
        PerlinNoise3D pn = new PerlinNoise3D(nx, ny, nz, seed);
        double vmin = Double.MAX_VALUE;
        double vmax = Double.MIN_VALUE;

        int Nx = nx * 10;
        int Ny = ny * 10;
        int Nz = nz * 10;

        long t0 = time();
        for(int iz = 0; iz <= Nz; iz++){
            for(int iy = 0; iy <= Ny; iy++){
                for(int ix = 0; ix <= Nz; ix++){
                    
                    double x = ix * 0.1+0.5;
                    double y = iy * 0.1+0.5;
                    double z = iz * 0.1+0.5;
                    
                    double v = pn.get(x,y,z);
                    if(v < vmin) vmin = v;
                    if(v > vmax) vmax = v;

                }        
            }
        }
        printf("grid: [%d x %d x %d] vmin: %5.2f vmax: %5.2f time: %d ms", Nx, Ny, Nz, vmin, vmax, (time() - t0));
    }


    public static void main(String arg[]){

        //new TestPerlinNoise3D().testRemainder();
        //new TestPerlinNoise3D().test1();
        new TestPerlinNoise3D().test2();
        
    }
}
