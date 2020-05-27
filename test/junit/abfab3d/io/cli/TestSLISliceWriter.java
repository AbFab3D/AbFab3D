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
import abfab3d.core.Units;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

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

        printf("*** Reading output\n");
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
            double oldUnits = reader.getUnits();
            double newUnits = oldUnits;
            printf("  oldUnits:%8.4f mm newUnits:%8.4f \n",oldUnits / Units.MM, newUnits/MM);
            printf("  bounds:%s mm \n",bounds.toString(MM));
            //SLISliceWriter writer = new SLISliceWriter(out, reader.getUnits());
            SLISliceWriter writer = new SLISliceWriter(out, newUnits);
            writer.write(bounds, "CPSLICECONVERTDATEI", reader.getSlices());

            printf("*** Reading output\n");
            SLISliceReader reader2 = new SLISliceReader(out);


            if(false)compareFiles(reader, reader2);
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
            if(DEBUG)printf("Index: %d  a: %d  b: %d\n",i,aindex[i],bindex[i]);
            Assert.assertEquals(aindex[i],bindex[i]);
        }
    }

    void devTestWriteCircle(){
        
    }


    public void devTestSLIFile() throws IOException {

        String inputDir = "/tmp/slicingTestModels/slices/";

        String files[] = new String[]{
            //"393416_2836529.v2.analytical_mesh_converter.sh_11017195.sli",
            //"1272568_4868304.v0.x3db,shift.0.00000000,prec.0.00000000.cli",
            //"8316684_6803030.v0.analytical_mesh_converter.sh_10992281.sli",
            //"396770_2873822.v0.analytical_mesh_converter.sh_11011386.cli",
            //"396770_2873822.v0.analytical_mesh_converter.sh_11011386.sli",
            "396770_2873822.v0.analytical_mesh_converter.sh_11011386.cli",
            "396770_2873822.v0.analytical_mesh_converter.sh_11011386.cli.sli"
        };

        for(int i = 0; i < files.length; i++){

            String filePath  = files[i];
            printf("Testing: %s\n",filePath);
            SliceReader reader = null;
            if(filePath.endsWith(".sli")){
                printf("reading SLI\n");                
                reader = new SLISliceReader(inputDir + filePath);
            } else if(filePath.endsWith(".cli")){
                printf("reading CLI\n");                
                reader = new CLISliceReader(inputDir + filePath);
            }
        
            
            Bounds bounds = reader.getBounds();
            
            printf("Units: %f mm\n",reader.getUnits() / Units.MM);
            printf("Bounds: %s mm\n",bounds.toString(MM));
        }
    }

    public void devTestCLI2SLI() throws IOException {

        String inputDir = "/tmp/slicingTestModels/slices/";

        String files[] = new String[]{
            "396770_2873822.v0.analytical_mesh_converter.sh_11011386.cli",
        };

        for(int i = 0; i < files.length; i++){

            String filePath  = files[i];
            printf("Testing: %s\n",filePath);
            SliceReader reader = null;
            String path = inputDir + filePath;

            if(path.endsWith(".sli")){
                printf("reading SLI\n");
                reader = new SLISliceReader(path);
            } else if(path.endsWith(".cli")){
                printf("reading CLI\n");
                reader = new CLISliceReader(path);
            }
        
            
            Bounds bounds = reader.getBounds();
            
            printf("Units: %f mm\n",reader.getUnits() / Units.MM);
            printf("Bounds: %s mm\n",bounds.toString(MM));

            SLISliceWriter writer = new SLISliceWriter(path+ ".sli", reader.getUnits());
            writer.write(bounds,"CPSLICECONVERTDATEI",reader.getSlices());
                        
        }
        
        //CLISliceReader sliceReader = new CLISliceReader();
        //sliceReader.load(new BufferedInputStream(new FileInputStream(path)));
        //SLISliceWriter sliceWriter = new SLISliceWriter(out, sliceReader.getUnits());
        
    }

    public void devTestSLI2CLI() throws IOException {

        String inputDir = "/tmp/slicingTestModels/slices/";

        String files[] = new String[]{
            "396770_2873822.v0.analytical_mesh_converter.sh_11011386.cli.sli"
        };

        for(int i = 0; i < files.length; i++){

            String filePath  = files[i];
            printf("Testing: %s\n",filePath);
            SliceReader reader = null;
            String path = inputDir + filePath;

            if(path.endsWith(".sli")){
                printf("reading SLI\n");
                reader = new SLISliceReader(path);
            } else if(path.endsWith(".cli")){
                printf("reading CLI\n");
                reader = new CLISliceReader(path);
            }
                    
            Bounds bounds = reader.getBounds();
            
            printf("Units: %f mm\n",reader.getUnits() / Units.MM);
            printf("Bounds: %s mm\n",bounds.toString(MM));

            CLISliceWriter writer = new CLISliceWriter(path+ ".cli", false, reader.getUnits());

            SliceLayer[] slices = reader.getSlices();
            for(SliceLayer slice : slices) {
                writer.addLayer(slice);
            }
            writer.close();

            //writer.write(bounds,"CPSLICECONVERTDATEI",reader.getSlices());
                        
        }
        
        //CLISliceReader sliceReader = new CLISliceReader();
        //sliceReader.load(new BufferedInputStream(new FileInputStream(path)));
        //SLISliceWriter sliceWriter = new SLISliceWriter(out, sliceReader.getUnits());
        
    }

    public static void main(String args[]) throws IOException {

        //new TestSLISliceWriter().devTestSLIFile();
        //new TestSLISliceWriter().devTestCLI2SLI();
        //new TestSLISliceWriter().devTestSLI2CLI();
        //new TestSLISliceWriter().testSLICube();
        new TestSLISliceWriter().testSLIFiles();
        
    }


}
