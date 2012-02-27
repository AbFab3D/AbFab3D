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

// External Imports
import javax.vecmath.*;
import java.util.*;

import toxi.geom.mesh.*;
import toxi.geom.*;

import org.web3d.vrml.sav.*;

import java.util.Collection;

/**
 * Outputs a toxi TriangleMesh into an X3D stream.
 *
 * @author Alan Hudson
 */
public class SAVExporter {
    public static final String EXPORT_NORMALS = "EXPORT_NORMALS";
    public static final String VERTEX_NORMALS = "VERTEX_NORMALS";

    /**
     * Output a toxiclibs TriangleMesh to an X3D stream.  By default this exporter exports
     * coordinates and normals.
     *
     * Supported params are:
     *    EXPORT_NORMALS, Boolean, TRUE -- Should we export normals
     *    VERTEX_NORMALS, Boolean, TRUE -- Should we use per-vertex normals
     *
     * @param mesh The mesh
     * @param params Output parameters
     * @param stream The SAV stream
     */
    public void outputX3D(TriangleMesh mesh, Map<String, Object> params, BinaryContentHandler stream) {

        boolean export_normals = true;
        boolean vertex_normals = true;
        
        if (params != null) {
            Boolean val = (Boolean) params.get("EXPORT_NORMALS");
            if (val != null) {
                export_normals = val.booleanValue();
            }
            
            val = (Boolean) params.get("VERTEX_NORMALS");
            if (val != null) {
                vertex_normals = val.booleanValue();
            }
        }

        if (vertex_normals) {
            mesh = mesh.computeVertexNormals();
        }

        List<Face> faces = mesh.getFaces();
        Collection<Vertex> vertices = mesh.getVertices();

        int[] indices = new int[faces.size() * 3];
        float[] coords = new float[vertices.size() * 3];
        float[] normals = null;

System.out.println("indices: " + faces.size() + " coords: " + vertices.size());
        int idx = 0;
        
        if (export_normals && !vertex_normals) {
            normals = new float[faces.size() * 3];
            int n_idx = 0;
            
            for(Face face : faces) {
                Vertex va = face.a;
                Vertex vb = face.b;
                Vertex vc = face.c;

                // TODO: javascript had this, id's start at -1, I suspect thats wrong
                indices[idx++] = va.id;
                indices[idx++] = vb.id;
                indices[idx++] = vc.id;

                normals[n_idx++] = face.normal.x;
                normals[n_idx++] = face.normal.y;
                normals[n_idx++] = face.normal.z;
            }

        } else {
            for(Face face : faces) {
                Vertex va = face.a;
                Vertex vb = face.b;
                Vertex vc = face.c;

                // TODO: javascript had this, id's start at -1, I suspect thats wrong
                indices[idx++] = va.id;
                indices[idx++] = vb.id;
                indices[idx++] = vc.id;
            }
        }

        idx = 0;

        for(Vertex vert : vertices) {
            coords[idx++] = vert.x;
            coords[idx++] = vert.y;
            coords[idx++] = vert.z;
        }

        if (export_normals && vertex_normals) {
            normals = new float[vertices.size() * 3];
            idx = 0;

            for(Vertex vert : vertices) {
                normals[idx++] = vert.normal.x;
                normals[idx++] = vert.normal.y;
                normals[idx++] = vert.normal.z;
            }
        }
//System.out.println("indices: " + java.util.Arrays.toString(indices));
//System.out.println("coords: " + java.util.Arrays.toString(coords));

        stream.startNode("Shape", null);
        stream.startField("appearance");

        //MaterialMapper mm = new MaterialMapper();
        //mm.createAppearance(material, finish, MaterialMapper.Shading.FIXED, 5, stream);
        stream.startField("appearance");
        stream.startNode("Appearance", null);
        stream.startField("material");
        stream.startNode("Material", null);
        stream.endNode();
        stream.endNode();
        
        stream.startField("geometry");
        stream.startNode("IndexedTriangleSet", null);
        stream.startField("normalPerVertex");
        stream.fieldValue(vertex_normals);
        stream.startField("index");
        stream.fieldValue(indices, indices.length);
        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");

        stream.fieldValue(coords, coords.length);
        stream.endNode();   // Coord

        if (export_normals) {
            stream.startField("normal");
            stream.startNode("Normal", null);
            stream.startField("vector");
            stream.fieldValue(normals, normals.length);
            stream.endNode();
        }
        stream.endNode();   // IndexedTriangleSet
        stream.endNode();   // Shape

    }

    /**
     * Output a toxiclibs TriangleMesh Array to an X3D stream.  Places all meshes into one Shape.
     * By default this exporter exports coordinates and normals.
     *
     * Supported params are:
     *    EXPORT_NORMALS, Boolean, TRUE -- Should we export normals
     *    VERTEX_NORMALS, Boolean, TRUE -- Should we use per-vertex normals
     *
     * @param mesh The mesh
     * @param params Output parameters
     * @param stream The SAV stream
     */
    public void outputX3D(TriangleMesh[] mesh, Map<String, Object> params, BinaryContentHandler stream) {
        // TODO: need to implement
    }
}
