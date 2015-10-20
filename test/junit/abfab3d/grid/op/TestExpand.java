package abfab3d.grid.op;

import abfab3d.grid.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.util.ImageGray16;
import abfab3d.util.ImageUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static abfab3d.util.Output.printf;

/**
 * Test Expand operation
 *
 * @author Alan Hudson
 */
public class TestExpand extends TestCase {
    public static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestExpand.class);
    }

    /**
     * Testing handling of an allblack value=1 image
     * @throws Exception
     */
    public void testAllWhite() throws Exception {
        String path = "test/images/allwhite.png";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        Grid2D grid = Grid2DShort.convertImageToGrid(image);


        Expand expand = new Expand(new int[] {0,1,0,0});
        grid = expand.execute(grid);

        Grid2DShort.write(grid, "/tmp/expand.png");

        assertTrue("Height increased", grid.getHeight() == (image.getHeight(null) + 1));

        assertTrue("keep white", grid.getAttribute(0, 0) != 0);
        assertTrue("keep white", grid.getAttribute(1,1) != 0);
        assertTrue("keep white", grid.getAttribute(0,2) != 0);
        assertTrue("keep white", grid.getAttribute(image.getWidth()-1,image.getHeight()-1) != 0);
    }

    /**
     * Testing handling of an allwhite image with black fill
     * @throws Exception
     */
    public void testAllWhite2() throws Exception {
        String path = "test/images/allwhite.png";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        Grid2D grid = Grid2DShort.convertImageToGrid(image);


        Expand expand = new Expand(new int[] {2,2,2,2},0);
        grid = expand.execute(grid);

        Grid2DShort.write(grid, "/tmp/expand_all.png");

        assertTrue("Width increased", grid.getWidth() == (image.getHeight(null) + 4));
        assertTrue("Height increased", grid.getHeight() == (image.getHeight(null) + 4));

        assertTrue("zero fill", grid.getAttribute(0, 0) == 0);
        assertTrue("keep white", grid.getAttribute(1,1) == 0);
        assertTrue("keep white", grid.getAttribute(64,64) != 0);
    }
}
