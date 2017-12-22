/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
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

import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.export.X3DClassicRetainedExporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;


import abfab3d.core.TriangleCollector;
import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.Vec;
import abfab3d.util.PointSetArray;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;


import static abfab3d.core.Output.printf;


/**
   class to write collection of (textured) triangles to X3D file 

   triangles are written via addTri() method of TriangleCollector interface 
   
   it is important to call close(). 
   It writes total triangle count to the file header. 

   @author Vladimir Bulatov
 */
public class X3DWriter implements TriangleCollector, AttributedTriangleCollector {

    static final boolean DEBUG = true;

    static final String DEFAULT_TEX_NAME = "texture.png";
    int m_triCount = 0;
    String m_path = null;
    String m_texFileName = DEFAULT_TEX_NAME;
    boolean m_writeTexturedMesh = false;

    private static final float[] m_color = new float[] {0.5f,0.5f,0.5f};

    PointSetArray m_points = null;
    PointSetArray m_texPoints = null;

    /**

       constructor to write to specified file
       
     */
    public X3DWriter(String filePath) throws IOException {

        m_path = filePath;
        m_points = new PointSetArray();

    }

    public X3DWriter(String filePath, String texFileName) throws IOException {
        m_writeTexturedMesh = true;
        m_texFileName = texFileName;
        m_path = filePath;
        m_points = new PointSetArray();
        m_texPoints = new PointSetArray();
        

    }

    /**
       save textured mesh 
     */
    public void setWriteTexturedMesh(boolean value){
        m_writeTexturedMesh = value;
    }

    /**
       does the actual writing 
     */
    public void close() throws IOException{
        if(DEBUG) {
            printf("closing export to %s\n", m_path);
            printf("triCount: %d\n", m_triCount);
            printf("pointsCount: %d\n", m_points.size());            
        }        
        
        write(getFormat(m_path));

    }


    /**
       method of interface TriangleCollector
     */
    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2 ){

        // each point is independent (not efficient)
        m_points.addPoint(v0.x,v0.y,v0.z);
        m_points.addPoint(v1.x,v1.y,v1.z);
        m_points.addPoint(v2.x,v2.y,v2.z);

        m_triCount++;
        
        return true;
        
    }

    /**
       adds attributed (textured) triangle 
     */
    public boolean addAttTri(Vec v0,Vec v1,Vec v2){
       
        // each point is independent (not efficient)
        m_points.addPoint(v0.v[0],v0.v[1],v0.v[2]);
        m_points.addPoint(v1.v[0],v1.v[1],v1.v[2]);
        m_points.addPoint(v2.v[0],v2.v[1],v2.v[2]);

        m_texPoints.addPoint(v0.v[3],v0.v[4],0);
        m_texPoints.addPoint(v1.v[3],v1.v[4],0);
        m_texPoints.addPoint(v2.v[3],v2.v[4],0);

        m_triCount++;
        
        return true;

    }


    public void write(String format)  throws IOException {
        
        int pcount = m_points.size();
        float coord[] = new float[pcount*3];
        int coordInd[] = new int[m_triCount*3];
        Vector3d pnt = new Vector3d();

        for(int i = 0; i < pcount; i++){
            m_points.getPoint(i,pnt);
            coord[i*3] = (float)pnt.x;
            coord[i*3+1] = (float)pnt.y;
            coord[i*3+2] = (float)pnt.z;
        }

        for(int i = 0; i < m_triCount*3; i++){
            coordInd[i] = i;
        }
        float texCoord[] = null;
        int texCoordInd[] = null;

        if(m_writeTexturedMesh){
            
            texCoord = new float[pcount*2];
            texCoordInd = new int[m_triCount*3];
            for(int i = 0; i < pcount; i++){
                m_texPoints.getPoint(i,pnt);
                texCoord[i*2] = (float)pnt.x;
                texCoord[i*2+1] = (float)pnt.y;            
            }
            for(int i = 0; i < m_triCount*3; i++){
                texCoordInd[i] = i;
            }
        }

        FileOutputStream fos = new FileOutputStream(m_path);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        BinaryContentHandler writer = createX3DWriter(format, bos);
        if(m_writeTexturedMesh) 
            writeTexturedMeshX3D(coord, coordInd, texCoord, texCoordInd, m_texFileName, writer);
        else 
            writeMeshX3D(coord, coordInd, writer);

        
    }

    public void writeMeshX3D(float fcoord[], int coordIndex[], BinaryContentHandler writer) throws IOException {
        
        coordIndex = insertMinusOne(coordIndex);

        writer.startNode("Shape", null);

        writer.startField("geometry");
        writer.startNode("IndexedFaceSet", null);
        writer.startField("coordIndex");        
        writer.fieldValue(coordIndex, coordIndex.length);

        writer.startField("coord");
        writer.startNode("Coordinate", null);
        writer.startField("point");
        writer.fieldValue(fcoord, fcoord.length);
        writer.endNode();   // Coord

        writer.endNode();   // IndexedFaceSet

        writer.startField("appearance");
        writer.startNode("Appearance", null);
        writer.startField("material");
        writer.startNode("Material",null);
        writer.startField("specularColor");
        writer.fieldValue(m_color, 3);
        writer.endNode();   // Material
        
        writer.endNode();   // Appearance
        
        writer.endNode();   // Shape
        
        writer.endDocument();
    } // writeMeshX3D

    /**
       writes textured triangles into a X3D file.
       texture file is already saved
    */
    public void writeTexturedMeshX3D(float fcoord[], int coordIndex[], float ftexCoord[], int texCoordIndex[],String texFileName,
                                     BinaryContentHandler writer) throws IOException {
        
        texCoordIndex = insertMinusOne(texCoordIndex);
        coordIndex = insertMinusOne(coordIndex);

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
        writer.fieldValue(m_color, 3);
        writer.endNode();   // Material
        writer.startField("texture");
        writer.startNode("ImageTexture", null);
        writer.startField("url");
        writer.fieldValue(new String[]{texFileName}, 1);
        
        writer.endNode();   //ImageTexture
        
        writer.endNode();   // Appearance
        
        writer.endNode();   // Shape
        
        writer.endDocument();
    } // writeTexturedMeshX3D


    private BinaryContentHandler createX3DWriter(String format, OutputStream os) {

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

        //x3dWriter.startDocument("", "", "utf8", "#X3D", "V3.0", "");
        x3dWriter.startDocument("", "", "utf8", "#VRML", "V2.0", "");
        //x3dWriter.profileDecl("Immersive");
        //x3dWriter.startNode("NavigationInfo", null);
        //x3dWriter.startField("avatarSize");
        //x3dWriter.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
        //x3dWriter.endNode(); // NavigationInfo

        return x3dWriter;
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


    static public String getFormat(String path){
        int ind = path.lastIndexOf('.');
        if(ind >= 0) return path.substring(ind+1);
        else return "unknown";
        
    }

} // class STLWriter
 
