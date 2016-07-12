/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.io.output;

import java.io.IOException;
import java.io.File;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.RenderingHints;

import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.util.Arrays;

import javax.imageio.ImageIO;

import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import abfab3d.core.LongConverter;
import abfab3d.util.DefaultLongConverter;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
 * Export grid into set of slice files in PNG format 
 *  
 * @author Vladimir Bulatov
 */
public class SlicesWriter {

    static final boolean DEBUG = true;
    static int debugCount = 100;
    public static final int 
        AXIS_X=0,
        AXIS_Y=1,
        AXIS_Z=2;

    static final int COLOR_WHITE = makeColor(0xFF);
    static final int COLOR_BLACK = makeColor(0);
    static final int COLOR_GRAY = makeColor(127);
    static final byte GL1[] = new byte[]{0, (byte)255};
    static final byte GL2[] = new byte[]{0, (byte)(255/3), (byte)(2*255/3), (byte)(255)};
    static final byte GL4[] = new byte[]{0,                (byte)(255/15), (byte)(2*255/15), (byte)(3*255/15), (byte)(4*255/15), (byte)(5*255/15), (byte)(6*255/15), (byte)(7*255/15),
                                         (byte)(8*255/15), (byte)(9*255/15), (byte)(10*255/15), (byte)(11*255/15), (byte)(12*255/15), (byte)(13*255/15), (byte)(14*255/15),(byte)(255)};    
    static final IndexColorModel PALETTE1 = new IndexColorModel(1,2,GL1,GL1,GL1);
    static final IndexColorModel PALETTE2 = new IndexColorModel(2,4,GL2,GL2,GL2);
    static final IndexColorModel PALETTE4 = new IndexColorModel(4,16,GL4,GL4,GL4);
    static final IndexColorModel PALETTES[] = new IndexColorModel[]{PALETTE1, PALETTE2, PALETTE4 };


    String m_imageFileType = "png";
    String m_filePattern = "slice_%04d." + m_imageFileType;

    int imgCellSize = 1;  // size of grid cell to write to 
    int imgVoxelSize = 1; // 
    int m_subvoxelResolution=0;
    int m_backgroundColor = COLOR_WHITE; // solid white 
    int m_foregroundColor = COLOR_BLACK; // solid black

    int xmin=-1, xmax=-1, ymin=-1, ymax=-1, zmin=-1, zmax=-1;
    double m_levels[] = new double[0];
    Grid m_grid;

    Color m_levelsColor = Color.RED;

    boolean m_writeVoxels = true;
    boolean m_writeLevels = false;

    double m_levelLineWidth = 3.;
    
    LongConverter m_dataConverter = new DefaultLongConverter();
    LongConverter m_colorMaker = new DefaultColorMaker();

    /** Skip if the slice % modSkip == 0 and modeSkip != 0 */
    int m_modSkip;

    public void setBounds(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax){

        this.xmin = xmin;
        this.ymin = ymin;
        this.zmin = zmin;
        this.xmax = xmax;
        this.ymax = ymax;
        this.zmax = zmax;
    }

    public void setImageFileType(String ending) {
        m_imageFileType = ending;
    }

    public void setModSkip(int skip) {
        m_modSkip = skip;
    }

    /**
       set level for topographical map output 
     */
    public void setLevels(double levels[]){
        m_levels = new double[levels.length];
        System.arraycopy(levels, 0, m_levels, 0, levels.length);
    }

    public void setWriteVoxels(boolean value){
        m_writeVoxels = value;
    }

    public void setWriteLevels(boolean value){
        m_writeLevels = value;
    }

    public void setDataConverter(LongConverter dataConverter){
        m_dataConverter = dataConverter;
    }
    public void setColorMaker(LongConverter colorMaker){
        m_colorMaker = colorMaker;
    }

    public void setMaxAttributeValue(int value){

        m_subvoxelResolution = value;

    }

    public void setSubvoxelResolution(int value){

        m_subvoxelResolution = value;
        
    }

    public void setBackgroundColor(int color){
        m_backgroundColor = color;
    }

    public void setForegroundColor(int color){
        m_foregroundColor = color;
    }
    public void setCellSize(int size){
        imgCellSize = size;
    }

    public void setVoxelSize(int size){
        
        imgVoxelSize = size;
    }

    public void setFilePattern(String pattern){

        m_filePattern = pattern;        

    }

    /**

     */
    public void writeSlices(AttributeGrid grid, 
                            String fileTemplate, 
                            int firstSlice, 
                            int firstFile, 
                            int sliceCount) throws IOException {        
        writeSlices(grid, fileTemplate, firstSlice, firstFile, sliceCount, 1,8, new DefaultLongConverter());
    }

    /**
       Writes series of slices from grid into a image file 

       @param grid to write from slices from 
       @param fileTemplate C style template used to make path for individual files (for example "/tmp/slice%03d.png")
       @param firstSlice  index of fist slice 
       @param firstFile   index of fist file (file numbers can be different from slices numbers)
       @param sliceCount hoew many slices to write        
       @param orientation - axis orthogonal to the slices (AXIS_X, AXIS_Y, AXIS_Z)
       @param voxelBitCount - cont of bits to be used for each pixel
       @param voxelDataConverter - converter to convert from gird attribute into pixel value       
     */
    public void writeSlices(AttributeGrid grid, 
                            String fileTemplate, 
                            int firstSlice, 
                            int firstFile, 
                            int sliceCount, 
                            int orientation, 
                            int voxelBitCount, 
                            LongConverter voxelDataConverter) throws IOException {
        
        if(DEBUG) printf("SlicesWriter.writeSlices(%s)\n",fileTemplate);
        int imgSize[] = getSliceSize(grid, orientation);
        int voxelByteCount = getVoxelByteCount(voxelBitCount);
        int dataBitCount = getDataBitCount(voxelBitCount);
        BufferedImage outImage = makeImage(imgSize[0], imgSize[1], voxelBitCount);
        if(DEBUG) printf("outImage: %s\n", outImage);        
        // images are created with byte buffer 
        DataBufferByte dataBuffer = (DataBufferByte)(outImage.getRaster().getDataBuffer());

        if(DEBUG) printf("DataBuffer: %s\n", dataBuffer);

        byte[] sliceData = dataBuffer.getData();
        if(DEBUG) printf("sliceData: %d\n", sliceData.length);
        
        
        for(int i = 0; i < sliceCount; i++){

            int slice = i + firstSlice; 
            int findex = i + firstFile;
            String fname = fmt(fileTemplate, findex);
            makeSliceData(imgSize[0], imgSize[1], slice, orientation, grid, voxelBitCount, voxelDataConverter, sliceData, dataBitCount);

            ImageIO.write(outImage, m_imageFileType, new File(fname));
        }        
    }

    
    /**
       write single pixel slices to zip 
     */
    public void writeSlices(AttributeGrid grid, OutputStream zipOut, String fileTemplate,
                            int firstSlice, int firstFile, int sliceCount) throws IOException {
        writeSlices(grid, zipOut, fileTemplate, firstSlice, firstFile, sliceCount, 1,8, new DefaultLongConverter());
    }

    public void writeSlices(AttributeGrid grid, OutputStream os, String fileTemplate,
                            int firstSlice, int firstFile, int sliceCount, int orientation, int voxelBitCount, LongConverter voxelDataConverter) throws IOException {

        if(DEBUG) printf("SlicesWriter.writeSlices(%s)\n",fileTemplate);

        int imgSize[] = getSliceSize(grid, orientation);
        int voxelByteCount = getVoxelByteCount(voxelBitCount);
        int dataBitCount = getDataBitCount(voxelBitCount);
        BufferedImage outImage = makeImage(imgSize[0], imgSize[1], voxelBitCount);

        DataBuffer db = outImage.getRaster().getDataBuffer();
        if(DEBUG) printf("DataBuffer: %s\n", db);
        
        DataBufferByte dbi = (DataBufferByte)(db);
        byte[] sliceData = dbi.getData();
        
        for(int i = 0; i < sliceCount; i++){
            
            int slice = i + firstSlice; 
            int findex = i + firstFile;
            String fname = fmt(fileTemplate, findex);
            makeSliceData(imgSize[0], imgSize[1], slice, orientation, grid, voxelBitCount, voxelDataConverter, sliceData, dataBitCount);
            if (os instanceof ZipOutputStream) {
                ZipEntry ze = new ZipEntry(fname);
                ((ZipOutputStream)os).putNextEntry(ze);
            }
            ImageIO.write(outImage, m_imageFileType, os);
            if (os instanceof ZipOutputStream) {
                ((ZipOutputStream)os).closeEntry();
            }
        }        
    }
   
    /**
       convert grid data into single slice 
       
     */
    void makeSliceData(int  width, int height, int slice, int orientation, 
                       AttributeGrid grid, int voxelBitCount, LongConverter voxelDataConverter, 
                       byte[] sliceData, int sliceBitCount) {
        
        int coord[] = new int[3];

        int bytesPerVoxel = getVoxelByteCount(voxelBitCount);
     
        int widthBytes = width*bytesPerVoxel;
        
        if(sliceBitCount < 8) {
            switch(sliceBitCount){
                // special cases to be treated separately have 1, 2, 4 bit png 
            case 1: widthBytes = (width+7)/8; break;
            case 2: widthBytes = (width+3)/4; break;
            case 4: widthBytes = (width+1)/2; break;
                
            }
        }
        
        for(int y = 0; y < height; y++){

            int pos = y * widthBytes;
            // used if dataBits < 8
            int shift = (8-sliceBitCount); 
            int currentByte = 0;
            for(int x = 0; x < width; x++ ){

                // coordinates of voxel in the grid depend on orientation 
                getVoxelCoord(slice, x,y, coord, orientation);
                // voxel data converted to format to be written 
                long vdata = voxelDataConverter.get(grid.getAttribute(coord[0],coord[1],coord[2])); 

                if(sliceBitCount < 8) {
                    currentByte |= (byte)(vdata << (shift));
                    shift -= sliceBitCount;
                    if(shift < 0){
                        shift = (8-sliceBitCount);
                        sliceData[pos] = (byte)(currentByte);
                        pos++;
                        currentByte = 0;
                    }                    
                } else if(bytesPerVoxel == 1) {

                    sliceData[pos++] = (byte)(vdata & 0xFF);

                } else { // sliceDataBits > 8
                    
                    for(int b = 0; b < bytesPerVoxel; b++){
                        
                        sliceData[pos++] = (byte)( vdata & 0xFF);
                        vdata = (vdata >> 8);
                        
                    }                                        
                }
            }
            //
            // last incomplete byte if needed 
            //
            if(sliceBitCount < 8 ) {
                if(shift < (8-sliceBitCount)) {
                    // we potentially have unwritten currentByte 
                    sliceData[pos] = (byte)(currentByte);
                }
            }            
        }                
    }

    /**
       makes mask with given bit count 
     */
    static final long getDataMask(int bitCount){
        long mask = 0;
        for(int i = 0; i < bitCount; i++){
            mask |= (1L << i);
        }
        printf("mask:0x%X\n", mask);
        return mask;
    }


    /**
       calculate slice size according to the slice orinetation 
     */
    static final int[] getSliceSize(AttributeGrid grid, int orientation){

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        switch(orientation){
        default:
        case 0: return new int[]{ny,nz};
        case 1: return new int[]{nx,nz};
        case 2: return new int[]{nx,ny};
        }
    }

    /**
     *
     *  makes image to held appropriate number of data bits 
     *
     */
    static BufferedImage makeImage(int imageWidth, int imageHeight, int voxelBitCount){

        if(DEBUG) printf("makeImage(%d, %d, %d)\n", imageWidth, imageHeight, voxelBitCount);
        switch(voxelBitCount){
        case 1:
            return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_BINARY);
        case 2:
            return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_BINARY, makeGrayPalette(2, 4));
        case 3:
            return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_INDEXED, makeGrayPalette(8, 8));
        case 4:
            return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_BINARY, makeGrayPalette(4, 16));
        case 5:
            return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_INDEXED, makeGrayPalette(8, 32));
        case 6:
            return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_INDEXED, makeGrayPalette(8, 64));
        case 7:
            return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_INDEXED, makeGrayPalette(8, 128));
        default: 
            int voxelByteCount = getVoxelByteCount(voxelBitCount);            
            switch(voxelByteCount){
            case 1: return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
            case 3: return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
            case 4: return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
            default: throw new RuntimeException(fmt("unsupported data output voxelBitCount: %d voxelByteCount:%d", voxelBitCount,voxelByteCount));                 
            }
        }        
    }

    /**
       makes indexed color model with gray palette 
     */
    static IndexColorModel makeGrayPalette(int bitCount, int size){
        if(DEBUG) printf("makeGrayPalette(%d, %d)\n", bitCount, size);
        byte gray[] = new byte[size];
        for(int k = 0; k < size; k++){
            gray[k] = (byte)(k*255/(size-1));
        }
        return new IndexColorModel(bitCount, size, gray, gray, gray);
        
    }

    /**
    static final int getImageType(int bitCount){

        int byteCount = getByteCount(bitCount);
        
        int bits = bitCount % 8;

        switch(byteCount){           
        default: throw new RuntimeException(fmt("unsupported count of bits per voxel: %d", bitCount)); 
            
        case 1: 
            switch(bits){
            case 1: return BufferedImage.TYPE_BYTE_BINARY;
            case 2: return BufferedImage.TYPE_BYTE_BINARY;
            case 4: return BufferedImage.TYPE_BYTE_BINARY;
            default: return BufferedImage.TYPE_BYTE_GRAY;
            }            
        case 2: 
        case 3: return BufferedImage.TYPE_3BYTE_BGR;
        case 4: return BufferedImage.TYPE_4BYTE_ABGR;        
        }  
    }
    */

    /**
       returns number of bytes necessary to represent given voxel data
     */
    static final int getVoxelByteCount(int bitCount){       

        int c = (bitCount+7) / 8;        
        if(c == 2) c = 3; // store 2 bytes data in 3 bytes RGB. May be changed to 2 bytes later 
        return c;
    }


    /**
       return count of data bits stored in output slice byte
     */
    static final int getDataBitCount(int voxelBitCount){
        switch(voxelBitCount){
        case 1: return 1;
        case 2: return 2;
        case 4: return 4;
        default: return 8;
        }
    }


    /**
       convert imafge coordinates nto voxel coordinates according to orientation 
     */
    static final void getVoxelCoord(int slice, int i, int j, int coord[], int orientation){
        switch(orientation){
        case 0: coord[0] = slice; coord[1] = i; coord[2] = j; break;
        case 1: coord[0] = i; coord[1] = slice; coord[2] = j; break;
        case 2: coord[0] = i; coord[1] = j; coord[2] = slice; break;
        }            
    }


    /**
       writes colored slices into set of files according to current settigns 
     */
    public void writeSlices(Grid grid) throws IOException {

        if(DEBUG) printf("%s.writeSlices()\n", this.getClass().getName());

        m_grid = grid;

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        if(xmax < 0)xmax = nx;
        if(ymax < 0)ymax = ny;
        if(zmax < 0)zmax = nz;
        if(xmin < 0 )xmin = 0;
        if(ymin < 0 )ymin = 0;
        if(zmin < 0 )zmin = 0;

        xmin = clamp(xmin, 0, nx);
        xmax = clamp(xmax, 0, nx);
        ymin = clamp(ymin, 0, ny);
        ymax = clamp(ymax, 0, ny);
        zmin = clamp(zmin, 0, nz);
        zmax = clamp(zmax, 0, nz);

        if(xmin >= xmax || ymin >= ymax || zmin >= zmax){
            throw new IllegalArgumentException(fmt("bad grid export bounds[xmin:%d xmax:%d, ymin:%d ymax:%d zimn:%d, zmax:%d]\n",
                                                   xmin, xmax, ymin, ymax, zmin, zmax));
        }


        int imgWidth = (xmax-xmin)*imgCellSize;
        int imgHeight = (ymax-ymin)*imgCellSize;

        if(DEBUG) printf("slice image size:[%d x %d]\n", imgWidth, imgHeight);
            
        BufferedImage outImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = null;
        if(m_writeLevels){
            graphics = outImage.createGraphics();
            graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setStroke(new BasicStroke((float)m_levelLineWidth, BasicStroke.CAP_ROUND,  BasicStroke.JOIN_ROUND));
        }

        DataBufferInt dbi = (DataBufferInt)(outImage.getRaster().getDataBuffer());
        int[] imageData = dbi.getData();

        int skip = 0;
        for(int z = zmin; z < zmax; z++){

            if (m_modSkip != 0 && (skip > 0)) {
                skip--;
                continue;
            }
            if (m_modSkip != 0 && (skip == 0)) {
                skip = m_modSkip;
            }


            Arrays.fill(imageData, m_backgroundColor);

            if(m_writeVoxels){
                for(int y = ymin; y < ymax; y++){
                    for(int x = xmin; x < xmax; x++){                        
                        int cc = getVoxelColor(x,y,z);
                        
                        int ix = x-xmin;
                        int iy = y-ymin;

                        int ix0 = ix*imgCellSize;
                        int ix1 = ix0 + imgVoxelSize;
                        int iy0 = iy*imgCellSize;
                        int iy1 = iy0 + imgVoxelSize;
                        
                        for(int yy = iy0; yy < iy1; yy++) {
                            int yy0 = yy*imgWidth;
                            for(int xx = ix0; xx < ix1; xx++) {
                                imageData[xx + yy0] = cc;
                            }
                        }
                    }
                } // y cycle
            } // if(m_writeVoxels)
            
            
            if(m_writeLevels){

                graphics.setPaint(m_levelsColor);
                double values[] = new double[4];
                int ymax1 = ymax-1; // no processing of the last raw and column 
                Point2D points[] = new Point2D[4];
                for(int i = 0; i < 4; i++){
                    points[i] = new Point2D.Double();
                }
                int xmax1 = xmax-1;
                
                for(int y = ymin; y < ymax1; y++){
                    for(int x = xmin; x < xmax1; x++){
                        
                        values[0] = getVoxelValue(x,y,z);
                        values[1] = getVoxelValue(x+1,y,z);
                        values[2] = getVoxelValue(x+1,y+1,z);
                        values[3] = getVoxelValue(x,y+1,z);
                        
                        
                        double ix0 = (x-xmin)*imgCellSize + imgCellSize/2;
                        double iy0 = (y-ymin)*imgCellSize + imgCellSize/2;
                        double ix1 = ix0 + imgCellSize;
                        double iy1 = iy0 + imgCellSize;
                        points[0].setLocation(ix0, iy0);
                        points[1].setLocation(ix1, iy0);
                        points[2].setLocation(ix1, iy1);
                        points[3].setLocation(ix0, iy1);
                        drawLevels(graphics, values, points, m_levels);
                        //graphics.drawLine(ix0, iy0, ix1, iy1);
                        
                    }
                }                
            }

            String fileName = fmt(m_filePattern, (z-zmin));
            if(DEBUG)printf("slice: %s\n", fileName);

            ImageIO.write(outImage, m_imageFileType, new File(fileName));
            
        } // zcycle 
                
    } //    writeSlices(AttributeGtrid grid){   

    /**
       draw contours in rectangle 
     */
    void drawLevels(Graphics2D graphics, double values[], Point2D points[], double levels[]){

        for(int i = 0; i < levels.length; i++){
            drawLevel(graphics, values, points, levels[i]);
        }                                        
    }

    
    void drawLevel(Graphics2D graphics, double v[], Point2D p[], double level){
                    
        int count = 0;
        Point2D pnts[] = null;

        for(int i = 0; i < 4; i++){
            
            int i1 = (i+1)%4;
            
            if((v[i] - level) * (level - v[i1])  >= 0.){
                
                if(pnts == null){
                    pnts = new Point2D[2];
                    count = 0;
                }
                pnts[count++] = lerp(p[i], p[i1], v[i], v[i1], level);
                
                if(DEBUG && v[0] > 0 &&  debugCount-- > 0){
                    printf("[%7.3f %7.3f %7.3f  ->(%7.3f %7.3f)\n", v[i], v[i1], level,pnts[count-1].getX(),pnts[count-1].getY());
                }
                
                if(count == 2){
                    Line2D line = new Line2D.Double(pnts[0].getX(), pnts[0].getY(), pnts[1].getX(), pnts[1].getY());
                    graphics.draw(line);
                    count = 0;
                }                
            }
        }                                           
    }
    
    /**
       linear interpolation between 2 points of linear function
       linear function:P(v0) = p0, P(v1) = p1. 
       
       @return value P(v) 
      
     */
    static Point2D lerp(Point2D p0, Point2D p1, double v0, double v1, double v){

        double t0 = (v1 - v)/(v1 - v0);
        double t1 = (v - v0)/(v1 - v0);

        return new Point2D.Double(t0*p0.getX() + t1*p1.getX(), t0*p0.getY() + t1*p1.getY());

    }

    /**
       returns color to be used for given vocxel
    */
    int getVoxelColor(int x, int y, int z){

        switch(m_subvoxelResolution){
        case 0: // use grid state 
            {
                switch(m_grid.getState(x,y,z)){
                default:
                case Grid.OUTSIDE:
                    return (int)m_colorMaker.get(0);
                case Grid.INSIDE:
                    return (int)m_colorMaker.get(1);
                }
            }
        default: // use grid attribute 

            long a = m_dataConverter.get(((AttributeGrid)m_grid).getAttribute(x,y,z));
            return (int)m_colorMaker.get(a);
        }
    }

    long getVoxelValue(int x, int y, int z){
        
        
        switch(m_subvoxelResolution){
        case 0: // use grid state 
            {
                switch(m_grid.getState(x,y,z)){
                default:
                case Grid.OUTSIDE:
                    return 0;
                case Grid.INSIDE:
                    return 1;
                }
            }
        default: // use grid attribute 
            return m_dataConverter.get(((AttributeGrid)m_grid).getAttribute(x,y,z));            
        }
    }
    
    static final int makeColor(int gray){

        return 0xFF000000 | (gray << 16) | (gray << 8) | gray;

    }

    static final int makeNegativeColor(int gray){

        return 0xFF000000 | gray;

    }

    class DefaultColorMaker  implements LongConverter {

        public final long get(long a){
            if (m_subvoxelResolution == 0) {
                if (a == Grid.INSIDE) {
                    return makeColor(0);
                } else {
                    return m_backgroundColor;
                }
            }

            int  level = (int)(((m_subvoxelResolution - a) * 255)/m_subvoxelResolution);                
            if(level == 255) // return background color for max value 
                return m_backgroundColor;
            else 
                return makeColor(level);
        }
    }
}
