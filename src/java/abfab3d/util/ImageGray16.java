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

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.util.Arrays;

import static abfab3d.util.ImageUtil.us2i; 
import static abfab3d.util.ImageUtil.ub2i; 
import static abfab3d.util.MathUtil.clamp;

import static abfab3d.util.Output.printf;

/**
   gray scale image represented as array of unsigned short
*/
public class ImageGray16 {
    
    public static final double MAX_USHORT_D = (double)(0xFFFF);
    public static final short MAX_USHORT_S = (short)0xFFFF;
    public static final int MAX_USHORT_I = 0xFFFF;
    
    short data[];


    int m_width;
    int m_height;
    
    public ImageGray16(){
        data = new short[]{0};
        m_width = 1;
        m_height = 1;
    }

    public ImageGray16(BufferedImage image){

        data = ImageUtil.getGray16Data(image);
        m_width = image.getWidth();
        m_height = image.getHeight();
    }

    public ImageGray16(int width, int height){

        data = new short[width*height];
        m_width = width;
        m_height = height;

    }

    public ImageGray16(short data[], int width, int height){

        this.data = data;
        m_width = width; 
        m_height = height;
            
    }

    public ImageGray16(byte bdata[], int width, int height){

        m_width = width; 
        m_height = height;
        this.data = new short[width*height];
        for(int i = 0; i < data.length; i++){
            data[i] = (short)ub2i(bdata[i]);
        }
            
    }

    public int getWidth(){
        return m_width;
    }

    public int getHeight(){
        return m_height;
    }

    public short[] getData(){
        return data;
    }

    public final double getDataD(int x, int y){
        
        return us2i(data[x + y * m_width])/MAX_USHORT_D;

    }

    public final int getDataI(int x, int y){

        if(x < 0) x = 0;
        if(y < 0) y = 0;
        if(x >= m_width) x = m_width-1;
        if(y >= m_height) y = m_height-1;

        return us2i(data[x + y * m_width]);

    }

    public final void setData( int x, int y, int value){
        data[x + y * m_width] = (short)value;
    }

    public Object clone(){
        short d[] = new short[data.length];
        System.arraycopy(data,0, d, 0, d.length);
        return new ImageGray16(d, m_width, m_height);
    }

    /**
       converts the image into black and white based on given 
       threshold. Threshold should be in (0,1) 
     */
    public void makeBlackWhite(double threshold){

        int len = data.length;
        int t = (int)(threshold * MAX_USHORT_D);

        for(int i = 0; i < len; i++){
            if(us2i(data[i]) > t)
                data[i] = MAX_USHORT_S;
            else
                data[i] = 0;
        }
    }
    
    /**
       
       convert black and white image into distance transformed image 

       points inside are black and 

                     
       --------------.                           .--------------
                     |                           |    outside - white 
                     |                           |
                     |                           |
                     |                           |
                     |                           |
                     |                           |
                     |          inside - black   |
                     .___________________________.


       ----------\                                    /---------
                  \                                  /
                   \                                /
                    \                              /
                     \                            /
                      \                          /
                       \                        /   
                        \______________________/

     */
    public void makeDistanceTransform(int maxRadius){
        // TODO
        
    }

    public void _makeDistanceTransform(int maxRadius){
        // 
        // 1) find all surface points 
        // 2) for each surface point scan all pixles inside of given radius
        //     and replace if with distance function 
        // TODO

        int len = data.length;
        int w = m_width;
        int h = m_height;
        
        for(int y = 0; y < h; y++){
            int yoffset = y*w;
            for(int x = 0; x < w; x++){
                if(data[x + yoffset] == 0){
                    // check if point has while neighbours 
                }                    
            }
        }        
    }

    public void gaussianBlur(double size){

        // use gaussian blur instead of distance transform 
        double[] kernel = MathUtil.getGaussianKernel(size, 0.001);
        convolute(kernel);
    }
    
    
    /**
       make 2 dimensional convolution with 1 dimensional kernel K2(x,y) = k(x)*k(y)
     */
    public void convolute(double kernel[]){
        
        int s = Math.max(m_width, m_height);
        
        double row[] = new double[s];
        
        convoluteX(kernel, row);
        convoluteY(kernel, row);
        
    }
    
    void convoluteX(double kernel[], double row[]){
        
        int w = m_width;
        int h = m_height;
        
        int ksize = kernel.length/2;
        int w1 = w-1;

        for(int y = 0; y < h; y++){
            
            // init accumulator array 
            Arrays.fill(row, 0, w, 0.);
            int offsety = y*w;

            for(int x = 0; x < w; x++){
                
                //int v = us2i(data[offsety + x]);
                
                for(int k = 0; k < kernel.length; k++){

                    //int kx = x + k - ksize;
                    int xx = x - (k-ksize); //offsety + x + k;

                    xx = clamp(xx, 0, w1); // boundary conditions 
                    row[x] += (kernel[k] * us2i(data[offsety + xx]));                  
                }
            }             
            for(int x = 0; x < w; x++){
                data[offsety + x] = (short)(row[x]+0.5);
            }                            
        }
    }
    
    void convoluteY(double kernel[], double row[]){

        //TODO take into account boundary effects 
        int w = m_width;
        int h = m_height;
        int ksize = kernel.length/2;
        int h1 = h-1;

        for(int x = 0; x < w; x++){
            // init accumulator array 
            Arrays.fill(row, 0, h, 0.);

            for(int y = 0; y < h; y++){                
                
                //int v = us2i(data[y*w + x]);                

                for(int k = 0; k < kernel.length; k++){
                    int yy = y - (k-ksize); 
                    yy = clamp(yy, 0, h1);
                    row[y] += (kernel[k] * us2i(data[yy*w + x])); 
                }
            } 
            
            for(int y = 0; y < h; y++){
                data[y*w + x] = (short)(row[y]+0.5);
            }                    
        }
    } // convolute y 


    public void write(String fileName, int maxDataValue) throws IOException {
        
        BufferedImage outImage = new BufferedImage(m_width, m_height, BufferedImage.TYPE_INT_ARGB);

        DataBufferInt dbi = (DataBufferInt)(outImage.getRaster().getDataBuffer());

        int[] imageData = dbi.getData();

        for(int y = 0; y < m_height; y++){
            for(int x = 0; x < m_width; x++){

                int cc = data[x + y * m_width];
                if(cc < 0) cc = -cc;
                if(cc > maxDataValue) cc = maxDataValue;

                int gray =  (int)(((maxDataValue - cc) * 255)/maxDataValue);
                imageData[x + y * m_width] = makeColor(gray);
            }
        }

        ImageIO.write(outImage, "PNG", new File(fileName));
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
                int gray =  (int)((cc / 65535.0) * 255);

                imageData[x + y * m_width] = makeColor(gray);
            }
        }

        ImageIO.write(outImage, "PNG", new File(fileName));
    }

    static final int makeColor(int gray){

        int ret_val = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
        return ret_val;

    }

} // class ImageGray16
