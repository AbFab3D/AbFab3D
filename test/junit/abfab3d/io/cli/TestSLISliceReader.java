/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.cli;


import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Test SLISliceReader
 *
 * @author Alan Hudson
 */
public class TestSLISliceReader {

    @Test
    public void testSLIBasic1() throws IOException {
                
        String filePath = "test/slices/gyroplane.sli";

        SLISliceReader reader = new SLISliceReader(filePath);

        //Assert.assertTrue("Missing slices", reader.getNumSlices() == 95);
    }

    @Test
    public void testSLIBasic2() throws IOException {

        String filePath = "test/slices/Box.sli";

        SLISliceReader reader = new SLISliceReader(filePath);

        //Assert.assertTrue("Missing slices", reader.getNumSlices() == 95);
    }
}
