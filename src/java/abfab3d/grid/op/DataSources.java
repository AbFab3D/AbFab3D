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
        
        private double 
            xmin, 
            xmax,
            ymin,
            ymax,
            zmin, zmax;
        
        public Block(){
        }

        /**
           makes block with given center and size 
         */
        public Block(double x, double y, double z, double sx, double sy, double sz){

            setLocation(x,y,z);
            setSize(sx, sy, sz);

        }

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

            double vs = pnt.voxelSize;

            if(vs == 0.){
                if(x < xmin || x > xmax ||
                   y < ymin || y > ymax ||
                   z < zmin || z > zmax ){
                    data.v[0] = 0.;
                    return RESULT_OK;                
                } else {
                    data.v[0] = 1.;
                    return RESULT_OK;                                    
                }                
            }

            double vs2 = 2*vs; 
            double vxi = step((x-(xmin-vs))/(vs2));
            double vxa = step(((xmax+vs)-x)/(vs2));
            double vyi = step((y-(ymin-vs))/(vs2));
            double vya = step(((ymax+vs)-y)/(vs2));
            double vzi = step((z-(zmin-vs))/(vs2));
            double vza = step(((zmax+vs)-z)/(vs2));
            
            vxi *= vxa;
            vyi *= vya;
            vzi *= vza;
            

            data.v[0] = vxi*vyi*vzi;
            
            return RESULT_OK;
        }                
    }  // class Block 

    /**
       Ball with given location and radius 
     */
    public static class Ball implements DataSource {
        
        private double R, R2, RR;
        
        private double x0, y0, z0;
        
        public Ball(double x0, double y0, double z0, double r){
            R = r;
            R2 = 2*r;
            RR = r*r;
        }
        
        /**
         * returns 1 if pnt is inside of ball
         * returns intepolated value if poiunt is within voxel size to the boundary 
         * returns 0 if pnt is outside the ball
         */
        public int getDataValue(Vec pnt, Vec data) {

            double res = 1.;
            double 
                x = pnt.v[0]-x0,
                y = pnt.v[1]-y0,
                z = pnt.v[2]-z0;

            // good approximation to the distance to the surface of the ball).x 
            double dist = ((x*x + y*y + z*z) - RR)/(R2);
            //double dist = (Math.sqrt(x*x + y*y + z*z) - R);//)/(R2);

            double vs = pnt.voxelSize;
            if(dist <= -vs){
                data.v[0] = 1;
            } else if(dist >= vs){
                data.v[0] = 0;                
            } else {// we are near the surface - return interpolation 
                data.v[0] = interpolate_linear(dist/vs);
                //data.v[0] = interpolate_cubic(dist/vs);
            }
                
            
            return RESULT_OK;
        }                
    }  // class Ball 


    /**

       makes embossed image from given file of given size 
       
     */
    public static class ImageBitmap implements DataSource, Initializable {
        
        public static final int IMAGE_TYPE_EMBOSSED = 0, IMAGE_TYPE_ENGRAVED = 1;
        public static final int IMAGE_PLACE_TOP = 0, IMAGE_PLACE_BOTTOM = 1, IMAGE_PLACE_BOTH = 2; 
        public static final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1, INTERPOLATION_MIPMAP = 2;

       static final double PIXEL_NORM = 1./255.;
                
        protected double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.001, m_centerX=0, m_centerY=0, m_centerZ=0; 
        
        protected double m_baseThickness = 0.5; // relative thickness of solid base 
        protected String m_imagePath; 
        
        protected int m_imageType = IMAGE_TYPE_EMBOSSED;
        protected int m_imagePlace = IMAGE_PLACE_TOP; 
        

        protected int m_xTilesCount = 1; // number of image tiles in x-direction 
        protected int m_yTilesCount = 1; // number of image tiles in y-direction 

        
        private BufferedImage m_image;
        private int m_interpolationType = INTERPOLATION_LINEAR;//INTERPOLATION_BOX;
        // probe size in world units
        private double m_probeSize = 1.e-4; // 0.1mm
        // probe size in image pixels, initiliazed in init()
        private double m_probeSizeImage=0;         
        // scaling to convert probe size into pixels of image 
        private double m_probeScale; 
        
        private double xmin, xmax, ymin, ymax, zmin, zmax;
        private double xscale, yscale, zscale;
        private boolean useGrayscale = true;
        private int imageWidth, imageHeight;
        //private int imageData[]; 
        private byte imageDataByte[]; 
        private ImageMipMap m_mipMap;
        private double m_pixelWeightNonlinearity = 0.;
        // solid white color of background to be used for images with transparency
        private double m_backgroundColor[] = new double[]{255.,255.,255.,255.};
        private int m_backgroundColorInt = 0xFFFFFFFF;
        
        private double imageThickness;

        private double m_imageThreshold = 0.5; // below threshold we have solid voxel, above - empty voxel  

        // scratch vars
        //double[] ci = new double[4];
        //double[] cc = new double[4];
        double color[] = new double[4];
        
        public ImageBitmap(){
        }

        public ImageBitmap(String imagePath, double sx, double sy, double sz){
            
            setImagePath(imagePath);
            setSize(sx, sy, sz);
            
        }


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

        /**

           possible values IMAGE_TYPE_EMBOSS, IMAME_TYPE_ENGRAVE
         */
        public void setImageType(int type){

            m_imageType = type; 

        }

        /**
           possible values: IMAGE_PLACE_TOP, IMAGE_PLACE_BOTTOM, IMAGE_PLACE_BOTH
         */
        public void setImagePlace(int place){

            m_imagePlace = place; 

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
            
            xmin = m_centerX - m_sizeX/2.;
            xmax = m_centerX + m_sizeX/2.;
            xscale = 1./(xmax-xmin);
            

            ymin = m_centerY - m_sizeY/2.;
            ymax = m_centerY + m_sizeY/2.;
            yscale = 1./(ymax-ymin);

            zmin = m_centerZ - m_sizeZ/2.;
            zmax = m_centerZ + m_sizeZ/2.;
            zscale = 1./(zmax-zmin);            

            imageThickness = 1. - m_baseThickness;

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
            printf("image: [%d x %d]\n", imageWidth, imageHeight);

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

            double vs = pnt.voxelSize;
            if(vs == 0.)
                return getDataValueZeroVoxel(pnt, data);
            else 
                return getDataValueFiniteVoxel(pnt, data);
        }

        
        /**
           calculation for finite voxel size 
         */
        
        public int getDataValueFiniteVoxel(Vec pnt, Vec data){

            double
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

            double vs = pnt.voxelSize; 
            
            double imageZmin = zmin + (zmax-zmin)*m_baseThickness;

            double baseValue = getBox(x,y,z, xmin, xmax, ymin, ymax, zmin, imageZmin, vs);
            double dx = vs;

            double scale = 1;

            double h0 = scale*getHeightFieldValue(x,y);
            double hx = scale*getHeightFieldValue(x+dx,y);
            double hy = scale*getHeightFieldValue(x,y+dx);

            // normal to the plane via 3 points:  (-(hx-h0), -(hy-h0), vs)
            double nx = -(hx-h0);
            double ny = -(hy-h0);
            double nz = dx;
            if(!(nx == 0. && ny == 0.)){
                // grayscale OFF 
                //nz = 0.;
            }

            double nn = Math.sqrt(nx*nx + ny*ny + nz*nz);
            
            //point on the surface p: (x,y,h0)
            // dstance from point to surface  ((p-p0), n)                
            double dist = ((z - h0)*vs)/nn;
            double hfValue = 0;
            if(dist <= -vs) 
                hfValue = 1;
            else if(dist >= vs)    
                hfValue = 0;
            else 
                hfValue = (1. - (dist/vs))/2;            

            hfValue *= intervalCap(z, imageZmin-vs, zmax, vs) * intervalCap(x, xmin, xmax, vs) * intervalCap(y, ymin, ymax, vs);
            
            if(baseValue > hfValue){
                data.v[0] = baseValue;
            } else {
                data.v[0] = hfValue; 
            }

            return RESULT_OK; 
            
        }
        

        double getHeightFieldValue(double x,double y){

            double v = getPixelValue((x-xmin)*xscale*imageWidth, 
                                     (y-ymin)*yscale*imageHeight, 
                                     1.);
            //v /= 255.;
            
            //v = (1.-v);

            v = zmin + (zmax-zmin)*(1-m_baseThickness)*v;

            /*
            double v = 7*Math.sin(500*x) * Math.sin(500*y);
            v *= v;

            v *= (zmax-zmin)*(1-m_baseThickness);
            */

            return v;

        }

        /**
           calculation for zero voxel size 
         */
        public int getDataValueZeroVoxel(Vec pnt, Vec data){
            
            double
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

            x = (x-xmin)*xscale;
            y = (y-ymin)*yscale;
            z = (z-zmin)*zscale;
            
            if(x < 0. || x > 1. ||
               y < 0. || y > 1. ||
               z < 0. || z > 1.){
                data.v[0] = 0;
                return RESULT_OK;
            }
            // z is in range [0, 1]
            switch(m_imagePlace){
            default:
            case IMAGE_PLACE_TOP:
                z = (z - m_baseThickness)/imageThickness;
                break;
            case IMAGE_PLACE_BOTTOM:
                z = ((1-z) - m_baseThickness)/imageThickness;
                break;
            case IMAGE_PLACE_BOTH:
                //scale and make symmetrical
                z = (2*z-1); 
                if( z < 0.) z = -z;
                z = (z-m_baseThickness)/imageThickness;
                break;
            }

            if(z < 0.0){
                data.v[0] = 1;
                return RESULT_OK;                                  
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
            double imageY = imageHeight*(1.-y);// reverse Y-direction 

            double pixelValue = getPixelValue(imageX, imageY, m_probeSizeImage);
            
            double d = 0;

            if(useGrayscale){ 
                
                // smooth transition 
                d = z - pixelValue; 
                if( d < 0) // we are inside 
                    data.v[0] = 1;
                else   // we are outside 
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
        
        /**
           returns value of pixel at given x,y location. value normalized to (0,1)
         */
        double getPixelValue(double x, double y, double probeSize){
            
            double grayLevel;

            switch(m_interpolationType){

            case INTERPOLATION_MIPMAP:
                m_mipMap.getPixel(x, y, probeSize, color);
                grayLevel = (color[0] + color[1] + color[2])/3.;
                break;
            default: 

            case INTERPOLATION_BOX:
                
                grayLevel = (0xFF) & ((int)imageDataByte[clamp((int)x,0,imageWidth-1)  + clamp((int)y,0,imageHeight-1) * imageWidth]);
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
            case IMAGE_TYPE_EMBOSSED: 
                pv = 1. - grayLevel*PIXEL_NORM;
                break;
            case IMAGE_TYPE_ENGRAVED:
                pv = grayLevel*PIXEL_NORM;
                break;
            }
            return pv;
        }
        
        // linear approximation
        double getPixelLinear(double x, double y){
            x = x -  imageWidth * Math.floor(x/imageWidth);
            y = y - imageHeight * Math.floor(y/imageHeight);            

            int ix = (int)x;
            int iy = (int)y;
            
            double dx = x-ix;
            double dy = y-iy;
            double dx1 = 1-dx;
            double dy1 = 1-dy;


            int i0 = ix  + iy * imageWidth;
            double v00 = 0., v10 = 0., v01 = 0., v11 = 0.;

            try {
                v00 = (0xFF) & ((int)imageDataByte[i0]);
                v10 = (0xFF) & ((int)imageDataByte[i0 + 1]);
                v01 = (0xFF) & ((int)imageDataByte[i0 + imageWidth]);
                v11 = (0xFF) & ((int)imageDataByte[i0 + 1 + imageWidth]);
            } catch (Exception e){}
            return v00 * dx1 * dy1 + v11 * dx * dy + v10 * dx * dy1 + v01 * dx1 * dy;            

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
      Y - up image class 
    public static class ImageBitmap_YUP implements DataSource, Initializable {
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

            int len = vDataSources.length;
            DataSource dss[] = vDataSources;

            double value = 0.;

            for(int i = 0; i < len; i++){
                
                DataSource ds = dss[i];
                int res = ds.getDataValue(pnt, data);                

                if(res != RESULT_OK){
                    // outside of domain 
                    continue;
                }
                double v = data.v[0];
                if(v >= 1.){
                    data.v[0] = 1;
                    return RESULT_OK;                                        
                }

                value += v *(1 - value); //1-(1-value)*(1-v);
                
            }

            data.v[0] = value; 

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
            
            DataSource dss[] = vDataSources;
            int len = dss.length;

            double value = 1;

            for(int i = 0; i < len; i++){
                
                DataSource ds = dss[i];
                //int res = ds.getDataValue(pnt, workPnt);
                int res = ds.getDataValue(pnt, data);
                if(res != RESULT_OK){
                    data.v[0] = 0.;
                    return res;
                }

                double v = data.v[0];

                if(v <= 0.){
                    data.v[0] = 0;
                    return RESULT_OK;                    
                }

                value *= v;
            }

            data.v[0] = value; 
            return RESULT_OK;
        }        

    } // class Intersection


    /**
       subtracts (dataSource1 - dataSource2)       
       can be used for boolean difference 
     */
    public static class Subtraction implements DataSource, Initializable {

        DataSource dataSource1;
        DataSource dataSource2;
        
        public Subtraction(){     

        }

        public void setDataSources(DataSource ds1, DataSource ds2){

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
            
            double v1 = 0, v2 = 0;

            int res = dataSource1.getDataValue(pnt, data);
            if(res != RESULT_OK){
                data.v[0] = 0.0;
                return res;
            }

            v1 = data.v[0];

            if(v1 <= 0.){
                data.v[0] = 0.0;
                return RESULT_OK;
            }

            // we are here if v1 > 0
            
            res = dataSource2.getDataValue(pnt, data);

            if(res != RESULT_OK){
                data.v[0] = v1;
                return res;
            }

            v2 = data.v[0];            
            if(v2 >= 1.){
                data.v[0] = 0.;
                return RESULT_OK;
            }            
            data.v[0] = v1*(1-v2);

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
         * 
         * 
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
                data.v[0] = 1.;
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
        double innerRadius;
        double exteriorRadius;        
        double exteriorRadius2;
        
        public Ring(double innerRadius, double thickeness, double width){  

            width2 = width/2;
            this.innerRadius = innerRadius;
            this.exteriorRadius = innerRadius + thickeness;

            this.innerRadius2 = innerRadius*innerRadius;

            this.exteriorRadius2 = exteriorRadius*exteriorRadius;
            
            //exteriorRadius2 *= exteriorRadius2;
            
        }


        /**
         * calculates values of all data sources and return maximal value
         * can be used to make union of few shapes 
         */
        public int getDataValue(Vec pnt, Vec data) {

            double y = pnt.v[1];
            double vs = pnt.voxelSize;
            double w2 = width2 + vs;

            double yvalue = 1.;

            if(y < -w2 || y > w2){

                data.v[0] = 0;
                return RESULT_OK;

            } else if(y < (-width2 + vs)){
                // interpolate lower rim 
                
                yvalue = (y - (-width2 - vs))/(2*vs);

            } else if(y > (width2 - vs)){

                // interpolate upper rim 
                yvalue = ((width2 + vs)-y)/(2*vs);                

            } 
                        
            
            double x = pnt.v[0];
            double z = pnt.v[2];
            double r = Math.sqrt(x*x + z*z);

            double rvalue = 1;
            if(r < (innerRadius-vs) || r > (exteriorRadius+vs)){
                data.v[0] = 0;
                return RESULT_OK;                

            } else if(r < (innerRadius+vs)){
                // interpolate interior surface 
                rvalue = (r-(innerRadius-vs))/(2*vs);
                
            } else if(r > (exteriorRadius - vs)){

                rvalue = ((exteriorRadius + vs) - r)/(2*vs);
                // interpolate exterior surface 
                
            } 
            
            //data.v[0] = (rvalue < yvalue)? rvalue : yvalue;
            data.v[0] = rvalue * yvalue;
            
            return RESULT_OK;             
        }        

    } // class Ring


    // linear intepolation 
    // x < -1 return 1;
    // x >  1 returns 0    
    public static final double interpolate_linear(double x){

        return 0.5*(1 - x);

    }

    /**
       x < 0 return 0
       x > 1 return 1
       return x inside (0.,1.)
     */
    public static final double step(double x){
        if(x < 0.)    
            return 0.;
        else if( x > 1.)
            return 1.;
        else 
            return x;
    }

    /**
       return 1 inside of interval and 0 outside of intervale with linerar transitrion in the boundaries
     */
    public static final double intervalCap(double x, double xmin, double xmax, double vs){

        double vs2 = vs*2;
        double vxi = step((x-(xmin-vs))/(vs2));
        double vxa = step(((xmax+vs)-x)/(vs2));

        return vxi*vxa;

    }
    
    // linear intepolation 
    // x < -1 return 1;
    // x >  1 returns 0    
    // smoth cubic polynom between 
    public static final double interpolate_cubic(double x){

        return 0.25*x*(x*x - 3.) + 0.5;
        
    }
    
    public final static double getBox(double x, double y, double z, 
                               double xmin, double xmax, 
                               double ymin, double ymax, 
                               double zmin, double zmax, 
                               double vs){
        
        double vs2 = 2*vs; 
        double vxi = step((x-(xmin-vs))/(vs2));
        double vxa = step(((xmax+vs)-x)/(vs2));
        double vyi = step((y-(ymin-vs))/(vs2));
        double vya = step(((ymax+vs)-y)/(vs2));
        double vzi = step((z-(zmin-vs))/(vs2));
        double vza = step(((zmax+vs)-z)/(vs2));
            
        vxi *= vxa;
        vyi *= vya;
        vzi *= vza;
        return vxi*vyi*vzi;
    }
        
}

