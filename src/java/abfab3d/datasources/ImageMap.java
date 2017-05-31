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


import javax.vecmath.Vector3d;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.Vector;


import abfab3d.core.Bounds;
import abfab3d.core.ResultCodes;
import abfab3d.core.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.core.GridDataChannel;
import abfab3d.core.Grid2DProducer;
import abfab3d.core.ImageProducer;

import abfab3d.grid.Operation2D;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.Parameter;
import abfab3d.param.ParamCache;

import abfab3d.grid.op.ImageReader;
import abfab3d.grid.op.ImageToGrid2D;
import abfab3d.grid.op.GaussianBlur;
import abfab3d.grid.op.Copy;

import abfab3d.util.ColorMapperDistance;

import abfab3d.core.Vec;
import abfab3d.util.ImageGray16;


import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;

import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step01;
import static abfab3d.core.MathUtil.step10;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;


/**
 * <p>
 * DataSource which fills 3D box with data from from 2D image in xy plane.
 * </p><p>
 * ImageMap may have multiple channels, according to the source image type 
 * </p><p>
 * The 2D image is placed in the XY-plane and for each pixel of the image with coordinate (x,y) the column of voxel of size size.z
 * is formed in both sides of XY plane
 * </p>
 * The image can be periodically repeated in both X and Y directions
 * 
 *
 * @author Vladimir Bulatov
 */
public class ImageMap extends TransformableDataSource {
    final static boolean DEBUG = false;
    final static boolean DEBUG_VIZ = false;
    final static boolean CACHING_ENABLED = true;

    public static int REPEAT_NONE = 0, REPEAT_X = 1, REPEAT_Y = 2, REPEAT_BOTH = 3;
    
    public static final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1, INTERPOLATION_MIPMAP = 2;
    public static final double DEFAULT_VOXEL_SIZE = 0.1*MM;

    private boolean m_repeatX = false;
    private boolean m_repeatY = false;
    private int m_interpolation = INTERPOLATION_BOX; 
    private double 
        m_originX,
        m_originY,
        m_originZ,
        m_sizeX,
        m_sizeY,
        m_sizeZ;
    private int m_imageSizeX, m_imageSizeY; 
    private double m_valueOffset, m_valueFactor;

    SNodeParameter mp_source = new SNodeParameter("source","Image source", null);
    // public parameters 
    //ObjectParameter  mp_imageSource = new ObjectParameter("image","image source",null);
    Vector3dParameter  mp_center = new Vector3dParameter("center","center of the image box",new Vector3d(0.,0.,0.));
    Vector3dParameter  mp_size = new Vector3dParameter("size","size of the image box",new Vector3d(0.1,0.1,0.1));
    BooleanParameter  mp_repeatX = new BooleanParameter("repeatX","repeat image along X", false);
    BooleanParameter  mp_repeatY = new BooleanParameter("repeatY","repeat image along Y", false);
    DoubleParameter  mp_whiteDisp = new DoubleParameter("whiteDisplacement","displacement for white level", 0*MM);
    DoubleParameter  mp_blackDisp = new DoubleParameter("blackDisplacement","displacement for black level", 1*MM);
    DoubleParameter  mp_blurWidth = new DoubleParameter("blurWidth", "width of gaussian blur on the image", 0.);

    private final Parameter m_aparams[] = new Parameter[]{
        mp_source,
        mp_center,
        mp_size,
        mp_repeatX,
        mp_repeatY,
        mp_whiteDisp,
        mp_blackDisp,
        mp_blurWidth,

    };

    /** Params which require changes in the underlying image */
    private final Parameter[] m_imageParams = new Parameter[] {
        mp_source,
        mp_blurWidth
    };

    // 
    private Grid2D m_imageGrid;
    // converted to get physical value from grid attribute
    protected GridDataChannel m_dataChannel;


    /**
     * Creates ImageMap from a file
     *
     * @param path source of the image. Can be url
     * @param sizex - width of the image
     * @param sizey - height of the image
     * @param sizez - depth of the image
     */
    public ImageMap(String path, double sizex, double sizey, double sizez) {
        this(new ImageToGrid2D(new ImageReader(path)), sizex, sizey, sizez);
    }

    /**
     * Creates ImageMap from a file
     *
     * @param grid source of the image. 
     * @param sizex - width of the image
     * @param sizey - height of the image
     * @param sizez - depth of the image
     */
    public ImageMap(Grid2D grid, double sizex, double sizey, double sizez) {
        // use memory has as grid label 
        this((Grid2DProducer)(new Grid2DSourceWrapper(grid.toString(),grid)), sizex, sizey, sizez);
    }


    /**
     * Creates ImageMap from a file
     *
     * @param imageProducer source of the image.
     * @param sizex - width of the image
     * @param sizey - height of the image
     * @param sizez - depth of the image
     */
    public ImageMap(ImageProducer imageProducer, double sizex, double sizey, double sizez) {
        this(new ImageToGrid2D(imageProducer), sizex, sizey, sizez);
    }

    /**
     * Creates ImageMap from a file
     *
     * @param producer source of the image. 
     * @param sizex - width of the image
     * @param sizey - height of the image
     * @param sizez - depth of the image
     */
    public ImageMap(Grid2DProducer producer, double sizex, double sizey, double sizez) {
        initParams();
        if(DEBUG)printf("ImageMap(%s, %7.5f, %7.5f, %7.5f )\n", producer, sizex, sizey, sizez);
        
        mp_source.setValue(producer);
        mp_size.setValue(new Vector3d(sizex, sizey, sizez));

    }

    /**
     * @noRefGuide
     */
    protected void initParams(){
        super.addParams(m_aparams);
    }


    /**
     * Set the source image
     * @param producer
     */
    public void setImage(Grid2DProducer producer) {
        mp_source.setValue(producer);
    }

    /**
     * Get the source image
     * @return
     */
    public Grid2DProducer getImage() {
        return (Grid2DProducer)mp_source.getValue();
    }

    /**
     * Set center of the image box
     * @param val The center in meters
     */
    public void setCenter(Vector3d val) {
        mp_center.setValue(val);
    }

    /**
     * Get the center of the image box
     */
    public Vector3d getCenter() {
        return mp_center.getValue();
    }

    /**
     * Set size of the image box
     * @param val The size in meters
     */
    public void setSize(Vector3d val) {
        mp_size.setValue(val);
    }

    /**
     * Get the size of the image box
     */
    public Vector3d getSize() {
        return mp_size.getValue();
    }

    /**
     * Set whether the image repeats in the X direction
     * @param val The value
     */
    public void setRepeatX(boolean val) {
        mp_repeatX.setValue(val);
    }

    /**
     * Is repeatX set
     */
    public boolean isRepeatX() {
        return mp_repeatX.getValue();
    }

    /**
     * Set whether the image repeats in the Y direction
     * @param val The value
     */
    public void setRepeatY(boolean val) {
        mp_repeatY.setValue(val);
    }

    /**
     * Is repeatY set
     */
    public boolean isRepeatY() {
        return mp_repeatY.getValue();
    }

    /**
     * Set how far white pixels displace the image
     * @param val The value in meters. Default is 0.
     */
    public void setWhiteDisplacement(double val) {
        mp_whiteDisp.setValue(val);
    }

    /**
     * Get the white displacement
     */
    public double getWhiteDisplacement() {
        return mp_whiteDisp.getValue();
    }

    /**
     * Set how far black pixels displace the image
     * @param val The value in meters. Default is 0.001.
     */
    public void setBlackDisplacement(double val) {
        mp_blackDisp.setValue(val);
    }

    /**
     * Get the black displacement
     * @return
     */
    public double getBlackDisplacement() {
        return mp_blackDisp.getValue();
    }

    /**
     * Set the blurring width to apply to the image
     * @param val The width in meters.  Default is 0.
     */
    public void setBlurWidth(double val) {
        mp_blurWidth.setValue(val);
    }

    /**
     * Get the blurWidth
     */
    public double getBlurWidth() {
        return mp_blurWidth.getValue();
    }

    /**
     * @noRefGuide
     */
    public int getBitmapWidth(){
        return m_imageGrid.getWidth();
    }

    /**
     * @noRefGuide
     */
    public int getBitmapHeight(){
        return m_imageGrid.getHeight();
    }

    /**
     * Get a label for the OpenCL buffer, account for all params which change the buffer value
     * @return
     */
/*
    public String getBufferLabel() {
        return BaseParameterizable.getParamString(getClass().getSimpleName(), m_imageParams);
    }
*/
    /**
     * @noRefGuide
     */
    public void getBitmapDataUByte(byte data[]){

        int nx = m_imageGrid.getWidth();
        int ny = m_imageGrid.getHeight();
        for(int y = 0;  y < ny; y++){
            for(int x = 0;  x < nx; x++){
                
                double d = m_dataChannel.getValue(m_imageGrid.getAttribute(x, y));
                // d in (0,1) 
                // normalization to byte 
                data[x + y * nx] = (byte)((int)(d * 255.) & 0xFF); 
            }
        }
    }

    // store bitmap data as 16 bit shorts
    /**
     * @noRefGuide
     */
    public void getBitmapDataUShort(byte data[]){

        int nx = m_imageGrid.getWidth();
        int ny = m_imageGrid.getHeight();
        int nx2 = nx*2;

        for(int y = 0;  y < ny; y++){
            for(int x = 0;  x < nx; x++){

                double d = m_dataChannel.getValue(m_imageGrid.getAttribute(x, y));

                // normalization to short
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

        Vector3d center = (Vector3d)mp_center.getValue();
        Vector3d size = (Vector3d)mp_size.getValue();
        m_originX = center.x - size.x/2;
        m_originY = center.y - size.y/2;
        m_originZ = center.z - size.z/2;
        m_sizeX = size.x;
        m_sizeY = size.y;
        m_sizeZ = size.z;

        m_repeatX = mp_repeatX.getValue();
        m_repeatY = mp_repeatY.getValue();

        double white = mp_whiteDisp.getValue();
        double black = mp_blackDisp.getValue();

        m_valueOffset = black;
        m_valueFactor = white - black;

        long t0 = System.currentTimeMillis();

        String label = getParamString(getClass().getSimpleName(), m_imageParams);

        Object co = null;
        if(CACHING_ENABLED)co = ParamCache.getInstance().get(label);
        if (co == null) {
            m_imageGrid = prepareImage();
            if(CACHING_ENABLED)ParamCache.getInstance().put(label, m_imageGrid);
            if (DEBUG) printf("ImageMap: caching image: %s -> %s\n",label, m_imageGrid);
        } else {
            m_imageGrid = (Grid2D) co;
            if (DEBUG) printf("ImageMap got cached image %s -> %s\n",label, m_imageGrid);
        }


        m_dataChannel = m_imageGrid.getDataDesc().getDefaultChannel();
        m_imageSizeX  = m_imageGrid.getWidth();
        m_imageSizeY  = m_imageGrid.getHeight();

        return ResultCodes.RESULT_OK;
        
    }
        
    private Grid2D prepareImage(){
        
        Object obj = mp_source.getValue(); 
        if(DEBUG) printf("prepareImage_v1(%s)\n", obj);
        if(obj == null || !(obj instanceof Grid2DProducer))
            throw new RuntimeException(fmt("unrecoginized grid source: %s\n",obj));

        Grid2DProducer producer = (Grid2DProducer)obj; 
        
        Grid2D grid = producer.getGrid2D(); 
        if(DEBUG) printf("ImageMap grid: %s\n", grid);
        grid = Copy.createCopy(grid);
        grid.setGridBounds(getBounds());

        
        grid = executeOps(grid, createOps(grid));

        return grid;
        
    }

    /**
       @Override 
    */
    public Bounds getBounds(){
        Vector3d size = mp_size.getValue();
        Vector3d center = mp_center.getValue();
        return new Bounds(center.x - size.x/2,center.x + size.x/2,center.y - size.y/2,center.y + size.y/2,center.z - size.z/2,center.z + size.z/2);
    }

    /**
       makes sequence of operations to apply to the image 
    */
    private Vector<Operation2D> createOps(Grid2D grid){
        
        Vector<Operation2D> ops = new Vector<Operation2D>(5);
        
        double blurWidth = mp_blurWidth.getValue();
        if(blurWidth > 0.){
            ops.add(new  GaussianBlur(blurWidth));            
        }
        
        return ops;
    }

    
    /**
       exacutes sequence of opeations 
     */
    private Grid2D executeOps(Grid2D grid, Vector<Operation2D> ops){
        
        for(int i = 0; i < ops.size(); i++){
            grid = ops.get(i).execute(grid);
        }
        return grid;
    }

    
    /**
     * @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec dataValue){

        double x = pnt.v[0];
        double y = pnt.v[1];
        double z = pnt.v[2];

        x -= m_originX;
        y -= m_originY;
        z -= m_originZ;

        // xy coordinates are normalized to the image size
        x /= m_sizeX;
        y /= m_sizeY;

        // xy are in (0,1) range 
        if(m_repeatX) 
            x -= floor(x);
        if(m_repeatY) 
            y -= floor(y);
       
        // x in [0, imageSizeX]
        // y in [0, imageSizeY]
        x *= m_imageSizeX;
        y *= m_imageSizeY;

        // half pixel shift 
        x -= 0.5;
        y -= 0.5;
        
        int ix = (int)floor(x);
        int iy = (int)floor(y);
        int ix1 = ix + 1;
        int iy1 = iy + 1;
        double dx = x - ix;
        double dy = y - iy;
        if(m_repeatX){
            ix -= m_imageSizeX*floor((double)ix/m_imageSizeX);
            ix1 -= m_imageSizeX*floor((double)ix1/m_imageSizeX);
        } else {
            ix = clamp(ix, 0, m_imageSizeX-1);            
            ix1 = clamp(ix1, 0, m_imageSizeX-1);            
        }
        if(m_repeatY){
            iy -= m_imageSizeY*floor((double)iy/m_imageSizeY);
            iy1 -= m_imageSizeY*floor((double)iy1/m_imageSizeY);
        } else {
            iy = clamp(iy, 0, m_imageSizeY-1);            
            iy1 = clamp(iy1, 0, m_imageSizeY-1);            
        }

        double 
            v00 = m_dataChannel.getValue(m_imageGrid.getAttribute(ix, iy)),
            v10 = m_dataChannel.getValue(m_imageGrid.getAttribute(ix1, iy)),
            v01 = m_dataChannel.getValue(m_imageGrid.getAttribute(ix, iy1)),
            v11 = m_dataChannel.getValue(m_imageGrid.getAttribute(ix1, iy1));
        double 
            dx1 = 1.- dx,
            dy1 = 1.- dy;


        double v = 
            dx * dy * v11 + dx1 * dy * v01 + dx * dy1 * v10 + dx1 * dy1 * v00;

        dataValue.v[0] = v*m_valueFactor + m_valueOffset;

        return ResultCodes.RESULT_OK;
    }
    
}