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
import javax.vecmath.Vector3d;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;


import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataChannel;
import abfab3d.core.GridDataDesc;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.ArrayAttributeGridInt;

import abfab3d.grid.util.GridUtil;

import abfab3d.core.Bounds;
import abfab3d.util.PointSet;
import abfab3d.util.PointSetArray;
import abfab3d.util.ColorMapper;
import abfab3d.util.ColorMapperDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;

/**
 * Test the PointSetShellBuilder
 *
 * @author Vladimir Bulatov
 */
public class TestPointSetShellBuilder extends TestCase {

    private static final boolean DEBUG = false;

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

    public void devTestTwoPoints() throws Exception{

        int imageWidth = 500;
        double vs = 1*MM;
        double x0 = -10*MM,y0 = -10*MM, z0 = -2*MM;
        double x1 = 10*MM,y1 = 10*MM, z1 = 2*MM;
        double bandWidth = 1*MM;
        double maxOutDistance = 5*vs;
        Bounds bounds = new Bounds(x0, x1, y0, y1, z0, z1, vs);
        int magnification = imageWidth/bounds.getGridWidth();
        int distanceBitCount = 16;

        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(bounds, vs,vs);

        PointSet pnts = new PointSetArray();
        
        double cx = bounds.getCenterX()+vs/2;
        double cy = bounds.getCenterY()+vs/2;
        double cz = bounds.getCenterZ()+vs/2;

        pnts.addPoint(-1., -1., -1.); // dummy point

        double dx = vs, dy = vs, dz = 0;

        int np = 5;

        for(int k = 0; k < np; k++){
            pnts.addPoint(cx + k*dx, cy+k*dy, cz+k*dz);
        }
        
        printf("points count: %d\n", pnts.size()-1);
        
        PointSetShellBuilder sb = new PointSetShellBuilder();
        sb.setPoints(pnts);
        sb.setShellHalfThickness(maxOutDistance/vs);

        long t0 = time();
        sb.execute(indexGrid);

        AttributeGrid distGrid = new ArrayAttributeGridShort(bounds, vs, vs);
        GridDataChannel distanceChannel = new GridDataChannel(GridDataChannel.DISTANCE, "dist", distanceBitCount, 0, 0, maxOutDistance);
        distGrid.setDataDesc(new GridDataDesc(distanceChannel));
        ClosestPointIndexer.makeDistanceGrid(indexGrid, pnts, null, distGrid, 0, maxOutDistance);

        GridDataChannel dataChannel = distGrid.getDataDesc().getChannel(0);
        ColorMapper colorMapper = new ColorMapperDistance(0xFF00FF00,0xFFDDFFDD, 0xFF0000FF,0xFFDDDDFF, bandWidth);

        for(int iz = 0; iz < distGrid.getDepth(); iz++){
            GridUtil.printSliceAttribute(indexGrid, iz);
            GridUtil.writeSlice(distGrid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dist%03d.png", iz));
        }
                
    }

    public void devTestPointSorting(){
        PointSet points = new PointSetArray();
        int n = 100;
        Random rnd = new Random(121);

        int nx = 10;
        int ny = 10;
        int nz = 10;

        points.addPoint(-1,-1,-1);

        for(int i = 0; i < n; i++){
            double x = nx*rnd.nextDouble();
            double y = ny*rnd.nextDouble();
            double z = 0.5;//nz*rnd.nextDouble();
            points.addPoint(x,y,z);
        }
        printf("original points:\n");
        printPointSet(points);
        PointSet points1 = PointSetShellBuilder.makeSortedPoints(points, 10, 0, 1.);
        printf("y sorted points:\n");
        printPointSet(points1);

    }

    static void printPointSet(PointSet points){
        
        Vector3d pnt = new Vector3d();
        
        for(int i = 0; i < points.size(); i++){
            points.getPoint(i, pnt);
            printf("%7.3f %7.3f %7.3f\n", pnt.x,pnt.y,pnt.z);
        }
    }

    public void devTestSphere() throws Exception{

        int imageWidth = 500;
        double vs = 1*MM;
        double x0 = -10*MM,y0 = -10*MM, z0 = -10*MM;
        double x1 = 10*MM,y1 = 10*MM, z1 = 10*MM;
        double bandWidth = 1*MM;
        double maxOutDistance = 3*vs;
        Bounds bounds = new Bounds(x0, x1, y0, y1, z0, z1, vs);
        int magnification = imageWidth/bounds.getGridWidth();
        int distanceBitCount = 16;

        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(bounds, vs,vs);

        PointSet pnts = new PointSetArray();
        
        double cx = bounds.getCenterX()+vs/2;
        double cy = bounds.getCenterY()+vs/2;
        double cz = bounds.getCenterZ()+vs/2;

        pnts.addPoint(-1., -1., -1.); // dummy point


        int np = 64;
        int nq = 40;

        for(int k = 0; k < np; k++){
            for(int q = 0; q < nq; q++){
                
                double phi = 2*Math.PI*k/np;
                double theta = Math.PI*q/nq;
                double r = 7*vs;
                
                double dx = r*cos(phi)*sin(theta);
                double dy = r*sin(phi)*sin(theta);
                double dz = r*cos(theta);
                
                pnts.addPoint(cx + dx, cy+dy, cz+ dz);
            }
        }
        
        printf("points count: %d\n", pnts.size()-1);
        
        PointSetShellBuilder sb = new PointSetShellBuilder();
        sb.setPoints(pnts);
        sb.setShellHalfThickness(maxOutDistance/vs);

        long t0 = time();
        sb.execute(indexGrid);

        AttributeGrid distGrid = new ArrayAttributeGridShort(bounds, vs, vs);
        GridDataChannel distanceChannel = new GridDataChannel(GridDataChannel.DISTANCE, "dist", distanceBitCount, 0, 0, maxOutDistance);
        distGrid.setDataDesc(new GridDataDesc(distanceChannel));
        ClosestPointIndexer.makeDistanceGrid(indexGrid, pnts, null, distGrid, 0, maxOutDistance);

        GridDataChannel dataChannel = distGrid.getDataDesc().getChannel(0);
        ColorMapper colorMapper = new ColorMapperDistance(0xFF00FF00,0xFFDDFFDD, 0xFF0000FF,0xFFDDDDFF, bandWidth);

        for(int iz = 0; iz < distGrid.getDepth(); iz++){
            GridUtil.printSliceAttribute(indexGrid, iz);
            GridUtil.writeSlice(distGrid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dist%03d.png", iz));
        }
                
    }

    public void devTestCircle() throws Exception{

        int imageWidth = 500;
        double vs = 1*MM;
        double x0 = -10*MM,y0 = -10*MM, z0 = -0.5*MM;
        double x1 = 11*MM,y1 = 11*MM, z1 = 0.5*MM;
        double bandWidth = 1*MM;
        double maxOutDistance = 3*vs;
        Bounds bounds = new Bounds(x0, x1, y0, y1, z0, z1, vs);
        int magnification = imageWidth/bounds.getGridWidth();
        int distanceBitCount = 16;
        boolean sortPoints = true;
        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(bounds, vs,vs);

        PointSet pnts = new PointSetArray();
        
        double cx = bounds.getCenterX()+vs/2;
        double cy = bounds.getCenterY()+vs/2;
        double cz = bounds.getCenterZ()+vs/2;

        pnts.addPoint(-1., -1., -1.); // dummy point


        int np = 16;

        for(int k = 0; k < np; k++){
            
            double phi = 2*Math.PI*k/np;
            double r = 7*vs;
            
            double dx = r*cos(phi);
            double dy = r*sin(phi);
            double dz = 0;
            
            pnts.addPoint(cx + dx, cy+dy, cz + dz);
        
        }
        
        printf("points count: %d\n", pnts.size()-1);
        
        PointSetShellBuilder sb = new PointSetShellBuilder();

        if(sortPoints) {
            pnts = PointSetShellBuilder.makeSortedPoints(pnts, bounds.getGridHeight(), bounds.ymin, vs);
        }

        sb.setPoints(pnts);
        sb.setShellHalfThickness(maxOutDistance/vs);

        long t0 = time();
        sb.execute(indexGrid);
        AttributeGrid distGrid = new ArrayAttributeGridShort(bounds, vs, vs);
        GridDataChannel distanceChannel = new GridDataChannel(GridDataChannel.DISTANCE, "dist", distanceBitCount, 0, 0, maxOutDistance);
        distGrid.setDataDesc(new GridDataDesc(distanceChannel));
        ClosestPointIndexer.makeDistanceGrid(indexGrid, pnts, null, distGrid, 0, maxOutDistance);

        GridDataChannel dataChannel = distGrid.getDataDesc().getChannel(0);
        ColorMapper colorMapper = new ColorMapperDistance(0xFF00FF00,0xFFDDFFDD, 0xFF0000FF,0xFFDDDDFF, bandWidth);

        for(int iz = 0; iz < distGrid.getDepth(); iz++){
            GridUtil.printSliceAttribute(indexGrid, iz);
            GridUtil.writeSlice(distGrid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dist%03d.png", iz));
        }
                
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

    public static void main(String arg[]) throws Exception{

        for(int i = 0; i < 1; i++){
            //new TestPointSetShellBuilder().devTestPoint();
            //new TestPointSetShellBuilder().testTwoPoints();
            //new TestPointSetShellBuilder().devTestTwoPoints();
            //new TestPointSetShellBuilder().devTestSphere();
            new TestPointSetShellBuilder().devTestCircle();
            //new TestPointSetShellBuilder().devTestPointSorting();
            //new TestPointSetShellBuilder().devTestSpeed();

        }
    }

}
