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

import abfab3d.util.TriangleMesh;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.*;
import org.web3d.vrml.sav.BinaryContentHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Export meshes to varies file formats.
 *
 * @author Alan Hudson
 */
public class MeshExporter {

    /**
     * Write a mesh to an X3D file
     *
     * @param we
     * @param filename
     * @throws IOException
     */
    public static void writeMesh(TriangleMesh we, String filename, Map<String, Object> params) throws IOException {
        writeMesh(we,filename, -1, params);
    }
        /**
        * Write a mesh to an X3D file
        *
        * @param we
        * @param filename
        * @throws IOException
        */
    public static void writeMesh(TriangleMesh we, String filename, int sigDigits, Map<String, Object> params) throws IOException {

        SAVExporter se = new SAVExporter();

        if (params == null) {
            params = new HashMap<String, Object>();
        }

        FileOutputStream fos = null;

        try {
            BinaryContentHandler writer = null;
            fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".") + 1);

            ErrorReporter console = new PlainTextErrorReporter();

            if (encoding.equals("x3db")) {
                writer = new X3DBinaryRetainedDirectExporter(fos,
                        3, 0, console,
                        X3DBinarySerializer.METHOD_FASTEST_PARSING,
                        0.001f, true);
            } else if (encoding.equals("x3dv")) {
                if (sigDigits > -1) {
                    writer = new X3DClassicRetainedExporter(fos, 3, 0, console, sigDigits);
                } else {
                    writer = new X3DClassicRetainedExporter(fos, 3, 0, console);
                }
            } else if (encoding.equals("x3d")) {
                if (sigDigits > -1) {
                    writer = new X3DXMLRetainedExporter(fos, 3, 0, console, sigDigits);
                } else {
                    writer = new X3DXMLRetainedExporter(fos, 3, 0, console);
                }
            } else {
                throw new IllegalArgumentException("Unhandled file format: " + encoding);
            }

            writer.startDocument("", "", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo

            se.outputX3D(we, params, writer, null);
            writer.endDocument();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Write a mesh to an X3D file
     *
     * @param we
     * @param filename
     * @throws IOException
     */
    public static void writeMesh(TriangleMesh we, String filename) throws IOException {
        writeMesh(we, filename, null);
    }

    /**
     * Write a mesh to an X3D file
     *
     * @param we
     * @param fos      The output stream.  Caller is responsible for closing
     * @param encoding The X3D encoding to use.  Supported x3d,x3dv,x3db
     * @throws IOException
     */
    public static void writeMesh(TriangleMesh we, OutputStream fos, String encoding, float[] pos) throws IOException {

        SAVExporter se = new SAVExporter();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(SAVExporter.EXPORT_NORMALS, false);   // Required now for ITS?
        params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.INDEXEDFACESET);   // Required now for ITS?

        BinaryContentHandler writer = null;

        ErrorReporter console = new PlainTextErrorReporter();

        if (encoding.equals("x3db")) {
            writer = new X3DBinaryRetainedDirectExporter(fos,
                    3, 0, console,
                    X3DBinarySerializer.METHOD_FASTEST_PARSING,
                    0.001f, true);
        } else if (encoding.equals("x3dv")) {
            writer = new X3DClassicRetainedExporter(fos, 3, 0, console);
        } else if (encoding.equals("x3d")) {
            writer = new X3DXMLRetainedExporter(fos, 3, 0, console);
            ((X3DXMLRetainedExporter)writer).setPrintDocType(false);
            ((X3DXMLRetainedExporter)writer).setPrintXML(false);
            HashMap<String,String> x3dAtts = new HashMap<String,String>();
            x3dAtts.put("x","0px");
            x3dAtts.put("y","0px");
            x3dAtts.put("width","600px");
            x3dAtts.put("height","600px");
            ((X3DXMLRetainedExporter)writer).setX3DAttributes(x3dAtts);

        } else {
            throw new IllegalArgumentException("Unhandled file format: " + encoding);
        }

        writer.startDocument("", "", "utf8", "#X3D", "V3.0", "");
        writer.profileDecl("Immersive");
        writer.startNode("NavigationInfo", null);
        writer.startField("avatarSize");
        writer.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
        writer.endNode(); // NavigationInfo
        writer.startNode("Viewpoint", null);
        writer.startField("position");
        writer.fieldValue(pos, 3);
        writer.endNode(); // Viewpoint

        se.outputX3D(we, params, writer, null);
        writer.endDocument();
    }

    /**
     * Write a mesh to an X3D file
     *
     * @param we
     * @throws IOException
     */
    public static void writeMesh(TriangleMesh we, BinaryContentHandler writer, Map<String, Object> params, float[] pos) throws IOException {

        SAVExporter se = new SAVExporter();

        ErrorReporter console = new PlainTextErrorReporter();

        writer.startDocument("", "", "utf8", "#X3D", "V3.0", "");
        writer.profileDecl("Immersive");
        writer.startNode("NavigationInfo", null);
        writer.startField("avatarSize");
        writer.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
        writer.endNode(); // NavigationInfo
        writer.startNode("Viewpoint", null);
        writer.startField("position");
        writer.fieldValue(pos, 3);
        writer.endNode(); // Viewpoint

        se.outputX3D(we, params, writer, null);
        writer.endDocument();
    }

    /**
     * Write a mesh to an X3D file
     *
     * @param verts
     * @throws IOException
     */
    public static void writePointSet(double[] verts, BinaryContentHandler writer, Map<String,
            Object> params, float[] pos, boolean meshOnly, String defName) throws IOException {

        if (!meshOnly) {
            writer.startDocument("", "", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo
            writer.startNode("Viewpoint", null);
            writer.startField("position");
            writer.fieldValue(pos, 3);
            writer.endNode(); // Viewpoint
        }

        SAVExporter se = new SAVExporter();
        se.outputX3D(verts, params, writer, defName);

        if (!meshOnly) {
            writer.endDocument();
        }
    }

    /**
     * Write a mesh to an X3D file
     *
     * @param we
     * @throws IOException
     */
    public static void writeMesh(TriangleMesh we, BinaryContentHandler writer, Map<String,
            Object> params, boolean meshOnly) throws IOException {

        writeMesh(we,writer,params,meshOnly,null);
    }

    /**
     * Write a mesh to an X3D file
     *
     * @param we
     * @throws IOException
     */
    public static void writeMesh(TriangleMesh we, BinaryContentHandler writer, Map<String,
            Object> params, boolean meshOnly, String defName) throws IOException {

        writeMesh(we,writer,params,null,meshOnly,defName);
    }


    /**
     * Write a mesh to an X3D file
     *
     * @param we
     * @throws IOException
     */
    public static void writeMesh(TriangleMesh we, BinaryContentHandler writer, Map<String,
    		Object> params, float[] pos, boolean meshOnly, String defName) throws IOException {

        if (!meshOnly) {
            writer.startDocument("", "", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo
            if (pos != null) {
                writer.startNode("Viewpoint", null);
                writer.startField("position");
                writer.fieldValue(pos, 3);
                writer.endNode(); // Viewpoint
            }
        }

        SAVExporter se = new SAVExporter();
        se.outputX3D(we, params, writer, defName);
        
        if (!meshOnly) {
            writer.endDocument();
        }
    }
    
    public static void writeMeshSTL(TriangleMesh we, String filename) throws IOException {

        STLWriter writer = new STLWriter(filename);
        we.getTriangles(writer);

        /*
        Face face = we.getFaces();
        while(face != null){
            
            
            HalfEdge he = face.getHe();
            Point3d p0 = he.getStart().getPoint();
            he = he.getNext();
            Point3d p1 = he.getStart().getPoint();
            he = he.getNext();
            Point3d p2 = he.getStart().getPoint();
            writer.addTri(new Vector3d(p0),new Vector3d(p1),new Vector3d(p2));
            
            face = face.getNext();
            
        }
        */

        writer.close();

    }

}
