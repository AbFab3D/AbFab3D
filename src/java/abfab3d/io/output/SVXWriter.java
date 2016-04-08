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
package abfab3d.io.output;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.GridDataDesc;
import abfab3d.grid.GridDataChannel;
import org.apache.commons.io.IOUtils;


import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static abfab3d.util.Output.printf;

/**
 * Writes a grid out in the svx format.
 *
 * @author Alan Hudson
 */
public class SVXWriter {

    static final boolean DEBUG = false;

    public static final String m_orientationNames[] = {"X","Y","Z"};

    static final int DEFAULT_ORIENTATION = 1;


    int m_orientation = DEFAULT_ORIENTATION;

    public SVXWriter(){
        this(DEFAULT_ORIENTATION);
    }

    public SVXWriter(int orientation){
        m_orientation = orientation;
    }

    /**
     * Writes a grid out to an svx file
     * @param grid
     * @param file
     */
    public void write(AttributeGrid grid, String file) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            write(grid,bos);
        } catch(IOException ioe) {

            ioe.printStackTrace();
            
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
        }

    }

    /**
     * Writes a grid out to an svx file
     * @param grid
     * @param os
     */
    public void write(AttributeGrid grid, OutputStream os) {
        ZipOutputStream zos = null;

        try {

            zos = new ZipOutputStream(os);

            ZipEntry zentry = new ZipEntry("manifest.xml");
            zos.putNextEntry(zentry);
            writeManifest(grid,zos);
            zos.closeEntry();

            SlicesWriter sw = new SlicesWriter();
            GridDataDesc attDesc = grid.getDataDesc();

            for(int i = 0; i < attDesc.size(); i++){

                GridDataChannel channel = attDesc.getChannel(i);
                String channelPattern = channel.getName() + "/" + "slice%04d.png";
                sw.writeSlices(grid,zos,channelPattern,0,0,getSlicesCount(grid), m_orientation, channel.getBitCount(), channel);
            }
        } catch(IOException ioe) {

            ioe.printStackTrace();

        } finally {
            IOUtils.closeQuietly(zos);
        }

    }

    private int getSlicesCount(AttributeGrid grid){
        switch(m_orientation){
        default: 
        case 0: return grid.getWidth();
        case 1: return grid.getHeight();
        case 2: return grid.getDepth();
        }
    }

    private void writeManifest(AttributeGrid grid, OutputStream os) {

        int subvoxelBits = 8; // where it should came from? 

        GridDataDesc attDesc = grid.getDataDesc();
        if(attDesc == null) 
            throw new RuntimeException("grid attribute description is null");
        PrintStream ps = new PrintStream(os);

        double[] bounds = new double[6];

        grid.getGridBounds(bounds);

        printf(ps,"<?xml version=\"1.0\"?>\n");
        printf(ps, "<grid gridSizeX=\"%d\" gridSizeY=\"%d\" gridSizeZ=\"%d\" voxelSize=\"%f\" subvoxelBits=\"%d\" originX=\"%f\" originY=\"%f\" originZ=\"%f\" slicesOrientation=\"%s\">\n", 
               grid.getWidth(), grid.getHeight(),grid.getDepth(),
               grid.getVoxelSize(), subvoxelBits,bounds[0],bounds[2],bounds[4], m_orientationNames[m_orientation]);

        String indent = "    ";
        String indent2 = indent+indent;

        printf(ps, indent+"<channels>\n");
        
        for(int i = 0; i < attDesc.size(); i++){

            GridDataChannel channel = attDesc.getChannel(i);

            String channelPattern = channel.getName() + "/" + "slice%04d.png";
            int bits = channel.getBitCount(); 
            if(DEBUG)printf("channel type: %s name: %s\n", channel.getType(), channel.getName());
            printf(ps, indent2 + "<channel type=\"%s\" slices=\"%s\" bits=\"%d\" />\n",channel.getType(), channelPattern, bits);
        }        
        printf(ps, indent + "</channels>\n");
        printf(ps, "</grid>\n");
        ps.flush();
    }
}
