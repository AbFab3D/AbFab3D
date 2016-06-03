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

import java.util.*;

import abfab3d.grid.util.ExecutionStoppedException;
import abfab3d.mesh.*;
import abfab3d.util.StructMixedData;
import static abfab3d.util.Output.printf;

import org.web3d.vrml.sav.*;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;
import toxi.geom.mesh.Vertex;

import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Outputs TriangleMeshes into an X3D stream.
 *
 * @author Alan Hudson
 */
public class SAVExporter {
    public static final String EXPORT_NORMALS = "EXPORT_NORMALS";
    public static final String VERTEX_NORMALS = "VERTEX_NORMALS";
    public static final String COMPACT_VERTICES = "COMPACT_VERTICES";
    public static final String GEOMETRY_TYPE = "GEOMETRY_TYPE";
    public static final String MATERIAL = "MATERIAL";
    public static final String FINISH = "FINISH";

    public enum GeometryType {INDEXEDTRIANGLESET, INDEXEDFACESET, INDEXEDLINESET, POINTSET}

    /**
     * Output a WingedEdgeTriangleMesh to an X3D stream.  By default this exporter exports
     * coordinates and normals.
     * <p/>
     * Supported params are:
     * EXPORT_NORMALS, Boolean, TRUE -- Should we export normals
     * VERTEX_NORMALS, Boolean, TRUE -- Should we use per-vertex normals
     *
     * @param mesh   The mesh
     * @param params Output parameters
     * @param stream The SAV stream
     */
    public void outputX3D(abfab3d.util.TriangleMesh mesh, Map<String, Object> params, BinaryContentHandler stream, String defName) {
        String material = null;
        String finish[] = null;

        if (params != null) {
            material = (String) params.get(MATERIAL);
            Object o = params.get(FINISH);
            if (o instanceof String) {
                finish = new String[]{(String) params.get(FINISH)};
            } else {
                finish = (String[]) o;
            }
        }
        outputX3D(mesh, params, material, finish, stream, defName);
    }

    /**
     * Output a PointSet to an X3D stream.  By default this exporter exports
     * coordinates and normals.
     * <p/>
     * Supported params are:
     * EXPORT_NORMALS, Boolean, TRUE -- Should we export normals
     * VERTEX_NORMALS, Boolean, TRUE -- Should we use per-vertex normals
     *
     * @param verts  The mesh
     * @param params Output parameters
     * @param stream The SAV stream
     */
    public void outputX3D(double[] verts, Map<String, Object> params, BinaryContentHandler stream, String defName) {
        String material = null;
        String finish[] = null;

        if (params != null) {
            material = (String) params.get(MATERIAL);
            Object o = params.get(FINISH);
            if (o instanceof String) {
                finish = new String[]{(String) params.get(FINISH)};
            } else {
                finish = (String[]) o;
            }
        }
        outputX3D(verts, params, material, finish, stream, defName);
    }


    /**
     * Output a WingedEdgeTriangleMesh to an X3D stream.  By default this exporter exports
     * coordinates and normals.
     * <p/>
     * Supported params are:
     * EXPORT_NORMALS, Boolean, TRUE -- Should we export normals
     * VERTEX_NORMALS, Boolean, TRUE -- Should we use per-vertex normals
     *
     * @param mesh     The mesh
     * @param params   Output parameters
     * @param material The material from MaterialMapper for the appearance
     * @param finish   The finish.
     * @param stream   The SAV stream
     */
    public void outputX3D(abfab3d.util.TriangleMesh mesh, Map<String, Object> params, String material, String[] finish,
                          BinaryContentHandler stream, String defName) {

        boolean export_normals = false;
        boolean vertex_normals = false;
        boolean compact_vertices = false;
        GeometryType gtype = GeometryType.INDEXEDTRIANGLESET;

        if (params != null) {
            Boolean val = (Boolean) params.get(EXPORT_NORMALS);
            if (val != null) {
                export_normals = val.booleanValue();
            }

            val = (Boolean) params.get(VERTEX_NORMALS);
            if (val != null) {
                vertex_normals = val.booleanValue();
            }

            val = (Boolean) params.get(COMPACT_VERTICES);
            if (val != null) {
                compact_vertices = val.booleanValue();
            }

            GeometryType val2 = (GeometryType) params.get(GEOMETRY_TYPE);

            if (val2 != null) {
                gtype = val2;
            }
        }

        int[] faces = mesh.getFaceIndexes();

        int[] indices = null;

        if (gtype == GeometryType.INDEXEDTRIANGLESET) {
            indices = new int[faces.length];
        } else if (gtype == GeometryType.INDEXEDFACESET || gtype == GeometryType.INDEXEDLINESET) {
            indices = new int[faces.length / 3 * 4];
        }
        float[] coords = new float[mesh.getVertexCount() * 3];
        float[] normals = null;
        float[] colors = null;
        float[] texCoords = null;
        int num_coords = mesh.getVertexCount();
        int color_channel = mesh.getColorChannel();
        int tex0_channel = mesh.getAttributeChannel(abfab3d.util.TriangleMesh.VA_TEXCOORD0);

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

        StructMixedData vertices = mesh.getVertices();

        if (export_normals && !vertex_normals) {

            normals = new float[faces.length];
            int idx = 0;
            int n_idx = 0;
            int color_idx = 0;
            int texCoord_idx = 0;

            int len = faces.length / 3;
            int f_idx = 0;
            Vector3d ac = new Vector3d();
            Vector3d ab = new Vector3d();
            double[] pnt = new double[3];
            double[] pnt2 = new double[3];

            for (int i = 0; i < len; i++) {
                int va = faces[f_idx++];
                int vb = faces[f_idx++];
                int vc = faces[f_idx++];

                if (gtype != GeometryType.POINTSET) {
                    indices[idx++] = abfab3d.mesh.Vertex.getID(vertices, va);
                    indices[idx++] = abfab3d.mesh.Vertex.getID(vertices, vb);
                    indices[idx++] = abfab3d.mesh.Vertex.getID(vertices, vc);
                }

                if (gtype == GeometryType.INDEXEDFACESET || gtype == GeometryType.INDEXEDLINESET) {
                    indices[idx++] = -1;
                }

                // TODO: These don't look right
                abfab3d.mesh.Vertex.getPoint(vertices, va, pnt);
                abfab3d.mesh.Vertex.getPoint(vertices, vc, pnt2);
                ac.set(pnt[0] - pnt2[0], pnt[1] - pnt2[1], pnt[2] - pnt2[2]);

                abfab3d.mesh.Vertex.getPoint(vertices, va, pnt);
                abfab3d.mesh.Vertex.getPoint(vertices, vb, pnt2);
                ab.set(pnt[0] - pnt2[0], pnt[1] - pnt2[1], pnt[2] - pnt2[2]);

                ac.cross(ac, ab);
                ac.normalize();

                normals[n_idx++] = (float) ac.x;
                normals[n_idx++] = (float) ac.y;
                normals[n_idx++] = (float) ac.z;
            }

            idx = 0;

            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }

            if (color_channel == -1 && tex0_channel == -1) {
                int v = mesh.getStartVertex();

                while (v != -1) {
                    abfab3d.mesh.Vertex.getPoint(vertices, v, pnt);
                    coords[idx++] = (float) pnt[0];
                    coords[idx++] = (float) pnt[1];
                    coords[idx++] = (float) pnt[2];

                    v = abfab3d.mesh.Vertex.getNext(vertices, v);
                }
            } else if (color_channel != -1 && tex0_channel == -1) {
                int v = mesh.getStartVertex();
                colors = new float[mesh.getVertexCount() * 3];
                float[] color = new float[3];
                int numAttribs = mesh.getSemantics().length;

                if (numAttribs == 1) {

                    while (v != -1) {

                        abfab3d.mesh.Vertex.getPoint(vertices, v, pnt);
                        coords[idx++] = (float) pnt[0];
                        coords[idx++] = (float) pnt[1];
                        coords[idx++] = (float) pnt[2];

                        VertexAttribs1.getAttrib(color_channel, vertices, v, color);

                        colors[color_idx++] = color[0];
                        colors[color_idx++] = color[1];
                        colors[color_idx++] = color[2];

                        v = abfab3d.mesh.Vertex.getNext(vertices, v);
                    }
                } else if (numAttribs == 3) {
                    while (v != -1) {

                        abfab3d.mesh.Vertex.getPoint(vertices, v, pnt);
                        coords[idx++] = (float) pnt[0];
                        coords[idx++] = (float) pnt[1];
                        coords[idx++] = (float) pnt[2];

                        VertexAttribs3.getAttrib(color_channel, vertices, v, color);

                        colors[color_idx++] = color[0];
                        colors[color_idx++] = color[1];
                        colors[color_idx++] = color[2];

                        v = abfab3d.mesh.Vertex.getNext(vertices, v);
                    }

                } else {
                    throw new IllegalArgumentException("Unsupported number of colors");
                }
            } else if (tex0_channel != -1 && color_channel == -1) {
                int v = mesh.getStartVertex();
                texCoords = new float[mesh.getVertexCount() * 2];
                float[] att = new float[3];
                int numAttribs = mesh.getSemantics().length;

                if (numAttribs == 1) {

                    while (v != -1) {

                        abfab3d.mesh.Vertex.getPoint(vertices, v, pnt);
                        coords[idx++] = (float) pnt[0];
                        coords[idx++] = (float) pnt[1];
                        coords[idx++] = (float) pnt[2];

                        VertexAttribs1.getAttrib(tex0_channel, vertices, v, att);

                        texCoords[texCoord_idx++] = att[0];
                        texCoords[texCoord_idx++] = att[1];

                        v = abfab3d.mesh.Vertex.getNext(vertices, v);
                    }
                } else if (numAttribs == 3) {
                    while (v != -1) {

                        abfab3d.mesh.Vertex.getPoint(vertices, v, pnt);
                        coords[idx++] = (float) pnt[0];
                        coords[idx++] = (float) pnt[1];
                        coords[idx++] = (float) pnt[2];

                        VertexAttribs3.getAttrib(tex0_channel, vertices, v, att);

                        texCoords[texCoord_idx++] = att[0];
                        texCoords[texCoord_idx++] = att[1];

                        v = abfab3d.mesh.Vertex.getNext(vertices, v);
                    }

                } else {
                    throw new IllegalArgumentException("Unsupported number of colors");
                }
            } else {
                throw new IllegalArgumentException("No support for writing colors and texCoords");
            }

        } else {
            int len = faces.length / 3;
            int f_idx = 0;

            int idx = 0;
            int max_coord = coords.length / 3 - 1;
            int max_idx = 0;
            int display_max = 10;
            int bad_cnt = 0;

            for (int i = 0; i < len; i++) {
                int va = faces[f_idx++];
                int vb = faces[f_idx++];
                int vc = faces[f_idx++];

                int va_id = abfab3d.mesh.Vertex.getID(vertices, va);
                int vb_id = abfab3d.mesh.Vertex.getID(vertices, vb);
                int vc_id = abfab3d.mesh.Vertex.getID(vertices, vc);

                if (va_id > max_coord || vb_id > max_coord || vc_id > max_coord) {
                    if (bad_cnt < display_max) {
                        System.out.println("Invalid face: " + faces[i]);
                    }

                    bad_cnt++;
                    continue;
                }

                if (va_id > max_idx) {
                    max_idx = va_id;
                }
                if (vb_id > max_idx) {
                    max_idx = vb_id;
                }
                if (vc_id > max_idx) {
                    max_idx = vc_id;
                }

                if (gtype != GeometryType.POINTSET) {
                    indices[idx++] = va_id;
                    indices[idx++] = vb_id;
                    indices[idx++] = vc_id;
                }

                if (gtype == GeometryType.INDEXEDFACESET || gtype == GeometryType.INDEXEDLINESET) {
                    indices[idx++] = -1;
                }

            }

            if (bad_cnt > 0) {
                System.out.println("Bad faces.  Removed: " + bad_cnt + " left: " + (idx - 1));
                int[] new_indices = new int[idx - 1];
                System.arraycopy(indices, 0, new_indices, 0, idx - 1);

                indices = new_indices;
            }

            idx = 0;

            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }

            double[] pnt = new double[3];

            if (color_channel == -1 && tex0_channel == -1) {
                int v = mesh.getStartVertex();

                while (v != -1) {
                    abfab3d.mesh.Vertex.getPoint(vertices, v, pnt);
                    coords[idx++] = (float) pnt[0];
                    coords[idx++] = (float) pnt[1];
                    coords[idx++] = (float) pnt[2];

                    v = abfab3d.mesh.Vertex.getNext(vertices, v);
                }
            } else if (color_channel != -1 && tex0_channel == -1) {
                colors = new float[mesh.getVertexCount() * 3];

                int v = mesh.getStartVertex();

                int color_idx = 0;
                float[] color = new float[3];
                int numAttribs = mesh.getSemantics().length;

                while (v != -1) {

                    abfab3d.mesh.Vertex.getPoint(vertices, v, pnt);
                    coords[idx++] = (float) pnt[0];
                    coords[idx++] = (float) pnt[1];
                    coords[idx++] = (float) pnt[2];

                    if (numAttribs == 1) {
                        VertexAttribs1.getAttrib(color_channel, vertices, v, color);

                        colors[color_idx++] = color[0];
                        colors[color_idx++] = color[1];
                        colors[color_idx++] = color[2];
                    } else if (numAttribs == 3) {
                        VertexAttribs3.getAttrib(color_channel, vertices, v, color);

                        colors[color_idx++] = color[0];
                        colors[color_idx++] = color[1];
                        colors[color_idx++] = color[2];
                    } else {
                        throw new IllegalArgumentException("Unsupported number of colors");
                    }

                           /*
                    color = v.getAttribs(color_channel);
                    colors[color_idx++] = color[0];
                    colors[color_idx++] = color[1];
                    colors[color_idx++] = color[2];
                             */
                    v = abfab3d.mesh.Vertex.getNext(vertices, v);
                }

            } else if (tex0_channel != -1 && color_channel == -1) {
                texCoords = new float[mesh.getVertexCount() * 2];

                int v = mesh.getStartVertex();

                int texCoord_idx = 0;
                float[] att = new float[3];
                int numAttribs = mesh.getSemantics().length;

                while (v != -1) {

                    abfab3d.mesh.Vertex.getPoint(vertices, v, pnt);
                    coords[idx++] = (float) pnt[0];
                    coords[idx++] = (float) pnt[1];
                    coords[idx++] = (float) pnt[2];

                    if (numAttribs == 1) {
                        VertexAttribs1.getAttrib(tex0_channel, vertices, v, att);

                        texCoords[texCoord_idx++] = att[0];
                        texCoords[texCoord_idx++] = att[1];
                    } else if (numAttribs == 3) {
                        VertexAttribs3.getAttrib(tex0_channel, vertices, v, att);

                        texCoords[texCoord_idx++] = att[0];
                        texCoords[texCoord_idx++] = att[1];
                    } else {
                        throw new IllegalArgumentException("Unsupported number of colors");
                    }

                    v = abfab3d.mesh.Vertex.getNext(vertices, v);
                }
            }

            if (export_normals && vertex_normals) {
                System.out.println("***Need to implement normal export");
                /*
                normals = new float[mesh.getVertexCount() * 3];
                idx = 0;

                abfab3d.mesh.Vertex v = mesh.getVertices();

                // TODO: need to calculate per vertex normals
                while (v != null) {
                    v = v.getNext();

                    normals[idx++] = vert.normal.x;
                    normals[idx++] = vert.normal.y;
                    normals[idx++] = vert.normal.z;

                }
            */
            }

        }

        //System.out.println("indices: " + java.util.Arrays.toString(indices));
        //System.out.println("coords: " + java.util.Arrays.toString(coords));

        stream.startNode("Shape", defName);

        stream.startField("appearance");

        MaterialMapper mm = new MaterialMapper();
        mm.createAppearance(material, finish, MaterialMapper.Shading.FIXED, 5, stream);

        stream.startField("geometry");
        if (gtype == GeometryType.INDEXEDTRIANGLESET) {
            stream.startNode("IndexedTriangleSet", null);
        } else if (gtype == GeometryType.INDEXEDFACESET) {
            stream.startNode("IndexedFaceSet", null);

            // Makes X3DOM load much faster
            stream.startField("creaseAngle");
            stream.fieldValue(0.5236f);  // 30 degrees
        } else if (gtype == GeometryType.INDEXEDLINESET) {
            stream.startNode("IndexedLineSet", null);
        } else if (gtype == GeometryType.POINTSET) {
            stream.startNode("PointSet", null);
        }

        if (gtype != GeometryType.INDEXEDLINESET && gtype != GeometryType.POINTSET) {
            stream.startField("normalPerVertex");
            stream.fieldValue(vertex_normals);
        }
        if (gtype == GeometryType.INDEXEDTRIANGLESET) {
            stream.startField("index");
        } else if (gtype == GeometryType.INDEXEDFACESET || gtype == GeometryType.INDEXEDLINESET) {
            stream.startField("coordIndex");
        }
        if (indices != null) stream.fieldValue(indices, indices.length);
        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");

        stream.fieldValue(coords, num_coords * 3);
        stream.endNode();   // Coord

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

        if (colors != null) {
            stream.startField("color");
            stream.startNode("Color", null);
            stream.startField("color");

            stream.fieldValue(colors, num_coords * 3);
            stream.endNode();   // Color
        }

        if (texCoords != null) {
            stream.startField("texCoord");
            stream.startNode("TextureCoordinate", null);
            stream.startField("point");

            stream.fieldValue(texCoords, num_coords * 2);
            stream.endNode();   // TextureCoordinate
        }

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
     * Output a PointSet to an X3D stream.  By default this exporter exports
     * coordinates and normals.
     * <p/>
     * Supported params are:
     * EXPORT_NORMALS, Boolean, TRUE -- Should we export normals
     * VERTEX_NORMALS, Boolean, TRUE -- Should we use per-vertex normals
     *
     * @param verts    The mesh
     * @param params   Output parameters
     * @param material The material from MaterialMapper for the appearance
     * @param finish   The finish.
     * @param stream   The SAV stream
     */
    public void outputX3D(double[] verts, Map<String, Object> params, String material, String[] finish,
                          BinaryContentHandler stream, String defName) {

        float[] coords = new float[verts.length];


        int len = verts.length;
        for (int i = 0; i < len; i++) {
            coords[i] = (float) verts[i];
        }
        stream.startNode("Shape", defName);

        stream.startField("appearance");

        MaterialMapper mm = new MaterialMapper();
        mm.createAppearance(material, finish, MaterialMapper.Shading.FIXED, 5, stream);

        stream.startField("geometry");
        stream.startNode("PointSet", null);

        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");

        stream.fieldValue(coords, coords.length);
        stream.endNode();   // Coord

        stream.endNode();   // IndexedTriangleSet
        stream.endNode();   // Shape

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }
    }

    /**
     * Output a toxiclibs TriangleMesh to an X3D stream.  By default this exporter exports
     * coordinates and normals.
     * <p/>
     * Supported params are:
     * EXPORT_NORMALS, Boolean, TRUE -- Should we export normals
     * VERTEX_NORMALS, Boolean, TRUE -- Should we use per-vertex normals
     *
     * @param mesh   The mesh
     * @param params Output parameters
     * @param stream The SAV stream
     */
    public void outputX3D(TriangleMesh mesh, Map<String, Object> params, BinaryContentHandler stream) {
        outputX3D(mesh, params, null, null, stream);
    }

    /**
     * Output a toxiclibs TriangleMesh to an X3D stream.  By default this exporter exports
     * coordinates and normals.
     * <p/>
     * Supported params are:
     * EXPORT_NORMALS, Boolean, TRUE -- Should we export normals
     * VERTEX_NORMALS, Boolean, TRUE -- Should we use per-vertex normals
     *
     * @param mesh     The mesh
     * @param params   Output parameters
     * @param material The material from MaterialMapper for the appearance
     * @param finish   The finish.
     * @param stream   The SAV stream
     */
    public void outputX3D(TriangleMesh mesh, Map<String, Object> params, String material, String[] finish, BinaryContentHandler stream) {

        boolean export_normals = true;
        boolean vertex_normals = true;
        boolean compact_vertices = false;
        GeometryType gtype = GeometryType.INDEXEDTRIANGLESET;

        if (params != null) {
            Boolean val = (Boolean) params.get(EXPORT_NORMALS);
            if (val != null) {
                export_normals = val.booleanValue();
            }

            val = (Boolean) params.get(VERTEX_NORMALS);
            if (val != null) {
                vertex_normals = val.booleanValue();
            }

            val = (Boolean) params.get(COMPACT_VERTICES);
            if (val != null) {
                compact_vertices = val.booleanValue();
            }
            GeometryType val2 = (GeometryType) params.get(GEOMETRY_TYPE);

            if (val2 != null) {
                gtype = val2;
            }
        }

        if (vertex_normals && export_normals) {
            mesh = mesh.computeVertexNormals();
        }

        List<Face> faces = mesh.getFaces();
        Collection<Vertex> vertices = mesh.getVertices();

        int[] indices = null;

        if (gtype == GeometryType.INDEXEDTRIANGLESET) {
            indices = new int[faces.size() * 3];
        } else if (gtype == GeometryType.INDEXEDFACESET) {
            indices = new int[faces.size() * 4];
        }

        float[] coords = new float[vertices.size() * 3];
        float[] normals = null;
        int num_coords = vertices.size();

        if (export_normals && !vertex_normals) {
            normals = new float[faces.size() * 3];
            int idx = 0;
            int n_idx = 0;

            for (Face face : faces) {
                Vertex va = face.a;
                Vertex vb = face.b;
                Vertex vc = face.c;

                // TODO: javascript had this, id's start at -1, I suspect thats wrong
                indices[idx++] = va.id;
                indices[idx++] = vb.id;
                indices[idx++] = vc.id;

                if (gtype == GeometryType.INDEXEDFACESET) {
                    indices[idx++] = -1;
                }
                normals[n_idx++] = face.normal.x;
                normals[n_idx++] = face.normal.y;
                normals[n_idx++] = face.normal.z;
            }

            idx = 0;
            for (Vertex vert : vertices) {
                coords[idx++] = vert.x;
                coords[idx++] = vert.y;
                coords[idx++] = vert.z;
            }
        } else {

            if (compact_vertices) {
                HashMap<Vertex, Integer> reassigned = new HashMap<Vertex, Integer>(mesh.vertices.size());

                int idx = 0;
                int last_vertex = 0;
                int c_idx = 0;

                for (Face face : faces) {
                    Vertex va = face.a;
                    Vertex vb = face.b;
                    Vertex vc = face.c;

                    Integer va_idx = reassigned.get(va);
                    Integer vb_idx = reassigned.get(vb);
                    Integer vc_idx = reassigned.get(vc);

                    if (va_idx == null) {
                        va_idx = new Integer(last_vertex++);
                        coords[c_idx++] = va.x;
                        coords[c_idx++] = va.y;
                        coords[c_idx++] = va.z;

                        reassigned.put(va, va_idx);
                    }
                    if (vb_idx == null) {
                        vb_idx = new Integer(last_vertex++);
                        coords[c_idx++] = vb.x;
                        coords[c_idx++] = vb.y;
                        coords[c_idx++] = vb.z;
                        reassigned.put(vb, vb_idx);
                    }
                    if (vc_idx == null) {
                        vc_idx = new Integer(last_vertex++);
                        coords[c_idx++] = vc.x;
                        coords[c_idx++] = vc.y;
                        coords[c_idx++] = vc.z;
                        reassigned.put(vc, vc_idx);
                    }

                    indices[idx++] = va_idx.intValue();
                    indices[idx++] = vb_idx.intValue();
                    indices[idx++] = vc_idx.intValue();
                    if (gtype == GeometryType.INDEXEDFACESET) {
                        indices[idx++] = -1;
                    }
                }
                num_coords = reassigned.size();
            } else {
                int idx = 0;
                int max_coord = coords.length / 3 - 1;
                int max_idx = 0;
                int display_max = 10;
                int bad_cnt = 0;

                for (Face face : faces) {
                    Vertex va = face.a;
                    Vertex vb = face.b;
                    Vertex vc = face.c;

                    if (va.id > max_coord || vb.id > max_coord || vc.id > max_coord) {
                        if (bad_cnt < display_max) {
                            System.out.println("Invalid face: " + face);
                        }

                        bad_cnt++;
                        continue;
                    }

                    if (va.id > max_idx) {
                        max_idx = va.id;
                    }
                    if (vb.id > max_idx) {
                        max_idx = vb.id;
                    }
                    if (vc.id > max_idx) {
                        max_idx = vc.id;
                    }
                    indices[idx++] = va.id;
                    indices[idx++] = vb.id;
                    indices[idx++] = vc.id;
                    if (gtype == GeometryType.INDEXEDFACESET) {
                        indices[idx++] = -1;
                    }

                }

                if (bad_cnt > 0) {
                    System.out.println("Bad faces.  Removed: " + bad_cnt + " left: " + (idx - 1));
                    int[] new_indices = new int[idx - 1];
                    System.arraycopy(indices, 0, new_indices, 0, idx - 1);

                    indices = new_indices;
                }

                idx = 0;
                for (Vertex vert : vertices) {
                    coords[idx++] = vert.x;
                    coords[idx++] = vert.y;
                    coords[idx++] = vert.z;
                }

                if (export_normals && vertex_normals) {
                    normals = new float[vertices.size() * 3];
                    idx = 0;

                    for (Vertex vert : vertices) {
                        normals[idx++] = vert.normal.x;
                        normals[idx++] = vert.normal.y;
                        normals[idx++] = vert.normal.z;
                    }
                }

            }
        }

//System.out.println("indices: " + java.util.Arrays.toString(indices));
//System.out.println("coords: " + java.util.Arrays.toString(coords));

        stream.startNode("Shape", null);

        stream.startField("appearance");

        MaterialMapper mm = new MaterialMapper();
        mm.createAppearance(material, finish, MaterialMapper.Shading.FIXED, 5, stream);

        stream.startField("geometry");
        if (gtype == GeometryType.INDEXEDTRIANGLESET) {
            stream.startNode("IndexedTriangleSet", null);
        } else if (gtype == GeometryType.INDEXEDFACESET || gtype == GeometryType.INDEXEDLINESET) {
            stream.startNode("IndexedFaceSet", null);
        }
        stream.startField("normalPerVertex");
        stream.fieldValue(vertex_normals);
        if (gtype == GeometryType.INDEXEDTRIANGLESET) {
            stream.startField("index");
        } else {
            stream.startField("coordIndex");
        }
        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");

        stream.fieldValue(coords, num_coords * 3);
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
     * <p/>
     * Supported params are:
     * EXPORT_NORMALS, Boolean, TRUE -- Should we export normals
     * VERTEX_NORMALS, Boolean, TRUE -- Should we use per-vertex normals
     *
     * @param mesh   The mesh
     * @param params Output parameters
     * @param stream The SAV stream
     */
    public void outputX3D(TriangleMesh[] mesh, Map<String, Object> params, BinaryContentHandler stream) {
        // TODO: need to implement
    }

}
