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
import abfab3d.grid.Grid;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writes a grid out in the svx format.
 *
 * @author Alan Hudson
 */
public class SVXWriter {
    /**
     * Writes a grid out to an svx file
     * @param grid
     * @param file
     */
    public void write(AttributeGrid grid, String file) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;

        try {

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            zos = new ZipOutputStream(bos);

            String format = "density/slice%4d.png";
            ZipEntry zentry = new ZipEntry("manifest.xml");
            zos.putNextEntry(zentry);
            writeManifest(grid,8,format,zos);
            zos.closeEntry();

            SlicesWriter sw = new SlicesWriter();
            sw.writeSlices(grid,zos,format,0,0,grid.getHeight());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (zos != null) zos.close();
                if (bos != null) bos.close();
                if (fos != null) fos.close();
            } catch(Exception e) {
                // ignore
            }
        }

    }

    private void writeManifest(AttributeGrid grid, int subvoxelBits, String format, OutputStream os) {
        PrintWriter pw = new PrintWriter(os);

        double[] bounds = new double[6];

        grid.getGridBounds(bounds);
        pw.println("<?xml version=\"1.0\"?>");

        pw.print("<grid gridSizeX=\"");
        pw.print(grid.getWidth());
        pw.print("\" gridSizeY=\"");
        pw.print(grid.getHeight());
        pw.print("\" gridSizeZ=\"");
        pw.print(grid.getDepth());
        pw.print("\" voxelSize=\"");
        pw.print(grid.getVoxelSize());
        pw.print("\" subvoxelBits=\"");
        pw.print(subvoxelBits);
        pw.print("\" originX=\"");
        pw.print(bounds[0]);
        pw.print("\" originY=\"");
        pw.print(bounds[2]);
        pw.print("\" originZ=\"");
        pw.print(bounds[4]);
        pw.println("\" >");

        String indent = "    ";
        pw.print(indent + "<channels>");
        pw.print(indent + "<channel type=\"");
        pw.print("DENSITY\" slices=\"");
        pw.print(format);
        pw.println("\" />");
        pw.print(indent + "</channels>");

        pw.println("</grid>");

        pw.flush();
    }
}
