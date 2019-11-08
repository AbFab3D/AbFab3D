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


import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

/**
 * Test CLISliceReader
 *
 * @author Alan Hudson
 */
public class TestCLISliceWriter {

    @Test
    public void testBasic() throws IOException {
        String src = "$$HEADERSTART\n" +
                "$$ASCII\n" +
                "$$UNITS/0.100000\n" +
                "$$HEADEREND\n" +
                "$$GEOMETRYSTART\n" +
                "$$LAYER/0.00000\n" +
                "$$LAYER/1.00000\n" +
                "$$POLYLINE/1,1,2,66.00000,43.80000,66.20000,47.90000\n";

        printf("Input:\n");
        printf("%s\n",src);
        StringInputStream sis = new StringInputStream(src);

        CLISliceReader reader = new CLISliceReader(sis);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BufferedOutputStream baos = new BufferedOutputStream(bytes);

        CLISliceWriter writer = new CLISliceWriter(baos,false,MM/10);

        SliceLayer[] slices = reader.getSlices();
        for(SliceLayer slice : slices) {
            writer.addLayer(slice);
        }
        writer.close();

        printf("Output:\n");
        printf("%s\n",new String(bytes.toByteArray()));
    }

    @Test
    public void testRoundtrip() throws IOException {
        String src = "$$HEADERSTART\n" +
                "$$ASCII\n" +
                "$$UNITS/0.100000\n" +
                "$$HEADEREND\n" +
                "$$GEOMETRYSTART\n" +
                "$$LAYER/0.00000\n" +
                "$$LAYER/1.00000\n" +
                "$$POLYLINE/1,1,2,66.00000,43.80000,66.20000,47.90000\n";

        printf("Input:\n");
        printf("%s\n",src);
        StringInputStream sis = new StringInputStream(src);

        printf("Parsing input\n");
        CLISliceReader reader = new CLISliceReader(sis);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BufferedOutputStream baos = new BufferedOutputStream(bytes);

        CLISliceWriter writer = new CLISliceWriter(baos,true,MM/10);

        SliceLayer[] slices = reader.getSlices();
        for(SliceLayer slice : slices) {
            writer.addLayer(slice);
        }
        writer.close();

        byte[] ba = bytes.toByteArray();

        FileOutputStream fos = new FileOutputStream("/tmp/output_binary.cli");
        fos.write(ba,0,ba.length);
        fos.close();

        printf("Output:\n%s\n",new String(ba));
        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(ba));

        printf("Parsing binary output\n");
        CLISliceReader reader2 = new CLISliceReader(bis);

        TestCLISliceReader.compareFiles(reader,reader2);
    }

    @Test
    public void testRoundtrip2() throws IOException {
        String binaryFile = "test/slices/gyroplane_binary.cli";

        printf("Parsing input\n");
        CLISliceReader reader = new CLISliceReader(binaryFile);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BufferedOutputStream baos = new BufferedOutputStream(bytes);

        CLISliceWriter writer = new CLISliceWriter(baos,true,MM/10);

        SliceLayer[] slices = reader.getSlices();
        for(SliceLayer slice : slices) {
            writer.addLayer(slice);
        }
        writer.close();

        byte[] ba = bytes.toByteArray();

        FileOutputStream fos = new FileOutputStream("/tmp/output_binary.cli");
        fos.write(ba,0,ba.length);
        fos.close();

        //printf("Output:\n%s\n",new String(ba));
        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(ba));

        printf("Parsing binary output\n");
        CLISliceReader reader2 = new CLISliceReader(bis);

        TestCLISliceReader.compareFiles(reader,reader2);
    }

    @Test
    public void testFile() throws IOException {
                
        String input = "test/slices/gyroplane.cli";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BufferedOutputStream baos = new BufferedOutputStream(bytes);

        CLISliceReader reader = new CLISliceReader(input);
        CLISliceWriter writer = new CLISliceWriter(baos,false,MM);

        SliceLayer[] slices = reader.getSlices();
        for(SliceLayer slice : slices) {
            writer.addLayer(slice);
        }
        writer.close();


    }


}
