package abfab3d.grid.op;

import abfab3d.grid.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.Operation2D;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
 * Test Trim operation
 *
 * @author Alan Hudson
 */
public class TestTrim extends TestCase {
    public static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTrim.class);
    }

    /**
     * Testing handling of an allwhite value=1 image
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

        Grid2D grid = Grid2DShort.convertImageToGrid(image, 0.1*MM);


        Operation2D trim = new TrimOp();
        grid = trim.execute(grid);

        printf("final size: %d x %d\n",grid.getHeight(),grid.getWidth());
        assertTrue("Same size", grid.getHeight() == (image.getHeight(null)));

        assertTrue("keep white", grid.getAttribute(0, 0) != 0);
    }

    /**
     * Testing handling of of different margins
     * @throws Exception
     */
    public void testDiffMargins() throws Exception {
        String path = "test/images/diffmargins.png";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        Grid2D grid = Grid2DShort.convertImageToGrid(image, 0.1*MM);


        Operation2D trim = new TrimOp();
        grid = trim.execute(grid);

        printf("final size: %d x %d\n",grid.getHeight(),grid.getWidth());
        assertTrue("Correct width", grid.getWidth() == 395);
        assertTrue("Correct height", grid.getHeight() == 395);

        int w = grid.getWidth();
        int h = grid.getHeight();

        for(int i=0; i < w; i++) {
            for(int j=0; j < h; j++) {
                assertTrue("Black",grid.getAttribute(i,j) == 0);
            }
        }
    }

    /**
     * Testing handling of of different margins
     * @throws Exception
     */
    public void _testR() throws Exception {
        String path = "test/images/letter_R_500.png";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        Grid2D grid = Grid2DShort.convertImageToGrid(image, 0.1*MM);


        Operation2D trim = new TrimOp();
        grid = trim.execute(grid);

        if (DEBUG) Grid2DShort.write(grid, "/tmp/trim_r.png");

        assertTrue("Should be not black",grid.getAttribute(grid.getWidth()-1,0) > 0);
    }

}
