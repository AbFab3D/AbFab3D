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

import javax.vecmath.Vector3d;

import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.BooleanParameter;


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
 * Makes 3D relief image from supplied 2D image.
 * The shape fits into a box of the specified size. The image is placed parallel to the xy plane.
 * The bounding box is centered at origin.
 * <p/>
 * <embed src="doc-files/Image3D.svg" type="image/svg+xml"/>
 * <p>
 * The image may be placed in 3 different places: on top side, on bottom side and on both sides.
 * </p>
 * <p>
 * The style of image may be emboss or engrave.
 * </p>
 * <p/>
 * <embed src="doc-files/Image3D_options.svg" type="image/svg+xml"/>
 * <p>
 * The image can by tiled (repeated) in both x and y directions finite number of times 
 * 
 * </p>
 *
 * @author Vladimir Bulatov
 */
public class Image3D extends TransformableDataSource {

    final static boolean DEBUG = true;

    public static final int IMAGE_TYPE_EMBOSSED = 0, IMAGE_TYPE_ENGRAVED = 1;
    public static final int IMAGE_PLACE_TOP = 0, IMAGE_PLACE_BOTTOM = 1, IMAGE_PLACE_BOTH = 2;
    public static final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1, INTERPOLATION_MIPMAP = 2;
    static final String MEMORY_IMAGE = "[memory image]";

    // public params of the image 
    Vector3dParameter  mp_center = new Vector3dParameter("center","center of the image box",new Vector3d(0,0,0));
    Vector3dParameter  mp_size = new Vector3dParameter("size","size of the image box",new Vector3d(0.1,0.1,0.1));
    // rounding of the edges
    DoubleParameter  mp_rounding = new DoubleParameter("rounding","rounding of the box edges", 0.);
    IntParameter  mp_imagePlace = new IntParameter("imagePlace","placement of the image", 0, 0, 2);
    IntParameter  mp_tilesX = new IntParameter("tilesX","image tiles in x-direction", 1);
    IntParameter  mp_tilesY = new IntParameter("tilesY","image tiles in y-direction", 1);
    DoubleParameter  mp_baseThickness = new DoubleParameter("baseThickness","relative thickness of image base", 0.);
    BooleanParameter  mp_useGrayscale = new BooleanParameter("useGrayscale","Use grayscale for image rendering", true);
    DoubleParameter  mp_blurWidth = new DoubleParameter("blurWidth", "width of gaussian blur on the image", 0.);
    DoubleParameter  mp_baseThreshold = new DoubleParameter("baseThreshold", "threshold of the image", 0.01);
    DoubleParameter  mp_distanceFactor = new DoubleParameter("distanceFactor", "distance factor in the image plane", 0.1);

    Parameter m_aparam[] = new Parameter[]{
        mp_center,
        mp_size,
        mp_rounding,
        mp_tilesX,
        mp_tilesY,
        mp_baseThickness,
        mp_useGrayscale,
        mp_blurWidth,
        mp_baseThreshold,
        mp_imagePlace,
        mp_distanceFactor,
    };

    static final double PIXEL_NORM = 1. / 255.;
    static final double SHORT_NORM = 1. / 0xFFFF;
    static double EPSILON = 1.e-3;
    static final double MAX_PIXELS_PER_VOXEL = 3.;

    public static final double DEFAULT_VOXEL_SIZE = 0.1*MM;

    // size of the box 
    protected double m_sizeX = 0., m_sizeY = 0., m_sizeZ = 10*DEFAULT_VOXEL_SIZE;
    // location of the box
    protected double m_centerX = 0, m_centerY = 0, m_centerZ = 0;

    protected String m_imagePath;
    protected double m_baseThickness = 0.0; // relative thickness of solid base 
    protected double m_baseThreshold = 0.01; // threshold to make cut from base of the image 

    protected int m_imageType = IMAGE_TYPE_EMBOSSED;
    protected int m_imagePlace = IMAGE_PLACE_TOP;


    protected int m_xTilesCount = 1; // number of image tiles in x-direction 
    protected int m_yTilesCount = 1; // number of image tiles in y-direction 
    protected boolean m_hasSmoothBoundaryX = false;//
    protected boolean m_hasSmoothBoundaryY = false;//false;

    private BufferedImage m_image;
    private int m_interpolationType = INTERPOLATION_LINEAR;//INTERPOLATION_BOX;
    // width of optional blur of the the image 
    private double m_voxelSize = 0.;

    private double xmin, xmax, ymin, ymax, zmin, zmax;
    private double imageZmin;// location of lowest point of thge image 
    private double baseBottom;
    private double imageZScale; // conversion form (0,1) to (0, imageZsize)

    private double xscale, yscale, zscale;
    private boolean useGrayscale = true;
    private int imageWidth, imageHeight, imageWidth1, imageHeight1;
    private ImageGray16 imageData;

    private ImageMipMapGray16 m_mipMap;

    private double m_pixelWeightNonlinearity = 0.;
    // solid white color of background to be used for images with transparency
    private double m_backgroundColor[] = new double[]{255., 255., 255., 255.};
    private int m_backgroundColorInt = 0xFFFFFFFF;

    private double imageThickness;

    private double m_imageThreshold = 0.5; // this is for black and white case. below threshold we have solid voxel, above - empty voxel  

    // scratch vars
    //double[] ci = new double[4];
    //double[] cc = new double[4];
    double color[] = new double[4];

    /**
     * @noRefGuide
     */
    public Image3D() {
        initParams();
    }

    /**
     * Image3D with given image path and size
     *
     * @param imagePath path to the image file
     * @param sx width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz depth of the box.
     */
    public Image3D(String imagePath, double sx, double sy, double sz) {
        initParams();

        setImagePath(imagePath);
        setSize(sx, sy, sz);
    }

    /**
     * Image3D with given image path and size
     *
     * @param imagePath path to the image file
     * @param sx width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz depth of the box.
     * @param voxelSize size of voxel to be used for image voxelization
     */
    public Image3D(String imagePath, double sx, double sy, double sz, double voxelSize) {
        initParams();

        setImagePath(imagePath);
        setSize(sx, sy, sz);
        setVoxelSize(voxelSize);
    }

    /**
     * Image3D with given image path and size
     *
     * @param image image data
     * @param sx width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz depth of the box.
     * @param voxelSize size of voxel to be used for image voxelization
     */
    public Image3D(BufferedImage image, double sx, double sy, double sz, double voxelSize) {
        initParams();

        setImage(image);
        setSize(sx, sy, sz);
        setVoxelSize(voxelSize);
    }

    /**
     * Image3D with given image path and size
     *
     * @param imwrapper holder of BufferedImage 
     * @param sx width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz depth of the box. 
     * @param voxelSize size of voxel to be used for image voxelization 
     */
    public Image3D(ImageWrapper imwrapper, double sx, double sy, double sz, double voxelSize) {
        initParams();
        setImage(imwrapper.getImage());
        setSize(sx, sy, sz);
        setVoxelSize(voxelSize);
    }
    
    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
     * set size of imagebox 
     */
    public void setSize(double sx, double sy, double sz) {

        mp_size.setValue(new Vector3d(sx, sy, sz));

        m_sizeX = sx;
        m_sizeY = sy;
        m_sizeZ = sz;
    }

    /**
     * set center of imagebox 
     */
    public void setCenter(double cx, double cy, double cz) {
        mp_center.setValue(new Vector3d(cx, cy, cz));

        m_centerX = cx;
        m_centerY = cy;
        m_centerZ = cz;
    }

    /**
     * set image tiling 
     */
    public void setTiles(int tilesX, int tilesY) {

        mp_tilesX.setValue(new Integer(tilesX));
        mp_tilesY.setValue(new Integer(tilesY));
        m_xTilesCount = tilesX;
        m_yTilesCount = tilesY;
    }

    /**
     * Sets thickness of the solid base  relative to the bounding box thickness
     *
     * @param baseThickness thickenss of solid base relative to the thickness of the bounding box. Default value is 0.
     */
    public void setBaseThickness(double baseThickness) {
        mp_baseThickness.setValue(new Double(baseThickness));
    }

    /**
     * @noRefGuide
     */
    public void setBlurWidth(double blurWidth) {

        mp_blurWidth.setValue(new Double(blurWidth));

    }

    public void setRounding(double rounding) {
        mp_rounding.setValue(new Double(rounding));
    }

    /**
     * @noRefGuide
     */
    public void setBaseThreshold(double baseThreshold) {

        mp_baseThreshold.setValue(new Double(baseThreshold));

    }

    /**
     * @noRefGuide
     */
    public void setVoxelSize(double vs) {

        m_voxelSize = vs;

    }


    /**
     * @noRefGuide
     */
    public void setImagePath(String path) {

        m_imagePath = path;

    }

    /**
     * @noRefGuide
     */
    public void setImage(BufferedImage image) {

        m_image = image;

    }

    public void setImage(ImageWrapper wrapper) {
        m_image = wrapper.getImage();
    }

    /**
     * set options to image embossing type
     *
     * @param type Type ot the image. Possible values Image3D.IMAGE_TYPE_EMBOSSED (default value), Image3D.IMAGE_TYPE_ENGRAVED.
     *             
     */
    public void setImageType(int type) {

        m_imageType = type;

    }

    /**
     * set options to place the image
     *
     * @param place of the image.
     *              Possible values: Image3D.IMAGE_PLACE_TOP, Image3D.IMAGE_PLACE_BOTTOM, Image3D.IMAGE_PLACE_BOTH
     *              Default Image3D.IMAGE_PLACE_TOP
     */
    public void setImagePlace(int place) {

        mp_imagePlace.setValue(new Integer(place));

    }

    /**
     * set option to use grayscale
     *
     * @param value if true grayscale componenent of the will be used, if false iumage will be converted into black and white.
     *              the black and white option is useful if one want to have image shape with sharp vertical walls.
     */
    public void setUseGrayscale(boolean value) {
        
        useGrayscale = value;
        mp_useGrayscale.setValue(new Boolean(value));
    }

    /**
     * @noRefGuide
     */
    public void setInterpolationType(int type) {

        m_interpolationType = type;

    }

    /**
     * @noRefGuide
     */
    public void setSmoothBoundaries(boolean boundaryX, boolean boundaryY) {
        m_hasSmoothBoundaryX = boundaryX;
        m_hasSmoothBoundaryY = boundaryY;
    }

    /**
     * value = 0 - linear resampling for mipmap
     * value > 0 - black pixels are givewn heigher weight
     * value < 0 - white pixels are givewn heigher weight
     *
     * @noRefGuide
     */
    public void setPixelWeightNonlinearity(double value) {
        m_pixelWeightNonlinearity = value;
    }

    public int getBitmapWidth(){
        return imageData.getWidth();
    }

    public int getBitmapHeight(){
        return imageData.getHeight();
    }

    public void getBitmapData(byte data[]){
        getBitmapDataUByte(data);
    }

    public void getBitmapDataUByte(byte data[]){

        int nx = imageData.getWidth();
        int ny = imageData.getHeight();
        double base = m_baseThreshold;
        for(int y = 0;  y < ny; y++){
            for(int x = 0;  x < nx; x++){
                double d = getImageHeight(x,y);//imageData.getDataD(x, y);
                // normalization to byte 
                data[x + y * nx] = (byte)((int)(d * 0xFF) & 0xFF); 
            }
        }
    }
    // store bitmap data as 16 bit shorts 
    public void getBitmapDataUShort(byte data[]){

        int nx = imageData.getWidth();
        int ny = imageData.getHeight();
        int nx2 = nx*2;
        
        double base = m_baseThreshold;
        for(int y = 0;  y < ny; y++){
            for(int x = 0;  x < nx; x++){
                double d = getImageHeight(x,y);
                // normalization to byte 
                int id = ((int)(d * 0xFFFF)) & 0xFFFF;
                int ind = 2*x + y * nx2;
                data[ind] = (byte)(id & 0xFF); 
                data[ind + 1] = (byte)((id >> 8) & 0xFF);  
            }
        }
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();

        if(DEBUG)printf("%s.initilize()\n",this);

        m_baseThreshold = mp_baseThreshold.getValue();
        m_baseThickness = mp_baseThickness.getValue();
        m_imagePlace = mp_imagePlace.getValue();

        int res = prepareImage();

        if(res != RESULT_OK){ 
            // something wrong with the image 
            imageWidth = 2;
            imageHeight = 2; 
            throw new IllegalArgumentException("undefined image");
        }
 
        
        if(m_sizeX == 0.){ // width undefined - get it from image aspect ratio 
            if(m_sizeY == 0.){ 
                // both sizes are undefined - do something reasonable 
                m_sizeX = imageWidth*DEFAULT_VOXEL_SIZE;
                m_sizeY = imageHeight*DEFAULT_VOXEL_SIZE;
            } else {
                
                m_sizeX = (m_sizeY * imageWidth) / imageHeight;
            }            
        } else if(m_sizeY == 0.0){ // height is undefined - get it from the image aspect ratio 
            m_sizeY = (m_sizeX * imageHeight) / imageWidth;            
        }
        
        xmin = m_centerX - m_sizeX / 2.;
        xmax = m_centerX + m_sizeX / 2.;
        xscale = 1. / (xmax - xmin);


        ymin = m_centerY - m_sizeY / 2.;
        ymax = m_centerY + m_sizeY / 2.;
        yscale = 1. / (ymax - ymin);

        zmin = m_centerZ - m_sizeZ / 2.;
        zmax = m_centerZ + m_sizeZ / 2.;
        zscale = 1. / (zmax - zmin);

        imageThickness = (1. - m_baseThickness);

        if (m_imagePlace == IMAGE_PLACE_BOTH) {
            imageZmin = m_centerZ + (zmax - m_centerZ) * m_baseThickness;
            baseBottom = 2 * m_centerZ - imageZmin;
        } else {
            imageZmin = zmin + (zmax - zmin) * m_baseThickness;
            baseBottom = zmin;
        }

        imageZScale = (zmax - imageZmin);

        //int imageData[] = null;

        
        return RESULT_OK;
    }

    /**
     * @noRefGuide
     */
    private int prepareImage(){

        long t0 = time();

        BufferedImage image = null;
        if (m_image != null) {
            // image was supplied via memory 
            image = m_image;
            m_imagePath = MEMORY_IMAGE;

        } else if (m_imagePath == null) {
            //imageDataByte = null; 
            return RESULT_ERROR;
            
        } else {
            try {
                image = ImageIO.read(new File(m_imagePath));

            } catch (Exception e) {

                printf("ERROR READING IMAGE: '%s' msg: %s\n", m_imagePath,e.getMessage());
                StackTraceElement[] st = Thread.currentThread().getStackTrace();
                int len = Math.min(10, st.length);
                for (int i = 1; i < len; i++) printf("\t\t %s\n", st[i]);
                imageData = new ImageGray16(); // default black 1x1 image 
                //e.printStackTrace();
                return RESULT_ERROR;
            }
        }

        if(DEBUG)printf("image %s [%d x %d ] reading done in %d ms\n", m_imagePath, image.getWidth(), image.getHeight(), (time() - t0));

        t0 = time();
        short imageDataShort[] = ImageUtil.getGray16Data(image);
        imageData = new ImageGray16(imageDataShort, image.getWidth(), image.getHeight());

        printf("image data size: done in %d ms\n", (time() - t0));

        if (!useGrayscale) {
            imageData.makeBlackWhite(m_imageThreshold);
        }

        if (m_voxelSize > 0.0) {
            // we have finite voxel size, try to scale the image down to reasonable size 
            double pixelSize = (m_sizeX / (imageData.getWidth() * m_xTilesCount));
            double pixelsPerVoxel = m_voxelSize / pixelSize;
            printf("pixelsPerVoxel: %f\n", pixelsPerVoxel);

            if (pixelsPerVoxel > MAX_PIXELS_PER_VOXEL) {

                double newPixelSize = m_voxelSize / MAX_PIXELS_PER_VOXEL;
                int newWidth = (int) Math.ceil((m_sizeX / m_xTilesCount) / newPixelSize);
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
        imageWidth1 = imageWidth - 1;
        imageHeight1 = imageHeight - 1;
        
        double blurWidth = mp_blurWidth.getValue();

        if (blurWidth > 0.0) {

            double pixelSize = (m_sizeX / (imageWidth * m_xTilesCount));

            printf("pixelSize: %8.6f MM \n", pixelSize / MM);

            double blurSizePixels = blurWidth / pixelSize;

            printf("gaussian blur: %7.2f\n", blurSizePixels);
            t0 = time();

            imageData.gaussianBlur(blurSizePixels);

            printf("gaussian blur done: %d ms\n", time() - t0);


        }
        //char data[] = getBufferData(databuffer);

        if (m_interpolationType == INTERPOLATION_MIPMAP) {

            t0 = time();
            m_mipMap = new ImageMipMapGray16(imageDataShort, imageWidth, imageHeight);
            // release imageData pointer 
            imageData = null;

            printf("mipmap ready in %d ms\n", (time() - t0));

        } else {

            m_mipMap = null;

        }

        if(DEBUG)printf("imageData: %s\n", imageData);

        return RESULT_OK;

    }

    /**
     * returns 1 if pnt is inside of image
     * returns 0 otherwise
     *
     * @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

        double vs = pnt.getScaledVoxelSize();
        if (vs == 0.)
            getDataValueZeroVoxel(pnt, data);
        else
            getDataValueFiniteVoxel(pnt, data, vs);

        super.getMaterialDataValue(pnt, data);        
        return RESULT_OK;        
        
    }


    /**
     * calculation for finite voxel size
     *
     * @noRefGuide
     */
    public int getDataValueFiniteVoxel(Vec pnt, Vec data, double vs) {

        double
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

        //double vs = pnt.getScaledVoxelSize();

        if (x <= xmin - vs || x >= xmax + vs ||
                y <= ymin - vs || y >= ymax + vs ||
                z <= zmin - vs || z >= zmax + vs) {
            data.v[0] = 0.;
            return RESULT_OK;
        }

        switch (m_imagePlace) {
            // do nothing 
            default:
            case IMAGE_PLACE_TOP:
                break;

            case IMAGE_PLACE_BOTTOM:
                // reflect z
                z = 2. * m_centerZ - z;
                break;

            case IMAGE_PLACE_BOTH:
                if (z <= m_centerZ)
                    z = 2. * m_centerZ - z;
                break;
        }

        //  getBox(x,y,z, xmin, xmax, ymin, ymax, baseBottom, imageZmin, vs);
        double baseValue = intervalCap(z, baseBottom, imageZmin, vs);
        double finalValue = baseValue;

        double dd = vs;

        double imageX = (x - xmin) * xscale; // x and y are now in (0,1)
        double imageY = 1. - (y - ymin) * yscale;

        if (m_xTilesCount > 1) {
            imageX *= m_xTilesCount;
            imageX -= Math.floor(imageX);
        }
        if (m_yTilesCount > 1) {
            imageY *= m_yTilesCount;
            imageY -= Math.floor(imageY);
        }

        imageX *= imageWidth;
        imageY *= imageHeight;

        // image x and imageY are in image units now 
        int ix = clamp((int) Math.floor(imageX), 0, imageWidth1);
        int iy = clamp((int) Math.floor(imageY), 0, imageHeight1);
        int ix1 = clamp(ix + 1, 0, imageWidth1);
        int iy1 = clamp(iy + 1, 0, imageHeight1);
        double dx = imageX - ix;
        double dy = imageY - iy;
        double dx1 = 1. - dx;
        double dy1 = 1. - dy;
        double v00 = getImageHeight(ix, iy);
        double v10 = getImageHeight(ix1, iy);
        double v01 = getImageHeight(ix, iy1);
        double v11 = getImageHeight(ix1, iy1);

        //if(debugCount-- > 0) printf("xyz: (%7.5f, %7.5f,%7.5f) ixy[%4d, %4d ] -> v00:%18.15f\n", x,y,z, ix, iy, v00);

        double h0 = (dx1 * (v00 * dy1 + v01 * dy) + dx * (v11 * dy + v10 * dy1));

        double imageValue = 0.; // distance to the image 

        if (!useGrayscale) {

            // black and white image 
            // image is precalculated to return normalized value of distance
            double bottomStep = step01(z, imageZmin, vs);
            double topStep = step10(z, zmax, vs);

            imageValue = 1;

            imageValue = Math.min(bottomStep, imageValue);

            imageValue = Math.min(topStep, imageValue);

            double sideStep = h0;

            imageValue = Math.min(imageValue, sideStep);

        } else {

            // using grayscale 

            if (h0 < m_baseThreshold) {
                // TODO - better treatment of threshold 
                // transparent background 
                imageValue = 0.;

            } else {

                double z0 = imageZmin + imageZScale * h0;
                double bottomStep = step((z - (imageZmin - vs)) / (2 * vs));

                //hy = imageZmin + imageZScale*h0;

                //TODO - better calculation of normal in case of tiles
                double pixelSize = (m_sizeX / (imageWidth * m_xTilesCount));
                double nx = -(v10 - v00) * imageZScale;
                double ny = -(v01 - v00) * imageZScale;
                double nz = pixelSize;

                double nn = Math.sqrt(nx * nx + ny * ny + nz * nz);

                // point on the surface p: (x,y,h0)
                // distance from point to surface  ((p-p0), n)                
                //double dist = ((z - h0)*vs)/nn;
                // signed distance to the plane via 3 points (v00, v10, v01)
                // outside distance is positive
                // inside distance is negative 
                double dist = ((z - z0) * pixelSize) / nn;

                if (dist <= -vs)
                    imageValue = 1.;
                else if (dist >= vs)
                    imageValue = 0.;
                else
                    imageValue = (1. - (dist / vs)) / 2;

                if (bottomStep < imageValue)
                    imageValue = bottomStep;
            }
        }


        //hfValue *= intervalCap(z, imageZmin, zmax, vs) * intervalCap(x, xmin, xmax, vs) * intervalCap(y, ymin, ymax, vs);
        // union of base and image layer 
        finalValue += imageValue;
        if (finalValue > 1) finalValue = 1;

        //  make c
        if (m_hasSmoothBoundaryX)
            finalValue = Math.min(finalValue, intervalCap(x, xmin, xmax, vs));
        if (m_hasSmoothBoundaryY)
            finalValue = Math.min(finalValue, intervalCap(y, ymin, ymax, vs));

        data.v[0] = finalValue;

        return RESULT_OK;

    }


    final double getImageHeight(int ix, int iy) {

        double v = 0.;

        try {
            v = imageData.getDataD(ix, iy);
        } catch (Exception e) {
            e.printStackTrace(Output.out);
        }

        switch (m_imageType) {
            case IMAGE_TYPE_EMBOSSED:
                v = 1. - v;
                if (v < EPSILON)
                    v = 0;
                break;

            default:
            case IMAGE_TYPE_ENGRAVED:
                break;
        }

        return v;

    }

    /**
     * @noRefGuide
     */
    private double getHeightFieldValue(double x, double y, double probeSize) {

        x = (x - xmin) * xscale; // x and y are now in (0,1)
        y = 1. - (y - ymin) * yscale;

        if (m_xTilesCount > 1) {
            x *= m_xTilesCount;
            x -= Math.floor(x);
        }
        if (m_yTilesCount > 1) {
            y *= m_yTilesCount;
            y -= Math.floor(y);
        }

        x *= imageWidth;
        y *= imageHeight;

        probeSize *= xscale * imageWidth;

        double v = getPixelValue(x, y, probeSize);

        v = imageZmin + imageZScale * v;

        return v;

    }

    /**
     * calculation for zero voxel size
     *
     * @noRefGuide
     */
    protected int getDataValueZeroVoxel(Vec pnt, Vec data) {

        double
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

        x = (x - xmin) * xscale;
        y = (y - ymin) * yscale;
        z = (z - zmin) * zscale;

        if (x < 0. || x > 1. ||
                y < 0. || y > 1. ||
                z < 0. || z > 1.) {
            data.v[0] = 0;
            return RESULT_OK;
        }
        // z is in range [0, 1]
        switch (m_imagePlace) {
            default:
            case IMAGE_PLACE_TOP:
                z = (z - m_baseThickness) / imageThickness;
                break;

            case IMAGE_PLACE_BOTTOM:
                z = ((1 - z) - m_baseThickness) / imageThickness;
                break;

            case IMAGE_PLACE_BOTH:
                //scale and make symmetrical
                z = (2 * z - 1);
                if (z < 0.) z = -z;
                z = (z - m_baseThickness) / imageThickness;
                break;
        }

        if (z < 0.0) {
            data.v[0] = 1;
            return RESULT_OK;
        }

        if (m_xTilesCount > 1) {
            x *= m_xTilesCount;
            x -= Math.floor(x);
        }
        if (m_yTilesCount > 1) {
            y *= m_yTilesCount;
            y -= Math.floor(y);
        }

        double imageX = imageWidth * x;
        double imageY = imageHeight * (1. - y);// reverse Y-direction

        double pixelValue = getPixelValue(imageX, imageY, 0.);
        /*
        if(debugCount-- > 0)
            printf("imageXY: [%7.2f, %7.2f] -> pixelValue: %8.5f\n", imageX, imageY, pixelValue);
        */
        double d = 0;

        if (useGrayscale) {

            // smooth transition 
            d = z - pixelValue;
            if (d < 0) // we are inside
                data.v[0] = 1.;
            else   // we are outside 
                data.v[0] = 0;

        } else {

            // sharp transition
            d = pixelValue;
            if (d > m_imageThreshold)
                data.v[0] = 1;
            else
                data.v[0] = 0;
        }

        return RESULT_OK;
    }


    /**
     * returns value of pixel at given x,y location. value normalized to (0,1)
     * x is inside [0, imageWidth]
     * y is inside [0, imageHeight]
     *
     * @noRefGuide
     */
    double getPixelValue(double x, double y, double probeSize) {

        double grayLevel;

        if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight) {

            grayLevel = 1;

        } else {
            switch (m_interpolationType) {

                case INTERPOLATION_MIPMAP:
                    grayLevel = m_mipMap.getPixel(x, y, probeSize);
                    break;

                default:

                case INTERPOLATION_BOX:
                    grayLevel = getPixelBoxShort(x, y);
                    break;

                case INTERPOLATION_LINEAR:

                    grayLevel = getPixelLinearShort(x, y);

                    break;
            }
        }

        //if(debugCount-- > 0)
        //    printf("(%10.7f, %10.7f) -> %10.7f\n", x, y, grayLevel);
        // pixel value for black is 0 for white is 255;
        // we may need to reverse it

        double pv = 0.;
        switch (m_imageType) {
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
    final double getPixelLinearShort(double x, double y) {

        // offset by half pixel 
        x -= 0.5;
        y -= 0.5;

        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int x1 = x0 + 1, y1 = y0 + 1;

        double dx = x - x0;
        double dy = y - y0;
        double dx1 = 1 - dx, dy1 = 1 - dy;

        x0 = clamp(x0, 0, imageWidth - 1);
        x1 = clamp(x1, 0, imageWidth - 1);
        y0 = clamp(y0, 0, imageHeight - 1);
        y1 = clamp(y1, 0, imageHeight - 1);

        int yoffset0 = y0 * imageWidth;
        int yoffset1 = y1 * imageWidth;

        double d00 = imageData.getDataD(x0, y0);
        double d10 = imageData.getDataD(x1, y0);
        double d01 = imageData.getDataD(x0, y1);
        double d11 = imageData.getDataD(x1, y1);
        return (dx1 * (d00 * dy1 + d01 * dy) + dx * (d11 * dy + d10 * dy1));
        
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

    double _getPixelLinear(double x, double y) {


        x = x - imageWidth * Math.floor(x / imageWidth);
        y = y - imageHeight * Math.floor(y / imageHeight);

        int ix = (int) x;
        int iy = (int) y;

        double dx = x - ix;
        double dy = y - iy;
        double dx1 = 1 - dx;
        double dy1 = 1 - dy;


        int i0 = ix + iy * imageWidth;
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

    final double getPixelBoxShort(double x, double y) {

        int ix = clamp((int) Math.floor(x), 0, imageWidth - 1);
        int iy = clamp((int) Math.floor(y), 0, imageHeight - 1);

        return imageData.getDataD(ix, iy);

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
