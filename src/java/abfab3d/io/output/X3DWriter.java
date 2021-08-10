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

import java.util.Vector;

import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.export.X3DClassicRetainedExporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;
import org.web3d.vrml.export.VrmlExporter;


import abfab3d.core.TriangleCollector;
import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.Vec;
import abfab3d.util.PointSetArray;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;


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
    String m_path = null;


    private static final float[] m_specularColor = new float[] {1.f,1.f,1.f};
    private static final float[] m_materialColor = new float[] {0.5f,0.5f,0.5f};

    boolean m_writeMaterial = true;

    Shape m_currentShape;  // current active shape 

    Vector<Shape> m_shapes = new Vector<Shape>(1); // all shapes

    /**

       constructor to write to specified file
       
     */
    public X3DWriter(String filePath) throws IOException {

        this(filePath, 0);

    }

    public X3DWriter(String filePath, int texDimension) throws IOException {

        m_path = filePath;
        addNewShape(texDimension);

    }

    public X3DWriter(String filePath, String texFileName) throws IOException {

        m_path = filePath;
                
        addNewShape(texFileName);
        
    }

    public void setTexFileName(String texFileName){
        m_currentShape.setTexFileName(texFileName);
    }

    public void setTexDimension(int dimension){
        
        m_currentShape.setTexDimension(dimension);

    }

    /**
       save textured mesh 
     */
    public void setWriteTexturedMesh(boolean value){
        m_currentShape.setWriteTexturedMesh(value);
    }

    /**
       save colored mesh 
     */
    public void setWriteColoredMesh(boolean value){
        
        m_currentShape.setWriteColoredMesh(value);

    }

    public void addNewShape(String texFileName){

        Shape shape = new Shape(texFileName); 
        m_shapes.add(shape);
        m_currentShape = shape;

    }
    
    public void addNewShape(int texDimension){

        
        switch(texDimension){
        case 0:
        case 1:
        case 2:
        case 3:
            m_currentShape = new Shape(texDimension); 
            break;
        }
        m_shapes.add(m_currentShape);

    }

    /**
       sets whether or not write material for colorless meshes 
     */
    public void setWriteMaterial(boolean value){

        m_writeMaterial = value;
        
    }

    /**
       does the actual writing 
     */
    public void close() throws IOException{
        if(DEBUG) {
            //printf("closing export to '%s'\n", m_path);
            //for(int s = 0; s < m_shapes.size(); s++){
            //    Shape shape =  m_shapes.get(s);
            //    printf("shape(%d)= %s\n", s, shape.toString());
            //}
        }        
        
        write(getFormat(m_path));

    }


    /**
       method of interface TriangleCollector
     */
    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2 ){
        
        return m_currentShape.addTri(v0, v1, v2);
        
    }

    /**
       adds attributed (textured) triangle 
     */
    public boolean addAttTri(Vec v0,Vec v1,Vec v2){

        return m_currentShape.addAttTri(v0, v1, v2);

    }


    public void write(String format)  throws IOException {
        
        if(DEBUG) printf("X3DWriter.write(%s)\n", format);
        FileOutputStream fos = new FileOutputStream(m_path);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        BinaryContentHandler writer = createX3DWriter(format, bos);

        for(int s = 0; s < m_shapes.size(); s++){

            Shape shape = m_shapes.get(s);

            if(DEBUG) printf(" shape(%d): %s\n", s, shape.toString());
            PointSetArray points = shape.points;
            int triCount = shape.triCount;

            int pcount = points.size();
            float coord[] = new float[pcount*3];
            int coordInd[] = new int[triCount*3];
            Vector3d pnt = new Vector3d();
            
            for(int i = 0; i < pcount; i++){
                points.getPoint(i,pnt);
                coord[i*3] = (float)pnt.x;
                coord[i*3+1] = (float)pnt.y;
                coord[i*3+2] = (float)pnt.z;
            }
            
            for(int i = 0; i < triCount*3; i++){
                coordInd[i] = i;
            }
            float texCoord[] = null;
            int texCoordInd[] = null;
            
            if(shape.writeTexturedMesh || shape.writeColoredMesh){
                PointSetArray texPoints = shape.texPoints;
                int texDimension = shape.texDimension;
                texCoord = new float[pcount*texDimension];
                texCoordInd = new int[triCount*3];
                for(int i = 0; i < pcount; i++){
                    texPoints.getPoint(i,pnt);
                    int ii = texDimension*i;
                    switch(texDimension){
                    case 3: texCoord[ii + 2] = (float)pnt.z; // no break
                    case 2: texCoord[ii + 1] = (float)pnt.y; // no break
                    case 1: texCoord[ii]     = (float)pnt.x; // no break
                    }                    
                }
                for(int i = 0; i < triCount*3; i++){
                    texCoordInd[i] = i;
                }
            }
            
            if(shape.writeColoredMesh) 
                writeColoredShape(coord, coordInd, texCoord, texCoordInd, writer);
            else if(shape.writeTexturedMesh) 
                writeTexturedShape(coord, coordInd, texCoord, texCoordInd, shape.texFileName, writer);
            else 
                writeColorlessShape(coord, coordInd, writer);            
        } // for(int s = 0; s < m_shapes.size(); s++){

        writer.endDocument();

    }

    
    public void writeColorlessShape(float fcoord[], int coordIndex[], BinaryContentHandler writer) throws IOException {
        
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
        if(m_writeMaterial){
            writer.startField("appearance");
            writer.startNode("Appearance", null);
            writer.startField("material");
            writer.startNode("Material",null);
            writer.startField("specularColor");
            writer.fieldValue(m_specularColor, 3);
            writer.endNode();   // Material            
            writer.endNode();   // Appearance
        }

        writer.endNode();   // Shape
        
    } // writeMeshX3D

    /**
       writes textured triangles into a X3D file.
       texture file is already saved
    */
    public void writeTexturedShape(float fcoord[], int coordIndex[], float ftexCoord[], int texCoordIndex[],String texFileName,
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
        if(m_writeMaterial){
            writer.startNode("Material",null);
            writer.startField("specularColor");
            writer.fieldValue(m_specularColor, 3);
            writer.endNode();   // Material
        }
        writer.startField("texture");
        writer.startNode("ImageTexture", null);
        writer.startField("url");
        writer.fieldValue(new String[]{texFileName}, 1);
        
        writer.endNode();   //ImageTexture
        
        writer.endNode();   // Appearance
        
        writer.endNode();   // Shape
        
    } // writeTexturedMeshX3D


    private void writeColoredShape(float fcoord[], int coordIndex[], float vertColors[], int vertColorInd[],
                                     BinaryContentHandler writer) throws IOException {
        
        //texCoordIndex = insertMinusOne(texCoordIndex);
        coordIndex = insertMinusOne(coordIndex);

        writer.startNode("Shape", null);

        writer.startField("geometry");
        writer.startNode("IndexedFaceSet", null);

        writer.startField("coord");
        writer.startNode("Coordinate", null);
        writer.startField("point");
        writer.fieldValue(fcoord, fcoord.length);
        writer.endNode();   // Coord

        writer.startField("coordIndex");        
        writer.fieldValue(coordIndex, coordIndex.length);

        writer.startField("color");
        writer.startNode("Color", null);
        writer.startField("color");
        writer.fieldValue(vertColors, vertColors.length);
        writer.endNode();   // Color

        // writer.startField("colorIndex");
        //writer.fieldValue(vertColorInd, vertColorInd.length);
        writer.startField("colorPerVertex");
        writer.fieldValue(true);


        writer.endNode();   // IndexedFaceSet

        writer.startField("appearance");
        writer.startNode("Appearance", null);
        if(m_writeMaterial){
            writer.startField("material");
            writer.startNode("Material",null);
            writer.startField("specularColor");
            writer.fieldValue(m_specularColor, 3);
            writer.endNode();   // Material
        }
        writer.endNode();   // Appearance
        
        writer.endNode();   // Shape
        
    }


    private BinaryContentHandler createX3DWriter(String format, OutputStream os) {

        BinaryContentHandler x3dWriter = null;

        ErrorReporter console = new PlainTextErrorReporter();

        int sigDigits = 6; // TODO: was -1 but likely needed
        int vers[] = {3,0};
        String versionTag = "V3.0";

        if (format.equals("x3db")) {
            x3dWriter = new X3DBinaryRetainedDirectExporter(os,
                                                            vers[0], vers[1],console,
                                                            X3DBinarySerializer.METHOD_FASTEST_PARSING,
                                                            0.001f, true);
        } else if (format.equals("x3dv")) {
            if (sigDigits > -1) {
                x3dWriter = new X3DClassicRetainedExporter(os, vers[0], vers[1], console, sigDigits);
            } else {
                x3dWriter = new X3DClassicRetainedExporter(os, vers[0], vers[1], console);
            }
        } else if (format.equals("wrl")) {
            
            x3dWriter = new VrmlExporter(os, vers[0], vers[1], console, true);            
                
        } else if (format.equals("x3d")) {
            if (sigDigits > -1) {
                x3dWriter = new X3DXMLRetainedExporter(os, vers[0], vers[1], console, sigDigits);
            } else {
                x3dWriter = new X3DXMLRetainedExporter(os, vers[0], vers[1], console);
            }
        } else {
            throw new IllegalArgumentException("Unhandled file format: " + format);
        }
        
        x3dWriter.startDocument("", "", "utf8", "#X3D", versionTag, fmt("exported by %s", this.getClass().getName()));
        x3dWriter.profileDecl("Immersive");

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

    //
    //  individual shape data 
    //
    static class Shape {

        PointSetArray points = null;
        PointSetArray texPoints = null;
        int texDimension = 2;
        int triCount = 0;
        //    String m_texFileName = DEFAULT_TEX_NAME;
        boolean writeTexturedMesh = false;
        boolean writeColoredMesh = false;
        String texFileName = DEFAULT_TEX_NAME;

        Shape(String texFileName){

            this.points = new PointSetArray();
            this.texPoints = new PointSetArray();
            texDimension = 2;
            this.texFileName = texFileName;
            writeTexturedMesh = true;
        }

        Shape(int texDimension){

            this.points = new PointSetArray();
            this.texDimension = texDimension;
            switch(texDimension){                
            case 1: 
            case 2: 
                this.texPoints = new PointSetArray();
                this.writeTexturedMesh = true;
                break;

            case 3: 
                this.texPoints = new PointSetArray();
                this.writeTexturedMesh = true;
                writeColoredMesh = true;
                break;
            }

        }

        void setTexFileName(String texFileName){
            this.texFileName = texFileName;
        }

        public boolean addAttTri(Vec v0,Vec v1,Vec v2){
            // each point is independent (not efficient)
            points.addPoint(v0.v[0],v0.v[1],v0.v[2]);
            points.addPoint(v1.v[0],v1.v[1],v1.v[2]);
            points.addPoint(v2.v[0],v2.v[1],v2.v[2]);
            switch(texDimension){
            case 1:
                texPoints.addPoint(v0.v[3],0,0);
                texPoints.addPoint(v1.v[3],0,0);
                texPoints.addPoint(v2.v[3],0,0);
                break;
            case 2:
                texPoints.addPoint(v0.v[3],v0.v[4],0);
                texPoints.addPoint(v1.v[3],v1.v[4],0);
                texPoints.addPoint(v2.v[3],v2.v[4],0);
                break;
            case 3: 
                texPoints.addPoint(v0.v[3],v0.v[4],v0.v[5]);
                texPoints.addPoint(v1.v[3],v1.v[4],v1.v[5]);
                texPoints.addPoint(v2.v[3],v2.v[4],v2.v[5]);
                break;
            }
            
            triCount++;
            
            return true;
        }

        public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2 ){
            
            // each point is independent (not efficient)
            points.addPoint(v0.x,v0.y,v0.z);
            points.addPoint(v1.x,v1.y,v1.z);
            points.addPoint(v2.x,v2.y,v2.z);
            
            triCount++;
            
            return true;
            
        } // addTri         

        //
        //
        //
        public void setTexDimension(int dimension){
        
            if(dimension < 0 || dimension > 3) throw new RuntimeException(fmt("illegal texture dimension: %d. May be only [0,1,2,3]", dimension));
            this.texDimension  = dimension;
        }

        /**
           save textured mesh 
        */
        public void setWriteTexturedMesh(boolean value){
            writeTexturedMesh = value;
            texDimension = 2;
        }
        
        /**
           save colored mesh 
        */
        public void setWriteColoredMesh(boolean value){
            writeColoredMesh = value;
            texDimension = 3;
            texPoints = new PointSetArray();
        }        

        public String toString(){
            StringBuffer sb = new StringBuffer();
            sb.append(fmt("Shape(triCount: %d, texDimension: %d, texFileName:'%s', ", triCount, texDimension, texFileName));
            if(points != null) sb.append(fmt("points:%d, ", points.size()));
            else  sb.append("points:null,");
            if(texPoints != null) sb.append(fmt("texPoints:%d, ", texPoints.size()));
            else  sb.append("texPoints:null, ");
            sb.append(fmt("writeTexturedMesh:%b, writeColoredMesh: %b", writeTexturedMesh, writeColoredMesh));        
            sb.append(")");        
            return sb.toString();
        }

    }  // class Shape 

} // class STLWriter
 
