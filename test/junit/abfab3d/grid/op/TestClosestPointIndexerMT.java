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

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeDesc;
import abfab3d.grid.AttributeChannel;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.util.GridUtil;

import abfab3d.util.Bounds;


import static java.lang.Math.round;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.PI ;
import static java.lang.Math.sqrt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.iround;

/**
 * Test the ClosestPointIndexer class.
 *
 * @author Vladimir Bulatov
 */
public class TestClosestPointIndexerMT extends TestCase {

    private static final boolean DEBUG = false;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestClosestPointIndexerMT.class);
    }

    public void testNothing()throws Exception{
        // to make tester happy 
    }

    
    /**
       this test compares result of MT and ST distance calculations 
     */
    void devTestMTvsSTprecision()throws Exception{
        
        if(DEBUG) printf("%s.testPrecisionMTvsST()\n", this.getClass().getName());
        double vs = 0.5*MM;
        double w = 10*MM; // half width 
        //double firstLayerThickness = 0.7;
        //double firstLayerThickness = 1.7;
        double firstLayerThickness = 2.7;
        //double xmin = -w, xmax = w, ymin = -w, ymax = w, zmin = -vs, zmax = 2*vs;
        double xmin = -w, xmax = w, ymin = -w, ymax = w, zmin = -w, zmax = w;
        int densityBitCount = 8;
        int distanceBitCount = 16;

        int subvoxelResolution = (1<<densityBitCount)-1;

        int voxelSquareSize = 20;// for visualization 
        boolean snapToGrid = false;
        int iterationsCount = 0;
        int threadCount = 4;
        boolean writeViz = false;
        boolean compareDistances = false;

        Bounds bounds = new Bounds(xmin,xmax,ymin,ymax,zmin,zmax);
        double pnts[];
        pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128);
        //double pnts[] = makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128);
        //double pnts[]  = makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128);
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128));
        //double pnts[] = makeUnion(makeUnion(makeCircleZ(0.5*vs, 0.25*vs, 0.5*w, 0.9*w, 128),makeCircleZ(0.5*vs, 0.5*vs, 0.5*w, 0.9*w, 128)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128));

        ArrayAttributeGridInt indexGrid1 = new ArrayAttributeGridInt(bounds, vs, vs);
        ArrayAttributeGridInt indexGrid2 = new ArrayAttributeGridInt(bounds, vs, vs);


        int pcount = pnts.length/3;
        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];

        ClosestPointIndexer.getPointsInGridUnits(indexGrid1, pnts, pntx, pnty, pntz);
        if(snapToGrid){
            ClosestPointIndexer.snapToVoxels(pntx);
            ClosestPointIndexer.snapToVoxels(pnty);
            ClosestPointIndexer.snapToVoxels(pntz);
        }

        ClosestPointIndexer.initFirstLayer(indexGrid1, pntx, pnty, pntz, firstLayerThickness, subvoxelResolution);
        ClosestPointIndexer.initFirstLayer(indexGrid2, pntx, pnty, pntz, firstLayerThickness, subvoxelResolution);

        for(int z = 0; z < indexGrid1.getDepth(); z++){
            renderDiff(indexGrid1, z, pntx, pnty, pntz, voxelSquareSize, fmt("/tmp/dist/dist00_%02d.png",z), true);
        }

        int usedCount = ClosestPointIndexer.removeUnusedPoints(indexGrid1, pntx, pnty, pntz);
        printf("grid: [%d x %d x %d] threads: %d points: %d usedPoints: %d \n", indexGrid1.getWidth(), indexGrid1.getHeight(), indexGrid1.getDepth(), threadCount, pcount, usedCount);

        // distribute distances to the whole grid 
        long t0 = time();
        ClosestPointIndexer.PI3_sorted(pntx, pnty, pntz, indexGrid1);
        printf("ClosestPointIndexer done: %d\n", (time() - t0));
        t0 = time();
        ClosestPointIndexerMT.PI3_MT(pntx, pnty, pntz, indexGrid2, threadCount);
        printf("ClosestPointIndexerMT done: %d\n ", (time() - t0));
        long diff = GridUtil.compareGrids(indexGrid1, indexGrid2);
        printf("index difference count: %d\n", diff);

        printf("maxDiff1: %6.4f\n", getMaxDiff(indexGrid1, pntx, pnty, pntz));
        printf("maxDiff2: %6.4f\n", getMaxDiff(indexGrid1, pntx, pnty, pntz));
            
        if(writeViz){
            for(int z = 0; z < indexGrid1.getDepth(); z++){
                //renderDiff(indexGrid1, z, pntx, pnty, pntz, voxelSquareSize, fmt("/tmp/dist/dist01_%02d.png",z), true);
                renderDiff(indexGrid2, z, pntx, pnty, pntz, voxelSquareSize, fmt("/tmp/dist/distDiff2_%02d.png",z), true);
            }
        }
        
        if(compareDistances){
            AttributeGrid distGrid1 = makeDistanceGrid(bounds, -w, w, distanceBitCount, vs);
            AttributeGrid distGrid2 = makeDistanceGrid(bounds, -w, w, distanceBitCount, vs);
            ClosestPointIndexer.getPointsInWorldUnits(indexGrid1, pntx, pnty, pntz);
            
            ClosestPointIndexer.makeDistanceGrid(indexGrid1,pntx, pnty, pntz, null, distGrid1, w, w);
            ClosestPointIndexer.makeDistanceGrid(indexGrid2,pntx, pnty, pntz, null, distGrid2, w, w);
            long diffDist = GridUtil.compareGrids(distGrid1, distGrid2);
            printf("distance difference count: %d\n", diffDist);
        }
        
    }
    
    AttributeGrid  makeDistanceGrid(Bounds bounds, double minDistance, double maxDistance, int bitCount, double voxelSize){

        AttributeGrid grid = new ArrayAttributeGridShort(bounds, voxelSize, voxelSize);
        grid.setAttributeDesc(new AttributeDesc(new AttributeChannel(AttributeChannel.DISTANCE,"dist",bitCount, 0, minDistance, maxDistance)));
        return grid;

    }
    
    void makeTestPoint()throws Exception{
        
        if(DEBUG) printf("%s.makeTestPoint()\n", this.getClass().getName());
        double vs = 0.05*MM;
        double w = 10*MM; // half width 
        double firstLayerThickness = 2.5;//1.5;//3.5;//2.4;//1.8;//0.8
        double xmin = -w, xmax = w, ymin = -w, ymax = w, zmin = -w, zmax = w;//10*vs;
        int subvoxelResolution = 100;
        double INF = 1.e5;// point to ignore 
        int voxelSquareSize = 25;// for visualization 
        boolean snapToGrid = false;
        int iterationsCount = 0;
        int threadCount = 1;

        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(new Bounds(xmin,xmax,ymin,ymax,zmin,zmax), vs, vs);
        double pnts[] = new double[]{0,0,0,
                                     //1*vs, 0*vs, 0.5*vs,  0.2*vs, -2.3*vs,  0.5*vs, 
                                     //1*vs, 0*vs, 0.5*vs,  0.2*vs, -2.4*vs,  0.5*vs, 
                                     //1*vs, 0*vs, 0.5*vs,  0.5*vs, -2.4*vs,  0.5*vs, / bad
                                     //7*vs, 0*vs, 0.5*vs,  6.5*vs, -2.4*vs,  0.5*vs, //6.5*vs, 2.4*vs,  0.5*vs, 
                                     
                                     7*vs, 0*vs, 0.5*vs, 
                                     6.5*vs, 2.4*vs,  0.5*vs, 
                                     6.5*vs, -2.4*vs,  0.5*vs, 
                                     
                                     -7*vs, 0*vs, 0.5*vs,                                      
                                     -6.5*vs, 2.4*vs,  0.5*vs, 
                                     -6.5*vs, -2.4*vs,  0.5*vs, 
                                     0, 7*vs,0.5*vs, 
                                     2.4*vs, 6.5*vs, 0.5*vs, 
                                     -2.4*vs,6.5*vs,  0.5*vs, 
                                     0, -7*vs,0.5*vs, 
                                     2.4*vs, -6.5*vs, 0.5*vs, 
                                     -2.4*vs,-6.5*vs,  0.5*vs, 
                                     
        };
        //pnts = makeCircle(0.2*vs, 0.6*vs, 0, 7*vs, 32);
        //pnts = makeCircleZ(0.2*vs, 0.2*vs, 0.2*vs, 7*vs, 128);
        //pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 64);
        //pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.8*w, 64);
        //pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.7*w, 80);
        //pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.7*w, 256);
        //pnts = makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32);
        //pnts = makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32);
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32));
        pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128));
        //pnts = makeUnion(makeUnion(makeCircleX(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 32),makeCircleY(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 32)),makeCircleZ(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 32));
        //pnts = makeUnion(makeUnion(makeCircleX(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 64),makeCircleY(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 64)),makeCircleZ(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 64));
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 64),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 64)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 64));
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 128),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 128)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 128));
        //pnts = makeUnion(makeCircle(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32),makeCircle(2.5*vs, 0.5*vs, 3.5*vs, 7*vs, 32));
        //pnts = makeUnion(makeCircle(-9.5*vs, 0.5*vs, 0, 7*vs, 32),makeCircle(9.5*vs, 0.5*vs, 0, 7*vs, 32));
        //pnts = makeUnion(makeUnion(makeCircle(-10.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32),makeCircle(11.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32)), 
        //                 makeUnion(makeCircle(0.5*vs, -10.5*vs, 0.5*vs, 7*vs, 32),makeCircle(0.5*vs, 11.5*vs, 0.5*vs, 7*vxs, 32)));
        //pnts = makeRandomPoints();
        int pcount = pnts.length/3;
        //printf("pcount: %d\n", (pcount-1));
        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];

        ClosestPointIndexer.getPointsInGridUnits(indexGrid, pnts, pntx, pnty, pntz);
        if(snapToGrid){
            ClosestPointIndexer.snapToVoxels(pntx);
            ClosestPointIndexer.snapToVoxels(pnty);
            ClosestPointIndexer.snapToVoxels(pntz);
        }

        ClosestPointIndexer.initFirstLayer(indexGrid, pntx, pnty, pntz, firstLayerThickness, subvoxelResolution);
        int usedCount = ClosestPointIndexer.removeUnusedPoints(indexGrid, pntx, pnty, pntz);
        printf("grid: [%d x %d x %d] threads: %d points: %d\n", 
               indexGrid.getWidth(), indexGrid.getHeight(), indexGrid.getDepth(), threadCount, pcount);

        int nz = indexGrid.getDepth();

        //if(true) printIndices(indexGrid);
        //for(int z = 0; z < nz; z++)renderDiff(indexGrid, z, pntx, pnty, pntz, voxelSquareSize, fmt("/tmp/dist/distDiff1_%02d.png",z), true);
        
        
        // distribute distances to the whole grid 
        long t0 = time();
        ClosestPointIndexerMT.PI3_MT(pntx, pnty, pntz, indexGrid, threadCount);
        //ClosestPointIndexer.PI3(pntx, pnty, pntz, indexGrid);
        printf("ClosestPointIndexer time: %d\n ", (time() - t0));
        //printIndices(indexGrid);
        //printDiff(indexGrid, pntx, pnty, pntz, true);
        //printDiff(indexGrid, pntx, pnty, pntz, false);
        //printf("maxDiff: %10.3e\n", getMaxDiff(indexGrid, pntx, pnty, pntz));
        for(int z = 0; z < nz; z++){
            //renderDiff(indexGrid, z, pntx, pnty, pntz, voxelSquareSize, fmt("/tmp/dist/distDiff2_%02d.png",z), true);
        }
        
    }

    static double[] makeCircleX(double cx, double cy, double cz, double radius, int count){
        double pnt[] = new double[3*(count+1)];
        for(int i = 0; i < count; i++){
            int j = i+1;
            pnt[3*j] = cx;
            pnt[3*j+1] = cy + radius*cos(2*PI*i/count);
            pnt[3*j+2] = cz +  + radius*sin(2*PI*i/count);
        }
        return pnt;        
    }

    static double[] makeCircleY(double cx, double cy, double cz, double radius, int count){
        double pnt[] = new double[3*(count+1)];
        for(int i = 0; i < count; i++){
            int j = i+1;
            pnt[3*j] = cx + radius*cos(2*PI*i/count);
            pnt[3*j+1] = cy;
            pnt[3*j+2] = cz +  + radius*sin(2*PI*i/count);
        }
        return pnt;        
    }

    static double[] makeCircle(double cx, double cy, double cz, double radius, int count){
        return makeCircleZ(cx, cy, cz, radius, count);
    }
    static double[] makeCircleZ(double cx, double cy, double cz, double radius, int count){
        double pnt[] = new double[3*(count+1)];
        for(int i = 0; i < count; i++){
            int j = i+1;
            pnt[3*j] = cx + radius*cos(2*PI*i/count);
            pnt[3*j+1] = cy + radius*sin(2*PI*i/count);
            pnt[3*j+2] = cz;
        }
        return pnt;
    }
    static double[] makeUnion(double pnt1[], double pnt2[]){
        double pnt[] = new double[pnt1.length + pnt2.length - 3];
        int cnt = 0;
        for(int i = 0; i < pnt1.length; i++){
            pnt[cnt++] = pnt1[i];
        }
        for(int i = 3; i < pnt2.length; i++){ // skip unused point 
            pnt[cnt++] = pnt2[i];
        }
        return pnt;
    }

    static void printIndices(AttributeGrid indexGrid){

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        double coord[] = new double[3];

        for(int z = 0; z < nz; z++){
            printf("z: %d\n", z);
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        // voxel has point accociated with it 
                        printf("%2d ", ind);
                    }  else {
                        printf(" . ");
                    }                                
                }
                printf("\n");
            }
            printf("--\n");
        }
       
    }

    static void printDiff(AttributeGrid indexGrid, double pntx[], double pnty[], double pntz[], boolean printInd){

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        double coord[] = new double[3];
        double maxDiff = 0;
        for(int z = 0; z < nz; z++){
            if(DEBUG)printf("z: %d\n", z);
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        double 
                            dx = pntx[ind] - (x+0.5),
                            dy = pnty[ind] - (y+0.5),
                            dz = pntz[ind] - (z+0.5);
                        double dist = sqrt(dx*dx + dy*dy + dz*dz);
                        double edist = minDistance((x+0.5), (y+0.5), (z+0.5), pntx, pnty, pntz);
                        double diff = abs(dist - edist);
                        if(diff > maxDiff ) maxDiff = diff;
                        int inde = minIndex((x+0.5), (y+0.5), (z+0.5), pntx, pnty, pntz);
                        //if(inde != ind) printf("%2d ", inde);
                        if(inde != ind) {
                            if(printInd) printf("%2d ", inde);                            
                            else {
                                int d = (int)(diff*100+0.5);
                                if(d != 0) printf("%2d ", d);
                                else printf(" . ");
                            }
                        } else {
                            if(printInd) printf("%2d ", inde);
                            else printf(" . ");
                        }
                        //if(diffind != 0.0) 
                            // voxel has point accociated with it 
                        //    printf("%3d ", diffind);
                        //else 
                        //    printf("  o ");
                    }  else {
                        printf("  x ");
                    }                                
                }
                printf("\n");
            }
            printf("--\n");
        }
        printf("maxDiff: %10.3e\n", maxDiff);
    }

    static double getMaxDiff(AttributeGrid indexGrid, double pntx[], double pnty[], double pntz[]){

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        double coord[] = new double[3];
        double maxDiff = 0;
        for(int z = 0; z < nz; z++){
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        double 
                            dx = pntx[ind] - (x+0.5),
                            dy = pnty[ind] - (y+0.5),
                            dz = pntz[ind] - (z+0.5);
                        double dist = sqrt(dx*dx + dy*dy + dz*dz);
                        double edist = minDistance((x+0.5), (y+0.5), (z+0.5), pntx, pnty, pntz);
                        double diff = abs(dist - edist);
                        if(diff > maxDiff ) maxDiff = diff;
                    }
                }
            }
        }
        return maxDiff;
    }
    
    static Color[] getRainbowColors(int count){
        Color c[] = new Color[count];
        for(int i = 0; i < count; i++){
            c[i] = new Color(Color.HSBtoRGB((float)(2.*((i*11)%count)/count),0.5f, 0.95f));
        }
        return c;
    }

    static void renderDiff(AttributeGrid indexGrid, int z, double pntx[], double pnty[], double pntz[], int vs, String filePath, 
                           boolean renderErrors) throws Exception {

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        int picx = nx*vs;
        int picy = ny*vs;
        BufferedImage image = new BufferedImage(picx, picy, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color backgroundColor = new Color(240,240,240);
        Color voxelColor = new Color(255,255,255);
        Color pointColor = new Color(0,0,0);
        Color calcLineColor = new Color(255,0,0);
        Color exactLineColor = new Color(0,0,255);
        Color colors[] = new Color[]{ new Color(255, 150, 150),
                                      new Color(255, 255, 150),
                                      new Color(150, 255, 150),
                                      new Color(150, 255, 255),
                                      new Color(200, 150, 255),
                                      new Color(255, 150, 255),
                                      new Color(200, 150, 150),
                                      new Color(50, 150, 200),
                                      new Color(250, 150, 150),
                                      new Color(150, 250, 150),
                                      new Color(200, 150, 150),
                                      new Color(255, 200, 150),
                                      new Color(255, 255, 150),
                                      new Color(150, 255, 200),
                                      new Color(100, 200, 255),
                                      new Color(150, 150, 255),
                                      new Color(230, 190, 255),
                                      new Color(200, 150, 150),
                                      new Color(150, 150, 200),
                                      new Color(200, 250, 150),

        };

        colors = getRainbowColors(61);

        int pointSize = 4;

        g.setColor(backgroundColor);        
        g.fillRect(0,0,picx, picy);

        g.setColor(voxelColor); 

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                g.fillRect(x*vs,y*vs, vs-1, vs-1);                
            }
        }
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int ind = (int)indexGrid.getAttribute(x,y,z);
                if(ind != 0) {
                    g.setColor(colors[(ind-1)%colors.length]);
                    g.fillRect(x*vs,y*vs, vs-1, vs-1);
                }
            }
        }        

        g.setColor(pointColor); 
        for(int k = 1; k < pntx.length; k++){

            int pz = (int)(pntz[k]);
            if(pz == z) {
                // point is in the z-slice 
                int px = iround(vs*pntx[k]);
                int py = iround(vs*pnty[k]);
                g.fillOval(px-pointSize/2, py-pointSize/2, pointSize,pointSize); 
            }
        }

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int ind = (int)indexGrid.getAttribute(x,y,z);
                if(ind != 0) {
                    double 
                        x0 = (x+0.5),
                        y0 = (y+0.5),
                        z0 = (z+0.5);
                        
                    double 
                        dx = pntx[ind] - x0,
                        dy = pnty[ind] - y0,
                        dz = pntz[ind] - z0;
                    double dist = sqrt(dx*dx + dy*dy + dz*dz);
                    double edist = minDistance(x0, y0, z0, pntx, pnty, pntz);
                    int inde = minIndex(x0, y0, z0, pntx, pnty, pntz);
                    double error = abs(dist - edist);
                    if(inde != ind &&  error > 0.01 && renderErrors) {
                        g.setColor(pointColor); 
                        if(error > 0.8) error = 0.8;
                        double s = (vs*error);
                        g.drawRect(iround(vs*x0 - s/2), iround(vs*y0-s/2), iround(s), iround(s));
                        //g.setColor(calcLineColor); 
                        //g.drawLine(iround(vs*x0), iround(vs*y0), iround(vs*pntx[ind]), iround(vs*pnty[ind]));
                        //g.setColor(exactLineColor); 
                        //g.drawLine(iround(vs*x0), iround(vs*y0), iround(vs*pntx[inde]), iround(vs*pnty[inde]));
                    }
                        
                }                                
            }
        }    
        
        ImageIO.write(image, "png", new File(filePath));

    }

    static double minDistance(double x, double y, double z, double px[],double py[],double pz[]){
        double minDist = 1.e10;
        for(int i = 1; i < px.length; i++){
            double 
                dx = x - px[i],
                dy = y - py[i],
                dz = z - pz[i];
            double dist = sqrt(dx*dx + dy*dy + dz*dz);
            if(dist < minDist) 
                minDist = dist; 
        }
        return minDist;
    }

    static int minIndex(double x, double y, double z, double px[],double py[],double pz[]){
        double minDist = 1.e10;
        int minIndex = 0;
        for(int i = 1; i < px.length; i++){
            double 
                dx = x - px[i],
                dy = y - py[i],
                dz = z - pz[i];
            double dist = sqrt(dx*dx + dy*dy + dz*dz);
            if(dist <= minDist) {
                minDist = dist; 
                minIndex = i;
            }
        }
        return minIndex;
    }

    public static void main(String arg[]) throws Exception {

        for(int i = 0; i < 4; i++){
            //new TestClosestPointIndexerMT().makeTestPoint();
            new TestClosestPointIndexerMT().devTestMTvsSTprecision();
        }        
    }
}
