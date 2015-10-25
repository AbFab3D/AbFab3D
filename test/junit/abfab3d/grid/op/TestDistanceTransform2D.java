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

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeChannel;
import abfab3d.grid.AttributeChannelSigned;
import abfab3d.grid.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.ValueMaker;

import java.io.File;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import abfab3d.util.ImageUtil;


import static java.lang.Math.round;
import static java.lang.Math.abs;

import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.abs;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;

import static abfab3d.util.MathUtil.L2S;

/**
 * Test the DistanceTransformLayered class.
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov

 */
public class TestDistanceTransform2D extends BaseTestDistanceTransform {

    private static final boolean DEBUG = true;
    private static final boolean DEBUG_TIMING = true;
    private static final boolean DEBUG_SLICES = false;

    double pixelSize = 0.1*MM;

    public void test1() throws Exception {
        String imagePath = "test/images/white_pixel.png";
        BufferedImage image = ImageIO.read(new File(imagePath)); 
        String imageType = ImageUtil.getImageTypeName(image.getType());
        printf("image: %s [%d x %d] %s\n", imagePath, image.getWidth(), image.getHeight(), imageType);
        Grid2D grid = Grid2DShort.convertImageToGrid(image, pixelSize);
        printf("grid: bounds: [%s] att: %s \n", grid.getGridBounds(), grid.getAttributeDesc());
        printGridAtt(grid);
        //printGridValue(grid, grid.getAttributeDesc().getChannel(0));
        printGridValue(grid, new AttributeChannelSigned(AttributeChannel.DISTANCE, "distance", 16, 0));
    }

    public void test2(){

        AttributeChannel at = new AttributeChannel(AttributeChannel.DISTANCE, "distance", 10, 0, 1.,10.);
        for(int i = -100; i < 100; i++){
            double v = 0.2*i;
            long bits = at.makeBits(v);
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
                printf(" %4x ", grid.getAttribute(x,y));
            }
            printf("\n");
        }
    }
    void printGridValue(Grid2D grid, AttributeChannel vm){

        int xmax = 20; 
        int ymax = 20;
        printf("printGridValue()\n");
        for(int y = 0; y < ymax; y++){
            for(int x = 0; x < xmax; x++){
                //printf("%5.1e ", vm.makeValue(grid.getAttribute(x,y)));
                printf("%4d ", vm.get(grid.getAttribute(x,y)));
            }
            printf("\n");
        }
    }

    public static void main(String arg[]) throws Exception{

        //new TestDistanceTransform2D().test1();
        new TestDistanceTransform2D().test2();
                    

    }
    
}
