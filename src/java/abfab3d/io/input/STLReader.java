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

import java.io.*;
import java.util.zip.GZIPInputStream;


import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;
import abfab3d.util.TriangleCollector;
import abfab3d.util.TriangleProducer;
import abfab3d.util.Vec;
import abfab3d.util.VecTransform;

import static java.lang.System.currentTimeMillis;
import static abfab3d.util.Output.fmt;

/**
   STL files reader. Triangles from STL fiule are passed to TriangleCollector 

   @author Vladimir Bulatov
 */
import static abfab3d.util.Output.printf; 


/**
   Class to read collection of triangles from STL file

   @author Vladimir Bulatov
 */
public class STLReader  implements TriangleProducer {

    static final boolean DEBUG = false;

    public static final double SCALE = 1./1000.; //to convert form STL standard millimeters into meters
    private TriangleCollector out;

    /** Transformation to apply to vertices, null for none */
    private VecTransform transform;

    /**
       path to file to read from 
     */
    private String m_path; 

    public static int readInt(DataInputStream data) throws IOException{
        
        int i = data.readUnsignedByte() | (data.readUnsignedByte()<<8)|
            (data.readUnsignedByte()<<16)|(data.readUnsignedByte()<<24);      
        
        return i;
    }
    
    public static float readFloat(DataInputStream data) throws IOException{
        
        //return data.readFloat();
        int i = data.readUnsignedByte() | (data.readUnsignedByte()<<8)|
            (data.readUnsignedByte()<<16)|(data.readUnsignedByte()<<24);      
        return Float.intBitsToFloat(i);
        
    }
    
    public static void readVector3Df(DataInputStream data, Vec v) throws IOException{

        double x = readFloat(data)*SCALE;
        double y = readFloat(data)*SCALE;
        double z = readFloat(data)*SCALE;

        v.set(x,y,z);
    }

    public static void readVector3Df(DataInputStream data, Vector3d v) throws IOException{

        v.x = readFloat(data)*SCALE;
        v.y = readFloat(data)*SCALE;
        v.z = readFloat(data)*SCALE;
    }

    public STLReader(){        
    }

    public STLReader(String path){        
        m_path = path;
    }

    public void setPath(String path){
        m_path = path;
    }

    /**
     * Set the transform.
     *
     * @param transform The transform or null for identity.
     */
    public void setTransform(VecTransform transform) {
        this.transform = transform;
    }

    /**
     * Read an STL file and output triangles to the collector.
     *
     * @param path File to read
     * @param out Destination
     * @throws IOException
     */
    public void read(String path, TriangleCollector out) throws IOException {

        if (out == null) {
            return;
        }

        if (transform == null) {
            readNoTransform(path, out);
        } else {
            readTransform(path,out);
        }
    }

    /**
       interface TriangleProducer
     */
    public boolean getTriangles(TriangleCollector out) {
        try {

            read(m_path, out);
            return true;

        } catch(Exception e){
            throw new RuntimeException(fmt("Exception while reading STL file:%s\n", m_path), e);
        }        
    }

    /**
     * Read in a file.
     *
     * @param path File to read
     * @param out Destination
     * @throws IOException
     */
    private void readNoTransform(String path, TriangleCollector out) throws IOException {
        long t0;

        if(DEBUG) {
            printf("STLReader.read(%s, %s)\n", path, out);
            t0 = currentTimeMillis();
        }
        
        this.out = out;

        InputStream bis = null;

        if (path.lastIndexOf(".gz") > -1) {
            bis = new GZIPInputStream(new FileInputStream(path), (1 << 14));
        } else {
            bis = new BufferedInputStream(new FileInputStream(path), (1 << 14));
        }
        DataInputStream data = new DataInputStream(bis);
        
        data.skip(80);
        
        int fcount = readInt(data);      
        if(DEBUG)
            printf("fcount: %d\n",fcount);
        int faces = 0;

        Vector3d
                v0 = new Vector3d(),
                v1 = new Vector3d(),
                v2 = new Vector3d();

        try {
            while(true) {
                // ignore normal 
                data.skip(3*4);
                readVector3Df(data, v0);                
                readVector3Df(data, v1);                
                readVector3Df(data, v2);                
                out.addTri(v0,v1, v2);
                
                data.skip(2); // unsused stuff 
                faces++;                
            }
        } catch(Exception e){
            data.close();
            
            if(DEBUG)
                printf("faces read: %d\n", faces);
        }
        if(DEBUG)
            printf("STLReader.read() done in %d ms\n", (currentTimeMillis() - t0));
    }

    /**
     * Read in a file and apply the specified transform.
     *
     * @param path File to read
     * @param out Destination
     * @throws IOException
     */
    private void readTransform(String path, TriangleCollector out) throws IOException {
        long t0;

        if(DEBUG) {
            printf("STLReader.read(%s, %s)\n", path, out);
            t0 = currentTimeMillis();
        }

        this.out = out;

        InputStream bis = null;

        if (path.lastIndexOf(".gz") > -1) {
            bis = new GZIPInputStream(new FileInputStream(path), (1 << 14));
        } else {
            bis = new BufferedInputStream(new FileInputStream(path), (1 << 14));
        }
        DataInputStream data = new DataInputStream(bis);

        data.skip(80);

        int fcount = readInt(data);
        if(DEBUG)
            printf("fcount: %d\n",fcount);
        int faces = 0;

        Vec
            v0 = new Vec(3),
            v1 = new Vec(3),
            v2 = new Vec(3);
        
        Vector3d 
            dv0 = new Vector3d(),
            dv1 = new Vector3d(),
            dv2 = new Vector3d();
        
        try {
            while(true) {
                // ignore normal
                data.skip(3*4);
                readVector3Df(data, dv0);
                readVector3Df(data, dv1);
                readVector3Df(data, dv2);

                v0.set(dv0);
                v1.set(dv1);
                v2.set(dv2);
                
                transform.transform(v0,v0);
                transform.transform(v1,v1);
                transform.transform(v2,v2);
                
                // TODO: I don't like having to change vector reps
                v0.get(dv0);
                v1.get(dv1);
                v2.get(dv2);            

                out.addTri(dv0,dv1,dv2);

                data.skip(2); // unsused stuff
                faces++;
            }
        } catch(Exception e){
            data.close();

            if(DEBUG)
                printf("faces read: %d\n", faces);
        }
        if(DEBUG)
            printf("STLReader.read() done in %d ms\n", (currentTimeMillis() - t0));

    }
}

