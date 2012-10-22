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

import abfab3d.mesh.WingedEdgeTriangleMesh;
import toxi.geom.mesh.*;

import org.web3d.vrml.sav.*;

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

    public enum GeometryType {INDEXEDTRIANGLESET,INDEXEDFACESET}

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
        outputX3D(mesh, params, null, null, stream);
    }
    
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
     * @param material The material from MaterialMapper for the appearance
     * @param finish The finish.
     * @param stream The SAV stream
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
            
            for(Face face : faces) {
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
            for(Vertex vert : vertices) {
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

                for(Face face : faces) {
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

                for(Face face : faces) {
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
                    System.out.println("Bad faces.  Removed: " + bad_cnt + " left: " + (idx -1));
                    int[] new_indices = new int[idx-1];
                    System.arraycopy(indices,0,new_indices,0,idx-1);

                    indices = new_indices;
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
        } else {
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
    public void outputX3D(WingedEdgeTriangleMesh mesh, Map<String, Object> params, BinaryContentHandler stream) {
        outputX3D(mesh, params, null, null, stream);
    }

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
     * @param material The material from MaterialMapper for the appearance
     * @param finish The finish.
     * @param stream The SAV stream
     */
    public void outputX3D(WingedEdgeTriangleMesh mesh, Map<String, Object> params, String material, String[] finish, BinaryContentHandler stream) {

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

        abfab3d.mesh.Vertex[][] faces = mesh.getFaceIndexes();

        int[] indices = null;

        if (gtype == GeometryType.INDEXEDTRIANGLESET) {
            indices = new int[faces.length * 3];
        } else if (gtype == GeometryType.INDEXEDFACESET) {
            indices = new int[faces.length * 4];
        }
        float[] coords = new float[mesh.getVertexCount() * 3];
        float[] normals = null;
        int num_coords = mesh.getVertexCount();

        if (export_normals && !vertex_normals) {
            normals = new float[faces.length * 3];
            int idx = 0;
            int n_idx = 0;

            for(int i=0; i < faces.length; i++) {
                abfab3d.mesh.Vertex va = faces[i][0];
                abfab3d.mesh.Vertex vb = faces[i][1];
                abfab3d.mesh.Vertex vc = faces[i][2];

                indices[idx++] = va.getID();
                indices[idx++] = vb.getID();
                indices[idx++] = vc.getID();
                if (gtype == GeometryType.INDEXEDFACESET) {
                    indices[idx++] = -1;
                }

                Vector3d ac = new Vector3d(va.getPoint());
                ac.sub(vc.getPoint());
                Vector3d ab = new Vector3d(va.getPoint());
                ab.sub(vb.getPoint());
                ac.cross(ac,ab);
                ac.normalize();

                normals[n_idx++] = (float) ac.x;
                normals[n_idx++] = (float) ac.y;
                normals[n_idx++] = (float) ac.z;
            }

            idx = 0;
            abfab3d.mesh.Vertex v = mesh.getVertices();

            while (v != null) {

                coords[idx++] = (float) v.getPoint().x;
                coords[idx++] = (float) v.getPoint().y;
                coords[idx++] = (float) v.getPoint().z;

                v = v.getNext();
            }

        } else {

            if (compact_vertices) {
                HashMap<abfab3d.mesh.Vertex, Integer> reassigned = new HashMap<abfab3d.mesh.Vertex, Integer>(mesh.getVertexCount());

                int idx = 0;
                int last_vertex = 0;
                int c_idx = 0;

                for(int i=0; i < faces.length; i++) {
                    abfab3d.mesh.Vertex va = faces[i][0];
                    abfab3d.mesh.Vertex vb = faces[i][1];
                    abfab3d.mesh.Vertex vc = faces[i][2];

                    Integer va_idx = reassigned.get(va);
                    Integer vb_idx = reassigned.get(vb);
                    Integer vc_idx = reassigned.get(vc);

                    if (va_idx == null) {
                        va_idx = new Integer(last_vertex++);
                        coords[c_idx++] = (float) va.getPoint().x;
                        coords[c_idx++] = (float) va.getPoint().y;
                        coords[c_idx++] = (float) va.getPoint().z;

                        reassigned.put(va, va_idx);
                    }
                    if (vb_idx == null) {
                        vb_idx = new Integer(last_vertex++);
                        coords[c_idx++] = (float) vb.getPoint().x;
                        coords[c_idx++] = (float) vb.getPoint().y;
                        coords[c_idx++] = (float) vb.getPoint().z;
                        reassigned.put(vb, vb_idx);
                    }
                    if (vc_idx == null) {
                        vc_idx = new Integer(last_vertex++);
                        coords[c_idx++] = (float) vc.getPoint().x;
                        coords[c_idx++] = (float) vc.getPoint().y;
                        coords[c_idx++] = (float) vc.getPoint().z;
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

                for(int i=0; i < faces.length; i++) {
                    abfab3d.mesh.Vertex va = faces[i][0];
                    abfab3d.mesh.Vertex vb = faces[i][1];
                    abfab3d.mesh.Vertex vc = faces[i][2];

                    if (va.getID() > max_coord || vb.getID() > max_coord || vc.getID() > max_coord) {
                        if (bad_cnt < display_max) {
                            System.out.println("Invalid face: " + faces[i]);
                        }

                        bad_cnt++;
                        continue;
                    }

                    if (va.getID() > max_idx) {
                        max_idx = va.getID();
                    }
                    if (vb.getID() > max_idx) {
                        max_idx = vb.getID();
                    }
                    if (vc.getID() > max_idx) {
                        max_idx = vc.getID();
                    }
                    indices[idx++] = va.getID();
                    indices[idx++] = vb.getID();
                    indices[idx++] = vc.getID();
                    if (gtype == GeometryType.INDEXEDFACESET) {
                        indices[idx++] = -1;
                    }

                }

                if (bad_cnt > 0) {
                    System.out.println("Bad faces.  Removed: " + bad_cnt + " left: " + (idx -1));
                    int[] new_indices = new int[idx-1];
                    System.arraycopy(indices,0,new_indices,0,idx-1);

                    indices = new_indices;
                }

                idx = 0;

                abfab3d.mesh.Vertex v = mesh.getVertices();

                while (v != null) {

                    coords[idx++] = (float) v.getPoint().x;
                    coords[idx++] = (float) v.getPoint().y;
                    coords[idx++] = (float) v.getPoint().z;

                    v = v.getNext();
                }

                if (export_normals && vertex_normals) {
                    normals = new float[mesh.getVertexCount() * 3];
                    idx = 0;

                    v = mesh.getVertices();

                    // TODO: need to calculate per vertex normals
                    while (v != null) {
                        v = v.getNext();
/*
                        normals[idx++] = vert.normal.x;
                        normals[idx++] = vert.normal.y;
                        normals[idx++] = vert.normal.z;
*/
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
        stream.startField("geometry");
        if (gtype == GeometryType.INDEXEDTRIANGLESET) {
            stream.startNode("IndexedTriangleSet", null);
        } else {
            stream.startNode("IndexedFaceSet", null);
        }
        stream.startField("normalPerVertex");
        stream.fieldValue(vertex_normals);
        if (gtype == GeometryType.INDEXEDTRIANGLESET) {
            stream.startField("index");
        } else {
            stream.startField("coordIndex");
        }
        stream.fieldValue(indices, indices.length);
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

}
