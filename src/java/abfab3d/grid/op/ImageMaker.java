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
import java.awt.image.DataBufferInt;

import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.Vec;

import static abfab3d.util.ImageUtil.makeARGB;


/**
   class to render images via DataSource interface 
   DataSource returns ARBG values which shall be inside interval [0.,1.] for alpha, red, green, blue values
   image is rendered in the rectangle in the xy plane with z values = center of the Bounds 
   the coordinates domain is divided into imgWidth x imgHeight pixels 
   data values values are calculated in CENTERS of pixels (using half pixel shift) 
   data values are assumed to be in the range [0,1]
   data.v[0] - RED
   data.v[1] - GREEN
   data.v[2] - BLUE
   data.v[3] - ALPHA 
 */
public class ImageMaker {
    
    protected int m_threadCount = 0;
    protected int m_imgType = BufferedImage.TYPE_INT_ARGB;

    public ImageMaker(){        
    }

    public void setThreadCount(int threadCount){        
        m_threadCount = threadCount;
    }


    /**
       creates and renders in default TYPE_INT_ARGB format 
     */
    public BufferedImage renderImage(int width, int height, Bounds bounds, DataSource imgRenderer){

        BufferedImage image =  new BufferedImage(width, height, m_imgType);
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] imageData = db.getData();
        
        renderImage(width, height, bounds, imgRenderer, imageData);
        return image;

    }

    public void renderImage(int width, int height, Bounds bounds, DataSource imgRenderer, int [] imageData){

        //
        // TODO make it MT 
        //
        Vec pnt = new Vec(3);
        Vec data = new Vec(4);
        double du = bounds.getSizeX()/width;
        double dv = bounds.getSizeY()/height;

        double umin = bounds.xmin + du/2; // half pixel shift 
        double vmin = bounds.ymin + dv/2;
        double wmin = (bounds.zmin + bounds.zmax)/2;
        


        for(int v = 0; v < height; v++){
            int offy = width*(height-1-v);
            for(int u = 0; u < width; u++){

                pnt.v[0] = umin + u*du;
                pnt.v[1] = vmin + v*dv;
                pnt.v[2] = wmin;
                imgRenderer.getDataValue(pnt, data);
                imageData[u + offy] = makeARGB(data.v[0],data.v[1],data.v[2],data.v[3]);
            }
        }
    }

} // class ImageMaker 