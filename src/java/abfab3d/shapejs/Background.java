/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.shapejs;

import abfab3d.core.Color;
import abfab3d.core.Initializable;
import abfab3d.core.ResultCodes;
import abfab3d.datasources.FormattedText2D;
import abfab3d.datasources.ImageColorMap;
import abfab3d.datasources.ImageWrapper;
import abfab3d.datasources.Text2D;
import abfab3d.param.*;
import abfab3d.util.FileUtil;
import abfab3d.util.ImageColor;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
 * Background setup for a scene
 *
 * Modes are:
 *      Single Color - The background is a single color
 *      Gradient - The background is a gradient from sky to ground color
 *      Image - The background is an image for lighting and pixels not hitting a surface
 *      Image Color - The background is an image for lighting and a single color for pixels not hitting a surface
 *      Image Gradient - THe background is an image for lighting and a gradient for pixels not hitting a surface
 *
 * @author Alan Hudson
 */
public class Background extends BaseParameterizable implements Initializable {
    
    public static final String 
        sSINGLE_COLOR="SINGLE_COLOR", 
        sGRADIENT = "GRADIENT", 
        sIMAGE = "IMAGE", 
        sIMAGE_COLOR="IMAGE_COLOR", 
        sIMAGE_GRADIENT= "IMAGE_GRADIENT";

    public enum Mode {SINGLE_COLOR, GRADIENT, IMAGE, IMAGE_COLOR, IMAGE_GRADIENT;
        public static int getIntValue(Mode mode) {
            switch(mode) {
                case SINGLE_COLOR:
                    return 0;
                case GRADIENT:
                    return 1;
                case IMAGE:
                    return 2;
                case IMAGE_COLOR:
                    return 3;
                case IMAGE_GRADIENT:
                    return 4;
                default:
                    throw new IllegalArgumentException("Unknown mode");
            }
        }
    }

    private ColorParameter mp_groundColor = new ColorParameter("groundColor","Color",new Color(225f/255,227f/255,228f/255));
    private ColorParameter mp_skyColor = new ColorParameter("skyColor","Color",new Color(245f/255,247f/255,248f/255));
    private DoubleParameter mp_smoothStart = new DoubleParameter("smoothStart","Start of smoothing", 0.4);
    private DoubleParameter mp_smoothEnd = new DoubleParameter("smoothEnd","End of smoothing", 0.5);
    URIParameter mp_imageSource = new URIParameter("image", "image source", resolveURN("urn:shapeways:stockImage:envmap_rays"));
    EnumParameter mp_mode = new EnumParameter("mode","mode",new String[] {sSINGLE_COLOR,sGRADIENT,sIMAGE,sIMAGE_COLOR,sIMAGE_GRADIENT},sIMAGE_GRADIENT);


    private ImageColor m_imageData;

    private Parameter m_aparam[] = new Parameter[]{
            mp_groundColor,
            mp_skyColor,
            mp_smoothStart,
            mp_smoothEnd,
            mp_imageSource,
            mp_mode
    };

    private Parameter[] m_imageParams = new Parameter[]{
            mp_imageSource
    };

    public Background() {
        initParams();
    }

    public Background(Color color) {
        initParams();
        mp_mode.setValue(sSINGLE_COLOR);
        mp_groundColor.setValue(color);
        mp_skyColor.setValue(color);
    }

    public Background(Color groundColor, Color skyColor) {
        initParams();
        mp_groundColor.setValue(groundColor);
        mp_skyColor.setValue(skyColor);
    }

    /**
      * @param imageSource source of the image. Can be url, BufferedImage or ImageWrapper
     */
    public Background(String imageSource) {
        initParams();
        setImage(imageSource);
    }

    protected void initParams(){
        super.addParams(m_aparam);
    }

    public Color getGroundColor() {
        return mp_groundColor.getValue();
    }

    public void setGroundColor(Color val) {
        mp_groundColor.setValue(val);
    }

    public Color getSkyColor() {
        return mp_skyColor.getValue();
    }

    public void setSkyColor(Color val) {
        mp_skyColor.setValue(val);
    }

    public void setSmoothStart(double val) {
        mp_smoothStart.setValue(val);
    }

    public double getSmoothStart() {
        return mp_smoothStart.getValue();
    }

    public void setSmoothEnd(double val) {
        mp_smoothEnd.setValue(val);
    }

    public double getSmoothEnd() {
        return mp_smoothEnd.getValue();
    }

    public void setMode(String val) {
        mp_mode.setValue(val);
    }

    public String getMode() {
        return mp_mode.getValue();
    }

    public void setImage(Object map) {
        if (map instanceof String) {
            map = resolveURN((String)map);
        }
        m_imageData = null;
        mp_imageSource.setValue(map);
    }

    /**
     * Change urn:shapeways into local files.  TODO: I'd like to commonize this with logic in ScriptManager
     * @param st
     * @return
     */
    private String resolveURN(String st) {
        String ret_val = st;
        if (st.startsWith("urn:shapeways:stockImage:")) {
            ret_val = "stock" + File.separator + "media" + File.separator + "images" + File.separator + st.substring(25) + ".png";
        }
        return ret_val;
    }

    public Object getImage() {
        return mp_imageSource.getValue();
    }

    public ImageColor getLoadedImage() {
        if (needToPrepareImage()) {
            initialize();
        }

        return m_imageData;
    }

    public void set(String paramName, Object value) {
        super.set(paramName,value);

        // TODO: need to clear m_imageData, this is tedious
    }

    /**
     * @noRefGuide
     */
    public int initialize() {
        if (needToPrepareImage()) {

            int res = prepareImage();
            if (res != ResultCodes.RESULT_OK) {
                // something wrong with the image
                throw new IllegalArgumentException("undefined image");
            }
        }

        return ResultCodes.RESULT_OK;

    }

    /**
     checks if params used to generate image have changed
     * @noRefGuide
     */
    protected boolean needToPrepareImage() {
        boolean hc = mp_imageSource.hasChanged();
        return (m_imageData == null || hc);
    }

    /**
     * @noRefGuide
     */
    private int prepareImage() {

        long t0 = time();

        String imageSource = mp_imageSource.getValue();
        if (imageSource == null)
            throw new RuntimeException("imageSource is null");

        InputStream is = FileUtil.getResourceAsStream(imageSource);
        if (is == null) {
            m_imageData = new ImageColor(1, 1);
            throw new RuntimeException("Cannot load background file: " + imageSource);
        }
        try {
            m_imageData = new ImageColor(ImageIO.read(is));
        } catch(IOException ioe) {
            m_imageData = new ImageColor(1, 1);
            throw new RuntimeException("Cannot load background file: " + imageSource);
        }

        return ResultCodes.RESULT_OK;
    }

    public void getBitmapDataInt(int data[]) {
        if (needToPrepareImage()) initialize();

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
}
