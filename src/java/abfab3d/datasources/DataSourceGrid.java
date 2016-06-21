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


import abfab3d.grid.GridDataChannel;
import abfab3d.util.Vec;

import abfab3d.util.Bounds;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributePacker;

import abfab3d.param.IntParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.Parameter;


import static abfab3d.util.Output.printf;
import static abfab3d.util.MathUtil.clamp;


/**
 * DataSource interface to Grid. This object shall be used if one wants to use a generated grid as a general shape.
 * By default, grid uses linear interpolation of values between voxels.
 *
 * @author Vladimir Bulatov
 */
public class DataSourceGrid extends TransformableDataSource implements Cloneable {

    static final boolean DEBUG = false;
    static int debugCount = 100;
    static final int MAX_SHORT = 0xFFFF;
    static final int MAX_BYTE = 0xFF;
    static final int DEFAULT_MAX_ATTRIBUTE_VALUE = 0xFF;


    static public final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1;
    protected int m_interpolationType = INTERPOLATION_LINEAR;


    protected AttributeGrid m_grid;

    // packer to pack data into data buffer
    protected AttributePacker m_bufferDataPacker;

    // packer to unpack data from grid
    protected AttributePacker m_gridDataPacker;

    protected GridDataChannel m_dataChannel;
    protected byte[] m_cachedByteData;

    int m_nx, m_ny, m_nz;
    double xmin, ymin, zmin, xscale, yscale, zscale;

    ObjectParameter  mp_grid = new ObjectParameter("grid","grid to be used as source", null);
    ObjectParameter  mp_bufferDataPacker = new ObjectParameter("dataPacker","packer for grid data into buffer", null);
    IntParameter  mp_gridDataTypeSize = new IntParameter("gridDataTypeSize","size of grid data type", 1);

    Parameter m_aparam[] = new Parameter[]{
        mp_grid,
        mp_gridDataTypeSize,
        mp_bufferDataPacker,
    };
       
    /**
       constructs DataSource from the given grid
     */
    public DataSourceGrid(AttributeGrid grid){
        this(grid, null, DEFAULT_MAX_ATTRIBUTE_VALUE);
    }

    /**
       obsolete

       @noRefGuide    
     */
    public DataSourceGrid(AttributeGrid grid, int subvoxelResolution){
        this(grid,null,subvoxelResolution);
    }

    /**
       params aftetr grid are obsolete 

       @noRefGuide  
       makes grid with given bounds and max attribute value
    */
    public DataSourceGrid(AttributeGrid grid, double _bounds[], int subvoxelResolution){
        
        super.addParams(m_aparam);
        
        mp_grid.setValue(grid);

        m_grid = grid;
        m_dataChannel = m_grid.getDataDesc().getDefaultChannel();
        
        Bounds bounds = m_grid.getGridBounds();
        
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();
        
        xmin = bounds.xmin;
        ymin = bounds.ymin;
        zmin = bounds.zmin;

        xscale = m_nx / bounds.getSizeX();
        yscale = m_ny / bounds.getSizeY();
        zscale = m_nz / bounds.getSizeZ();


        if(DEBUG){
            printf("DataSourceGrid()\n");
            printf("  dataChannel : %s\n",m_dataChannel);
            printf("  grid: (%d x %d x %d) \n", grid.getWidth(),grid.getHeight(),grid.getDepth());
            printf("  xmin: (%10.7f,%10.7f,%10.7f) \n", xmin, ymin, zmin);
            printf("  xscale: (%10.7f,%10.7f,%10.7f) \n", xscale,yscale,zscale);
        }
    }

    protected void emptyCache(){
        m_cachedByteData = null;
    }

    /**
       
       sets type used for intervoxel interpolation
       @param value of interpolation (INTERPOLATION_BOX or INTERPOLATION_LINEAR)
       
     */
    public void setInterpolationType(int value){

        emptyCache();
        
        m_interpolationType = value;

    }

    /**
     * Gets type used for intervoxel interpolation
     */
    public int getInpolationType() {
        return m_interpolationType;
    }

    /**
     * Get the grid bounds in meters
     * @return
     */
    public Bounds getGridBounds(){
        return m_grid.getGridBounds();
    }

    /**
     * Get the grid width in voxels
     * @return
     */
    public int getGridWidth(){
        return m_nx;
    }

    /**
     * Get the grid height in voxels
     * @return
     */
    public int getGridHeight(){
        return m_ny;
    }

    /**
     * Get the grid depth in voxels
     * @return
     */
    public int getGridDepth(){
        return m_nz;
    }

    public GridDataChannel getDataChannel() {
        return m_dataChannel;
    }

    /**
     * Set the data size in bytes to use for a grid.  Determines precision of rendering.
     * @param size Currently supports 1 or 2
     */
    public void setGridDataTypeSize(int size) {
        mp_gridDataTypeSize.setValue(size);
    }

    public int getGridDataTypeSize() {
        return mp_gridDataTypeSize.getValue();
    }

    /**
     * @noRefGuide
     */
    public byte[] getCachedData(){
        if(m_cachedByteData == null){

            int gridDataTypeSize = mp_gridDataTypeSize.getValue();
            switch(gridDataTypeSize){
            default: 
            case 1: 
                m_cachedByteData = new byte[m_nx*m_ny*m_nz];
                getGridDataUByte(m_cachedByteData);
                break;
            case 2: 
                m_cachedByteData = new byte[m_nx*m_ny*m_nz*2];
                getGridDataUShort(m_cachedByteData);
                break;
            }
        } 
        return m_cachedByteData;         
    }

    /**
     * @noRefGuide
     * fill byte array with grid data converted into bytes
     */
    public void getGridDataUByte(byte data[]){

        int nx = m_nx;
        int ny = m_ny;
        int nz = m_nz;
        int nxy = nx*ny;

        double vmin = m_dataChannel.getValue0();
        double vmax = m_dataChannel.getValue1();
        if(DEBUG) printf("vmin: %9.5f, vmax: %9.5f\n", vmin, vmax);

        for(int z = 0; z < nz; z++){
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    
                    double v = m_dataChannel.getValue(m_grid.getAttribute(x, y, z));
                    // v is inside of (vmin, vmax);
                    data[x + y * nx + z * nxy] = (byte)((int)(MAX_BYTE*clamp((v - vmin)/(vmax - vmin),0.,1.)) & MAX_BYTE);
                }
            }            
        }
    }

    /**
     * @noRefGuide
     * fill byte array with grid data converted into unsigned short
     */
    public void getGridDataUShort(byte data[] ){

        int nx = m_nx;
        int ny = m_ny;
        int nz = m_nz;
        int nxy = nx*ny;

        double vmin = m_dataChannel.getValue0();
        double vmax = m_dataChannel.getValue1();
        if(DEBUG) printf("vmin: %9.5f, vmax: %9.5f\n", vmin, vmax);

        for(int z = 0; z < nz; z++){
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    
                    double v = m_dataChannel.getValue(m_grid.getAttribute(x, y, z));
                    // v is inside of (vmin, vmax);
                    int value = ((int)(MAX_SHORT*clamp((v - vmin)/(vmax - vmin),0.,1.)) & MAX_SHORT);
                    int ind = 2*(x + y * nx + z * nxy);
                    data[ind] = (byte)(value & MAX_BYTE); 
                    data[ind + 1] = (byte)((value >> 8) & MAX_BYTE);
                }
            }            
        }
    }


    /**

       @noRefGuide            
     */
    public int initialize(){

        super.initialize();
        
        m_bufferDataPacker = (AttributePacker)mp_bufferDataPacker.getValue();

        if(m_bufferDataPacker == null) {
            // no specific data packer provided - use default 
            m_bufferDataPacker = m_grid.getDataDesc().getAttributePacker();
        }
        
        if(m_dataChannel == null)
            throw new RuntimeException("m_dataChannel == null");

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

        data.v[0] = getGridValue(ix, iy, iz);
        
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
        
        
        double  
            v000 = getGridValue(ix,  iy,  iz ),  
            v100 = getGridValue(ix1, iy,  iz ), 
            v010 = getGridValue(ix,  iy1, iz ), 
            v110 = getGridValue(ix1, iy1, iz ),
            v001 = getGridValue(ix,  iy,  iz1),
            v101 = getGridValue(ix1, iy,  iz1),
            v011 = getGridValue(ix,  iy1, iz1),
            v111 = getGridValue(ix1, iy1, iz1);

        if(false && debugCount-- > 0) printf("[%3d, %3d, %3d]: %7.4f %7.4f %7.4f %7.4f  %7.4f %7.4f %7.4f %7.4f \n",
                                             ix, iy, iz, 
                                             v000,v100, v010, v110, 
                                             v001,v101, v011, v111);
        double d = 
            dx1 *(dy1 * (dz1 * v000 + dz  * v001) +  dy*(dz1 * v010 + dz  * v011)) +   
            dx  *(dy1 * (dz1 * v100 + dz  * v101) +  dy*(dz1 * v110 + dz  * v111));

        data.v[0] = d;
        
        return RESULT_OK; 
        
    }

    private final double getGridValue(int x, int y, int z){
        return m_dataChannel.getValue(m_grid.getAttribute(x,y,z));
    }

    /**
     * @noRefGuide
     */
    public DataSourceGrid clone() throws CloneNotSupportedException {
        return (DataSourceGrid) super.clone();
    }
}
