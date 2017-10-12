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

package abfab3d.io.input;

import javax.vecmath.Vector3d;

import abfab3d.core.DataSource;
import abfab3d.core.Bounds;
import abfab3d.util.ColorMapper;
import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapperDensity;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import abfab3d.core.Vec;

import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataDesc;
import abfab3d.core.GridDataChannel;

import abfab3d.distance.DistanceData;
import abfab3d.distance.DistanceDataSphere;

import abfab3d.datasources.DataSourceGrid;
import abfab3d.datasources.Constant;

import abfab3d.grid.util.GridUtil;

import abfab3d.io.output.GridSaver;

import static java.lang.Math.max;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.CM3;
import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.util.ImageUtil.lerpColors;

import static abfab3d.core.Output.printf;


/**
 * Tests the functionality of GridLoader
 *
 * @version
 */
public class TestGridLoader extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGridLoader.class);
    }

    public void testNothing() throws Exception {
        
    }

    public void testRasterizerDistancePrecision() throws Exception {

        int densityBitCount = 8;
        int distanceBitCount = 16;
        double voxelSize = 2*MM;
        int magnification = 20;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
        double maxInDistance = 50*MM; //voxelSize*2;
        double maxOutDistance = maxInDistance;

        
        //String path  = "test/models/sphere_10cm_5K_tri.stl"; //10cm diameter sphere
        String path  = "test/models/sphere_10cm_32K_tri.stl";
        //String path  = "test/models/sphere_10cm_400K_tri.stl";
        

        printf("voxelSize: %5.2f mm\n", voxelSize/MM);
        printf("path: %s\n", path);
        
        GridLoader loader = new GridLoader();
        loader.setDensityBitCount(densityBitCount);
        loader.setDistanceBitCount(distanceBitCount);
        loader.setPreferredVoxelSize(voxelSize);
        loader.setDistanceAlgorithm(rasterAlgorithm);
        loader.setMaxInDistance(maxInDistance);
        loader.setMaxOutDistance(maxOutDistance);
        loader.setShellHalfThickness(2.5);
        //loader.setSurfaceVoxelSize(1./2);
        //loader.setMargins(2*voxelSize);
        //loader.setUseMultiPass(false);

        AttributeGrid grid = loader.loadDistanceGrid(path);
        
        Difference diff = getDifference(grid, new DistanceDataSphere(50*MM), 0.25);
        
        printf("diffMax:    %7.3f voxels\n", diff.diffmax/voxelSize);
        printf("  diff1: %7.3f voxels\n", diff.diff1/voxelSize);
        printf("  diff2: %7.3f voxels\n", diff.diff2/voxelSize);

        if(true){
            assertTrue("((maxdiff < 0.19) != true)\n", (diff.diffmax/voxelSize < 0.19));
            assertTrue("((diff1 < 0.036) != true)\n", (diff.diff1/voxelSize < 0.036));
            assertTrue("((diff2 < 0.044) != true)\n", (diff.diff2/voxelSize < 0.044));
        }

        if(false){
            GridDataChannel dataChannel = grid.getDataDesc().getChannel(0);
            printf("gridDataChannel: %s\n", dataChannel);
            ColorMapper colorMapper = new ColorMapperDistance(0xFF00FF00,0xFFDDFFDD, 0xFF0000FF,0xFFDDDDFF, voxelSize);
            //int iz = grid.getDepth()/2;
            for(int iz = 0; iz < grid.getDepth(); iz++){
                GridUtil.writeSlice(grid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dist%03d.png", iz));
            }
        }   
    }
    
    public void testRasterizerDistance2Precision() throws Exception {

        int densityBitCount = 8;
        int distanceBitCount = 16;
        double voxelSize = 2*MM;
        int magnification = 20;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE2;
        double maxInDistance = 50*MM;
        double maxOutDistance = 50*MM;

        
        //String path  = "test/models/sphere_10cm_5K_tri.stl"; //10cm diameter sphere
        String path  = "test/models/sphere_10cm_32K_tri.stl";
        //String path  = "test/models/sphere_10cm_400K_tri.stl";
        

        printf("voxelSize: %5.2 mm\n", voxelSize/MM);
        printf("path: %s\n", path);
        
        GridLoader loader = new GridLoader();
        loader.setDensityBitCount(densityBitCount);
        loader.setDistanceBitCount(distanceBitCount);
        loader.setPreferredVoxelSize(voxelSize);
        loader.setDensityAlgorithm(rasterAlgorithm);
        loader.setMaxInDistance(maxInDistance);
        loader.setMaxOutDistance(maxOutDistance);
        //loader.setShellHalfThickness(1.5);
        loader.setShellHalfThickness(2.6);
        //loader.setSurfaceVoxelSize(0.35);
        loader.setSurfaceVoxelSize(1./2);
                
        AttributeGrid grid = loader.loadDistanceGrid(path);
        
        Difference diff = getDifference(grid, new DistanceDataSphere(50*MM), 0.4);
        
        printf("diffMax: %7.3f voxels\n", diff.diffmax/voxelSize);
        printf("  diff1: %7.3f voxels\n", diff.diff1/voxelSize);
        printf("  diff2: %7.3f voxels\n", diff.diff2/voxelSize);

        assertTrue("((maxdiff < 0.37) != true)\n", (diff.diffmax/voxelSize < 0.37));
        assertTrue("((diff1 < 0.042) != true)\n", (diff.diff1/voxelSize < 0.042));
        assertTrue("((diff2 < 0.057) != true)\n", (diff.diff2/voxelSize < 0.057));
        
        if(false){
            GridDataChannel dataChannel = grid.getDataDesc().getChannel(0);
            ColorMapper colorMapper = new ColorMapperDistance(0xFF00FF00,0xFFDDFFDD, 0xFF0000FF,0xFFDDDDFF, 2*MM);
            int iz = grid.getDepth()/2;
            GridUtil.writeSlice(grid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dist%03d.png", iz));
        }   
    }


    /**
       load STL file and write its density to a slice
     */
    public void devTestSTL_density() throws Exception {
        
        printf("devTestSTL()\n");
        int densityBitCount = 8;
        int distanceBitCount = 16;
        double voxelSize = 2*MM;
        int magnification = 4;
        //int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE2;
        //int rasterAlgorithm = GridLoader.RASTERIZER_ZBUFFER;
        //int rasterAlgorithm = GridLoader.RASTERIZER_WAVELET;

        String path[] = new String[] {
            "test/models/sphere_10cm_.4K_tri.stl",
            //"test/models/sphere_10cm_5K_tri.stl",
            //            "test/models/sphere_10cm_32K_tri.stl",
            //"test/models/gyrosphere.stl",
            //"test/models/sphere_10cm_400K_tri.stl"
        };

            
        GridLoader loader = new GridLoader();
        loader.setMaxGridSize(1000*1000*1000L);
        loader.setDensityBitCount(densityBitCount);
        loader.setDistanceBitCount(distanceBitCount);
        loader.setPreferredVoxelSize(voxelSize);
        loader.setDensityAlgorithm(rasterAlgorithm);
        
        for(int i = 0; i < path.length; i++){
            printf("voxelSize: %7.2f mm \n", voxelSize/MM);
            printf("rasterization algorithm: %s \n", GridLoader.getAlgorithmName(rasterAlgorithm));            
            printf("loading %s\n", path[i]);
            long t0 = time();
            AttributeGrid densGrid = loader.loadDensityGrid(path[i]);
            printf("grid %s loaded in %d ms\n", path[i], (time() - t0));
            t0 = time();
            double volume = getVolume(densGrid);
            printf("volume: %7.3f CM^3 in %d ms\n", volume/CM3, (time() - t0));
            //for(int iz = densGrid.getDepth()/2; iz < densGrid.getDepth()/2+1; iz++){
            for(int iz = 0; iz < densGrid.getDepth(); iz++){
                GridDataChannel dataChannel = densGrid.getDataDesc().getChannel(0);
                ColorMapper colorMapper = new ColorMapperDensity(0xFF000000, 0xFFFF0000, 1./2);
                GridUtil.writeSlice(densGrid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dens%03d.png", iz));
                //GridUtil.printSliceAttribute(densGrid, iz);
            }
        }
    }

    /**
       load STL file and write its density to a slice
     */
    public void devTestSTL_distance() throws Exception {
        
        printf("running devTestSTL_distance()\n");
        int densityBitCount = 8;
        int distanceBitCount = 16;
        double voxelSize = 1*MM;
        double bandWidth = 0.5*MM;
        double maxInDistance = 5*MM;
        double maxOutDistance = 5*MM;
        int magnification = 3;
        int threadCount = 4;
        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE2;
        //int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
        //int rasterAlgorithm = GridLoader.RASTERIZER_ZBUFFER;
        //int rasterAlgorithm = GridLoader.RASTERIZER_WAVELET;

        String path[] = new String[] {
            //"test/models/sphere_10cm_.4K_tri.stl",
            //"test/models/sphere_10cm_5K_tri.stl",
            //"test/models/gyrosphere.stl",
            "test/models/sphere_10cm_32K_tri.stl",
            //"test/models/sphere_10cm_400K_tri.stl"
            //"test/models/deer.stl",
            //"test/models/coffee_maker.x3db"

        };

            
        GridLoader loader = new GridLoader();
        loader.setMaxGridSize(1000*1000*1000L);
        loader.setDensityBitCount(densityBitCount);
        loader.setDistanceBitCount(distanceBitCount);
        loader.setPreferredVoxelSize(voxelSize);
        loader.setDensityAlgorithm(rasterAlgorithm);
        loader.setMaxInDistance(maxInDistance);
        loader.setMaxOutDistance(maxOutDistance);
        loader.setThreadCount(threadCount);
        loader.setMargins(max(maxInDistance, maxOutDistance));        
        loader.setShellHalfThickness(2.);
        loader.setSurfaceVoxelSize(1);
        
        for(int i = 0; i < path.length; i++){
            printf("path: %s\n", path[i]);
            printf("voxelSize: %7.2f mm \n", voxelSize/MM);            
            printf("algorithm: %s \n", GridLoader.getAlgorithmName(rasterAlgorithm));            
            printf("distanceRange: (%7.3f, %7.3f)mm \n", (-maxInDistance/MM), (maxOutDistance/MM));            
            printf("threadCount: %d\n", threadCount);            
            
            long t0 = time();
            AttributeGrid grid = loader.loadDistanceGrid(path[i]);

            GridDataDesc desc = grid.getDataDesc();            
            GridDataChannel channel = desc.getChannel(0);
            int np = 100;
            for(int k = 0; k <= np; k++){
                double v = k*(maxOutDistance+maxInDistance)/np-maxInDistance;
                long att = channel.makeAtt(v);
                printf("%7.5f -> 0x%x\n", v, att);
            }
            
            long tt = (time() - t0);
            printf("gridSize: [%d x %d x %d]\n", grid.getWidth(), grid.getHeight(), grid.getDepth(), loader.getTriangleCount());
            printf("triangleCount: %d \n", loader.getTriangleCount());
            printf("loadingTime: %d ms\n", tt);
            GridSaver writer = new GridSaver();
            writer.write(grid, "/tmp/dist.svx");
            //if(false){int iz = grid.getDepth()/2;
            //for(int iz = grid.getDepth()/2; iz < grid.getDepth()/2+1; iz++){
            for(int iz = 0; iz < grid.getDepth(); iz += 1){
                GridDataChannel dataChannel = grid.getDataDesc().getChannel(0);
                ColorMapper colorMapper = new ColorMapperDistance(0xFF00FF00,0xFFDDFFDD, 0xFF0000FF,0xFFDDDDFF, bandWidth);
                if(false)GridUtil.writeSlice(grid, magnification, iz, dataChannel, colorMapper, fmt("/tmp/dens/dist%03d.png", iz));
            }
        }
    }

    /**
       calculates volume of the shape defined as voxels
     */
    double getVolume(AttributeGrid grid){
        
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        GridDataDesc ad = grid.getDataDesc();
        GridDataChannel ch = ad.getDensityChannel();
        double sum = 0;
        double voxelVolume = grid.getVoxelSize();
        voxelVolume = voxelVolume*voxelVolume*voxelVolume;

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    sum += ch.getValue(grid.getAttribute(x,y,z));                    
                }                
            }            
        }
        return sum*voxelVolume;
    }
    
    static Difference getDifference(AttributeGrid grid, DistanceData distData){
        return getDifference(grid, distData,10.);
    }
    static Difference getDifference(AttributeGrid grid, DistanceData distData, double bigError){
        
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double vs = grid.getVoxelSize();
        double maxDiff = 0;
        double diffSum = 0;
        double diff2Sum = 0;
        int count = 0;

        GridDataChannel dataChannel = grid.getDataDesc().getChannel(0);
        double pnt[] = new double[3];
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    grid.getWorldCoords(x, y, z, pnt);
                    double exactDist = distData.getDistance(pnt[0],pnt[1],pnt[2]);
                    double gridDist = dataChannel.getValue(grid.getAttribute(x,y,z));
                    double diff = Math.abs(gridDist - exactDist);
                    if(diff/vs > bigError) printf("dist: %7.2f exact:%7.2f diff: %4.2f (%8.5f %8.5f %8.5f)\n", gridDist/vs, exactDist/vs, diff/vs, pnt[0]/vs, pnt[1]/vs, pnt[2]/vs);
                    if(diff > maxDiff){
                        maxDiff = diff;
                    }
                    diffSum += diff;
                    diff2Sum += diff*diff;                    
                    count++;
                }
            }
        }
        return new Difference(maxDiff, diffSum/count, Math.sqrt(diff2Sum/count));
    }


    static class Difference {
        double diffmax;
        double diff1;
        double diff2;

        Difference(double diffmax, double diff1, double diff2){
            this.diffmax = diffmax;
            this.diff1 = diff1;
            this.diff2 = diff2;
        }

    }


    /**
       rasterizatoion of textured triangles 
     */   
    public void devTestRasterizeTexturedTriangles() throws Exception {

        printf("devTestRasterizeTexturedTriangles()\n");        
        GridLoader loader = new GridLoader();
        loader.setThreadCount(8);
        loader.setMaxInDistance(1*MM);
        loader.setMaxOutDistance(1*MM);           
        loader.setMargins(1*MM);   
        loader.setPreferredVoxelSize(0.5*MM);   
        double Rout = 30*MM;
        double Rin = 15*MM;
        AttributeGrid grid = loader.rasterizeAttributedTriangles(new TexturedTorus(Rout, Rin,3,40),new GradientColorizer(new Vector3d(0.05,0,0)));
        GridSaver writer = new GridSaver();
        writer.setWriteTexturedMesh(true);
        writer.setTexPixelSize(1);
        writer.setMeshSmoothingWidth(1);
        writer.setTexTriExt(1.5);
        writer.setTexTriGap(1.5);
        //String outPath = "/tmp/tex/outGrid.svx";
        //String outPath = "/tmp/tex/texturedTorus3.x3d";
        String outPath = "/tmp/tex/texturedTorus7.x3d";
        printf("writing: %s\n", outPath);
        writer.write(grid, outPath);
        printf("done\n");        
        
    }


    public void devTestDataSourceGrid() throws Exception {

        printf("devTestDataSourceGrid()\n");
        GridLoader loader = new GridLoader();
        loader.setThreadCount(8);
        loader.setMaxInDistance(1*MM);
        loader.setMaxOutDistance(1*MM);           
        loader.setMargins(1*MM);   
        loader.setPreferredVoxelSize(0.5*MM);   
        double R = 5*MM;
        AttributeGrid grid = loader.rasterizeAttributedTriangles(new TexturedSphere(R, 40,40),new Constant(0.2, 0.4, 0.6));
        //AttributeGrid grid = loader.rasterizeAttributedTriangles(new TexturedSphere(R, 40,40),new GradientColorizer(new Vector3d(0.05,0,0)));
        DataSourceGrid ds = new DataSourceGrid(grid);
        ds.initialize();
        double s = R+1*MM;
        printDataSourceSlice(ds, new Bounds(-s,s,-s,s,-s,s),20,20, 1*MM);
        /*
        GridSaver writer = new GridSaver();
        writer.setWriteTexturedMesh(true);
        writer.setTexPixelSize(1);
        writer.setMeshSmoothingWidth(1);
        writer.setTexTriExt(1.5);
        writer.setTexTriGap(1.5);
        //String outPath = "/tmp/tex/outGrid.svx";
        //String outPath = "/tmp/tex/texturedTorus3.x3d";
        String outPath = "/tmp/tex/texturedTorus7.x3d";
        printf("writing: %s\n", outPath);
        writer.write(grid, outPath);
        printf("done\n");        
        */
    }

    public void devTestGradientColorizer(){
        GradientColorizer gc = new GradientColorizer(new Vector3d(10*MM,0,0));
        int n = 40;
        Vec v0 = new Vec(0.,0.,0.);
        Vec v1 = new Vec(20*MM,0.,0.);
        Vec v = new Vec(3);
        Vec c = new Vec(3);
        
        double delta = 2./n;
        for(int k = 0; k <= n; k++){
            
            Vec.lerp(v0, v1, delta*k, v);
            gc.getDataValue(v, c);
            printf("(%7.5f %7.5f)->(%5.2f,%5.2f,%5.2f)\n", v.v[0],v.v[1],c.v[0],c.v[1],c.v[2]);
            
        }
    }

    static void printDataSourceSlice(DataSource ds, Bounds bounds, int nx, int ny, double z){
        
        Vec pnt = new Vec(3);
        Vec data = new Vec(4);

        for(int d = 0; d < 4; d++){            
            printf("d: %d ===\n", d);
            for(int iy = 0; iy < ny; iy++){            
                for(int ix = 0; ix < nx; ix++){
                    double x = bounds.xmin + (bounds.xmax-bounds.xmin)*ix/nx;
                    double y = bounds.ymin + (bounds.ymax-bounds.ymin)*iy/ny;
                    pnt.v[0] = x;
                    pnt.v[1] = y;
                    pnt.v[2] = z;
                    ds.getDataValue(pnt, data);
                    printf("%8.5f ", data.v[d]);
                }
                printf("\n");
            }
            printf("===\n");
        }
    }

    static int debugCount = 1000;

    public static void main(String[] args) throws Exception{

        for(int k = 0; k < 1; k++){
            //new TestGridLoader().devTestSTL_density();
            //new TestGridLoader().devTestSTL_distance();
            //new TestGridLoader().testRasterizerDistancePrecision();
            new TestGridLoader().testRasterizerDistance2Precision();
            //new TestGridLoader().testRasterizerDistancePrecision();
            //new TestGridLoader().testRasterizerDistance2Precision();
            //new TestGridLoader().devTestRasterizeTexturedTriangles();
            //new TestGridLoader().devTestDataSourceGrid();
            //new TestGridLoader().devTestGradientColorizer();
        }
    }
}
