
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static abfab3d.core.Output.printf;

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
                imageData.write("/tmp/allblack.png");
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
                imageData.write("/tmp/allblack_1.png");
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }

        int expected = 257;  // why 257 and not 256 I don't understand
        for(int i=0; i < imageDataShort.length; i++) {
            assertTrue("raw one",imageDataShort[i]==expected);
        }

        for(int x=0; x < image.getWidth(); x++) {
            for(int y=0; y < image.getHeight(); y++) {
                assertTrue("conv one", imageData.getDataI(x, y) == expected);
            }
        }

    }

    /**
     * Testing handling of an allblack value=1 image
     * @throws Exception
     */
    public void testAllWhite() throws Exception{
        String path = "test/images/allwhite.png";

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
                imageData.write("/tmp/allwhite.png");
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }

        int expected = -1;
        for(int i=0; i < imageDataShort.length; i++) {
            assertTrue("raw one",imageDataShort[i]==expected);
        }

        for(int x=0; x < image.getWidth(); x++) {
            for(int y=0; y < image.getHeight(); y++) {
                int val = imageData.getDataI(x, y);
                assertTrue("conv one", val == 65535);
            }
        }

    }

}
