/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.DataBufferByte;

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import abfab3d.grid.AttributeGrid;
import abfab3d.util.Output;
import abfab3d.util.ImageUtil;

import static abfab3d.util.Output.printf;

/**
 * Slces Reader.
 *
 * Reads the a bunch of slices images into a grid
 *
 * @author Vladimir Bulatov
 */
public class SlicesReader {

    static final boolean DEBUG = false;

    static final int ORIENTATION_X = 1, ORIENTATION_Y = 2, ORIENTATION_Z= 3;

    int m_orientation = ORIENTATION_Y;
    String fileTemplate = "slice%04d.png";

    public SlicesReader(){

    }

    public void setOrientation(){

    }

    /**
     reads a set of PNG image files into a grid

     @param grid grid to read slices into
     @param fileTemplate printf style template to generate slice file names
     @param firstFile index of first file in the list
     @param firstSlice index of first slice of the grid to read slice into
     @param count number of slices to read


     */
    public int readSlices(AttributeGrid grid, String fileTemplate,
                          int firstFile, int firstSlice,
                          int count) throws IOException {

        for(int i=0; i < count; i++) {
            String fname = Output.fmt(fileTemplate, i+firstFile);
            if(DEBUG) printf("reading: %s\n", fname);
            InputStream is = new FileInputStream(fname);
            readSlice(is, grid, i + firstSlice);

        }
        return 0;
    }

    /**
       reads a set of PNG image files into a grid
     @param grid grid to read slices into
     @param zip zip file to read slices from
     @param fileTemplate printf style template to generate slice file names
     @param firstFile index of first file in the list
     @param firstSlice index of first slice of the grid to read slice into
     @param count number of slices to read

     */
    public int readSlices(AttributeGrid grid, ZipFile zip, String fileTemplate,
                          int firstFile, int firstSlice,
                          int count) throws IOException {

        for(int i=0; i < count; i++) {
            String fname = Output.fmt(fileTemplate, i+firstFile);
            if(DEBUG) printf("reading: %s\n", fname);
            ZipEntry entry = zip.getEntry(fname);

            if (entry == null) {
                printf("Cannot find slice file: %s\n",fname);
            }

            InputStream is = zip.getInputStream(entry);
            readSlice(is, grid, i + firstSlice);
        }

        return 0;
    }

    /**
       read single slice from input stream
     */
    void readSlice(InputStream is, AttributeGrid grid, int slice) throws IOException{

        BufferedImage image = ImageIO.read(is);
        if(image == null)throw new IOException("unsupported image file format");
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        if(DEBUG){

            printf("image type: %s\n",ImageUtil.getImageTypeName(image.getType()));
        }

        DataBuffer dataBuffer = image.getRaster().getDataBuffer();
        byte componentData[] = new byte[imgWidth*imgHeight];

        switch(image.getType()){

        case BufferedImage.TYPE_BYTE_GRAY:
            {
                componentData = ((DataBufferByte)dataBuffer).getData();
                break;
            }
        case BufferedImage.TYPE_4BYTE_ABGR:
            {
                componentData = new byte[imgWidth*imgHeight];
                ImageUtil.getABGRcomponent(((DataBufferByte)dataBuffer).getData(), 3, componentData);
                break;

            }
        default:
            throw new IOException("unsupported image data format");
        }

        for(int x = 0; x < imgWidth; x++){
            for(int y = 0; y < imgHeight; y++){
                grid.setAttribute(x,slice, y,componentData[x + y*imgWidth]);
            }
        }

    }

}
