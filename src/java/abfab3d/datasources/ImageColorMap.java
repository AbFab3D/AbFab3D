/*****************************************************************************
 * Shapeways, Inc Copyright (c) 20116
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.datasources;


import abfab3d.core.Bounds;
import abfab3d.core.ResultCodes;
import abfab3d.core.Grid2D;
import abfab3d.core.Vec;
import abfab3d.core.Grid2DProducer;
import abfab3d.core.ImageProducer;


import abfab3d.param.BaseParameterizable;
import abfab3d.param.ParamCache;
import abfab3d.param.Parameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.SNodeParameter;

import abfab3d.grid.op.ImageToGrid2D;
import abfab3d.grid.op.ImageLoader;

import abfab3d.util.ImageColor;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.MathUtil.clamp;
import static java.lang.Math.floor;


/**
 * <p>
 * DataSource which fills 3D box with color data from a 2D image in xy plane.
 * </p><p>
 * ImageColorMap may have multiple channels, according to the source image type 
 * </p><p>
 * The 2D image is placed in the XY-plane and for each pixel of the image with coordinate (x,y) the column of voxel of size size.z
* is formed in both sides of XY plane
 * </p>
 * The image can be periodically repeated in X,Y and Z directions.
 *
 *
 * @author Vladimir Bulatov
 */
public class ImageColorMap extends TransformableDataSource {

    final static boolean DEBUG = false;

    private boolean m_repeatX = false;
    private boolean m_repeatY = false;
    private double
            m_originX,
            m_originY,
            m_originZ,
            m_sizeX,
            m_sizeY,
            m_sizeZ;
    private int m_imageSizeX, m_imageSizeY;

    // public parameters 
    SNodeParameter mp_imageSource = new SNodeParameter("image", "image source", null);
    //ObjectParameter mp_imageSource = new ObjectParameter("image", "image source", null);
    Vector3dParameter mp_center = new Vector3dParameter("center", "center of the image box", new Vector3d(0., 0., 0.));
    Vector3dParameter mp_size = new Vector3dParameter("size", "size of the image box", new Vector3d(0.1, 0.1, 0.1));
    BooleanParameter mp_repeatX = new BooleanParameter("repeatX", "repeat image along X", false);
    BooleanParameter mp_repeatY = new BooleanParameter("repeatY", "repeat image along Y", false);
    BooleanParameter mp_repeatZ = new BooleanParameter("repeatZ", "repeat image along Z", false);

    Parameter m_aparams[] = new Parameter[]{
            mp_imageSource,
            mp_center,
            mp_size,
            mp_repeatX,
            mp_repeatY,
            mp_repeatZ,
    };

    /** Params which require changes in the underlying image */
    private Parameter[] imageParams = new Parameter[] {
            mp_imageSource
    };

    // 
    private Grid2D m_imageData;

    /**
     * Creates ImageColorMap from a file
     *
     * @param path source of the image. Can be url, BufferedImage or ImageWrapper
     * @param sizex - width of the image
     * @param sizey - height of the image
     * @param sizez - depth of the image
     */
    public ImageColorMap(String path, double sizex, double sizey, double sizez) {
        this(new ImageToGrid2D(new ImageLoader(path), true), sizex, sizey, sizez);
    }

    public ImageColorMap(ImageProducer producer, double sizex, double sizey, double sizez) {
        this(new ImageToGrid2D(producer, true), sizex, sizey, sizez);
    }

    public ImageColorMap(Grid2DProducer gridProducer, double sizex, double sizey, double sizez) {

        super.addParams(m_aparams);
        mp_imageSource.setValue(gridProducer);
        mp_size.setValue(new Vector3d(sizex, sizey, sizez));
    }


    /**
     * Set the source image
     * @param val
     */
    public void setImage(Object val) {

        if (val instanceof String) {
            val = new ImageToGrid2D(new ImageLoader((String)val), true);
        } else if (val instanceof ImageProducer) {
            val = new ImageToGrid2D((ImageProducer) val, true);
        } else if (val instanceof Grid2DProducer) {
            // fine
        } else {
            throw new IllegalArgumentException("Unsupported object for ImageColorMap");
        }
        mp_imageSource.setValue(val);
    }

    /**
     * Get the source image
     * @return
     */
    public Object getImage() {
        return mp_imageSource.getValue();
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
    public boolean getRepeatX() {
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
    public boolean getRepeatY() {
        return mp_repeatY.getValue();
    }

    /**
     * @noRefGuide
     */
    public int getBitmapWidth() {
        return m_imageData.getWidth();
    }

    /**
     * @noRefGuide
     */
    public int getBitmapHeight() {
        return m_imageData.getHeight();
    }

    /**
     * @noRefGuide
     */
    public void getBitmapDataInt(int data[]) {

        int nx = m_imageData.getWidth();
        int ny = m_imageData.getHeight();
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                long d = m_imageData.getAttribute(x, y);
                data[x + y * nx] = (int)(d & 0xFFFFFFFF);
            }
        }
    }

    /**
     * Get a label for the OpenCL buffer, account for all params which change the buffer value
     * @return
     */
/*
    public String getBufferLabel() {
        return BaseParameterizable.getParamString(getClass().getSimpleName(), m_aparams);
    }
*/
    /**
     * @noRefGuide
     */
    public int initialize() {
        super.initialize();


        String label = getParamString(getClass().getSimpleName(), imageParams);

        Object co = ParamCache.getInstance().get(label);
        if (co == null) {
            long t0 = time();
            m_imageData = prepareImage();
            //if(DEBUG)printf("ImageColorMap.prepareImage() time: %d ms\n", (time() - t0));            
            if (DEBUG) printf("ImageColorMap: caching image: %s -> %s\n",label, m_imageData);
            if(m_imageData == null){
                // something wrong with the image
                throw new IllegalArgumentException("undefined image");
            }

            ParamCache.getInstance().put(label, m_imageData);

        } else {
            m_imageData = (Grid2D) co;
            if (DEBUG) printf("ImageColorMap got cached image %s -> %s\n",label, m_imageData);
        }


        Vector3d center = (Vector3d) mp_center.getValue();
        Vector3d size = (Vector3d) mp_size.getValue();
        m_originX = center.x - size.x / 2;
        m_originY = center.y - size.y / 2;
        m_originZ = center.z - size.z / 2;
        m_sizeX = size.x;
        m_sizeY = size.y;
        m_sizeZ = size.z;

        m_imageSizeX = m_imageData.getWidth();
        m_imageSizeY = m_imageData.getHeight();
        m_repeatX = mp_repeatX.getValue();
        m_repeatY = mp_repeatY.getValue();

        // this may be different depending on the image 
        // good for general ARGB 
        m_channelsCount = 4;

        return ResultCodes.RESULT_OK;

    }

    private Grid2D prepareImage() {

        Object obj = mp_imageSource.getValue(); 
        if(DEBUG) printf("ImageColorMap.prepareImage() source: %s\n", obj);
        if(obj == null || !(obj instanceof Grid2DProducer))
            throw new RuntimeException(fmt("unrecoginized grid source: %s\n",obj));
        
        
        Grid2DProducer producer = (Grid2DProducer)obj; 
        
        Grid2D grid = producer.getGrid2D(); 
        if(DEBUG) printf("ImageColorMap grid: %s\n", grid);
        // if we want image processing operations on the grid, we need to make a copy 
        grid.setGridBounds(getBounds());

        if(DEBUG) printf("ImageColorMap() image: [%d x %d]\n", grid.getWidth(), grid.getHeight());

        return grid;
    }
    
    
    @Override
    public Bounds getBounds(){

        Vector3d size = mp_size.getValue();
        Vector3d center = mp_center.getValue();
        return new Bounds(center.x - size.x/2,center.x + size.x/2,center.y - size.y/2,center.y + size.y/2,center.z - size.z/2,center.z + size.z/2);
    }

    /**
     * @noRefGuide
     */
    /*
    private int prepareImage_v0() {

        long t0 = time();

        printf("ImageColorMap.prepareImage()\n");

        Object imageSource = mp_imageSource.getValue();
        if (imageSource == null)
            throw new RuntimeException("imageSource is null");

        if (imageSource instanceof String) {

            try {
                m_imageData = new ImageColor(ImageIO.read(new File((String) imageSource)));
            } catch (IOException e) {
                printf("Can't find file: %s\n",imageSource);
                // empty 1x1 image 
                m_imageData = new ImageColor(1, 1);
                throw new RuntimeException(e);
            }

        } else if (imageSource instanceof Text2D) {

            m_imageData = new ImageColor(((Text2D) imageSource).getImage());
        } else if (imageSource instanceof FormattedText2D) {

            m_imageData = new ImageColor(((FormattedText2D) imageSource).getImage());

        } else if (imageSource instanceof BufferedImage) {

            m_imageData = new ImageColor((BufferedImage) imageSource);

        } else if (imageSource instanceof ImageWrapper) {

            m_imageData = new ImageColor(((ImageWrapper) imageSource).getImage());
        }

        if (m_imageData == null) {
            // Cast to String for now, not sure how to really handle this
            String file = imageSource.toString();
            printf("Converted to string: " + file);
            try {
                m_imageData = new ImageColor(ImageIO.read(new File(file)));
            } catch (IOException e) {
                // empty 1x1 image
                m_imageData = new ImageColor(1, 1);
                throw new RuntimeException(e);
            }
        }
        if (m_imageData == null) {
            m_imageData = new ImageColor(1, 1);
            throw new IllegalArgumentException("Unhandled imageSource: " + imageSource + " class: " + imageSource.getClass());
        }



        return ResultCodes.RESULT_OK;
    }
    */

    /**
     * @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec dataValue) {

        //TODO repeatX,Y,Z implementation 
        double x = pnt.v[0];
        double y = pnt.v[1];
        double z = pnt.v[2];

        //printf("[%3.1f %3.1f]", x, y);

        x -= m_originX;
        y -= m_originY;
        z -= m_originZ;

        //printf("[%3.1f %3.1f]", x, y);

        // xy coordinates are normalized to the box size
        x /= m_sizeX;
        y /= m_sizeY;

        // xy are in (0,1) range 
        if (m_repeatX) x -= floor(x);
        if (m_repeatY) y -= floor(y);

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

        /*
        //printf("[%3.1f %3.1f]", x, y);

        int ix = (int) floor(x);
        int iy = (int) floor(y);

        double dx = x - ix;
        double dy = y - iy;
        if (ix < 0) {
            if (m_repeatX) ix = m_imageSizeX - 1;
            else ix = 0;
        }
        if (iy < 0) {
            if (m_repeatY) iy = m_imageSizeY - 1;
            else iy = 0;
        }
        int ix1 = ix + 1;
        if (ix1 >= m_imageSizeX) {
            if (m_repeatX) ix = 0;
            else ix = m_imageSizeX - 1;
        }
        int iy1 = iy + 1;
        if (iy1 >= m_imageSizeY) {
            if (m_repeatY) iy = 0;
            else iy = m_imageSizeY - 1;
        }
        */

        double
            dx1 = 1. - dx,
            dy1 = 1. - dy,
            dxdy = dx * dy,
            dx1dy = dx1 * dy,
            dxdy1 = dx * dy1,
            dx1dy1 = dx1 * dy1;
        
        int
            v00 = getPixelData(ix, iy),
            v10 = getPixelData(ix1, iy),
            v01 = getPixelData(ix, iy1),
            v11 = getPixelData(ix1, iy1);
        
        int
            r00 = getRed(v00),
            r10 = getRed(v10),
            r01 = getRed(v01),
            r11 = getRed(v11),
            g00 = getGreen(v00),
            g10 = getGreen(v10),
            g01 = getGreen(v01),
            g11 = getGreen(v11),
            b00 = getBlue(v00),
            b10 = getBlue(v10),
            b01 = getBlue(v01),
            b11 = getBlue(v11),
            a00 = getAlpha(v00),
            a10 = getAlpha(v10),
            a01 = getAlpha(v01),
            a11 = getAlpha(v11);            
        

        double r = (dxdy * r11 + dx1dy * r01 + dxdy1 * r10 + dx1dy1 * r00) / 255.;
        double g = (dxdy * g11 + dx1dy * g01 + dxdy1 * g10 + dx1dy1 * g00) / 255.;
        double b = (dxdy * b11 + dx1dy * b01 + dxdy1 * b10 + dx1dy1 * b00) / 255.;
        double a = (dxdy * a11 + dx1dy * a01 + dxdy1 * a10 + dx1dy1 * a00) / 255.;

        dataValue.v[0] = r;
        dataValue.v[1] = g;
        dataValue.v[2] = b;
        dataValue.v[3] = a;

        return ResultCodes.RESULT_OK;
    }

    final static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    final static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    final static int getBlue(int color) {
        return (color) & 0xFF;
    }

    final static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    final int getPixelData(int x, int y) {
        int ny = m_imageData.getHeight();

        return (int)m_imageData.getAttribute(x, y);

    }
}