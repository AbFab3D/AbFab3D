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

package abfab3d.grid.op;


import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;

import java.io.File;
import java.util.Vector;

import javax.imageio.ImageIO;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;


import static abfab3d.util.ImageUtil.getRed;
import static abfab3d.util.ImageUtil.getGreen;
import static abfab3d.util.ImageUtil.getBlue;
import static abfab3d.util.ImageUtil.getAlpha;
import static abfab3d.util.MathUtil.clamp;

/**
   
   a collection of various DataSource 

   @author Vladimir Bulatov

 */
public class DataSources {
   
    /**

       makes solid block of given size
       
     */
    public static class Block implements DataSource, Initializable {

        public double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.1, m_centerX=0, m_centerY=0, m_centerZ=0;             
        
        private double xmin, xmax, ymin, ymax, zmin, zmax;
        
        /**
           
         */
        public void setSize(double sx, double sy, double sz){
            m_sizeX = sx;
            m_sizeY = sy;
            m_sizeZ = sz;
        }

        /**
           
         */
        public void setLocation(double x, double y, double z){
            m_centerX = x;
            m_centerY = y;
            m_centerZ = z;
        }

        /**
           
         */
        public int initialize(){
            
            xmin = m_centerX - m_sizeX/2;
            xmax = m_centerX + m_sizeX/2;

            ymin = m_centerY - m_sizeY/2;
            ymax = m_centerY + m_sizeY/2;

            zmin = m_centerZ - m_sizeZ/2;
            zmax = m_centerZ + m_sizeZ/2;

            return RESULT_OK;

        }

        /**
         * returns 1 if pnt is inside of block of given size and location
         * returns 0 otherwise
         */
        public int getDataValue(Vec pnt, Vec data) {

            double res = 1.;
            double 
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

            if(x < xmin || x > xmax ||
               y < ymin || y > ymax ||
               z < zmin || z > zmax)
                res = 0;

            data.v[0] = res;
            
            return RESULT_OK;
        }                
    }  // class Block 



    /**

       makes embossed image fron given file of given size 
       
     */
    public static class ImageBitmap implements DataSource, Initializable {
        
        public double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.001, m_centerX=0, m_centerY=0, m_centerZ=0; 
        
        public double m_baseThickness = 0.5; // relative thickness of solid base 
        public String m_imagePath; 

        public int m_imageType = IMAGE_POSITIVE;

        public static final int IMAGE_POSITIVE = 0, IMAGE_NEGATIVE = 1;
        
        
        
        public int m_xTilesCount = 1; // number of image tiles in x-direction 
        public int m_yTilesCount = 1; // number of image tiles in y-direction 
        
        protected BufferedImage m_image;

        private double xmin, xmax, ymin, ymax, zmin, zmax, zbase;
        
        private int imageWidth, imageHeight;
        private int imageData[]; 

        /**
           
         */
        public void setSize(double sx, double sy, double sz){
            m_sizeX = sx;
            m_sizeY = sy;
            m_sizeZ = sz;
        }

        /**
           
         */
        public void setLocation(double x, double y, double z){
            m_centerX = x;
            m_centerY = y;
            m_centerZ = z;
        }

        public void setTiles(int tx, int ty){

            m_xTilesCount = tx;
            m_yTilesCount = ty;
            
        }

        public void setBaseThickness(double baseThickness){
            
            m_baseThickness = baseThickness;
            
        }

        public void setImagePath(String path){
            
            m_imagePath = path;
            
        }

        public void setImage(BufferedImage image){

            m_image = image;

        }

        public void setImageType(int type){

            m_imageType = type; 

        }

        public int initialize(){
            
            xmin = m_centerX - m_sizeX/2;
            xmax = m_centerX + m_sizeX/2;

            ymin = m_centerY - m_sizeY/2;
            ymax = m_centerY + m_sizeY/2;

            zmin = m_centerZ - m_sizeZ/2;
            zmax = m_centerZ + m_sizeZ/2;
            zbase = zmin + (zmax - zmin)*m_baseThickness;

            BufferedImage image = null;
            if(m_image != null){
                image = m_image;
            } else if(m_imagePath == null){
                imageData = null; 
                return RESULT_OK;                
            } else {
                try {
                    image = ImageIO.read(new File(m_imagePath));
                } catch(Exception e){
                    imageData = null;
                    e.printStackTrace();
                    return RESULT_ERROR;
                }
            }

            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
            
            DataBuffer dataBuffer = image.getRaster().getDataBuffer();          
            imageData = new int[imageWidth * imageHeight];
            image.getRGB(0,0,imageWidth, imageHeight, imageData, 0, imageWidth);
            
            return RESULT_OK;
        }

        /**
         * returns 1 if pnt is inside of image
         * returns 0 otherwise
         */
        public int getDataValue(Vec pnt, Vec data) {
                        
            double res = 1.;
            double 
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

            x = (x-xmin)/(xmax-xmin);
            y = (y-ymin)/(ymax-ymin);
            
            if(x < 0 || x > 1 ||
               y < 0 || y > 1 ||
               z < zmin || z > zmax){
                data.v[0] = 0;
                return RESULT_OK;
            }

            if(imageData == null){               
                data.v[0] = 1;
                return RESULT_OK;
            }

            if(m_xTilesCount > 1){
                x *= m_xTilesCount;
                x -= Math.floor(x);
            }
            if(m_yTilesCount > 1){
                y *= m_yTilesCount;
                y -= Math.floor(y);
            }
                
            double imageX = imageWidth*x;
            double imageY = imageHeight*(1-y);// reverse Y-direction 

            double pixelValue = getPixelBox(imageX,imageY);

            // pixel value for black is 0 for white is 255;
            // we may need to reverse it
            switch(m_imageType){
            default: 
            case IMAGE_POSITIVE: 
                pixelValue = 1 - pixelValue/255.;
                break;
            case IMAGE_NEGATIVE:
                pixelValue = pixelValue/255.;
                break;
            }

            double d = (zbase  + (zmax - zbase)*pixelValue - z);

            if(d > 0)
                data.v[0] = 1;
            else 
                data.v[0] = 0;
            
            return RESULT_OK;
        }                
        
        double getPixelBox(double x, double y){

            int ix = clamp((int)Math.floor(x), 0, imageWidth-1);
            int iy = clamp((int)Math.floor(y), 0, imageHeight-1);
            
            int rgb00 = imageData[ix + iy * imageWidth];
            
            int red   = getRed(rgb00);
            int green = getGreen(rgb00);
            int blue  = getBlue(rgb00);

            double alpha = getAlpha(rgb00);
            
            return (red + green + blue)/3.;

        }

    }  // class ImageBitmap


    /**
       return 1 if any of input data sources is 1, return 0 if all data sources are 0 
       can be used to make union of few shapes        
     */
    public static class Union implements DataSource, Initializable {

        Vector<DataSource> dataSources = new Vector<DataSource>();
        
        public Union(){  

        }

        /**
           add items to set of data sources 
         */
        public void addDataSource(DataSource ds){

            dataSources.add(ds);

        }

        public int initialize(){

            for(int i = 0; i < dataSources.size(); i++){
                
                DataSource ds = dataSources.get(i);
                if(ds instanceof Initializable){
                    ((Initializable)ds).initialize();
                }
            }      
            return RESULT_OK;
            
        }
        

        /**
         * calculates values of all data sources and return maximal value
         * can be used to make union of few shapes 
         */
        public int getDataValue(Vec pnt, Vec data) {

            for(int i = 0; i < dataSources.size(); i++){
                
                DataSource ds = dataSources.get(i);
                int res = ds.getDataValue(pnt, data);
                if(res != RESULT_OK)
                    continue;
                
                if(data.v[0] > 0){
                    data.v[0] = 1;
                    return RESULT_OK;                    
                }
                
            }
            // we are here if none of dataSources return positive value 
            data.v[0] = 0;            
            return RESULT_OK;
        }        

    } // class Union

    
    /**
       Intersection of multiple data sourrces 
       return 1 if all data sources return 1
       return 0 otherwise
     */
    public static class Intersection implements DataSource, Initializable {

        Vector<DataSource> dataSources = new Vector<DataSource>();
        
        public Intersection(){  

        }

        /**
           add items to set of data sources 
         */
        public void addDataSource(DataSource ds){

            dataSources.add(ds);

        }

        public int initialize(){

            for(int i = 0; i < dataSources.size(); i++){
                
                DataSource ds = dataSources.get(i);
                if(ds instanceof Initializable){
                    ((Initializable)ds).initialize();
                }
            }      
            return RESULT_OK;
            
        }
        

        /**
         * calculates values of all data sources and return maximal value
         * can be used to make union of few shapes 
         */
        public int getDataValue(Vec pnt, Vec data) {

            for(int i = 0; i < dataSources.size(); i++){
                
                DataSource ds = dataSources.get(i);
                int res = ds.getDataValue(pnt, data);
                if(res != RESULT_OK){
                    data.v[0] = 0;
                    return res;
                }
                
                if(data.v[0] <= 0.){
                    data.v[0] = 0;
                    return RESULT_OK;                    
                }
                
            }
            // we are here if none of dataSources return 0
            data.v[0] = 2;            
            return RESULT_OK;
        }        

    } // class Intersection


    /**
       subtracts (dataSource1 - dataSource2)       
       can be usef for boolean difference 
     */
    public static class Subtraction implements DataSource, Initializable {

        DataSource dataSource1;
        DataSource dataSource2;
        
        public Subtraction(){     

        }

        public void setSources(DataSource ds1, DataSource ds2){

            dataSource1 = ds1;
            dataSource2 = ds2;

        }
        

        public int initialize(){

            if(dataSource1 != null && dataSource1 instanceof Initializable){
                ((Initializable)dataSource1).initialize();
            }
            if(dataSource2 != null && dataSource2 instanceof Initializable){
                ((Initializable)dataSource2).initialize();
            }
            return RESULT_OK;
            
        }
        
        /**
         * calculates values of all data sources and return maximal value
         * can be used to make union of few shapes 
         */
        public int getDataValue(Vec pnt, Vec data) {
            
            //workPnt.set(pnt);
            int res = dataSource1.getDataValue(pnt, data);

            if(res != RESULT_OK || data.v[0] <= 0.0){
                data.v[0] = 0.0;
                return res;
            }
            // we are here if data.v[0] > 0
            
            res = dataSource2.getDataValue(pnt, data);
            if(res != RESULT_OK)
                return res;
            
            if(data.v[0] > 0){
                // we should return 0
                data.v[0] = 0;
            } else {
                data.v[0] = 1;                    
            }                    
            
            return RESULT_OK;
        }        

    } // class Subtraction

    
    /**
       class to accept generic DataSource and VecTransform 
       
       in getDataValue() it applued inverse_transform to the point and calcylates data value in 
       transformed point
       
     */
    public static class DataTransformer implements DataSource, Initializable {
        
        protected DataSource dataSource;
        protected VecTransform transform;
        
        public DataTransformer(){  
        }

        public void setDataSource(DataSource ds){
            dataSource = ds;
        }

        public void setTransform(VecTransform vt){
            transform = vt;
        }

        public int initialize(){
            
            if(dataSource != null && dataSource instanceof Initializable){
                ((Initializable)dataSource).initialize();
            }
            if(transform != null && transform instanceof Initializable){
                ((Initializable)transform).initialize();
            }               
            return RESULT_OK;            
        }
        

        /**
         * calculates values of all data sources and return maximal value
         * can be used to make union of few shapes 
         */
        public int getDataValue(Vec pnt, Vec data) {

            Vec workPnt = new Vec(pnt);

            if(transform != null){
                int res = transform.inverse_transform(pnt, workPnt);
                if(res != RESULT_OK){
                    data.v[0] = 0;                    
                    return res;
                }
            }               
            
            if(dataSource != null){
                return dataSource.getDataValue(workPnt, data);
            } else {
                data.v[0] = 1;
                return RESULT_OK;
            }
        }        

    } // class DataTransformer
 
   
}

