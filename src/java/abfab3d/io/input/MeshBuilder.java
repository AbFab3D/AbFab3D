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

import javax.vecmath.Vector3d;

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.VecTransform;
import abfab3d.core.Vec;
import abfab3d.core.Transformer;
import abfab3d.core.Initializable;


import abfab3d.util.PointSet;
import abfab3d.util.PointSetArray;

import static abfab3d.core.Output.printf;

/**
 * builds triangle mesh from individual triangles and 
 *
 * @author Vladimir Bulatov
 */
public class MeshBuilder extends BaseMeshReader implements TriangleProducer, TriangleCollector, Transformer {

    private static final boolean DEBUG = false;

    private VecTransform m_transform;
    private TriangleProducer m_producer;

    PointSet m_pnts;
    
    public MeshBuilder() {

        m_pnts = new PointSetArray();

    }

    public int getCount(){

        return m_pnts.size()/3;

    }


    /**
       add triangles from another source to this mesh 
       @param producer of triangles 
     */
    public boolean append(TriangleProducer producer){

        return producer.getTriangles(this);

    }
    
    /**

       @override 
     */
    public boolean getTriangles(TriangleCollector out) {
        
        int triCount = m_pnts.size()/3;
        Vector3d v0 = new Vector3d();
        Vector3d v1 = new Vector3d();
        Vector3d v2 = new Vector3d();
        if(DEBUG) printf("MeshBuilder.getTriangles()\n");
        if(DEBUG) printf("  triCount: %d\n", triCount);

        for(int i = 0; i < triCount; i++){
            m_pnts.getPoint(3*i, v0);
            m_pnts.getPoint(3*i+1,v1);
            m_pnts.getPoint(3*i+2,v2);
            if(false) printf("  tri:(%7.5f, %7.5f,%7.5f)(%7.5f, %7.5f,%7.5f)(%7.5f, %7.5f,%7.5f)\n", 
                             v0.x,v0.y,v0.z,v1.x,v1.y,v1.z,v2.x,v2.y,v2.z);            
            if(m_transform != null) {
                transform(v0);
                transform(v1);
                transform(v2);
            }
            out.addTri(v0, v1, v2);
        }
        return true;
         
    }

    protected void transform(Vector3d p){
        Vec v = new Vec(6);
        v.set(p);
        m_transform.transform(v, v);
        v.get(p);
    
    }
    
    /**

       @override 
     */
    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2) {

        m_pnts.addPoint(v0.x,v0.y,v0.z);
        m_pnts.addPoint(v1.x,v1.y,v1.z);
        m_pnts.addPoint(v2.x,v2.y,v2.z);

        return true;
    }

    /**

       @override 
     */
    public void setTransform(VecTransform trans) {

        m_transform = trans;
        if(m_transform instanceof Initializable)
            ((Initializable)m_transform).initialize();
    }
    
}