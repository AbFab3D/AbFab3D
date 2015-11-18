package abfab3d.grid.op;

import abfab3d.grid.AttributeChannel;
import abfab3d.grid.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.Operation2D;
import abfab3d.util.ImageGray16;
import abfab3d.util.ImageUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
 * Test Expand operation
 *
 * @author Alan Hudson
 */
public class TestSmooth extends TestCase {
    public static final boolean DEBUG = false;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSmooth.class);
    }

    /**
     * An all black image should be all black after smoothing
     * @throws Exception
     */
    public void testAllBlack() throws Exception {
        String path = "test/images/allblack.jpg";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }


        Grid2D grid = Grid2DShort.convertImageToGrid(image, 0.1*MM);


        Operation2D smooth = new Smooth(0.1*MM);
        grid = smooth.execute(grid);

        if (DEBUG) Grid2DShort.write(grid, "/tmp/smooth_allblack.png");
        assertTrue("All black",isConstant(grid,0,1e-5));

    }

    /**
     * An all white image should be all white after smoothing
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


        Operation2D smooth = new Smooth(0.1*MM);
        grid = smooth.execute(grid);

        if (DEBUG) Grid2DShort.write(grid, "/tmp/smooth_allwhite.png");

        assertTrue("All White", isConstant(grid, 1, 1e-5));

    }

    /**
     * @throws Exception
     */
    public void testLine() throws Exception {
        String path = "test/images/LineLeftToRight.png";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }


        Grid2D grid = Grid2DShort.convertImageToGrid(image, 0.1 * MM);


        double inten0 = getAverageIntensity(grid);

        Operation2D smooth = new Smooth(1.5*MM);
        grid = smooth.execute(grid);

        double inten1 = getAverageIntensity(grid);

        smooth = new Smooth(3.5*MM);
        grid = smooth.execute(grid);

        double inten2 = getAverageIntensity(grid);

        printf("iten 0: %f 1: %f 2: %f\n",inten0,inten1,inten2);

        assertTrue("Increasing intensity1", inten1 > inten0);
        assertTrue("Increasing intensity2", inten2 > inten1);

        if (DEBUG) Grid2DShort.write(grid, "/tmp/smooth.png");
    }

    /**
     * @throws Exception
     */
    public void testTime() throws Exception {
        String path = "test/images/letter_r_500.png";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }


        long lowest1 = Long.MAX_VALUE;
        long tot;
        double blurWidth = 1*MM;
        int TIMES = 50;

        for(int i=0; i < TIMES; i++) {
            Grid2D grid = Grid2DShort.convertImageToGrid(image, 0.1 * MM);

            long t0 = System.currentTimeMillis();
            Operation2D smooth = new Smooth(blurWidth);
            grid = smooth.execute(grid);
            tot = System.currentTimeMillis() - t0;
            printf("time: %d ms\n", tot);
            if (tot < lowest1) {
                lowest1 = tot;
            }
            if (DEBUG) Grid2DShort.write(grid, "/tmp/smooth_r_new.png");
        }


        long lowest2 = Long.MAX_VALUE;

        for(int i=0; i < TIMES; i++) {
            short imageDataShort[] = ImageUtil.getGray16Data(image);
            ImageGray16 imageData = new ImageGray16(imageDataShort, image.getWidth(), image.getHeight());

            double blurSizePixels = blurWidth / (0.1*MM);
            long t0 = time();
            imageData.gaussianBlur(blurSizePixels);
            tot = System.currentTimeMillis() - t0;

            printf("time: %d ms\n", tot);
            if (tot < lowest2) {
                lowest2 = tot;
            }

            if (DEBUG) imageData.write("/tmp/smooth_r_old.png");
        }

        printf("Speed new: %d ms  old: %d ms\n",lowest1,lowest2);

    }

    private static boolean isConstant(Grid2D src,double expected, double eps) {
        int w = src.getWidth();
        int h = src.getHeight();

        AttributeChannel channel = src.getAttributeDesc().getDefaultChannel();

        for(int x=0; x < w; x++) {
            for(int y=0; y < h; y++) {
                double val = channel.getValue(src.getAttribute(x,y));
                if (Math.abs(val - expected) > eps) return false;
            }
        }

        return true;
    }

    /**
     * Get the average intensity of a rendering.
     * @param image
     */
    public static double getAverageIntensity(Grid2D image) {
        AttributeChannel channel = image.getAttributeDesc().getDefaultChannel();

        int width = image.getWidth();
        int height = image.getHeight();

        double intensity;
        double tot=0;
        long count = 0;

        double EPS = 1e-6;

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                intensity =  channel.getValue(image.getAttribute(x, y));

                //System.out.println(x + ", " + y + " : " + red + " " + green + " " + blue);
                if (Math.abs(1.0 - intensity) > EPS) {
                    // non background color
                    count++;
                    tot += intensity;
                }
            }
        }

        if (count == 0) {
            // all background
            return 1;
        }

        return (tot / count);
    }
}

