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

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;

import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;


/**
 * Export grid into set of slice files in PNG format 
 *  
 * @author Vladimir Bulatov
 */
public class SlicesWriter {

    static final boolean DEBUG = true;

    static final int COLOR_WHITE = makeColor(0xFF);
    static final int COLOR_BLACK = makeColor(0);
    static final int COLOR_GRAY = makeColor(127);


    String m_filePattern = "slice_%04d.png";
    String m_imageFileType = "png";
    
    int imgCellSize = 1;  // size of grid cell to write to 
    int imgVoxelSize = 1; // 
    int m_maxAttributeValue=0; 

    int xmin=-1, xmax=-1, ymin=-1, ymax=-1, zmin=-1, zmax=-1;

    AttributeGrid m_grid;
    
    public void setBounds(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax){

        this.xmin = xmin;
        this.ymin = ymin;
        this.zmin = zmin;
        this.xmax = xmax;
        this.ymax = ymax;
        this.zmax = zmax;

    }

    public void setMaxAttributeValue(int value){

        m_maxAttributeValue = value;

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

    public void writeSlices(AttributeGrid grid) throws IOException {   

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
            throw new IllegalArgumentException(fmt("band grid export bounds[xmin:%d xmax:%d, ymin:%d ymax:%d zimn:%d, zmax:%d]\n",
                                                   xmin, xmax, ymin, ymax, zmin, zmax));
        }


        int imgWidth = (xmax-xmin)*imgCellSize;
        int imgHeight = (ymax-ymin)*imgCellSize;

        if(DEBUG) printf("slice image size:[%d x %d]\n", imgWidth, imgHeight);
            
        BufferedImage outImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);

        DataBufferInt dbi = (DataBufferInt)(outImage.getRaster().getDataBuffer());
        int[] imageData = dbi.getData();
        
        for(int z = zmin; z < zmax; z++){

            Arrays.fill(imageData, COLOR_WHITE);
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
            String fileName = fmt(m_filePattern, (z-zmin));
            if(DEBUG)printf("slice: %s\n", fileName);

            ImageIO.write(outImage, m_imageFileType, new File(fileName));
            
        } // zcycle 
                
    } //    writeSlices(AttributeGtrid grid){   


    /**
       returns color to be used for given vocxel
    */
    int getVoxelColor(int x, int y, int z){

        switch(m_maxAttributeValue){
        case 0: // use grid state 
            {
                switch(m_grid.getState(x,y,z)){
                default:
                case Grid.OUTSIDE:
                    return COLOR_WHITE;
                case Grid.INTERIOR:
                    return COLOR_BLACK;
                case Grid.EXTERIOR:
                    return COLOR_GRAY;                    
                }
            }
        default: // use grid attribute 

            long a = clamp(m_grid.getAttribute(x,y,z), 0, m_maxAttributeValue);

            return makeColor( (int)(((m_maxAttributeValue - a) * 255)/m_maxAttributeValue));
            
        }
    }

    
    static final int makeColor(int gray){

        return 0xFF000000 | (gray << 16) | (gray << 8) | gray;

    }

}
