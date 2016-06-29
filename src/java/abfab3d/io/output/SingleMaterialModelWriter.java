package abfab3d.io.output;

import abfab3d.core.AttributeGrid;
import abfab3d.grid.ModelWriter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.util.TriangleMesh;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.util.AbFab3DGlobals;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.*;
import org.web3d.vrml.sav.BinaryContentHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;

/**
 * Writes a model out as a single material.  Caller is responsible for closing the provided output stream.
 *
 * @author Alan Hudson
 */
public class SingleMaterialModelWriter implements ModelWriter {
    public static double errorFactorDefault = 0.1;
    public static double smoothingWidthDefault = 0.5;
    public static double minimumVolumeDefault = 0;
    public static int maxPartsDefault = Integer.MAX_VALUE;

    private String format;
    private OutputStream os;
    private BinaryContentHandler x3dWriter;
    private Map<String,Object> x3dParams;
    private double smoothingWidth = smoothingWidthDefault;
    private double errorFactor = errorFactorDefault;
    private int maxPartsCount = maxPartsDefault;
    private double minPartVolume = minimumVolumeDefault;
    private int threadCount;
    private TriangleMesh mesh;

    private static HashSet<String> SUPPORTED_FORMATS;

    static {
        SUPPORTED_FORMATS = new HashSet<String>(4);
        SUPPORTED_FORMATS.add("x3d");
        SUPPORTED_FORMATS.add("x3dv");
        SUPPORTED_FORMATS.add("x3db");
        SUPPORTED_FORMATS.add("stl");
    }

    @Override
    public void setOutputFormat(String fileEnding) {
        if (!SUPPORTED_FORMATS.contains(fileEnding)) throw new IllegalArgumentException("Unsupported triangle format: " + fileEnding);

        this.format = fileEnding;
    }

    @Override
    public void setOutputStream(OutputStream os) {
        this.os = os;
    }

    /**
     * Set the X3D content handler to use.  For X3D content writes will go through this writer.
     *
     * @param writer
     */
    public void setX3DWriter(BinaryContentHandler writer,Map<String,Object> params) {
        this.x3dWriter = writer;
        this.x3dParams = params;
    }

    @Override
    public void execute(AttributeGrid grid) throws IOException {
        double maxDecimationError = errorFactor * grid.getVoxelSize() * grid.getVoxelSize();

        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(30);
        meshmaker.setThreadCount(threadCount);
        meshmaker.setSmoothingWidth(smoothingWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(10);
        meshmaker.setMaxAttributeValue(255);

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

        this.mesh = mesh;

        if(format.equals("x3d") || format.equals("x3dv") || format.equals("x3db")){

            double[] bounds_min = new double[3];
            double[] bounds_max = new double[3];

            grid.getGridBounds(bounds_min,bounds_max);
            double max_axis = Math.max(bounds_max[0] - bounds_min[0], bounds_max[1] - bounds_min[1]);
            max_axis = Math.max(max_axis, bounds_max[2] - bounds_min[2]);

            double z = 2 * max_axis / Math.tan(Math.PI / 4);
            float[] pos = new float[] {0,0,(float) z};

            if (x3dWriter == null) createX3DWriter();

            GridSaver.writeMesh(mesh, 10,x3dWriter,x3dParams,true);

            // TODO: not certain who should call this yet
            // TODO: and this makes the passed in x3dWriter invalid for future usage
            x3dWriter.endDocument();

        } else if(format.equals("stl")){
            STLWriter stl = new STLWriter(os,mesh.getTriangleCount());
            mesh.getTriangles(stl);
            stl.close();
        }
    }

    /**
     * Get the generated mesh or null if not available
     * @return
     */
    public TriangleMesh getGeneratedMesh() {
        return mesh;
    }

    /**
     * Get a string name for this writer.
     * @return
     */
    public String getStyleName() {
        return "singleMaterialMesh";
    }

    private void createX3DWriter() {
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
    }

    /**
     * Returns a mesh if its been generated, null otherwise
     * @return
     */
    public TriangleMesh getMesh(AttributeGrid grid) throws IOException {

        if (mesh == null) {
            execute(grid);
        }

        return mesh;
    }

    public double getSmoothingWidth() {
        return smoothingWidth;
    }

    public void setSmoothingWidth(double smoothingWidth) {
        this.smoothingWidth = smoothingWidth;
    }

    public double getErrorFactor() {
        return errorFactor;
    }

    public void setErrorFactor(double errorFactor) {
        this.errorFactor = errorFactor;
    }

    public int getMaxPartsCount() {
        return maxPartsCount;
    }

    public void setMaxPartsCount(int maxPartsCount) {
        this.maxPartsCount = maxPartsCount;
    }

    public double getMinPartVolume() {
        return minPartVolume;
    }

    public void setMinPartVolume(double minPartVolume) {
        this.minPartVolume = minPartVolume;
    }

    private void setThreadCount(int count) {
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        threadCount = count;
    }
}
