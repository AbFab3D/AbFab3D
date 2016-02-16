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

package abfab3d.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.util.Arrays;

import static abfab3d.util.ImageUtil.us2i; 
import static abfab3d.util.ImageUtil.ub2i; 
import static abfab3d.util.MathUtil.clamp;

import static abfab3d.util.Output.printf;

/**
   color ARGB image as array of int
*/
public class ImageColor {
        
    int data[];

    int m_width;
    int m_height;
    
    public ImageColor(){
        data = new int[]{0};
        m_width = 1;
        m_height = 1;
    }

    public ImageColor(BufferedImage image){

        data = getImageData(image);
        m_width = image.getWidth();
        m_height = image.getHeight();
    }

    public ImageColor(int width, int height){

        data = new int[width*height];
        m_width = width;
        m_height = height;

    }

    public ImageColor(int data[], int width, int height){

        this.data = data;
        m_width = width; 
        m_height = height;
            
    }

    public int getWidth(){
        return m_width;
    }

    public int getHeight(){
        return m_height;
    }

    public int[] getData(){
        return data;
    }

    public final int getDataI(int x, int y){

        if(x < 0) x = 0;
        if(y < 0) y = 0;
        if(x >= m_width) x = m_width-1;
        if(y >= m_height) y = m_height-1;

        return data[x + y * m_width];

    }

    public final void setData( int x, int y, int value){
        data[x + y * m_width] = value;
    }

    public Object clone(){
        int d[] = new int[data.length];
        System.arraycopy(data,0, d, 0, d.length);
        return new ImageColor(d, m_width, m_height);
    }
        
    /**
     * Write a debug version of the map out in a range of 0-255 grey per channel
     *
     * @param fileName
     * @throws IOException
     */
    public void write(String fileName) throws IOException {

        BufferedImage outImage = new BufferedImage(m_width, m_height, BufferedImage.TYPE_INT_ARGB);

        DataBufferInt dbi = (DataBufferInt)(outImage.getRaster().getDataBuffer());

        int[] imageData = dbi.getData();

        for(int y = 0; y < m_height; y++){
            for(int x = 0; x < m_width; x++){

                int cc = getDataI(x,y);
                imageData[x + y * m_width] = cc;
            }
        }

        ImageIO.write(outImage, "PNG", new File(fileName));
    }

    /**
       return color image data as ints 
     */
    public static int[] getImageData(BufferedImage image){

        return ImageUtil.getImageData_INT_ARGB(image);

    }

} // class ImageColor
