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
   
   
 */
public class ImageMaker {
    
    protected int m_threadCount = 0;

    public ImageMaker(){        
    }

    public void setThreadCount(int threadCount){        
        m_threadCount = threadCount;
    }


    /**
       creates and renders in default TYPE_INT_ARGB format 
     */
    public BufferedImage renderImage(int width, int height, Bounds bounds, DataSource renderer){

        BufferedImage image =  new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] imageData = db.getData();
        
        renderImage(width, height, bounds, renderer, imageData);
        return image;

    }

    void renderImage(int width, int height, Bounds bounds, DataSource renderer, int [] imageData){

        //
        // TODO make it MT 
        //
        Vec pnt = new Vec(3);
        Vec data = new Vec(4);
        double du = bounds.getSizeX()/width;
        double dv = bounds.getSizeY()/height;

        double umin = bounds.xmin + du/2;
        double vmin = bounds.ymin + dv/2;
        double wmin = (bounds.zmin + bounds.zmax)/2;
        


        for(int v = 0; v < height; v++){
            for(int u = 0; u < width; u++){

                pnt.v[0] = umin + u*du;
                pnt.v[1] = vmin + v*dv;
                pnt.v[2] = wmin;
                renderer.getDataValue(pnt, data);
                imageData[u + width*v] = makeARGB(data.v[0],data.v[1],data.v[2],data.v[3]);
            }
        }
    }

} // class ImageMaker 