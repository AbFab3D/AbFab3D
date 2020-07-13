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

import static abfab3d.core.Output.printf;

/**
 * Test SLISliceReader
 *
 * @author Alan Hudson
 */
public class TestSLISliceReader {

    @Test
    public void testSLISimple() throws IOException {

        String filePath = "test/slices/simple.sli";

        SLISliceReader reader = new SLISliceReader(filePath);

        //Assert.assertTrue("Missing slices", reader.getNumSlices() == 95);
    }

    @Test
    public void testSLIGyroplane() throws IOException {
                
        String filePath = "test/slices/gyroplane.sli";

        SLISliceReader reader = new SLISliceReader(filePath);

        //Assert.assertTrue("Missing slices", reader.getNumSlices() == 95);
    }

    @Test
    public void testSLICube() throws IOException {

        String filePath = "test/slices/Box.sli";

        SLISliceReader reader = new SLISliceReader(filePath);

        //Assert.assertTrue("Missing slices", reader.getNumSlices() == 95);
    }


    public void devTestCompare()throws Exception {

        String file1 = "/tmp/slicing/sphere_01.sli";
        String file2 = "/tmp/slicing/sphere_01b.sli";
        //printf("reading %s\n", file1);
        SLISliceReader reader1 = new SLISliceReader(file1);

        //printf("reading %s\n", file2);
        SLISliceReader reader2 = new SLISliceReader(file2);

        printf("slices1: %d\n", reader1.getNumSlices());
        printf("slices2: %d\n", reader2.getNumSlices());
        
        
    }

    public static void main(String arg[]) throws Exception{
        new TestSLISliceReader().devTestCompare();
    }

}
