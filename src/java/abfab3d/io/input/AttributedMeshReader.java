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

import abfab3d.util.DataSource;
import abfab3d.util.Transformer;
import abfab3d.util.TriangleCollector;
import abfab3d.util.TriangleCollector2;
import abfab3d.util.TriangleProducer;
import abfab3d.util.TriangleProducer2;
import abfab3d.util.VecTransform;
import org.apache.commons.io.FilenameUtils;

import java.io.InputStream;

/**
 * Reads triangle mesh from a file/stream into various formats.  Supports attribute loading of color and multimaterial
 * files.
 *
 * Currently supported are x3db, x3d, x3dv
 *
 * @author Alan Hudson
 */
public class AttributedMeshReader implements TriangleProducer2, Transformer {

    public static final String
        EXT_X3DB = "x3db",
        EXT_X3D = "x3d",
        EXT_X3DV = "x3dv";


    private String m_path;
    private InputStream m_is;
    private String m_format;
    private String m_baseURL;
    private VecTransform m_transform;
    private TriangleProducer2 m_producer;
    private DataSource m_attributeData;


    public AttributedMeshReader(String path) {
        m_path = path;
        m_format = FilenameUtils.getExtension(path);
    }

    /**
     * Load stream versions.  For the STL path we only support binary STL
     *
     * @param is
     * @param format
     */
    public AttributedMeshReader(InputStream is, String baseURL, String format) {
        m_is = is;
        m_format = format;
        m_baseURL = baseURL;
    }


    public boolean getTriangles2(TriangleCollector2 out) {
        
        if(m_producer == null){
            createReader();
        }
        if(m_producer != null && m_producer instanceof Transformer){

            ((Transformer)m_producer).setTransform(m_transform); 
            
        }
        
        return m_producer.getTriangles2(out);
         
    }
    
    protected void createReader() {
        if(m_format.equalsIgnoreCase(EXT_X3DB) || m_format.equalsIgnoreCase(EXT_X3D) || m_format.equalsIgnoreCase(EXT_X3DV)) {

            if (m_is != null) {
                m_producer = new AttributedX3DReader(m_is,m_baseURL);
                m_attributeData = ((AttributedX3DReader) m_producer).getAttributeData();
            } else {
                m_producer = new AttributedX3DReader(m_path);
                m_attributeData = ((AttributedX3DReader) m_producer).getAttributeData();
            }
        }
    }
        
    public void setTransform(VecTransform trans) {
        m_transform = trans;
    }

    /**
     *  Calculate attribute properties at (u,v, tz) point.   Think about reusing ImageColorMap.
     * @return
     */
    public DataSource getAttributeData() { return m_attributeData; }
    
}