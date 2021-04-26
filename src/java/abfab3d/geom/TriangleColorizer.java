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

package abfab3d.geom;

// External Imports
import java.util.*;


import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.core.Vec;
import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.Transformer;
import abfab3d.core.Initializable;
import abfab3d.core.ResultCodes;
import abfab3d.core.VecTransform;
import abfab3d.core.DataSource;


import abfab3d.util.TriangleTransformer;
import abfab3d.core.MathUtil;


import static abfab3d.core.Output.printf;




/**
 *  applies color to the input triangles via and outputs AttributedTriangle 
 *
 * 
 * @author Vladimir Bulatov
 */
public class TriangleColorizer implements AttributedTriangleProducer, TriangleCollector, Initializable {

    static final boolean DEBUG = true;


    TriangleProducer m_triProducer;
    DataSource m_colorizer;
    boolean m_initialized = false;
    int m_dataDimension;


    AttributedTriangleCollector m_attTriCollector;


    public TriangleColorizer(TriangleProducer triProducer, DataSource colorizer){

        m_colorizer = colorizer;
        m_triProducer = triProducer;

    }


    public int getDataDimension(){
        initialize(); 
        return m_dataDimension;
    }
        
    /**
       return AttributedTriangles to triagles 
     */
    public boolean getAttTriangles(AttributedTriangleCollector attTriCollector){

        initialize();

        m_attTriCollector = attTriCollector;

        m_triProducer.getTriangles(this);

        return true;

    }
    
    
    /**

       
     */
    public int initialize(){

        if(m_initialized) return ResultCodes.RESULT_OK;
        m_initialized = true;

        MathUtil.initialize(m_triProducer);
        MathUtil.initialize(m_colorizer);
        
        int chanCnt = m_colorizer.getChannelsCount();
        m_dataDimension = 3 + chanCnt;
        
        m_v = new Vec(3);
        m_c = new Vec(chanCnt);
        if(DEBUG)printf("TriangleColorizer.initialize() dataDimension: %d\n", m_dataDimension);
        m_t0 = new Vec(m_dataDimension);
        m_t1 = new Vec(m_dataDimension);
        m_t2 = new Vec(m_dataDimension);

        return ResultCodes.RESULT_OK;
    }

    Vec m_v;
    Vec m_c;
    Vec m_t0, m_t1, m_t2;

    /**
       
     */    
    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
        
        int dataOffset = 3;
        //if(DEBUG) printf("TriangleColorizer.addTri()t:%d v:%d, c:%d\n", m_t0.v.length, m_v.v.length, m_c.v.length);
        m_v.set(v0);
        m_colorizer.getDataValue(m_v, m_c);        
        m_t0.set(m_v); 
        m_t0.setAt(m_c,dataOffset);
        
        m_v.set(v1);
        m_colorizer.getDataValue(m_v, m_c);
        m_t1.set(m_v); 
        m_t1.setAt(m_c,dataOffset);

        m_v.set(v2);
        m_colorizer.getDataValue(m_v, m_c);
        m_t2.set(m_v); 
        m_t2.setAt(m_c,dataOffset);
        m_attTriCollector.addAttTri(m_t0, m_t1, m_t2);
        return true;
    }
} // TriangleColorizer

