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

package abfab3d.io.input;

import javax.vecmath.Vector3d;

import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.MathUtil;
import abfab3d.util.TriangleProducer;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.io.output.SlicesWriter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;

import abfab3d.transforms.Scale;

import abfab3d.mesh.MeshDistance;

import abfab3d.geom.TriangulatedModels;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Output.fmt;


public class DevTestWaveletRasterizer {


    public static void testSTLfile() throws Exception {
                
        //int level = 9;
        int maxAttribute = 255;
        //double bounds[] = new double[]{0.,1.,0.,1.,0.,1.};
        double voxelSize = 0.1*MM;
        

        //String filePath = "/tmp/stl/mm3mstl.stl";
        //String filePath = "/tmp/00_image_1x1.stl";
        String filePath = "/tmp/dodecahedron_1a_100mm.stl";
        //String filePath = "/tmp/00_image_4x4_bad.stl";
        //String filePath = "/tmp/star_400.stl";
        
        
        printf("reading file: %s\n", filePath);

        STLReader stl = new STLReader();
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        stl.read(filePath, bb);        
        double bounds[] = bb.getRoundedBounds(voxelSize);
        
        printf("rbounds:[(%7.2f %7.2f) (%7.2f %7.2f) (%7.2f %7.2f)] MM \n", bounds[0]/MM,bounds[1]/MM,bounds[2]/MM,bounds[3]/MM,bounds[4]/MM,bounds[5]/MM);
        bounds = MathUtil.extendBounds(bounds, 1*voxelSize);
        int gn[] = MathUtil.getGridSize(bounds, voxelSize);

        printf("grid size: [%d x %d x %d]\n", gn[0], gn[1], gn[2]);
        
        //MeshRasterizer rasterizer = new MeshRasterizer(bounds, gn[0],gn[1], gn[2]);  
        WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, gn[0],gn[1], gn[2]);  
        rasterizer.setMaxAttributeValue(maxAttribute);
        
        long t0 = time();
        stl.read(filePath, rasterizer); 
        printf("octree calculation: %d ms\n", (time() - t0));        
        AttributeGrid grid = new ArrayAttributeGridByte(gn[0],gn[1], gn[2], voxelSize, voxelSize);
        grid.setGridBounds(bounds);
        //AttributeGrid grid = new ArrayAttributeGridByte(64, 64, 64, voxelSize, voxelSize);
        t0 = time();

        rasterizer.getRaster(grid);
        
        printf("rasterization: %d ms\n", (time() - t0));
        if(false){
            SlicesWriter slicer = new SlicesWriter();
            slicer.setFilePattern("/tmp/slices/slice_%03d.png");
            slicer.setCellSize(1);
            slicer.setVoxelSize(1);            
            slicer.setMaxAttributeValue(maxAttribute);
            
            //slicer.writeSlices(grid);
        } 

        if(true){
            int blockSize = 50;
            //double errorFactor = 0.5;
            double errorFactor = 0.1;
            //double smoothWidth = 0.5;
            double smoothWidth = 0.2;
            int maxDecimationCount= 10;
            int threadsCount = 4;
            //double voxelSize = 2*s/grid.getWidth();
            
            double maxDecimationError = errorFactor*voxelSize*voxelSize;
            
            MeshMakerMT meshmaker = new MeshMakerMT();
            meshmaker.setBlockSize(blockSize);
            meshmaker.setThreadCount(threadsCount);
            meshmaker.setSmoothingWidth(smoothWidth);
            meshmaker.setMaxDecimationError(maxDecimationError);
            meshmaker.setMaxDecimationCount(maxDecimationCount);
            meshmaker.setMaxAttributeValue(maxAttribute);
            
            STLWriter stlw = new STLWriter("/tmp/raster_to_voxels_0..stl");
            meshmaker.makeMesh(grid, stlw);
            stlw.close();
            
        }
    }
    
    public static void testSphere()throws Exception {
        
        TriangulatedModels.Sphere source = new TriangulatedModels.Sphere(5.05*MM, new Vector3d(0,0,0), 10, 0.001*MM);
        TriangulatedModels.Sphere target = new TriangulatedModels.Sphere(5.*MM, new Vector3d(0,0,0), 10, 0.001*MM);
              
        MeshDistance md = new MeshDistance();
        md.setMaxTriangleSize(0.2*MM);
        md.setTriangleSplit(true);
        md.setHashDistanceValues(true);
        md.setUseTriBuckets(true);
        md.setTriBucketSize(0.3*MM);

        md.measure(source, target);  
        long t0 = time();
        printf("  HDF distance: %6.4f mm\n", md.getHausdorffDistance()/MM);
        printf("  L_1 distance: %6.4f mm\n", md.getL1Distance()/MM);
        printf("  L_2 distance: %6.4f mm\n", md.getL2Distance()/MM);
        printf("  min distance: %6.4f mm\n", md.getMinDistance()/MM);        
        printf("  measure time: %d ms\n", (time() - t0));
        
        tri2grid2tri(source, "/tmp/sphere_tri2grid2tri_a.stl");

        //writeSTL(source, "/tmp/mesh_source.stl");
        //writeSTL(target, "/tmp/mesh_target.stl");
    }

    public static void testTetra()throws Exception {
        
        TriangulatedModels.TetrahedronInParallelepiped source = new TriangulatedModels.TetrahedronInParallelepiped(-5*MM,-5*MM,-2*MM, 5*MM, 5*MM, 2*MM,0);
                      
        tri2grid2tri(source, "/tmp/tetra_tri2grid2tri_a.stl");

        writeSTL(source, "/tmp/tetra_orig.stl");
        //writeSTL(target, "/tmp/mesh_target.stl");
    }

    public static void testTorus()throws Exception {
        
        TriangulatedModels.Torus torus = new TriangulatedModels.Torus(4*MM, 6*MM, 0.001*MM);
                      
        //writeSTL(torus, "/tmp/torus_orig.stl");
        tri2grid2tri(torus, "/tmp/torus_t2g2t_2.stl");
        //writeSTL(target, "/tmp/mesh_target.stl");
    }

    public static void testX3DReader()throws Exception {
        
        
        MeshReader tp = new MeshReader("/tmp/wtfix/tooth.x3db");
        
        tp.setTransform(new Scale(0.5));

        //writeSTL(torus, "/tmp/torus_orig.stl");
        //tri2grid2tri(tp, "/tmp/test.stl");
        long t0 = time();
        writeSTL(tp, "/tmp/wtfix/tooth_orig.stl");
        printf("stl conversion: %d ms\n", time() - t0);
        t0 = time();

        tri2grid2tri(tp, "/tmp/wtfix/tooth_t2g.stl", 0.2*MM, 1.5);
        printf("grid conversion: %d ms\n", time() - t0);
        
    }

    // convert triangle set into voxels and back into triangles 
    static TriangleProducer tri2grid2tri(TriangleProducer tp, String path) throws Exception {

        return tri2grid2tri(tp, path, 0.1*MM, 0.3);
        
    }

    static TriangleProducer tri2grid2tri(TriangleProducer tp, String path, double voxelSize, double smoothWidth) throws Exception {
        printf("tri2grid2tri()\n");
        int maxAttribute = 255;

       
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        tp.getTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);
        bounds = MathUtil.extendBounds(bounds, voxelSize);
        int gn[] = MathUtil.getGridSize(bounds, voxelSize);
        printf("grid [%d x %d x %d]\n", gn[0], gn[1], gn[2]);

        WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, gn[0], gn[1],gn[2]);
        
        rasterizer.setMaxAttributeValue(maxAttribute);
        
        long t0 = time();

        tp.getTriangles(rasterizer);

        printf("octree calculation: %d ms\n", (time() - t0));
        
        AttributeGrid grid = new ArrayAttributeGridByte(gn[0],gn[1],gn[2], voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        t0 = time();
        
        rasterizer.getRaster(grid);

        printf("grid calculation: %d ms\n", (time() - t0));
        
        if(true){
            int blockSize = 30;
            double errorFactor = 0.1;
            int maxDecimationCount= 10;
            int threadsCount = 4;
            //double voxelSize = 2*s/grid.getWidth();
            
            double maxDecimationError = errorFactor*voxelSize*voxelSize;
            
            MeshMakerMT meshmaker = new MeshMakerMT();
            meshmaker.setBlockSize(blockSize);
            meshmaker.setThreadCount(threadsCount);
            meshmaker.setSmoothingWidth(smoothWidth);
            meshmaker.setMaxDecimationError(maxDecimationError);
            meshmaker.setMaxDecimationCount(maxDecimationCount);
            meshmaker.setMaxAttributeValue(maxAttribute);            
            meshmaker.setInterpolationAlgorithm(IsosurfaceMaker.INTERPOLATION_INDICATOR_FUNCTION);
            //meshmaker.setInterpolationAlgorithm(IsosurfaceMaker.INTERPOLATION_LINEAR);
            STLWriter stlw = new STLWriter(path);
            meshmaker.makeMesh(grid, stlw);
            stlw.close();
        }
        return null;
        
    }

    
    static void writeSTL(TriangleProducer tp, String path) throws Exception {
        STLWriter stl = new STLWriter(path);
        tp.getTriangles(stl);
        stl.close();
    }

    public static void main(String arg[]) throws Exception {
        //testSTLfile();
        //testSphere();
        //testTorus();
        //testTetra();
        testX3DReader();
    }

}
