/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2018
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

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;


import abfab3d.core.Bounds;
import abfab3d.core.Grid2D;
import abfab3d.core.Grid2DProducer;
import abfab3d.core.GridDataDesc;
import abfab3d.core.ImageProducer;
import abfab3d.grid.Grid2DInt;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.Grid2DSourceWrapper;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.BooleanParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.ParamCache;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;

import java.awt.image.BufferedImage;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.PT;
import static abfab3d.util.ImageUtil.getGray16Data;
import static abfab3d.util.ImageUtil.getImageData_INT_ARGB;


/**
   converts grid2D into buffered image. 
   
 */
public class Grid2DtoImage  {
    

    /**
       image created in BufferedImage.TYPE_INT_ARGB
     */    
    public static BufferedImage getARGBImage(Grid2D grid){
        
        int width = grid.getWidth();
        int height = grid.getHeight();
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        DataBuffer dataBuffer = image.getRaster().getDataBuffer();
        int data[] = ((DataBufferInt)dataBuffer).getData();

        for(int y = 0; y < height; y++){

            int offset = (height-1-y)*width;
            for(int x = 0; x < width; x++){
                
                data[offset + x] = (int)(0xFFFFFFFFL & grid.getAttribute(x,y));
                
            }
        }

        return image;
    }
}