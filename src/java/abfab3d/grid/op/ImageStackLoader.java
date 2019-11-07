/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import abfab3d.core.Color;
import abfab3d.core.ResultCodes;

import javax.imageio.ImageIO;


import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.util.SVGConstants;

import abfab3d.core.ImageStackProducer;


import abfab3d.param.BaseParameterizable;
import abfab3d.param.URIParameter;
import abfab3d.param.ColorParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.Parameter;
import abfab3d.param.ParamCache;

import abfab3d.grid.Grid2DShort;


import static abfab3d.util.ImageUtil.getGray16Data;
import static abfab3d.core.Units.PT;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
   class to load stack of images

   @author Vladimir Bulatov

 */
public class ImageStackLoader extends BaseParameterizable implements ImageStackProducer {

    static final boolean DEBUG = false;
    static final boolean CACHING_ENABLED = true;

    URIParameter mp_uri = new URIParameter("uriTemplate", "image path");
    IntParameter mp_svgRasterizationWidth = new IntParameter("svgRasterizationWidth", "svg Rasterizattion Width", 1000);
    ColorParameter mp_backgroundColor = new ColorParameter("backgroundColor", "background color", new Color(1,1,1));
    IntParameter mp_firstIndex = new IntParameter("firstIndex", 0);    
    IntParameter mp_count = new IntParameter("count", -1);    
    Parameter m_params[] = new Parameter[]{
        mp_uri,
        mp_svgRasterizationWidth,
        mp_backgroundColor,
        mp_firstIndex,
        mp_count,
    };

    // loaded images 
    BufferedImage m_images[];
    boolean m_initialized = false;

    /**
       @param pathTemplate - image path tempate
       @param firstIndex - index of first image 
       @param imagesCount count of images 
       
     */
    public ImageStackLoader(String pathTemplate, int firstIndex, int imagesCount){
        addParams(m_params);
        mp_uri.setValue(pathTemplate);
        mp_firstIndex.setValue(firstIndex);
        mp_count.setValue(imagesCount);
    }
    

    public int initialize(){
        
        m_images = new BufferedImage[mp_count.getValue()];

        m_initialized = true;
        return ResultCodes.RESULT_OK;

    }


    public int getCount(){
        
        return mp_count.getValue();
        
    }

    public BufferedImage getImage(int index){
        
        prepareImage(index);
        return m_images[index];

    }

    public int getWidth(){
        int index = mp_firstIndex.getValue();
        prepareImage(index);
        return m_images[index].getWidth();

    }

    public int getHeight(){

        int index = mp_firstIndex.getValue();
        prepareImage(index);
        return m_images[index].getHeight();

    }


    protected String getLabel(int index){

        return getDataLabel() + ","+String.valueOf(index);

    }
    /**
              
       @Override
    */
    public void prepareImage(int index){

        if(!m_initialized) initialize();

        Object co = null;
        String label = null;
        if(CACHING_ENABLED){
            label = getLabel(index);
            co = ParamCache.getInstance().get(label);
        }
        if (co == null) {
            m_images[index] = loadImage(mp_uri.getValue(), index);
            if(CACHING_ENABLED){
                ParamCache.getInstance().put(label, m_images[index]);
                if (DEBUG) printf("ImageLoader: caching image: %s -> %s\n",label, m_images[index]);
            }
        } else {
            m_images[index] = (BufferedImage) co;
            if (DEBUG) printf("ImageLoader: got cached image %s -> %s\n",label, m_images[index]);
        }            
    }
    

    protected BufferedImage loadImage(String pathTemplate, int index){

        String path = fmt(pathTemplate, index);

        long stime = System.currentTimeMillis();
        if (DEBUG) printf("%s.loadImage(%s)\n",this, path);
        if (!new File(path).exists()) {
            throw new IllegalArgumentException(fmt("image %s does not exist", path));
            /*
            printf(fmt("image file %s does not exist, using default image\n", path));
            return makeDefaultImage(10,10);
            */
        }

        try {
            if(path.endsWith(".svg") || path.endsWith(".SVG")){
                return readImageSVG(path, mp_svgRasterizationWidth.getValue(), mp_backgroundColor.getValue());
            }
            BufferedImage image = ImageIO.read(new File(path));
            if (DEBUG) printf("Loaded image in: %d ms\n",(System.currentTimeMillis() - stime));
            return image;
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(fmt("exception reading image %s", path));
            //printf(fmt("exception %s reading image file: %s \n", e.getMessage(),path));
        }        
        //return makeDefaultImage(10,10);


    }

    BufferedImage makeDefaultImage(int width, int height){

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(java.awt.Color.white);
        g.fillRect(0,0,width, height);
        g.setColor(java.awt.Color.black);
        int w1 = width/2;
        int h1 = height/2;
        g.fillRect(1,1,w1, h1);
        g.fillRect(w1,h1,w1, h1);
        return image;
    }


    
    public static BufferedImage readImageSVG(String path, int width, Color background) throws Exception {
        

        java.awt.Color color = new java.awt.Color((float)background.getr(),(float)background.getg(),(float)background.getb(),(float)background.geta());
        TranscodingHints th = new TranscodingHints();
        th.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        th.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION,SVGDOMImplementation.getDOMImplementation());
        th.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,SVGConstants.SVG_NAMESPACE_URI);
        th.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
        th.put(SVGAbstractTranscoder.KEY_EXECUTE_ONLOAD, new Boolean(false));
        th.put(ImageTranscoder.KEY_WIDTH, new Float(width));
        th.put(ImageTranscoder.KEY_BACKGROUND_COLOR, color);
        
        try {
            
            TranscoderInput input = new TranscoderInput(new FileInputStream(new File(path)));
            
            MyImageTranscoder t = new MyImageTranscoder();                    
            t.setTranscodingHints(th);
            t.transcode(input, null);
            return t.image;
        }

        catch (TranscoderException ex) {
            // Requires Java 6
            ex.printStackTrace();
            throw new IOException("Couldn't convert " + path);
        }
        finally {
            //cssFile.delete();
        }        
    }
    
    static class MyImageTranscoder extends ImageTranscoder {
        BufferedImage image;
        @Override
            public BufferedImage createImage(int w, int h) {                        
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        
        @Override
            public void writeImage(BufferedImage image, TranscoderOutput out) {
            this.image = image;
        }        
    }

} // class ImageReadre