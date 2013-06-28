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

import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.MathUtil;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.io.output.SlicesWriter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.STLWriter;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Output.fmt;


public class DevTestWaveletRasterizer {


    public static void testSTLfile() throws Exception {
                
        //int level = 9;
        int maxAttribute = 63;
        //double bounds[] = new double[]{0.,1.,0.,1.,0.,1.};
        double voxelSize = 0.15*MM;
        

        String filePath = "/tmp/stl/mm3mstl.stl";
        //String filePath = "/tmp/00_image_1x1.stl";
        //String filePath = "/tmp/dodecahedron_1a_100mm.stl";
        //String filePath = "/tmp/00_image_4x4_bad.stl";
        //String filePath = "/tmp/star_400.stl";
        
        
        printf("reading file: %s\n", filePath);

        STLReader stl = new STLReader();
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        stl.read(filePath, bb);        
        double bounds[] = new double[6];
        bb.getBounds(bounds);
        printf(" bounds:[(%7.2f %7.2f) (%7.2f %7.2f) (%7.2f %7.2f)] MM \n", bounds[0]/MM,bounds[1]/MM,bounds[2]/MM,bounds[3]/MM,bounds[4]/MM,bounds[5]/MM);
        MathUtil.roundBounds(bounds, voxelSize);
        printf("rbounds:[(%7.2f %7.2f) (%7.2f %7.2f) (%7.2f %7.2f)] MM \n", bounds[0]/MM,bounds[1]/MM,bounds[2]/MM,bounds[3]/MM,bounds[4]/MM,bounds[5]/MM);
        bounds = MathUtil.extendBounds(bounds, 1*voxelSize);
        printf("ebounds:[(%7.2f %7.2f) (%7.2f %7.2f) (%7.2f %7.2f)] MM \n", bounds[0]/MM,bounds[1]/MM,bounds[2]/MM,bounds[3]/MM,bounds[4]/MM,bounds[5]/MM);
        int nx = (int)Math.round((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)Math.round((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)Math.round((bounds[5] - bounds[4])/voxelSize);

        printf("grid size: [%d x %d x %d]\n", nx, ny, nz);
        
        WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
        
        rasterizer.setMaxAttributeValue(maxAttribute);
        
        long t0 = time();

        stl.read(filePath, rasterizer); 

        printf("octree calculation: %d ms\n", (time() - t0));
        
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
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
            double errorFactor = 0.5;
            double smoothWidth = 0.5;
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
            
            STLWriter stlw = new STLWriter("/tmp/raster_to_voxels_0.1.stl");
            meshmaker.makeMesh(grid, stlw);
            stlw.close();
            
        }
    }
    
    public static void main(String arg[]) throws Exception {
        testSTLfile();
    }

}
