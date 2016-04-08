/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid;

// External Imports
import junit.framework.Test;
import junit.framework.TestSuite;
import static abfab3d.util.Output.printf;

// Internal Imports

/**
 * Tests the functionality of AttributeChannel
 *
 * @author Alan Hudson
 * @version
 */
public class TestAttributeChannel extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestAttributeChannel.class);
    }

    /**
     * Test round tripping of double values
     */
    public void testRoundTrip63() {
        printf("test64\n");
        GridDataDesc adesc = GridDataDesc.getDefaultAttributeDesc(63);
        GridDataChannel channel = adesc.getChannel(0);

        double EPS = 1e-14;
        double orig = 0.5;
        long att = channel.makeAtt(orig);
        double val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);

        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);
        orig = 0.1;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 0;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 0.25;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 1.0;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);
    }

    /**
     * Test round tripping of double values
     */
    public void testRoundTrip32() {
        printf("test32\n");
        GridDataDesc adesc = GridDataDesc.getDefaultAttributeDesc(32);
        GridDataChannel channel = adesc.getChannel(0);

        double EPS = 1e-6;
        double orig = 0.5;
        long att = channel.makeAtt(orig);
        double val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);

        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);
        orig = 0.1;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 0;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 0.25;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 1.0;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);
    }

    /**
     * Test round tripping of double values
     */
    public void testRoundTrip16() {
        printf("test16\n");
        GridDataDesc adesc = GridDataDesc.getDefaultAttributeDesc(16);
        GridDataChannel channel = adesc.getChannel(0);

        double EPS = 1e-5;
        double orig = 0.5;
        long att = channel.makeAtt(orig);
        double val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);

        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);
        orig = 0.1;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 0;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 0.25;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 1.0;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);
    }

    /**
     * Test round tripping of double values
     */
    public void testRoundTrip8() {
        printf("test8\n");
        GridDataDesc adesc = GridDataDesc.getDefaultAttributeDesc(8);
        GridDataChannel channel = adesc.getChannel(0);

        double EPS = 1e-2;
        double orig = 0.5;
        long att = channel.makeAtt(orig);
        double val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);

        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);
        orig = 0.1;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 0;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 0.25;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);

        orig = 1.0;
        att = channel.makeAtt(orig);
        val = channel.getValue(att);
        printf("orig: %2.16f  val: %2.16f\n",orig,val);
        assertTrue("Failed Val: " + orig, Math.abs(orig - val) < EPS);
    }

}
