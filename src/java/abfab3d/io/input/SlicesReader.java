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
import java.io.FileInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import abfab3d.core.AttributeGrid;
import abfab3d.core.Output;
import abfab3d.util.ImageUtil;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;

/**
 * Slces Reader.
 *
 * Reads the a bunch of slices images into a grid
 *
 * @author Vladimir Bulatov
 */
public class SlicesReader {

    static final boolean DEBUG = false;
    private static final boolean DEBUG_TIMING = true;

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
        return readSlices(grid, fileTemplate,firstFile, firstSlice, count, 1);
    }


    /**
     reads a set of PNG image files into a grid

     @param grid grid to read slices into
     @param fileTemplate printf style template to generate slice file names
     @param firstFile index of first file in the list
     @param firstSlice index of first slice of the grid to read slice into
     @param count number of slices to read
     @param orientation (0,1,2) axis orthogonel to the slices 
     */
    public int readSlices(AttributeGrid grid, String fileTemplate,
                          int firstFile, int firstSlice,
                          int count, int orientation) throws IOException {

        for(int i=0; i < count; i++) {
            String fname = Output.fmt(fileTemplate, i+firstFile);
            if(DEBUG) printf("reading: %s\n", fname);
            InputStream is = new FileInputStream(fname);
            readSlice(is, grid, i + firstSlice, orientation);

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
        return readSlices(grid, zip, fileTemplate, firstFile, firstSlice,count, 1);

    }

    /**
       reads a set of PNG image files into a grid
     @param grid grid to read slices into
     @param zip zip file to read slices from
     @param fileTemplate printf style template to generate slice file names
     @param firstFile index of first file in the list
     @param firstSlice index of first slice of the grid to read slice into
     @param count number of slices to read
     @param orientation 0,1,2 - axis orthogonal to the slices 
     */
    public int readSlices(AttributeGrid grid, ZipFile zip, String fileTemplate,
                          int firstFile, int firstSlice,
                          int count, int orientation) throws IOException {

        long t0 = time();
        if (DEBUG) printf("Reading slices: %d\n",count);
        for(int i=0; i < count; i++) {
            String fname = Output.fmt(fileTemplate, i+firstFile);
            if(DEBUG) printf("reading: %s\n", fname);
            ZipEntry entry = zip.getEntry(fname);

            if (entry == null) {
                printf("Cannot find slice file: %s\n",fname);
            }

            if (DEBUG) printf("zip: %s entry: %s",zip,entry);
            System.out.flush();
            InputStream is = zip.getInputStream(entry);
            readSlice(is, grid, i + firstSlice, orientation);
        }

        printf("readSlice %d ms\n",(time() - t0));
        return 0;
    }

    /**
       read single slice from input stream
     */
    void readSlice(InputStream is, AttributeGrid grid, int slice, int orientation) throws IOException{

        BufferedImage image = ImageIO.read(is);
        if(image == null)throw new IOException("unsupported image file format");
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        if(DEBUG){

            printf("image type: %s\n",ImageUtil.getImageTypeName(image.getType()));
        }

        DataBuffer dataBuffer = image.getRaster().getDataBuffer();

        byte componentData[] = null;

        switch(image.getType()){

        case BufferedImage.TYPE_BYTE_GRAY:
            {
                //TODO - proccess non DataBufferShort 
                componentData = ((DataBufferByte)dataBuffer).getData();
                break;
            }

        case BufferedImage.TYPE_4BYTE_ABGR:
            {
                componentData = new byte[imgWidth*imgHeight];
                //ImageUtil.getABGRcomponent(((DataBufferByte)dataBuffer).getData(), 3, componentData);
                getChannelData(((DataBufferByte)dataBuffer).getData(), 4, componentData);
                break;

            }
        case BufferedImage.TYPE_3BYTE_BGR:
            {
                componentData = new byte[imgWidth*imgHeight];
                getChannelData(((DataBufferByte)dataBuffer).getData(), 3, componentData);
                break;

            }
        default:
            throw new IOException(fmt("unsupported image data format: %s", ImageUtil.getImageTypeName(image.getType())));
        }

        int coord[] = new int[3];

        for(int x = 0; x < imgWidth; x++){
            for(int y = 0; y < imgHeight; y++){
                getVoxelCoord(slice, x,y, coord, orientation);
                grid.setAttribute(coord[0],coord[1],coord[2],componentData[x + y*imgWidth]);
            }
        }
    }

    static final void getVoxelCoord(int slice, int i, int j, int coord[], int orientation){
        switch(orientation){
        case 0: coord[0] = slice; coord[1] = i; coord[2] = j; break;
        case 1: coord[0] = i; coord[1] = slice; coord[2] = j; break;
        case 2: coord[0] = i; coord[1] = j; coord[2] = slice; break;
        }            
    }


    /**
       get channel data form byte array of imageData of byte data into  ABGR form into 8 bit 
     */
    static void getChannelData(byte imageData[], int bytesPerPixel, byte channelData[]){

        int len = channelData.length;
        // take only last bytes from pixel 
        int offset = bytesPerPixel-1;

        for(int i = 0, k = 0; i < len; i++, k += bytesPerPixel){
            channelData[i] = imageData[k+offset];
        }        
    }

}
