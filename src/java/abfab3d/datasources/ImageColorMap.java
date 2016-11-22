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


import abfab3d.core.ResultCodes;
import abfab3d.param.*;
import abfab3d.util.ImageColor;
import abfab3d.core.Vec;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
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
    ObjectParameter mp_imageSource = new ObjectParameter("image", "image source", null);
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
    private ImageColor m_imageData;

    /**
     * Creates ImageColorMap from a file
     *
     * @param imageSource source of the image. Can be url, BufferedImage or ImageWrapper
     * @param sizex - width of the image
     * @param sizey - height of the image
     * @param sizez - depth of the image
     */
    public ImageColorMap(Object imageSource, double sizex, double sizey, double sizez) {

        super.addParams(m_aparams);

        mp_imageSource.setValue(imageSource);
        mp_size.setValue(new Vector3d(sizex, sizey, sizez));
    }


    /**
     * Set the source image
     * @param val
     */
    public void setImage(Object val) {
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
                int d = m_imageData.getDataI(x, y);
                data[x + y * nx] = d;
            }
        }
    }

    /**
     * @noRefGuide
     */
    public int initialize() {
        super.initialize();

        // this may be different depending on the image 
        m_channelsCount = 3;

        String vhash = getParamString(imageParams);

        Object co = ParamCache.getInstance().get(vhash);
        if (co == null) {
            long t0 = time();
            int res = prepareImage();

            printf("sizeX: %f\n", m_sizeX);
            printf("sizeY: %f\n", m_sizeY);
            printf("sizeZ: %f\n", m_sizeZ);
            printf("originX: %f\n", m_originX);
            printf("originY: %f\n", m_originY);
            printf("originZ: %f\n", m_originZ);
            printf("imageSizeX: %d\n", m_imageSizeX);
            printf("imageSizeY: %d\n", m_imageSizeY);

            printf("ImageColorMap.prepareImage() time: %d ms\n", (time() - t0));

            if(res != ResultCodes.RESULT_OK){
                // something wrong with the image
                throw new IllegalArgumentException("undefined image");
            }

            ParamCache.getInstance().put(vhash, m_imageData);

        } else {
            m_imageData = (ImageColor) co;
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

        return ResultCodes.RESULT_OK;

    }

    /**
     * @noRefGuide
     */
    private int prepareImage() {

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

    /**
     * Get a label for the OpenCL buffer, account for all params which change the buffer value
     * @return
     */
    public String getBufferLabel() {
        return BaseParameterizable.getParamString(getClass().getSimpleName(), m_aparams);
    }

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

        double
                dx1 = 1. - dx,
                dy1 = 1. - dy,
                dxdy = dx * dy,
                dx1dy = dx1 * dy,
                dxdy1 = dx * dy1,
                dx1dy1 = dx1 * dy1;

        int
                v00 = m_imageData.getDataI(ix, iy),
                v10 = m_imageData.getDataI(ix1, iy),
                v01 = m_imageData.getDataI(ix, iy1),
                v11 = m_imageData.getDataI(ix1, iy1);

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
                b11 = getBlue(v11);


        double r = (dxdy * r11 + dx1dy * r01 + dxdy1 * r10 + dx1dy1 * r00) / 255.;
        double g = (dxdy * g11 + dx1dy * g01 + dxdy1 * g10 + dx1dy1 * g00) / 255.;
        double b = (dxdy * b11 + dx1dy * b01 + dxdy1 * b10 + dx1dy1 * b00) / 255.;

        dataValue.v[0] = r;
        dataValue.v[1] = g;
        dataValue.v[2] = b;

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

}