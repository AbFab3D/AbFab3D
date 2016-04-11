/** 
 *                        Shapeways, Inc Copyright (c) 2014
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

import abfab3d.grid.GridDataChannel;
import abfab3d.grid.GridDataDesc;
import abfab3d.grid.Grid2D;
import abfab3d.grid.Grid2DShort;

import java.io.File;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import abfab3d.util.ImageUtil;


import static java.lang.Math.round;
import static java.lang.Math.abs;

import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.abs;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;

import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.lerp2;
import static abfab3d.util.ImageUtil.lerpColors;


/**
 * Test the DistanceTransform2D class.
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov

 */
public class TestDistanceTransform2D extends BaseTestDistanceTransform {

    private static final boolean DEBUG = true;
    private static final boolean DEBUG_TIMING = true;
    private static final boolean DEBUG_SLICES = false;

    static double pixelSize = 0.1*MM;

    public void testNothing() {

    }

    public void _test1() throws Exception {
        
        //Grid2D grid = loadImageToGrid2D("test/images/white_pixel.png");
        Grid2D grid = loadImageToGrid2D("test/images/letter_R.png");
        //Grid2D grid = loadImageToGrid2D("test/images/letter_R_500.png");
        //Grid2D grid = loadImageToGrid2D("test/images/letter_S_500.png");
        //Grid2D grid = loadImageToGrid2D("test/images/letter_S_blurred_500.png");
        //Grid2D grid = makeTestGrid(20,20);

        printf("grid bounds: [%s] att: %s \n", grid.getGridBounds(), grid.getAttributeDesc());
        if(false)printGridAtt(grid);

        //AttributeChannel dataChannel = new AttributeChannel(AttributeChannel.DISTANCE, "distance", 16, 0, 0., 1.);
        if(false)printGridValue(grid);
        double threshold = 0.5;
        double maxInDistance = 30*MM;
        double maxOutDistance = 30*MM;

        DistanceTransform2DOp dt = new DistanceTransform2DOp(maxInDistance, maxOutDistance, threshold);
        //dt.setDataChannel(dataChannel);
        //dt.setInterpolation(DistanceTransform2D.INTERP_THRESHOLD);
        dt.setInterpolation(DistanceTransform2DOp.INTERP_IF);
        Grid2D distanceGrid = dt.execute(grid);
        
        Grid2D indexGrid = dt.getIndexGrid();
        if(false){
            printf("indexGrid: \n");
            printGridAtt(indexGrid);
        }

        //Grid2D distanceGrid = dt.getDistanceGrid();
        if(false){
            printf("distanceGrid: \n");
            printGridValue(distanceGrid);
            //printGridAttShort(distanceGrid);
        }

        //writeDistanceMap(distanceGrid, 10, 0.1*MM, "/tmp/distance.png");
        writeDistanceMap(distanceGrid, 10, 0.1*MM, "/tmp/distance.png");

        
    }

    public void _test2(){

        GridDataChannel at = new GridDataChannel(GridDataChannel.DISTANCE, "distance", 10, 0, 1.,10.);
        for(int i = -100; i < 100; i++){
            double v = 0.2*i;
            long bits = at.makeAtt(v);
            double v1 = at.getValue(bits);
            printf("%7.2f -> %4x (%12s) -> %7.2f\n", v, bits, Long.toBinaryString(bits), v1);
        }
    } 

    void printGridAtt(Grid2D grid){
        int xmax = 20; 
        int ymax = 20;
        printf("printGridAtt()\n");
        for(int y = 0; y < ymax; y++){
            for(int x = 0; x < xmax; x++){
                //printf(" %4x ", grid.getAttribute(x,y));
                printf(" %4d ", grid.getAttribute(x,y));
            }
            printf("\n");
        }
    }
    void printGridAttShort(Grid2D grid){
        int xmax = 20; 
        int ymax = 20;
        printf("printGridAtt()\n");
        for(int y = 0; y < ymax; y++){
            for(int x = 0; x < xmax; x++){
                //printf(" %4x ", grid.getAttribute(x,y));
                printf(" %4d ", (short)(grid.getAttribute(x,y)));
            }
            printf("\n");
        }
    }
    void printGridValue(Grid2D grid){

        GridDataChannel vm = grid.getAttributeDesc().getChannel(0);
        int xmax = 20; 
        int ymax = 20;
        printf("printGridValue()\n");
        for(int y = 0; y < ymax; y++){
            for(int x = 0; x < xmax; x++){
                printf("%5.2f ", vm.getValue(grid.getAttribute(x,y))/MM);
                //printf("%4d ", vm.get(grid.getAttribute(x,y)));
            }
            printf("\n");
        }
    }

    static void writeDistanceMap(Grid2D grid, int magnification, double valueStep, String path) throws Exception {

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int imgx = nx*magnification;
        int imgy = ny*magnification;
        GridDataDesc adesc = grid.getAttributeDesc();
        GridDataChannel ac = adesc.getChannel(0);

        BufferedImage image =  new BufferedImage(imgx, imgy, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        if(DEBUG) printf("DataBuffer: %s\n", db);        
        int[] sliceData = db.getData();

        double pix = 1./magnification;        
        int debugSize= 30;

        for(int iy = 0; iy < imgy; iy++){

            double y = (iy+0.5)*pix - 0.5;

            for(int ix = 0; ix < imgx; ix++){

                double x = (ix+0.5)*pix-0.5;
                int gx = (int)Math.floor(x);
                int gy = (int)Math.floor(y);
                double dx = x - gx;
                double dy = y - gy;
                //if(ix < magnification/2 && iy < magnification/2)
                //    printf("[%2d %2d](%4.2f %4.2f) ", gx, gy, dx, dy);
                int gx1 = clamp(gx + 1,0, nx-1);
                int gy1 = clamp(gy + 1,0, ny-1);
                gx = clamp(gx,0, nx-1);
                gy = clamp(gy,0, ny-1);
                
                double v00 = ac.getValue(grid.getAttribute(gx,gy));
                double v10 = ac.getValue(grid.getAttribute(gx1,gy));
                double v01 = ac.getValue(grid.getAttribute(gx,gy1));
                double v11 = ac.getValue(grid.getAttribute(gx1,gy1));
                double v = lerp2(v00, v10, v11, v01,dx, dy);
                v /= valueStep;                
                sliceData[ix + (imgy-1-iy)*imgx] = makeDistanceColor(v);
                //sliceData[ix + imgx*iy] = makeDistanceColor(v);
            }
        }
        
        ImageIO.write(image, "png", new File(path));        

    }

    static Grid2D makeTestGrid(int w, int h) {

        Grid2D grid = new Grid2DShort(w,h, pixelSize);
        grid.setAttributeDesc(GridDataDesc.getDefaultAttributeDesc(16));

        for(int y = 7; y < 15; y++){
            for(int x = 7; x < 15; x++){ 
                grid.setAttribute(x,y,0xFFFF);
            }
        }
        return grid;
    }

    static Grid2D loadImageToGrid2D(String imagePath) throws Exception {
        BufferedImage image = ImageIO.read(new File(imagePath)); 
        String imageType = ImageUtil.getImageTypeName(image.getType());
        printf("image: %s [%d x %d] %s\n", imagePath, image.getWidth(), image.getHeight(), imageType);
        Grid2D grid = Grid2DShort.convertImageToGrid(image, pixelSize);
        return grid;
    }

    static int makeDistanceColor(double v){

        if(v >= 0.0) {
            return lerpColors(0xFF0000FF,0xFFFFFFFF,v - Math.floor(v));
        } else {
            return lerpColors(0xFF00FF00,0xFFFFFFFF,1-(v - Math.floor(v)));
        }

    }

    public static void main(String arg[]) throws Exception{

        for(int i = 0; i < 1; i++){
            new TestDistanceTransform2D()._test1();
        }
        //new TestDistanceTransform2D().test2();
                    

    }
    
}
