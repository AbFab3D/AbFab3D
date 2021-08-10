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

import abfab3d.core.DataSource;
import abfab3d.core.Transformer;
import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.VecTransform;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;

import static abfab3d.core.Output.printf;

/**
 * Reads triangle mesh from a file/stream into various formats.  Supports attribute loading of color and multimaterial
 * files.
 *
 * Currently supported are x3db, x3d, x3dv
 *
 * @author Alan Hudson
 */
public class AttributedMeshReader extends BaseMeshReader implements AttributedTriangleProducer, Transformer {

    private String m_path;
    private InputStream m_is;
    private String m_format;
    private String m_baseURL;
    private VecTransform m_transform;
    private AttributedTriangleProducer m_producer;
    private DataSource m_attributeCalc;
    private int m_dataDimension = -1;
    private boolean m_initialized = false;
    private String m_texURL[] = new String[]{"texture.png"};

    public AttributedMeshReader(String path) {
        m_path = path;

        File f = new File(m_path);

        if (f.isDirectory()) {
            // got a directory, need to find the main file
            File[] files = f.listFiles();
            int len = files.length;
            String main = null;

            for(int i=0; i < len; i++) {
                String ext = FilenameUtils.getExtension(files[i].getName());
                if (supportedExt.contains(ext)) {
                    if (main != null) {
                        throw new IllegalArgumentException("Zip contains multiple main file.  First: " + main + " Second: " + files[i]);
                    }

                    main = files[i].getAbsolutePath();
                }
            }

            if (main == null) {
                throw new IllegalArgumentException("Zip does not contain a supported main file");
            }
            m_path = main;

            printf("main file: %s\n",m_path);
        }

        m_format = FilenameUtils.getExtension(m_path);
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


    public boolean getAttTriangles(AttributedTriangleCollector out) {
        
        initialize();
        
        boolean ret_val = m_producer.getAttTriangles(out);

        return ret_val;
    }
    
    protected void createReader() {

        if(m_format.equalsIgnoreCase(EXT_STL)) {
            if (m_is != null) {
                m_producer = new STLReader(m_is);
            } else {
                m_producer = new STLReader(m_path);
            }
        } else if(m_format.equalsIgnoreCase(EXT_X3DB) || m_format.equalsIgnoreCase(EXT_X3D) || m_format.equalsIgnoreCase(EXT_X3DV) || m_format.equalsIgnoreCase(EXT_WRL)) {

            if (m_is != null) {
                m_producer = new AttributedX3DReader(m_is,m_baseURL);
            } else {
                m_producer = new AttributedX3DReader(m_path);
            }            
        } else if(m_format.equalsIgnoreCase(EXT_OBJ)){
            if (m_is != null) {
                m_producer = new OBJReader(m_is);
            } else {
                m_producer = new OBJReader(m_path);
            }            
        }
        
        if (m_producer == null) throw new IllegalArgumentException("Unsupported format: " + m_format + " path: " + m_path);
        
        if (m_producer instanceof AttributedX3DReader) {

            AttributedX3DReader xreader = (AttributedX3DReader)m_producer;
            m_dataDimension = xreader.getDataDimension();
            m_attributeCalc = xreader.getAttributeCalculator();
            m_texURL = xreader.getTexURL();

        } else {
            m_dataDimension = 3;
        }
        
    }
        
    public void setTransform(VecTransform trans) {
        m_transform = trans;
    }

    public void initialize(){

        if(m_initialized)
            return;

        if (m_producer == null){
            createReader();
        }
       
        if(m_producer != null && m_producer instanceof Transformer){
            
            ((Transformer)m_producer).setTransform(m_transform); 
            
        }

        m_initialized = true;
    }

    /**
     * Get the attribute calculator.  Must be called after getTriangles2 call.
     * @return
     */
    public DataSource getAttributeCalculator() {
        
        initialize();         

        return m_attributeCalc;
    }

    public String[] getTexURL(){

        return m_texURL;

    }

    /**
     * Returns how many data channels this file contains.  
     */
    public int getDataDimension() {
        
        initialize();
        
        return m_dataDimension;
    }
}