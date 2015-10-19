
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

/**
 * Test ImageGray16 methods
 *
 * @author Alan Hudson
 */
public class TestImageGray16 extends TestCase {
    public static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageGray16.class);
    }

    // TODO: Move to own class
    public void _testImageUtils() {
        printf("ub2i  0: %3d 1: %3d  2: %3d\n",ImageUtil.ub2i((byte)0),ImageUtil.ub2i((byte)1),ImageUtil.ub2i((byte)2));
        printf("ub2us 0: %3d 1: %3d  2: %3d\n", ImageUtil.ub2us(0), ImageUtil.ub2us(1),ImageUtil.ub2us(2));
    }

    /**
     * Testing handling of an allblack value=0 image
     * @throws Exception
     */
    public void testAllBlack() throws Exception{
        String path = "test/images/allblack.jpg";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        if(DEBUG)printf("image %s [%d x %d ] reading done\n", path, image.getWidth(), image.getHeight());

        short imageDataShort[] = ImageUtil.getGray16Data(image);

        ImageGray16 imageData = new ImageGray16(imageDataShort, image.getWidth(), image.getHeight());
        if (DEBUG) {
            try {
                imageData.write("/tmp/allblack.png", 0xFFFF);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }

        for(int i=0; i < imageDataShort.length; i++) {
            assertTrue("raw zero",imageDataShort[i]==0);
        }

        for(int x=0; x < image.getWidth(); x++) {
            for(int y=0; y < image.getHeight(); y++) {
                assertTrue("conv zero", imageData.getDataI(x, y) == 0);
            }
        }
    }

    /**
     * Testing handling of an allblack value=1 image
     * @throws Exception
     */
    public void _testAllBlack1() throws Exception{
        String path = "test/images/allblack_1.png";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        if(DEBUG)printf("image %s [%d x %d ] reading done\n", path, image.getWidth(), image.getHeight());

        short imageDataShort[] = ImageUtil.getGray16Data(image);
        ImageGray16 imageData = new ImageGray16(imageDataShort, image.getWidth(), image.getHeight());

        if (DEBUG) {
            try {
                imageData.write("/tmp/allblack_1.png", 0xFF);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }

        int expected = 1;
        for(int i=0; i < imageDataShort.length; i++) {
            assertTrue("raw one",imageDataShort[i]==expected);
        }

        for(int x=0; x < image.getWidth(); x++) {
            for(int y=0; y < image.getHeight(); y++) {
                assertTrue("conv one", imageData.getDataI(x, y) == 1);
            }
        }

    }

}
