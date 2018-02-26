/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2011
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.datasources;


import abfab3d.core.ResultCodes;
import abfab3d.core.ImageProducer;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.ParamCache;
import abfab3d.param.StringParameter;
import org.w3c.dom.Document;
import org.xhtmlrenderer.swing.Java2DRenderer;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;


/**
 * Makes 2D image of formatted text
 *
 * @author Alan Hudson
 */
public class FormattedText2D extends BaseParameterizable implements ImageProducer {

    static final boolean DEBUG_VIZ = false;
    static final boolean DEBUG = false;
    static final boolean CACHING_ENABLED = true;

    BufferedImage m_bitmap = null;

    DoubleParameter mp_height = new DoubleParameter("height", "height of text", 5 * MM);
    DoubleParameter mp_width = new DoubleParameter("width", "width of text", 5 * MM); // width is initially undefined ??
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "size of voxel for text rendering", 0.05 * MM);
    StringParameter mp_source = new StringParameter("source", "text to be created", "Hello<br/>World");

    Parameter m_param[] = new Parameter[]{
            mp_height,
            mp_width,
            mp_voxelSize,
            mp_source
    };


    /**
     * Constructor
     @param source The text source
     */
    public FormattedText2D(String source) {
        super.addParams(m_param);
        mp_source.setValue(source);
    }

    public void setSource(String val) {
        mp_source.setValue(val);
    }

    /**
     * Set the voxel size
     * @param val The size in meters
     */
    public void setVoxelSize(double val) {
        mp_voxelSize.setValue(val);
    }

    /**
     * Get the voxel size
     * @return
     */
    public double getVoxelSize() {
        return mp_voxelSize.getValue();
    }
    
    public void set(String param, Object val) {
        super.set(param, val);
    }

    public void setWidth(double val) {
        if (val <= 0) {
            throw new IllegalArgumentException("Text2D width cannot be <= 0");
        }
        mp_width.setValue(val);
    }

    public double getWidth() {
        return mp_width.getValue();
    }

    public void setHeight(double val) {
        if (val <= 0) {
            throw new IllegalArgumentException(fmt("illegal Text2D height:%11.9", val));
        }
        mp_height.setValue(val);
    }

    public double getHeight() {
        return mp_height.getValue();
    }

    /**
     * @noRefGuide
     * @return
     */
    public BufferedImage getImage() {

        initialize();

        return m_bitmap;
    }

    /**
     * @noRefGuide
     */
    protected void initParams() {
        super.addParams(m_param);
    }

    /**
     @noRefGuide
     */
    public int initialize() {

        if (DEBUG) printf("FormattedText2D.initialize()\n");

        String label = getDataLabel();
        Object co = null;
        if(CACHING_ENABLED)co = ParamCache.getInstance().get(label);
        if (co == null) {
            m_bitmap = prepareImage();
            if(CACHING_ENABLED)ParamCache.getInstance().put(label, m_bitmap);
            if (DEBUG) printf("Text2D: caching image: %s -> %s\n",label, m_bitmap);
        } else {
            m_bitmap = (BufferedImage) co;
            if (DEBUG) printf("Text2D: got cached image %s -> %s\n",label, m_bitmap);
        }
        return ResultCodes.RESULT_OK;

    }

    protected BufferedImage prepareImage(){

        double voxelSize = mp_voxelSize.getValue();
        if (DEBUG) printf("  voxelSize:%7.5f\n", voxelSize);
        int height = (int) Math.round(mp_height.getValue() / voxelSize);
        if (DEBUG) printf("  text height:%d pixels\n", height);

        int width = (int) Math.round(mp_width.getValue() / voxelSize);
        if (DEBUG) printf("  text width:%d pixels\n", width);

        String text = mp_source.getValue();

        if (DEBUG) printf("  text:\"%s\"\n", text);

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document doc;
        BufferedImage bitmap  = null;
        try {
            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">";
            text = header + text + "</html>";
            builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(
                    text.getBytes("UTF-8"));
            doc = builder.parse(input);

            if(DEBUG)printf("FormattedText2D  Rendering:  [%d x %d]\n", width, height);
            Java2DRenderer renderer = new Java2DRenderer(doc, width, height);
            renderer.setBufferedImageType(BufferedImage.TYPE_INT_RGB);
            bitmap = renderer.getImage();
            if(DEBUG_VIZ)ImageIO.write(bitmap, "png", new File("/tmp/formatted.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DEBUG) printf("Text2D bitmap height: %d x %d\n", bitmap.getWidth(), bitmap.getHeight());

        return bitmap;

    }

}  // class Text2D
