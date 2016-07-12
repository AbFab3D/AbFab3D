/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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

import java.util.Vector;

import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;

import abfab3d.core.AttributeGrid;


import static java.lang.Math.floor;
import static java.lang.Math.abs;
import static java.lang.Math.log;

import static abfab3d.core.Output.printf;

/**
   provides accress to a grid on several levels of details. 
   similar to mipmapping techniques used for texture mapping

   @author Vladimir Bulatov
 */
public class GridMipMap extends TransformableDataSource {
    
    static final boolean DEBUG = false;
    static int debugCount = 100;

    static public final int REPEAT_NONE = 0, REPEAT_X = 1, REPEAT_Y = 2, REPEAT_Z = 4, REPEAT_XYZ = 7;
 
    static final double LOG2 = log(2.);

    protected AttributeGrid m_grids[];
    // type of mip map interpolation
    static public final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1;
    // type of grid downsampling 
    static public final int SCALING_AVERAGE = 0, SCALING_MAX = 1;

    protected int m_interpolationType = INTERPOLATION_LINEAR;//INTERPOLATION_BOX;
    protected int m_repeatType = REPEAT_NONE;

    // what algorithm to use for grid downsampling 
    //protected int m_scalingType = SCALING_AVERAGE;
    protected int m_scalingType = SCALING_MAX;
    
    protected double scaleFactor;
    protected double xgmin, ygmin, zgmin;
    long m_maxAttribute = 255; // max value of attribute (for normalization)
    double m_normalization = 1.;

    // bounds of top level grid
    double gbounds[] = new double[6]; 
    AttributeGrid m_grid; 

    public GridMipMap(AttributeGrid grid){

        m_grid = grid;
        //new Exception().printStackTrace();
        
    }

    public int initialize(){
        
        super.initialize();

        m_normalization = 1./m_maxAttribute;
        
        createMipMap(m_grid);
        
        return ResultCodes.RESULT_OK;
    } 

    public void setScalingType(int type){
        m_scalingType = type;
    }

    public void setInterpolationType(int type){
        m_interpolationType = type;
    }

    /**
       allowed values REPEAT_NONE or bitwise combination of REPEAT_X, REPEAT_Y, REPEAT_Z 
     */
    public void setRepeatType(int type){
        m_repeatType = type;
    }

    public void setMaxAttribute(long value){
        m_maxAttribute  = value;
    }

    public void setSubvoxelResolution(long value){
        m_maxAttribute  = value;
    }
    

    protected void createMipMap(AttributeGrid grid){
        grid.getGridBounds(gbounds);
        xgmin = gbounds[0];
        ygmin = gbounds[2];
        zgmin = gbounds[4];

        scaleFactor = grid.getWidth()/(gbounds[1] - gbounds[0]);

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        
        if(DEBUG){
            printf("createMipMap()\n grid [%d x %d x %d]\n", nx, ny, nz);
        }
        Vector vgrids = new Vector();
        vgrids.add(grid);
        int levelCount = 1;
        while(nx > 1 || ny > 1 ||nz > 1){
            
            grid = makeGridHalfSize(grid, nx, ny, nz, m_scalingType);
            vgrids.add(grid);
            
            nx = (nx+1)/2;
            ny = (ny+1)/2;
            nz = (nz+1)/2;            

            if(DEBUG){
                printf("  mipmap level [%d x %d x %d]\n", nx, ny, nz);
            }
            levelCount++;
        }
        printf("  levelCount: %d\n", levelCount);

        m_grids = (AttributeGrid[])vgrids.toArray(new AttributeGrid[levelCount]);
        
    }

    
    /**      
       returns interpolated mipmaped value at point (x,y,z) 
       point and voxelSize is given in world coordinates 
    */
    public int getBaseValue(Vec pnt, Vec dataValue){

        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];

        //getValue(double x, double y, double z, double voxelSize){
        // move into grid coordinates
        double xg  = (x - xgmin)*scaleFactor;
        double yg  = (y - ygmin)*scaleFactor;
        double zg  = (z - zgmin)*scaleFactor;
        
        double vg = abs(pnt.getScaledVoxelSize()) * scaleFactor;
        
        if(vg <= 1.) {        
            dataValue.v[0] = m_normalization*getValue(m_grids[0], xg, yg, zg);
            return ResultCodes.RESULT_OK;
        } 

        int level = 0, maxLevel = m_grids.length-1;
        int scale  = 1;
                
        while((vg > 1.0) && (level < maxLevel)){
            
            vg /= 2;
            level++;
            scale *= 2;
        }
                
        //TODO interpolation between levels 
        double v0 = m_normalization*getValue(m_grids[level], xg/scale, yg/scale, zg/scale);
        if(false){
            dataValue.v[0] = v0;
            return ResultCodes.RESULT_OK;
        }
        if(level < maxLevel && m_interpolationType != INTERPOLATION_BOX){  
            // intrerpolate with next level 
            level--; scale /= 2;
            double v1 = m_normalization*getValue(m_grids[level], xg/scale, yg/scale, zg/scale);

            //double v = v1 * exp(2*(1-vg) * log(v0/v1));
            double lv = -log(vg)/LOG2;  // lv is in [0,1]

            double v = v1* lv + v0*(1-lv);
            
            if(DEBUG && debugCount-- > 0) {
                printf("     vg: %10.6f lv: %10.6f  v0: %10.6f v1: %10.6f v: %10.6f \n", vg, lv, v0, v1, v);
            }
            dataValue.v[0] = v;

        } else {
            // last level or INTERPOLATION_BOX 
            dataValue.v[0] = v0;
        }
        return ResultCodes.RESULT_OK;
    }
    

    /**
        Y
        |
         010__________ 110
        /            / |
       / |          /  | 
      /__|________ /   |
     011 |        111  |    
      |  |        |    |
      |  000_____ | __ 100________X 
      |  /        |   /
      | /         |  /
      |/          | /
      001-------- 101
     /    
    Z


     */
    
    static final int 
        B000 = 1 << 0, B100 = 1 << 1, B010 = 1 << 2,B110 = 1 << 3,
        B001 = 1 << 4, B101 = 1 << 5, B011 = 1 << 6,B111 = 1 << 7;
        

    static final int MASKX0 = ~(B000 | B010 | B011 | B001);
    static final int MASKX1 = ~(B100 | B110 | B111 | B101);
    static final int MASKY0 = ~(B000 | B100 | B101 | B001);
    static final int MASKY1 = ~(B010 | B110 | B111 | B011);
    static final int MASKZ0 = ~(B000 | B100 | B010 | B110);
    static final int MASKZ1 = ~(B001 | B101 | B011 | B111);
    
    static final int ALLCORNERS = 0xFF; 
    
    /**
       returns interpolated value from one grid; 
    */
    double getValue(AttributeGrid grid, double x, double y, double z){
        
        int 
            nx = grid.getWidth(),
            ny = grid.getHeight(),
            nz = grid.getDepth();
        
        // half voxel shift to get to the voxels centers 
        x -= 0.5;
        y -= 0.5;
        z -= 0.5;
        
        int 
            ix = (int)floor(x),
            iy = (int)floor(y),
            iz = (int)floor(z);
        
        int 
            ix1 = (ix+1),
            iy1 = (iy+1),
            iz1 = (iz+1);

        
        double 
            dx = x - ix,
            dy = y - iy,
            dz = z - iz,
            dx1 = 1. - dx,
            dy1 = 1. - dy,
            dz1 = 1. - dz;
        
        int mask = ALLCORNERS; // mask to calculate 8 corners of the cube 
        
        if((m_repeatType & REPEAT_X) != 0) {
            ix = reminder(ix, nx);
            ix1 = reminder(ix1, nx);
        } else {
            if(isOutside(ix,nx)) mask &= MASKX0;
            if(isOutside(ix1,nx)) mask &= MASKX1;
        }
        if((m_repeatType & REPEAT_Y) != 0) {
            iy = reminder(iy, ny);
            iy1 = reminder(iy1, ny);
        } else {
            if(isOutside(iy,ny)) mask &= MASKY0;
            if(isOutside(iy1,ny)) mask &= MASKY1;
        }
        
        if((m_repeatType & REPEAT_Z) != 0) {
            iz = reminder(iz, nz);
            iz1 = reminder(iz1, nz);
        } else {
            if(isOutside(iz,nz)) mask &= MASKZ0;
            if(isOutside(iz1,nz)) mask &= MASKZ1;
        }
        
        //try {
            long 
            v000 = ((mask & B000) != 0) ? grid.getAttribute(ix,  iy,  iz ): 0,  
            v100 = ((mask & B100) != 0) ? grid.getAttribute(ix1, iy,  iz ): 0, 
            v010 = ((mask & B010) != 0) ? grid.getAttribute(ix,  iy1, iz ): 0, 
            v110 = ((mask & B110) != 0) ? grid.getAttribute(ix1, iy1, iz ): 0,
            v001 = ((mask & B001) != 0) ? grid.getAttribute(ix,  iy,  iz1): 0,
            v101 = ((mask & B101) != 0) ? grid.getAttribute(ix1, iy,  iz1): 0,
            v011 = ((mask & B011) != 0) ? grid.getAttribute(ix,  iy1, iz1): 0,
            v111 = ((mask & B111) != 0) ? grid.getAttribute(ix1, iy1, iz1): 0;
        double d = 
            dx1 *(dy1 * (dz1 * v000 + dz  * v001) +  dy*(dz1 * v010 + dz  * v011)) +   
            dx  *(dy1 * (dz1 * v100 + dz  * v101) +  dy*(dz1 * v110 + dz  * v111));
        
        return d;
        //} catch(Exception e){
            //e.printStackTrace();
        //    printf("        ix: (%d %d %d), nx: (%d %d %d)  [%s %s %s]\n", ix, iy, iz, nx, ny, nz, 
        //           ((m_repeatType & REPEAT_X) != 0),((m_repeatType & REPEAT_Y) != 0),((m_repeatType & REPEAT_Z) != 0));
        //}
        //return 0;
    }
    
    public int getLevelsCount(){
        return m_grids.length;
    }
    
    public AttributeGrid getLevel(int level){
        return m_grids[level];
    }
    
    
    /**
       creates grid of half size 
       
     */
    static AttributeGrid makeGridHalfSize(AttributeGrid inGrid, int nx, int ny, int nz, int type){

            
        int nx1 = (nx+1)/2;
        int ny1 = (ny+1)/2;
        int nz1 = (nz+1)/2;
        AttributeGrid grid = (AttributeGrid)inGrid.createEmpty(nx1, ny1, nz1, 2*inGrid.getVoxelSize(),2*inGrid.getSliceHeight());        

        long att[] = new long[8];

        for(int y = 0; y < ny1; y++){
            int yy = 2*y;
            int yy1 = (yy+1);
            if(yy1 >= ny) yy1 = yy;
            for(int x = 0; x < nx1; x++){

                int xx = 2*x;
                int xx1 = (xx+1);
                if(xx1 >= nx) xx1 = xx;

                for(int z = 0; z < nz1; z++){

                    int zz = 2*z;
                    int zz1 = (zz+1) % nz;
                    if(zz1 >= nz) zz1 = zz;
                    int c = 0;

                    att[c++] = inGrid.getAttribute(xx,yy,zz);
                    att[c++] = inGrid.getAttribute(xx,yy,zz1);
                    att[c++] = inGrid.getAttribute(xx1,yy,zz);
                    att[c++] = inGrid.getAttribute(xx1,yy,zz1);
                    att[c++] = inGrid.getAttribute(xx1,yy1,zz);
                    att[c++] = inGrid.getAttribute(xx1,yy1,zz1);
                    att[c++] = inGrid.getAttribute(xx,yy1,zz);
                    att[c++] = inGrid.getAttribute(xx,yy1,zz1);
                    
                    switch(type){                        
                    default:
                    case SCALING_AVERAGE:
                        grid.setAttribute(x,y,z,average(att));
                        break;
                    case SCALING_MAX:
                        grid.setAttribute(x,y,z,max(att));
                        break;
                    }
                }
            }
        }
        
        return grid;
            
    }


    protected static long max(long att[]){
        long s = att[0];
        for(int k = 1; k < 8; k++){
            long ss = att[k];
            if(ss > s)
                s = ss;
        }
        return s;
    }

    protected static long average(long att[]){
        
        long s = 0;
        for(int k = 0; k < 8; k++){
            s += att[k];
        }
        return  (s + 4) >> 3;
        
    }

    static final int clamp(int v, int v0, int v1){
        if(v < v0)
            return v0;
        if(v >= v1) 
            return v1-1;
        else return v;
    }

    static final int reminder(int x, int n){

        int res = (x % n);
        if(res < 0) {
            res += n;
        } 
        return res;
        
    }

    static final boolean isOutside(int x, int nx){
        if( (x < 0) || x >= nx)
            return true;
        else 
            return false; 
    }
    
}
