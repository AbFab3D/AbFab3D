/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;

// External Imports

import abfab3d.core.GridDataChannel;
import abfab3d.core.GridDataDesc;
import abfab3d.grid.*;
import abfab3d.core.Bounds;
import junit.framework.Test;
import junit.framework.TestSuite;

import static abfab3d.core.Output.printf;

// Internal Imports

/**
 * Tests the functionality of the Downsample operation
 *
 * @author Alan Hudson
 * @version
 */
public class TestResample extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestResample.class);
    }

    /**
     * Test prefer black
     *
     * xy plane
     *
     * xxxx
     * xxx0 ==>x0
     * x000    00
     * 0xx0
     */
    public void testWeightingMin() {
        int w = 4;
        int h = w;

        double pixelSize = 0.001;
        Grid2DShort grid = new Grid2DShort(w,h,pixelSize);
        grid.setGridBounds(new Bounds(0, w * pixelSize, 0, h * pixelSize, 0, pixelSize));
        GridDataDesc adesc = GridDataDesc.getDefaultAttributeDesc(16);
        grid.setDataDesc(adesc);
        GridDataChannel channel = adesc.getChannel(0);

        double ival = 0.5;
        grid.setAttribute(1, 0, channel.makeAtt(ival));
        grid.setAttribute(2, 0, channel.makeAtt(ival));
        grid.setAttribute(0, 1, channel.makeAtt(ival));
        grid.setAttribute(0, 2, channel.makeAtt(ival));
        grid.setAttribute(1, 2, channel.makeAtt(ival));
        grid.setAttribute(2, 2, channel.makeAtt(ival));
        grid.setAttribute(0, 3, channel.makeAtt(ival));
        grid.setAttribute(1, 3, channel.makeAtt(ival));
        grid.setAttribute(2, 3, channel.makeAtt(ival));
        grid.setAttribute(3, 3, channel.makeAtt(ival));


        Operation2D rs = new ResampleOp(w/2,h/2, ResampleOp.WEIGHTING_MINIMUM);
        Grid2DShort dest = (Grid2DShort) rs.execute(grid);

        printf("%4.5f %4.5f\n",channel.getValue(dest.getAttribute(0, 1)),channel.getValue(dest.getAttribute(1, 1)));
        printf("%4.5f %4.5f\n",channel.getValue(dest.getAttribute(0, 0)),channel.getValue(dest.getAttribute(1, 0)));

        double EPS = 1e-5;
        assertTrue("Dest value00", 0 == channel.getValue(dest.getAttribute(0, 0)));
        assertTrue("Dest value10", 0 == channel.getValue(dest.getAttribute(1, 0)));
        assertTrue("Dest value01", Math.abs(ival - channel.getValue(dest.getAttribute(0, 1))) < EPS);
        assertTrue("Dest value11", 0 == channel.getValue(dest.getAttribute(1,1)));
    }

    /**
     * Test averaging
     *
     * xy plane
     *
     * xxxx
     * xxx0 ==>0.5  0.375
     * x000    0.25 0.125
     * 0xx0
     */
    public void testWeightingAverage() {
        int w = 4;
        int h = w;

        double pixelSize = 0.001;
        Grid2DShort grid = new Grid2DShort(w,h,pixelSize);
        grid.setGridBounds(new Bounds(0, w * pixelSize, 0, h * pixelSize, 0, pixelSize));
        GridDataDesc adesc = GridDataDesc.getDefaultAttributeDesc(16);
        grid.setDataDesc(adesc);
        GridDataChannel channel = adesc.getChannel(0);

        double ival = 0.5;
        grid.setAttribute(1, 0, channel.makeAtt(ival));
        grid.setAttribute(2, 0, channel.makeAtt(ival));
        grid.setAttribute(0, 1, channel.makeAtt(ival));
        grid.setAttribute(0, 2, channel.makeAtt(ival));
        grid.setAttribute(1, 2, channel.makeAtt(ival));
        grid.setAttribute(2, 2, channel.makeAtt(ival));
        grid.setAttribute(0, 3, channel.makeAtt(ival));
        grid.setAttribute(1, 3, channel.makeAtt(ival));
        grid.setAttribute(2, 3, channel.makeAtt(ival));
        grid.setAttribute(3, 3, channel.makeAtt(ival));


        Operation2D rs = new ResampleOp(w/2,h/2, ResampleOp.WEIGHTING_AVERAGE);
        Grid2DShort dest = (Grid2DShort) rs.execute(grid);

        printf("%4.5f %4.5f\n",channel.getValue(dest.getAttribute(0, 1)),channel.getValue(dest.getAttribute(1, 1)));
        printf("%4.5f %4.5f\n", channel.getValue(dest.getAttribute(0, 0)), channel.getValue(dest.getAttribute(1, 0)));

        double EPS = 1e-5;
        assertTrue("Dest value00", Math.abs(channel.getValue(dest.getAttribute(0, 0)) - 0.25) < EPS);
        assertTrue("Dest value10", Math.abs(channel.getValue(dest.getAttribute(1, 0)) - 0.125) < EPS);
        assertTrue("Dest value01", Math.abs(channel.getValue(dest.getAttribute(0, 1)) - 0.5) < EPS);
        assertTrue("Dest value11", Math.abs(channel.getValue(dest.getAttribute(1,1)) - 0.375) < EPS);
    }

    /**
     * Test prefer white
     *
     * xy plane
     *
     * xxxx
     * xxx0 ==>xx
     * x000    x0
     * 0x00
     */
    public void testWeightingMax() {
        int w = 4;
        int h = w;

        double pixelSize = 0.001;
        Grid2DShort grid = new Grid2DShort(w,h,pixelSize);
        grid.setGridBounds(new Bounds(0, w * pixelSize, 0, h * pixelSize, 0, pixelSize));
        GridDataDesc adesc = GridDataDesc.getDefaultAttributeDesc(16);
        grid.setDataDesc(adesc);
        GridDataChannel channel = adesc.getChannel(0);

        double ival = 0.5;
        grid.setAttribute(1, 0, channel.makeAtt(ival));
        grid.setAttribute(0, 1, channel.makeAtt(ival));
        grid.setAttribute(0, 2, channel.makeAtt(ival));
        grid.setAttribute(1, 2, channel.makeAtt(ival));
        grid.setAttribute(2, 2, channel.makeAtt(ival));
        grid.setAttribute(0, 3, channel.makeAtt(ival));
        grid.setAttribute(1, 3, channel.makeAtt(ival));
        grid.setAttribute(2, 3, channel.makeAtt(ival));
        grid.setAttribute(3, 3, channel.makeAtt(ival));


        Operation2D rs = new ResampleOp(w/2,h/2, ResampleOp.WEIGHTING_MAXIMUM);
        Grid2DShort dest = (Grid2DShort) rs.execute(grid);

        printf("%4.5f %4.5f\n",channel.getValue(dest.getAttribute(0, 1)),channel.getValue(dest.getAttribute(1, 1)));
        printf("%4.5f %4.5f\n", channel.getValue(dest.getAttribute(0, 0)), channel.getValue(dest.getAttribute(1, 0)));

        double EPS = 1e-5;
        assertTrue("Dest value00", Math.abs(ival - channel.getValue(dest.getAttribute(0, 0))) < EPS);
        assertTrue("Dest value10", 0 == channel.getValue(dest.getAttribute(1,0)));
        assertTrue("Dest value01", Math.abs(ival - channel.getValue(dest.getAttribute(0, 1))) < EPS);
        assertTrue("Dest value11", Math.abs(ival - channel.getValue(dest.getAttribute(1, 1))) < EPS);
    }

}
