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
import abfab3d.util.VecTransform;
import abfab3d.util.Transformer;


/**
   
   reads triangle mesh from a file in various formats  
   currently supported are STL, x3db, x3d
   
 */
public class MeshReader implements TriangleProducer, Transformer {
    
    public static final String 
        EXT_STL = ".stl",
        EXT_X3DB = ".x3db",
        EXT_X3D = ".x3d";
    

    String m_path;
    VecTransform m_transform;
    TriangleProducer m_producer;
    
    /**
       
     */
    public boolean getTriangles(TriangleCollector out) {
        
        if(m_producer == null){
            m_producer = createReader();
        }
        if(m_producer != null && m_producer instanceof Transformer){

            ((Transformer)m_producer).setTransform(m_transform); 
            
        }
        
        return m_producer.getTriangles(out);
         
    }
    
    protected TriangleProducer createReader(){
        String path = m_path.toLowerCase();
        if(path.endsWith(EXT_STL))
            return new STLReader(m_path);
        if(path.endsWith(EXT_X3DB) || path.endsWith(EXT_X3D))
            return new X3DReader(m_path);
        return null;
    }
        
    public void setTransform(VecTransform trans){
        m_transform = trans;
    }
    

    public MeshReader(String path){
        m_path = path;
    }

    
}