/***************************************************************************
 * 
 *                        Shapeways, Inc Copyright (c) 2020
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

import java.util.Vector;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;


/**
   represent Vertex which has references to Segments connected to that vertex
 */
public class Vertex {
    
    Vector<Segment> inSegments = new Vector<Segment>(1);
    Vector<Segment> outSegments = new Vector<Segment>(1);
    
    public Vertex(){
        
    }
    
    public Segment getInSegment(int index){
        return inSegments.get(index);
    }

    public Segment getOutSegment(int index){
        return outSegments.get(index);
    }


    public void addInSegment(Segment segment){
        inSegments.add(segment);
    }

    public void addOutSegment(Segment segment){
        outSegments.add(segment);
    }

    public void removeInSegment(Segment segment){
        inSegments.remove(segment);
        /*
        int index = segments.indexOf(segment);
        if(index >=0) {
            segments.set(index, null);
        }
        */
    }

    public void removeOutSegment(Segment segment){
        outSegments.remove(segment);
    }
    
    public int inSegmentCount(){
        return inSegments.size();
    }

    public int outSegmentCount(){
        return outSegments.size();
    }

    public void toSB(StringBuffer sb){
        sb.append("{ in:");
        for(int i = 0; i < inSegments.size(); i++){
            Segment segment = inSegments.get(i);
            segment.toSB(sb);
            if(i < inSegments.size()-1)
                sb.append(",");                
        }
        sb.append("; out:");                
        for(int i = 0; i < outSegments.size(); i++){
            Segment segment = outSegments.get(i);
            segment.toSB(sb);
            if(i < outSegments.size()-1)
                sb.append(",");                
        }
        sb.append("}");
    }        
} // class Vertex 


