/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package app.common;

import abfab3d.grid.Grid;
import org.apache.commons.io.FileUtils;
import abfab3d.io.output.GridSaver;
import java.awt.*;
import java.io.*;

/**
 * Common code for viewing X3D files.
 *
 * @author Alan Hudson
 */
public class X3DViewer {
    /**
     * View an X3D file using an external X3DOM player
     *
     * @param grid
     * @param smoothSteps
     * @param maxDecimateError
     * @throws IOException
     */
    public static void viewX3DOM(Grid grid, int smoothSteps, double maxDecimateError) throws IOException {
        // TODO: Make thread safe using tempDir
        String dest = "/tmp/ringpopper/";
        File dir = new File(dest);
        dir.mkdirs();

        String pf = "x3dom/x3dom.css";
        String dest_pf = dest + pf;
        File dest_file = new File(dest_pf);
        File src_file = new File("src/html/" + pf);
        if (!dest_file.exists()) {
            System.out.println("Copying file: " + src_file + " to dir: " + dest);
            FileUtils.copyFile(src_file, dest_file, true);
        }

        pf = "x3dom/x3dom.js";
        dest_pf = dest + pf;
        dest_file = new File(dest_pf);
        src_file = new File("src/html/" + pf);
        if (!dest_file.exists()) {
            System.out.println("Copying file: " + src_file + " to dir: " + dest);
            FileUtils.copyFile(src_file, dest_file, true);
        }
        File f = new File("/tmp/ringpopper/out.xhtml");
        FileOutputStream fos = new FileOutputStream(f);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        PrintStream ps = new PrintStream(bos);


        try {
            ps.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
            ps.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
            ps.println("<head>");
            ps.println("<meta http-equiv=\"X-UA-Compatible\" content=\"chrome=1\" />");
            ps.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
            ps.println("<title>Ring Popper Demo</title>");
//            ps.println("<link rel='stylesheet' type='text/css' href='http://www.x3dom.org/x3dom/release/x3dom.css'></link>");
            ps.println("<link rel='stylesheet' type='text/css' href='x3dom/x3dom.css'></link>");
            ps.println("</head>");
            ps.println("<body>");

            ps.println("<p class='case'>");
            GridSaver.writeIsosurfaceMaker(grid, bos, "x3d", smoothSteps, maxDecimateError);

//            ps.println("<script type='text/javascript' src='http://www.x3dom.org/x3dom/release/x3dom.js'></script>");
            ps.println("<script type='text/javascript' src='x3dom/x3dom.js'></script>");

            ps.println("</p>");
            ps.println("</body></html>");
        } finally {
            bos.flush();
            bos.close();
            fos.close();
        }

        Desktop.getDesktop().browse(f.toURI());
    }

    /**
     * View an X3D file using an external X3DOM player
     *
     * @param filename
     * @throws IOException
     */
    public static void viewX3DOM(String filename, float[] pos) throws IOException {
        viewX3DOM(new String[] {filename}, pos);
    }

    /**
     * View an X3D file using an external X3DOM player
     *
     * @param filename
     * @throws IOException
     */
    public static void viewX3DOM(String[] filename, float[] pos) throws IOException {
        // TODO: Make thread safe using tempDir
        String dest = "/tmp/";
        File dir = new File(dest);
        dir.mkdirs();

        String pf = "x3dom/x3dom.css";
        String dest_pf = dest + pf;
        File dest_file = new File(dest_pf);
        File src_file = new File("src/html/" + pf);
        if (!dest_file.exists()) {
            System.out.println("Copying file: " + src_file + " to dir: " + dest);
            FileUtils.copyFile(src_file, dest_file, true);
        }

        pf = "x3dom/x3dom.js";
        dest_pf = dest + pf;
        dest_file = new File(dest_pf);
        src_file = new File("src/html/" + pf);
        if (!dest_file.exists()) {
            System.out.println("Copying file: " + src_file + " to dir: " + dest);
            FileUtils.copyFile(src_file, dest_file, true);
        }
        File f = new File("/tmp/out.xhtml");
        FileOutputStream fos = new FileOutputStream(f);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        PrintStream ps = new PrintStream(bos);


        try {
            ps.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
            ps.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
            ps.println("<head>");
            ps.println("<meta http-equiv=\"X-UA-Compatible\" content=\"chrome=1\" />");
            ps.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
            ps.println("<title>Ring Popper Demo</title>");
//            ps.println("<link rel='stylesheet' type='text/css' href='http://www.x3dom.org/x3dom/release/x3dom.css'></link>");
            ps.println("<link rel='stylesheet' type='text/css' href='x3dom/x3dom.css'></link>");
            ps.println("</head>");
            ps.println("<body>");

            ps.println("<p class='case'>");
            ps.println("<X3D profile='Immersive' showLog='true' showStats='true' version='3.0' height='600px' width='600px' y='0px' x='0px'>");
            // had to turn of isStaticHierarchy
//            ps.println("<Scene isStaticHierarchy=\"true\" sortTrans=\"false\" doPickPass=\"false\" frustumCulling=\"false\">");
            ps.println("<Scene sortTrans=\"false\" doPickPass=\"false\" frustumCulling=\"false\">");
//            ps.println("<Scene>");
            ps.println("<Background skyColor=\"1 1 1\" />");

            if (pos != null) {
                ps.println("<Viewpoint position='" + pos[0] + " " + pos[1] + " " + pos[2] + "' />");
            }
            for(int i=0; i < filename.length; i++) {
                if (filename[i] != null) {
                    ps.println("<Inline url='" + filename[i] + "' />");
                }
            }
            ps.println("</Scene>");
            ps.println("</X3D>");

//            ps.println("<script type='text/javascript' src='http://www.x3dom.org/x3dom/release/x3dom.js'></script>");
            ps.println("<script type='text/javascript' src='x3dom/x3dom.js'></script>");

            ps.println("</p>");
            ps.println("</body></html>");
        } finally {
            bos.flush();
            bos.close();
            fos.close();
        }

        Desktop.getDesktop().browse(f.toURI());
    }

}
