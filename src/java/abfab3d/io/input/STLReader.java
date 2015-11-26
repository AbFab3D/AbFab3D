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

import abfab3d.util.TriangleCollector;
import abfab3d.util.TriangleProducer;
import abfab3d.util.Vec;
import abfab3d.util.VecTransform;
import abfab3d.util.Transformer;

import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.stl.STLFileReader;
import org.web3d.vrml.lang.VRMLException;

import javax.vecmath.Vector3d;
import java.io.*;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static java.lang.System.currentTimeMillis;

/**
 STL files reader. Triangles from STL fiule are passed to TriangleCollector

 @author Vladimir Bulatov
 */


/**
 * Class to read collection of triangles from STL file
 *
 * @author Vladimir Bulatov
 */
public class STLReader implements TriangleProducer, Transformer {

    static final boolean DEBUG = false;

    public double scale = 1. / 1000.; //to convert form STL standard millimeters into meters
    private TriangleCollector out;

    /**
     * Transformation to apply to vertices, null for none
     */
    private VecTransform transform;

    /**
     * path to file to read from
     */
    private String m_path;
    private InputStream m_is;

    public void setScale(double scale) {
        this.scale = scale;
    }

    private int readInt(DataInputStream data) throws IOException {

        int i = data.readUnsignedByte() | (data.readUnsignedByte() << 8) |
                (data.readUnsignedByte() << 16) | (data.readUnsignedByte() << 24);

        return i;
    }

    private float readFloat(DataInputStream data) throws IOException {

        //return data.readFloat();
        int i = data.readUnsignedByte() | (data.readUnsignedByte() << 8) |
                (data.readUnsignedByte() << 16) | (data.readUnsignedByte() << 24);
        return Float.intBitsToFloat(i);

    }

    private void readVector3Df(DataInputStream data, Vec v) throws IOException {

        double x = readFloat(data) * scale;
        double y = readFloat(data) * scale;
        double z = readFloat(data) * scale;

        v.set(x, y, z);
    }

    private void readVector3Df(DataInputStream data, Vector3d v) throws IOException {

        v.x = readFloat(data) * scale;
        v.y = readFloat(data) * scale;
        v.z = readFloat(data) * scale;
    }

    public STLReader() {
    }

    public STLReader(String path) {
        m_path = path;
    }

    public STLReader(InputStream is) {
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
        this.transform = transform;
    }

    /**
     * Read an STL file and output triangles to the collector.
     *
     * @param path File to read
     * @param out  Destination
     * @throws IOException
     */
    public void read(String path, TriangleCollector out) throws IOException {

        if (out == null) {
            return;
        }

        boolean ascii = isAscii(path);

        try {
            if (ascii) {
                readAscii(path, out);            
            } else { // binary 
                readBinary(makeInputStream(path), out);
            }
        } catch(InvalidFormatException ife) {
            if (ascii) {
                // try binary as some binary files have solid in their header
                readBinary(makeInputStream(path), out);
            }
        }
    }

    /**
     * Read an STL file and output triangles to the collector. Assumes binary
     *
     * @param is File to read
     * @param out  Destination
     * @throws IOException
     */
    public void read(InputStream is, TriangleCollector out) throws IOException {

        try {
            readBinary(is, out);           
        } catch(InvalidFormatException ife) {
            throw new IOException("Cannot parse file: " + ife.getMessage());
        }
    }

    /**
     * interface TriangleProducer
     */
    public boolean getTriangles(TriangleCollector out) {
        try {
           
            if (m_is != null) {

                readBinary(m_is,out);

            } else {
                
                readBinary(makeInputStream(m_path), out);
            }

            return true;

        } catch (Exception e) {
            throw new RuntimeException(fmt("Exception while reading STL file:%s\n", m_path), e);
        }
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
    private void readBinary(InputStream bis, TriangleCollector out) throws IOException {
        long t0;

        if (DEBUG) {
            printf("STLReader.read(%s, %s)\n", bis, out);
            t0 = currentTimeMillis();
        }

        this.out = out;

        DataInputStream data = new DataInputStream(bis);

        data.skip(80);

        int fcount = readInt(data);
        if (DEBUG)
            printf("fcount: %d\n", fcount);
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
            while (true) {
                // ignore normal
                data.skip(3 * 4);
                readVector3Df(data, dv0);
                readVector3Df(data, dv1);
                readVector3Df(data, dv2);

                if(transform != null){
                    v0.set(dv0);
                    v1.set(dv1);
                    v2.set(dv2);
                    transform.transform(v0, v0);
                    transform.transform(v1, v1);
                    transform.transform(v2, v2);
                    v0.get(dv0);
                    v1.get(dv1);
                    v2.get(dv2);
                }

                out.addTri(dv0, dv1, dv2);

                data.skip(2); // unsused stuff
                faces++;
            }
        } catch (Exception e) {
            data.close();

            if (DEBUG)
                printf("faces read: %d\n", faces);
        }
        if (DEBUG)
            printf("STLReader.read() done in %d ms\n", (currentTimeMillis() - t0));

    }

    /**
     * Read in a file and apply the specified transform.
     *
     * @param path File to read
     * @param out  Destination
     * @throws IOException
     */
    private void readAscii(String path, TriangleCollector out) throws IOException {
        long t0;

        if (DEBUG) {
            printf("STLReader.read(%s, %s)\n", path, out);
            t0 = currentTimeMillis();
        }

        this.out = out;

        InputStream bis = null;

        // TODO: how to add gzip support?
        /*
        if (path.lastIndexOf(".gz") > -1) {
            bis = new GZIPInputStream(new FileInputStream(path), (1 << 14));
        } else {
            bis = new BufferedInputStream(new FileInputStream(path), (1 << 14));
        }
        */
        STLFileReader reader = null;

        try {
            File f = new File(path);
            reader = new STLFileReader(new URL(f.toURI().toString()), false);
        } catch (InvalidFormatException ife) {
            String msg = ife.getMessage();
            System.out.println("Error: " + msg);

            return;
        }

        generateTriangles(reader, transform, out);
    }

    /**
     * Generate the coordinate and normal information for the TriangleSet node
     * based on that read from the STL file.
     */
    private void generateTriangles(STLFileReader rdr, VecTransform transform, TriangleCollector out)
            throws IOException, VRMLException {

        int num_objects = rdr.getNumOfObjects();
        int[] num_tris = rdr.getNumOfFacets();
        String[] obj_names = rdr.getObjectNames();
        int max_tris = 0;

        // Copy locally for speed and avoid any MT weirdness
        double unit_scale = scale;

        for (int j = 0; j < num_objects; j++) {
            if (num_tris[j] > max_tris)
                max_tris = num_tris[j];
        }

        if (max_tris == 0) {
            return;
        }

        if (num_objects == 1) {
            // Special case 1 object to read as many triangles as possible for binary miscounts
            num_tris[0] = Integer.MAX_VALUE;
        }

        double[] in_normal = new double[3];
        double[][] in_coords = new double[3][3];

        Vec
                v0 = new Vec(3),
                v1 = new Vec(3),
                v2 = new Vec(3);


        Vector3d
                dv0 = new Vector3d(),
                dv1 = new Vector3d(),
                dv2 = new Vector3d();


        for (int i = 0; i < num_objects; i++) {
            if (num_tris[i] == 0)
                continue;

            for (int j = 0; j < num_tris[i]; j++) {
                if (!rdr.getNextFacet(in_normal, in_coords)) {
                    break;
                }

                dv0.set(in_coords[0][0] * unit_scale, in_coords[0][1] * unit_scale, in_coords[0][2] * unit_scale);
                dv1.set(in_coords[1][0] * unit_scale, in_coords[1][1] * unit_scale, in_coords[1][2] * unit_scale);
                dv2.set(in_coords[2][0] * unit_scale, in_coords[2][1] * unit_scale, in_coords[2][2] * unit_scale);

                if(transform != null){
                    v0.set(dv0);
                    v1.set(dv1);
                    v2.set(dv2);
                    
                    transform.transform(v0, v0);
                    transform.transform(v1, v1);
                    transform.transform(v2, v2);
                    
                    v0.get(dv0);
                    v1.get(dv1);
                    v2.get(dv2);
                }

                out.addTri(dv0, dv1, dv2);
            }
        }
    }

    /**
     * Is the stl file an ascii file.  Determined by starting line of solid.
     *
     * @param path
     * @return
     */
    private boolean isAscii(String path) throws IOException {

        BufferedReader reader = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis);
            reader = new BufferedReader(isr);

            String line = reader.readLine();
            line = line.trim();  // "Spec" says whitespace maybe anywhere except within numbers or words.  Great design!

            while(line.length() == 0) {
                line = reader.readLine();
                line = line.trim();  // "Spec" says whitespace maybe anywhere except within numbers or words.  Great design!
            }

            // check if ASCII format
            if (line.startsWith("solid")) {
                return true;
            } else {
                return false;
            }
        } finally {
            if (fis != null) {
                fis.close();
            }

            if (isr != null) {
                isr.close();
            }

            if (reader != null) {
                reader.close();
            }
        }
    }
}

