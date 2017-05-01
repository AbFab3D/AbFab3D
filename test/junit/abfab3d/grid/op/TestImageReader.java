package abfab3d.grid.op;

import abfab3d.core.GridDataChannel;
import abfab3d.core.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.Operation2D;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.time;

/**
 * Test ImageReader
 *
 * @author Vladimir Bulatov
 */
public class TestImageReader extends TestCase {
    public static final boolean DEBUG = false;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageReader.class);
    }

    public void testNothing() {
        
    }


    /**
     * An all black image should be all black after smoothing
     * @throws Exception
     */
    public void devTestReadSVG() throws Exception {

        //String path = "test/images/letter_R.svg";
        //String path = "test/images/letter_R_vb.svg";
        //String path = "test/images/letter_S_blurred.svg";
        String path = "test/images/square.svg";
        
        for(int i = 0; i < 10; i++){
            long t0 = time();
            ImageReader reader = new ImageReader(path);
            reader.set("svgRasterizationWidth", 2000);
            BufferedImage img = reader.getImage();            
            int w = img.getWidth();
            int h = img.getHeight();
            printf("image: %s [%d x %d] loaded: %d ms\n", path, w, h, (time() - t0));
            if(i == 0){
                try {
                    ImageIO.write(img, "png", new File("/tmp/image.png")); 
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String args[]) throws Exception {

        new TestImageReader().devTestReadSVG();
        
    }


}

