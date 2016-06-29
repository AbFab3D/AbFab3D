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

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.VecTransform;
import abfab3d.core.Transformer;
import org.apache.commons.io.FilenameUtils;

import java.io.InputStream;

/**
 * Reads triangle mesh from a file/stream into various formats
 * Currently supported are STL, x3db, x3d, x3dv
 *
 * @author Vladimir Bulatov
 * @author Alan Hudson
 */
public class MeshReader implements TriangleProducer, Transformer {
    
    public static final String 
        EXT_STL = "stl",
        EXT_X3DB = "x3db",
        EXT_X3D = "x3d",
        EXT_X3DV = "x3dv";


    private String m_path;
    private InputStream m_is;
    private String m_format;
    private String m_baseURL;
    private VecTransform m_transform;
    private TriangleProducer m_producer;


    public MeshReader(String path) {
        m_path = path;
        m_format = FilenameUtils.getExtension(path);
    }

    /**
     * Load stream versions.  For the STL path we only support binary STL
     *
     * @param is
     * @param format
     */
    public MeshReader(InputStream is, String baseURL, String format) {
        m_is = is;
        m_format = format;
        m_baseURL = baseURL;
    }


    public boolean getTriangles(TriangleCollector out) {
        
        if(m_producer == null){
            m_producer = createReader();
        }
        if(m_producer != null && m_producer instanceof Transformer){

            ((Transformer)m_producer).setTransform(m_transform); 
            
        }
        
        return m_producer.getTriangles(out);
         
    }
    
    protected TriangleProducer createReader() {
        if(m_format.equalsIgnoreCase(EXT_STL)) {
            if (m_is != null) {
                return new STLReader(m_is);
            } else {
                return new STLReader(m_path);
            }
        } else if(m_format.equalsIgnoreCase(EXT_X3DB) || m_format.equalsIgnoreCase(EXT_X3D) || m_format.equalsIgnoreCase(EXT_X3DV)) {

            if (m_is != null) {
                return new X3DReader(m_is,m_baseURL);
            } else {
                return new X3DReader(m_path);
            }
        }
        return null;
    }
        
    public void setTransform(VecTransform trans) {
        m_transform = trans;
    }
    
}