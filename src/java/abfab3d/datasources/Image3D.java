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


import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.Grid2D;
import abfab3d.core.Grid2DProducer;
import abfab3d.core.GridDataChannel;
import abfab3d.core.ImageProducer;
import abfab3d.core.Output;
import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.Grid2DSourceWrapper;
import abfab3d.grid.Operation2D;
import abfab3d.grid.op.Copy;
import abfab3d.grid.op.DistanceTransform2DOp;
import abfab3d.grid.op.GaussianBlur;
import abfab3d.grid.op.GridValueTransformer;
import abfab3d.grid.op.ImageLoader;
import abfab3d.grid.op.ImageToGrid2D;
import abfab3d.grid.op.ResampleOp;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.BooleanParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.ParamCache;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.util.ImageMipMapGray16;

import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.util.Vector;

import static abfab3d.core.MathUtil.lerp2;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.fmod;
import static abfab3d.core.MathUtil.blendMax;
import static abfab3d.core.MathUtil.blendMin;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.Math.max;


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
 * <p>
 * </p>
 *
 * @author Vladimir Bulatov
 */
public class Image3D extends TransformableDataSource {

    final static boolean DEBUG = false;
    final static boolean DEBUG_VIZ = false;
    final static boolean CACHING_ENABLED = true;

    public static final int IMAGE_TYPE_EMBOSSED = 0, IMAGE_TYPE_ENGRAVED = 1;
    public static final int IMAGE_PLACE_TOP = 0, IMAGE_PLACE_BOTTOM = 1, IMAGE_PLACE_BOTH = 2;
    public static final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1, INTERPOLATION_MIPMAP = 2;
    static final double MIN_GRADIENT = 0.05;
    static final String MEMORY_IMAGE = "[memory image]";

    SNodeParameter mp_source = new SNodeParameter("source", "Image source", null);

    // public params of the image 
    //ObjectParameter mp_image = new ObjectParameter("image","Image source", null); // obsolete 
    Vector3dParameter mp_center = new Vector3dParameter("center", "center of the image box", new Vector3d(0, 0, 0));
    Vector3dParameter mp_size = new Vector3dParameter("size", "size of the image box", new Vector3d(0.1, 0.1, 0.1));
    // rounding of the edges

    DoubleParameter mp_rounding = new DoubleParameter("rounding", "rounding of the box edges", 0.);
    IntParameter mp_imageType = new IntParameter("imageType", "placement of the image", IMAGE_TYPE_EMBOSSED, 0, 1);
    IntParameter mp_imagePlace = new IntParameter("imagePlace", "placement of the image", IMAGE_PLACE_TOP, 0, IMAGE_PLACE_BOTH);
    IntParameter mp_tilesX = new IntParameter("tilesX", "image tiles in x-direction", 1);
    IntParameter mp_tilesY = new IntParameter("tilesY", "image tiles in y-direction", 1);
    DoubleParameter mp_baseThickness = new DoubleParameter("baseThickness", "relative thickness of image base", 0.);
    BooleanParameter mp_useGrayscale = new BooleanParameter("useGrayscale", "Use grayscale for image rendering", true);
    BooleanParameter mp_useImageProcessing = new BooleanParameter("useImageProcessing", "Use default image processing", true);
    DoubleParameter mp_blurWidth = new DoubleParameter("blurWidth", "width of gaussian blur on the image", 0.);
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "size of voxel to use for image voxelization", 0.);
    DoubleParameter mp_baseThreshold = new DoubleParameter("baseThreshold", "threshold of the image", 0.01);
    DoubleParameter mp_pixelsPerVoxel = new DoubleParameter("pixelsPerVoxel", "image pixels per voxel", 3.);
    DoubleParameter mp_maxDist = new DoubleParameter("maxDist", "maximal distance to calculate distance transform", 20 * MM);

    Parameter m_aparam[] = new Parameter[]{
        mp_source,
        //mp_image,
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
        mp_imageType,
        mp_voxelSize,
        mp_maxDist,
        mp_pixelsPerVoxel,
        mp_useImageProcessing,
    };

    // Params which require changes in the underlying image 
    Parameter m_imageParams[] = new Parameter[]{
        mp_source,
        mp_useImageProcessing,
        mp_size,
        mp_tilesX,
        mp_tilesY,  // Not technically used in prepare image?
        mp_imageType,
        mp_blurWidth,
        mp_useGrayscale,
        mp_maxDist
    };

    public static final double DEFAULT_PIXEL_SIZE = 0.1 * MM;

    //static double EPSILON = 1.e-3;
    //static final double MAX_PIXELS_PER_VOXEL = 3.;

    public static final double DEFAULT_VOXEL_SIZE = 0.1 * MM;

    // size of the box 
    protected double m_sizeX = 0., m_sizeY = 0., m_sizeZ = 0.;
    protected double m_halfSizeX, m_halfSizeY, m_halfSizeZ;
    // location of the box
    protected double m_centerX = 0, m_centerY = 0, m_centerZ = 0;

    int m_imagePlace;

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

    //protected int m_imageType = IMAGE_TYPE_EMBOSSED;


    protected int m_tilesX = 1; // number of image tiles in x-direction 
    protected int m_tilesY = 1; // number of image tiles in y-direction 

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

    // solid white color of background to be used for images with transparency
    private double m_backgroundColor[] = new double[]{255., 255., 255., 255.};
    private int m_backgroundColorInt = 0xFFFFFFFF;

    private double m_imageThickness; // thickness of image layer in physical units

    private double m_imageThreshold = 0.5; // this is for black and white case. below threshold we have solid voxel, above - empty voxel  
    // maximal distance to calculate distance transform 
    // it mostly affect precision of stored distance, because distance is stored in 16 bits of short
    //private double m_maxDistPixels = 100;

    private static Grid2D m_emptyGrid = new Grid2DShort(1, 1, DEFAULT_PIXEL_SIZE);

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
     * @param sx        width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy        height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz        depth of the box.
     */
    public Image3D(String imagePath, double sx, double sy, double sz) {
        this(imagePath, sx, sy, sz, 0.);
    }

    /**
     * Image3D with given image path and size
     *
     * @param imagePath path to the image file
     * @param sx        width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy        height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz        depth of the box.
     * @param voxelSize size of voxel to be used for image voxelization
     */
    public Image3D(String imagePath, double sx, double sy, double sz, double voxelSize) {
        this(new ImageToGrid2D(new ImageLoader(imagePath)), sx, sy, sz, voxelSize);
    }

    /**
     * Image3D with given image path and size
     *
     * @param grid image producer
     * @param sx       width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy       height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz       depth of the box.
     */
    public Image3D(Grid2D grid, double sx, double sy, double sz) {
        // use memory hash as label  
        this((Grid2DProducer) (new Grid2DSourceWrapper(grid.toString(), grid)), sx, sy, sz);
        //mp_useImageProcessing.setValue(false);   // Breaks backwards comp
    }

    /**
     * Image3D with given image path and size
     *
     * @param grid image producer
     * @param sx       width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy       height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz       depth of the box.
     */
    public Image3D(Grid2D grid, double sx, double sy, double sz,double vs) {
        // use memory hash as label
        this((Grid2DProducer) (new Grid2DSourceWrapper(grid.toString(), grid)), sx, sy, sz);
        //mp_useImageProcessing.setValue(false);  // Breaks backwards compat
        setVoxelSize(vs);
    }

    /**
     * Image3D with given image path and size
     *
     * @param imgProducer holder of BufferedImage
     * @param sx        width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy        height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz        depth of the box.
     */
    public Image3D(BufferedImage imgProducer, double sx, double sy, double sz) {
        // Added for backwards compatibility
        this(new ImageToGrid2D(imgProducer), sx, sy, sz, 0.);
    }


    /**
     * Image3D with given image and size
     *
     * @param imgProducer producer of the image
     * @param sx          width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy          height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz          depth of the box.
     * @param voxelSize   size of voxel to be used for image voxelization
     */
    public Image3D(BufferedImage imgProducer, double sx, double sy, double sz, double voxelSize) {
        // Added for backwards compatibility
        this(new ImageToGrid2D(imgProducer), sx, sy, sz, voxelSize);
    }

    /**
     * Image3D with given image path and size
     *
     * @param imgProducer holder of BufferedImage
     * @param sx        width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy        height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz        depth of the box.
     */
    public Image3D(ImageProducer imgProducer, double sx, double sy, double sz) {
        this(new ImageToGrid2D(imgProducer), sx, sy, sz, 0.);
    }


    /**
     * Image3D with given image and size
     *
     * @param imgProducer producer of the image
     * @param sx          width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy          height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz          depth of the box.
     * @param voxelSize   size of voxel to be used for image voxelization
     */
    public Image3D(ImageProducer imgProducer, double sx, double sy, double sz, double voxelSize) {
        this(new ImageToGrid2D(imgProducer), sx, sy, sz, voxelSize);
    }

    /**
     * Image3D with given image path and size
     *
     * @param producer image producer
     * @param sx       width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy       height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz       depth of the box.
     */
    public Image3D(Grid2DProducer producer, double sx, double sy, double sz) {
        this(producer, sx, sy, sz, 0.);
    }

    /**
     * Image3D with given image path and size
     *
     * @param producer image producer
     * @param sx       width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy       height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz       depth of the box.
     */
    public Image3D(Grid2DSourceWrapper producer, double sx, double sy, double sz, double vs) {
        this((Grid2DProducer) producer, sx, sy, sz,vs);  // TODO: Alan added
    }

    /**
     * Image3D with given image path and size
     *
     * @param producer image producer
     * @param sx       width of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sy       height of the box (if it is 0.0 it will be calculated automatically to maintain image aspect ratio
     * @param sz       depth of the box.
     * @param vs       voxel size used for image voxelization
     */
    public Image3D(Grid2DProducer producer, double sx, double sy, double sz, double vs) {
        initParams();
        if (DEBUG) printf("Image3D(Grid2DProducer %s) !!!\n", producer);
        mp_source.setValue(producer);
        setSize(sx, sy, sz);
        setVoxelSize(vs);
    }


    public void setUseImageProcessing(boolean val) {
        mp_useImageProcessing.setValue(val);
    }

    /**
     * @noRefGuide
     */
    protected void initParams() {
        super.addParams(m_aparam);
    }

    /**
     * @noRefGuide
     * @deprecated Remove next release
     */
    public void setDistanceFactor(double value) {
        //mp_distanceFactor.setValue(new Double(value));
    }

    /**
     * Set size of the image box
     *
     * @param sx The x dimension in meters
     * @param sy The y dimension in meters
     * @param sz The z dimension in meters
     */
    public void setSize(double sx, double sy, double sz) {
        mp_size.setValue(new Vector3d(sx, sy, sz));
    }

    /**
     * Set size of the image box
     *
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
     *
     * @param cx The x location in meters
     * @param cy The y location in meters
     * @param cz The z location in meters
     */
    public void setCenter(double cx, double cy, double cz) {
        mp_center.setValue(new Vector3d(cx, cy, cz));

    }

    /**
     * Set center of the image box
     *
     * @param val The center in meters
     */
    public void setCenter(Vector3d val) {
        mp_center.setValue(val);

    }

    /**
     * Get center of the image box
     *
     * @return
     */
    public Vector3d getCenter() {
        return mp_center.getValue();
    }

    /**
     * Set image tiling
     *
     * @param tilesX the number of X tiles
     * @param tilesY the number of Y tiles
     */
    public void setTiles(int tilesX, int tilesY) {

        mp_tilesX.setValue(new Integer(tilesX));
        mp_tilesY.setValue(new Integer(tilesY));
    }

    /**
     * Set image tilingX
     *
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
     *
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
     *
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
     *
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

        if (val instanceof String) {
            val = new ImageToGrid2D(new ImageLoader((String) val));
        } else if (val instanceof Grid2DSourceWrapper) {
              // fine
        } else if (val instanceof Grid2D) {
            Grid2D grid2d = (Grid2D) val;
            val = new Grid2DSourceWrapper(grid2d.toString(),grid2d);
        } else if (val instanceof BufferedImage) {
            val = new ImageToGrid2D((BufferedImage)val);
        } else if (val instanceof ImageProducer) {
            val = new ImageToGrid2D((ImageProducer) val);
        } else if (val instanceof Grid2DProducer) {
            // fine
        } else {
            throw new IllegalArgumentException("Unsupported object for Image3D: " + val.getClass());
        }

        mp_source.setValue(val);
    }

    /**
     * set options to image embossing type
     *
     * @param type Type ot the image. Possible values Image3D.IMAGE_TYPE_EMBOSSED (default value), Image3D.IMAGE_TYPE_ENGRAVED.
     */
    public void setImageType(int type) {

        mp_imageType.setValue(type);

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
     * Set the current value of a parameter.
     * @param paramName The name
     * @param value the value
     */
    @Override
    public void set(String paramName, Object value) {
        if (paramName.equals("distanceFactor")) {
            // ignore for backwards compatibility
            return;
        }

        super.set(paramName,value);
    }


    /**
     * returns physical data value which corresponds to the given attribute value
     */
    public double getPhysicalValue(long attribute) {
        return m_dataChannel.getValue(attribute);
    }

    /**
     * @noRefGuide
     */
    public int getBitmapWidth() {
        return m_dataGrid.getWidth();
    }

    /**
     * @noRefGuide
     */
    public int getBitmapHeight() {
        return m_dataGrid.getHeight();
    }

    /**
     * @noRefGuide
     */
    public void getBitmapData(byte data[]) {
        getBitmapDataUByte(data);
    }

    /**
     * @noRefGuide
     */
    public void getBitmapDataUByte(byte data[]) {

        int nx = m_dataGrid.getWidth();
        int ny = m_dataGrid.getHeight();
        double base = m_baseRelThreshold;
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                double d = getImageValue(x, y);
                // normalization to byte 
                data[x + y * nx] = (byte) ((int) (d * 0xFF) & 0xFF);
            }
        }
    }

    // store bitmap data as 16 bit shorts 

    /**
     * @noRefGuide
     */
    public void getBitmapDataUShort(byte data[]) {

        int nx = m_dataGrid.getWidth();
        int ny = m_dataGrid.getHeight();
        int nx2 = nx * 2;

        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {

                long id = m_dataGrid.getAttribute(x, y);

                //double d = getImageValue(x,y);
                // normalization to byte 
                //int id = ((int)(d * 0xFFFF)) & 0xFFFF;
                int ind = 2 * x + y * nx2;
                data[ind] = (byte) (id & 0xFF);
                data[ind + 1] = (byte) ((id >> 8) & 0xFF);
            }
        }
    }

    /**
     * Get a label suitable for caching.  Includes only the items that would affect the computationally expensive items to cache.
     * @return
     */
    public void getDataLabel(StringBuilder sb) {
        getParamString(getClass().getSimpleName(), m_imageParams,sb);
    }

    /**
     * Get a label suitable for caching.  Includes only the items that would affect the computationally expensive items to cache.
     * @return
     */
    public String getDataLabel() {
        return getParamString(getClass().getSimpleName(), m_imageParams);
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();

        if (DEBUG) printf("%s.initialize()\n", this);

        Vector3d c = mp_center.getValue();
        m_centerX = c.x;
        m_centerY = c.y;
        m_centerZ = c.z;

        Vector3d s = mp_size.getValue();
        m_sizeX = s.x;
        m_sizeY = s.y;
        m_sizeZ = s.z;

        m_halfSizeX = m_sizeX / 2;
        m_halfSizeY = m_sizeY / 2;
        m_halfSizeZ = m_sizeZ / 2;

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

        m_imageThickness = (1. - m_baseRelThickness) * m_sizeZ;
        m_baseThickness = m_baseRelThickness * m_sizeZ;
        m_hasBase = (m_baseRelThickness > 0.);
        m_baseHalfSizeZ = m_baseThickness / 2;

        switch (m_imagePlace) {
            case IMAGE_PLACE_BOTH:
                m_baseCenterZ = m_centerZ;
                m_slopeZcoeff = 4;
                break;
            case IMAGE_PLACE_TOP:
                m_baseCenterZ = m_zmin + m_baseHalfSizeZ;
                m_slopeZcoeff = 1;
                break;
            case IMAGE_PLACE_BOTTOM:
                m_baseCenterZ = m_zmax - m_baseHalfSizeZ;
                m_slopeZcoeff = 1;
                break;
        }

        String label = getDataLabel();

        Object co = null;
        if (CACHING_ENABLED) {
            if (DEBUG) printf("\nChecking cache.  label: %s  hash: %d\n",label,label.hashCode());
            co = ParamCache.getInstance().get(label);
        }
        if (co == null) {
            m_dataGrid = prepareImage();
            if (CACHING_ENABLED) ParamCache.getInstance().put(label, m_dataGrid);
            if (DEBUG) printf("Image3D: caching image: %s hc: %d -> %s\n", label, label.hashCode(),m_dataGrid);
        } else {
            m_dataGrid = (Grid2D) co;
            if (DEBUG) printf("Image3D: got cached image %s -> %s\n", label, m_dataGrid);
        }

        m_dataChannel = m_dataGrid.getDataDesc().getChannel(m_dataChannelIndex);
        m_imageSizeX = m_dataGrid.getWidth();
        m_imageSizeY = m_dataGrid.getHeight();
        m_imageSizeX1 = m_imageSizeX - 1;
        m_imageSizeY1 = m_imageSizeY - 1;
        m_xfactor = m_imageSizeX / m_sizeX;
        m_yfactor = m_imageSizeY / m_sizeY;

        m_gradXfactor = m_tilesX * m_imageThickness * m_xfactor / 2;
        m_gradYfactor = m_tilesY * m_imageThickness * m_yfactor / 2;

        if (DEBUG) printf("Image3D.  size: %d %d  xfac: %f yfac: %f\n",m_imageSizeX,m_imageSizeY,m_xfactor,m_yfactor);


        return ResultCodes.RESULT_OK;
    }

    private Grid2D prepareImage() {

        long t0 = 0;
        Object obj = mp_source.getValue();

        if (DEBUG) printf("Image3D.prepareImage(%s)\n", obj);
        if (DEBUG) t0 = time();

        if (obj == null || !(obj instanceof Grid2DProducer))
            throw new RuntimeException(fmt("unknown grid source: %s expecting Grid2DProducer, found %s", obj, obj.getClass().getName()));

        Grid2DProducer producer = (Grid2DProducer) obj;

        Grid2D grid = producer.getGrid2D();

        if (mp_useImageProcessing.getValue()) {
            // need copy grid to preserve the original for future use 
            grid = Copy.createCopy(grid);
            grid.setGridBounds(getBounds());
            grid = executeOps(grid, createOps(grid));
        }
        if (DEBUG) {
            printf("Image3D.prepareImage() grid:%s [%d x %d] ready %d ms\n", grid, grid.getWidth(), grid.getHeight(), (time() - t0));

        }
        return grid;

    }

    /**
     * @Override
     */
    public Bounds getBounds() {
        Vector3d size = mp_size.getValue();
        Vector3d center = mp_center.getValue();
        return new Bounds(center.x - size.x / 2, center.x + size.x / 2, center.y - size.y / 2, center.y + size.y / 2, center.z - size.z / 2, center.z + size.z / 2);
    }


    /**
     * makes sequence of operations to apply to the image
     */
    private Vector<Operation2D> createOps(Grid2D grid) {

        Vector<Operation2D> ops = new Vector<Operation2D>(5);

        double voxelSize = mp_voxelSize.getValue();
        if (voxelSize / grid.getVoxelSize() > mp_pixelsPerVoxel.getValue()) {

            double newPixelSize = voxelSize / mp_pixelsPerVoxel.getValue();
            double sizeX = mp_size.getValue().x;
            double tilesX = mp_tilesX.getValue();
            int newWidth = (int) Math.ceil((sizeX / tilesX) / newPixelSize);
            int newHeight = (grid.getHeight() * newWidth) / grid.getWidth();
            ops.add(new ResampleOp(newWidth, newHeight, ResampleOp.WEIGHTING_MINIMUM));
        }
        if (mp_imageType.getValue() == IMAGE_TYPE_EMBOSSED) {
            // invert the image 
            ops.add(new GridValueTransformer(new DensityInvertor()));
        }
        double blurWidth = mp_blurWidth.getValue();
        if (blurWidth > 0.) {
            ops.add(new GaussianBlur(blurWidth));
        }

        if (!mp_useGrayscale.getValue()) {
            // do distance transform 
            double maxDist = mp_maxDist.getValue();
            ops.add(new DistanceTransform2DOp(maxDist, maxDist, m_imageThreshold));
        }

        return ops;
    }


    /**
     * executes sequence of operations
     */
    private Grid2D executeOps(Grid2D grid, Vector<Operation2D> ops) {

        for (int i = 0; i < ops.size(); i++) {
            grid = ops.get(i).execute(grid);
        }
        return grid;
    }


    /**
     original outdated procedure
     */
    /*
    private Grid2D prepareImage_v0(){

        if(DEBUG)printf("Image3D.prepareImage();\n");

        long t0 = time();
        
        BufferedImage image = null;

        Object oimage = null;//mp_image.getValue();
        //printf("Image3D.  buff_image: %s\n",image);

        if (oimage == null) {
            throw new RuntimeException(fmt("bad image: %s", oimage));
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
                throw new RuntimeException(fmt("error readng image: %s", oimage));
            }

        } else if (oimage instanceof BufferedImage) {
            image = (BufferedImage) oimage;
        } else if (oimage instanceof ImageWrapper) {
            image = ((ImageWrapper)oimage).getImage();
        } else if (oimage instanceof Grid2D) {
            Grid2D grid = (Grid2D)oimage;
            if(grid.getDataDesc().isDistanceData(m_dataChannelIndex)){
                prepareDataFromDistance(grid);
                return m_dataGrid;
            } else {
                image = Grid2DShort.convertGridToImage(grid);
            }
        } else if (oimage instanceof Text2D) {
            image = ((Text2D)oimage).getImage();
        } else {
            throw new IllegalArgumentException("Unhandled image type: " + oimage.getClass());
        }

        if (image == null) {
            throw new RuntimeException(fmt("Image is null.  source: %s  class: %s\n",oimage,oimage.getClass()));
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

            if (pixelsPerVoxel > mp_pixelsPerVoxel.getValue()) {

                double newPixelSize = voxelSize / mp_pixelsPerVoxel.getValue();
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

        return m_dataGrid;

    }
    */
    /**
     we already have distance grid
     will use it directly
     */
    /*
    protected int prepareDataFromDistance(Grid2D distGrid){

        m_imageSizeX = distGrid.getWidth();
        m_imageSizeY = distGrid.getHeight();
        m_imageSizeX1 = m_imageSizeX - 1;
        m_imageSizeY1 = m_imageSizeY - 1;
        
        m_dataGrid = distGrid;
        m_dataChannel = m_dataGrid.getDataDesc().getChannel(m_dataChannelIndex);
        return 0;
    }
    */

    /**
     makes data for black and white image
     data is represented as distance from 2D outline of the image
     @noRefGuide
     */
    /*
    protected int makeImageBlack(ImageGray16 image){

        long t0 = time();

        int nx = image.getWidth();
        int ny = image.getHeight();
        double imagePixelSize = ((Vector3d)mp_size.getValue()).x/nx;
        if(DEBUG)printf("makeImageBlack()  threshold: %f  pixelSize: %f\n",m_imageThreshold,imagePixelSize);

        Grid2DShort imageGrid = Grid2DShort.convertImageToGrid(image, (mp_imageType.getValue() == IMAGE_TYPE_EMBOSSED), imagePixelSize);

        double maxDist = mp_maxDist.getValue();

        DistanceTransform2DOp dt = new DistanceTransform2DOp(maxDist, maxDist, m_imageThreshold);
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
    */
    /**
     * @noRefGuide
     */
    /*
    protected int makeImageGray(ImageGray16 image){

        long t0 = time();
        if(DEBUG)printf("makeImageGray()\n");

        
        double imagePixelSize = ((Vector3d)mp_size.getValue()).x/image.getWidth();
        m_dataGrid = Grid2DShort.convertImageToGrid(image, (mp_imageType.getValue() == IMAGE_TYPE_EMBOSSED), imagePixelSize);
        m_dataChannel = m_dataGrid.getDataDesc().getChannel(0);
            
        if(DEBUG)printf("makeImageGray() done %d ms\n", time() -t0);

        return ResultCodes.RESULT_OK;
                
    }
    */

    /**
     * calculates value of shape distance or density
     * the
     *
     * @return ResultCodes.RESULT_OK
     * @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec data) {

        double dist = getDistanceValue(pnt);
        data.v[0] = getShapeValue(dist, pnt);

        return ResultCodes.RESULT_OK;

    }

    /**
     * @return distance to the Image3D
     */
    public double getDistanceValue(Vec pnt) {
        if (m_useGrayscale)
            return getDistanceGray(pnt);
        else
            return getDistanceBW(pnt);
    }


    /**
     * returns distance to gray image 3D
     */
    public double getDistanceGray(Vec pnt) {

        final boolean PRINT_VALUES = false;

        // original coord 
        double
            x0 = pnt.v[0],
            y0 = pnt.v[1],
            z0 = pnt.v[2];
        if (PRINT_VALUES) printf("pnt: (%7.5f %7.5f %7.5f)\n", x0, y0, z0);
        double x, y, z; // coord in image units
        x = x0 - m_xmin;
        y = y0 - m_ymin;
        z = z0 - m_zmin;
        // transform coord treat all 3 cases in uniform way


        switch (m_imagePlace) {
            default:
            case IMAGE_PLACE_TOP:
                // canonic orientation
                break;
            case IMAGE_PLACE_BOTTOM:
                // flip z
                z = m_sizeZ - z;
                break;
            case IMAGE_PLACE_BOTH: // both sides
                // fold and scale z
                z = abs(2 * z - m_sizeZ);
                break;
        }

        x *= m_xfactor;
        y *= m_yfactor;

        if (PRINT_VALUES) printf("pnt_grid: (%7.5f %7.5f %7.5f)\n", x, y, z);

        x = clamp(x, 0., (double) m_imageSizeX);
        y = clamp(y, 0., (double) m_imageSizeY);
        // x, y are in [0, 0, m_imageSizeX, mageHeight] range

        if (m_tilesX > 1) x = fmod(x * m_tilesX, (double) m_imageSizeX);
        if (m_tilesY > 1) y = fmod(y * m_tilesY, (double) m_imageSizeY);
        int ix = (int) x;
        int iy = (int) y;
        double
            dx = x - ix,
            dy = y - iy;

        ix = clamp(ix, 0, m_imageSizeX1);
        iy = clamp(iy, 0, m_imageSizeY1);
        int ix1 = ix + 1;
        int iy1 = iy + 1;
        ix1 = clamp(ix1, 0, m_imageSizeX1);
        iy1 = clamp(iy1, 0, m_imageSizeY1);
        int ix_1 = clamp(ix - 1, 0, m_imageSizeX1);
        int iy_1 = clamp(iy - 1, 0, m_imageSizeY1);
        int ix2 = clamp(ix + 2, 0, m_imageSizeX1);
        int iy2 = clamp(iy + 2, 0, m_imageSizeY1);

        double v00 = getImageValue(ix, iy);
        double v10 = getImageValue(ix1, iy);
        double v01 = getImageValue(ix, iy1);
        double v11 = getImageValue(ix1, iy1);
        // interpolated image value 
        double iValue = lerp2(v00, v10, v01, v11, dx, dy);
        // iValue is in range (0,1)

        if (PRINT_VALUES) printf("iValue: %7.5f\n", iValue);
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

        double gradX = lerp2(gx00, gx10, gx01, gx11, dx, dy) * m_gradXfactor;
        double gradY = lerp2(gy00, gy10, gy01, gy11, dx, dy) * m_gradYfactor;
        double slopeFactor = 1 / sqrt(m_slopeZcoeff + gradX * gradX + gradY * gradY);
        double thresholdFactor = m_imageThickness / max(MIN_GRADIENT, sqrt(gradX * gradX + gradY * gradY));
        // distance in xy pane to image threshold 
        double thresholdDist = -(iValue - m_baseRelThreshold) * thresholdFactor;

        // image value in physical units
        iValue = iValue * m_imageThickness + m_baseThickness;

        double dist = slopeFactor * (z - iValue); // distance to surface

        if (m_imagePlace != IMAGE_PLACE_BOTH)
            dist = max(dist, m_baseHalfSizeZ - z);

        if (PRINT_VALUES) printf("grad: %7.5f %7.5f final hfDist: %7.5f\n", gradX, gradY, dist);

        dist = blendMax(dist, thresholdDist, m_rounding);
        double distBoxXY = blendMax(abs(x0 - m_centerX) - m_halfSizeX, abs(y0 - m_centerY) - m_halfSizeY, m_rounding);
        dist = blendMax(dist, distBoxXY, m_rounding);
        //add base      
        if (m_hasBase) {
            double distBase = blendMax(distBoxXY, abs(z0 - m_baseCenterZ) - m_baseHalfSizeZ, m_rounding);
            dist = blendMin(dist, distBase, m_rounding);
        }
        return dist;
    }


    /**
     * returns distance to BW image
     */
    public double getDistanceBW(Vec pnt) {

        final boolean PRINT_VALUES = false;
        double
            x0 = pnt.v[0],
            y0 = pnt.v[1],
            z0 = pnt.v[2];
        if (PRINT_VALUES) printf("pnt: (%7.5f %7.5f %7.5f)\n", x0, y0, z0);
        double x, y, z; // coord in image units
        x = x0 - m_xmin;
        y = y0 - m_ymin;
        z = z0 - m_zmin;
        x *= m_xfactor;
        y *= m_yfactor;

        if (PRINT_VALUES) printf("pnt_grid: (%7.5f %7.5f %7.5f)\n", x, y, z);

        x = clamp(x, 0., (double) m_imageSizeX);
        y = clamp(y, 0., (double) m_imageSizeY);
        // x, y are in [0, 0, m_imageSizeX, mageHeight] range

        if (m_tilesX > 1) x = fmod(x * m_tilesX, (double) m_imageSizeX);
        if (m_tilesY > 1) y = fmod(y * m_tilesY, (double) m_imageSizeY);
        int ix = (int) x;
        int iy = (int) y;
        double
            dx = x - ix,
            dy = y - iy;

        ix = clamp(ix, 0, m_imageSizeX1);
        iy = clamp(iy, 0, m_imageSizeY1);
        int ix1 = ix + 1;
        int iy1 = iy + 1;
        ix1 = clamp(ix1, 0, m_imageSizeX1);
        iy1 = clamp(iy1, 0, m_imageSizeY1);

        double v00 = getImageValue(ix, iy);
        double v10 = getImageValue(ix1, iy);
        double v01 = getImageValue(ix, iy1);
        double v11 = getImageValue(ix1, iy1);

        // image is precalculated to return normalized value of distance 
        // tiling distort the distance 
        double iValue = 1 / (0.5 * (m_tilesX + m_tilesY)) * lerp2(v00, v10, v01, v11, dx, dy);

        double dist = iValue;
        // extrapolate distance outside of image box 
        double ddx = (x0 - (x / m_xfactor + m_xmin));
        double ddy = (y0 - (y / m_yfactor + m_ymin));
        dist += sqrt(ddx * ddx + ddy * ddy);
        dist = blendMax(dist, z - m_sizeZ, m_rounding); // top crop 
        dist = blendMax(dist, -z, m_rounding); // bottom crop 

        // intersect with YX box 
        double dBoxXY = blendMax(abs(x0 - m_centerX) - m_halfSizeX, abs(y0 - m_centerY) - m_halfSizeY, m_rounding);
        dist = blendMax(dist, dBoxXY, m_rounding);

        if (m_hasBase) {
            double dBase = blendMax(dBoxXY, abs(z0 - m_baseCenterZ) - m_baseHalfSizeZ, m_rounding);
            dist = blendMin(dist, dBase, m_rounding);
        }

        return dist;
    }

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

    //
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


    static class DensityInvertor extends BaseParameterizable implements DataSource {

        public int getDataValue(Vec pnt, Vec dataValue) {
            dataValue.v[0] = 1. - pnt.v[0];
            return ResultCodes.RESULT_OK;
        }

        public int getChannelsCount() {
            return 1;
        }

        public Bounds getBounds(){
            return null;
        }
    }


}  // class Image3D
