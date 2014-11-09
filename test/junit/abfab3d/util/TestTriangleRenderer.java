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

import static java.lang.Math.sin;
import static java.lang.Math.cos;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

/**
 */
public class TestTriangleRenderer extends TestCase {

    static final boolean DEBUG = false;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangleRenderer.class);
    }


    public void testSinglePixel(){
        
        TriangleRenderer tr = new TriangleRenderer();
        PixelCounter pc = new PixelCounter(10,10);
        
        tr.fillTriangle(pc, 5,5, 6,5, 5.5,5.5001);
        if(DEBUG)pc.dump();
        if(pc.pixelCount != 1 ) throw new RuntimeException(fmt("wrong rendered pixels count:%d ", pc.pixelCount));
    }
    public void testPolygon(){

        TriangleRenderer tr = new TriangleRenderer();
        int nx = 50, ny = 50; // size
        int n = 20;   // polygon sides count 
        double cx = 25.3;
        double cy = 25.5;
        double r = 20.4; // radius of polygon 

        PixelCounter pc = new PixelCounter(50,50);

        for(int k = 0; k < n; k++){

            double a1 = 2*k*Math.PI/n;
            double a2 = 2*((k+1)%n)*Math.PI/n;
            tr.fillTriangle(pc, cx, cy, cx + r*cos(a1),cy + r*sin(a1), cx + r*cos(a2),cy + r*sin(a2));
        }
        
        if(DEBUG)pc.dump();
        if(DEBUG)printf("pixels: %d\n", pc.pixelCount);
        if(pc.pixelCount != 1285 ) throw new RuntimeException(fmt("wrong rendered pixels count:%d ", pc.pixelCount));
    }

    static class PixelCounter implements TriangleRenderer.PixelRenderer {
        int pixels[];
        int nx, ny;

        int pixelCount = 0;
        PixelCounter(int nx, int ny) {
            this.nx = nx;
            this.ny = ny;
            pixels = new int[nx*ny];
        }
        public void setPixel(int x, int y){
            pixelCount++;
            pixels[x + y*nx]++;
            //printf("%d %d\n", x,y);
        }
        void dump(){
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int c = pixels[x + (ny-1-y)*nx];
                    if(c == 0) printf(".");
                    else  printf("%d",c);
                }
                printf("\n");
            }
        }
    }

    public static void main(String arg[]){

        //new TestTriangleRenderer().testPolygon();
        //new TestTriangleRenderer().testSinglePixel();

        
    }
}
