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

package abfab3d.datasources;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

import abfab3d.grid.Grid;
import abfab3d.grid.Bounds;
import abfab3d.grid.AttributeGrid;

import abfab3d.util.Output;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;


/**
   
   
   DataSource interface to Grid. This object shall be used if one want to use generated grid as a general shape. 
   by default grid uses linear interpolation of values between voxels. 

   @author Vladimir Bulatov
   
   
*/
public class DataSourceGrid extends TransformableDataSource {

    static final boolean DEBUG = false;
    static int debugCount = 100;
    static final int DEFAULT_MAX_ATTRIBUTE_VALUE = 255;

    static public final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1;
    protected int m_interpolationType = INTERPOLATION_LINEAR;

    AttributeGrid m_grid;
    // default subvoxelResolution 
    int m_subvoxelResolution=DEFAULT_MAX_ATTRIBUTE_VALUE; 
    double m_bounds[] = new double[6];
    int m_nx, m_ny, m_nz;
    double xmin, ymin, zmin, xscale, yscale, zscale;

    LinearMapper m_mapper = null;
       
    /**
       constructs DataSoure from the given grid 
     */
    public DataSourceGrid(AttributeGrid grid){
        this(grid, null, DEFAULT_MAX_ATTRIBUTE_VALUE);
    }

    /**

       @noRefGuide            
     */
    public DataSourceGrid(AttributeGrid grid, int subvoxelResolution){
        this(grid,null,subvoxelResolution);
    }

    /**
       @noRefGuide            
       makes grid with given bounds and max attribute value
    */
    public DataSourceGrid(AttributeGrid grid, double bounds[], int subvoxelResolution){
        
        m_subvoxelResolution = subvoxelResolution;
        m_grid = grid;
        if(bounds == null){
            m_grid.getGridBounds(m_bounds);
        } else {
            System.arraycopy(bounds, 0, m_bounds, 0, m_bounds.length);
        }
        
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();
        
        xmin = m_bounds[0];
        ymin = m_bounds[2];
        zmin = m_bounds[4];

        xscale = m_nx / (m_bounds[1] - m_bounds[0]);
        yscale = m_ny / (m_bounds[3] - m_bounds[2]);
        zscale = m_nz / (m_bounds[5] - m_bounds[4]);


        if(DEBUG && debugCount > 0){
            printf("DataSourceGrid()\n");
            printf("nx: (%d x %d x %d) \n", grid.getWidth(),grid.getHeight(),grid.getDepth());
            printf("xmin: (%10.7f,%10.7f,%10.7f) \n", xmin, ymin, zmin);
            printf("xscale: (%10.7f,%10.7f,%10.7f) \n", xscale,yscale,zscale);
        }
    }


    public void setMapper(LinearMapper mapper){

        m_mapper = mapper; 

    }

    public LinearMapper getMapper(){

        return m_mapper; 
    }

    /**
       
       sets type iused for intervoxel interplation 
       @param type or interpolation (INTERPOLATION_BOX or INTERPOLATION_LINEAR)
       
     */
    public void setInterpolationType(int value){

        m_interpolationType = value;

    }

    public Bounds getGridBounds(){
        return m_grid.getGridBounds();
    }

    public int getGridWidth(){
        return m_nx;
    }
    public int getGridHeight(){
        return m_ny;
    }
    public int getGridDepth(){
        return m_nz;
    }
    public void getGridData(byte data[]){

        int nx = m_nx;
        int ny = m_ny;
        int nz = m_nz;
        int nxy = nx*ny;

        for(int z = 0; z < nz; z++){
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    long att = m_grid.getAttribute(x,y,z);
                    data[x + y * nx + z * nxy] = (byte)(att & 0xFF);
                }
            }            
        }
    }

    /**

       @noRefGuide            
     */
    public int initialize(){

        super.initialize();

        if(m_mapper == null){
            // create default mapper 
            if(m_subvoxelResolution > 0) 
                m_mapper = new LinearMapper(0,m_subvoxelResolution, 0, 1);
            else 
                m_mapper = new LinearMapper(0,1, 0, 1);
        }

        return RESULT_OK;

    }

    /**
     * returns 1 if pnt is inside of grid
     * returns 0 if pont is poutsid eof grid
     * returns interpolared value near the boundary 
      @noRefGuide            
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);
                                                                
        switch(m_interpolationType){
        default:
        case INTERPOLATION_BOX:
            getBoxInterpolatedValue(pnt, data);
            break;
        case INTERPOLATION_LINEAR:
            getLinearInterpolatedValue(pnt, data);
            break;
        }

        super.getMaterialDataValue(pnt, data);        
        return RESULT_OK;
        
    }

    private int getBoxInterpolatedValue(Vec pnt, Vec data){

        double v[] = pnt.v;
        // values normalized to voxel size 
        double 
            x = (v[0]-xmin)*xscale,
            y = (v[1]-ymin)*yscale,
            z = (v[2]-zmin)*zscale;        
        
        int ix = (int)x;
        int iy = (int)y;
        int iz = (int)z;

        if(ix < 0 || iy < 0 || iz < 0 || ix >= m_nx || iy >= m_ny || iz >= m_nz) {
            data.v[0] = 0;
            return RESULT_OUTSIDE; 
        }

        data.v[0] = m_mapper.map(getGridValue(ix, iy, iz));
        
        return RESULT_OK; 
            
    }
    private int getLinearInterpolatedValue(Vec pnt, Vec data){
        
        double v[] = pnt.v;
        // values normalized to voxel size 
        double 
            x = (v[0]-xmin)*xscale-0.5, // half voxel shift 
            y = (v[1]-ymin)*yscale-0.5,
            z = (v[2]-zmin)*zscale-0.5;        
        int ix = (int)x;
        int iy = (int)y;
        int iz = (int)z;
        if(ix < 0 || iy < 0 || iz < 0 || ix >= m_nx || iy >= m_ny || iz >= m_nz) {
            data.v[0] = 0;
            return RESULT_OUTSIDE; 
        }
        int ix1 = (ix + 1);
        int iy1 = (iy + 1);
        int iz1 = (iz + 1);
        if(ix1 >= m_nx || iy1 >= m_ny || iz1 >= m_nz ) {
            data.v[0] = 0;
            return RESULT_OUTSIDE; 
        }

        double 
            dx = x - ix,
            dy = y - iy,
            dz = z - iz,
            dx1 = 1. - dx,
            dy1 = 1. - dy,
            dz1 = 1. - dz;

        
        long 
            v000 = getGridValue(ix,  iy,  iz ),  
            v100 = getGridValue(ix1, iy,  iz ), 
            v010 = getGridValue(ix,  iy1, iz ), 
            v110 = getGridValue(ix1, iy1, iz ),
            v001 = getGridValue(ix,  iy,  iz1),
            v101 = getGridValue(ix1, iy,  iz1),
            v011 = getGridValue(ix,  iy1, iz1),
            v111 = getGridValue(ix1, iy1, iz1);

        if(DEBUG && debugCount-- > 0) printf("[%3d, %3d, %3d]: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n",ix, iy, iz, 
                                             v000,v100, v010, v110, 
                                             v001,v101, v011, v111);
        double d = 
            dx1 *(dy1 * (dz1 * v000 + dz  * v001) +  dy*(dz1 * v010 + dz  * v011)) +   
            dx  *(dy1 * (dz1 * v100 + dz  * v101) +  dy*(dz1 * v110 + dz  * v111));

        data.v[0] = m_mapper.map(d);
        
        return RESULT_OK; 
        
    }

    private final long getGridValue(int x, int y, int z){

        //TODO hee we need to have flexible way of getting data from the grid value
        
        switch(m_subvoxelResolution){            
        case 0:  // grid value is in state 
            byte state = m_grid.getState(x, y, z);            
            switch(state){
            case Grid.OUTSIDE:
                return 0;                
            default:
                return 1;
            }             
        default:            
            return (long) (short)m_grid.getAttribute(x, y, z);
        }        
    }    
}
