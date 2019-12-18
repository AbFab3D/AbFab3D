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


import abfab3d.core.Bounds;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static abfab3d.core.Output.printf;

/**
 * Test SLISliceReader
 *
 * @author Alan Hudson
 */
public class TestSLISliceWriter {
    private static final boolean DEBUG = false;

    @Test
    public void testSLISimple() throws IOException {

        String filePath = "test/slices/simple.sli";

        SLISliceReader reader = new SLISliceReader(filePath);

        Bounds bounds = reader.getBounds();
        String out = "/tmp/simple_out.sli";
        SLISliceWriter writer = new SLISliceWriter(out,reader.getUnits());
        writer.write(bounds,"CPSLICECONVERTDATEI",reader.getSlices());

        printf("*** Reading output");
        SLISliceReader reader2 = new SLISliceReader(out);


        compareFiles(reader,reader2);
        //Assert.assertTrue("Missing slices", reader.getNumSlices() == 95);
    }

    // This file fails.  It has a 20K unused block in it.
    public void testSLICube() throws IOException {

        String filePath = "test/slices/Box.sli";

        SLISliceReader reader = new SLISliceReader(filePath);

        Bounds bounds = reader.getBounds();
        String out = "/tmp/box_out.sli";
        SLISliceWriter writer = new SLISliceWriter(out,reader.getUnits());
        writer.write(bounds,"CPSLICECONVERTDATEI",reader.getSlices());

        printf("*** Reading output");
        SLISliceReader reader2 = new SLISliceReader(out);


        compareFiles(reader,reader2);
        //Assert.assertTrue("Missing slices", reader.getNumSlices() == 95);
    }

    @Test
    public void testSLIFiles() throws IOException {

        String inputDir = "test/slices/";
        String outDir = "/tmp/";

        String[] files = new String[] {"planter.sli","wood.sli","gyroplane.sli","hyperbolic.sli","coupler.sli","coaster.sli","laufrad.sli" };

        for(String filePath : files) {
            printf("Testing: %s\n",filePath);
            SLISliceReader reader = new SLISliceReader(inputDir + filePath);

            Bounds bounds = reader.getBounds();
            String out = outDir + filePath;

            SLISliceWriter writer = new SLISliceWriter(out, reader.getUnits());
            writer.write(bounds, "CPSLICECONVERTDATEI", reader.getSlices());

            printf("*** Reading output");
            SLISliceReader reader2 = new SLISliceReader(out);


            compareFiles(reader, reader2);
        }
        //Assert.assertTrue("Missing slices", reader.getNumSlices() == 95);
    }

    public static void compareFiles(SLISliceReader ar, SLISliceReader br) {
        if (DEBUG) printf("slice.  a: %d  b: %d\n",ar.getNumSlices(),br.getNumSlices());
        Assert.assertEquals("Num slices",ar.getNumSlices(),br.getNumSlices());

        // Binary values in ascii format are slightly different than binary so use small epsilon
        double EPS = 1e-5;

        SliceLayer[] aslices = ar.getSlices();
        SliceLayer[] bslices = br.getSlices();

        Assert.assertEquals("Number of slices",aslices.length,bslices.length);

        for(int i=0; i < aslices.length; i++) {
            if (DEBUG) printf("Layer: %d.  aheight: %f  bheight: %f\n",i,aslices[i].getLayerHeight(),bslices[i].getLayerHeight());
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

        int[] aindex = ar.getIndex();
        int[] bindex = br.getIndex();

        for(int i=0; i < aindex.length; i++) {
            printf("Index: %d  a: %d  b: %d\n",i,aindex[i],bindex[i]);
            Assert.assertEquals(aindex[i],bindex[i]);
        }
    }

}
