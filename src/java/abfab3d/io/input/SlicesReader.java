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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import abfab3d.grid.AttributeGrid;
import abfab3d.util.Output;
import static abfab3d.util.Output.printf;

/**
 * Slces Reader.
 *
 * Reads the a bunch of slices images into a grid
 *
 * @author Vladimir Bulatov
 */
public class SlicesReader {

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
            InputStream is = new FileInputStream(fname);
            readSlice(is, grid, i + fistsSlice);
            
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
            ZipEntry entry = zip.getEntry(fname);

            if (entry == null) {
                printf("Cannot find slice file: %s\n",fname);
            }

            InputStream is = zip.getInputStream(entry);
            readSlice(is, grid, i + fistsSlice);
        }

        return 0;
    }

    /**
       read single slice from input stream 
     */
    void readSlice(InputStream is, AttributeGrid grid, int slice){
        
        //TODO 
    } 

}
