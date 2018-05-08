/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2011
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.io.output;

import javax.vecmath.Vector2d;

import org.web3d.vrml.sav.BinaryContentHandler;

import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.export.X3DClassicRetainedExporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;

import org.web3d.util.ErrorReporter;


import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataDesc;
import abfab3d.core.Grid;
import abfab3d.core.GridDataChannel;
import abfab3d.grid.DensityMaker;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.DensityMakerFromDensityChannel;
import abfab3d.grid.DensityMakerFromDistanceChannel;

import abfab3d.grid.util.ExecutionStoppedException;

import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.ShellFinder;
import abfab3d.mesh.LaplasianSmooth;
import abfab3d.mesh.AreaCalculator;
import abfab3d.mesh.MeshDecimator;

import abfab3d.util.FileUtil;
import abfab3d.core.Bounds;
import abfab3d.core.Units;
import abfab3d.core.LongConverter;
import abfab3d.util.DefaultLongConverter;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static abfab3d.core.MathUtil.extendBounds;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.CM3;


/**
 * Common code for saving grids.
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class GridSaver {


    static final boolean DEBUG = true;

    protected double m_meshErrorFactor = 0.1;
    protected double m_meshSmoothingWidth = 0.2;

    final static public double VOLUME_UNDEFINED = -Double.MAX_VALUE;
    final static public int SHELLS_COUNT_UNDEFINED = Integer.MAX_VALUE;

    /** Skipp shell removal entirely if shell count is less than or equal to this value */
    int m_minShellCount = 0;

    int m_maxShellsCount = SHELLS_COUNT_UNDEFINED;
    double m_minShellVolume = VOLUME_UNDEFINED;

    int m_maxThreads = 0;
    int m_maxTrianglesCount = 2000000;
    int m_maxDecimationCount = 10;
    int m_svr = 255;

    double m_isosurfaceValue;
    boolean m_writeTexturedMesh = false;
    int m_decimalDigits = -1; // numer of decimal digits to use for ascii output 
    double m_texPixelSize = 0.75;
    // extension of textured triangles 
    double m_texTriExt = 1.5; 
    // gap between packed triangles
    double m_texTriGap = 1.5;

    public static final String EXT_X3DB = ".x3db";// binary
    public static final String EXT_X3DV = ".x3dv";  // classic
    public static final String EXT_X3D = ".x3d"; // XML
    public static final String EXT_STL = ".stl"; // STL
    public static final String EXT_SVX = ".svx"; // SVX

    public static final int TYPE_UNDEFINED = -1;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_STL = 1;
    public static final int TYPE_X3D = 2;
    public static final int TYPE_X3DB = 3;
    public static final int TYPE_X3DV = 4;
    public static final int TYPE_SVX = 5;


    float m_avatarSize[] = new float[]{0.01f, 1.6f, 0.75f};// size of avatar for x3d output
    private static final float[] GRAY_COLOR = new float[] {0.25f,0.25f,0.25f};
    int m_savingType = TYPE_UNDEFINED;

    String m_texType = "png";

    /**
       
     */
    public GridSaver() {

    }

    /**
       set value of isosurface 
     */
    public void setSurfaceLevel(double value) {
        m_isosurfaceValue = value;
    }

    /**
       sets size of texture pixels relative to grid voxel size
       default value 0.5 
     */
    public void setTexPixelSize(double value) {
        m_texPixelSize = value;
    }

    /**
       set textured triangles extension width (in texture pixels). Default value is 1.5
     */
    public void setTexTriExt(double value) {
        m_texTriExt = value;
    }

    /**
       set textured triangles gap width (in texture pixels). Default value is 3
     */
    public void setTexTriGap(double value) {
        m_texTriGap = value;
    }


    /**
       force writer to save textured mesh (if supported by format (X3D, X3DB or X3DV)
     */
    public void setWriteTexturedMesh(boolean value){
        m_writeTexturedMesh = value;
    }

    public void setMaxTrianglesCount(int value) {
        m_maxTrianglesCount = value;
    }

    public void setMeshErrorFactor(double value) {
        m_meshErrorFactor = value;
    }

    public void setMeshSmoothingWidth(double value) {
        m_meshSmoothingWidth = value;
    }

    public void setMaxShellsCount(int value) {
        m_maxShellsCount = value;
    }

    public void setMinShellCount(int value) {
        m_minShellCount = value;
    }

    public void setMaxThreads(int value) {
        m_maxThreads = value;
    }

    public void setMinShellVolume(double value) {
        m_minShellVolume = value;
    }

    public static int getOutputType(String fname) {

        fname = fname.toLowerCase();
        
        if (fname.endsWith(EXT_STL)) return TYPE_STL;
        if (fname.endsWith(EXT_SVX)) return TYPE_SVX;
        if (fname.endsWith(EXT_X3D)) return TYPE_X3D;
        if (fname.endsWith(EXT_X3DV)) return TYPE_X3DV;
        if (fname.endsWith(EXT_X3DB)) return TYPE_X3DB;
        return TYPE_UNKNOWN;
    }


    /**
       writes grid to the given file in various formats
     */
    public void write(AttributeGrid grid, String outFile) throws IOException {

        // Write output to a file
        int type = getOutputType(outFile);
        switch (type) {
        default:
            throw new RuntimeException(fmt("unknow output file type: '%s'", outFile));
        case TYPE_STL: 
        case TYPE_X3D:
        case TYPE_X3DV:
        case TYPE_X3DB: 
            writeAsMesh(grid, outFile);
            break;
        case TYPE_SVX: 
            SVXWriter writer = new SVXWriter();
            writer.write(grid, outFile);
        }
    }

    public WingedEdgeTriangleMesh writeAsMesh(AttributeGrid grid, String outFile) throws IOException {

        WingedEdgeTriangleMesh mesh = null;

        // Write output to a file
        int type = getOutputType(outFile);
        switch (type) {
            default:
                throw new RuntimeException(fmt("unknow output file type: '%s'", outFile));
            case TYPE_STL: {
                mesh = getMesh(grid);
                STLWriter stl = new STLWriter(outFile);
                String OS = System.getProperty("os.name").toLowerCase();

                if (OS.indexOf("mac") != -1) {
                     stl.setGenerateNormals(true);
                }
                mesh.getTriangles(stl);
                stl.close();
            }
            break;
            case TYPE_X3D:
            case TYPE_X3DV:
            case TYPE_X3DB: 
                {
                    mesh = getMesh(grid);
                    if(m_writeTexturedMesh)
                        writeTexturedMesh(mesh, grid, makeDefaultColorMaker(grid),outFile);
                    else 
                        writeMesh(mesh, outFile);
                }
            break;
        }

        return mesh;
    }

    public void write(AttributeGrid grid, OutputStream os, int type) throws IOException {

        // TODO: Handle other file types
        WingedEdgeTriangleMesh mesh = getMesh(grid);

        //if (m_minShellVolume > 0 || m_maxShellsCount < SHELLS_COUNT_UNDEFINED) {
           //if(DEBUG)printf("min shell Volume: %e  max shellCount: %d\n", m_minShellVolume, m_maxShellsCount);
        //}
        //if (m_minShellVolume != VOLUME_UNDEFINED || m_maxShellsCount != SHELLS_COUNT_UNDEFINED) {
        
        //    ShellResults sr = GridSaver.getLargestShells(mesh, m_maxShellsCount, m_minShellVolume, m_minShellCount);
        //    mesh = sr.getLargestShell();
        //    int regions_removed = sr.getShellsRemoved();
        //    if (DEBUG) printf("maxShells: %d minVol: %4.2f shells removed: %d\n", m_maxShellsCount, m_minShellVolume, regions_removed);
        //}
    
        switch (type) {
            case TYPE_STL:
                STLWriter stl = new STLWriter(os, mesh.getTriangleCount());
                String OS = System.getProperty("os.name").toLowerCase();

                if (OS.indexOf("mac") != -1) {
                    stl.setGenerateNormals(true);
                }

                mesh.getTriangles(stl);
                stl.close();
                break;
            case TYPE_X3D:
                if (m_writeTexturedMesh)
                    writeTexturedMesh(mesh, grid, makeDefaultColorMaker(grid), os,"x3d");
                else
                    writeMesh(mesh, os, "x3d");
                break;
            case TYPE_X3DV:
                if (m_writeTexturedMesh)
                    writeTexturedMesh(mesh, grid, makeDefaultColorMaker(grid), os,"x3dv");
                else
                    writeMesh(mesh, os, "x3dv");
                break;
            case TYPE_X3DB:
                if (m_writeTexturedMesh)
                    writeTexturedMesh(mesh, grid, makeDefaultColorMaker(grid), os,"x3db");
                else
                    writeMesh(mesh, os, "x3db");
                break;
            default:
                throw new IllegalArgumentException("Unhandled type: " + type);
        }
    }


    /**
       extracts mesh from grid and writes into 3d file with texture
     */
    public void writeAsTexturedMesh(AttributeGrid grid, LongConverter colorMaker, String outFile) throws IOException{
        WingedEdgeTriangleMesh mesh = getMesh(grid);
        writeTexturedMesh(mesh, grid, colorMaker, outFile);
    }

    /**
       writes mesh into 3d file with texture
     */
    public void writeTexturedMesh(WingedEdgeTriangleMesh mesh, AttributeGrid grid, LongConverter colorMaker, String outFile) throws IOException{

        if(DEBUG)printf("writeTexturedMesh()\n");
        
        double vs = grid.getVoxelSize();

        String baseDir = FileUtil.getFileDir(outFile);
        String fileName = FileUtil.getFileName(outFile);        
        String texFileName = fileName + "." + m_texType;
        String texFilePath = baseDir + "/" + texFileName;
        if(DEBUG){
            printf("baseDir:%s\n",baseDir);
            printf("fileName:%s\n",fileName);
            printf("texFileName:%s\n",texFileName);
            printf("texFilePath:%s\n",texFilePath);
        }
        // TriangleProducer mesh = getMesh(grid);
               
        TrianglePacker tp = new TrianglePacker();
        tp.setGap(m_texTriGap);
        tp.setTexturePixelSize(vs*m_texPixelSize);

        mesh.getTriangles(tp);
   
        int rc = tp.getTriCount();
        if(DEBUG)printf("tripacker count: %d\n", tp.getTriCount());        
        tp.packTriangles();
        
        Vector2d area = tp.getPackedSize();

        if(DEBUG)printf("texture packedSize: [%7.2f x %7.2f] \n", area.x, area.y); 
               
        int imgWidth = (int)(area.x+2*m_texTriGap);
        int imgHeight = (int)(area.y+2*m_texTriGap);
                
        Bounds texBounds = new Bounds(0, imgWidth, 0, 1, 0, imgHeight);
        AttributeGrid texGrid = new ArrayAttributeGridInt(texBounds, 1., 1.);
        texGrid.setGridBounds(texBounds);
        texGrid.setDataDesc(new GridDataDesc(new GridDataChannel(GridDataChannel.COLOR, "color", 24, 0)));
        
        tp.renderTexturedTriangles(grid, colorMaker, texGrid, m_texTriExt);

        // write single slice 
        SlicesWriter sw = new SlicesWriter();
        sw.setImageFileType(m_texType);
        sw.writeSlices(texGrid, texFilePath, 0, 0, 1, SlicesWriter.AXIS_Y, 24, new DefaultLongConverter());

        double coord[] = tp.getCoord();
        int coordIndex[] = tp.getCoordIndex();
        double texCoord[] = tp.getTexCoord();
        int texCoordIndex[] = tp.getTexCoordIndex();
        for(int k = 0; k < texCoord.length; k += 2){
            texCoord[k] /= imgWidth;
            texCoord[k+1] = (imgHeight - texCoord[k+1])/imgHeight;
        }
        
        writeTexturedX3D(coord, coordIndex, texCoord, texCoordIndex, outFile, texFileName);
        
    }  //writeTexturedMesh()

    /**
     writes mesh into 3d file with texture
     */
    public void writeTexturedMesh(WingedEdgeTriangleMesh mesh, AttributeGrid grid, LongConverter colorMaker, OutputStream zos, String encoding) throws IOException{

        if(DEBUG)printf("writeTexturedMesh()\n");

        double vs = grid.getVoxelSize();

        String fileName = "texture0";
        String texFileName = fileName + "." + m_texType;
        String texFilePath = texFileName;
        if(DEBUG){
            printf("fileName:%s\n",fileName);
            printf("texFileName:%s\n",texFileName);
            printf("texFilePath:%s\n",texFilePath);
        }

        TrianglePacker tp = new TrianglePacker();
        tp.setGap(m_texTriGap);
        tp.setTexturePixelSize(vs*m_texPixelSize);

        mesh.getTriangles(tp);

        int rc = tp.getTriCount();
        if(DEBUG)printf("tripacker count: %d\n", tp.getTriCount());
        tp.packTriangles();

        Vector2d area = tp.getPackedSize();

        if(DEBUG)printf("texture packedSize: [%7.2f x %7.2f] \n", area.x, area.y);

        int imgWidth = (int)(area.x+2*m_texTriGap);
        int imgHeight = (int)(area.y+2*m_texTriGap);

        Bounds texBounds = new Bounds(0, imgWidth, 0, 1, 0, imgHeight);
        AttributeGrid texGrid = new ArrayAttributeGridInt(texBounds, 1., 1.);
        texGrid.setGridBounds(texBounds);
        texGrid.setDataDesc(new GridDataDesc(new GridDataChannel(GridDataChannel.COLOR, "color", 24, 0)));

        tp.renderTexturedTriangles(grid, colorMaker, texGrid, m_texTriExt);

        // write single slice
        SlicesWriter sw = new SlicesWriter();
        String template ="texture%d." + m_texType;
        sw.writeSlices(texGrid, zos, template, 0, 0, 1, SlicesWriter.AXIS_Y, 24, new DefaultLongConverter());

        double coord[] = tp.getCoord();
        int coordIndex[] = tp.getCoordIndex();
        double texCoord[] = tp.getTexCoord();
        int texCoordIndex[] = tp.getTexCoordIndex();
        for(int k = 0; k < texCoord.length; k += 2){
            texCoord[k] /= imgWidth;
            texCoord[k+1] = (imgHeight - texCoord[k+1])/imgHeight;
        }

        writeTexturedX3D(coord, coordIndex, texCoord, texCoordIndex, zos, "main." + encoding ,texFileName);

    }

    /**
     writes textured triangles into a X3D file.
     texture file is already saved
     */
    public void writeTexturedX3D(double coord[], int coordIndex[], double texCoord[], int texCoordIndex[],
                                 OutputStream os, String fileName, String texFileName) throws IOException {


        if (os instanceof ZipOutputStream) {
            if(DEBUG)printf("Creating zipentry: %s\n",fileName);
            ZipEntry ze = new ZipEntry(fileName);
            ((ZipOutputStream)os).putNextEntry(ze);
        }

        int outType = getOutputType(fileName);
        BinaryContentHandler writer = null;
        ErrorReporter console = new PlainTextErrorReporter();

        switch(outType){
            case TYPE_X3DB:
                writer = new X3DBinaryRetainedDirectExporter(os,3, 0, console,X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);
                break;
            case TYPE_X3DV:
                if (m_decimalDigits > -1) writer = new X3DClassicRetainedExporter(os, 3, 0, console, m_decimalDigits);
                else                      writer = new X3DClassicRetainedExporter(os, 3, 0, console);
                break;
            case TYPE_X3D:
                if (m_decimalDigits > -1) writer = new X3DXMLRetainedExporter(os, 3, 0, console, m_decimalDigits);
                else                writer = new X3DXMLRetainedExporter(os, 3, 0, console);
                break;
            default:
                throw new IllegalArgumentException("Unhandled file format: '" + fileName + "'");
        }

        writeTexturedX3D(coord,coordIndex,texCoord,texCoordIndex,writer,texFileName);

        if (os instanceof ZipOutputStream) {
            ((ZipOutputStream)os).closeEntry();
        }
    }

    /**
     writes textured triangles into a X3D file.
     texture file is already saved
     */
    public void writeTexturedX3D(double coord[], int coordIndex[], double texCoord[], int texCoordIndex[], String fileName, String texFileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ErrorReporter console = new PlainTextErrorReporter();



        int outType = getOutputType(fileName);
        BinaryContentHandler writer = null;

        switch(outType){
            case TYPE_X3DB:
                writer = new X3DBinaryRetainedDirectExporter(bos,3, 0, console,X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);
                break;
            case TYPE_X3DV:
                if (m_decimalDigits > -1) writer = new X3DClassicRetainedExporter(bos, 3, 0, console, m_decimalDigits);
                else                      writer = new X3DClassicRetainedExporter(bos, 3, 0, console);
                break;
            case TYPE_X3D:
                if (m_decimalDigits > -1) writer = new X3DXMLRetainedExporter(bos, 3, 0, console, m_decimalDigits);
                else                writer = new X3DXMLRetainedExporter(bos, 3, 0, console);
                break;
            default:
                throw new IllegalArgumentException("Unhandled file format: '" + fileName + "'");
        }

        writeTexturedX3D(coord,coordIndex,texCoord,texCoordIndex,writer,texFileName);
    }

    /**
       writes textured triangles into a X3D file.
       texture file is already saved
    */
    public void writeTexturedX3D(double coord[], int coordIndex[], double texCoord[], int texCoordIndex[],
                                 BinaryContentHandler writer,String texFileName) throws IOException {
        
        float fcoord[] = getFloatArray(coord);
        float ftexCoord[] = getFloatArray(texCoord);
        texCoordIndex = insertMinusOne(texCoordIndex);
        coordIndex = insertMinusOne(coordIndex);

        writer.startDocument("", "", "utf8", "#X3D", "V3.0", "");
        writer.profileDecl("Immersive");
        writer.startNode("NavigationInfo", null);
        writer.startField("avatarSize");
        writer.fieldValue(m_avatarSize, 3);
        writer.endNode(); // NavigationInfo
        // 

        writer.startNode("Shape", null);

        writer.startField("geometry");
        writer.startNode("IndexedFaceSet", null);
        writer.startField("coordIndex");        
        writer.fieldValue(coordIndex, coordIndex.length);
        writer.startField("texCoordIndex");        
        writer.fieldValue(texCoordIndex, texCoordIndex.length);

        writer.startField("coord");
        writer.startNode("Coordinate", null);
        writer.startField("point");
        writer.fieldValue(fcoord, fcoord.length);
        writer.endNode();   // Coord

        writer.startField("texCoord");
        writer.startNode("TextureCoordinate", null);
        writer.startField("point");
        writer.fieldValue(ftexCoord, ftexCoord.length);
        writer.endNode();   // TextureCoord

        writer.endNode();   // IndexedFaceSet

        writer.startField("appearance");
        writer.startNode("Appearance", null);
        writer.startField("material");
        writer.startNode("Material",null);
        writer.startField("specularColor");
        writer.fieldValue(GRAY_COLOR, 3);
        writer.endNode();   // Material
        writer.startField("texture");
        writer.startNode("ImageTexture", null);
        writer.startField("url");
        writer.fieldValue(new String[]{texFileName}, 1);
        
        writer.endNode();   //ImageTexture
        
        writer.endNode();   // Appearance
        
        writer.endNode();   // Shape
        
        writer.endDocument();
        /*
        PrintStream out = new PrintStream(new File(outFile));

        out.printf("		coord Coordinate{\n"+
               "			point[\n");
        for(int k = 0; k < coord.length; k += 3){
            out.printf("\t\t\t%7.5f %7.5f %7.5f\n",coord[k],coord[k+1],coord[k+2]);
        }        
        out.printf("			]\n"+
               "		}\n"+
               "		coordIndex[\n");
        for(int k = 0; k < coordIndex.length; k += 3){
            out.printf("\t\t\t%d %d %d -1\n",coordIndex[k],coordIndex[k+1],coordIndex[k+2]);
        }                
        out.printf("		]\n");
        out.printf("		texCoord TextureCoordinate {\n"+
               "			point [\n");
        for(int k = 0; k < texCoord.length; k += 2){
            out.printf("\t\t\t%7.5f %7.5f\n",texCoord[k], texCoord[k+1]);
        }
        out.printf("			]\n"+
               "		}\n"+
               "		texCoordIndex[\n");
        for(int k = 0; k < texCoordIndex.length; k += 3){
            out.printf("\t\t\t%d %d %d -1\n",texCoordIndex[k],texCoordIndex[k+1],texCoordIndex[k+2]);
        }                        
         out.printf("		]\n");        
        */
    } // writeTexturedX3D

    /**
       makes decimated mesh as isosurface
     */
    public WingedEdgeTriangleMesh getMesh(AttributeGrid grid) {

        double voxelSize = grid.getVoxelSize();

        if (DEBUG) printf("GridSaver.getMesh().  m_meshErrorFactor: %f\n", m_meshErrorFactor);
        double maxDecimationError = m_meshErrorFactor * voxelSize * voxelSize;

        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setThreadCount(m_maxThreads);
        meshmaker.setSmoothingWidth(m_meshSmoothingWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(m_maxDecimationCount);               
        meshmaker.setDensityMaker(getDensityMaker(grid, m_isosurfaceValue));
        meshmaker.setMaxTriangles(m_maxTrianglesCount);
        if(false)printSlice(grid);
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        if (DEBUG) printf("decimated mesh vertices: %d faces: %d\n", its.getVertexCount(), its.getFaceCount());

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        if (m_minShellVolume != VOLUME_UNDEFINED || m_maxShellsCount != SHELLS_COUNT_UNDEFINED) {
            ShellResults sr = GridSaver.getLargestShells(mesh, m_maxShellsCount, m_minShellVolume, m_minShellCount);
            mesh = sr.getLargestShell();
            int regions_removed = sr.getShellsRemoved();
            if (DEBUG) printf("maxShells: %d minVol: %e cm3 shells removed: %d\n", m_maxShellsCount, m_minShellVolume/CM3, regions_removed);
        }
        return mesh;
    }
    
    /**
       debug output 
     */
    void printSlice(AttributeGrid grid){
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        int z = nz/2;
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                long a = grid.getAttribute(x,y,z);
                printf("%2x ", a);
            }            
            printf("\n");
        }
    }


    /**
       makes density maker to convert distance grid into density grid 
       
     */
    DensityMaker getDensityMaker(AttributeGrid grid, double surfaceValue){
        
        GridDataChannel dataChannel = grid.getDataChannel();
        switch(dataChannel.getIType()){
        default: throw new RuntimeException(fmt("unsupported grid data channel type: %s (%d)", dataChannel.getType(),dataChannel.getIType()));
            
        case GridDataChannel.TYPE_DISTANCE:
            return new DensityMakerFromDistanceChannel(dataChannel, surfaceValue, grid.getVoxelSize());
        case GridDataChannel.TYPE_DENSITY:
            return new DensityMakerFromDensityChannel(dataChannel);            
        }
        
    }


    /**
     * Write a grid using the IsoSurfaceMaker to the specified file
     *
     * @param grid
     * @param smoothSteps
     * @param maxCollapseError
     * @throws IOException
     */
    public static void writeIsosurfaceMaker(Grid grid, OutputStream os, String encoding, int smoothSteps, double maxCollapseError) throws IOException {
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double vs = grid.getVoxelSize();


        double gbounds[] = new double[]{-nx * vs / 2, nx * vs / 2, -ny * vs / 2, ny * vs / 2, -nz * vs / 2, nz * vs / 2};
        double ibounds[] = extendBounds(gbounds, -vs / 2);

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, ny, nz);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gbounds, 0), its);
        int[] faces = its.getFaces();
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), faces);

        double centerWeight = 1.0; // any non negative value is OK

        LaplasianSmooth ls = new LaplasianSmooth();

        ls.setCenterWeight(centerWeight);

        long t0 = time();
        if(DEBUG)printf("smoothMesh(%d)\n", smoothSteps);
        t0 = time();
        ls.processMesh(mesh, smoothSteps);
        if(DEBUG)printf("mesh smoohed in %d ms\n", (time() - t0));

        int fcount = faces.length;

        if (maxCollapseError > 0) {
            mesh = decimateMesh(mesh, maxCollapseError);
        }

        float[] pos = new float[]{0, 0, (float) getViewDistance(grid)};

        if (encoding.equals("stl")) {
            // TODO: Need to implement streaming version
            throw new IllegalArgumentException("Unsupported file format: " + encoding);
            //MeshExporter.writeMeshSTL(mesh, os, encoding);
        } else if (encoding.startsWith("x3d")) {
            MeshExporter.writeMesh(mesh, os, encoding, pos);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + encoding);
        }
    }

    /**
     * Write a grid mesh into output
     *
     * @throws IOException
     */
    public static void writeMesh(WingedEdgeTriangleMesh mesh,String filename) throws IOException {
        
        MeshExporter.writeMesh(mesh, filename);
        
        return;
    }

    /**
     * Write a grid mesh into output
     *
     * @throws IOException
     */
    public static void writeMesh(WingedEdgeTriangleMesh mesh,OutputStream zos, String encoding) throws IOException {

        MeshExporter.writeMesh(mesh, zos, encoding, null);

        return;
    }

    /**
     * Write a grid mesh into output
     *
     * @throws IOException
     */
    public static void writeMesh(WingedEdgeTriangleMesh mesh,
                                 double viewDistance,
                                 BinaryContentHandler writer,
                                 Map<String, Object> params,
                                 boolean meshOnly) throws IOException {

        float[] pos = new float[]{0, 0, (float) viewDistance};

        MeshExporter.writeMesh(mesh, writer, params, pos, meshOnly, null);

        return;
    }

    /**
     retuns good viewpoint for given box
     */
    public static double getViewDistance(Grid grid) {

        double bounds[] = new double[6];
        grid.getGridBounds(bounds);

        double sizex = bounds[1] - bounds[0];
        double sizey = bounds[3] - bounds[2];
        double sizez = bounds[5] - bounds[4];

        double max = sizex;
        if (sizey > max) max = sizey;
        if (sizez > max) max = sizez;

        double z = 2 * max / Math.tan(Math.PI / 4);
        return z;

    }

    /**
       returns mesh with largest shell
     */
    /*
    public static ShellResults getLargestShell(WingedEdgeTriangleMesh mesh, int minVolume) {

        ShellFinder shellFinder = new ShellFinder();
        ShellFinder.ShellInfo shells[] = shellFinder.findShells(mesh);
        if(DEBUG)printf("shellsCount: %d\n", shells.length);

        int regions_removed = 0;

        if (shells.length > 1) {

            ShellFinder.ShellInfo maxShell = shells[0];

            for (int i = 0; i < shells.length; i++) {

                printf("shell: %d faces\n", shells[i].faceCount);
                if (shells[i].faceCount > maxShell.faceCount) {
                    maxShell = shells[i];
                }
            }

            for (int i = 0; i < shells.length; i++) {

                if (shells[i] != maxShell) {
                    if (shells[i].faceCount >= minVolume) {
                        regions_removed++;
                    }
                }
            }
            printf("extracting largest shell: %d\n", maxShell.faceCount);
            IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(maxShell.faceCount);
            shellFinder.getShell(mesh, maxShell.startFace, its);
            mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
            return new ShellResults(mesh, regions_removed);

        } else {

            return new ShellResults(mesh, regions_removed);
        }
    }
    */
    /**
     * Returns up to numShells shells that are above the minimum volume.
     *
     * @param mesh The mesh
     * @param maxShellsCount The maximum number of shells
     * @param minVolume The minimum volume
     */
    public static ShellResults getLargestShells(WingedEdgeTriangleMesh mesh, int maxShellsCount, double minVolume) {

        return getLargestShells(mesh, maxShellsCount, minVolume, 0);
        
    }

    /**
     * Returns up to numShells shells that are above the minimum volume.
     * TODO: Commonize getLargestShells methods
     *
     * @param mesh The mesh
     * @param maxShellsCount The maximum number of shells
     * @param minVolume The minimum volume
     */
    public static ShellResults getLargestShells(WingedEdgeTriangleMesh mesh, int maxShellsCount, double minVolume, int minShellCount) {

        ShellFinder shellFinder = new ShellFinder();
        ShellFinder.ShellInfo shells[] = shellFinder.findShells(mesh);
        if(DEBUG)printf("GridSaver.getLargestShells(shells: %d maxShellsCount:%d, minShellCount: %d miVolume: %e cm^3)\n", 
               shells.length, maxShellsCount, minShellCount, minVolume/CM3);

        if (shells.length <= minShellCount) {
            return new ShellResults(mesh, 0);
        }

        return extractShells(mesh, maxShellsCount, minVolume, shellFinder, shells);
    }

    
    public static ShellResults extractShells(WingedEdgeTriangleMesh mesh, int maxShellsCount, double minVolume,
                                             ShellFinder shellFinder, ShellFinder.ShellInfo shells[]) {

        if(DEBUG)printf("GridSaver.extractShells(maxShellsCount: %d, minVolume: %e cm3, shells.lengh:  %d\n",maxShellsCount, minVolume/CM3, shells.length);
        if (shells.length > 5) {
            return extractShellsMT(mesh, maxShellsCount, minVolume, shellFinder, shells);
        } else {
            return extractShellsST(mesh, maxShellsCount, minVolume, shellFinder, shells);
        }
    }


    public static ShellResults extractShellsMT(WingedEdgeTriangleMesh mesh, int maxShellsCount, double minVolume,
                                               ShellFinder shellFinder, ShellFinder.ShellInfo shells[]) { 

        if(DEBUG) printf("GridSaver.extractShellsMT(maxShellsCount:%d, minVolume: %e cm3)\n", maxShellsCount, minVolume);

        //System.out.println("Minimum volume: " + (minVolume / CM3));
        ArrayBlockingQueue<ShellData> saved_shells = new ArrayBlockingQueue<ShellData>(shells.length);
        int face_count = 0;
        int cnt = 0;

        int threads = 8;
        ShellExtracter[] workers = new ShellExtracter[threads];
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        ConcurrentLinkedQueue<ShellFinder.ShellInfo> work = new ConcurrentLinkedQueue();
        for (ShellFinder.ShellInfo shell : shells) {
            work.add(shell);
        }

        for (int i = 0; i < threads; i++) {
            workers[i] = new ShellExtracter(mesh, minVolume, saved_shells, work);
            executor.submit(workers[i]);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int regions_removed = 0;
        for (int i = 0; i < threads; i++) {
            regions_removed += workers[i].getRemoved();
        }

        ArrayList<ShellData> ss = new ArrayList<ShellData>(saved_shells.size());
        ss.addAll(saved_shells);
        Collections.sort(ss, Collections.reverseOrder());

        int shell_cnt = 0;

        for (ShellData sd : ss) {
            face_count += sd.info.faceCount;
            shell_cnt++;
            if (shell_cnt >= maxShellsCount) break;
        }

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(face_count);
        shell_cnt = 0;
        for (ShellData sd : ss) {
            shellFinder.getShell(mesh, sd.info.startFace, its);
            shell_cnt++;
            if (shell_cnt >= maxShellsCount) break;
        }

        if(DEBUG) printf("GridSaver.extractShellsMT() face_count: %d  shell_cnt: %d  removed shells: %d\n", face_count, shell_cnt, (shells.length - shell_cnt));
        mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        return new ShellResults(mesh, shells.length - shell_cnt);
    }


    public static ShellResults extractShellsST(WingedEdgeTriangleMesh mesh, int maxShellsCount, double minVolume,
                                               ShellFinder shellFinder, ShellFinder.ShellInfo shells[]) {
        int regions_removed = 0;

        if(DEBUG)printf("GridSaver.extractShellsST(maxShellsCount:%d, minVolume:%e cm^3)\n", maxShellsCount, (minVolume / CM3));
        ArrayList<ShellData> saved_shells = new ArrayList<ShellData>();
        int face_count = 0;
        int cnt = 0;

        for (int i = 0; i < shells.length; i++) {
            AreaCalculator ac = new AreaCalculator();

            shellFinder.getShell(mesh, shells[i].startFace, ac);

            double volume = ac.getVolume();
            if(DEBUG)printf("   shell %3d faces: %6d (%e cm^3)", i, shells[i].faceCount,(volume / CM3));
            if (volume >= minVolume) {
                if(DEBUG)printf(" Keeping shell\n");
                saved_shells.add(new ShellData(shells[i], volume));
                if (cnt < maxShellsCount) {
                    face_count += shells[i].faceCount;
                }
                cnt++;
            } else {
                if(DEBUG)printf(" Removing shell\n");
                regions_removed++;
            }
        }

        Collections.sort(saved_shells, Collections.reverseOrder());

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(face_count);
        int shell_cnt = 0;
        for (ShellData sd : saved_shells) {
            shellFinder.getShell(mesh, sd.info.startFace, its);
            shell_cnt++;
            if (shell_cnt >= maxShellsCount) break;
        }

        if(DEBUG)printf("GridSaver.extractShellsST() faces count: %d  shells saved: %d  removed: %d\n", face_count, shell_cnt, (shells.length - shell_cnt));
        mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        return new ShellResults(mesh, shells.length - shell_cnt);
    }

    /**
       inserts -1 after each triple of indices 
     */
    public static int[] insertMinusOne(int ind[]){

        int count = ind.length/3;
        int ind4[] = new int[4*count];

        for(int i = 0, j=0, k = 0; i < count; i++){
            ind4[j++] = ind[k++];
            ind4[j++] = ind[k++];
            ind4[j++] = ind[k++];
            ind4[j++] = -1;
        }
        return ind4;
    }

    public static float[] getFloatArray(double d[]){
        float f[] = new float[d.length];
        for(int i = 0; i < d.length; i++){
            f[i] = (float)d[i];
        }
        return f;
    }
    
    public static WingedEdgeTriangleMesh decimateMesh(WingedEdgeTriangleMesh mesh, double maxCollapseError) {

        if(DEBUG)printf("GridSaver.decimateMesh()\n");

        MeshDecimator md = new MeshDecimator();
        md.setMaxCollapseError(maxCollapseError);
        long start_time = System.currentTimeMillis();

        int fcount = mesh.getTriangleCount();
        int target = fcount / 2;
        int current = fcount;
        if(DEBUG)printf("   Original face count: " + fcount);

        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }

            target = mesh.getTriangleCount() / 2;
            if(DEBUG)printf("   Target face count : %d\n", target);
            md.processMesh(mesh, target);

            current = mesh.getFaceCount();
            if(DEBUG)printf("   Current face count: %d \n", current);
            if (current >= target * 1.25) {
                // not worth continuing
                break;
            }
        }
        fcount = current;
        if(DEBUG)printf("   Final face count: %d \n", fcount);
        return mesh;
    }


    public LongConverter makeDefaultColorMaker(AttributeGrid grid){
        return new DefaultColorMaker();
    }

    // temporary hack to get make color from 
    public static class DefaultColorMaker implements LongConverter {

        public long get(long value) {
            // TODO - swaps colors
            return ((value >> 8)& 0xFFFFFF);

        }
    }

    public static class ColorMakerIdentity implements LongConverter {

        public long get(long value) {
            return ((value)& 0xFFFFFF);
        }
    }

    static class ShellExtracter implements Runnable {
        private boolean terminate;
        private WingedEdgeTriangleMesh mesh;
        private double minVolume;
        private BlockingQueue<ShellData> saved;
        private int removed;
        private ConcurrentLinkedQueue<ShellFinder.ShellInfo> shells;
        
        public ShellExtracter(WingedEdgeTriangleMesh mesh, double minVolume, BlockingQueue<ShellData> saved, ConcurrentLinkedQueue<ShellFinder.ShellInfo> shells) {
            this.mesh = mesh;
            this.minVolume = minVolume;
            this.saved = saved;
            this.shells = shells;
        }
        
        public void run() {
            while (!terminate) {
                ShellFinder.ShellInfo shell = shells.poll();
                if (shell == null) break;
                
                
                AreaCalculator ac = new AreaCalculator();
                ShellFinder sf = new ShellFinder();
                sf.getShell(mesh, shell.startFace, ac);

                double volume = ac.getVolume();
                if(DEBUG)printf("  shell sf: %d faces: %d (%e cm^3)", shell.startFace, shell.faceCount,(volume / CM3));

                if (volume >= minVolume) {
                    if(DEBUG)printf(" Keeping shell: %e cm3 \n", volume / CM3);
                    saved.add(new ShellData(shell, volume));
                } else {
                    if(DEBUG)printf(" Removing shell\n ");

                    removed++;
                }
            }
        }
        
        public int getRemoved() {
            return removed;
        }
    }   // static class ShellExtracter 
           
} // class GridSaver

