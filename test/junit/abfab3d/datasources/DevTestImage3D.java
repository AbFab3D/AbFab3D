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

package abfab3d.datasources;

// External Imports


// external imports


// Internal Imports
import abfab3d.core.Grid;
import abfab3d.core.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;

        import abfab3d.grid.op.GridMaker;


        import abfab3d.io.output.STLWriter;
import abfab3d.io.output.MeshMakerMT;


        import static abfab3d.core.Output.printf;
        import static abfab3d.core.Units.MM;


        import static abfab3d.core.MathUtil.normalizePlane;


/**
 * Tests the functionality of GridMaker
 *
 * @version
 */
public class DevTestImage3D{


    int gridMaxAttributeValue = 127;

    public void testBitmapAspectRatio() throws Exception {

        printf("testBitmap()\n");

        double voxelSize = 1.e-4;
        int nx = 100;
        int ny = 100;
        int nz = 100;

        double s = 12*MM/2;
        double bounds[] = new double[]{-s, s, -s, s, -s, s};
        
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);            
        grid.setGridBounds(bounds);
        
        String imagePath = "test_imageBitmap.stl";

        Image3D image = new Image3D("test/images/blackcircle.png", 10*MM, 0, 10*MM, voxelSize);
        image.setUseGrayscale(false);
        image.setBlurWidth(voxelSize);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(gridMaxAttributeValue);
        gm.setSource(image);

        gm.makeGrid(grid); 

        writeGrid(grid, "/tmp/test_bitmap.stl");
        
    }

    public void testBitmapGray() throws Exception {

        printf("testBitmap()\n");

        double voxelSize = 1.e-4;
        int nx = 100;
        int ny = 100;
        int nz = 100;

        double s = 12*MM/2;
        double bounds[] = new double[]{-s, s, -s, s, -s, s};
        
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);            
        grid.setGridBounds(bounds);
        
        String imagePath = "test_imageBitmap.stl";

        Image3D image = new Image3D("test/images/blackcircle_blur.png", 10*MM, 0, 10*MM, voxelSize);
        image.setUseGrayscale(true);
        //image.setBlurWidth(voxelSize);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(gridMaxAttributeValue);
        gm.setSource(image);

        gm.makeGrid(grid); 

        writeGrid(grid, "/tmp/test_bitmap.stl");
        
    }
    
    void writeGrid(Grid grid, String path) throws Exception {

        MeshMakerMT mmaker = new MeshMakerMT();
        mmaker.setMaxAttributeValue(gridMaxAttributeValue);
        mmaker.setSmoothingWidth(0.);
        mmaker.setBlockSize(50);
        mmaker.setMaxDecimationError(3.e-10);

        STLWriter stl = new STLWriter(path);
        mmaker.makeMesh(grid, stl);
        stl.close();

    }

    public static void main(String[] args) throws Exception {

        DevTestImage3D dt = new DevTestImage3D();
        //dt.testBitmapAspectRatio();
        dt.testBitmapGray();
    }
}
