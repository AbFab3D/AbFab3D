
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

package abfab3d.util;

// External Imports
import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.Raster;
import java.awt.image.BufferedImage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
 * Test ImageUtil methods
 *
 * @author Alan Hudson
 */
public class TestImageUtil extends TestCase {
    public static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageUtil.class);
    }

    /**
     * Test that the spacing of input to output is the same
     */
    public void testUb2us() {
        //
        // distance between values shoule be 0x101 = 257
        //
        for(int i = 0; i < 255; i++){
            int s = ImageUtil.ub2us(i);
            int s1 = ImageUtil.ub2us(i+1);
            assertTrue(fmt("ub2us(0x%02x) = 0x%04x diff: 0x%04x", i, s, (s1-s)), (s1 - s) == 0x101);
            //printf("ub2us(0x%02x) = 0x%04x  ub2us(0x%02x) = 0x%04x  diff: 0x%04x\n", i, s, (i+1), s1, s1 - s);
            
        }
    }
        

    void readJPG()throws Exception{

        String path = "test/images/image_datafile.jpg";

        File f = new File(path);

        //Find a suitable ImageReader
        Iterator readers = ImageIO.getImageReadersByFormatName("JPEG");
        ImageReader reader = null;

        while(readers.hasNext()) {
            reader = (ImageReader)readers.next();
            printf("reader  %s\n", reader);
            if(reader.canReadRaster()) {
                printf("reader found: %s\n", reader);
                break;
            }
        }
        
        //Stream the image file (the original CMYK image)
        ImageInputStream input = ImageIO.createImageInputStream(f); 
        reader.setInput(input); 

        //Read the image raster
        Raster raster = reader.readRaster(0, null); 
        
        //Create a new RGB image
        BufferedImage bi = new BufferedImage(raster.getWidth(), raster.getHeight(), 
                                             BufferedImage.TYPE_4BYTE_ABGR); 
        
        //Fill the new image with the old raster
        bi.getRaster().setRect(raster);
        int w = bi.getWidth();
        int h = bi.getHeight();

        printf("image %s loaded [%d x %d]\n", path, w, h);

    }

 
    public static void main(String arg[])throws Exception {
        
        new TestImageUtil().readJPG();

    }

}
