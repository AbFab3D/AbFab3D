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

import javax.vecmath.Vector3d;

/**
   converts AttributedTriangleProducer into TriangleProducer 
   
   accept triangles from AttributedTriangleProducer and spretends to be TriangleProducer

   @author Vladimir Bulatov
 */
public class AttributedTriangleProducerConverter implements TriangleProducer, AttributedTriangleCollector {

    // producer to get traingles from 
    AttributedTriangleProducer atp;
    // collector to send triamngles to 
    TriangleCollector tc;

    /**
       
     */
    public AttributedTriangleProducerConverter(AttributedTriangleProducer atp){
        this.atp = atp;
    }


    /**
       feeds all triangles into supplied TriangleCollector 

       returns true if success, false if faiure        
     */
    public boolean getTriangles(TriangleCollector tc){
        this.tc = tc;
        return atp.getAttTriangles(this);
    }

    Vector3d // work vectors
        w0 = new Vector3d(),
        w1 = new Vector3d(),
        w2 = new Vector3d();


    /**
       method of interace AttributedTriangleCollector which accept non attributed triangles 
     */
    public boolean addAttTri(Vec p0, Vec p1, Vec p2){

        p0.get(w0);
        p1.get(w1);
        p2.get(w2);

        return tc.addTri(w0, w1, w2);
    }
    
}
