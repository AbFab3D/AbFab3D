package abfab3d.geom;

import org.j3d.geom.GeometryData;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;
import toxi.geom.mesh.Vertex;

import java.util.Collection;
import java.util.List;

/**
 * Utilities moving toxiclibs structures to internal structures
 *
 * @author Alan Hudson
 */
public class ToxiConverter {
    /**
     *  Convert a toxilibs TriangleMesh to a j3d.org GeometryData
     *
     * @param mesh The toxi mesh
     * @return The j3d.org mesh
     */
    public static GeometryData convertMesh(TriangleMesh mesh) {
        GeometryData ret_val = new GeometryData();

        ret_val.geometryType = GeometryData.INDEXED_TRIANGLES;

        List<Face> faces = mesh.getFaces();
        Collection<Vertex> vertices = mesh.getVertices();

        ret_val.indexes = new int[faces.size() * 3];
        ret_val.coordinates = new float[vertices.size() * 3];
        float[] normals = null;

        System.out.println("indices: " + faces.size() + " coords: " + vertices.size());

        int idx = 0;
        for(Face face : faces) {
            Vertex va = face.a;
            Vertex vb = face.b;
            Vertex vc = face.c;

            ret_val.indexes[idx++] = va.id;
            ret_val.indexes[idx++] = vb.id;
            ret_val.indexes[idx++] = vc.id;
        }

        idx = 0;
        for(Vertex vert : vertices) {
            ret_val.coordinates[idx++] = vert.x;
            ret_val.coordinates[idx++] = vert.y;
            ret_val.coordinates[idx++] = vert.z;
        }

        ret_val.vertexCount = vertices.size();
        ret_val.indexesCount = ret_val.indexes.length;

        return ret_val;
    }

}
