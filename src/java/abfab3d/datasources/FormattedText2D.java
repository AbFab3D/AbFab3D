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

package abfab3d.datasources;


import abfab3d.core.ResultCodes;
import abfab3d.param.*;
import abfab3d.util.Insets2;
import abfab3d.util.TextUtil;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.xhtmlrenderer.swing.Java2DRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;


/**
 * Makes 2D image of formatted text
 *
 * @author Alan Hudson
 */
public class FormattedText2D extends BaseParameterizable implements ExpensiveInitializable {
    static final boolean DEBUG = false;

    BufferedImage m_bitmap = null;

    DoubleParameter  mp_height = new DoubleParameter("height","height of text",5*MM);
    DoubleParameter  mp_width = new DoubleParameter("width","width of text",0*MM); // width is initially undefined
    DoubleParameter  mp_voxelSize = new DoubleParameter("voxelSize","size of voxel for text rendering", 0.1*MM);
    URIParameter mp_source      = new URIParameter("source","text to be created", "Hello<br/>World");

    Parameter m_aparam[] = new Parameter[]{
        mp_height,
        mp_width,
        mp_voxelSize,
        mp_source
    };


    /**
     * Constructor
     @param source The text source
     */
    public FormattedText2D(String source){
        super.addParams(m_aparam);
        mp_source.setValue(source);
    }

    public void setSource(String val) {
        m_bitmap = null;
        mp_source.setValue(val);
    }

    /**
     * Set the voxel size
     * @param val The size in meters
     */
    public void setVoxelSize(double val) {
        m_bitmap = null;
        mp_voxelSize.setValue(val);
    }

    public void set(String param, Object val) {
        // change of param causes change of bitmap
        m_bitmap = null;
        super.set(param, val);
    }

    public void setWidth(double val) {
        if (val <= 0) {
            throw new IllegalArgumentException("Text2D width cannot be <= 0");
        }

        m_bitmap = null;
        mp_width.setValue(val);
    }

    public double getWidth() {
        return mp_width.getValue();
    }

    public void setHeight(double val) {
        if (val <= 0) {
            throw new IllegalArgumentException(fmt("illegal Text2D height:%11.9", val));
        }

        m_bitmap = null;
        mp_height.setValue(val);
    }

    public double getHeight() {
        return mp_height.getValue();
    }

    /**
     * @noRefGuide
     * @return
     */
    public BufferedImage getImage(){

        if(m_bitmap == null)
            initialize();

        return m_bitmap;
    }

    /**
     * @noRefGuide
     */
    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        if(DEBUG) printf("FormattedText2D.initialize()\n");

        double voxelSize = mp_voxelSize.getValue();
        if(DEBUG) printf("  voxelSize:%7.5f\n", voxelSize);
        int height = (int)Math.round(mp_height.getValue()/voxelSize);
        if(DEBUG) printf("  text height:%d pixels\n", height);

        int width = (int)Math.round(mp_width.getValue()/voxelSize);
        if(DEBUG) printf("  text width:%d pixels\n", width);

        String text = mp_source.getValue();

        if(DEBUG) printf("  text:\"%s\"\n", text);

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document doc;
        try {
            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">";
            text = header + text + "</html>";
            builder = factory.newDocumentBuilder();
            ByteArrayInputStream input =  new ByteArrayInputStream(
                    text.getBytes("UTF-8"));
            doc = builder.parse(input);

            System.out.printf("Rendering:  w: %d  h: %d\n",width,height);
            Java2DRenderer renderer = new Java2DRenderer(doc, width,height);
            renderer.setBufferedImageType(BufferedImage.TYPE_INT_RGB);
            m_bitmap = renderer.getImage();

            ImageIO.write(m_bitmap,"png", new File("/tmp/formatted.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(DEBUG)printf("Text2D bitmap height: %d x %d\n", m_bitmap.getWidth(), m_bitmap.getHeight());
        
        return ResultCodes.RESULT_OK;
        
    }

    /**
     * Implement this as a value
     * @return
     */
    public String getParamString() {
        return BaseParameterizable.getParamString("Text2D",getParams());
    }

}  // class Text2D 
