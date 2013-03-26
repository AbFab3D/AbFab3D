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

import java.util.Arrays;

import static abfab3d.util.ImageUtil.us2i; 
import static abfab3d.util.MathUtil.clamp;

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
    
    public ImageGray16(short data[], int width, int height){

        this.data = data;
        m_width = width; 
        m_height = height;
        
    }


    public final double getDataD(int x, int y){

        return us2i(data[x + y * m_width])/MAX_USHORT_D;

    }

    public final int getDataI(int x, int y){

        return us2i(data[x + y * m_width]);

    }

    /**
       converts the image inot black and white based on given 
       threshold form (0,1) 
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
       
       convert black and white image into distance transformaed image 

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
                    // chack if point has while neighbours 
                }                    
            }
        }        
    }

    public void gaussianBlur(double size){

        // temporaty use gaussian blur instead of distance transform        
        double[] kernel = MathUtil.getGaussianKernel(size, 0.001);
        convolute(kernel);        
    }
    
    
    /**
       make 2 dimensional convolution with 1 dimensional kernel K2(x,y) = k(x)*k(y)
     */
    public void convolute(double kernel[]){
        
        int s = (m_width > m_height) ? m_width : m_height;
        
        int row[] = new int[s];
        
        convoluteX(kernel, row);
        convoluteY(kernel, row);
        
    }
    
    void convoluteX(double kernel[], int row[]){
        
        int w = m_width;
        int h = m_height;
        
        int ksize = kernel.length/2;
        int w1 = w-1;

        for(int y = 0; y < h; y++){
            
            // init accumulator array 
            Arrays.fill(row, 0, w, 0);
            int offsety = y*w;

            for(int x = 0; x < w; x++){
                
                //int v = us2i(data[offsety + x]);
                
                for(int k = 0; k < kernel.length; k++){

                    //int kx = x + k - ksize;
                    int xx = x - (k-ksize); //offsety + x + k;

                    xx = clamp(xx, 0, w1); // boundary conditions 
                    row[x] += (int)(kernel[k] * us2i(data[offsety + xx]));                  
                }
            }             
            for(int x = 0; x < w; x++){
                data[offsety + x] = (short)row[x];
            }                            
        }
    }
    
    void convoluteY(double kernel[], int row[]){

        //TODO take into account boundary effects 
        int w = m_width;
        int h = m_height;
        int ksize = kernel.length/2;
        int h1 = h-1;

        for(int x = 0; x < w; x++){
            // init accumulator array 
            Arrays.fill(row, 0, h, 0);

            for(int y = 0; y < h; y++){                
                
                //int v = us2i(data[y*w + x]);                

                for(int k = 0; k < kernel.length; k++){
                    int yy = y - (k-ksize); 
                    yy = clamp(yy, 0, h1);
                    row[y] += (int)(kernel[k] * us2i(data[yy*w + x])); 
                }
            } 
            
            for(int y = 0; y < h; y++){
                data[y*w + x] = (short)row[y];
            }                    
        }
    } // convolute y 
    
} // class ImageGray16

        

        

