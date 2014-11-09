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
import abfab3d.grid.MaterialMaker;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import org.apache.commons.io.IOUtils;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.*;
import org.web3d.vrml.sav.BinaryContentHandler;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static abfab3d.util.Output.printf;

/**
 * Writes a grid out in the sts format.
 *
 * <model>
 * <parts>
 *    <part file="part0001.stl" materialId="1" />
 *    <part file="part0002.stl" materialId="2" />
 * </parts>
 * <materials>
 *    <material id="1" urn="urn:stratsys:materials:material1" />
 *    <material id="2" urn="urn:stratsys:materials:material2" />
 * </materials>
 * <finishes>
 *    <finish urn="urn:shapeways:finish:Polished" />
 * </finishes>
 * </model>
 * @author Alan Hudson
 */
public class STSWriter {
    public static double errorFactorDefault = 0.1;
    public static double smoothingWidthDefault = 0.5;
    public static double minimumVolumeDefault = 0;
    public static int maxPartsDefault = Integer.MAX_VALUE;

    private double smoothingWidth = smoothingWidthDefault;
    private double errorFactor = errorFactorDefault;
    private int maxPartsCount = maxPartsDefault;
    private double minPartVolume = minimumVolumeDefault;
    private int threadCount;
    private String format = "stl";

    private static HashSet<String> SUPPORTED_FORMATS;

    static {
        SUPPORTED_FORMATS = new HashSet<String>(4);
        SUPPORTED_FORMATS.add("x3d");
        SUPPORTED_FORMATS.add("x3dv");
        SUPPORTED_FORMATS.add("x3db");
        SUPPORTED_FORMATS.add("stl");
    }

    public void setOutputFormat(String fileEnding) {
        if (!SUPPORTED_FORMATS.contains(fileEnding)) throw new IllegalArgumentException("Unsupported triangle format: " + fileEnding);

        this.format = fileEnding;
    }

    /**
     * Writes a grid out to an svx file
     * @param grid
     * @param file
     */
    public void write(AttributeGrid grid, MaterialMaker[] makers, String[] finish, String file) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;

        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            zos = new ZipOutputStream(bos);

            write(grid,makers,finish,zos);
        } catch(IOException ioe) {

            ioe.printStackTrace();
            
        } finally {
            IOUtils.closeQuietly(zos);
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
        }

    }

    /**
     * Writes a grid out to an svx file
     * @param grid
     * @param os
     */
    public void write(AttributeGrid grid, MaterialMaker[] makers, String[] finish, ZipOutputStream os) {
        try {

            ZipEntry zentry = new ZipEntry("manifest.xml");
            os.putNextEntry(zentry);
            writeManifest(grid, makers, finish, os);
            os.closeEntry();

            double maxDecimationError = errorFactor * grid.getVoxelSize() * grid.getVoxelSize();

            int len = makers.length;

            for(int i=0; i < len; i++) {
                ZipEntry ze = new ZipEntry("part" + i + "." + format);
                ((ZipOutputStream)os).putNextEntry(ze);

                MeshMakerMT meshmaker = new MeshMakerMT();
                meshmaker.setBlockSize(30);
                meshmaker.setThreadCount(threadCount);
                meshmaker.setSmoothingWidth(smoothingWidth);
                meshmaker.setMaxDecimationError(maxDecimationError);
                meshmaker.setMaxDecimationCount(10);
                meshmaker.setMaxAttributeValue(255);
                meshmaker.setDensityMaker(makers[i].getDensityMaker());

                IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
                meshmaker.makeMesh(grid, its);

                System.out.println("Vertices: " + its.getVertexCount() + " faces: " + its.getFaceCount());

                WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

                if (minPartVolume > 0 || maxPartsCount < Integer.MAX_VALUE) {
                    ShellResults sr = GridSaver.getLargestShells(mesh, maxPartsCount, minPartVolume);
                    mesh = sr.getLargestShell();
                    int regions_removed = sr.getShellsRemoved();
                    System.out.println("Regions removed: " + regions_removed);
                }

                if (format.equals("x3d") || format.equals("x3dv") || format.equals("x3db")) {

                    double[] bounds_min = new double[3];
                    double[] bounds_max = new double[3];

                    grid.getGridBounds(bounds_min, bounds_max);
                    double max_axis = Math.max(bounds_max[0] - bounds_min[0], bounds_max[1] - bounds_min[1]);
                    max_axis = Math.max(max_axis, bounds_max[2] - bounds_min[2]);

                    double z = 2 * max_axis / Math.tan(Math.PI / 4);
                    float[] pos = new float[]{0, 0, (float) z};

                    BinaryContentHandler x3dWriter = createX3DWriter(os);

                    HashMap<String,Object> x3dParams = new HashMap<String,Object>();
                    GridSaver.writeMesh(mesh, 10, x3dWriter, x3dParams, true);
                    x3dWriter.endDocument();
                } else if (format.equals("stl")) {
                    STLWriter stl = new STLWriter(os, mesh.getTriangleCount());
                    mesh.getTriangles(stl);
                }

                ((ZipOutputStream)os).closeEntry();
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void writeManifest(AttributeGrid grid, MaterialMaker[] makers, String[] finish, OutputStream os) {

        PrintStream ps = new PrintStream(os);

        printf(ps,"<?xml version=\"1.0\"?>\n");
        printf(ps,"<model>\n");
        printf(ps,"<parts>\n");
        int len = makers.length;

        for(int i=0; i < len; i++) {
            String fname = "part" + i + ".stl";
            // TODO: Do we want to dedup materialURN's?
            printf(ps,"   <part file=\"%s\" material=\"%s\" />\n",fname,makers[i].getMaterialURN());
        }
        printf(ps,"</parts>\n");
        printf(ps,"<finishes>\n");
        for(String f : finish) {
            printf(ps,"   <finish urn=\"%s\" />\n",f);
        }
        printf(ps,"</finishes>\n");
        printf(ps,"</model>\n");

        ps.flush();
    }

    private BinaryContentHandler createX3DWriter(OutputStream os) {
        BinaryContentHandler x3dWriter = null;

        ErrorReporter console = new PlainTextErrorReporter();

        int sigDigits = 6; // TODO: was -1 but likely needed

        if (format.equals("x3db")) {
            x3dWriter = new X3DBinaryRetainedDirectExporter(os,
                    3, 0, console,
                    X3DBinarySerializer.METHOD_FASTEST_PARSING,
                    0.001f, true);
        } else if (format.equals("x3dv")) {
            if (sigDigits > -1) {
                x3dWriter = new X3DClassicRetainedExporter(os, 3, 0, console, sigDigits);
            } else {
                x3dWriter = new X3DClassicRetainedExporter(os, 3, 0, console);
            }
        } else if (format.equals("x3d")) {
            if (sigDigits > -1) {
                x3dWriter = new X3DXMLRetainedExporter(os, 3, 0, console, sigDigits);
            } else {
                x3dWriter = new X3DXMLRetainedExporter(os, 3, 0, console);
            }
        } else {
            throw new IllegalArgumentException("Unhandled file format: " + format);
        }

        x3dWriter.startDocument("", "", "utf8", "#X3D", "V3.0", "");
        x3dWriter.profileDecl("Immersive");
        x3dWriter.startNode("NavigationInfo", null);
        x3dWriter.startField("avatarSize");
        x3dWriter.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
        x3dWriter.endNode(); // NavigationInfo

        return x3dWriter;
    }

}