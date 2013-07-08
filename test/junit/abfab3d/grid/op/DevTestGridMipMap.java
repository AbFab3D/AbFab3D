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

package abfab3d.grid.op;

// External Imports
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

// Internal Imports
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.io.output.SlicesWriter;

import abfab3d.util.Vec;
import abfab3d.util.VecTransform;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Units.MM;

import static java.lang.Math.sin;
import static java.lang.Math.PI;

/**
 *
 * @author Vladimir Bulatov
 * @version
 */
public class DevTestGridMipMap {
    
    static void testMipMapCreation() throws IOException{

        int nx = 250, ny = 200, nz = 200;
        int margin = 5;
        int maxAttributeValue = 255;
        double voxelSize = 20*MM/nx;
        
        AttributeGrid grid = makeBlock(new ArrayAttributeGridByte(1,1,1,voxelSize, voxelSize), nx, ny, nz, margin, maxAttributeValue);        
        
        GridMipMap mm = new GridMipMap(grid);
        if(true){
            SlicesWriter slicer = new SlicesWriter();
            for(int k = 1; k < mm.getLevelsCount(); k++){
                String pattern = fmt("/tmp/slices/slice%d_%%03d.png", k);
                slicer.setFilePattern(pattern);
                int cs = 1<<(k+1);
                slicer.setCellSize(cs);
                slicer.setVoxelSize(cs/2);        
                slicer.setMaxAttributeValue(maxAttributeValue);
                slicer.writeSlices(mm.getLevel(k));     
            }
        }   
        
    }

    static double boxWidth = 20*MM;
    static double boxHeight = 20*MM;

    static void testMipMapInterpolation() throws IOException{

        int nx = 256, ny = 256, nz = 128;
        int bandDistance = 32;
        int bandWidth = 7;
        int maxAttributeValue = 255;

        double voxelSize = 0.1*MM;

        boxWidth = voxelSize * nx;
        boxHeight = voxelSize * ny;

        
        double bounds[] = new double[]{0,nx*voxelSize,0,ny*voxelSize,0,nz*voxelSize};
        
        //AttributeGrid grid = makeBlock(new ArrayAttributeGridByte(1,1,1,voxelSize, voxelSize), nx, ny, nz, margin, maxAttributeValue);        
        AttributeGrid grid = makeBands(new ArrayAttributeGridByte(1,1,1,voxelSize, voxelSize), nx, ny, nz, bandDistance, bandWidth, maxAttributeValue);        
        grid.setGridBounds(bounds);

        GridMipMap mipmap = new GridMipMap(grid);
        mipmap.setMaxAttribute(maxAttributeValue);
        mipmap.setInterpolationType(GridMipMap.INTERPOLATION_LINEAR);
        //mipmap.setInterpolationType(GridMipMap.INTERPOLATION_BOX);
        //mipmap.setScalingType(GridMipMap.SCALING_AVERAGE);
        mipmap.setScalingType(GridMipMap.SCALING_MAX);

        GridMaker gm = new GridMaker();
        
        //bounds[3] /= 2;
        gm.setBounds(bounds);
        AttributeGrid grid1 = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        gm.setDataSource(mipmap);
        gm.setTransform(new VoxelSizeTransformer());

        gm.setVoxelSize(voxelSize);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setMargin(0);

        gm.makeGrid(grid1);

        if(true){
            SlicesWriter slicer = new SlicesWriter();
            slicer.setFilePattern("/tmp/slices/slice_%03d.png");
            slicer.setCellSize(4);
            slicer.setVoxelSize(3);        
            slicer.setMaxAttributeValue(maxAttributeValue);
            slicer.writeSlices(grid1);     
        }
        
    }

    static class VoxelSizeTransformer implements VecTransform {

        public int transform(Vec in, Vec out){
            out.set(in);
            return RESULT_OK;
        }
        public int inverse_transform(Vec in, Vec out){
            out.set(in);
            out.setVoxelSize(in.getVoxelSize() * (25*(1-in.v[0]/boxWidth)));
            return RESULT_OK;
        }
    }


    static AttributeGrid makeBlock(AttributeGrid g, int nx, int ny, int nz, int margin, int attributeValue) {

        AttributeGrid grid = (AttributeGrid)g.createEmpty(nx, ny, nz, g.getVoxelSize(), g.getSliceHeight());
        int xmin = margin;
        int xmax  = nx - xmin;
        int ymin = margin;
        int ymax  = ny - margin;
        int zmin = margin;
        int zmax  = nz - zmin;

        for (int y = ymin; y < ymax; y++) {
            for (int x = xmin; x < xmax; x++) {
                for (int z = zmin; z < zmax; z++) {
                    grid.setAttribute(x,y,z, attributeValue);
                }
            }
        }
        return grid;
    }


    static AttributeGrid makeBands(AttributeGrid g, int nx, int ny, int nz, int bandDistance, int bandWidth, int attributeValue) {

        AttributeGrid grid = (AttributeGrid)g.createEmpty(nx, ny, nz, g.getVoxelSize(), g.getSliceHeight());
        int barCount = bandDistance/bandWidth;
        
        int xmin = 0;
        int xmax  = nx;
        int ymin = 0;
        int ymax  = ny;
        int zmin = 0;
        int zmax  = nz;

        for (int y = ymin; y < ymax; y++) {
            double sy = sin((2*PI * y / bandDistance));

            for (int z = zmin; z < zmax; z++) {
                double sz = sin((2*PI * z / bandDistance));

                for (int x = xmin; x < xmax; x++) {
                    double sx = sin((2*PI * x / bandDistance));

                    //double s = sin( 1000./(2*PI * y / bandDistance));
                    //double s = sin( 100./(2*PI * y / bandDistance));
                    double s = sx*sy +sy*sz + sz*sx;
                    if(s > 0){
                        grid.setAttribute(x,y,z, (long)(s*attributeValue));
                    }
                    if(false){
                        int xx = y % bandDistance;
                        if(xx < bandWidth){
                            grid.setAttribute(x,y,z, attributeValue);
                        }
                    }
                }
            }
        }
        return grid;
    }

    static void testMinMax(){
        double v = Double.MAX_VALUE;
        double v1 = -Double.MAX_VALUE;
        printf("max value:  %18.15e\n", v);
        printf("-max value: %18.15e\n", v1);
    }


    public static void main(String[] args) throws Exception{
        
        //testMipMapCreation();
        //testMipMapInterpolation();
        testMinMax();

    }
}
