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

package abfab3d.core;

//External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.BaseTestCase;
import static abfab3d.core.Output.printf;

// Internal Imports

/**
 * Tests the functionality of Color
 *
 * @author Alan Hudson
 * @version
 */
public class TestColor extends BaseTestCase  {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestColor.class);
    }
    
    public void testHexParsing() {
        Color c = Color.fromHEX("0xFF00FF");

        double EPS = 1e-6;
        assertTrue("red channel", Math.abs(c.getRed() - 1.0) < EPS);
        assertTrue("green channel", Math.abs(c.getGreen() - 0) < EPS);
        assertTrue("blue channel", Math.abs(c.getBlue() - 1.0) < EPS);
    }

    public void testHSVConversionWhite() {
        Color c = Color.fromHSV(0f,0f,1.0f);

        double EPS = 1e-6;
        assertTrue("red channel", Math.abs(c.getRed() - 1.0) < EPS);
        assertTrue("green channel", Math.abs(c.getGreen() - 1.0) < EPS);
        assertTrue("blue channel", Math.abs(c.getBlue() - 1.0) < EPS);

    }

    public void testHSVConversionPurple() {
        Color c = Color.fromHSV(300/360f,1.0f,0.5f);

        printf("Purple: %s\n",Color.toString(c));
        double EPS = 1e-6;
        assertTrue("red channel", Math.abs(c.getRed() - 0.5) < EPS);
        assertTrue("green channel", Math.abs(c.getGreen() - 0) < EPS);
        assertTrue("blue channel", Math.abs(c.getBlue() - 0.5) < EPS);

    }

    public void testHexParsingNull() {
        Color c = Color.fromHEX(null);

        double EPS = 1e-6;
        assertTrue("red channel", Math.abs(c.getRed() - 0) < EPS);
        assertTrue("green channel", Math.abs(c.getGreen() - 0) < EPS);
        assertTrue("blue channel", Math.abs(c.getBlue() - 0) < EPS);
    }

    public void testToHex() {
        Color c = new Color(1,1,1);
        String s = c.toHEX();

        s = s.toUpperCase();
        assertEquals("white", "0XFFFFFF",s);
    }

}
