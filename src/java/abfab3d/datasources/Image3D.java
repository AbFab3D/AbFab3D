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


import abfab3d.core.Output;
import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;
import abfab3d.core.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.core.GridDataChannel;
import abfab3d.grid.op.DistanceTransform2DOp;

import abfab3d.param.ObjectParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.LongParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.Parameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.ParamCache;
import abfab3d.param.SourceWrapper;


import abfab3d.util.ImageMipMapGray16;
import abfab3d.util.ImageGray16;
import abfab3d.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.vecmath.Vector3d;


import static abfab3d.util.ImageMipMapGray16.getScaledDownDataBlack;
import static abfab3d.core.MathUtil.intervalCap;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.fmod;
import static abfab3d.core.MathUtil.step01;
import static abfab3d.core.MathUtil.step10;
import static abfab3d.core.MathUtil.step;
import static abfab3d.core.MathUtil.lerp2;
import static abfab3d.core.MathUtil.blendMax;
import static abfab3d.core.MathUtil.blendMin;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;


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
 * The style of image may be embossed or engraved.
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

    final static boolean DEBUG = false;
    final static boolean DEBUG_VIZ = false;


    public static final int IMAGE_TYPE_EMBOSSED = 0, IMAGE_TYPE_ENGRAVED = 1;
    public static final int IMAGE_PLACE_TOP = 0, IMAGE_PLACE_BOTTOM = 1, IMAGE_PLACE_BOTH = 2;
    public static final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1, INTERPOLATION_MIPMAP = 2;
    static final double MIN_GRADIENT = 0.05;
    static final String MEMORY_IMAGE = "[memory image]";
    
    //SNodeParameter mp_source = new SNodeParameter("source","Image source", null);

    // public params of the image 
    ObjectParameter mp_image = new ObjectParameter("image","Image source", null); // obsolete 
    ObjectParameter mp_imageSource = new ObjectParameter("imageSource","Image source", null);
    Vector3dParameter  mp_center = new Vector3dParameter("center","center of the image box",new Vector3d(0,0,0));
    Vector3dParameter  mp_size = new Vector3dParameter("size","size of the image box",new Vector3d(0.1,0.1,0.1));
    // rounding of the edges
    DoubleParameter  mp_rounding = new DoubleParameter("rounding","rounding of the box edges", 0.);
    IntParameter  mp_imagePlace = new IntParameter("imagePlace","placement of the image", 0, 0, IMAGE_PLACE_BOTH);
    IntParameter  mp_tilesX = new IntParameter("tilesX","image tiles in x-direction", 1);
    IntParameter  mp_tilesY = new IntParameter("tilesY","image tiles in y-direction", 1);
    DoubleParameter  mp_baseThickness = new DoubleParameter("baseThickness","relative thickness of image base", 0.);
    BooleanParameter  mp_useGrayscale = new BooleanParameter("useGrayscale","Use grayscale for image rendering", true);
    DoubleParameter  mp_blurWidth = new DoubleParameter("blurWidth", "width of gaussian blur on the image", 0.);
    DoubleParameter  mp_voxelSize = new DoubleParameter("voxelSize", "size of voxel to use for image voxelization", 0.);
    DoubleParameter  mp_baseThreshold = new DoubleParameter("baseThreshold", "threshold of the image", 0.01);
    LongParameter  mp_imageFileTimeStamp = new LongParameter("imageTimeStamp", 0);
    DoubleParameter  mp_distanceFactor = new DoubleParameter("distanceFactor", "distance factor in the image plane", 0.1);

    Parameter m_aparam[] = new Parameter[]{
        mp_imageSource, 
        mp_image,
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
        mp_voxelSize,
        mp_distanceFactor
    };

    // Params which require changes in the underlying image 
    Parameter m_imageParams[] = new Parameter[] {
        mp_image, 
        mp_imageFileTimeStamp,
        mp_size, 
        mp_tilesX, 
        mp_tilesY, 
        mp_blurWidth, 
        mp_useGrayscale
    };
    
    public static final double DEFAULT_PIXEL_SIZE = 0.1*MM;

    //static double EPSILON = 1.e-3;
    static final double MAX_PIXELS_PER_VOXEL = 3.;

    public static final double DEFAULT_VOXEL_SIZE = 0.1*MM;

    // size of the box 
    protected double m_sizeX = 0., m_sizeY = 0., m_sizeZ = 0.;
    protected double m_halfSizeX,  m_halfSizeY,  m_halfSizeZ;
    // location of the box
    protected double m_centerX = 0, m_centerY = 0, m_centerZ = 0;

    protected double m_baseRelThickness = 0.0; // relative thickness of solid base
    protected double m_baseThickness = 0.0; // thickness of solid base in physical units
    protected double m_baseRelThreshold = 0.01; // threshold to make cut from base of the image (in relative units)    
    protected double m_baseHalfSizeZ;  // 
    protected double m_baseCenterZ = 0; // 
    protected double m_rounding = 0.0; // 
    protected double m_slopeZcoeff; // factor in gradient to compensate for Z stretch in case of IMAGE_PLACE_BOTH
    protected double m_gradXfactor; // coeff for gradient X calculation
    protected double m_gradYfactor; // coeff for gradient Y calculation

    protected int m_dataChannelIndex = 0; // data channel index to use 

    protected int m_imageType = IMAGE_TYPE_EMBOSSED;
    protected int m_imagePlace = IMAGE_PLACE_TOP;

    protected int m_tilesX = 1; // number of image tiles in x-direction 
    protected int m_tilesY = 1; // number of image tiles in y-direction 

    private int m_interpolationType = INTERPOLATION_LINEAR;//INTERPOLATION_BOX;
    // width of optional blur of the the image     
    // bounds of box 
    protected double m_xmin, m_xmax, m_ymin, m_ymax, m_zmin, m_zmax;

    protected boolean m_hasBase = false;

    // conversion from physical units into image units
    private double m_xfactor, m_yfactor;

    private boolean m_useGrayscale = true;
    private int m_imageSizeX, m_imageSizeY, m_imageSizeX1, m_imageSizeY1;
    
    // the image data is stored in the Grid2D 
    protected Grid2D m_dataGrid = null; 
    // converted to get physical value from grid attribute 
    protected GridDataChannel m_dataChannel;

    // image is stored in mipmap
    private ImageMipMapGray16 m_mipMap;

    private double m_pixelWeightNonlinearity = 0.;
    // solid white color of background to be used for images with transparency
    private double m_backgroundColor[] = new double[]{255., 255., 255., 255.};
    private int m_backgroundColorInt = 0xFFFFFFFF;

    // minimal value of distance 
    private double m_minDistance;

    private double m_maxDistance;

    private double m_imageThickness; // thickness of image layer in physical units

    private double m_imageThreshold = 0.5; // this is for black and white case. below threshold we have solid voxel, above - empty voxel  
    // maximal distance to calculate distance transform 
    // it mostly affect precision of stored distrance, because distance is stored in 16 bits of short
    private double m_maxOutDistancePixels = 100;
    private double m_maxInDistancePixels = 100;

    private static Grid2D m_emptyGrid = new Grid2DShort(1,1,DEFAULT_PIXEL_SIZE);

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
        //mp_imageSource.setValue(new ImageLoader(imagePath));
        if (!new File(imagePath).exists()) {
            throw new IllegalArgumentException("Image does not exist.  image: " + imagePath);
        }
        setImage(imagePath);
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

        if (!new File(imagePath).exists()) {
            throw new IllegalArgumentException("Image does not exist.  image: " + imagePath);
        }
        setImage(imagePath);
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
     */
    public Image3D(BufferedImage image, double sx, double sy, double sz) {
        initParams();

        setImage(image);
        setSize(sx, sy, sz);
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
     * Image3D with given text
     *
     * @param text text data
     * @param sx width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz depth of the box.
     */
    public Image3D(Text2D text, double sx, double sy, double sz) {
        initParams();

        setImage(text.getImage());
        setSize(sx, sy, sz);
    }

    /**
     * Image3D with given text
     *
     * @param text text data
     * @param sx width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz depth of the box.
     * @param voxelSize size of voxel to be used for image voxelization
     */
    public Image3D(Text2D text, double sx, double sy, double sz, double voxelSize) {
        initParams();

        setImage(text.getImage());
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
     */
    public Image3D(ImageWrapper imwrapper, double sx, double sy, double sz) {
        initParams();
        setImage(imwrapper);
        setSize(sx, sy, sz);
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
        setImage(imwrapper);
        setSize(sx, sy, sz);
        setVoxelSize(voxelSize);
    }

    /**
     * Image3D with given image path and size
     *
     * @param grid grid representation of image
     * @param sx width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz depth of the box.
     */
    public Image3D(Grid2D grid, double sx, double sy, double sz) {
        initParams();
        setImage(grid);
        setSize(sx, sy, sz);
    }

    /**
     * Image3D with given image path and size
     *
     * @param grid grid representation of image
     * @param sx width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz depth of the box.
     * @param voxelSize size of voxel to be used for image voxelization
     */
    public Image3D(Grid2D grid, double sx, double sy, double sz, double voxelSize) {
        initParams();
        setImage(grid);
        setSize(sx, sy, sz);
        setVoxelSize(voxelSize);
    }

    /**
     * @noRefGuide
     */
    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
     * @noRefGuide
     * @deprecated Remove next release
     */
    public void setDistanceFactor(double value) {
        mp_distanceFactor.setValue(new Double(value));
    }

    /**
     * Set size of the image box
     * @param sx The x dimension in meters
     * @param sy The y dimension in meters
     * @param sz The z dimension in meters
     */
    public void setSize(double sx, double sy, double sz) {
        mp_size.setValue(new Vector3d(sx, sy, sz));
    }

    /**
     * Set size of the image box
     * @param size The size in meters
     */
    public void setSize(Vector3d size) {
        mp_size.setValue(size);
    }

    /**
     * Get the size of the image box
     */
    public Vector3d getSize() {
        return mp_size.getValue();
    }

    /**
     * Set center of the image box
     * @param cx The x location in meters
     * @param cy The y location in meters
     * @param cz The z location in meters
     */
    public void setCenter(double cx, double cy, double cz) {
        mp_center.setValue(new Vector3d(cx, cy, cz));

    }

    /**
     * Set center of the image box
     * @param val The center in meters
     */
    public void setCenter(Vector3d val) {
        mp_center.setValue(val);

    }

    /**
     * Get center of the image box
     * @return
     */
    public Vector3d getCenter() {
        return mp_center.getValue();
    }

    /**
     * Set image tiling
     * @param tilesX the number of X tiles
     * @param tilesY the number of Y tiles
     */
    public void setTiles(int tilesX, int tilesY) {

        mp_tilesX.setValue(new Integer(tilesX));
        mp_tilesY.setValue(new Integer(tilesY));
    }

    /**
     * Set image tilingX
     * @param val The value
     */
    public void setTilesX(int val) {
        mp_tilesX.setValue(new Integer(val));
    }

    /**
     * Get image tilingX
     */
    public int getTilesX() {
        return mp_tilesX.getValue();
    }

    /**
     * Set image tilingY
     * @param val The value
     */
    public void setTilesY(int val) {
        mp_tilesY.setValue(new Integer(val));
    }

    /**
     * Get image tilingY
     */
    public int getTilesY() {
        return mp_tilesY.getValue();
    }

    /**
     * Sets thickness of the solid base  relative to the bounding box thickness
     *
     * @param baseThickness thickness of solid base relative to the thickness of the bounding box. Default value is 0.
     */
    public void setBaseThickness(double baseThickness) {
        mp_baseThickness.setValue(new Double(baseThickness));
    }

    /**
     * Set the blurring width to apply to the image
     * @param blurWidth The width in meters.  Default is 0.
     */
    public void setBlurWidth(double blurWidth) {
        mp_blurWidth.setValue(new Double(blurWidth));

    }

    /**
     * Get the blurring width to apply to the image
     */
    public double getBlurWidth() {
        return mp_blurWidth.getValue();
    }

    /**
     * Set the rounding applied to the image.
     * @param rounding The rounding in meters
     */
    public void setRounding(double rounding) {
        mp_rounding.setValue(new Double(rounding));
    }

    /**
     * Set the threshold for determining if a pixel creates geometry.  Threshold is a 0-1 double based on the
     * incoming image intensity.  Default is 0.01 which means anything not exactly black.
     */
    public void setBaseThreshold(double baseThreshold) {
        mp_baseThreshold.setValue(new Double(baseThreshold));
    }

    /**
     * Set the voxel size to use for the image
     *
     * @param vs The voxel size in meters
     */
    public void setVoxelSize(double vs) {
        mp_voxelSize.setValue(vs);
    }

    public void setImage(Object val) {
        
        mp_image.setValue(val);
        if(val instanceof String){
            mp_imageFileTimeStamp.setValue(new File((String)val).lastModified());
        } else {
            mp_imageFileTimeStamp.setValue(0);
        }

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
        
        mp_useGrayscale.setValue(new Boolean(value));
    }

    /**
     * @noRefGuide
     */
    public void setInterpolationType(int type) {

        m_interpolationType = type;

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

    /**
       returns physical data value which corresponds to the given attribute value 
     */
    public double getPhysicalValue(long attribute){
        return m_dataChannel.getValue(attribute);
    }

    /**
     * @noRefGuide
     */
    public int getBitmapWidth(){
        return m_dataGrid.getWidth();
    }

    /**
     * @noRefGuide
     */
    public int getBitmapHeight(){
        return m_dataGrid.getHeight();
    }

    /**
     * @noRefGuide
     */
    public void getBitmapData(byte data[]){
        getBitmapDataUByte(data);
    }

    /**
     * @noRefGuide
     */
    public void getBitmapDataUByte(byte data[]){

        int nx = m_dataGrid.getWidth();
        int ny = m_dataGrid.getHeight();
        double base = m_baseRelThreshold;
        for(int y = 0;  y < ny; y++){
            for(int x = 0;  x < nx; x++){
                double d = getImageValue(x, y);
                // normalization to byte 
                data[x + y * nx] = (byte)((int)(d * 0xFF) & 0xFF); 
            }
        }
    }

    // store bitmap data as 16 bit shorts 
    /**
     * @noRefGuide
     */
    public void getBitmapDataUShort(byte data[]){

        int nx = m_dataGrid.getWidth();
        int ny = m_dataGrid.getHeight();
        int nx2 = nx*2;
        
        for(int y = 0;  y < ny; y++){
            for(int x = 0;  x < nx; x++){

                long id = m_dataGrid.getAttribute(x,y); 
                
                //double d = getImageValue(x,y);
                // normalization to byte 
                //int id = ((int)(d * 0xFFFF)) & 0xFFFF;
                int ind = 2*x + y * nx2;
                data[ind] = (byte)(id & 0xFF); 
                data[ind + 1] = (byte)((id >> 8) & 0xFF);  
            }
        }
    }

    /**
     * Get a label for the OpenCL buffer, account for all params which change the buffer value
     * @return
     * @noRefGuide
     */
    public String getBufferLabel() {
        return BaseParameterizable.getParamString(getClass().getSimpleName(), m_imageParams);
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();
        
        if(DEBUG)printf("%s.initialize()\n",this);
        
        Vector3d c = mp_center.getValue(); 
        m_centerX = c.x;
        m_centerY = c.y;
        m_centerZ = c.z;

        Vector3d s = mp_size.getValue(); 
        m_sizeX = s.x;
        m_sizeY = s.y;
        m_sizeZ = s.z;

        m_halfSizeX = m_sizeX/2;
        m_halfSizeY = m_sizeY/2;
        m_halfSizeZ = m_sizeZ/2;

        m_baseRelThreshold = mp_baseThreshold.getValue();
        m_baseRelThickness = mp_baseThickness.getValue();        
        m_rounding = mp_rounding.getValue();
       
        m_imagePlace = mp_imagePlace.getValue();
        m_useGrayscale = mp_useGrayscale.getValue();

        m_tilesX = mp_tilesX.getValue();
        m_tilesY = mp_tilesY.getValue();

        m_xmin = m_centerX - m_halfSizeX;
        m_xmax = m_centerX + m_halfSizeX;
        m_ymin = m_centerY - m_halfSizeY;
        m_ymax = m_centerY + m_halfSizeY;
        m_zmin = m_centerZ - m_halfSizeZ;
        m_zmax = m_centerZ + m_halfSizeZ;

        m_imageThickness = (1. - m_baseRelThickness)*m_sizeZ;
        m_baseThickness = m_baseRelThickness*m_sizeZ;
        m_hasBase = (m_baseRelThickness > 0.);
        m_baseHalfSizeZ = m_baseThickness/2;

        switch(m_imagePlace){
        case IMAGE_PLACE_BOTH:            
            m_baseCenterZ = m_centerZ;
            m_slopeZcoeff = 4;
            break;
        case IMAGE_PLACE_TOP:            
            m_baseCenterZ = m_zmin + m_baseHalfSizeZ;
            m_slopeZcoeff= 1;
            break;
        case IMAGE_PLACE_BOTTOM:            
            m_baseCenterZ = m_zmax - m_baseHalfSizeZ;
            m_slopeZcoeff = 1;
            break;
        }

        String vhash = BaseParameterizable.getParamString(getClass().getSimpleName(), m_imageParams);

        Object co = ParamCache.getInstance().get(vhash);
        if (co == null) {
            int res = prepareImage();
            if(res != ResultCodes.RESULT_OK){
                m_dataGrid = m_emptyGrid;
                m_dataChannel = m_dataGrid.getDataDesc().getChannel(0);
                // something wrong with the image
                throw new IllegalArgumentException("undefined image");
            }
            ParamCache.getInstance().put(vhash, m_dataGrid);

        } else {
            m_dataGrid = (Grid2D) co;
            m_dataChannel = m_dataGrid.getDataDesc().getDefaultChannel();

            m_imageSizeX = m_dataGrid.getWidth();
            m_imageSizeY = m_dataGrid.getHeight();
            m_imageSizeX1 = m_imageSizeX - 1;
            m_imageSizeY1 = m_imageSizeY - 1;
            if (DEBUG) printf("%s image cached.  w: %d  h: %d  size: %f x %f \n",this,m_imageSizeX,m_imageSizeY,m_sizeX,m_sizeY);
        }
        
        m_xfactor = m_imageSizeX/m_sizeX;
        m_yfactor = m_imageSizeX/m_sizeY;

        m_gradXfactor = m_tilesX*m_imageThickness*m_xfactor/2;
        m_gradYfactor = m_tilesY*m_imageThickness*m_yfactor/2;

        return ResultCodes.RESULT_OK;
    }

    /**
     * @noRefGuide
     */
    private int prepareImage(){

        if(DEBUG)printf("Image3D.prepareImage();\n");

        long t0 = time();

        BufferedImage image = null;

        Object oimage = mp_image.getValue();
        //printf("Image3D.  buff_image: %s\n",image);

        if (oimage == null) {
            return ResultCodes.RESULT_ERROR;            
        }

        if (oimage instanceof String) {
            try {
                File f = new File((String)oimage);
                image = ImageIO.read(f);                
            } catch (Exception e) {

                printf("ERROR READING IMAGE: '%s' msg: %s\n", (String)oimage,e.getMessage());
                StackTraceElement[] st = Thread.currentThread().getStackTrace();
                int len = Math.min(10, st.length);
                for (int i = 1; i < len; i++)
                    printf("\t\t %s\n", st[i]);
                return ResultCodes.RESULT_ERROR;
            }

        } else if (oimage instanceof BufferedImage) {
            image = (BufferedImage) oimage;
        } else if (oimage instanceof ImageWrapper) {
            image = ((ImageWrapper)oimage).getImage();
        } else if (oimage instanceof Grid2D) {
            Grid2D grid = (Grid2D)oimage;
            if(grid.getDataDesc().isDistanceData(m_dataChannelIndex)){
                return prepareDataFromDistance(grid);
            } else {
                image = Grid2DShort.convertGridToImage(grid);
            }
        } else if (oimage instanceof Text2D) {
            image = ((Text2D)oimage).getImage();
        } else {
            throw new IllegalArgumentException("Unhandled image type: " + oimage.getClass());
        }

        if (image == null) {
            printf("Image is null.  source: %s  class: %s\n",oimage,oimage.getClass());
            return ResultCodes.RESULT_ERROR;
        }
        if(DEBUG)printf("image %s [%d x %d ] reading done in %d ms\n", oimage, image.getWidth(), image.getHeight(), (time() - t0));

        long t1 = time();
        short imageDataShort[] = ImageUtil.getGray16Data(image);
        ImageGray16 imageData = new ImageGray16(imageDataShort, image.getWidth(), image.getHeight());

        if(DEBUG)printf("imageData done in %d ms\n", (time() - t1));

        double voxelSize = mp_voxelSize.getValue();
        if (voxelSize > 0.0) {
            // we have finite voxel size, try to scale the image down to reasonable size 
            double pixelSize = (m_sizeX / (imageData.getWidth() * m_tilesX));
            double pixelsPerVoxel = voxelSize / pixelSize;
            if(DEBUG)printf("pixelsPerVoxel: %f\n", pixelsPerVoxel);

            if (pixelsPerVoxel > MAX_PIXELS_PER_VOXEL) {

                double newPixelSize = voxelSize / MAX_PIXELS_PER_VOXEL;
                int newWidth = (int) Math.ceil((m_sizeX / m_tilesX) / newPixelSize);
                int newHeight = (imageData.getHeight() * newWidth) / imageData.getWidth();
                if(DEBUG)printf("resampling image[%d x %d] -> [%d x %d]\n",
                        imageData.getWidth(), imageData.getHeight(), newWidth, newHeight);
                t1 = time();
                //short[] newData = getScaledDownData(imageDataShort, imageData.getWidth(), imageData.getHeight(), newWidth, newHeight);
                short[] newData = getScaledDownDataBlack(imageDataShort, imageData.getWidth(), imageData.getHeight(), newWidth, newHeight);

                if(DEBUG)printf("resampling image[%d x %d] -> [%d x %d]  done in %d ms\n",
                        imageData.getWidth(), imageData.getHeight(), newWidth, newHeight, (time() - t1));
                imageData = new ImageGray16(newData, newWidth, newHeight);
            }
        }

        m_imageSizeX = imageData.getWidth();
        m_imageSizeY = imageData.getHeight();
        m_imageSizeX1 = m_imageSizeX - 1;
        m_imageSizeY1 = m_imageSizeY - 1;
        
        double blurWidth = mp_blurWidth.getValue();

        if (blurWidth > 0.0) {

            double pixelSize = (m_sizeX / (m_imageSizeX * m_tilesX));

            double blurSizePixels = blurWidth / pixelSize;
            t1 = time();
            imageData.gaussianBlur(blurSizePixels);

            if(DEBUG)printf("Image3D image[%d x %d] gaussian blur: %7.2f pixels blur width: %10.5fmm time: %d ms\n",
                   m_imageSizeX, m_imageSizeY, blurSizePixels, blurWidth/MM, (time() - t1));
        }

        int res = 0;

        if (m_useGrayscale) {
            res = makeImageGray(imageData);            
        } else {
            res = makeImageBlack(imageData);
        }

        if(DEBUG)printf("Image3D.prepareImage() time: %d ms\n", (time() - t0));

        if (DEBUG_VIZ) {
            try {
                printf("***Writing debug file for Image3D");
                String source = null;
                Object src = mp_image.getValue();
                if (src instanceof SourceWrapper) {
                    source = ((SourceWrapper)src).getParamString();
                } else {
                    source = "" + src.hashCode();
                }
                source = source.replace("\\","_");
                source = source.replace("/","_");
                source = source.replace(".","_");
                source = source.replace("\"","_");
                source = source.replace(";","_");

                printf("final: %s\n",source);
                imageData.write("/tmp/image3d_" + source + ".png");
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return res;

    }

    /**
       we already have distance grid 
       will use it directly 
     */
    protected int prepareDataFromDistance(Grid2D distGrid){

        m_imageSizeX = distGrid.getWidth();
        m_imageSizeY = distGrid.getHeight();
        m_imageSizeX1 = m_imageSizeX - 1;
        m_imageSizeY1 = m_imageSizeY - 1;
        
        m_dataGrid = distGrid;
        m_dataChannel = m_dataGrid.getDataDesc().getChannel(m_dataChannelIndex);
        return 0;
    }


    /**
       makes data for black and white image 
       data is represented as distance from 2D outline of the image
       @noRefGuide
     */
    protected int makeImageBlack(ImageGray16 image){

        long t0 = time();

        int nx = image.getWidth();
        int ny = image.getHeight();
        double imagePixelSize = ((Vector3d)mp_size.getValue()).x/nx;
        if(DEBUG)printf("makeImageBlack()  threshold: %f  pixelSize: %f\n",m_imageThreshold,imagePixelSize);

        Grid2DShort imageGrid = Grid2DShort.convertImageToGrid(image, (m_imageType == IMAGE_TYPE_EMBOSSED), imagePixelSize);

        double maxOutDistance = imagePixelSize*m_maxOutDistancePixels;
        double maxInDistance = imagePixelSize*m_maxInDistancePixels;

        DistanceTransform2DOp dt = new DistanceTransform2DOp(maxInDistance, maxOutDistance, m_imageThreshold);
        Grid2D distanceGrid = dt.execute(imageGrid);
        m_dataGrid = distanceGrid;

        if (DEBUG_VIZ) {
            // TODO: this is not a good viz.  Should use GridUtil.writeSlice but it needs to support Grid2D interface
            BufferedImage img = Grid2DShort.convertGridToImage(distanceGrid);
            try {

                ImageIO.write(img, "PNG",new File("/tmp/black_dist.png"));
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        m_dataChannel = m_dataGrid.getDataDesc().getChannel(0);

        if(DEBUG)printf("makeImageBlack() done %d ms\n", time() -t0);

        return ResultCodes.RESULT_OK;
        
        
    }

    /**
     * @noRefGuide
     */
    protected int makeImageGray(ImageGray16 image){

        long t0 = time();
        if(DEBUG)printf("makeImageGray()\n");

        if (m_interpolationType == INTERPOLATION_MIPMAP) {

            t0 = time();
            if(true)throw new RuntimeException("INTERPOLATION_MIPMAP not implemented");
            //m_mipMap = new ImageMipMapGray16(imageDataShort, m_imageSizeX, m_imageSizeY);
            
            if(DEBUG)printf("mipmap ready in %d ms\n", (time() - t0));

        } else {
            
            double imagePixelSize = ((Vector3d)mp_size.getValue()).x/image.getWidth();
            m_dataGrid = Grid2DShort.convertImageToGrid(image, (m_imageType == IMAGE_TYPE_EMBOSSED), imagePixelSize);
            m_dataChannel = m_dataGrid.getDataDesc().getChannel(0);

        }

        if(DEBUG)printf("makeImageGray() done %d ms\n", time() -t0);

        return ResultCodes.RESULT_OK;
                
    }

    /**
     * calculates value of shape distance or density 
     * the 
     * @return ResultCodes.RESULT_OK
     *
     * @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec data) {

        double dist = getDistanceValue(pnt);
        data.v[0] = getShapeValue(dist, pnt);
        
        return ResultCodes.RESULT_OK;
        
    }

    /**      
     *
     *  @return distance to the Image3D
     *  
     */
    public double getDistanceValue(Vec pnt){
        if(m_useGrayscale)
            return getDistanceGray(pnt);
        else 
            return getDistanceBW(pnt);
    }


    /**
     *       
     *   returns distance to gray image 3D 
     *  
     */    
    public double getDistanceGray(Vec pnt){

        final boolean PRINT_VALUES = false;

        // original coord 
        double 
            x0 = pnt.v[0],
            y0 = pnt.v[1],
            z0 = pnt.v[2];
        if(PRINT_VALUES)printf("pnt: (%7.5f %7.5f %7.5f)\n",x0,y0,z0);
        double x,y,z; // coord in image units
        x = x0 - m_xmin;
        y = y0 - m_ymin;
        z = z0 - m_zmin;
        // transform coord treat all 3 cases in uniform way 
        switch(m_imagePlace){
        default:
        case IMAGE_PLACE_TOP:  
            // canonic orientation 
            break;
        case IMAGE_PLACE_BOTTOM:
            // flip z 
            z = m_sizeZ-z;
            break;
        case IMAGE_PLACE_BOTH: // both sides
            // fold and scale z 
            z = abs(2*z - m_sizeZ);
            break;
        }
       
        x *= m_xfactor;
        y *= m_yfactor;

        if(PRINT_VALUES)printf("pnt_grid: (%7.5f %7.5f %7.5f)\n",x,y,z);

        x = clamp(x, 0., (double)m_imageSizeX);
        y = clamp(y, 0., (double)m_imageSizeY);
        // x, y are in [0, 0, m_imageSizeX, mageHeight] range

        if(m_tilesX > 1) x = fmod(x*m_tilesX,(double)m_imageSizeX);
        if(m_tilesY > 1) y = fmod(y*m_tilesY,(double)m_imageSizeY);
        int ix = (int)x;
        int iy = (int)y;
        double 
            dx = x - ix,
            dy = y - iy;

        ix = clamp(ix, 0, m_imageSizeX1);
        iy = clamp(iy, 0, m_imageSizeY1);
        int ix1 = ix+1;
        int iy1 = iy+1;
        ix1 = clamp(ix1, 0, m_imageSizeX1);
        iy1 = clamp(iy1, 0, m_imageSizeY1);
        int ix_1 = clamp(ix-1, 0, m_imageSizeX1);
        int iy_1 = clamp(iy-1, 0, m_imageSizeY1);
        int ix2 = clamp(ix+2, 0, m_imageSizeX1);
        int iy2 = clamp(iy+2, 0, m_imageSizeY1);

        double v00 = getImageValue(ix, iy);
        double v10 = getImageValue(ix1, iy);
        double v01 = getImageValue(ix, iy1);
        double v11 = getImageValue(ix1, iy1);
        // interpolated image value 
        double iValue = lerp2(v00, v10, v01, v11, dx, dy);
        // iValue is in range (0,1)

        if(PRINT_VALUES) printf("iValue: %7.5f\n", iValue);
        //if(PRINT_VALUES) printf("hfDist: %7.5f\n", hfDist);        
        double v_10 = getImageValue(ix_1, iy);
        double v20 = getImageValue(ix2, iy);
        double v_11 = getImageValue(ix_1, iy1);
        double v21 = getImageValue(ix2, iy1);
        double v0_1 = getImageValue(ix, iy_1);
        double v02 = getImageValue(ix, iy2);
        double v1_1 = getImageValue(ix1, iy_1);
        double v12 = getImageValue(ix1, iy2);

        double gx00 = (v10 - v_10);
        double gx10 = (v20 - v00);
        double gx01 = (v11 - v_11);
        double gx11 = (v21 - v01);
        double gy00 = (v01 - v0_1);
        double gy10 = (v11 - v1_1);
        double gy01 = (v02 - v00);
        double gy11 = (v12 - v10);
        
        double gradX = lerp2(gx00, gx10, gx01, gx11, dx, dy)*m_gradXfactor;
        double gradY = lerp2(gy00, gy10, gy01, gy11, dx, dy)*m_gradYfactor;
        double slopeFactor = 1/sqrt(m_slopeZcoeff + gradX*gradX + gradY*gradY);
        double thresholdFactor = m_imageThickness/max(MIN_GRADIENT, sqrt(gradX*gradX + gradY*gradY));
        // distance in xy pane to image threshold 
        double thresholdDist = -(iValue-m_baseRelThreshold)* thresholdFactor;

        // image value in physical units
        iValue = iValue*m_imageThickness+m_baseThickness;

        double dist = slopeFactor*(z - iValue); // distance to surface 
        if(m_imagePlace != IMAGE_PLACE_BOTH) 
             dist = max( dist, m_baseHalfSizeZ - z);

        if(PRINT_VALUES) printf("grad: %7.5f %7.5f final hfDist: %7.5f\n", gradX, gradY, dist);
               
        dist = blendMax( dist, thresholdDist, m_rounding);
        double distBoxXY = blendMax(abs(x0 - m_centerX) - m_halfSizeX,abs(y0 - m_centerY) - m_halfSizeY, m_rounding);
        dist = blendMax(dist, distBoxXY, m_rounding);
        //add base      
        if(m_hasBase){
            double distBase = blendMax(distBoxXY, abs(z0 - m_baseCenterZ) - m_baseHalfSizeZ, m_rounding);
            dist  = blendMin(dist, distBase, m_rounding);
        }
        return dist;
    }

    /**
       returns distance to BW image 
     */
    public double getDistanceBW(Vec pnt){

        final boolean PRINT_VALUES = false;
        double 
            x0 = pnt.v[0],
            y0 = pnt.v[1],
            z0 = pnt.v[2];
        if(PRINT_VALUES)printf("pnt: (%7.5f %7.5f %7.5f)\n",x0,y0,z0);
        double x,y,z; // coord in image units
        x = x0 - m_xmin;
        y = y0 - m_ymin;
        z = z0 - m_zmin;       
        x *= m_xfactor;
        y *= m_yfactor;

        if(PRINT_VALUES)printf("pnt_grid: (%7.5f %7.5f %7.5f)\n",x,y,z);

        x = clamp(x, 0., (double)m_imageSizeX);
        y = clamp(y, 0., (double)m_imageSizeY);
        // x, y are in [0, 0, m_imageSizeX, mageHeight] range

        if(m_tilesX > 1) x = fmod(x*m_tilesX,(double)m_imageSizeX);
        if(m_tilesY > 1) y = fmod(y*m_tilesY,(double)m_imageSizeY);
        int ix = (int)x;
        int iy = (int)y;
        double 
            dx = x - ix,
            dy = y - iy;

        ix = clamp(ix, 0, m_imageSizeX1);
        iy = clamp(iy, 0, m_imageSizeY1);
        int ix1 = ix+1;
        int iy1 = iy+1;
        ix1 = clamp(ix1, 0, m_imageSizeX1);
        iy1 = clamp(iy1, 0, m_imageSizeY1);

        double v00 = getImageValue(ix, iy);
        double v10 = getImageValue(ix1, iy);
        double v01 = getImageValue(ix, iy1);
        double v11 = getImageValue(ix1, iy1);
        
        // image is precalculated to return normalized value of distance to the side 
        double iValue = 1/(0.5*(m_tilesX + m_tilesY))*lerp2(v00, v10, v01, v11, dx, dy);

        double dist = iValue; 
        dist = blendMax(dist, z - m_sizeZ, m_rounding); // top crop 
        dist = blendMax(dist, -z, m_rounding); // bottom crop 

        double dBoxXY = blendMax(abs(x0 - m_centerX) - m_halfSizeX,abs(y0 - m_centerY) - m_halfSizeY, m_rounding);
        dist = blendMax(dist, dBoxXY, m_rounding);
                
        if(m_hasBase){
            double dBase = blendMax(dBoxXY, abs(z0 - m_baseCenterZ) - m_baseHalfSizeZ, m_rounding);
            dist  = blendMin(dist, dBase, m_rounding);            
        }
        
        return dist;        
    }

    /**
     * calculation for finite voxel size
     *
     * @noRefGuide
     */
    /*
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
            return ResultCodes.RESULT_OK;
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

        double baseValue = intervalCap(z, baseBottom, m_imageZmin, vs);
        double finalValue = baseValue;

        double dd = vs;

        double imageX = (x - xmin) * xscale; // x and y are now in (0,1)
        double imageY = 1. - (y - ymin) * yscale;

        if (m_tilesX > 1) {
            imageX *= m_tilesX;
            imageX -= Math.floor(imageX);
        }
        if (m_tilesY > 1) {
            imageY *= m_tilesY;
            imageY -= Math.floor(imageY);
        }

        imageX *= m_imageSizeX;
        imageY *= m_imageSizeY;

        // image x and imageY are in image units now 
        int ix = clamp((int) Math.floor(imageX), 0, m_imageSizeX1);
        int iy = clamp((int) Math.floor(imageY), 0, m_imageSizeY1);
        int ix1 = clamp(ix + 1, 0, m_imageSizeX1);
        int iy1 = clamp(iy + 1, 0, m_imageSizeY1);
        double dx = imageX - ix;
        double dy = imageY - iy;
        double dx1 = 1. - dx;
        double dy1 = 1. - dy;
        double v00 = getImageValue(ix, iy);
        double v10 = getImageValue(ix1, iy);
        double v01 = getImageValue(ix, iy1);
        double v11 = getImageValue(ix1, iy1);

        //if(debugCount-- > 0) printf("xyz: (%7.5f, %7.5f,%7.5f) ixy[%4d, %4d ] -> v00:%18.15f\n", x,y,z, ix, iy, v00);

        double h0 = (dx1 * (v00 * dy1 + v01 * dy) + dx * (v11 * dy + v10 * dy1));

        double imageValue = 0.; // distance to the image 

        if (!m_useGrayscale) {

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

            // grayscale image 

            if (h0 < m_baseThreshold) {
                // TODO - better treatment of threshold 
                // transparent background 
                imageValue = 0.;

            } else {

                double z0 = imageZmin + imageZScale * h0;
                double bottomStep = step((z - (imageZmin - vs)) / (2 * vs));

                //TODO - better calculation of normal in case of tiles
                double pixelSize = (m_sizeX / (m_imageSizeX * m_tilesX));
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

        return ResultCodes.RESULT_OK;

    }
    */

    /**
     * @return normalized value of image data at the given point 
     * @noRefGuide
     */
    final double getImageValue(int ix, int iy) {
        
        try {
            return m_dataChannel.getValue(m_dataGrid.getAttribute(ix, iy));
        } catch (Exception e) {
            e.printStackTrace(Output.out);
        }
        return 0.;
    }

    /**
     * @noRefGuide
     */
    /*
    private double getHeightFieldValue(double x, double y, double probeSize) {

        x = (x - xmin) * xscale; // x and y are now in (0,1)
        y = 1. - (y - ymin) * yscale;

        if (m_tilesX > 1) {
            x *= m_tilesX;
            x -= Math.floor(x);
        }
        if (m_tilesY > 1) {
            y *= m_tilesY;
            y -= Math.floor(y);
        }

        x *= m_imageSizeX;
        y *= m_imageSizeY;

        probeSize *= (xscale * m_imageSizeX);

        double v = getPixelValue(x, y, probeSize);

        v = m_imageZmin + imageZScale * v;

        return v;

    }
    */
    /**
     * calculation for zero voxel size
     *
     * @noRefGuide
     */
    /*
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
            return ResultCodes.RESULT_OK;
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
            return ResultCodes.RESULT_OK;
        }

        if (m_tilesX > 1) {
            x *= m_tilesX;
            x -= Math.floor(x);
        }
        if (m_tilesY > 1) {
            y *= m_tilesY;
            y -= Math.floor(y);
        }

        double imageX = m_imageSizeX * x;
        double imageY = m_imageSizeY * (1. - y);// reverse Y-direction

        double pixelValue = getPixelValue(imageX, imageY, 0.);
        
        //if(debugCount-- > 0)
        //    printf("imageXY: [%7.2f, %7.2f] -> pixelValue: %8.5f\n", imageX, imageY, pixelValue);
       
        double d = 0;

        if (m_useGrayscale) {

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

        return ResultCodes.RESULT_OK;
    }
    */

    /**
     * returns value of pixel at given x,y location. 
     * returned value normalized to (0,1)
     * x is inside [0, m_imageSizeX]
     * y is inside [0, m_imageSizeY]
     *
     * @noRefGuide
     */
    double getPixelValue(double x, double y, double probeSize) {

        double grayLevel;

        if (x <= 0 || x >= m_imageSizeX || y <= 0 || y >= m_imageSizeY) {

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
    /**
     * @noRefGuide
     */
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

        x0 = clamp(x0, 0, m_imageSizeX - 1);
        x1 = clamp(x1, 0, m_imageSizeX - 1);
        y0 = clamp(y0, 0, m_imageSizeY - 1);
        y1 = clamp(y1, 0, m_imageSizeY - 1);

        int yoffset0 = y0 * m_imageSizeX;
        int yoffset1 = y1 * m_imageSizeX;

        double d00 = getImageValue(x0, y0);
        double d10 = getImageValue(x1, y0);
        double d01 = getImageValue(x0, y1);
        double d11 = getImageValue(x1, y1);
        return (dx1 * (d00 * dy1 + d01 * dy) + dx * (d11 * dy + d10 * dy1));
        
    }

    /**
     * @noRefGuide
     */
    final double getPixelBoxShort(double x, double y) {

        int ix = clamp((int) Math.floor(x), 0, m_imageSizeX - 1);
        int iy = clamp((int) Math.floor(y), 0, m_imageSizeY - 1);

        return getImageValue(ix, iy);

    }
    
}  // class Image3D
