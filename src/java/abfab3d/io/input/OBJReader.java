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

package abfab3d.io.input;

import java.util.Vector;
import javax.vecmath.Vector3d;

import abfab3d.core.TriangleCollector;
import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.Vec;
import abfab3d.core.VecTransform;
import abfab3d.core.Transformer;

import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.stl.STLFileReader;

import org.web3d.vrml.lang.VRMLException;

import java.io.*;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.StringTokenizer;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
 STL files reader. Triangles from STL fiule are passed to TriangleCollector

 @author Vladimir Bulatov
 */


/**
 * Class to read collection of OBJ file and output colleciton of triangles 
 *
 * @author Vladimir Bulatov
 */
public class OBJReader implements TriangleProducer, AttributedTriangleProducer, Transformer {

    static final boolean DEBUG = false;

    protected double m_scale = 1. / 1000.; //to convert from millimeters into meters

    /**
     * Transformation to apply to vertices, null for none
     */
    private VecTransform m_transform;

    /**
     * path to file to read from
     */
    private String m_path;
    private InputStream m_is;

    public void setScale(double scale) {
        m_scale = scale;
    }

    public OBJReader() {
    }

    public OBJReader(String path) {
        m_path = path;
    }

    public OBJReader(InputStream is) {
        m_is = is;
    }

    public void setPath(String path) {
        m_path = path;
    }

    /**
     * Set the transform.
     *
     * @param transform The transform or null for identity.
     */
    public void setTransform(VecTransform transform) {
        m_transform = transform;
    }

    /**
     * Read an STL file and output triangles to the collector.
     *
     * @param path File to read
     * @param out  Destination
     * @throws IOException
     */
    public void read(String path, TriangleCollector out) throws IOException {

        readStream(makeInputStream(path), out); 

    }

    /**
     * Read an OBJ file and output triangles to the collector. 
     *
     * @param is File to read
     * @param out  Destination
     * @throws IOException
     */
    public void read(InputStream is, TriangleCollector out) throws IOException {


    }

    /**
     * interface TriangleProducer
     */
    public boolean getTriangles(TriangleCollector out) {
        try {
           
            if (m_is != null) {

                readStream(m_is,out);

            } else {
                
                readStream(makeInputStream(m_path), out);
            }

            return true;

        } catch (Exception e) {
            throw new RuntimeException(fmt("Exception while reading STL file:%s\n", m_path), e);
        }
    }

    /**
     * interface TriangleProducer
     */
    public boolean getAttTriangles(AttributedTriangleCollector out) {
        try {

            if (m_is != null) {

                readStream(m_is,new TriangleCollectorConverter(out));

            } else {

                readStream(makeInputStream(m_path), new TriangleCollectorConverter(out));
            }

            return true;

        } catch (Exception e) {
            throw new RuntimeException(fmt("Exception while reading STL file:%s\n", m_path), e);
        }
    }

    /**
       @override
     */
    public int getDataDimension() {
        return 3;
    }

    protected DataInputStream makeInputStream(String path) throws IOException{
        
        InputStream bis = null;
        
        if (path.lastIndexOf(".gz") > -1) {
            bis = new GZIPInputStream(new FileInputStream(path), (1 << 14));
        } else {
            bis = new BufferedInputStream(new FileInputStream(path), (1 << 14));
        }
        DataInputStream data = new DataInputStream(bis);
        return data;
    }

    /**
     * Read in a file and apply the specified transform.
     *
     * @param bis File to read
     * @param out  Destination
     * @throws IOException
     */
    private void readStream(InputStream is, TriangleCollector out) throws IOException {

        long t0 = time();
        if (DEBUG) {
            printf("OBJReader.read(%s, %s)\n", is, out);
            t0 = time();
        }
        BufferedReader br  = new BufferedReader(new InputStreamReader(is));
        Vector<Vector3d> vertices = new Vector<Vector3d>();

        while(true) {

            String line = br.readLine();
            if(line == null) break;
            readLine(line, vertices, out);

        }        

        printf("OBJReader.read() done in %d ms\n", (time() - t0));
    }

    void readLine(String line, Vector<Vector3d> vertices, TriangleCollector out){

        if(line.startsWith("v ")){
            // vertex 
            readVertex(line, vertices);
        } else if(line.startsWith("f ")){
            //face 
            readFace(line, vertices, out);
        }
    }
    
    void readFace(String line, Vector<Vector3d> vertices, TriangleCollector out){
        StringTokenizer st = new StringTokenizer(line, "f ", false);
        int v0 = 0,v1 = 0,v2 = 0;
        if(!st.hasMoreTokens()) return;
        v0 = Integer.parseInt(st.nextToken())-1;
        if(!st.hasMoreTokens()) return;
        v1 = Integer.parseInt(st.nextToken())-1;
        if(!st.hasMoreTokens()) return;
        v2 = Integer.parseInt(st.nextToken())-1;
                
        out.addTri(vertices.get(v0), vertices.get(v1), vertices.get(v2));
        // convert polygin into fan of triangles 
        while(st.hasMoreTokens()){
            v1 = v2;
            v2 = Integer.parseInt(st.nextToken())-1;
            out.addTri(vertices.get(v0), vertices.get(v1), vertices.get(v2));
        }
        
    }

    void readVertex(String line, Vector<Vector3d> vertices){
        
        StringTokenizer st = new StringTokenizer(line, "v ", false);
        double x = 0,y = 0,z = 0;
        if(!st.hasMoreTokens()) return;
        x = Double.parseDouble(st.nextToken());
        if(!st.hasMoreTokens()) return;
        y = Double.parseDouble(st.nextToken());
        if(!st.hasMoreTokens()) return;
        z = Double.parseDouble(st.nextToken());

        Vector3d v = new Vector3d(m_scale*x,m_scale*y,m_scale*z);
        Vec vv = new Vec(3);
        if(m_transform != null){
            vv.set(v);
            //printf("v: %7.5f %7.5f %7.5f ", v.x, v.y, v.z);
            m_transform.transform(vv, vv);
            vv.get(v);
            //    printf(" -> v: %7.5f %7.5f %7.5f\n", v.x, v.y, v.z);
        }
        
        vertices.add(v);
    }

    static class TriangleCollectorConverter implements TriangleCollector {

        AttributedTriangleCollector out;
        Vec vv0 = new Vec(3);
        Vec vv1 = new Vec(3);
        Vec vv2 = new Vec(3);
        TriangleCollectorConverter(AttributedTriangleCollector out){
            this.out = out;
        }

        public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
            vv0.set(v0);
            vv1.set(v1);
            vv2.set(v2);
            return out.addAttTri(vv0, vv1, vv2);
            
        }
        
    }

}

