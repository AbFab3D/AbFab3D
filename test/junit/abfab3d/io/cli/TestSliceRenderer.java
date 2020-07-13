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
import java.io.File;

import java.awt.Graphics2D;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import abfab3d.core.Bounds;

import static java.lang.Math.abs;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;


public class TestSliceRenderer {


    void devTestSingleFile() throws IOException {

        String folder = "/tmp/slicingTestModels/";
        String outFolder = "/tmp/slicingTestModels/images/";

        String filePath = folder + "hyperbolic.sli";

        SLISliceReader reader = new SLISliceReader(filePath);
        Bounds bounds = reader.getBounds();

        printf("file: %s bounds: %s mm\n", filePath, bounds.toString(MM));
        int sliceCount = reader.getNumSlices();
        printf("slice count: %d\n", sliceCount);
        SliceRenderer renderer = new SliceRenderer(bounds);

        for(int i = 0; i < sliceCount; i++){
            BufferedImage image  = renderer.createImage();
            Graphics2D graphics = (Graphics2D)image.getGraphics();
            int sliceIndex = i;
            SliceLayer slice = reader.getSlice(sliceIndex);            
            renderer.renderSlice(graphics, slice);
            String imagePath = outFolder + fmt("slice%04d.png",sliceIndex);
            ImageIO.write(image, "png", new File(imagePath));            
        }
    }

    void devTestAllFilesInFolder(String inFolder, String outFolder) throws IOException {

        //String inFolder = "/tmp/slicingTestModels/compare/tray1n/";
        //String inFolder = "/tmp/slicingTestModels/compare/tray1s/";
        printf("devTestAllFilesInFolder(%s)\n",inFolder);
        File inFiles[] = new File(inFolder).listFiles();
        Bounds totalBounds = null;
        double vs = 0.1*MM;
        double sliceThickness = 0.1*MM;
        SLISliceReader readers[] = new SLISliceReader[inFiles.length];

        for(int i = 0; i < inFiles.length; i++){
            File f = inFiles[i];
            String fpath = f.getAbsolutePath();
            //printf("processing:%s\n",f.getName());
            readers[i] = new SLISliceReader(fpath);
            Bounds bounds = readers[i].getBounds();
            if(totalBounds == null){
                totalBounds = bounds;
            } else {
                totalBounds.combine(bounds);  
            }            
        }

        printf("totalBounds:%s mm\n",totalBounds.toString(MM));
        SliceRenderer renderer = new SliceRenderer(totalBounds);
        int sliceCount = (int)Math.ceil(totalBounds.zmax/sliceThickness);

        for(int k = 0; k < sliceCount; k++){
            //for(int k = sliceCount/2; k < sliceCount/2+1; k++){
            
            BufferedImage image  = renderer.createImage();
            Graphics2D graphics = (Graphics2D)image.getGraphics();
            double sliceHeight = k*sliceThickness;
            renderLayer(readers, graphics, sliceHeight, renderer);
            String imagePath = outFolder + fmt("slice_%04d.png", k);            
            ImageIO.write(image, "png", new File(imagePath));
        }
                
        //Bounds traybounds = new Bounds()

        
    }

    /**
       compare renders slices from 2 folders 
       all folders aftger baseFolder are given relative to the base folder 
     */
    void devTestCompareFolders(String baseFolder, String folder1, String folder2, String outFolder1,String outFolder2,String outFolderDiff) throws IOException {
        
        Bounds bounds1 = new Bounds();
        Bounds bounds2 = new Bounds();

        folder1 = baseFolder + folder1;
        folder2 = baseFolder + folder2;
        outFolder1 = baseFolder + outFolder1;
        outFolder2 = baseFolder + outFolder2;
        outFolderDiff = baseFolder + outFolderDiff;

        printf("devTestCompareFolders()\n");
        printf("   folder1:%s\n", folder1);
        printf("   folder2:%s\n", folder2);
        printf("   outFolder1:%s\n", outFolder1);
        printf("   outFolder2:%s\n", outFolder2);
        printf("   outFolderDiff:%s\n", outFolderDiff);

        new File(outFolder1).mkdir();
        new File(outFolder2).mkdir();
        new File(outFolderDiff).mkdir();

        SliceReader slices1[] = readSlicesInFolder(folder1, bounds1);
        SliceReader slices2[] = readSlicesInFolder(folder2, bounds2);
        
        printf("bounds1:%s mm\n", bounds1.toString(MM));
        printf("bounds2:%s mm\n", bounds2.toString(MM));
        double margins = 1*MM;
        Bounds bounds = new Bounds(bounds1);
        bounds.expand(1*MM);
        
        SliceRenderer renderer = new SliceRenderer(bounds);
        double sliceThickness = 0.1*MM;

        int sliceCount = (int)Math.ceil((bounds.zmax-bounds.zmin)/sliceThickness);

        long totalDiffCount = 0;

        for(int k = 0; k < sliceCount; k++){
        //for(int k = sliceCount/2; k < sliceCount/2+1; k++){
            
            double sliceHeight = bounds.zmin + k*sliceThickness;

            BufferedImage image1  = renderer.createImage();
            Graphics2D graphics1 = (Graphics2D)image1.getGraphics();
            renderLayer(slices1, graphics1, sliceHeight, renderer);
            ImageIO.write(image1, "png", new File(outFolder2 + fmt("%04d.png", k)));

            BufferedImage image2  = renderer.createImage();
            Graphics2D graphics2 = (Graphics2D)image2.getGraphics();
            renderLayer(slices2, graphics2, sliceHeight, renderer);
            ImageIO.write(image2, "png", new File(outFolder1 + fmt("%04d.png", k)));

            BufferedImage imageDiff = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_INT_ARGB);            
            long layerDiffCount = getImagesDifference(image1, image2, imageDiff);
            printf("layer: %4d layerDiffCount: %d\n", k, layerDiffCount); 
            totalDiffCount += layerDiffCount;
            ImageIO.write(imageDiff, "png", new File(outFolderDiff + fmt("%04d.png", k)));

        }

        printf("differentPixelsCount: %d\n", totalDiffCount);
    }

    void devTestCompareFiles(String baseFolder, String file1, String file2, String outFolder1, String outFolder2, String diffFolder) throws IOException {
        
        Bounds bounds1 = new Bounds();
        Bounds bounds2 = new Bounds();

        file1 = baseFolder + file1;
        file2 = baseFolder + file2;

        outFolder1 = baseFolder + outFolder1;
        outFolder2 = baseFolder + outFolder2;
        diffFolder = baseFolder + diffFolder;
        new File(outFolder1).mkdir();
        new File(outFolder2).mkdir();
        new File(diffFolder).mkdir();

        SliceReader slices1[] = new SliceReader[]{readSlicesFile(file1, bounds1)};
        SliceReader slices2[] = new SliceReader[]{readSlicesFile(file2, bounds2)};
        
        printf("bounds1:%s mm\n", bounds1.toString(MM));
        printf("bounds2:%s mm\n", bounds2.toString(MM));
        double margins = 1*MM;
        Bounds bounds = new Bounds(bounds1);
        bounds.expand(1*MM);
        
        SliceRenderer renderer = new SliceRenderer(bounds);
        double sliceThickness = 0.1*MM;

        int sliceCount = (int)Math.ceil((bounds.zmax-bounds.zmin)/sliceThickness);

        long totalDiffCount = 0;
        for(int k = 0; k < sliceCount; k++){
        //for(int k = sliceCount/2; k < sliceCount/2+1; k++){
            
            double sliceHeight = bounds.zmin + k*sliceThickness;

            BufferedImage image1  = renderer.createImage();
            Graphics2D graphics1 = (Graphics2D)image1.getGraphics();
            renderLayer(slices1, graphics1, sliceHeight, renderer);
            ImageIO.write(image1, "png", new File(outFolder1 + fmt("1_%04d.png", k)));

            BufferedImage image2  = renderer.createImage();
            Graphics2D graphics2 = (Graphics2D)image2.getGraphics();
            renderLayer(slices2, graphics2, sliceHeight, renderer);
            ImageIO.write(image2, "png", new File(outFolder2 + fmt("2_%04d.png", k)));

            BufferedImage imageDiff = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_INT_ARGB);            
            long layerDiffCount = getImagesDifference(image1, image2, imageDiff);
            printf("layer: %4d layerDiffCount: %d\n", k, layerDiffCount); 
            totalDiffCount += layerDiffCount;
            ImageIO.write(imageDiff, "png", new File(diffFolder + fmt("d_%04d.png", k)));

        }
        printf("differentPixelsCount: %d\n", totalDiffCount);
    }

    static long getImagesDifference(BufferedImage image1, BufferedImage image2, BufferedImage imageDiff){

        int w = image1.getWidth();
        int h = image1.getHeight();
        int row1[] = new int[w];
        int row2[] = new int[w];
        int rowDiff[] = new int[w];
        long count = 0;
        for(int y = 0; y < h; y++){

            image1.getRGB(0, y, w, 1, row1, 0, w);
            image2.getRGB(0, y, w, 1, row2, 0, w);
            count += getRowDifference(row1, row2, rowDiff);
            imageDiff.setRGB(0, y, w, 1, rowDiff, 0, w);
        }

        return count;

    }

    static int getRowDifference(int row1[], int row2[], int rowDiff[]){

        int count = 0;
        for(int x = 0; x < row1.length; x++){
            
            int p1 = row1[x];
            int p2 = row2[x];
            int color = 0;

            if(p1 == p2){
                rowDiff[x] = 0;
                continue;
            }
            count++;
            if((p1 != 0) && (p2 != 0))
                color = 0xFFFF00FF;
            else if((p1 == 0) && (p2 != 0))
                color = 0xFFFF0000;
            else if((p1 != 0) && (p2 == 0))
                color = 0xFF0000FF;
            
            rowDiff[x] = color;
        }
        return count;
    }

    static SliceReader[] readSlicesInFolder(String inFolder, Bounds outBounds) throws IOException {

        printf("readSlicesInFolder(%s)\n",inFolder);
        File inFiles[] = new File(inFolder).listFiles();
        Bounds totalBounds = null;
        double vs = 0.1*MM;
        double sliceThickness = 0.1*MM;
        SliceReader readers[] = new SLISliceReader[inFiles.length];

        for(int i = 0; i < inFiles.length; i++){
            File f = inFiles[i];
            String fpath = f.getAbsolutePath();
            //printf("processing:%s\n",f.getName());
            readers[i] = new SLISliceReader(fpath);
            Bounds bounds = readers[i].getBounds();
            if(totalBounds == null){
                totalBounds = bounds;
            } else {
                totalBounds.combine(bounds);  
            }            
        }
        outBounds.set(totalBounds);
        return readers;
    }

    static SliceReader readSlicesFile(String filePath, Bounds outBounds) throws IOException {

        printf("readSlicesFile(%s)\n",filePath);
        SliceReader reader = BaseSliceReader.readFile(filePath);
        Bounds bounds = reader.getBounds();
        outBounds.set(bounds);
        return reader;
    }
    
    static void renderLayer(SliceReader readers[], Graphics2D graphics, double sliceHeight, SliceRenderer renderer){
        for(int i = 0; i < readers.length; i++){
            SliceReader reader = readers[i];
            SliceLayer layer = getLayer(reader, sliceHeight);
            if(layer != null) 
                renderer.renderSlice(graphics, layer);            
        }
    }
 
    static SliceLayer getLayer(SliceReader reader, double sliceHeight){
        int count = reader.getNumSlices();
        double precision = 0.2*MM;

        for(int i = 0; i < count; i++){
            SliceLayer layer = reader.getSlice(i);
            double height = layer.getLayerHeight();
            if(abs(height - sliceHeight) < precision)
                return layer;
        }
        return null;
    }
   

    public static void main(String arg[])  throws IOException {

        String baseFolder = "/tmp/slicingTestModels/compare/";
        //new TestSliceRenderer().devTestSingleFile();
        //new TestSliceRenderer().devTestAllFilesInFolder(baseFolder+"tray5n/", baseFolder+"tray5n_png/");
        //new TestSliceRenderer().devTestAllFilesInFolder(baseFolder+"tray5s/",baseFolder+"tray5s_png/");
        //new TestSliceRenderer().devTestCompareFolders(baseFolder+"tray5s/",baseFolder+"tray5n/",baseFolder+"tray5_diff_png/");
        //new TestSliceRenderer().devTestCompareFolders(baseFolder+"test_tray_1/", "nf/","nt/","png_nf/","png_nt/","png_diff/");
        new TestSliceRenderer().devTestCompareFolders(baseFolder+"test_tray_2/", "nf/","nt/","png_nf/","png_nt/","png_diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt1/","1272568_4868304_opt.cli","1272568_4868304_orig.cli","orig/", "opt/", "diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt2/","1677655_5534876_orig.cli","1677655_5534876_opt.cli","orig/", "opt/", "diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt3/","1527142_5307597_orig.cli","1527142_5307597_opt.cli","orig/", "opt/", "diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt4/","3665693_5905400.orig.cli","3665693_5905400.opt.cli","orig/", "opt/", "diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt5/","5757986_5905406.orig.cli","5757986_5905406.opt.cli","orig/", "opt/", "diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt6/","8196147_5861575.orig.cli","8196147_5861575.opt.cli","orig/", "opt/", "diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt7/","8288771_6787679.orig.cli","8288771_6787679.opt.cli","orig/", "opt/", "diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt8/","8310663_6799866.orig.cli","8310663_6799866.opt.cli","orig/", "opt/", "diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt9/","8871781_7087703.orig.cli","8871781_7087703.opt.cli","orig/", "opt/", "diff/");
        //new TestSliceRenderer().devTestCompareFiles(baseFolder+"opt0/","sphere_2cm_32K_tri.orig.cli","sphere_2cm_32K_tri.opt.cli","orig/", "opt/", "diff/");
 
      
    }

}