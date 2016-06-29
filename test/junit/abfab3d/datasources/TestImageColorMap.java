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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports

        import abfab3d.core.Vec;


        import static abfab3d.core.Output.printf;

        import static abfab3d.core.MathUtil.normalizePlane;

/**
 * Tests the functionality of ImageColorMap
 *
 * @version
 */
public class TestImageColorMap extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImage3D.class);
    }

    int gridMaxAttributeValue = 127;

    public void testBitmap() {

        printf("testBitmap()\n");
    }

    public void devTestColorImage(){
        
        double boxWidth = 20;
        double boxHeight = 20;
        double boxDepth = 20;

        ImageColorMap image = new ImageColorMap("test/images/redcircle_20.png", boxWidth,boxHeight,boxDepth);
        image.initialize();
        
        int w = image.getBitmapWidth();
        int h = image.getBitmapHeight();

        int bitmap[] = new int[w*h];

        image.getBitmapDataInt(bitmap);
        
        printf("==========================[%d x %d]\n", w,h);
        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                printf("%8x ", bitmap[x + y*w]);
            }
            printf("\n");
        }
        printf("==========================\n");

        double dx = boxWidth/w;
        double dy = boxHeight/h;
        double dz = dx;

        double x0 = -boxWidth/2;
        double y0 = -boxHeight/2;
        double z0 = -boxDepth/2;

        Vec pnt = new Vec(3);
        Vec value = new Vec(3);

        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                double xx = (x+0.5)*dx + x0;
                double yy = (y+0.5)*dy + y0;
                double zz = (0.5)*dz + z0;
                pnt.set(xx,yy,zz);
                image.getDataValue(pnt, value);
                printf("%2d %2d %2d,", (int)(value.v[0]*10),(int)(value.v[1]*10),(int)(value.v[2]*10));
            }
            printf("\n");
        }
        printf("==========================\n");
 
    }

     public void devTestBadImage(){
   
 
         double boxWidth = 20;
         double boxHeight = 20;
         double boxDepth = 20;
         String path = "test/images/image_datafile.jpg";

         ImageColorMap image = new ImageColorMap(path, boxWidth,boxHeight,boxDepth);
         
         image.initialize();
         int w = image.getBitmapWidth();
         int h = image.getBitmapHeight();
         printf("image %s: [%d x %d]\n", path, w,h);
        
     }

   public static void main(String[] args) {
        //new TestImageColorMap().devTestColorImage();
        new TestImageColorMap().devTestBadImage();
    }
}