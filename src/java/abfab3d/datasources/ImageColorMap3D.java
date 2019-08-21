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
import abfab3d.core.AttributeGrid;
import abfab3d.core.Vec;
import abfab3d.core.GridProducer;
import abfab3d.core.ImageStackProducer;

import abfab3d.param.ParamCache;
import abfab3d.param.Parameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.EnumParameter;

import abfab3d.grid.op.ImageStackToGrid;
import abfab3d.grid.op.ImageStackLoader;

import javax.vecmath.Vector3d;

import static abfab3d.core.MathUtil.lerp;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.MathUtil.clamp;
import static java.lang.Math.floor;


/**
 * <p>
 * DataSource which fills 3D box with color data from a stack of 2D images in xy plane.
 * </p><p>
 * ImageColorMap3d may have multiple channels, according to the source image type 
 * </p><p>
 * The 2D images are oriented in XY plane orthogonal to Z axis . 
 * </p>
 * The image map can be periodically repeated in X,Y and Z directions.
 *
 *
 * @author Vladimir Bulatov
 */
public class ImageColorMap3D extends TransformableDataSource {

    final static boolean DEBUG = false;

    final static double NORM = (1./255.);
    final static public String sm_projectionNames[] = new String[]{"plain", "spherical", "cylindrical"};
    
    private boolean m_repeatX = false;
    private boolean m_repeatY = false;
    private boolean m_repeatZ = false;
    private double
            m_originX,
            m_originY,
            m_originZ,
            m_sizeX,
            m_sizeY,
            m_sizeZ;
    private int m_imageSizeX, m_imageSizeY, m_imageSizeZ;

    // public parameters 
    SNodeParameter mp_imageSource = new SNodeParameter("image", "image source", null);
    Vector3dParameter mp_center = new Vector3dParameter("center", "center of the image box", new Vector3d(0., 0., 0.));
    Vector3dParameter mp_size = new Vector3dParameter("size", "size of the image box", new Vector3d(0.1, 0.1, 0.1));
    BooleanParameter mp_repeatX = new BooleanParameter("repeatX", "repeat image along X", false);
    BooleanParameter mp_repeatY = new BooleanParameter("repeatY", "repeat image along Y", false);
    BooleanParameter mp_repeatZ = new BooleanParameter("repeatZ", "repeat image along Z", false);
    EnumParameter mp_projection = new EnumParameter("projection", "type of projection to use", sm_projectionNames, sm_projectionNames[0]);

    Parameter m_aparams[] = new Parameter[]{
            mp_imageSource,
            mp_center,
            mp_size,
            mp_repeatX,
            mp_repeatY,
            mp_repeatZ,
            mp_projection,
    };

    /** Params which require changes in the underlying image */
    private Parameter[] m_imageParams = new Parameter[] {
            mp_imageSource
    };

    // 
    private AttributeGrid m_imageData;

    /**
     * Creates ImageColorMap from a file
     *
     * @param path source of the image. Can be url, BufferedImage or ImageWrapper
     * @param sizex - width of the image
     * @param sizey - height of the image
     * @param sizez - depth of the image
     */
    public ImageColorMap3D(String pathTemplate, int firstIndex, int count, double sizex, double sizey, double sizez) {
        this(new ImageStackToGrid(new ImageStackLoader(pathTemplate, firstIndex, count), true), sizex, sizey, sizez);
    }

    public ImageColorMap3D(ImageStackProducer producer, double sizex, double sizey, double sizez) {
        this(new ImageStackToGrid(producer, true), sizex, sizey, sizez);
    }

    public ImageColorMap3D(GridProducer gridProducer, double sizex, double sizey, double sizez) {

        super.addParams(m_aparams);
        mp_imageSource.setValue(gridProducer);
        mp_size.setValue(new Vector3d(sizex, sizey, sizez));
    }


    /**
     * Set the source image
     * @param val
     */
   
    public void setImage(Object val) {
                
        if (val instanceof ImageStackProducer) {
            val = new ImageStackToGrid((ImageStackProducer) val, true);
        } else if (val instanceof GridProducer) {
            // fine
        } else {
            throw new IllegalArgumentException(fmt("Unsupported object for ImageColorMap: %s", val.getClass()));
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
    public void getRawData(int data[]) {

        if(true) throw new RuntimeException("not implented");
        int nx = m_imageData.getWidth();
        int ny = m_imageData.getHeight();
        int nz = m_imageData.getDepth();
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                for (int z = 0; z < nz; z++) {
                    long d = m_imageData.getAttribute(x, y, z);
                    data[z + x*nz + y * nx*nz ] = (int)(d & 0xFFFFFFFF);
                }
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


        String label = getDataLabel();

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
            m_imageData = (AttributeGrid) co;
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
        m_imageSizeZ = m_imageData.getDepth();

        m_repeatX = mp_repeatX.getValue();
        m_repeatY = mp_repeatY.getValue();
        m_repeatZ = mp_repeatZ.getValue();

        // this may be different depending on the image 
        // good for general ARGB 
        m_channelsCount = 4;

        return ResultCodes.RESULT_OK;

    }

    private AttributeGrid prepareImage() {

        Object obj = mp_imageSource.getValue(); 
        if(DEBUG) printf("ImageColorMap.prepareImage() source: %s\n", obj);
        if(obj == null || !(obj instanceof GridProducer))
            throw new RuntimeException(fmt("unrecoginized grid source: %s\n",obj));
        
        
        GridProducer producer = (GridProducer)obj; 
        
        AttributeGrid grid = producer.getGrid(); 
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
        z /= m_sizeZ;

        // xy are in (0,1) range 
        if (m_repeatX) x -= floor(x);
        if (m_repeatY) y -= floor(y);
        if (m_repeatZ) z -= floor(z);

        // x in [0, imageSizeX]
        // y in [0, imageSizeY]
        // z in [0, imageSizeZ]
        x *= m_imageSizeX;
        y *= m_imageSizeY;
        z *= m_imageSizeZ;
        // half pixel shift 
        x -= 0.5;
        y -= 0.5;
        z -= 0.5;

        int ix = (int)floor(x);
        int iy = (int)floor(y);
        int iz = (int)floor(z);

        int ix1 = ix + 1;
        int iy1 = iy + 1;
        int iz1 = iz + 1;

        double dx = x - ix;
        double dy = y - iy;
        double dz = z - iz;

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
        if(m_repeatZ){
            iz -= m_imageSizeZ*floor((double)iz/m_imageSizeZ);
            iz1 -= m_imageSizeZ*floor((double)iz1/m_imageSizeZ);
        } else {
            iz = clamp(iz, 0, m_imageSizeZ-1);            
            iz1 = clamp(iz1, 0, m_imageSizeZ-1);            
        }


        double
            dx1 = 1. - dx,
            dy1 = 1. - dy,
            dz1 = 1. - dz,
            dxdy = dx * dy,
            dx1dy = dx1 * dy,
            dxdy1 = dx * dy1,
            dx1dy1 = dx1 * dy1;
        
        int
            v000 = getPixelData(ix,  iy, iz),
            v100 = getPixelData(ix1, iy, iz),
            v010 = getPixelData(ix, iy1, iz),
            v110 = getPixelData(ix1,iy1, iz),
            v001 = getPixelData(ix,  iy, iz1),
            v101 = getPixelData(ix1, iy, iz1),
            v011 = getPixelData(ix, iy1, iz1),
            v111 = getPixelData(ix1,iy1, iz1);

        
        int
            r000 = getRed(v000),
            r100 = getRed(v100),
            r010 = getRed(v010),
            r110 = getRed(v110),

            r001 = getRed(v001),
            r101 = getRed(v101),
            r011 = getRed(v011),
            r111 = getRed(v111),

            g000 = getGreen(v000),
            g100 = getGreen(v100),
            g010 = getGreen(v010),
            g110 = getGreen(v110),
            g001 = getGreen(v001),
            g101 = getGreen(v101),
            g011 = getGreen(v011),
            g111 = getGreen(v111),

            b000 = getBlue(v000),
            b100 = getBlue(v100),
            b010 = getBlue(v010),
            b110 = getBlue(v110),
            b001 = getBlue(v001),
            b101 = getBlue(v101),
            b011 = getBlue(v011),
            b111 = getBlue(v111),

            a000 = getAlpha(v000),
            a100 = getAlpha(v100),
            a010 = getAlpha(v010),
            a110 = getAlpha(v110),            
            a001 = getAlpha(v001),
            a101 = getAlpha(v101),
            a011 = getAlpha(v011),
            a111 = getAlpha(v111);            
        
        
        double r = lerp(lerp(lerp(r000, r100,dx),lerp(r010, r110,dx),dy),lerp(lerp(r001, r101,dx),lerp(r011, r111,dx),dy),dz);
        double g = lerp(lerp(lerp(g000, g100,dx),lerp(g010, g110,dx),dy),lerp(lerp(g001, g101,dx),lerp(g011, g111,dx),dy),dz);
        double b = lerp(lerp(lerp(b000, b100,dx),lerp(b010, b110,dx),dy),lerp(lerp(b001, b101,dx),lerp(b011, b111,dx),dy),dz);
        double a = lerp(lerp(lerp(a000, a100,dx),lerp(a010, a110,dx),dy),lerp(lerp(a001, a101,dx),lerp(a011, a111,dx),dy),dz);

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

    final int getPixelData(int x, int y, int z) {

        return (int)m_imageData.getAttribute(x, y, z);

    }
}