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

import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.Vec;

import javax.vecmath.Vector3d;

/**
   convert TriangleProducer into AttributedTriangleProducer
   
   accept triangles from TriangleProducer and pretends to be AttributedTriangleProducer 

   @author Vladimir Bulatov
 */
public class TriangleProducerConverter implements AttributedTriangleProducer, TriangleCollector {

    // producer to get traingles from 
    TriangleProducer tp;
    // collector to send triamngles to 
    AttributedTriangleCollector atc;

    /**
       
     */
    public TriangleProducerConverter(TriangleProducer tp){
        this.tp = tp;
    }

    /**
       feeds all triangles into supplied TriangleCollector 

       returns true if success, false if faiure        
     */
    public boolean getAttTriangles(AttributedTriangleCollector atc){
        this.atc = atc;
        return tp.getTriangles(this);
    }
    /**
       @override
    */
    public int getDataDimension(){
        return 3; 
    }

    Vec // work vectors
        w0 = new Vec(6),
        w1 = new Vec(6),
        w2 = new Vec(6);


    
    public boolean addTri(Vector3d p0, Vector3d p1, Vector3d p2){

        w0.set(p0);
        w1.set(p1);
        w2.set(p2);

        return atc.addAttTri(w0, w1, w2);
    }
    
}
