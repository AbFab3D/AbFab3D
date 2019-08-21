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
import abfab3d.core.GridProducer;
import abfab3d.core.GridDataDesc;
import abfab3d.core.ImageStackProducer;

import abfab3d.core.AttributeGrid;

import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.ArrayAttributeGridInt;

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
 * class to produce AttribiuteGrid from ImageStackProducer
 */
public class ImageStackToGrid extends BaseParameterizable implements GridProducer {

    static final boolean DEBUG = false;
    static final double DEFAULT_PIXEL_SIZE = PT; // 1 point
    static final boolean CACHING_ENABLED = true;

    SNodeParameter mp_imageStackProducer = new SNodeParameter("imageStackProducer", "image producer");
    DoubleParameter mp_pixelSize = new DoubleParameter("pixelSize", "pixel Size", DEFAULT_PIXEL_SIZE);
    BooleanParameter mp_useColor = new BooleanParameter("useColor", "use color", false);

    Parameter m_params[] = new Parameter[]{
        mp_imageStackProducer,
        mp_useColor,
        mp_pixelSize,
    };

    // loaded images converted into grid
    AttributeGrid m_grid;

    /**
     * @param imageProducer - image producer
     * @param useColor      - use color or convert to gray
     */
    public ImageStackToGrid(ImageStackProducer imageProducer, boolean useColor) {
        addParams(m_params);
        mp_imageStackProducer.setValue(imageProducer);
        mp_useColor.setValue(useColor);

    }

    /**
     * @param imageProducer - image producer
     */
    public ImageStackToGrid(ImageStackProducer producer) {

        addParams(m_params);
        mp_imageStackProducer.setValue(producer);
    }

    public int getWidth() {

        AttributeGrid grid = getGrid();
        return grid.getWidth();
    }
    
    public int getHeight() {

        AttributeGrid grid = getGrid();
        return grid.getHeight();

    }

    /**
       @override 
     */
    public Bounds getGridBounds(){
        //TODO 
        return null;
    }


    /**
       @override 
     */
    public int getChannelCount(){
        //TODO 
        return 1;

    }

    /**
     * @Override
     */
    public AttributeGrid getGrid() {

        Object co = null;
        String label = null;

        label = getDataLabel();

        if (CACHING_ENABLED) {
            co = ParamCache.getInstance().get(label);
            if (DEBUG) printf("ImageToGrid2D.  label: %s  cached: %b\n",label,co!=null);
        }
        if (co == null) {
            m_grid = prepareGrid();
            if (CACHING_ENABLED) {
                ParamCache.getInstance().put(label, m_grid);
                if (DEBUG) printf("ImageStackToGrid: caching grid: %s -> %s\n", label, m_grid);
            }
            return m_grid;
        } else {
            m_grid = (AttributeGrid) co;
            if (DEBUG) printf("ImageToGrid: got cached image %s -> %s\n", label, m_grid);

            return m_grid;
        }
    }

    protected AttributeGrid prepareGrid() {

        if (DEBUG) printf("%s.prepareGrid()\n", this);
        ImageStackProducer producer = (ImageStackProducer) mp_imageStackProducer.getValue();
        if (mp_useColor.getValue())
            return makeColorGrid(producer, mp_pixelSize.getValue());
        else
            return makeGrayGrid(producer, mp_pixelSize.getValue());
    }

    public static AttributeGrid makeGrayGrid(ImageStackProducer producer, double vs) {


        BufferedImage image = producer.getImage(0);
        int nx = image.getWidth();
        int ny = image.getHeight();
        int nz = producer.getCount();

        if (DEBUG) printf("Making grayGrid.  p%d x %d x %d]\n", nx, ny, nz);

        AttributeGrid grid = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);
        grid.setDataDesc(GridDataDesc.getDefaultAttributeDesc(16));

        for(int z = 0; z < nz; z++){
            
            image = producer.getImage(z);
            
            short data[] = getGray16Data(image);

            for (int y = 0; y < ny; y++) {

                // convert from image (0,0) upper left to grid (0,0) lower left
                int yoff = nx * (ny - 1 - y);

                for (int x = 0; x < ny; x++) {

                    short d = data[x + yoff];
                    grid.setAttribute(x, y, z, d);
                }
            }
        }
        return grid;
    }
    
    public static AttributeGrid makeColorGrid(ImageStackProducer producer, double vs) {

        BufferedImage image = producer.getImage(0);
        int nx = image.getWidth();
        int ny = image.getHeight();
        int nz = producer.getCount();

        if (DEBUG) printf("Making grayGrid.  p%d x %d x %d]\n", nx, ny, nz);

        AttributeGrid grid = new ArrayAttributeGridInt(nx, ny, nz, vs, vs);

        //grid.setDataDesc(GridDataDesc.getDefaultAttributeDesc());

        for(int z = 0; z < nz; z++){
            
            image = producer.getImage(z); 

            int[] data = getImageData_INT_ARGB(image);
            for (int y = 0; y < ny; y++) {
                // convert from image (0,0) upper left to grid (0,0) lower left
                int yoff = nx * (ny - 1 - y);
                for (int x = 0; x < nz; x++) {
                    int d = data[x + yoff];
                    grid.setAttribute(x, y, z, d);
                }
            }
        }
        return grid;

    }

} // class ImageStackToGrid