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

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

import static abfab3d.util.ImageUtil.getRed;
import static abfab3d.util.ImageUtil.getGreen;
import static abfab3d.util.ImageUtil.getBlue;
import static abfab3d.util.ImageUtil.getAlpha;



/**
  makes Mip map acess to the image 

 */
public class ImageMipMap {

    static final boolean DEBUG = false;


    MipMapEntry m_data[];
    double m_width, m_height;
    
    //static final int 
    PixelWeight m_pixelWeight = null;//new LinearPixelWeight(127);
    
    public ImageMipMap(BufferedImage bi){
        
        makeMipMap(bi);
        
    }
    
    /**
       makes mipmap with non-linear pixels weight during resampling 
       nonlinearity = 0 - linear resampling 
       nonlinearity > 0 (any value is OK, good typical value is 1.) weight of black pixels increased
       nonlinearity < 0 (any value is OK, good typical value is -1.) weight of white pixels increased       
     */
    public ImageMipMap(BufferedImage bi, double nonlinearity){
        
        m_pixelWeight = new LinearPixelWeight((int)(128*(1+nonlinearity)));
        
        makeMipMap(bi);
        
    }
    
    void makeMipMap(BufferedImage image){
        
        //long t0 = System.currentTimeMillis();
        if(DEBUG)printf("start mipmap\n");
        int width = image.getWidth();
        int height = image.getHeight();
        m_width = width;
        m_height = height;
        
        BufferedImage img = image;
        
        Vector<MipMapEntry> data = new Vector<MipMapEntry>();
        
        while(width > 0 && height > 0){
            
            DataBuffer db = img.getRaster().getDataBuffer();
            if(DEBUG) printf("mipmap [%d x %d]: %s %s\n", width, height, db, getTypeName(img.getType()));
            int dt = db.getDataType();
            
            if(dt == DataBuffer.TYPE_INT ){
                
                DataBufferInt  dbi = (DataBufferInt)db;
                int[] b = dbi.getData();
                MipMapEntry me = new MipMapEntryInt(b, width, height, m_width/width);
                data.add(me);
                //printf("INT b.lengt: %d - %d\n", b.length, width*height);
                
            } else if(dt == DataBuffer.TYPE_BYTE){
                
                DataBufferByte dbb = (DataBufferByte)db;
                byte[] b = dbb.getData();
                MipMapEntry me = new MipMapEntryByte(b, width, height, m_width/width);
                data.add(me);
                //printf("BYTE b.lengt: %d - %d\n", b.length, width*height);
            }
            
            if(width <= 1 || height <= 1)
                break;
            
            width = (width + 1)/2;
            height = (height + 1)/2;
            // scale down the image 
            if(m_pixelWeight != null)
                img = ImageUtil.getScaledImage(img, width, height, m_pixelWeight); 
            else 
                img = ImageUtil.getScaledImage(img, width, height); 
            if(DEBUG) try {ImageIO.write(img, "png", new File(fmt("/tmp/mipmap%02d.png",data.size())));} catch(Exception e){}
          
        }
        
        m_data = (MipMapEntry[])data.toArray(new MipMapEntry[data.size()]);
        
    }

  public void printData(int ind, int start, int count){

    MipMapEntry me = m_data[ind];
    me.printData(start, count);

  }
    int debugCount = 100;
    
    /**
       return interpolated color of given pixel 
       size - diameter of the averaging area in pixels 
       
    */
    public void getPixel(double x, double y, double probesize, double color[]){
        
        //printf("MipMap.getPixel(%10.5f, %10.5f, %10.5f)\n", x, y, probesize);
        
        // find mipmap entry, which has pixel size less, than probesize
        // and next mipmap entry has larger pixel size
        // linearly interpolate between results
        int upperIndex = -1;
        int lowerIndex = -1;
        
        for(int i = 0; i < m_data.length; i++){
            MipMapEntry me = m_data[i];
            if(probesize < me.pixelSize){
                upperIndex = i;
                break;
            }
        }
        
        switch(upperIndex){
        case 0: 
            {
                // probe is smaler than pixel size of the first image
                // no interpolation between maps 
                MipMapEntry me = m_data[0];
                //printf("map: %s\n", me);
                me.getPixel(x,y,color);
                return;
            }
        case -1:
            
            // probe is larger than pixel size of the last image 
            // no interpolation between maps 
            {
                
                MipMapEntry me = m_data[m_data.length-1];      
                
                //printf("map: %s\n", me);
                me.getPixel(me.width*x/m_width, me.height*y/m_height,color);      
                return;
            }
            
        default:
            {
                // we are btween the maps
                MipMapEntry me0 = m_data[upperIndex-1]; 
                MipMapEntry me1 = m_data[upperIndex];
                //printf("map0: %s\n", me0);
                //printf("map1: %s\n", me1);
                
                double color0[] = new double[4];
                double color1[] = new double[4];
                double w = (probesize-me0.pixelSize)/(me1.pixelSize-me0.pixelSize); // interpolation between maps 
                if(DEBUG && debugCount-- > 0){
                    printf("probe: %7.2f, w0: %7.2f w1:%7.2f %7.2f\n", probesize, me0.pixelSize, me1.pixelSize, w);
                }
                
                //printf("w: %s\n", w);
                
                //synchronized(this) {
                me0.getPixel(me0.width*x/m_width, me0.height*y/m_height, color0);      
                me1.getPixel(me1.width*x/m_width, me1.height*y/m_height,color1);      
                ImageUtil.lerpColors(color0, color1, w, color);
                
                return;
                //}
            }
        }
        
    }

    /**
       data representation for one level of mipmap
     */
    static abstract class MipMapEntry {
        
        double pixelSize; 
        int width, height; 
        abstract void getPixel(double x, double y, double color[]);
        
        void init(int width, int height, double pixelSize){ 
            this.width = width;
            this.height = height;
            this.pixelSize = pixelSize;
        }   
        void printData(int start, int count){
        }
        
        public String toString(){
            return fmt("%s[%dx%d:%8.5f]",this.getClass().getName(), width, height, pixelSize);
        }
        
    }
    
    static class MipMapEntryByte extends MipMapEntry {
        
        byte data[];
        int unitlength;
        
        MipMapEntryByte(byte data[], int width, int height, double pixelSize){
            this.data = data;
            init(width, height, pixelSize);
            unitlength = data.length/(width*height);
            
        }
        
        void printData(int start, int count){
            
            for(int i = start; i < count; i++){
                printf("%d: %x\n", i, data[i]);
            }
        }
        
        void getPixel(double x, double y, double color[]){
            
            x -= 0.5;
            y -= 0.5;
            
            int x0 = (int)Math.floor(x);
            int y0 = (int)Math.floor(y);
            int x1 = x0+1, y1 = y0+1;
            
            double dx = x-x0;
            double dy = y-y0;
            double dx1= 1-dx, dy1=1-dy;
            
            if(x0 < 0) x0 = width-1;
            if(y0 < 0) y0 = height-1;
            if(y0 >= height)y0 = 0;
            if(x0 >= width) x0 = 0;
            if(y1 >= height)y1 = 0;
            if(x1 >= width) x1 = 0;
            int offset00 = (y0 * width  + x0)*unitlength;
            int offset10 = (y0 * width  + x1)*unitlength;
            int offset01 = (y1 * width  + x0)*unitlength;
            int offset11 = (y1 * width  + x1)*unitlength;
            
            int a00, a01, a10, a11; 
            a00 = a01 = a10 = a11 = 255;
            boolean hasAlpha  = (unitlength == 4);
            try {
                if(hasAlpha) a00 = ((int)data[offset00++])& 0xff;    
                int b00 = ((int)data[offset00++])& 0xff;
                int g00 = ((int)data[offset00++])& 0xff;
                int r00 = ((int)data[offset00++])& 0xff;
                
                if(hasAlpha) a10 = ((int)data[offset10++])& 0xff;      
                int b10 = ((int)data[offset10++])& 0xff;
                int g10 = ((int)data[offset10++])& 0xff;
                int r10 = ((int)data[offset10++])& 0xff;
                
                if(hasAlpha) a01 = ((int)data[offset01++])& 0xff;      
                int b01 = ((int)data[offset01++])& 0xff;
                int g01 = ((int)data[offset01++])& 0xff;
                int r01 = ((int)data[offset01++])& 0xff;
                
                if(hasAlpha) a11 = ((int)data[offset11++])&0xff;      
                int b11 = ((int)data[offset11++])& 0xff;
                int g11 = ((int)data[offset11++])& 0xff;
                int r11 = ((int)data[offset11++]) & 0xff;
                
                color[0] = dy1*(dx1*r00 + dx * r10) + dy*(dx1*r01 + dx * r11);
                color[1] = dy1*(dx1*g00 + dx * g10) + dy*(dx1*g01 + dx * g11);
                color[2] = dy1*(dx1*b00 + dx * b10) + dy*(dx1*b01 + dx * b11);
                double alpha = 255;
                if(hasAlpha) alpha = dy1*(dx1*a00 + dx * a10) + dy*(dx1*a01 + dx * a11);
                
                color[3] = alpha;
                if(hasAlpha){
                    // premultiply by alpha 
                    alpha /= 255;
                    color[0] *= alpha; color[1] *= alpha; color[2] *= alpha; 
                }    
            } catch(Exception e){
                e.printStackTrace(System.out);
                printf("xo:%d, y0:%d, x1:%d y1:%d [%dx%d]\n", x0, y0, x1, y1, width, height);
            }
        }
    }
    
    static class MipMapEntryInt extends MipMapEntry {
        
        int data[];
        MipMapEntryInt(int data[], int width, int height, double pixelSize){
            this.data = data;
            init(width, height, pixelSize);
        }
        
        void getPixel(double x, double y, double color[]){
            //printf("map:(%s).getPixel(%10.5f,%10.5f)\n", this,x,y);      
            x -= 0.5;
            y -= 0.5;
            
            int x0 = (int)Math.floor(x);
            int y0 = (int)Math.floor(y);
            int x1 = x0+1, y1 = y0+1;
            
            double dx = x-x0;
            double dy = y-y0;
            double dx1= 1-dx, dy1=1-dy;
            
            if(x0 < 0) x0 = width-1;
            if(y0 < 0) y0 = height-1;
            if(y1 >= height)y1 = 0;
            if(x1 >= width) x1 = 0;
            
            int rgb00 = data[(y0 * width  + x0)];
            int rgb10 = data[(y0 * width  + x1)];
            int rgb01 = data[(y1 * width  + x0)];
            int rgb11 = data[(y1 * width  + x1)];
            
            color[0]   = dy1*(dx1*getRed(rgb00)   + dx * getRed(rgb10))   + dy*(dx1*getRed(rgb01)   + dx * getRed(rgb11));
            color[1] = dy1*(dx1*getGreen(rgb00) + dx * getGreen(rgb10)) + dy*(dx1*getGreen(rgb01) + dx * getGreen(rgb11));
            color[2]  = dy1*(dx1*getBlue(rgb00)  + dx * getBlue(rgb10))  + dy*(dx1*getBlue(rgb01)  + dx * getBlue(rgb11));
            double alpha = dy1*(dx1*getAlpha(rgb00) + dx * getAlpha(rgb10)) + dy*(dx1*getAlpha(rgb01) + dx * getAlpha(rgb11));
            color[3] = alpha;
            
            // premultiply by alpha 
            alpha /= 255;
            color[0] *= alpha; color[1] *= alpha; color[2] *= alpha; 
            
            //printf("[(%d, %d),(%d, %d)]\n", x0, y0, x1, y1);
            //printf("dx: %8.5f, dy: %8.5f\n", dx, dy);
            //printf("data(%d,%d): %x %x %x %x \n", x0, y0,rgb00, data01, data10, data11);
        }
    }
    
    public static String getTypeName(int t){
        switch(t){
        default: return "unknown";
        case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
        case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR";
        case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "4BYTE_ABGR_PRE";
        case BufferedImage.TYPE_BYTE_BINARY: return "BYTE_BINARY";
        case BufferedImage.TYPE_BYTE_GRAY: return "BYTE_GRAY";
        case BufferedImage.TYPE_BYTE_INDEXED: return "BYTE_INDEXED";
        case BufferedImage.TYPE_CUSTOM: return "CUSTOM";
        case BufferedImage.TYPE_INT_ARGB: return "INT_ARGB";
        case BufferedImage.TYPE_INT_ARGB_PRE: return "INT_ARGB_PRE";
        case BufferedImage.TYPE_INT_BGR: return "INT_BGR";
        case BufferedImage.TYPE_INT_RGB: return "INT_RGB";
        case BufferedImage.TYPE_USHORT_555_RGB: return "USHORT_555_RGB";
        case BufferedImage.TYPE_USHORT_565_RGB: return "USHORT_565_RGB";
        case BufferedImage.TYPE_USHORT_GRAY: return "USHORT_GRAY";
        }
    }
    
  
    /**
       
       linearly increasing weight for darker pixels
       
       

                /  weight
               /
              /
             /
            ------  255
           /
          /
  -------+-------- 128
        /
       / 
  ____/            1


    */
    public static class LinearPixelWeight implements PixelWeight {
        
        int m_weight = 128;
        
        public LinearPixelWeight(int weight){
            m_weight = weight;
        }
        
        //
        // input is not premult 
        //
        public int getWeight(int r, int g, int b, int a){
            
            if(a == 0){
                // transparent pixel is given minimal weight
                return 1;
            }
            int intens = ((255 - (r + g + b)/3));
            
            int w = ((intens - 127) * (m_weight-127))/128 + 127; 
            if(w < 1)
                w = 1;
            else if(w > 255) 
                w = 255;
            return w;
            
        }
    } //class LinearPixelWeight
  
  
}
