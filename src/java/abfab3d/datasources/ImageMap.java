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


import abfab3d.grid.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.param.ObjectParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.BooleanParameter;

import abfab3d.util.ImageUtil;
import abfab3d.util.Vec;
import abfab3d.util.ImageGray16;


import static java.lang.Math.floor;

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

    // public parameters 
    ObjectParameter  mp_imageSource = new ObjectParameter("image","image source",null);
    Vector3dParameter  mp_center = new Vector3dParameter("center","center of the image box",new Vector3d(0.,0.,0.));
    Vector3dParameter  mp_size = new Vector3dParameter("size","size of the image box",new Vector3d(0.1,0.1,0.1));
    BooleanParameter  mp_repeatX = new BooleanParameter("repeatX","repeat image along X", false);
    BooleanParameter  mp_repeatY = new BooleanParameter("repeatY","repeat image along Y", false);
    DoubleParameter  mp_whiteDisp = new DoubleParameter("whiteDisplacement","displacement for white level", 0*MM);
    DoubleParameter  mp_blackDisp = new DoubleParameter("blackDisplacement","displacement for black level", 1*MM);
    DoubleParameter  mp_blurWidth = new DoubleParameter("blurWidth", "width of gaussian blur on the image", 0.);

    Parameter m_aparams[] = new Parameter[]{
        mp_imageSource, 
        mp_center,
        mp_size,
        mp_repeatX,
        mp_repeatY,
        mp_whiteDisp,
        mp_blackDisp,
        mp_blurWidth,

    };

    // 
    private ImageGray16 m_imageData;
    private String m_savedParamString = "";    

    /**
     * Creates ImageMap from a file
     *
     * @param imageSource source of the image. Can be url, BufferedImage or ImageWrapper
     * @param sizex - width of the image
     * @param sizey - height of the image
     * @param sizez - depth of the image
     */
    public ImageMap(Object imageSource, double sizex, double sizey, double sizez) {

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
        return m_imageData.getWidth();
    }

    /**
     * @noRefGuide
     */
    public int getBitmapHeight(){
        return m_imageData.getHeight();
    }

    /**
     * @noRefGuide
     */
    public void getBitmapDataUByte(byte data[]){

        int nx = m_imageData.getWidth();
        int ny = m_imageData.getHeight();
        for(int y = 0;  y < ny; y++){
            for(int x = 0;  x < nx; x++){
                
                double d = m_imageData.getDataD(x, y);
                // d in (0,1) 
                // normalization to byte 
                data[x + y * nx] = (byte)((int)(d * 255.) & 0xFF); 
            }
        }
    }

    /**
     * @noRefGuide
     */
    public int initialize() {
        super.initialize();

        if(needToPrepareImage()){
            int res = prepareImage();
            if(res != RESULT_OK){ 
                // something wrong with the image 
                throw new IllegalArgumentException("undefined image");
            }
            saveImageData();
        }

        return RESULT_OK;
        
    }

    /**
       checks if params used to generate image have chaned 
     * @noRefGuide
     */
    protected boolean needToPrepareImage(){
        return 
            (m_imageData == null) || 
            !m_savedParamString.equals(getParamString(m_aparams));
        
    }

    /**
       saves params used to generate the image 
     * @noRefGuide
     */
    protected void saveImageData(){ 
        m_savedParamString = getParamString(m_aparams);
        printf("ImageMap.savedParamString:\n%s",m_savedParamString);
    }


    /**
     * @noRefGuide
     */
    private int prepareImage(){

        long t0 = time();

        printf("ImageMap.prepareImage()\n");

        Object imageSource = mp_imageSource.getValue();
        if(imageSource == null)
            throw new RuntimeException("imageSource is null");

        if(imageSource instanceof String){
            
            try {
                m_imageData = new ImageGray16(ImageIO.read(new File((String)imageSource)));
            } catch(IOException e) {
                // empty 1x1 image 
                m_imageData = new ImageGray16();
                throw new RuntimeException(e);
            }

        } else if(imageSource instanceof Text2D){
            
            m_imageData = new ImageGray16(((Text2D)imageSource).getImage());

        } else if(imageSource instanceof BufferedImage){

            m_imageData = new ImageGray16((BufferedImage)imageSource);                        

        } else if(imageSource instanceof ImageWrapper){

           m_imageData = new ImageGray16(((ImageWrapper)imageSource).getImage());
        } else if (imageSource instanceof Grid2DShort) {
            long t1 = System.currentTimeMillis();
            m_imageData = new ImageGray16(Grid2DShort.convertGridToImage((Grid2DShort)imageSource));
            printf("Convert to grid.  time: %d ms\n",(System.currentTimeMillis() - t1));
        }

        if (m_imageData == null) {
            // Cast to String for now, not sure how to really handle this
            String file = imageSource.toString();
            printf("Converted to string: " + file);
            try {
                m_imageData = new ImageGray16(ImageIO.read(new File(file)));

            } catch(IOException e) {
                // empty 1x1 image
                m_imageData = new ImageGray16();
                throw new IllegalArgumentException("Unhandled imageSource: " + imageSource);
            }
        }

        Vector3d center = (Vector3d)mp_center.getValue();
        Vector3d size = (Vector3d)mp_size.getValue();
        m_originX = center.x - size.x/2;
        m_originY = center.y - size.y/2;
        m_originZ = center.z - size.z/2;
        m_sizeX = size.x;
        m_sizeY = size.y;
        m_sizeZ = size.z;

        m_imageSizeX  = m_imageData.getWidth();
        m_imageSizeY  = m_imageData.getHeight();
        m_repeatX = mp_repeatX.getValue();
        m_repeatY = mp_repeatY.getValue();
        
        double white = mp_whiteDisp.getValue();
        double black = mp_blackDisp.getValue();
        
        m_valueOffset = black;
        m_valueFactor = white - black;

        double blurWidth = mp_blurWidth.getValue();
        if (blurWidth > 0.0) {
            long t1 = System.currentTimeMillis();
            double pixelSize = m_sizeX / m_imageSizeX;

            double blurSizePixels = blurWidth / pixelSize;

            m_imageData.gaussianBlur(blurSizePixels);
            printf("ImageMap image[%d x %d] gaussian blur: %7.2f pixels blur width: %10.5fmm time: %d ms\n", 
                   m_imageSizeX, m_imageSizeY, blurSizePixels, blurWidth/MM, (time() - t1));


        }

        printf("ImageMap.prepareImage() time: %d ms\n",(time() - t0));
        
        return RESULT_OK;
    }

    /**
     * @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec dataValue){

        super.transform(pnt);

        double x = pnt.v[0];
        double y = pnt.v[1];
        double z = pnt.v[2];

        x -= m_originX;
        y -= m_originY;
        z -= m_originZ;

        // xy coordinates are normalized to the image size
        x /= m_sizeX;
        y /= m_sizeY;


        x /= m_sizeX;
        y /= m_sizeY;

        // xy are in (0,1) range 
        if(m_repeatX) x -= floor(x);
        if(m_repeatY) y -= floor(y);

        // x in [0, imageSizeX]
        // y in [0, imageSizeY]
        x *= m_imageSizeX;
        y *= m_imageSizeY;
        // half pixel shift 
        x -= 0.5;
        y -= 0.5;
        
        int ix = (int)floor(x);
        int iy = (int)floor(y);
        double dx = x - ix;
        double dy = y - iy;
        if(ix < 0){
            if(m_repeatX) ix = m_imageSizeX-1;
            else ix = 0;
        }
        if(iy < 0){
            if(m_repeatY) iy = m_imageSizeY-1;
            else iy = 0;
        }
        int ix1 = ix + 1;
        if(ix1 >= m_imageSizeX){
            if(m_repeatX) ix = 0;
            else ix = m_imageSizeX-1;            
        }
        int iy1 = iy + 1;
        if(iy1 >= m_imageSizeY){
            if(m_repeatY) iy = 0;
            else iy = m_imageSizeY-1;            
        }
        
        double 
            v00 = m_imageData.getDataD(ix, iy),
            v10 = m_imageData.getDataD(ix1, iy),
            v01 = m_imageData.getDataD(ix, iy1),
            v11 = m_imageData.getDataD(ix1, iy1);
        double 
            dx1 = 1.- dx,
            dy1 = 1.- dy;


        double v = 
            dx * dy * v11 + dx1 * dy * v01 + dx * dy1 * v10 + dx1 * dy1 * v00;

        dataValue.v[0] = v*m_valueFactor + m_valueOffset;

        return RESULT_OK;
    }
    
}