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
package abfab3d.datasources;


import abfab3d.core.DataSource;
import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;
import abfab3d.core.Bounds;
import abfab3d.core.TriangleCollector;
import abfab3d.core.AttributeGrid;

import static abfab3d.core.MathUtil.lerp3;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.getDistance;


/**
   intepolator of distances from index grid and array of coordinates
   the data are supplied as array of point coordinates (and optional other values) 
   each grid voxel contains index of closes point in that array 
   values in between voxel centers are linearly interpolated 
   
   @author Vladimir Bulatov
*/
public class IndexedDistanceInterpolator implements DataSource {
    
    static final int UNDEFINED_INDEX = 0;  // value of voxels with undefined index 
    static final double HALF = 0.5;

    static final long INTERIOR_MASK = (1L << 31); // bit to store interior mask 
    static final long INDEX_MASK = 0x7FFFFFFF; // bits containing actual index data 

    static final public int INTERPOLATION_BOX = 0;
    static final public int INTERPOLATION_LINEAR = 1;
    static final public int INTERPOLATION_COMBINED = 2;

    // coordinates of points 
    double 
        pntx[], 
        pnty[], 
        pntz[]; 
    // indices of closest point to each voxel 
    AttributeGrid indexGrid;
    
    // max values of voxel coord
    private int nxmax, nymax, nzmax;
    
    // grid origin 
    private double xmin, ymin, zmin;
    // minimal physical coordinates of voxel 
    private double xmin1, ymin1, zmin1;
    // maximal physical coordinates of voxel 
    private double xmax1, ymax1, zmax1;
    // scale to convert coord into voxels 
    private double scale;    
    // voxel size 
    private double voxelSize;
    // maximal distace to store in the grid 
    private  double maxDistance;
    // interpolation to use between voxels 
    private  int interpolationType = INTERPOLATION_LINEAR;
    
    // optional low res data for calculation of missing data in the grid 
    DataSource lowResData;
    
    /**
       @param pnts[][] array of data points 
       pnts[0][] - x cordinates 
       pnts[1][] - y cordinates 
       pnts[2][] - z cordinates 
       pnts[3][] - u cordinates (optional, used for texture or color coord)
       pnts[4][] - v cordinates (optional, used for texture or color coord)
       pnts[5][] - w cordinates (optional, used for color coord)        
     */
    public IndexedDistanceInterpolator(double pnts[][], AttributeGrid indexGrid, double maxDistance){
        
        this.pntx = pnts[0];
        this.pnty = pnts[1];
        this.pntz = pnts[2];
        this.indexGrid = indexGrid;
        this.maxDistance = maxDistance;
        Bounds bounds = indexGrid.getGridBounds();
        
        this.voxelSize = indexGrid.getVoxelSize();
        this.scale = 1./voxelSize;
        
        this.nxmax = indexGrid.getWidth()-1;
        this.nymax = indexGrid.getHeight()-1;
        this.nzmax = indexGrid.getDepth()-1;
        double vs2 = voxelSize/2;
        this.xmin = bounds.xmin;
        this.ymin = bounds.ymin;
        this.zmin = bounds.zmin;

        this.xmin1 = bounds.xmin + vs2;
        this.ymin1 = bounds.ymin + vs2;
        this.zmin1 = bounds.zmin + vs2;
        this.xmax1 = bounds.xmax - vs2;
        this.ymax1 = bounds.ymax - vs2;
        this.zmax1 = bounds.zmax - vs2;
        
    }
    
    /**
       method of DataSource interface 
    */
    public int getDataValue(Vec pnt, Vec data){
        
        switch(interpolationType){
        default:
        case INTERPOLATION_BOX:
            getValueBox(pnt, data);
            break;
        case INTERPOLATION_LINEAR:
            getValueLinear(pnt, data);                
        case INTERPOLATION_COMBINED:
            getValueLinear(pnt, data);                
        }
        return ResultCodes.RESULT_OK;
    }
    
    /**
       method of DataSource interface 
    */
    public Bounds getBounds(){
        if(true) throw new RuntimeException("method not implemented");
        return null;
    }
    public void setBounds(Bounds bounds){
        if(true) throw new RuntimeException("method not implemented");
    }
    
    public int getChannelsCount(){
        return 1;
    }
    /**
       calculates distance to arbitrary pnt(x,y,z) as distance to center of nearest voxel 
    */
    public int getValueBox(Vec pnt, Vec data){
        
        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];
        
        int  
            ix = (int)((pnt.v[0] - xmin)*scale),
            iy = (int)((pnt.v[1] - ymin)*scale),
            iz = (int)((pnt.v[2] - zmin)*scale);
        
        double d000 = distanceToVoxel(ix, iy, iz);
        
        data.v[0] = d000;
        
        return ResultCodes.RESULT_OK;
        
    }
    
    /**
       calculates distance to arbitrary pnt(x,y,z) as linear interpolation of distances to centers of 8 nearest voxel 
    */
    public int getValueLinear(Vec pnt, Vec data){
        
        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];
        
        double   
            gx = (pnt.v[0] - xmin)*scale - HALF,
            gy = (pnt.v[1] - ymin)*scale  - HALF,
            gz = (pnt.v[2] - zmin)*scale - HALF;
        int 
            ix = (int)gx,
            iy = (int)gy,
            iz = (int)gz;
        
        double 
            dx = gx - ix,
            dy = gy - iy,
            dz = gz - iz;
        
        double 
            d000 = distanceToVoxel(ix  , iy,   iz),
            d100 = distanceToVoxel(ix+1, iy,   iz),
            d110 = distanceToVoxel(ix+1, iy+1, iz),
            d010 = distanceToVoxel(ix  , iy+1, iz),
            d001 = distanceToVoxel(ix  , iy,   iz+1),
            d101 = distanceToVoxel(ix+1, iy,   iz+1),
            d111 = distanceToVoxel(ix+1, iy+1, iz+1),
            d011 = distanceToVoxel(ix  , iy+1, iz+1);
        
        data.v[0] = lerp3(d000,d100,d010,d110,d001,d101,d011,d111,dx, dy, dz);
            
        return ResultCodes.RESULT_OK;
        
    }
    
    /**
       interpolates value from current grid and low res data source 
       if voxel data is missing, the data is calculated from low res data source 
    */
    public int getValueCombined(Vec pnt, Vec data){
        
        double v[] = pnt.v;
        double 
            x = v[0], 
            y = v[1], 
            z = v[2],
            x1 = x + voxelSize,
            y1 = y + voxelSize,
            z1 = z + voxelSize;
        
        
            // coord in grid units
        double 
            gx = (clamp(x,xmin1, xmax1) - xmin)*scale - HALF,
            gy = (clamp(y,ymin1, ymax1) - ymin)*scale - HALF,
            gz = (clamp(z,zmin1, zmax1) - zmin)*scale - HALF;
        int ix = clamp((int)gx, 0, nxmax);
        int iy = clamp((int)gy, 0, nymax);
        int iz = clamp((int)gz, 0, nzmax);
        int ix1 = clamp(ix+1,0,nxmax);
        int iy1 = clamp(iy+1,0,nymax);
        int iz1 = clamp(iz+1,0,nzmax);
        double 
            dx = gx - ix,
            dy = gy - iy,
            dz = gz - iz;
        
        
        double v000, v100, v110, v010, v001, v101, v111, v011;
        boolean hasData = false;
        long a000 = indexGrid.getAttribute(ix,  iy,  iz);  hasData = hasData || (a000 != UNDEFINED_INDEX);
        long a100 = indexGrid.getAttribute(ix1, iy,  iz);  hasData = hasData || (a100 != UNDEFINED_INDEX);
        long a010 = indexGrid.getAttribute(ix,  iy1, iz);  hasData = hasData || (a010 != UNDEFINED_INDEX);
        long a110 = indexGrid.getAttribute(ix1, iy1, iz);  hasData = hasData || (a110 != UNDEFINED_INDEX);
        long a001 = indexGrid.getAttribute(ix,  iy,  iz1); hasData = hasData || (a001 != UNDEFINED_INDEX);
        long a101 = indexGrid.getAttribute(ix1, iy,  iz1); hasData = hasData || (a101 != UNDEFINED_INDEX);
        long a011 = indexGrid.getAttribute(ix,  iy1, iz1); hasData = hasData || (a011 != UNDEFINED_INDEX);
        long a111 = indexGrid.getAttribute(ix1, iy1, iz1); hasData = hasData || (a111 != UNDEFINED_INDEX);
        
        if(!hasData){
            // no voxels have any data => use data from low res DataSource 
            lowResData.getDataValue(pnt, data);
            return ResultCodes.RESULT_OK;
        }
        v000 = combineData(a000, ix,  iy,  iz,  pnt,data);
        v100 = combineData(a100, ix1, iy,  iz,  pnt,data);
        v010 = combineData(a010, ix,  iy1, iz,  pnt,data);
        v110 = combineData(a110, ix1, iy1, iz,  pnt,data);
        v001 = combineData(a001, ix,  iy,  iz1, pnt,data);
        v101 = combineData(a101, ix1, iy,  iz1, pnt,data);
        v011 = combineData(a011, ix,  iy1, iz1, pnt,data);
        v111 = combineData(a111, ix1, iy1, iz1, pnt,data);
        
        data.v[0] = lerp3(v000,v100,v010,v110,v001,v101,v011,v111,dx, dy, dz);            
        
        return ResultCodes.RESULT_OK;           
    }
    
    /**
       if att == UNDEFINED_INDEX - return data calculated from lowGridData 
       otherwise return data calculated from current data 
    */
    double combineData(long att, int ix, int iy, int iz, Vec pnt, Vec data){
        
        if(att != UNDEFINED_INDEX){
            return distanceToVoxel(ix, iy, iz);
        } else {
            // voxel is undefined - use lowResData 
            setCoord(pnt, ix, iy, iz);
            lowResData.getDataValue(pnt, data);
            return data.v[0];
        }
        
    }
    
    /**
       sets phyical coordinates of point from voxel coordinates
    */
    void setCoord(Vec pnt, int ix, int iy, int iz){
        pnt.v[0] = (ix + HALF)*voxelSize + xmin;
        pnt.v[1] = (iy + HALF)*voxelSize + ymin;
        pnt.v[2] = (iz + HALF)*voxelSize + zmin;
    }
    
    /**
       return distance associated with voxel ix, iy, iz 
    */
    double distanceToVoxel(int ix, int iy, int iz){
        
        ix = clamp(ix, 0, nxmax);
        iy = clamp(iy, 0, nymax);
        iz = clamp(iz, 0, nzmax);
        
        long a = indexGrid.getAttribute(ix, iy, iz);        
        int sign = 1;
        if((a & INTERIOR_MASK) != 0)
            sign = -1;
        int ind = (int)(a & INDEX_MASK);
        if(ind == UNDEFINED_INDEX){
            return sign*maxDistance;
        } else {
            double x = (ix+HALF)*voxelSize + xmin;
            double y = (iy+HALF)*voxelSize + ymin;
            double z = (iz+HALF)*voxelSize + zmin;            
            return sign*getDistance(x,y,z, pntx[ind],pnty[ind],pntz[ind]);
        }        
    }        
} //  class  IndexedDistanceInterpolator
