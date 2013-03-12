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
import abfab3d.util.ImageMipMap;

import abfab3d.util.ImageUtil;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

import static abfab3d.util.ImageUtil.getRed;
import static abfab3d.util.ImageUtil.getGreen;
import static abfab3d.util.ImageUtil.getBlue;
import static abfab3d.util.ImageUtil.getAlpha;
import static abfab3d.util.ImageUtil.RED;
import static abfab3d.util.ImageUtil.GREEN;
import static abfab3d.util.ImageUtil.BLUE;
import static abfab3d.util.ImageUtil.ALPHA;

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

        private double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.1, m_centerX=0, m_centerY=0, m_centerZ=0;             
        
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
        public static final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1, INTERPOLATION_MIPMAP = 2;

                
        public int m_xTilesCount = 1; // number of image tiles in x-direction 
        public int m_yTilesCount = 1; // number of image tiles in y-direction 
        
        private BufferedImage m_image;
        private int m_interpolationType = INTERPOLATION_BOX;
        // probe size in world units
        private double m_probeSize = 1.e-4; // 0.1mm
        // probe size in image pixels, initiliazed in init()
        private double m_probeSizeImage=0;         
        // scaling to convert probe size into pixels of image 
        private double m_probeScale; 
        
        private double xmin, xmax, ymin, ymax, zmin, zmax, zbase;
        private double xscale, yscale;
        private boolean useGrayscale = true;
        private int imageWidth, imageHeight;
        //private int imageData[]; 
        private byte imageDataByte[]; 
        private ImageMipMap m_mipMap;
        private double m_pixelWeightNonlinearity = 0.;
        // solid white color of background to be used fpr images with transparency
        private double m_backgroundColor[] = new double[]{255.,255.,255.,255.};
        private int m_backgroundColorInt = 0xFFFFFFFF;

        double m_imageThreshold = 0.5; // below threshold we have solid voxel, above - empty voxel  

        // scratch vars
        double[] ci = new double[4];
        double[] cc = new double[4];
        double color[] = new double[4];

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

        public void setUseGrayscale(boolean value){
            useGrayscale = value;
        }

        public void setInterpolationType(int type){

            m_interpolationType = type;

        }

        /**
           value = 0 - linear resampling for mipmap
           value > 0 - black pixels are givewn heigher weight 
           value < 0 - white pixels are givewn heigher weight 
         */
        public void setPixelWeightNonlinearity(double value){
            m_pixelWeightNonlinearity = value;        
        }

        public void setProbeSize(double size){

            m_probeSize = size;
            m_probeSizeImage = m_probeSize*m_probeScale;
        }

        public int initialize(){
            
            xmin = m_centerX - m_sizeX/2;
            xmax = m_centerX + m_sizeX/2;
            xscale = 1./(xmax-xmin);
            

            ymin = m_centerY - m_sizeY/2;
            ymax = m_centerY + m_sizeY/2;
            yscale = 1./(ymax-ymin);

            zmin = m_centerZ - m_sizeZ/2;
            zmax = m_centerZ + m_sizeZ/2;
            zbase = zmin + (zmax - zmin)*m_baseThickness;

            int imageData[] = null;

            long t0 = time();

            BufferedImage image = null;
            if(m_image != null){
                // image was supplied via memory 
                image = m_image;
            } else if(m_imagePath == null){
                imageDataByte = null; 
                return RESULT_OK;                
            } else {
                try {
                    image = ImageIO.read(new File(m_imagePath));
                } catch(Exception e){
                    System.out.println("Cannot load image: " + m_imagePath);
                    imageData = null;
                    e.printStackTrace();
                    return RESULT_ERROR;
                }
            }
            printf("reading image %d ms\n", (time() - t0));
            if(m_interpolationType == INTERPOLATION_MIPMAP){
                
                t0 = time();
                m_mipMap = new ImageMipMap(image, m_pixelWeightNonlinearity);
                printf("mipmap initialization %d ms\n", (time() - t0));

            }else {
                m_mipMap = null;
            }
            
            t0 = time();
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            // convert probe size in meters into image units 
            m_probeScale = xscale*imageWidth;
            m_probeSizeImage = m_probeSize*m_probeScale;

            DataBuffer dataBuffer = image.getRaster().getDataBuffer();          
            imageData = new int[imageWidth * imageHeight];
            image.getRGB(0,0,imageWidth, imageHeight, imageData, 0, imageWidth);

            printf("image.getRGB() %d ms\n", (time() - t0));
            t0 = time();

            int len = imageData.length; 
            imageDataByte = new byte[len];

            for(int i = 0; i < len; i++){
                // convert data into grayscale 
                imageDataByte[i] = (byte)ImageUtil.getCombinedGray(m_backgroundColorInt, imageData[i]);                
            }
            printf("gray scale data initialization %d ms\n", (time() - t0));
            
            imageData = null;
            
            return RESULT_OK;
        }

        /**
         * returns 1 if pnt is inside of image
         * returns 0 otherwise
         */
        public int getDataValue(Vec pnt, Vec data) {
                        
            // TODO get proper pointer size from chain of transforms

            //double probeSize = pnt.probeSize*probeScale;

            double
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

            x = (x-xmin)*xscale;
            y = (y-ymin)*yscale;
            
            if(x < 0 || x > 1 ||
               y < 0 || y > 1 ||
               z < zmin || z > zmax){
                data.v[0] = 0;
                return RESULT_OK;
            }
            
            if(m_baseThickness != 0.0){
                if( z < zbase){
                    data.v[0] = 1;
                    return RESULT_OK;                    
                }                    
            }

            if(imageDataByte == null){               
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

            double pixelValue = getPixelValue(imageX, imageY, m_probeSizeImage);
            
            double d = 0;

            if(useGrayscale){ 
                // smooth transition 
                d = (zbase  + (zmax - zbase)*pixelValue - z); 
                if( d > 0)
                    data.v[0] = 1;
                else 
                    data.v[0] = 0;

            } else {  
                // sharp transition
                d = pixelValue;
                if(d > m_imageThreshold)
                    data.v[0] = 1;
                else 
                    data.v[0] = 0;
            }
        
            return RESULT_OK;
        }                

        double getPixelValue(double x, double y, double probeSize){
            
            double grayLevel;

            switch(m_interpolationType){

            case INTERPOLATION_MIPMAP:
                m_mipMap.getPixel(x, y, probeSize, color);
                grayLevel = (color[0] + color[1] + color[2])/3.;
                break;
            default: 

            case INTERPOLATION_BOX:
                grayLevel = (0xFF) & ((int)imageDataByte[(int)x + (int)y * imageWidth]);
                //grayLevel = ImageUtil.getCombinedGray(m_backgroundColorInt, imageData[(int)x + (int)y * imageWidth]); 
                break;
            case INTERPOLATION_LINEAR:
                grayLevel = getPixelLinear(x,y);
                break;
            }
                        

            // pixel value for black is 0 for white is 255;
            // we may need to reverse it
            double pv = 0.;
            switch(m_imageType){
            default: 
            case IMAGE_POSITIVE: 
                pv = 1 - grayLevel/255.;
                break;
            case IMAGE_NEGATIVE:
                pv = grayLevel/255.;
                break;
            }
            return pv;
        }
        
        // linear approximation
        double getPixelLinear(double x, double y){
            //TODO 
            return getPixelBox(x,y);
        }

        double getPixelBox(double x, double y){

            return getPixelBoxInt(x,y);

        }

        // BOX approximation with double colors 
        double getPixelBoxDouble(double x, double y){

            int ix = clamp((int)Math.floor(x), 0, imageWidth-1);
            int iy = clamp((int)Math.floor(y), 0, imageHeight-1);
            
            int rgb = (0xFF) & ((int)imageDataByte[ix + iy * imageWidth]);
            return rgb;
            //ImageUtil.getPremultColor(rgb, ci);
            //ImageUtil.combinePremultColors(m_backgroundColor, ci, cc, ci[ALPHA]);            
            //return (cc[RED] + cc[GREEN] + cc[BLUE])/3.;

        }
        // BOX approximation with int colors 
        int getPixelBoxInt(double x, double y){

            int ix = (int)x;
            int iy = (int)y;

            //int ix = clamp((int)Math.floor(x), 0, imageWidth-1);
            //int iy = clamp((int)Math.floor(y), 0, imageHeight-1);
            //return imageData[ix + iy * imageWidth];

            int rgb = 0xFF & (int)imageDataByte[ix + iy * imageWidth];
            return ImageUtil.getCombinedGray(m_backgroundColorInt, rgb);

        }

    }  // class ImageBitmap

    /*
    public static class ImageBitmap_YUP implements DataSource, Initializable {

        public double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.001, m_centerX=0, m_centerY=0, m_centerZ=0;

        public double m_baseThickness = 0.5; // relative thickness of solid base
        public String m_imagePath;

        public int m_imageType = IMAGE_POSITIVE;

        public static final int IMAGE_POSITIVE = 0, IMAGE_NEGATIVE = 1;
        public static final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1, INTERPOLATION_MIPMAP = 2;


        public int m_xTilesCount = 1; // number of image tiles in x-direction
        public int m_zTilesCount = 1; // number of image tiles in z-direction

        private BufferedImage m_image;
        private int m_interpolationType = INTERPOLATION_BOX;
        private double m_probeSize = 1.e-4; // 0.1mm

        private double xmin, xmax, ymin, ymax, zmin, zmax, ybase;
        private double xscale, zscale;
        private double probeScale; // scaling to convert probe size into pixels of image
        private boolean useGrayscale = true;
        private int imageWidth, imageHeight;
        private int imageData[];
        private ImageMipMap m_mipMap;
        private double m_pixelWeightNonlinearity = 0.;
        // solid white color of background to be used fpr images with transparency
        private double m_backgroundColor[] = new double[]{255.,255.,255.,255.};

        double m_imageThreshold = 0.5; // below threshold we have solid voxel, above - empty voxel
        double probeSize;

        // Scratch vars
        double cc[] = new double[4];
        double ci[] = new double[4];
        double color[] = new double[4];

        public void setSize(double sx, double sy, double sz){
            m_sizeX = sx;
            m_sizeY = sy;
            m_sizeZ = sz;
        }

        public void setLocation(double x, double y, double z){
            m_centerX = x;
            m_centerY = y;
            m_centerZ = z;
        }

        public void setTiles(int tx, int tz){

            m_xTilesCount = tx;
            m_zTilesCount = tz;

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

        public void setUseGrayscale(boolean value){
            useGrayscale = value;
        }

        public void setInterpolationType(int type){

            m_interpolationType = type;

        }

        
        //   value = 0 - linear resampling for mipmap
        // value > 0 - black pixels are givewn heigher weight
        // value < 0 - white pixels are givewn heigher weight
        // 
        public void setPixelWeightNonlinearity(double value){
            m_pixelWeightNonlinearity = value;
        }

        public void setProbeSize(double size){

            m_probeSize = size;
            probeSize = m_probeSize*probeScale;
        }

        public int initialize(){

            xmin = m_centerX - m_sizeX/2;
            xmax = m_centerX + m_sizeX/2;
            xscale = 1./(xmax-xmin);


            ymin = m_centerY - m_sizeY/2;
            ymax = m_centerY + m_sizeY/2;
            ybase = ymin + (ymax - ymin)*m_baseThickness;

            zmin = m_centerZ - m_sizeZ/2;
            zmax = m_centerZ + m_sizeZ/2;
            zscale = 1./(zmax-zmin);

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

            if(m_interpolationType == INTERPOLATION_MIPMAP){
                m_mipMap = new ImageMipMap(image, m_pixelWeightNonlinearity);
            }else {
                m_mipMap = null;
            }

            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            // convert probe size in meters into image units
            probeScale = xscale*imageWidth;
            probeSize = m_probeSize*probeScale;

            DataBuffer dataBuffer = image.getRaster().getDataBuffer();
            imageData = new int[imageWidth * imageHeight];
            image.getRGB(0,0,imageWidth, imageHeight, imageData, 0, imageWidth);

            return RESULT_OK;
        }

        //
        //  returns 1 if pnt is inside of image
        //  returns 0 otherwise
        //
        public int getDataValue(Vec pnt, Vec data) {

            // TODO get proper pointer size from chain of transforms
            //double probeSize = pnt.probeSize*probeScale;

            double
                    x = pnt.v[0],
                    y = pnt.v[1],
                    z = pnt.v[2];

            x = (x-xmin)*xscale;
            z = (z-zmin)*zscale;

            if(x < 0 || x > 1 || z < 0 || z > 1 ||  y < ymin || y > ymax){
                data.v[0] = 0;
                return RESULT_OK;
            }

            if(m_baseThickness != 0.0){
                if( y < ybase){
                    data.v[0] = 1;
                    return RESULT_OK;
                }
            }

            if(imageData == null){
                data.v[0] = 1;
                return RESULT_OK;
            }

            if(m_xTilesCount > 1){
                x *= m_xTilesCount;
                x -= Math.floor(x);
            }
            if(m_zTilesCount > 1){
                z *= m_zTilesCount;
                z -= Math.floor(y);
            }

            double imageX = imageWidth*x;
            double imageY = imageHeight*(1-z);// reverse Y-direction

            double pixelValue = getPixelValue(imageX, imageY, probeSize);

            double d;

            if(useGrayscale){
                d = (ybase  + (ymax - ybase)*pixelValue - z);
                if( d > 0)
                    data.v[0] = 1;
                else
                    data.v[0] = 0;

            } else {  // sharp threshold
                d = pixelValue;
                if(d > m_imageThreshold)
                    data.v[0] = 1;
                else
                    data.v[0] = 0;
            }

            return RESULT_OK;
        }

        double getPixelValue(double x, double y, double probeSize){

            double grayLevel;
            switch(m_interpolationType){
                case INTERPOLATION_MIPMAP:
                    m_mipMap.getPixel(x, y, probeSize, color);
                    grayLevel = (color[0] + color[1] + color[2])/3.;
                    break;
                default:
                case INTERPOLATION_BOX:
                    grayLevel = getPixelBox(x,y);
                    break;
                case INTERPOLATION_LINEAR:
                    grayLevel = getPixelLinear(x,y);
                    break;
            }


            // pixel value for black is 0 for white is 255;
            // we may need to reverse it
            double pv = 0.;
            switch(m_imageType){
                default:
                case IMAGE_POSITIVE:
                    pv = 1 - grayLevel/255.;
                    break;
                case IMAGE_NEGATIVE:
                    pv = grayLevel/255.;
                    break;
            }
            return pv;
        }

        // linear approximation
        double getPixelLinear(double x, double y){
            //TODO
            return getPixelBox(x,y);
        }

        // BOX approximation
        double getPixelBox(double x, double y){

            int ix = clamp((int)Math.floor(x), 0, imageWidth-1);
            int iy = clamp((int)Math.floor(y), 0, imageHeight-1);

            int rgb = imageData[ix + iy * imageWidth];

            // ci and cc does not need to be initialized as all values are assigned.
            ImageUtil.getPremultColor(rgb, ci);
            ImageUtil.combinePremultColors(m_backgroundColor, ci, cc, ci[ALPHA]);

            return (cc[RED] + cc[GREEN] + cc[BLUE])/3.;

        }

    }  // class ImageBitmap_VUP
    */

    /**
       return 1 if any of input data sources is 1, return 0 if all data sources are 0 
       can be used to make union of few shapes        
     */
    public static class Union implements DataSource, Initializable {

        Vector<DataSource> dataSources = new Vector<DataSource>();
        // fixed vector for calculations 
        DataSource vDataSources[]; 


        public Union(){  

        }

        /**
           add items to set of data sources 
         */
        public void addDataSource(DataSource ds){

            dataSources.add(ds);

        }

        public int initialize(){
            
            vDataSources = (DataSource[])dataSources.toArray(new DataSource[dataSources.size()]);
           
            for(int i = 0; i < vDataSources.length; i++){
                
                DataSource ds = vDataSources[i];
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

            // TODO - garbage generation 
            Vec workPnt = new Vec(pnt);

            int len = vDataSources.length;
            DataSource dss[] = vDataSources;
            
            for(int i = 0; i < len; i++){
                
                DataSource ds = dss[i];
                int res = ds.getDataValue(pnt, workPnt);
                if(res != RESULT_OK)
                    continue;
                
                if(workPnt.v[0] > 0){
                    data.v[0] = 1;
                    return RESULT_OK;                    
                }
                
            }
            // we are here if none of hhe dataSources return positive value 
            data.v[0] = 0;            
            return RESULT_OK;
        }        

    } // class Union


    /**
       does boolean complement 
     */
    public static class Complement implements DataSource, Initializable {

        DataSource dataSource = null;
        
        public Complement(){  

        }

        /**
           add items to set of data sources 
         */
        public void setDataSource(DataSource ds){

            dataSource = ds;

        }

        public int initialize(){
            
            if(dataSource instanceof Initializable){
                ((Initializable)dataSource).initialize();
            }
            
            return RESULT_OK;
            
        }
        

        /**
         * calculates complement of given data 
           replaces 1 to 0 and 0 to 1
         */
        public int getDataValue(Vec pnt, Vec data) {

            int res = dataSource.getDataValue(pnt, data);
            if(res != RESULT_OK){
                data.v[0] = 1;
                return res;
            } else {
                // we have good result
                // do complement 
                data.v[0] = 1-data.v[0];            
                return RESULT_OK;
            }        
        }
    } // class Complement

    
    /**
       Intersection of multiple data sourrces 
       return 1 if all data sources return 1
       return 0 otherwise
     */
    public static class Intersection implements DataSource, Initializable {

        Vector<DataSource> dataSources = new Vector<DataSource>();
        // fixed vector for calculations 
        DataSource vDataSources[];

        public Intersection(){  

        }

        /**
           add items to set of data sources 
         */
        public void addDataSource(DataSource ds){

            dataSources.add(ds);

        }

        public int initialize(){

            vDataSources = (DataSource[])dataSources.toArray(new DataSource[dataSources.size()]);
           
            for(int i = 0; i < vDataSources.length; i++){
                
                DataSource ds = vDataSources[i];
                if(ds instanceof Initializable){
                    ((Initializable)ds).initialize();
                }
            }      
            return RESULT_OK;
            
        }
        

        /**
         * calculates intersection of all values
         * 
         */
        public int getDataValue(Vec pnt, Vec data) {

            // TODO - garbage generation 
            //Vec workPnt = new Vec(pnt);
            
            DataSource dss[] = vDataSources;
            int len = dss.length;
            for(int i = 0; i < len; i++){
                
                DataSource ds = dss[i];
                //int res = ds.getDataValue(pnt, workPnt);
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
            data.v[0] = 1; 
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
            // TODO - garbage generation 
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


    /**
       ring in XZ plane of given radius, width and thickness 
       
     */
    public static class Ring implements DataSource{

        double width2;
        double innerRadius2;
        double exteriorRadius2;
        
        public Ring(double innerRadius, double thickeness, double width){  

            width2 = width/2;
            innerRadius2 = innerRadius*innerRadius;

            exteriorRadius2 = innerRadius + thickeness;
            
            exteriorRadius2 *= exteriorRadius2;
            
        }


        /**
         * calculates values of all data sources and return maximal value
         * can be used to make union of few shapes 
         */
        public int getDataValue(Vec pnt, Vec data) {
            //if(true){
            //    data.v[0] = 1;
            //    return RESULT_OK;             
            //}
            double y = pnt.v[1];
            if(y < -width2 || y > width2){
                data.v[0] = 0;
                return RESULT_OK;
            }
                
            double x = pnt.v[0];
            double z = pnt.v[2];
            double r2 = x*x + z*z;

            if(r2 < innerRadius2 || r2 > exteriorRadius2){
                data.v[0] = 0;
                return RESULT_OK;                
            }
            // we are inside 
            data.v[0] = 1;
            return RESULT_OK;             
        }        

    } // class Ring


    
}

