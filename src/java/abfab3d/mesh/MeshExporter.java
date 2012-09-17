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
package abfab3d.mesh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.HashMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


import org.web3d.vrml.sav.BinaryContentHandler;

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.export.X3DClassicRetainedExporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;

import abfab3d.io.output.SAVExporter;
import abfab3d.io.output.STLWriter;

public class MeshExporter {
    
    public static void writeMesh(WingedEdgeTriangleMesh we, String filename) throws IOException {
        
        SAVExporter se = new SAVExporter();
        HashMap<String,Object> params = new HashMap<String, Object>();
        
        FileOutputStream fos = null;
        
        try {
            BinaryContentHandler writer = null;
            fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            
            ErrorReporter console = new PlainTextErrorReporter();

            if (encoding.equals("x3db")) {
                writer = new X3DBinaryRetainedDirectExporter(fos,
                        3, 0, console,
                        X3DBinarySerializer.METHOD_FASTEST_PARSING,
                        0.001f, true);
            } else if (encoding.equals("x3dv")) {
                writer = new X3DClassicRetainedExporter(fos,3,0,console);
            } else if (encoding.equals("x3d")) {
                writer = new X3DXMLRetainedExporter(fos,3,0,console);
            } else {
                throw new IllegalArgumentException("Unhandled X3D encoding: " + encoding);
            }

            writer.startDocument("","", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo

            se.outputX3D(we, params,writer);
            writer.endDocument();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static void writeMeshSTL(WingedEdgeTriangleMesh we, String filename) throws IOException {
        
        Face face = we.getFaces();
        STLWriter writer = new STLWriter(filename);

        
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


        writer.close();

    }

}
