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
import java.awt.image.DataBufferUShort;
//import java.awt.image.Raster;

import java.io.File;
import java.util.Vector;

import javax.imageio.ImageIO;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;
import abfab3d.util.ImageMipMapGray16;
import abfab3d.util.ImageGray16;

import abfab3d.util.ImageUtil;
import abfab3d.util.Output;

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
import static abfab3d.util.ImageMipMapGray16.getScaledDownData;
import static abfab3d.util.ImageMipMapGray16.getScaledDownDataBlack;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.ImageUtil.us2i;
import static abfab3d.grid.op.DataSources.step;
import static abfab3d.grid.op.DataSources.step01;
import static abfab3d.grid.op.DataSources.step10;
import static abfab3d.grid.op.DataSources.getBox;
import static abfab3d.grid.op.DataSources.intervalCap;

/**
   
   DataSourceImageBitmap
   
   @author Vladimir Bulatov
   
*/

/**
   
   makes embossed image from given file of given size 
   
*/
public class DataSourceImageBitmap implements DataSource, Initializable {

    double MM = 0.001; //
    
    public static final int IMAGE_TYPE_EMBOSSED = 0, IMAGE_TYPE_ENGRAVED = 1;
    public static final int IMAGE_PLACE_TOP = 0, IMAGE_PLACE_BOTTOM = 1, IMAGE_PLACE_BOTH = 2; 
    public static final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1, INTERPOLATION_MIPMAP = 2;
    static final String MEMORY_IMAGE = "[memory image]";

    static final double PIXEL_NORM = 1./255.;
    static final double SHORT_NORM = 1./0xFFFF;
    static double EPSILON = 1.e-3;
    static final double MAX_PIXELS_PER_VOXEL = 3.;
    
    protected double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.001, m_centerX=0, m_centerY=0, m_centerZ=0; 
    
    protected double m_baseThickness = 0.5; // relative thickness of solid base 
    protected String m_imagePath; 
    protected double m_baseThreshold = 0.01; 

    protected int m_imageType = IMAGE_TYPE_EMBOSSED;
    protected int m_imagePlace = IMAGE_PLACE_TOP; 
    
    
    protected int m_xTilesCount = 1; // number of image tiles in x-direction 
    protected int m_yTilesCount = 1; // number of image tiles in y-direction 
    protected boolean m_hasSmoothBoundaryX = false; // 
    protected boolean m_hasSmoothBoundaryY = false;
    
    private BufferedImage m_image;
    private int m_interpolationType = INTERPOLATION_LINEAR;//INTERPOLATION_BOX;
    // probe size in world units
    private double m_probeSize = 1.e-4; // 0.1mm
    // probe size in image pixels, initiliazed in init()
    private double m_probeSizeImage=0;         
    // scaling to convert probe size into pixels of image 
    private double m_probeScale; 
    // width of optional blur of the the image 
    private double m_blurWidth = 0.;
    private double m_voxelSize = 0.;

    private double xmin, xmax, ymin, ymax, zmin, zmax;    
    private double imageZmin;// location of lowest poiunt of thge image 
    private double baseBottom;
    private double imageZScale; // conversion form (0,1) to (0, imageZsize)
    
    private double xscale, yscale, zscale;
    private boolean useGrayscale = true;
    private int imageWidth, imageHeight, imageWidth1, imageHeight1;
    //private int imageData[]; 
    //private byte imageDataByte[]; 
    //private short imageDataShort[];
    private ImageGray16 imageData;

    
    //private ImageMipMap m_mipMap;
    
    private ImageMipMapGray16 m_mipMap;
    
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
    
    public DataSourceImageBitmap(){
    }
    
    public DataSourceImageBitmap(String imagePath, double sx, double sy, double sz){
        
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

    public void setBlurWidth(double blurWidth){
        
        m_blurWidth = blurWidth;
        
    }

    public void setBaseThreshold(double baseThreshold){
        
        m_baseThreshold = baseThreshold;
        
    }

    public void setVoxelSize(double vs){

        m_voxelSize = vs;

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
        
        imageThickness = (1. - m_baseThickness);
        
        if(m_imagePlace == IMAGE_PLACE_BOTH){           
            imageZmin = m_centerZ + (zmax - m_centerZ)*m_baseThickness;            
            baseBottom = 2*m_centerZ - imageZmin;
        } else {
            imageZmin = zmin + (zmax - zmin)*m_baseThickness;
            baseBottom = zmin;
        }

        imageZScale = (zmax - imageZmin);            

        //int imageData[] = null;
        
        long t0 = time();
        
        BufferedImage image = null;
        if(m_image != null){
            // image was supplied via memory 
            image = m_image;
            m_imagePath = MEMORY_IMAGE;
        } else if(m_imagePath == null){
            //imageDataByte = null; 
            
            return RESULT_OK;                
        } else {
            try {
                image = ImageIO.read(new File(m_imagePath));
                
            } catch(Exception e){
                
                printf("ERROR READING IMAGE: '%s' \n",m_imagePath);
                StackTraceElement[] st = Thread.currentThread().getStackTrace();
                int len = Math.min(10, st.length);
                for(int i = 1; i < len; i++) printf("\t\t %s\n",st[i]);                
                imageData = new ImageGray16(); // default black 1x1 image 
                //e.printStackTrace();
                return RESULT_ERROR;
            }
        }            
        
        printf("image %s [%d x %d ] reading done in %d ms\n", m_imagePath, image.getWidth(), image.getHeight(), (time() - t0));
        
        t0 = time();
        short imageDataShort[] = ImageUtil.getGray16Data(image);
        imageData = new ImageGray16(imageDataShort, image.getWidth(), image.getHeight());
        
        printf("image data size: done in %d ms\n", (time() - t0));
        
        if(!useGrayscale){
            imageData.makeBlackWhite(m_imageThreshold);
        }

        if(m_voxelSize >  0.0){ 
            // we have finite voxel size, try to scale the image down to reasonable size 
            double pixelSize = (m_sizeX/(imageData.getWidth()*m_xTilesCount));
            double pixelsPerVoxel = m_voxelSize / pixelSize;
            printf("pixelsPerVoxel: %f\n", pixelsPerVoxel);

            if(pixelsPerVoxel > MAX_PIXELS_PER_VOXEL){
                
                double newPixelSize = m_voxelSize / MAX_PIXELS_PER_VOXEL;
                int newWidth =  (int)Math.ceil((m_sizeX/m_xTilesCount)/newPixelSize);
                int newHeight = (imageData.getHeight() * newWidth) / imageData.getWidth();
                printf("resampling image[%d x %d] -> [%d x %d]\n", 
                       imageData.getWidth(), imageData.getHeight(), newWidth, newHeight);
                t0 = time();
                //short[] newData = getScaledDownData(imageDataShort, imageData.getWidth(), imageData.getHeight(), newWidth, newHeight);
                short[] newData = getScaledDownDataBlack(imageDataShort, imageData.getWidth(), imageData.getHeight(), newWidth, newHeight);
                
                printf("resampling image[%d x %d] -> [%d x %d]  done in %d ms\n", 
                       imageData.getWidth(), imageData.getHeight(), newWidth, newHeight, (time() - t0));
                imageData = new ImageGray16(newData, newWidth, newHeight);
            }
        }
        
        imageWidth = imageData.getWidth();
        imageHeight = imageData.getHeight();
        imageWidth1 = imageWidth-1;
        imageHeight1 = imageHeight-1;

        if(m_blurWidth >  0.0){ 
            
            double pixelSize = (m_sizeX/(imageWidth*m_xTilesCount));

            printf("pixelSize: %8.6f MM \n",pixelSize/MM);
            
            double blurSizePixels = m_blurWidth/pixelSize;

            printf("gaussian blur: %7.2f\n",blurSizePixels);
            t0 = time();

            imageData.gaussianBlur(blurSizePixels);
            
            printf("gaussian blur done: %d ms\n",time() - t0);


        }
        //char data[] = getBufferData(databuffer);
        
        if(m_interpolationType == INTERPOLATION_MIPMAP){
            
            t0 = time();
            m_mipMap = new ImageMipMapGray16(imageDataShort, imageWidth, imageHeight);
            // release out pointer 
            imageData = null;
            
            printf("mipmap ready in %d ms\n", (time() - t0));
            
        }else {

            m_mipMap = null;

        }

        
        /*
          
          t0 = time();
          imageWidth = image.getWidth();
          imageHeight = image.getHeight();
          printf("image: [%d x %d]\n", imageWidth, imageHeight);
          
          // convert probe size in meters into image units 
          m_probeScale = xscale*imageWidth;
          m_probeSizeImage = m_probeSize*m_probeScale;
          
          //DataBuffer dataBuffer = image.getRaster().getDataBuffer();          
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
        */
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
        
        if(x <= xmin - vs || x >= xmax + vs || 
           y <= ymin - vs || y >= ymax + vs || 
           z <= zmin - vs || z >= zmax + vs ){
            data.v[0] = 0.;
            return RESULT_OK;             
        }
                
        switch(m_imagePlace){            
            // do nothing 
        default:
        case IMAGE_PLACE_TOP:
            break;

        case IMAGE_PLACE_BOTTOM:
            // reflect z 
            z = 2.*m_centerZ - z;
            break;            
            
        case IMAGE_PLACE_BOTH:
            if(z <= m_centerZ)
                z = 2.*m_centerZ - z;
            break;
        }
        
        //  getBox(x,y,z, xmin, xmax, ymin, ymax, baseBottom, imageZmin, vs);
        double baseValue = intervalCap(z,baseBottom, imageZmin, vs);
        double finalValue = baseValue;

        double dd = vs;
        
        double imageX = (x-xmin)*xscale; // x and y are now in (0,1) 
        double imageY = 1. - (y-ymin)*yscale;

        if(m_xTilesCount > 1){
            imageX *= m_xTilesCount;
            imageX -= Math.floor(imageX);
        }
        if(m_yTilesCount > 1){
            imageY *= m_yTilesCount;
            imageY -= Math.floor(imageY);            
        }
        
        imageX *= imageWidth;
        imageY *= imageHeight;   
             
        // image x and imageY are in image units now 
        int ix = clamp((int)Math.floor(imageX), 0, imageWidth1);
        int iy = clamp((int)Math.floor(imageY), 0, imageHeight1);
        int ix1 = clamp(ix+1, 0, imageWidth1);
        int iy1 = clamp(iy+1, 0, imageHeight1);        
        double dx = imageX - ix;
        double dy = imageY - iy;
        double dx1 = 1. - dx;
        double dy1 = 1. - dy;
        double v00 = getImageHeight(ix, iy);
        double v10 = getImageHeight(ix1,iy);
        double v01 = getImageHeight(ix, iy1);
        double v11 = getImageHeight(ix1,iy1);

        //if(debugCount-- > 0) printf("xyz: (%7.5f, %7.5f,%7.5f) ixy[%4d, %4d ] -> v00:%18.15f\n", x,y,z, ix, iy, v00);

        double h0 = (dx1*(v00 * dy1 + v01 * dy) + dx * (v11 * dy + v10 * dy1)); 

        double imageValue = 0.; // distance to the image 

        if(!useGrayscale){

            // black and white image 
            // image is precalculated to return normalized value of distance
            double bottomStep = step01(z, imageZmin, vs);            
            double topStep = step10(z, zmax, vs);

            imageValue = 1;

            imageValue = Math.min(bottomStep,imageValue);

            imageValue = Math.min(topStep,imageValue);

            double sideStep = h0;

            imageValue = Math.min(imageValue, sideStep);
            
        } else {

            // using grayscale 

            if(h0 < m_baseThreshold){
                // TODO - better treatment of threshold 
                // transparent background 
                imageValue = 0.;

            } else {
                
                double z0 = imageZmin + imageZScale*h0;
                double bottomStep = step((z - (imageZmin - vs))/(2*vs));

                //hy = imageZmin + imageZScale*h0;
                
                //TODO - better calculation of normal in case of tiles
                double pixelSize = (m_sizeX/ (imageWidth * m_xTilesCount)); 
                double nx = -(v10 - v00)*imageZScale;
                double ny = -(v01 - v00)*imageZScale;
                double nz = pixelSize; 
                                
                double nn = Math.sqrt(nx*nx + ny*ny + nz*nz);
                
                // point on the surface p: (x,y,h0)
                // distance from point to surface  ((p-p0), n)                
                //double dist = ((z - h0)*vs)/nn;
                // signed distance to the plane via 3 points (v00, v10, v01)
                // outside distance is positive
                // inside distance is negative 
                double dist = ((z - z0)*pixelSize)/nn;
                
                if(dist <= -vs) 
                    imageValue = 1.;
                else if(dist >= vs)    
                    imageValue = 0.;
                else 
                    imageValue = (1. - (dist/vs))/2;

                if(bottomStep < imageValue) 
                    imageValue = bottomStep;                
            }  
        }
        
        
        //hfValue *= intervalCap(z, imageZmin, zmax, vs) * intervalCap(x, xmin, xmax, vs) * intervalCap(y, ymin, ymax, vs);
        // union of base and image layer 
        finalValue += imageValue;
        if(finalValue > 1) finalValue = 1;
        
        //  make c
        if(m_hasSmoothBoundaryX)
            finalValue = Math.min(finalValue, intervalCap(x, xmin, xmax, vs));
        if(m_hasSmoothBoundaryY)
            finalValue = Math.min(finalValue, intervalCap(y, ymin, ymax, vs));
        
        data.v[0] = finalValue;

        return RESULT_OK; 
        
    }
    
   
    final double getImageHeight(int ix, int iy){

        double v = 0.;

        try {
            v = imageData.getDataD(ix, iy);
        } catch(Exception e){
            e.printStackTrace(Output.out);
        }

        switch(m_imageType){
        case IMAGE_TYPE_EMBOSSED: 
            v = 1. - v;
            if( v < EPSILON)
                v = 0;
            break;

        default: 
        case IMAGE_TYPE_ENGRAVED:
            break;
        }

        return v;
        
    }
    
    double getHeightFieldValue(double x,double y, double probeSize){
        
        //if(debugCount-- > 0)
        //    printf("ImageBitmap.getHeightFieldValue(%10.5f, %10.5f, %10.5f)\n", x,y,probeSize);
        
        x = (x-xmin)*xscale; // x and y are now in (0,1) 
        y = 1. - (y-ymin)*yscale;

        if(m_xTilesCount > 1){
            x *= m_xTilesCount;
            x -= Math.floor(x);
        }
        if(m_yTilesCount > 1){
            y *= m_yTilesCount;
            y -= Math.floor(y);            
        }
        
        x *= imageWidth;
        y *= imageHeight;                

        probeSize *= xscale*imageWidth;

        double v = getPixelValue(x,y,probeSize); 

        v = imageZmin + imageZScale*v;
        
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
        
        double pixelValue = getPixelValue(imageX, imageY, 0.);
        /*
        if(debugCount-- > 0)
            printf("imageXY: [%7.2f, %7.2f] -> pixelValue: %8.5f\n", imageX, imageY, pixelValue);
        */
        double d = 0;
        
        if(useGrayscale){ 
            
            // smooth transition 
            d = z - pixelValue; 
            if( d < 0) // we are inside 
                data.v[0] = 1.;
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
       x is inside [0, imageWidth]
       y is inside [0, imageHeight]
    */
    double getPixelValue(double x, double y, double probeSize){
        
        double grayLevel;
        
        if(x < 0 || x >= imageWidth || y < 0 || y >= imageHeight){

            grayLevel = 1;

        } else {
            switch(m_interpolationType){
                
            case INTERPOLATION_MIPMAP:
                grayLevel = m_mipMap.getPixel(x, y, probeSize);
                break;
                
            default: 
                
            case INTERPOLATION_BOX:               
                grayLevel = getPixelBoxShort(x,y);
                break;
                
            case INTERPOLATION_LINEAR:
                
                grayLevel = getPixelLinearShort(x,y);
                
                break;
            }
        }

        //if(debugCount-- > 0)
        //    printf("(%10.7f, %10.7f) -> %10.7f\n", x, y, grayLevel);
        // pixel value for black is 0 for white is 255;
        // we may need to reverse it

        double pv = 0.;
        switch(m_imageType){
        default: 
        case IMAGE_TYPE_EMBOSSED: 
            pv = 1. - grayLevel;
            break;
        case IMAGE_TYPE_ENGRAVED:
            pv = grayLevel;
            break;
        }
        return pv;

    }
    
    // linear approximation
    final double getPixelLinearShort(double x, double y){

        // offset by half pixel 
        x -= 0.5;
        y -= 0.5;

        int x0 = (int)Math.floor(x);
        int y0 = (int)Math.floor(y);
        int x1 = x0+1, y1 = y0+1;
        
        double dx = x-x0;
        double dy = y-y0;
        double dx1= 1-dx, dy1=1-dy;
        
        x0 = clamp(x0, 0, imageWidth-1);
        x1 = clamp(x1, 0, imageWidth-1);
        y0 = clamp(y0, 0, imageHeight-1);
        y1 = clamp(y1, 0, imageHeight-1);

        int yoffset0 = y0 * imageWidth;
        int yoffset1 = y1 * imageWidth;

        double d00 = imageData.getDataD(x0, y0);
        double d10 = imageData.getDataD(x1, y0);
        double d01 = imageData.getDataD(x0, y1);
        double d11 = imageData.getDataD(x1, y1);
        return (dx1*(d00 * dy1 + d01 * dy) + dx * (d11 * dy + d10 * dy1));            
        
        /*
        int v00 = us2i(imageDataShort[yoffset0 + x0]);
        int v10 = us2i(imageDataShort[yoffset0 + x1]);
        int v01 = us2i(imageDataShort[yoffset1 + x0]);
        int v11 = us2i(imageDataShort[yoffset1 + x1]);
        
        return SHORT_NORM*(dx1*(v00 * dy1 + v01 * dy) + dx * (v11 * dy + v10 * dy1));            
        */
        //x = x - imageWidth * Math.floor(x/imageWidth);
        //y = y - imageHeight * Math.floor(y/imageHeight); 

    }
    
    double _getPixelLinear(double x, double y){
        
        
        x = x - imageWidth * Math.floor(x/imageWidth);
        y = y - imageHeight * Math.floor(y/imageHeight);            
        
        int ix = (int)x;
        int iy = (int)y;
        
        double dx = x-ix;
        double dy = y-iy;
        double dx1 = 1-dx;
        double dy1 = 1-dy;
        
        
        int i0 = ix  + iy * imageWidth;
        double v00 = 0., v10 = 0., v01 = 0., v11 = 0.;
        /*
        try {
            v00 = (0xFF) & ((int)imageDataByte[i0]);
            v10 = (0xFF) & ((int)imageDataByte[i0 + 1]);
            v01 = (0xFF) & ((int)imageDataByte[i0 + imageWidth]);
            v11 = (0xFF) & ((int)imageDataByte[i0 + 1 + imageWidth]);
        } catch (Exception e){}

        return SHORT_NORM*(dx1*(v00 * dy1 + v01 * dy) + dx * (v11 * dy + v10 * dy1));            
        */
        return 0.;
    }
    
    final double getPixelBoxShort(double x, double y){
        
        int ix = clamp((int)Math.floor(x), 0, imageWidth-1);
        int iy = clamp((int)Math.floor(y), 0, imageHeight-1);

        return imageData.getDataD(ix,iy);
        
        //return SHORT_NORM*us2i(imageDataShort[ix + imageWidth * iy]);
                
    }
    
    // BOX approximation with double colors 
    /*
    double getPixelBoxDouble(double x, double y){
        
        int ix = clamp((int)Math.floor(x), 0, imageWidth-1);
        int iy = clamp((int)Math.floor(y), 0, imageHeight-1);
        
        int rgb = (0xFF) & ((int)imageDataByte[ix + iy * imageWidth]);
        return rgb;
        //ImageUtil.getPremultColor(rgb, ci);
        //ImageUtil.combinePremultColors(m_backgroundColor, ci, cc, ci[ALPHA]);            
        //return (cc[RED] + cc[GREEN] + cc[BLUE])/3.;
        
    }
    */
    // BOX approximation with int colors 
    /*
    int getPixelBoxInt(double x, double y){
        
        int ix = (int)x;
        int iy = (int)y;
        
        //int ix = clamp((int)Math.floor(x), 0, imageWidth-1);
        //int iy = clamp((int)Math.floor(y), 0, imageHeight-1);
        //return imageData[ix + iy * imageWidth];
        
        int rgb = 0xFF & (int)imageDataByte[ix + iy * imageWidth];
        return ImageUtil.getCombinedGray(m_backgroundColorInt, rgb);
        
    }
    */
}  // class DataSourceImageBitmap

/*
  Y - up image class 
  public static class ImageBitmap_YUP implements DataSource, Initializable {
*/
