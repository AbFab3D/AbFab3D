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

package abfab3d.grid;

import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;


import abfab3d.core.Grid2D;
import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataChannel;
import abfab3d.param.Parameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;

import abfab3d.util.ColorMapper;
import abfab3d.util.ColorMapperDensity;
import abfab3d.util.ColorMapperDistance;

import static abfab3d.core.MathUtil.lerp2;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
   class to write grid into bunch of individual image files 
   using given data channel and colorizer 
 */
public class GridDataWriter extends BaseParameterizable {
    
    static final boolean DEBUG = false;

    public static final int 
        TYPE_DENSITY = 0, 
        TYPE_DISTANCE = 1;
       
    protected IntParameter mp_type = new IntParameter("type", TYPE_DISTANCE);
    protected IntParameter mp_slicesStep = new IntParameter("slicesStep", 1);
    protected IntParameter mp_magnification = new IntParameter("magnification", 1);
    protected DoubleParameter mp_distanceStep = new DoubleParameter("distanceStep",0.001);
    protected DoubleParameter mp_densityStep = new DoubleParameter("densityStep",0.5);
    //protected StringParameter mp_pathFormat = new StringParameter("pathFormat","/tmp/slice%03d.png");
    
    static final int DENSITY_COLOR_1 = 0xFFFFFFFF;
    static final int DENSITY_COLOR_0 = 0xFF000000;
    static final int DISTANCE_COLOR_0 = 0xFF000000;
    
    static final int DIST_INT_COLOR_0  = 0xFFDD0000;
    static final int DIST_INT_COLOR_1  = 0xFFFFDDDD;
    static final int DIST_EXT_COLOR_0  = 0xFF00DD00;
    static final int DIST_EXT_COLOR_1  = 0xFFDDFFDD;

    Parameter m_params[] = new Parameter[]{
        mp_type,
        mp_distanceStep,
        mp_densityStep,
        mp_magnification,
        mp_slicesStep,               
    };

    public GridDataWriter(){
        super.addParams(m_params);
    }

    public void printSlices(AttributeGrid grid, String format){
        
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        for(int z = 0; z < nz; z++){
            printf("z: %d\n",z);
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    long a = grid.getAttribute(x,y,z);
                    printf(format, a);
                }
                printf("\n");
            }            
            printf("\n");
        } 
    }

    public void writeSlices(AttributeGrid grid, GridDataChannel dataChannel, String pathFormat) {

        int magnification = mp_magnification.getValue();

        int imgx = grid.getWidth()*magnification;
        int imgy = grid.getHeight()*magnification;
        int nz = grid.getDepth();
        ColorMapper colorMapper = null;
        //double dataStep = 1.;
        switch(mp_type.getValue()){
        default:
        case TYPE_DENSITY:
            colorMapper = new  ColorMapperDensity(DENSITY_COLOR_0, DENSITY_COLOR_1, mp_densityStep.getValue());
            break;
        case TYPE_DISTANCE:
            colorMapper = new  ColorMapperDistance(DIST_INT_COLOR_0, DIST_INT_COLOR_1, DIST_EXT_COLOR_0, DIST_EXT_COLOR_1, mp_distanceStep.getValue());
            break;

        }

        BufferedImage image =  new BufferedImage(imgx, imgy, BufferedImage.TYPE_INT_ARGB);
        int slicesStep = mp_slicesStep.getValue();

        for(int z = 0; z < nz; z += slicesStep){
            
            makeSlice(grid, mp_magnification.getValue(), z, dataChannel, colorMapper, image);
            String path = fmt(pathFormat, z);
            if(DEBUG)printf("writing slice %d inot file: %s\n", z, path);
            try {
                ImageIO.write(image, "png", new File(path));        
            } catch(IOException e){
                throw new RuntimeException(fmt("failed to write \"%s\"",path));
            }            
        }

    }
    

    public void writeSlices(Grid2D grid, GridDataChannel dataChannel, String pathFormat) {

        int magnification = mp_magnification.getValue();

        int imgx = grid.getWidth()*magnification;
        int imgy = grid.getHeight()*magnification;
        int z = 0; // z-slice coord 
        ColorMapper colorMapper = null;
        switch(mp_type.getValue()){
        default:
        case TYPE_DENSITY:
            colorMapper = new  ColorMapperDensity(DENSITY_COLOR_0, DENSITY_COLOR_1, mp_densityStep.getValue());
            break;
        case TYPE_DISTANCE:
            colorMapper = new  ColorMapperDistance(DIST_INT_COLOR_0, DIST_INT_COLOR_1, DIST_EXT_COLOR_0, DIST_EXT_COLOR_1, mp_distanceStep.getValue());
            break;

        }

        BufferedImage image =  new BufferedImage(imgx, imgy, BufferedImage.TYPE_INT_ARGB);
        int slicesStep = mp_slicesStep.getValue();

        makeSlice(grid, mp_magnification.getValue(), dataChannel, colorMapper, image);
        String path = fmt(pathFormat, z);
        if(DEBUG)printf("writing slice %d into file: %s\n", z, path);
        try {
            ImageIO.write(image, "png", new File(path));        
        } catch(IOException e){
            throw new RuntimeException(fmt("failed to write \"%s\"",path));
        }                
    }

    public static void makeSlice(AttributeGrid grid, int magnification, int iz, GridDataChannel dataChannel, ColorMapper colorMapper, BufferedImage image) {

        int gnx = grid.getWidth();
        int gny = grid.getHeight();
        int inx = gnx*magnification;
        int iny = gny*magnification;
        int nz = grid.getDepth();

        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();

        int[] sliceData = db.getData();

        double pix = 1./magnification;

        for(int iy = 0; iy < iny; iy++){

            double y = (iy+0.5)*pix - 0.5;

            for(int ix = 0; ix < inx; ix++){

                double x = (ix+0.5)*pix-0.5;
                int gx = (int)Math.floor(x);
                int gy = (int)Math.floor(y);
                double dx = x - gx;
                double dy = y - gy;
                //if(ix < magnification/2 && iy < magnification/2)
                //    printf("[%2d %2d](%4.2f %4.2f) ", gx, gy, dx, dy);
                int gx1 = clamp(gx + 1,0, gnx-1);
                int gy1 = clamp(gy + 1,0, gny-1);
                gx = clamp(gx,0, gnx-1);
                gy = clamp(gy,0, gny-1);
                long a00 = grid.getAttribute(gx,gy, iz);
                long a10 = grid.getAttribute(gx1,gy, iz);
                long a01 = grid.getAttribute(gx,gy1, iz);
                long a11 = grid.getAttribute(gx1,gy1, iz);

                double v00 = dataChannel.getValue(a00);
                double v10 = dataChannel.getValue(a10);
                double v01 = dataChannel.getValue(a01);
                double v11 = dataChannel.getValue(a11);

                double v = lerp2(v00, v10, v01, v11, dx, dy);
                sliceData[ix + (iny-1-iy)*inx] = colorMapper.getColor(v);
            }
        }
    }

    public static void makeSlice(Grid2D grid, int magnification, GridDataChannel dataChannel, ColorMapper colorMapper, BufferedImage image) {

        int gnx = grid.getWidth();
        int gny = grid.getHeight();
        int inx = gnx*magnification;
        int iny = gny*magnification;

        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();

        int[] sliceData = db.getData();

        double pix = 1./magnification;

        for(int iy = 0; iy < iny; iy++){

            double y = (iy+0.5)*pix - 0.5;

            for(int ix = 0; ix < inx; ix++){

                double x = (ix+0.5)*pix-0.5;
                int gx = (int)Math.floor(x);
                int gy = (int)Math.floor(y);
                double dx = x - gx;
                double dy = y - gy;
                int gx1 = clamp(gx + 1,0, gnx-1);
                int gy1 = clamp(gy + 1,0, gny-1);
                gx = clamp(gx,0, gnx-1);
                gy = clamp(gy,0, gny-1);
                long a00 = grid.getAttribute(gx,gy);
                long a10 = grid.getAttribute(gx1,gy);
                long a01 = grid.getAttribute(gx,gy1);
                long a11 = grid.getAttribute(gx1,gy1);

                double v00 = dataChannel.getValue(a00);
                double v10 = dataChannel.getValue(a10);
                double v01 = dataChannel.getValue(a01);
                double v11 = dataChannel.getValue(a11);

                double v = lerp2(v00, v10, v01, v11, dx, dy);
                sliceData[ix + (iny-1-iy)*inx] = colorMapper.getColor(v);
            }
        }
    }
    
}
