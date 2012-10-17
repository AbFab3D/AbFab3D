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

// External Imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;


import org.web3d.vrml.sav.BinaryContentHandler;

import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.io.input.STLReader;
import abfab3d.io.input.STLRasterizer;
import abfab3d.io.input.MeshRasterizer;
import abfab3d.io.output.SAVExporter;
import abfab3d.io.output.MeshExporter;
import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;


import abfab3d.grid.Grid;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.op.ErosionMask;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.j3d.geom.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.*;


import static abfab3d.util.Output.printf; 
import static abfab3d.util.Output.fmt; 

import static java.lang.System.currentTimeMillis;

/**
 * Tests the functionality of TestLaplasianSmooth
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestLaplasianSmooth extends TestCase {
    
    static final double MM = 1000; // m -> mm conversion 
    static final double MM3 = 1.e9; // m^3 -> mm^3 conversion 

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestLaplasianSmooth.class);
    }

    public void testFile() throws Exception {
        
        printf("TestLaplasianSmooth.testFile()\n");

        //String fpath = "c:/tmp/ring_numbers_00.stl";
        String fpath = "c:/tmp/ring_numbers_45.stl";
        //String fpath = "c:/tmp/ring_numbers_90.stl";

        int iterationCount = 10;
        double centerWeight = 1.0; // any non negative value is OK 

        long t0 = currentTimeMillis();
        WingedEdgeTriangleMesh mesh = loadMesh(fpath);
        printf("mesh loading: %d ms\n",(currentTimeMillis() - t0));
        t0 = currentTimeMillis();

        printf("mesh vertices: %d, edges: %d\n", mesh.getVertexCount(), mesh.getEdgeCount());        

        LaplasianSmooth ls = new LaplasianSmooth();

        ls.setCenterWeight(centerWeight);

        ls.DEBUG = false;
        mesh.DEBUG = false; 
        t0 = currentTimeMillis();
        for(int i = 0; i < iterationCount; i++){
            printf("processMesh()\n");
            t0 = currentTimeMillis();
            ls.processMesh(mesh, 10);
            printf("mesh processed: %d ms\n",(currentTimeMillis() - t0));

            MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_smooth_%d.stl", i));
            
        }
    }

    /**
       
     */
    public static WingedEdgeTriangleMesh loadMesh(String fpath){
        if(fpath.toLowerCase().lastIndexOf(".stl") > 0){
            return loadSTL(fpath);
        } else {
            return loadX3D(fpath);
        }
    }

    /**
       load STL file 
     */
    public static WingedEdgeTriangleMesh loadSTL(String fpath){

        STLReader reader = new STLReader();
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        try {
            reader.read(fpath, its);
            return new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    

    /**
       load X3D file
     */
    public static WingedEdgeTriangleMesh loadX3D(String fpath){
        
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        
        loader.processFile(new File(fpath));
        
        GeometryData data = new GeometryData();
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Vector3d[] verts = new Vector3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;        
        
        for(int i=0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Vector3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        idx = 0;
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        for(int i=0; i < len; i++) {
            its.addTri(verts[data.indexes[idx++]],verts[data.indexes[idx++]],verts[data.indexes[idx++]]);
        }
        
        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        return we;

    }

}

