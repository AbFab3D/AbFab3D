/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2014
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

import abfab3d.core.Bounds;
import abfab3d.core.Grid2D;
import abfab3d.core.Grid2DProducer;
import abfab3d.core.GridDataDesc;
import abfab3d.core.ImageProducer;
import abfab3d.datasources.Grid2DSourceWrapper;
import abfab3d.grid.Grid2DInt;
import abfab3d.grid.Grid2DShort;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.BooleanParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.ParamCache;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;

import java.awt.image.BufferedImage;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.PT;
import static abfab3d.util.ImageUtil.getGray16Data;
import static abfab3d.util.ImageUtil.getImageData_INT_ARGB;


/**
 * class to produce Grid2D from ImageProducer
 */
public class ImageToGrid2D extends BaseParameterizable implements Grid2DProducer {

    static final boolean DEBUG = false;
    static final double DEFAULT_PIXEL_SIZE = PT; // 1 point
    static final boolean CACHING_ENABLED = true;

    SNodeParameter mp_imageProducer = new SNodeParameter("imageProducer", "image producer");
    DoubleParameter mp_pixelSize = new DoubleParameter("pixelSize", "pixel Size", DEFAULT_PIXEL_SIZE);
    BooleanParameter mp_useColor = new BooleanParameter("useColor", "use color", false);
    Parameter m_params[] = new Parameter[]{
        mp_imageProducer,
        mp_useColor,
        mp_pixelSize,
    };

    // loaded image converted into grid
    Grid2D m_grid;


    /**
     * @param imageProducer - image producer
     * @param useColor      - use color or convert to gray
     */
    public ImageToGrid2D(ImageProducer imageProducer, boolean useColor) {
        addParams(m_params);
        mp_imageProducer.setValue(imageProducer);
        mp_useColor.setValue(useColor);

    }

    /**
     * @param imageProducer - image producer
     */
    public ImageToGrid2D(ImageProducer imageProducer) {
        addParams(m_params);
        mp_imageProducer.setValue(imageProducer);
    }

    /**
     * @param imageProducer - image producer
     */
    public ImageToGrid2D(BufferedImage imageProducer) {
        // Added for backwards compatibility
        addParams(m_params);
        mp_imageProducer.setValue(new ImageWrapper(imageProducer));
    }

    public int getWidth() {
        Grid2D grid = getGrid2D();
        return grid.getWidth();
    }

    public int getHeight() {

        Grid2D grid = getGrid2D();
        return grid.getHeight();

    }

    /**
     * @Override
     */
    public Grid2D getGrid2D() {

        Object co = null;
        String label = null;
        if (CACHING_ENABLED) {
            label = getParamString(getClass().getSimpleName(), m_params);
            co = ParamCache.getInstance().get(label);
        }
        if (co == null) {
            m_grid = prepareGrid();
            if (CACHING_ENABLED) {
                ParamCache.getInstance().put(label, m_grid);
                if (DEBUG) printf("ImageToGrid2D: caching image: %s -> %s\n", label, m_grid);
            }
        } else {
            m_grid = (Grid2D) co;
            if (DEBUG) printf("ImageToGrid2D: got cached image %s -> %s\n", label, m_grid);
        }

        Grid2DSourceWrapper wrapper = new Grid2DSourceWrapper(label,m_grid);  // TODO: not so sure this is the right label
        return wrapper;

    }

    /**
     * Added for backwards compatibility
     *
     * @return
     */
    public Grid2D getGrid() {
        return getGrid2D();
    }

    protected Grid2D prepareGrid() {

        if (DEBUG) printf("%s.prepareGrid()\n", this);
        ImageProducer producer = (ImageProducer) mp_imageProducer.getValue();
        BufferedImage image = producer.getImage();
        if (mp_useColor.getValue())
            return makeColorGrid(image, mp_pixelSize.getValue());
        else
            return makeGrayGrid(image, mp_pixelSize.getValue());
    }

    public static Grid2D makeGrayGrid(BufferedImage image, double pixelSize) {

        int w = image.getWidth();
        int h = image.getHeight();

        if (DEBUG) printf("Making grayGrid.  %d x %d\n", w, h);
        Grid2DShort grid = new Grid2DShort(w, h, pixelSize);
        grid.setGridBounds(new Bounds(0, w * pixelSize, 0, h * pixelSize, 0, pixelSize));
        grid.setDataDesc(GridDataDesc.getDefaultAttributeDesc(16));
        short data[] = getGray16Data(image);
        // Need to convert from image (0,0) upper left to grid (0,0) lower left
        for (int y = 0; y < h; y++) {
            int y1 = h - 1 - y;
            for (int x = 0; x < w; x++) {
                short d = data[x + y * w];
                grid.setAttribute(x, y1, d);
            }
        }
        return grid;
    }

    public static Grid2D makeColorGrid(BufferedImage image, double pixelSize) {

        int nx = image.getWidth();
        int ny = image.getHeight();
        if (DEBUG) printf("ImageToGrid2D.makeColorGrid() %d x %d\n", nx, ny);
        int[] imageData = getImageData_INT_ARGB(image);

        Grid2DInt grid = new Grid2DInt(nx, ny, pixelSize);
        for (int y = 0; y < ny; y++) {
            int yoff = nx * (ny - 1 - y);
            for (int x = 0; x < nx; x++) {
                grid.setAttribute(x, y, imageData[x + yoff]);
            }
        }
        if (DEBUG) printf("ImageToGrid2D. color grid %d x %d\n", grid.getWidth(), grid.getHeight());
        return grid;
    }


    static class ImageWrapper implements ImageProducer {

        private BufferedImage image;

        public ImageWrapper(BufferedImage image) {
            this.image = image;
        }

        @Override
        public BufferedImage getImage() {
            return image;
        }
    }
} // class ImageReader