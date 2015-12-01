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

import java.util.Random;

import abfab3d.grid.VectorIndexerStructMap;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;


import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.VectorIndexerArray;

import abfab3d.geom.PointCloud;
import abfab3d.util.Bounds;
import abfab3d.util.PointSet;
import abfab3d.util.PointSetArray;

import static java.lang.Math.sqrt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.L2S;

/**
 * Test the PointSetShellBuilder
 *
 * @author Vladimir Bulatov
 */
public class TestPointSetShellBuilder extends TestCase {

    private static final boolean DEBUG = true;

    int subvoxelResolution = 100;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToPointSetVectorIndexer.class);
    }
    
    public void testNothing(){
        // make tester happy
    }

    public void devTestPoint(){

        double vs = 1*MM;
        double x0 = -10*MM,y0 = -10*MM, z0 = -2*MM;
        double x1 = 10*MM,y1 = 10*MM, z1 = 2*MM;

        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(new Bounds(x0, x1, y0, y1, z0, z1), vs,vs);

        PointSet pnts = new PointSetArray();
        double cx = indexGrid.getWidth()/2 + 0.5;
        double cy = indexGrid.getHeight()/2 + 0.5;
        double cz = indexGrid.getDepth()/2 + 0.5;

        pnts.addPoint(-1., -1., -1.); // dummy point

        pnts.addPoint(cx, cy, cz);
        pnts.addPoint(cx-6, cy-3, cz);
        
        printf("points count: %d\n", pnts.size()-1);
        
        PointSetShellBuilder sb = new PointSetShellBuilder();
        sb.setPoints(pnts);
        sb.setShellHalfThickness(1.);

        long t0 = time();
        sb.execute(indexGrid);
        long ind1 = countAttribute(indexGrid, 1);
        long ind2 = countAttribute(indexGrid, 2);
        printf("index1: %d\n", ind1);
        printf("index2: %d\n", ind2);

        printf("PointSetShellBuilder done %d ms\n", time() - t0);
        if(true){
            // print slices 
            printSlice(indexGrid, indexGrid.getDepth()/2);
        }
    }

    public void devTestSpeed(){

        double vs = 0.1*MM;
        double w = 25*MM;
        double x0 = -w,y0 = -w, z0 = -w;
        double x1 = w,y1 = w, z1 = w;

        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(new Bounds(x0, x1, y0, y1, z0, z1), vs,vs);
        
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        printf("grid: [%d x %d x %d]\n", nx,ny,nz);

        PointSet pnts = new PointSetArray();
        
        pnts.addPoint(-1., -1., -1.); // dummy point

        if(true){
            printf("sorted points\n");
            int gy = 200;
            int gz = 200;
            int gx = 200;
            for(int y = 0; y < gy; y++){
                for(int x = 0; x < gx; x++){
                    for(int z = 0; z < gz; z++){
                        double px = (double)x*nx/gx;
                        double py = (double)y*ny/gy;
                        double pz = (double)z*nz/gz;
                        pnts.addPoint(px, py, pz);                        
                    }
                }
            }
        }


        if(false){
            int npnt = 8000000;
            printf("random points on sphere\n");
            
            Random rnd = new Random(125);
            
            double 
                vx = 1./sqrt(3),
                vy = 1./sqrt(3),
                vz = 1./sqrt(3);
            
            for(int i = 0; i < npnt; i++){
                
                double px = (2*rnd.nextDouble()-1);
                double py = (2*rnd.nextDouble()-1);
                double pz = (2*rnd.nextDouble()-1);
                double r = 2*sqrt(px*px + py*py + pz*pz);
                
                px /= r;
                py /= r;
                pz /= r;
                
                px = (px + 0.5)*nx;
                py = (py + 0.5)*ny;
                pz = (pz + 0.5)*nz;
                
                pnts.addPoint(px, py, pz);                
            }
        }
        
        printf("points count: %d\n", pnts.size()-1);
        
        PointSetShellBuilder sb = new PointSetShellBuilder();
        sb.setPoints(pnts);
        double thickness = 1.;

        sb.setShellHalfThickness(thickness);

        long t0 = time();
        sb.execute(indexGrid);

        printf("PointSetShellBuilder done %d ms\n", time() - t0);

        long volume = countVolume(indexGrid);
        printf("volume %d  ~ %d\n", volume, (long)(Math.PI*(nx*nx)*2*thickness));
    }

    public void testTwoPoints(){

        double vs = 1*MM;
        double x0 = -10*MM,y0 = -10*MM, z0 = -2*MM;
        double x1 = 10*MM,y1 = 10*MM, z1 = 2*MM;

        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(new Bounds(x0, x1, y0, y1, z0, z1), vs,vs);

        PointSet pnts = new PointSetArray();
        double cx = indexGrid.getWidth()/2 + 0.5;
        double cy = indexGrid.getHeight()/2 + 0.5;
        double cz = indexGrid.getDepth()/2 + 0.5;

        pnts.addPoint(-1., -1., -1.); // dummy point

        pnts.addPoint(cx, cy, cz);
        pnts.addPoint(cx-6, cy-3, cz);
        
        printf("points count: %d\n", pnts.size()-1);
        
        PointSetShellBuilder sb = new PointSetShellBuilder();
        sb.setPoints(pnts);
        sb.setShellHalfThickness(1.5);

        long t0 = time();
        sb.execute(indexGrid);
        long count1 = countAttribute(indexGrid, 1);
        long count2 = countAttribute(indexGrid, 2);
        printf("count1: %d\n", count1);
        printf("count2: %d\n", count2);
        // check that both points have exactly 19 neighbours 
        assertTrue("((count1 == 19 && count2 == 19) failed)\n", (count1 == 19 && count2 == 19));

    }

    long countAttribute(AttributeGrid grid, long attribute){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();

        //printf("grid:[ %d x %d x %d] slice %d\n",nx,ny,nz,z);
        long count = 0;
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    if(grid.getAttribute(x,y,z) == attribute)
                        count++;
                }                
            }
        }
        return count;
    }

    long countVolume(AttributeGrid grid){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();

        //printf("grid:[ %d x %d x %d] slice %d\n",nx,ny,nz,z);
        long count = 0;
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    if(grid.getAttribute(x,y,z) != 0)
                        count++;
                }                
            }
        }
        return count;
    }

    static void printSlice(AttributeGrid grid, int z){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();
        printf("grid:[ %d x %d x %d] slice %d\n",nx,ny,nz,z);

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int d = (int)grid.getAttribute(x,y,z);
                switch(d){
                case 0: printf("    ."); break;
                default:printf("%5d", d); break;
                }
            }
            printf("\n");
        }
    }

    public static void main(String arg[]){

        for(int i = 0; i < 4; i++){
            //new TestPointSetShellBuilder().devTestPoint();
            //new TestPointSetShellBuilder().testTwoPoints();
            new TestPointSetShellBuilder().devTestSpeed();
        }
    }

}
