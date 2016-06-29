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


        import static abfab3d.core.Output.printf;

        import static abfab3d.core.MathUtil.normalizePlane;

/**
 * Tests the functionality of GridMaker
 *
 * @version
 */
public class TestImage3D extends TestCase {

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

    public void devTestBlackImage(){
        
        Image3D image = new Image3D("test/images/circle_20.png", 10,10,10);
        image.set("useGrayscale", false);
        image.set("blurWidth", 0.2);

        image.initialize();

    }
    
    public static void devTestLinearMapper(){
    
        LinearMapper mapper = new LinearMapper(-1000, 1000, -0.001, 0.001);

        double vmin = mapper.getVmin();
        double vmax = mapper.getVmax();
        
        printf("%vmin: %f vmax: %f\n", vmin, vmax);

        for(int i = -2000; i < 2000; i += 100) {
            long att = i;
            double v = mapper.map(att);
            int vi = (int)(255*((v - vmin)/(vmax - vmin)))&0xFF;
            byte vb = (byte)vi;
            int vii = (vb) & 0xFF;
            double vv = vii*(vmax - vmin)/255 + vmin;
            printf("%8x %5d -> v:%9.5f vi:%4d vb:%4d vii:%4d vv: %9.5f\n", i, att, v*1000, vi, vb, vii, vv*1000);
        }
    }
    
    public static void main(String[] args) {
        //testLinearMapper();
        new TestImage3D().devTestBlackImage();
    }
}