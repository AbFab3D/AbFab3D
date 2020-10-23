/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.VecTransform;
import abfab3d.core.Initializable;
import abfab3d.core.Vec;

import javax.vecmath.Vector3d;

import static abfab3d.core.Output.printf;

/**
 * produces transformed triagles from input trinagles
 *
 * @author Vladimir Bulatov
 */
public class TriangleTransformer implements TriangleCollector, TriangleProducer {

    static final boolean DEBUG = false;
    VecTransform m_transform = null;
    TriangleProducer m_producer = null;
    TriangleCollector m_collector = null;

    public TriangleTransformer(TriangleProducer producer){
        m_producer = producer;
    }

    public TriangleTransformer(VecTransform transform){
        setTransform(transform);
    }

    public void setTransform(VecTransform transform){

        m_transform = transform;
        if(m_transform instanceof Initializable){
            ((Initializable)m_transform).initialize();
        }

    }

    public void setTriangleProducer(TriangleProducer tp){
        m_producer = tp;
    }
  
    /**
       add triangle 
       vertices are copied into internal structure and can be reused after return       

       returns true if success, false if faiure 
       
     */
    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2){
        
        Vec in = new Vec(3);
        Vec out = new Vec(3);
        Vector3d 
            tv0 = new Vector3d(v0),
            tv1 = new Vector3d(v1),
            tv2 = new Vector3d(v2);
        
        in.set(v0);
        m_transform.transform(in, out);
        out.get(tv0);
        
        in.set(v1);
        m_transform.transform(in, out);
        out.get(tv1);

        in.set(v2);
        m_transform.transform(in, out);
        out.get(tv2);
        
        m_collector.addTri(tv0, tv1, tv2);

        return true;
    }

    public boolean getTriangles(TriangleCollector tc){

        m_collector = tc;
        m_producer.getTriangles(this);

        return true;
    }
}

