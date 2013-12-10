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
import abfab3d.util.SysErrorReporter;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.sav.ErrorHandler;

import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.ArrayData;

import javax.vecmath.Vector3d;
import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.List;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static java.lang.System.currentTimeMillis;


/**
 * Class to read collection of triangles from X3D file
 * Triangles from STL file are passed to TriangleCollector
 *
 * @author Vladimir Bulatov
 */
public class X3DReader implements TriangleProducer, Transformer {

    static final boolean DEBUG = true;

    private TriangleCollector out;

    /**
     * Transformation to apply to vertices, null for none
     */
    private VecTransform m_transform;
    private X3DFileLoader m_fileLoader;
    /**
     * path to file to read from
     */
    private String m_path;

    public X3DReader(String path) {
        m_path = path;
    }

    /**
     * Set the transform to be applied to each triangle before passing it to TriangleCollector.
     *
     * @param transform The transform or null for identity.
     */
    public void setTransform(VecTransform transform) {
        this.m_transform = transform;
    }

    /**
       reads XML file and passes troiangles to TriangleCollector
     */
    private void read(TriangleCollector out) throws IOException {

        if(m_fileLoader == null){
            m_fileLoader = new X3DFileLoader(new SysErrorReporter(SysErrorReporter.PRINT_ERRORS));
            m_fileLoader.loadFile(new File(m_path));
        }
                
        List<CommonEncodable> shapes = m_fileLoader.getShapes();

        Iterator<CommonEncodable> itr = shapes.iterator();

        while(itr.hasNext()) {

            CommonEncodable shape = itr.next();
            CommonEncodable its = (CommonEncodable) shape.getValue("geometry");
            CommonEncodable coordNode = (CommonEncodable) its.getValue("coord");
            float[] coord = (float[]) ((ArrayData)coordNode.getValue("point")).data;
            int[] coordIndex = (int[]) ((ArrayData)its.getValue("index")).data;

            addTriangles(coord,coordIndex, out);
            
        }
        
    }
    
    /**
       send tiangles stored as indices to TriangleCollector
     */
    private void addTriangles(float coord[],int coordIndex[], TriangleCollector out){
        if(DEBUG)printf("%s.addTriangles(coord:%d, coordIndex:%d)\n", this,coord.length, coordIndex.length );
        // count of triangles 
        int len = coordIndex.length / 3;

        Vector3d 
            v0 = new Vector3d(),
            v1 = new Vector3d(),
            v2 = new Vector3d();

        for(int i=0, idx = 0; i < len; i++ ) {
            
            int off = coordIndex[idx++] * 3;
            
            v0.x = coord[off++];
            v0.y = coord[off++];
            v0.z = coord[off++];
            
            off = coordIndex[idx++] * 3;

            v1.x = coord[off++];
            v1.y = coord[off++];
            v1.z = coord[off++];
            
            off = coordIndex[idx++] * 3;
            v2.x = coord[off++];
            v2.y = coord[off++];
            v2.z = coord[off++];
            makeTransform(v0, v1, v2);
            out.addTri(v0, v1, v2);
        }
        
    }    
    
    // work vectors used for transformations
    Vec vv0 = new Vec(3), 
        vv1 = new Vec(3),
        vv2 = new Vec(3);
    
    final void makeTransform(Vector3d v0, Vector3d v1, Vector3d v2){
        
        if(m_transform == null)
            return;
        
        vv0.set(v0);
        vv1.set(v1);
        vv2.set(v2);
        
        m_transform.transform(vv0, vv0);
        m_transform.transform(vv1, vv1);
        m_transform.transform(vv2, vv2);
        
        vv0.get(v0);
        vv1.get(v1);
        vv2.get(v2);

    }

    
    /**
     * interface TriangleProducer
     */
    public boolean getTriangles(TriangleCollector out) {
        try {

            read(out);
            return true;

        } catch (Exception e) {
            throw new RuntimeException(fmt("Exception while reading file:%s\n", m_path), e);
        }
    }

}

