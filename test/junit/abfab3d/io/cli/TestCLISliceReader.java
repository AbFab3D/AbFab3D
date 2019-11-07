/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.cli;


import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Test CLISliceReader
 *
 * @author Alan Hudson
 */
public class TestCLISliceReader {

    @Test
    public void testCLIBasic() throws IOException {
                
        String filePath = "test/slices/gyroplane.cli";

        CLISliceReader reader = new CLISliceReader(filePath);

        Assert.assertTrue("Missing slices", reader.getNumSlices() == 95);
    }

    @Test
    public void testCLIBasicBinary() throws IOException {

        String asciiFile = "test/slices/gyroplane.cli";
        String binaryFile = "test/slices/gyroplane_binary.cli";

        CLISliceReader ar = new CLISliceReader(asciiFile);
        CLISliceReader br = new CLISliceReader(binaryFile);

        Assert.assertEquals("Num slices",ar.getNumSlices(),br.getNumSlices());

        // Binary values in ascii format are slightly different than binary so use small epsilon
        double EPS = 1e-5;

        SliceLayer[] aslices = ar.getSlices();
        SliceLayer[] bslices = br.getSlices();

        for(int i=0; i < aslices.length; i++) {
            Assert.assertEquals("Layer Height",aslices[i].getLayerHeight(),bslices[i].getLayerHeight(),EPS);
            PolyLine[] alines = aslices[i].getPolyLines();
            PolyLine[] blines = bslices[i].getPolyLines();
            Assert.assertEquals("Num Lines",alines.length,blines.length);
            for(int j=0; j < alines.length; j++) {
                Assert.assertEquals("id",alines[j].getId(),blines[j].getId());
                Assert.assertEquals("dir",alines[j].getDir(),blines[j].getDir());
                double[] apoints = alines[j].getPoints();
                double[] bpoints = blines[j].getPoints();

                Assert.assertEquals("n",apoints.length,bpoints.length);
                for(int n=0; n < apoints.length; n++) {
                    Assert.assertEquals("points",apoints[n],bpoints[n],EPS);
                }
            }

        }
    }

    @Test
    public void testCLIHatches() throws IOException {

        String filePath = "test/slices/gyroplane_hatches.cli";

        CLISliceReader reader = new CLISliceReader(filePath);

        Assert.assertTrue("Missing slices", reader.getNumSlices() == 7);

        SliceLayer[] layers = reader.getSlices();
        PolyLine[] lines = layers[1].getPolyLines();
        Hatches[] hatches = layers[1].getHatches();

        Assert.assertEquals("Num lines",1,lines.length);
        Assert.assertEquals("Num hatches",1,hatches.length);
    }

}
