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

import java.io.File;

import javax.imageio.ImageIO;

import abfab3d.core.ImageProducer;


import abfab3d.param.BaseParameterizable;
import abfab3d.param.URIParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.LongParameter;
import abfab3d.param.Parameter;
import abfab3d.param.ParamCache;

import abfab3d.grid.Grid2DShort;


import static abfab3d.util.ImageUtil.getGray16Data;
import static abfab3d.core.Units.PT;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
   class to produce Grid2D from image file
 */
public class ImageReader extends BaseParameterizable implements ImageProducer {
    
    static final double DEFAULT_PIXEL_SIZE = PT; // 1 point 
    static final boolean CACHING_ENABLED = true;
    static final boolean DEBUG = false;

    URIParameter mp_uri = new URIParameter("uri", "image path");
    Parameter m_params[] = new Parameter[]{
        mp_uri,
    };

    // loaded image converted into grid
    BufferedImage m_image;

    /**
       @param path - image path
     */
    public ImageReader(String path){
        addParams(m_params);
        mp_uri.setValue(path);
    }
    

    public BufferedImage getImage(){
        
        prepareImage();
        return m_image;

    }

    public int getWidth(){
        prepareImage();
        return m_image.getWidth();
    }

    public int getHeight(){

        prepareImage();
        return m_image.getHeight();

    }

    /**
              
       @Override
    */
    public void prepareImage(){
        
        Object co = null;
        String label = null;
        if(CACHING_ENABLED){
            label = BaseParameterizable.getParamString(getClass().getSimpleName(), m_params);
            co = ParamCache.getInstance().get(label);
        }
        if (co == null) {
            m_image = loadImage(mp_uri.getValue());
            if(CACHING_ENABLED){
                ParamCache.getInstance().put(label, m_image);
                if (DEBUG) printf("ImageReader: caching image: %s -> %s\n",label, m_image);                
            }
        } else {
            m_image = (BufferedImage) co;
            if (DEBUG) printf("ImageReader: got cached image %s -> %s\n",label, m_image);
        }            
    }
    

    protected BufferedImage loadImage(String path){

        if (DEBUG) printf("%s.loadImage(%s)\n",this, path);
        if (!new File(path).exists()) {
            throw new IllegalArgumentException(fmt("image %s does not exist", path));
        }
        try {
            BufferedImage image = ImageIO.read(new File(path)); 
            return image;
        } catch (Exception e){
            throw new RuntimeException(fmt("exception reading image %s", path));
        }        
    }
} // class ImageReader