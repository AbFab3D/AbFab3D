package abfab3d.io.output;

import abfab3d.core.AttributeGrid;
import abfab3d.core.AttributePacker;
import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.DataSource;
import abfab3d.core.MathUtil;
import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;
import junit.framework.TestCase;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import javax.vecmath.Vector3d;

//import java.io.*;
//import java.util.zip.ZipInputStream;
//import java.util.zip.ZipOutputStream;



import static abfab3d.core.MathUtil.step10;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Test for X3D triangles writer
 *
 * @author Vladimir Bulatov
 */
public class TestX3DWriter extends TestCase {

    public void testNothing() {

    }


    void devTestWriteTriangle(String path)throws Exception {

        X3DWriter writer = new X3DWriter(path);
        writer.setWriteColoredMesh(true);
        writer.addAttTri(new Vec(1,0,0, 0,1,1),new Vec(0,1,0, 1,0,1),new Vec(0,0,1, 1,1,0));        
        writer.addAttTri(new Vec(-1,0,0, 0,1,1),new Vec(0,1,0, 1,0,1),new Vec(0,0,1, 1,1,0));        

        writer.close();
    }

    void devTestWriteTetrahedron(String path)throws Exception {

        X3DWriter writer = new X3DWriter(path);
        writer.setWriteColoredMesh(true);
        double a = 0.5;
        writer.addAttTri(new Vec(a,a,a, 1,0,0),new Vec(-a, a, -a, 1,0,0),new Vec(-a,-a, a, 1,0,0));  
        writer.addAttTri(new Vec(a,a,a, 0,1,0),new Vec(-a,-a,  a, 0,1,0),new Vec(a, -a,-a, 0,1,0));  
        writer.addAttTri(new Vec(a,a,a, 0,0,1),new Vec( a,-a, -a, 0,0,1),new Vec(-a, a,-a, 0,0,1));  
        writer.addAttTri(new Vec(-a,-a,a, 1,1,0),new Vec( -a, a,-a, 1,1,0),new Vec(a,-a,-a, 1,1,0));  

        writer.close();
    }
    

    void devTestWriteTetrahedron2(String path)throws Exception {

        X3DWriter writer = new X3DWriter(path);
        writer.setWriteColoredMesh(true);
        double a = 0.5;
        writer.addAttTri(new Vec(a,a,a, 1,0,0),new Vec(-a, a, -a, 1,0,0),new Vec(-a,-a, a, 1,0,0));  
        writer.addAttTri(new Vec(a,a,a, 0,1,0),new Vec(-a,-a,  a, 0,1,0),new Vec(a, -a,-a, 0,1,0));  
        writer.addAttTri(new Vec(a,a,a, 0,0,1),new Vec( a,-a, -a, 0,0,1),new Vec(-a, a,-a, 0,0,1));  
        writer.addAttTri(new Vec(-a,-a,a, 0,1,0),new Vec( -a, a,-a,1,0,0),new Vec(a,-a,-a, 0,0,1));  

        writer.close();
    }

    void devTestWriteTetrahedron2a(String path)throws Exception {

        X3DWriter writer = new X3DWriter(path);
        writer.setWriteColoredMesh(true);
        double a = 0.5;
        writer.addAttTri(new Vec(a,a,a, 1,0,0),new Vec(-a, a, -a, 1,0,0),new Vec(-a,-a, a, 1,0,0));  
        writer.addAttTri(new Vec(a,a,a, 0,1,0),new Vec(-a,-a,  a, 0,1,0),new Vec(a, -a,-a, 0,1,0));  
        writer.addAttTri(new Vec(a,a,a, 0,0,1),new Vec( a,-a, -a, 0,0,1),new Vec(-a, a,-a, 0,0,1));  
        //writer.addAttTri(new Vec(-a,-a,a, 0,1,0),new Vec( -a, a,-a,1,0,0),new Vec(a,-a,-a, 0,0,1));  

        writer.close();
    }

    void devTestWriteTetrahedron3(String path)throws Exception {
        X3DWriter writer = new X3DWriter(path);
        writer.setWriteColoredMesh(true);
        double a = 0.5;
        double b = 0.25;

        addTetra(writer, a, new Vector3d(0,0,0));
        addTetra(writer, a, new Vector3d(b,b,b));
        addTetra(writer, a, new Vector3d(-b,-b,-b));

        writer.close();
        
    }

    void devTestWriteTetrahedron4(String path)throws Exception {
        X3DWriter writer = new X3DWriter(path);
        writer.setWriteColoredMesh(true);
        double a = 0.5;
        double b = 0.5;
        int count = 20;
        for(int i = 0; i < count; i++){
            double x = i*b/count;
            addTetra(writer, a, new Vector3d(x,0,0));
        }
        writer.close();
        
    }

    static void addTetra(AttributedTriangleCollector tc, double a, Vector3d c){

        Vector3d c0 = new Vector3d(1,0,0);
        Vector3d c1 = new Vector3d(0,1,0);
        Vector3d c2 = new Vector3d(0,0,1);
        Vector3d v0 = new Vector3d(c.x + a,c.y + a,c.z + a);
        Vector3d v1 = new Vector3d(c.x - a,c.y + a,c.z - a);
        Vector3d v2 = new Vector3d(c.x - a,c.y - a,c.z + a);
        Vector3d v3 = new Vector3d(c.x + a,c.y - a,c.z - a);

        tc.addAttTri(new Vec(v0, c0),new Vec(v1, c0),new Vec(v2, c0));  
        tc.addAttTri(new Vec(v0, c1),new Vec(v2, c1),new Vec(v3, c1));  
        tc.addAttTri(new Vec(v0, c2),new Vec(v3, c2),new Vec(v1, c2));  
        tc.addAttTri(new Vec(v2, c1),new Vec(v1, c0),new Vec(v3, c2));  
        
    }

    public static void main(String args[])throws Exception{

        //new TestX3DWriter().devTestWriteTriangle("/tmp/triangle.x3db");
        //new TestX3DWriter().devTestWriteTetrahedron("/tmp/tetra.x3db");
        //new TestX3DWriter().devTestWriteTetrahedron2("/tmp/tetra2.x3db");
        //new TestX3DWriter().devTestWriteTetrahedron2("/tmp/tetra2.wrl");
        //new TestX3DWriter().devTestWriteTetrahedron2("/tmp/tetra2.x3d");
        //new TestX3DWriter().devTestWriteTetrahedron2("/tmp/tetra2.x3dv");
        //new TestX3DWriter().devTestWriteTetrahedron2("/tmp/tetra2.x3db");
        new TestX3DWriter().devTestWriteTetrahedron2a("/tmp/tetra2a.x3db");
        //new TestX3DWriter().devTestWriteTetrahedron3("/tmp/tetra3.x3db");
        //new TestX3DWriter().devTestWriteTetrahedron4("/tmp/tetra4.x3db");


    }

}
