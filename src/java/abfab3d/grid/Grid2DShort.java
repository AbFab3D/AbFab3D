/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011-2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid;


import abfab3d.util.ImageUtil;
import abfab3d.util.Bounds;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import abfab3d.util.ImageGray16;


import static abfab3d.util.ImageUtil.us2i;
import static abfab3d.util.Output.fmt;

/**
 * A 2D grid backed by arrays. 
 * it formally uses interface of AttributeGrid, but z param is ignored 
 * 
 * @author Alan Hudson
 */
public class Grid2DShort extends BaseGrid2D implements Grid2D {

    static final long MAX_USHORT = 0xFFFF;

    protected short[] data;

    /**
     * Constructor.
     *
     * @param width The number of voxels in width
     * @param height The number of voxels in height
     */
    public Grid2DShort(int width, int height){
        super(width, height,1.);
        allocateData();
        setAttributeDesc(GridDataDesc.getDefaultAttributeDesc(16));
    }

    /**
     * Constructor.
     *
     * @param width The number of voxels in width
     * @param height The number of voxels in height
     * @param pixel The pixel size in meters
     */
    public Grid2DShort(int width, int height, double pixel){
        super(width, height,pixel);
        allocateData();
        setAttributeDesc(GridDataDesc.getDefaultAttributeDesc(16));
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public Grid2DShort(Grid2DShort grid) {
        super(grid.getWidth(), grid.getHeight(), 1.);
        copyBounds(grid);
        this.data = grid.data.clone();
        setAttributeDesc(grid.getAttributeDesc());
    }
    
    /**
       @param bounds grid bounds 
       @param pxiel size of grid pixel
     */
    public Grid2DShort(Bounds bounds, double pixel) {
        super(bounds, pixel);
        allocateData();
        setAttributeDesc(GridDataDesc.getDefaultAttributeDesc(16));
    }

    /**
     * Create an empty grid of the specified size.  Reuses
     * the grid type and material type(byte, short, int).
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param pixel The pixel size in meters
     */
    public Grid2D createEmpty(int w, int h, double pixel) {
        Grid2D ret_val = new Grid2DShort(w,h,pixel);
        ret_val.setAttributeDesc(GridDataDesc.getDefaultAttributeDesc(16));
        return ret_val;
    }

    protected void allocateData(){
        
        if((long)height*width > Integer.MAX_VALUE)
            throw new RuntimeException(fmt("grid size: [%d x %d] exceeds maximum [46340 x 46340]", width, height));
        data = new short[height * width];
    }

    /**
     * Get the attribute of the voxel, 2D version
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @return The voxel attribute 
     */
    public final long getAttribute(int x, int y) {

        int idx = y * width + x;
        return ((int)data[idx] & MAX_USHORT);
    }

    /**
     * Set the attribute of a voxel, 2D version 
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param attribute value of attribute 
     */
    public final void setAttribute(int x, int y, long attribute) {
        int idx = y * width + x;
        data[idx] = (short) (attribute & MAX_USHORT);
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        return new Grid2DShort(this);
    }

    /**
     copy data from fromGrid into this grid
     */
    public void copyData(Grid2D fromGrid){
        if(fromGrid instanceof Grid2DShort){
            System.arraycopy(((Grid2DShort)fromGrid).data,0, data, 0, data.length);
        } else {
            throw new IllegalArgumentException("Unsupported");
        }
    }

    /**
     copy data from fromGrid into this grid
     */
    public void copyData(short[] fromGrid){
        System.arraycopy(fromGrid,0, data, 0, data.length);
    }

    /**
     * Convert a Grid2D class into a Java Image.
     * @param grid
     * @return
     */
    public static BufferedImage convertGridToImage(Grid2D grid) {
        BufferedImage bi = new BufferedImage(grid.getWidth(), grid.getHeight(),BufferedImage.TYPE_INT_ARGB);

        int height = grid.getHeight();
        int width = grid.getWidth();

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){

                long cc = grid.getAttribute(x,height-y-1);

                bi.setRGB(x,y,makeColor((int)cc));
            }
        }

        return bi;
    }

    /**
     * Convert a Java image to a Grid2D
     *
     * @param image
     * @return
     */
    public static Grid2DShort convertImageToGrid(BufferedImage image, double pixelSize) {
        
        int w = image.getWidth();
        int h = image.getHeight();
        
        Grid2DShort grid = new Grid2DShort(w,h);
        grid.setGridBounds(new Bounds(0, w*pixelSize, 0, h*pixelSize, 0, pixelSize)); 
        grid.setAttributeDesc( GridDataDesc.getDefaultAttributeDesc(16));
        short data[] = ImageUtil.getGray16Data(image);
        int h1 = h-1;
        // Need to convert from 0,0 upper left to 0,0 lower left
        for(int y=0; y < h; y++) {
            for(int x=0; x < w; x++) {
                short d = data[x + y * w];
                grid.setAttribute(x, h1 - y, d);
            }
        }

        return grid;
    }

    public static Grid2DShort convertImageToGrid(ImageGray16 image, boolean invertImage, double pixelSize) {
        
        int w = image.getWidth();
        int h = image.getHeight();
        
        Grid2DShort grid = new Grid2DShort(w,h);
        grid.setGridBounds(new Bounds(0, w*pixelSize, 0, h*pixelSize, 0, pixelSize)); 
        grid.setAttributeDesc( GridDataDesc.getDefaultAttributeDesc(16));
        short data[] = image.getData();
        int h1 = h-1;
        // Need to convert from 0,0 upper left to 0,0 lower left
        for(int y=0; y < h; y++) {
            int x0 = y * w;
            for(int x=0; x < w; x++) {
                long d = data[x + x0] & MAX_USHORT;
                if(invertImage) d = MAX_USHORT - d;
                grid.setAttribute(x, h1 - y, d);
            }
        }

        return grid;
    }

    /**
     * Write a debug version of the map out in a range of 0-255 grey per channel
     * Convert back from Grid coordinates to Java image upper left 0,0
     *
     * @param fileName
     * @throws IOException
     */
    public static void write(Grid2D grid, String fileName) throws IOException {
        int w = grid.getWidth();
        int h = grid.getHeight();


        BufferedImage outImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        DataBufferInt dbi = (DataBufferInt)(outImage.getRaster().getDataBuffer());

        int[] imageData = dbi.getData();

        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){

                int cc = us2i((short)grid.getAttribute(x,h-y-1));
                int gray =  (int)((cc / 65535.0) * 255);

                imageData[x + y * w] = makeColor(gray);
            }
        }

        ImageIO.write(outImage, "PNG", new File(fileName));
    }


    private static final int makeColor(int gray){

        return 0xFF000000 | (gray << 16) | (gray << 8) | gray;

    }
}
