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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import javax.imageio.ImageIO;
import java.util.Vector;
import java.io.File;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

import static abfab3d.util.ImageUtil.us2i;



/**
  makes Mip map access to the 16 bit gray image 
  
 */
public class ImageMipMapGray16 {

    static final boolean DEBUG = true;
    static int debugCount = 0;
    public static double SHORT_NORM = 1./0xFFFF;

    MapEntryShort m_data[];
    double m_pixelSizes[];
    int m_width, m_height;
    
    public ImageMipMapGray16(short imageData[], int imageWidth, int imageHeight){
        makeMipMap(imageData, imageWidth, imageHeight);
        
    }
    
    void makeMipMap(short imageData[], int width, int height){
        
        if(DEBUG)printf("start mipmap\n");

        m_width = width;
        m_height = height;
                

        Vector<MapEntryShort> data = new Vector<MapEntryShort>();
        
        short idata[] = imageData;

        while(true){
            
            MapEntryShort me = new MapEntryShort(idata, width, height, ((double)m_width)/width);

            data.add(me);
            
            if(width <= 1 || height <= 1){
                break;
            }
            
            width = (me.width + 1)/2;
            height = (me.height + 1)/2;

            long t0 = time();
            idata = getScaledDownData(me.data, me.width, me.height, width, height); 
            
            printf("mipmap entry:[%d x %d] %d ms\n", width, height, (time() - t0));

            //if(DEBUG) try {ImageIO.write(img, "png", new File(fmt("/tmp/mipmap%02d.png",data.size())));} catch(Exception e){}
          
        }
        
        m_data = (MapEntryShort[])data.toArray(new MapEntryShort[data.size()]);
        m_pixelSizes = new double[m_data.length];
        for(int i = 0; i < m_data.length; i++){
            m_pixelSizes[i] = m_data[i].pixelSize;
        }
    }
        
    
    /**
       return interpolated color of given pixel 
       size - diameter of the averaging area in pixels 
       
    */
    public double getPixel(double x, double y, double probesize){

        if(debugCount-- > 0)
            printf("MipMapGray16.getPixel(%10.5f, %10.5f, %10.5f)\n", x, y, probesize);
        
        // find mipmap entry, which has pixel size less, than probesize
        // and next mipmap entry has larger pixel size
        // linearly interpolate between results
        
        //if(true){
        //    MapEntryShort me = m_data[3];      
        //    return getPixel(me.data, me.width, me.height, me.width*x/(double)m_width, me.height*y/(double)m_height);      
        //}
        int upperIndex = -1;
        
        for(int i = 0; i < m_pixelSizes.length; i++){            
            if(m_pixelSizes[i] > probesize){
                upperIndex = i;
                break;
            }
        }
        
        if(debugCount-- > 0)
            printf("upperIndex: %d \n", upperIndex);

        switch(upperIndex){
        case 0: 
            {
                // probe is smaller than pixel size of the first image
                // no interpolation between maps 
                MapEntryShort me = m_data[0];
                //printf("map: %s\n", me);
                return me.getPixel(x,y);
            }
        case -1:
            
            // probe is larger than pixel size of the last image 
            // no interpolation between maps 
            {
                
                MapEntryShort me = m_data[m_data.length-1];      
                
                //printf("map: %s\n", me);
                return me.getPixel(me.width*x/m_width, me.height*y/m_height);      
            }
            
        default:
            {
                // we are between the maps
                MapEntryShort me0 = m_data[upperIndex-1]; 
                MapEntryShort me1 = m_data[upperIndex];
                //printf("map0: %s\n", me0);
                //printf("map1: %s\n", me1);

                // interpolation between maps 
                // TODO - use exponential interpolation 
                double w = (probesize-me0.pixelSize)/(me1.pixelSize-me0.pixelSize); 

                if(DEBUG && debugCount-- > 0){
                    printf("probe: %7.2f, w0: %7.2f w1:%7.2f %7.2f\n", probesize, me0.pixelSize, me1.pixelSize, w);
                }
                
                double v0 = me0.getPixel(me0.width*x/m_width, me0.height*y/m_height);      
                double  v1 = me1.getPixel(me1.width*x/m_width, me1.height*y/m_height);      
                
                return (v0 + (v1 - v0)*w);
            }
        }        
    }

    /**
       scaled down image from input image
     */
    public static short[] getScaledDownData(short indata[], int inwidth, int inheight, int width, int height){

        
        short outData[] = new short[width * height];
        
        for(int y = 0; y < height; y++){
            
            int y0 = (y * inheight)/height;
            int y1 = ((y + 1) * inheight)/height;
            
            for(int x = 0; x < width; x++){
                
                int x0 = (x * inwidth)/width;
                int x1 = ((x + 1) * inwidth)/width;
                                
                int pv = 0; // pixel value 
                int count = (y1 - y0) * (x1-x0);
                
                for(int yy = y0; yy < y1; yy++){                    
                    for(int xx = x0; xx < x1; xx++){                        
                        pv +=us2i(indata[xx + yy * inwidth]);

                    }
                }
                
                outData[x + y*width] = (short)(pv/count);
                
            }
        }

        return outData;

    } //getScaledDownData

    
    /**
       represents one mipmap entry 
     */
    static class MapEntryShort {
        
        double pixelSize; 
        int width, height; 
        short data[]; 

        MapEntryShort(short _data[], int _width, int _height, double _pixelSize){

            printf("MapEntryShort(%s, %d %d %10.5f)\n", _data, _width, _height, _pixelSize);
            this.data = _data;
            this.width = _width;
            this.height = _height;
            this.pixelSize = _pixelSize;

        }
        
        public String toString(){
            return fmt("%s[%dx%d:%8.5f]",this.getClass().getName(), width, height, pixelSize);
        }

        double getPixel(double x, double y){

            int x0 = (int)Math.floor(x);
            int y0 = (int)Math.floor(y);
            int x1 = x0+1, y1 = y0+1;
            
            double dx = x-x0;
            double dy = y-y0;
            double dx1= 1-dx, dy1=1-dy;
            
            if(x0 < 0) x0 = 0;
            if(x1 < 0) x1 = 0;
            if(y0 < 0) y0 = 0;
            if(y1 < 0) y1 = 0;

            if(x0 >= width) x0 = width-1;
            if(x1 >= width) x1 = width-1;
            if(y0 >= height)y0 = height-1;
            if(y1 >= height)y1 = height-1;
            
            int v00 = us2i(data[y0 * width  + x0]);
            int v10 = us2i(data[y0 * width  + x1]);
            int v01 = us2i(data[y1 * width  + x0]);
            int v11 = us2i(data[y1 * width  + x1]);
            
            double v = SHORT_NORM*(dy1*(dx1*v00 + dx * v10) + dy*(dx1*v01 + dx * v11));
            if(debugCount-- > 0)
                printf("pnt: [%d, %d, %d %d] -> v: %d\n", x0, y0, x1, y1, v);
            return v;

        } // getPixel
        
    } // class MapEntryShort

    /*
    static final double getPixel(short data[], int width, int height, double x, double y){
        
        int x0 = (int)Math.floor(x);
        int y0 = (int)Math.floor(y);
        int x1 = x0+1, y1 = y0+1;
        
        double dx = x-x0;
        double dy = y-y0;
        double dx1= 1-dx, dy1=1-dy;
        
        if(x0 < 0) x0 = 0;
        if(x1 < 0) x1 = 0;
        if(y0 < 0) y0 = 0;
        if(y1 < 0) y1 = 0;
        
        if(x0 >= width) x0 = width-1;
        if(x1 >= width) x1 = width-1;
        if(y0 >= height)y0 = height-1;
        if(y1 >= height)y1 = height-1;
        
        int v00 = us2i(data[y0 * width  + x0]);
        int v10 = us2i(data[y0 * width  + x1]);
        int v01 = us2i(data[y1 * width  + x0]);
        int v11 = us2i(data[y1 * width  + x1]);
        
        double v = (dy1*(dx1*v00 + dx * v10) + dy*(dx1*v01 + dx * v11));
        if(debugCount-- > 0)
            printf("pnt: [%d, %d, %d %d] -> v: %d\n", x0, y0, x1, y1, v);
        return v;
        
    } // getPixel
    */
} // class ImageMipMapGray16 

