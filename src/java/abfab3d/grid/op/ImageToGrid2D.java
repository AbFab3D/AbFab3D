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


import abfab3d.core.Bounds;
import abfab3d.core.Grid2D;
import abfab3d.core.Grid2DProducer;
import abfab3d.core.Vec;
import abfab3d.core.GridDataDesc;
import abfab3d.core.ImageProducer;


import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.Parameter;
import abfab3d.param.ParamCache;

import abfab3d.grid.Grid2DShort;


import static abfab3d.util.ImageUtil.getGray16Data;
import static abfab3d.core.Units.PT;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
   class to produce Grid2D from ImageProducer
 */
public class ImageToGrid2D extends BaseParameterizable implements Grid2DProducer {
    
    static final double DEFAULT_PIXEL_SIZE = PT; // 1 point 
    static final boolean CACHING_ENABLED = true;
    static final boolean DEBUG = false;

    SNodeParameter mp_imageProducer = new SNodeParameter("imageProducer", "image producer");
    DoubleParameter mp_pixelSize = new DoubleParameter("pixelSize", "pixel Size", DEFAULT_PIXEL_SIZE);
    BooleanParameter mp_useColor = new BooleanParameter("useColor", "use color", false);
    Parameter m_params[] = new Parameter[]{
        mp_imageProducer,
        mp_useColor,
        mp_pixelSize,
    };

    // loaded image converted into grid
    Grid2D m_grid;

    /**
       @param path - image path
     */
    public ImageToGrid2D(ImageProducer imageProducer){
        addParams(m_params);
        mp_imageProducer.setValue(imageProducer);
    }
    
    public int getWidth(){
        Grid2D grid = getGrid2D();
        return grid.getWidth();
    }

    public int getHeight(){

        Grid2D grid = getGrid2D();
        return grid.getHeight();

    }

    /**
              
       @Override
    */
    public Grid2D getGrid2D(){
        
        Object co = null;
        String label = null;
        if(CACHING_ENABLED){
            label = BaseParameterizable.getParamString(getClass().getSimpleName(), m_params);
            co = ParamCache.getInstance().get(label);
        }
        if (co == null) {
            m_grid = loadImage();
            if(CACHING_ENABLED){
                ParamCache.getInstance().put(label, m_grid);
                if (DEBUG) printf("ImageReader: caching image: %s -> %s\n",label, m_grid);                
            }
        } else {
            m_grid = (Grid2D) co;
            if (DEBUG) printf("ImageReader: got cached image %s -> %s\n",label, m_grid);
        }        
        return m_grid;
    
    }
    

    protected Grid2D loadImage(){

        if (DEBUG) printf("%s.loadImage()\n",this);
        ImageProducer producer = (ImageProducer)mp_imageProducer.getValue(); 
        BufferedImage image = producer.getImage();
        return makeGrid(image, mp_pixelSize.getValue());
    }


    public static Grid2D makeGrid(BufferedImage image, double pixelSize) {

        return makeGrayGrid(image, pixelSize);

    }
    
    public static Grid2D makeGrayGrid(BufferedImage image, double pixelSize) {

        int w = image.getWidth();
        int h = image.getHeight();
        
        Grid2DShort grid = new Grid2DShort(w,h,pixelSize);        
        grid.setGridBounds(new Bounds(0, w*pixelSize, 0, h*pixelSize, 0, pixelSize)); 
        grid.setDataDesc(GridDataDesc.getDefaultAttributeDesc(16));
        short data[] = getGray16Data(image);
        // Need to convert from image (0,0) upper left to grid (0,0) lower left
        for(int y=0; y < h; y++) {
            int y1 = h - 1 - y;
            for(int x=0; x < w; x++) {
                short d = data[x + y * w];
                grid.setAttribute(x, y1, d);
            }
        }
        return grid;
    }

    public static Grid2D makeColorGrid(BufferedImage image, double pixelSize) {

        if(false)return null;
        throw new RuntimeException("not implemented");
    }


} // class ImageReader