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
   convert TriangleProducer into TriangleProducer2 
   
   accept triangles from TriangleProducer and sends them to TriangleCollector2 

   @author Vladimir Bulatov
 */
public class TriangleProducerConverter2 implements TriangleProducer2, TriangleCollector {

    // producer to get traingles from 
    TriangleProducer tp;
    // collector to send triamngles to 
    TriangleCollector2 tc2;

    /**
       
     */
    public TriangleProducerConverter2(TriangleProducer tp){
        this.tp = tp;
    }


    /**
       feeds all triangles into supplied TriangleCollector 

       returns true if success, false if faiure        
     */
    public boolean getTriangles2(TriangleCollector2 tc2){
        this.tc2 = tc2;
        return tp.getTriangles(this);
    }

    Vec // work vectors
        w0 = new Vec(6),
        w1 = new Vec(6),
        w2 = new Vec(6);


    
    public boolean addTri(Vector3d p0, Vector3d p1, Vector3d p2){

        w0.set(p0);
        w1.set(p1);
        w2.set(p2);

        return tc2.addTri2(w0, w1, w2);
    }
    
}
