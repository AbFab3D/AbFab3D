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


import abfab3d.util.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static abfab3d.util.ImageMipMapGray16.getScaledDownDataBlack;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.step01;
import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.MathUtil.step;
import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;


/**
 * <p>
 * DataSource which fills 3D space with columns of data originated from 2D image.
 * </p><p>
 * ImageMap may have multiple channels, according to the source image type 
 * </p><p>
 * The 2D image is placed in the XY-plane and for each pixel of the image with coordinate (x,y) the infinite column of voxel 
 * is formed in both directions along Z-axis  
 * </p>
 * by default the image is periodicaly repeated in both directions X and Y 
 * The repeating propety can be set with setRepeat()
 * 
 *
 * @author Vladimir Bulatov
 */
public class ImageMap extends TransformableDataSource {
   
    public static int REPEAT_X = 1, REPEAT_Y = 2, REPEAT_BOTH = 3;
    
    public static final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1, INTERPOLATION_MIPMAP = 2;
    public static final double DEFAULT_VOXEL_SIZE = 0.1*MM;

    int m_repeat = REPEAT_BOTH;
    int m_interpolation = INTERPOLATION_BOX; 
    double v_voxelSize = DEFAULT_VOXEL_SIZE;

    /**
       creates ImageMap from a file

       @param path - path to the image file
       @param sizex - width of the image 
       @param sizey - height of the image 
     */
    public ImageMap(String path, double sizex, double sizey) {
        //TODO 
        
    }

    public ImageMap(BufferedImage image, double sizex, double sizey) {
        //TODO 

    }

    public ImageMap(ImageWrapper imwrapper, double sizex, double sizey) {
        //TODO         
    }
 
    
    public void setRepeat(int value){
        m_repeat = value;
    }

    public void setInterpolation(int value){
        m_interpolation = value;
    }

    public int getDataValue(Vec pnt, Vec davaValue){
        //TODO 
        return RESULT_OK;
    }
    
}