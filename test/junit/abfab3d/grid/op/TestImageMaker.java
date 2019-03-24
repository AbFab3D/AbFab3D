package abfab3d.grid.op;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import abfab3d.datasources.Constant;
import abfab3d.core.Bounds;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
 * Test ImageMaker
 *
 * @author Vladimir Bulatov
 */
public class TestImageMaker extends TestCase {

    public static final boolean DEBUG = false;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageMaker.class);
    }

    public void testNothing() {
        
    }

    static void devTestSolidColor() throws Exception {

        printf("devTestSolidColor()\n");
        ImageMaker im = new ImageMaker();
        im.set("threadCount", 1);
        im.set("imgRenderer", new Constant(0,0,1., 0.5));
        im.set("width", 512);
        im.set("height", 512);
        im.setBounds(new Bounds(-1,1,-1,1,-1,1));
        
        BufferedImage image = im.getImage();
        
        ImageIO.write(image, "png", new File("/tmp/image.png")); 
        printf("devTestSolidColor() done\n");
        
    }


    public static void main(String arg[])throws Exception {
        devTestSolidColor();
    }

}