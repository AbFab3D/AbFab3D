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
import java.util.Random;

import javax.imageio.ImageIO;
import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridInt;

import abfab3d.util.Bounds;


import static java.lang.Math.round;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.PI ;
import static java.lang.Math.sqrt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Output.nanoTime;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.iround;

/**
 * Test the ClosestPointIndexer class.
 *
 * @author Vladimir Bulatov
 */
public class TestClosestPointIndexer extends TestCase {

    private static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestClosestPointIndexer.class);
    }

    public void testNothing()throws Exception{
        // to make tester happy 
    }

    void devTestPI1_bounded()throws Exception{

        int gridSize = 20;
        double maxDistance = 3.01;
        double coord[] = new double[]{0, 5.5, 14.};
        int indices[] = new int[]{1,2};
        double values[] = new double[]{0.,0.};
        
        printf("maxDistance: %7.3f\n", maxDistance);

        printf("coord:\n");
        printArray(coord);
        printf("values:\n");
        printArray(values);
        printf("indices:\n");
        printArray(indices);

        int worki[] = new int[gridSize+1];
        double workd[] = new double[gridSize+1];        
        int closestPointIndex[] = new int[gridSize];
        ClosestPointIndexer.PI1_bounded(gridSize, indices.length, indices, coord, values, maxDistance, closestPointIndex, worki, workd);
        printf("result:\n");
        printHeader(gridSize);
        printArray(closestPointIndex);
        printDistances(closestPointIndex, coord, values);

    }

    void makeTestPoint()throws Exception{
        
        if(DEBUG) printf("%s.makeTestPoint()\n", this.getClass().getName());
        double vs = 0.05*MM;
        //double w = 3*MM; // half width 
        double w = 10*MM; // half width 
        //double firstLayerThickness = 0.9;
        //double firstLayerThickness = 1.7;
        //double firstLayerThickness = 2.7;
        double firstLayerThickness = 2.5;
        double maxDistance = 50.;
        //double xmin = -w, xmax = w, ymin = -w, ymax = w, zmin = 0, zmax = vs;
        double xmin = -w, xmax = w, ymin = -w, ymax = w, zmin = -w, zmax = w;
        int subvoxelResolution = 100;
        double INF = 1.e5;// point to ignore 
        int voxelSquareSize = 10;// for visualization 
        boolean snapToGrid = false;
        int iterationsCount = 0;
        int sliceAxis = 2;
        boolean writeViz = false;
        boolean calcDiff = false;

        Bounds bounds = new Bounds(xmin,xmax,ymin,ymax,zmin,zmax);
        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(bounds, vs, vs);
        double pnts[];
        // = new double[]{0,0,0,
                                     //1*vs, 0*vs, 0.5*vs,  0.2*vs, -2.3*vs,  0.5*vs, 
                                     //1*vs, 0*vs, 0.5*vs,  0.2*vs, -2.4*vs,  0.5*vs, 
                                     //1*vs, 0*vs, 0.5*vs,  0.5*vs, -2.4*vs,  0.5*vs, / bad
                                     //7*vs, 0*vs, 0.5*vs,  6.5*vs, -2.4*vs,  0.5*vs, //6.5*vs, 2.4*vs,  0.5*vs, 
                                     
        //7*vs, 0*vs, 0.5*vs, 
        //                           6.5*vs, 2.4*vs,  0.5*vs, 
        //                                     6.5*vs, -2.4*vs,  0.5*vs, 
                                     
                                         //                           -7*vs, 0*vs, 0.5*vs,                                      
        //                           -6.5*vs, 2.4*vs,  0.5*vs, 
        //                           -6.5*vs, -2.4*vs,  0.5*vs, 
        //                           0, 7*vs,0.5*vs, 
        //                           2.4*vs, 6.5*vs, 0.5*vs, 
        //                           -2.4*vs,6.5*vs,  0.5*vs, 
        //                           0, -7*vs,0.5*vs, 
        //                           2.4*vs, -6.5*vs, 0.5*vs, 
        //                           -2.4*vs,-6.5*vs,  0.5*vs, 
        //                           
        //};
        //pnts = makeCircle(0.2*vs, 0.6*vs, 0, 7*vs, 32);
        //pnts = makeCircleZ(0.2*vs, 0.2*vs, 0.5*vs, 7*vs, 16);
        //pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32);
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128));
        //pnts = makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128);
        //pnts = new double[]{0,0,0, 0.5*vs, 1.5*vs, 0, 1.5*vs, 0.5*vs, 0, 1.5*vs, 1.5*vs, 0}; // 3pnt_centers
        //pnts = new double[]{0,0,0, 0.5*vs, 1.75*vs, 0, 1.75*vs, 0.5*vs, 0, 1.25*vs, 1.25*vs, 0}; // 3_pnt_off center
        //pnts = new double[]{0,0,0, 0.5*vs, 1.75*vs, 0, 1.75*vs, 0.5*vs, 0, 1.5*vs, 1.5*vs, 0}; // 3_pnt_off center
        //pnts = new double[]{0,0,0, 0.5*vs, 1.6*vs, 0, 1.6*vs, 0.5*vs, 0, 1.5*vs, 1.5*vs, 0}; // 3_pnt_off center
        //pnts = new double[]{0,0,0, 0.5*vs, 1.75*vs, 0.5*vs, 1.75*vs, 0.5*vs, 0.5*vs, 1.15*vs, 1.15*vs, 0.5*vs}; // 3_pnt_off center
        //pnts = new double[]{0,0,0, 0.5*vs, 1.75*vs, 0.5*vs, 1.75*vs, 0.5*vs, 0.5*vs, 1.05*vs, 1.05*vs, 0.5*vs}; // 3_pnt_off center (test for firstLayerThickness = 0.7)
        //pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128);
        // large max error ( > 4) with a lot of sorting arrors  
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 128));
        //pnts = makeUnion(makeUnion(makeCircleX(0.1*vs, 0.2*vs, 0.3*vs, 0.9*w, 128),makeCircleY(0.4*vs, 0.5*vs, 0.6*vs, 0.9*w, 128)),makeCircleZ(0.7*vs, 0.8*vs, 0.9*vs, 0.9*w, 128));
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 256),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 256)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.9*w, 256));
        //pnts = makeCircleZ(0.2*vs, 0.2*vs, 0.2*vs, 7*vs, 128);
        //pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 64);
        //pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.7*w, 80);
        //pnts = makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 0.7*w, 256);
        //pnts = makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32);
        //pnts = makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32);
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32));
        //pnts = makeUnion(makeUnion(makeCircleX(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 32),makeCircleY(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 32)),makeCircleZ(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 32));
        //pnts = makeUnion(makeUnion(makeCircleX(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 64),makeCircleY(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 64)),makeCircleZ(0.3*vs, 0.3*vs, 0.3*vs, 7*vs, 64));
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 64),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 64)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 64));
        //pnts = makeUnion(makeUnion(makeCircleX(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 128),makeCircleY(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 128)),makeCircleZ(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 128));
        //pnts = makeUnion(makeCircle(0.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32),makeCircle(2.5*vs, 0.5*vs, 3.5*vs, 7*vs, 32));
        //pnts = makeUnion(makeCircle(-9.5*vs, 0.5*vs, 0, 7*vs, 32),makeCircle(9.5*vs, 0.5*vs, 0, 7*vs, 32));
        //pnts = makeUnion(makeUnion(makeCircle(-10.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32),makeCircle(11.5*vs, 0.5*vs, 0.5*vs, 7*vs, 32)), 
        //                 makeUnion(makeCircle(0.5*vs, -10.5*vs, 0.5*vs, 7*vs, 32),makeCircle(0.5*vs, 11.5*vs, 0.5*vs, 7*vs, 32)));
        pnts = makeRandomPoints(bounds, 500, 121);
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

        ClosestPointIndexer.initFirstLayer(indexGrid, pntx, pnty, pntz, firstLayerThickness);
        int usedCount = ClosestPointIndexer.removeUnusedPoints(indexGrid, pntx, pnty, pntz);
        printf("total points: %d,  used points: %d\n", pcount, usedCount);

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        printf("grid [%d x %d x %d]\n ", nx, ny, nz);
        //if(true) printIndices(indexGrid);

        if(writeViz) {
            for(int z = 0; z < nz; z++){
                renderDiff(indexGrid, z, pntx, pnty, pntz, voxelSquareSize, fmt("/tmp/dist/distDiff1_%02d.png",z), true, sliceAxis);
            }
        }
        long t0 = nanoTime();
        // distribute distances to the whole grid 
        //ClosestPointIndexer.PI3_multiPass(pntx, pnty, pntz, indexGrid, iterationsCount);
        //ClosestPointIndexer.PI3(pntx, pnty, pntz, indexGrid);
        //ClosestPointIndexer.PI3_sorted(pntx, pnty, pntz, indexGrid);
        ClosestPointIndexer.PI3_bounded(pntx, pnty, pntz, maxDistance, indexGrid);
        
        printf("PI3() done: %7.3f ms\n", (nanoTime() - t0)*1.e-6);

        //printIndices(indexGrid);
        //printDiff(indexGrid, pntx, pnty, pntz, true);
        //printDiff(indexGrid, pntx, pnty, pntz, false);
        if(calcDiff){
            double diff[] = getDiff(indexGrid, pntx, pnty, pntz);
            printf("diff1:      %6.4f voxels\n", diff[0]);
            printf("diff2:      %6.4f voxels\n", diff[1]);
            printf("maxDiff:    %6.4f voxels\n", diff[2]);
            printf("relDiff1:   %6.5f\n", diff[3]);
            printf("relDiff2:   %6.5f\n", diff[4]);
            printf("maxRelDiff: %6.5f\n", diff[5]);
        }

        if(writeViz) {
            for(int z = 0; z < nz; z++){
                renderDiff(indexGrid, z, pntx, pnty, pntz, voxelSquareSize, fmt("/tmp/dist/distDiff2_%02d.png",z), true, sliceAxis);
            }
        }
        
    }

    static double[] makeRandomPoints(Bounds bounds, int count, int seed){

        double dx = bounds.xmax - bounds.xmin;
        double dy = bounds.ymax - bounds.ymin;
        double dz = bounds.zmax - bounds.zmin;
        double x0 = bounds.xmin;
        double y0 = bounds.ymin;
        double z0 = bounds.zmin;

        Random rnd = new Random(seed);
        double coord[] = new double[3*(count+1)];
        for(int k = 1; k < count; k++){
            double x = x0 + rnd.nextDouble() * dx;
            double y = x0 + rnd.nextDouble() * dy;
            double z = x0 + rnd.nextDouble() * dz;
            coord[3*k] = x;
            coord[3*k + 1] = y;
            coord[3*k + 2] = z;
        }
        return coord;
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

    static double[] getDiff(AttributeGrid indexGrid, double pntx[], double pnty[], double pntz[]){

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        double coord[] = new double[3];
        double maxDiff = 0;
        double totalDiff2 = 0;
        double totalDiff = 0;
        double totalRelDiff = 0;
        double totalRelDiff2 = 0;
        double maxRelDiff = 0;

        int diffCount = 0;
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
                        totalDiff2 += diff*diff;
                        totalDiff += diff;
                        if(edist != 0.0){ 
                            double relDiff = diff/edist;
                            if(relDiff > maxRelDiff ) maxRelDiff = relDiff;
                            totalRelDiff2 += relDiff*relDiff;
                            totalRelDiff += relDiff;
                        }
                        diffCount++;
                    }
                }
            }
        }
        totalDiff /= diffCount;
        totalDiff2 /= diffCount;
        totalDiff2 = Math.sqrt(totalDiff2);

        totalRelDiff /= diffCount;
        totalRelDiff2 /= diffCount;
        totalRelDiff2 = Math.sqrt(totalRelDiff2);
        
        //printf("diff1:   %7.5f\n", totalDiff);
        //printf("diff2:   %7.5f\n", totalDiff2);
        //printf("maxDiff: %7.5f\n", maxDiff);

        return new double[]{totalDiff, totalDiff2, maxDiff, totalRelDiff, totalRelDiff2, maxRelDiff};
    }
    
    static Color[] getRainbowColors(int count){
        Color c[] = new Color[count];
        for(int i = 0; i < count; i++){
            c[i] = new Color(Color.HSBtoRGB((float)(2.*((i*11)%count)/count),0.5f, 0.95f));
        }
        return c;
    }

    static void renderDiff(AttributeGrid indexGrid, int slice, double pntx[], double pnty[], double pntz[], int vs, String filePath, 
                           boolean renderErrors, int axis) throws Exception {

        int gx = indexGrid.getWidth();
        int gy = indexGrid.getHeight();
        int gz = indexGrid.getDepth();
        double spx[], spy[], spz[]; // points coords in slice system
        int nx, ny, nz;
        switch(axis){
        default:
        case 2: nx = gx; ny = gy; nz = gz; spx = pntx; spy = pnty; spz = pntz;
            break;
        case 1: nx = gz; ny = gx; nz = gy; spx = pntz; spy = pntx; spz = pnty;
            break;
        case 0: nx = gy; ny = gz; nz = gx; spx = pnty; spy = pntz; spz = pntx;
            break;
        }
        

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
        Color colors[] = getRainbowColors(61);

        int pointSize = 4;

        g.setColor(backgroundColor);        
        g.fillRect(0,0,picx, picy);

        g.setColor(voxelColor); 
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                g.fillRect(x*vs,y*vs, vs-1, vs-1);                
            }
        }
        for(int j = 0; j < ny; j++){

            for(int i = 0; i < nx; i++){
                int x,y,z;
                switch(axis){
                default:
                case 2: x = i; y = j; z = slice; break;
                case 1: x = j; y = slice; z = i; break;
                case 0: x = slice; y = i; z = j; break;
                }
                int ind = (int)indexGrid.getAttribute(x,y,z);
                if(ind != 0) {
                    g.setColor(colors[(ind-1)%colors.length]);
                    g.fillRect(i*vs,j*vs, vs-1, vs-1);
                }
            }
        }        

        g.setColor(pointColor); 
        for(int k = 1; k < pntx.length; k++){

            int pz = (int)(spz[k]);
            
            if(pz == slice) {
                // point is in the z-slice 
                int px = iround(vs*spx[k]);
                int py = iround(vs*spy[k]);
                
                g.fillOval(px-pointSize/2, py-pointSize/2, pointSize,pointSize); 
            }
        }

        for(int j = 0; j < ny; j++){
            for(int i = 0; i < nx; i++){

                int x,y,z;
                switch(axis){
                default:
                case 2: x = i; y = j; z = slice; break;
                case 1: x = j; y = slice; z = i; break;
                case 0: x = slice; y = i; z = j; break;
                }
                
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
                    if(inde != ind &&  error > 0.00 && renderErrors) {
                        g.setColor(pointColor); 
                        if(error > 0.8) error = 0.8;
                        double s = (vs*error);
                        g.drawRect(iround(vs*(i+0.5)-s/2), iround(vs*(j+0.5)-s/2), iround(s), iround(s));
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

    static void printHeader(int count){
        for(int i = 0; i < count; i++){
            printf("  %2d", i);                
        }
        printf("\n");                
    }
    static void printArray(int index[]){
        for(int i = 0; i < index.length; i++){
            if(index[i] == 0) 
                printf("  . ");
            else 
                printf("  %2d", index[i]);                
        }
        printf("\n");                
    }

    static void printArray(double value[]){
        for(int i = 0; i < value.length; i++){
            printf(" %7.3f", value[i]);                
        }
        printf("\n");                
    }
    static void printDistances(int pindex[], double coord[], double value[]){
        for(int i = 0; i < pindex.length; i++){
            int ind = pindex[i];
            if(ind == 0) 
                printf("  . ");
            else {
                double x = (i + 0.5) - coord[ind];
                double dist = Math.sqrt(x*x);// + value[ind];
                printf(" %3d", (int)(dist*100+0.5));
            }
        }
        printf("\n");
    }
    

    public static void main(String arg[]) throws Exception {

        for(int i = 0; i < 10; i++){
            new TestClosestPointIndexer().makeTestPoint();
            //new TestClosestPointIndexer().devTestPI1_bounded();
        }        
    }
}
