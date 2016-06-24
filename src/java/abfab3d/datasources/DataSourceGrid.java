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

import abfab3d.util.Bounds;

import abfab3d.grid.GridDataChannel;
import abfab3d.grid.GridDataDesc;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.AttributePacker;

import abfab3d.param.IntParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.Parameter;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.lerp3;


/**
 * DataSource interface to Grid. This object shall be used if one wants to use a generated grid as a general shape.
 * By default, grid uses linear interpolation of values between voxels.
 *
 * @author Vladimir Bulatov
 */
public class DataSourceGrid extends TransformableDataSource implements Cloneable {

    static final boolean DEBUG = false;
    static int debugCount = 1000;
    //static final int MAX_SHORT = 0xFFFF;
    //static final int MAX_BYTE = 0xFF;

    static final int BYTE_BITS = 8;  // bits in byte 
    static final int BYTE_MASK = 0xFF; 

    static final int SHORT_BITS = 16;
    static final int SHORT_MASK = 0xFFFF; 

    static final int INT_BITS = 32; // bits in int
    static final int INT_BYTES = 4; // bytes in int 
    static final long INT_MASK = 0xFFFFFFFFL;
    static final int MAX_DATA_DIMENSION = 6;
    //static final int DEFAULT_MAX_ATTRIBUTE_VALUE = 0xFF;


    static public final int INTERPOLATION_BOX = 0, INTERPOLATION_LINEAR = 1;
    protected int m_interpolationType = INTERPOLATION_LINEAR;


    protected AttributeGrid m_grid;

    // packer to pack data into data buffer
    //protected AttributePacker m_bufferDataPacker;
    // packer to unpack data from grid
    //protected AttributePacker m_gridDataPacker;

    protected GridDataChannel m_dataChannels[];
    protected byte[] m_cachedByteData;
    // mask for byte in int packing 
    // complement mask for byte in int packing 
    static final int BYTE_COMPLEMENT[] = new int[]{0xFFFFFF00,0xFFFF00FF,0xFF00FFFF,0x00FFFFFF};
    // bit shift for byte in int packing 
    static final int BYTE_SHIFT[] = new int[]{0, 8, 16, 24};
    // complement mask for short in int packing 
    static final int SHORT_COMPLEMENT[] = new int[]{0xFFFF0000,0x0000FFFF};
    // bit shift for short in int packing 
    static final int SHORT_SHIFT[] = new int[]{0, 16};

    int m_nx, m_ny, m_nz;
    double xmin, ymin, zmin, xscale, yscale, zscale;

    ObjectParameter  mp_grid = new ObjectParameter("grid","grid to be used as source", null);
    ObjectParameter  mp_bufferDataPacker = new ObjectParameter("dataPacker","packer for grid data into buffer", null);
    IntParameter  mp_gridDataTypeSize = new IntParameter("gridDataTypeSize","size of grid data type in bytes", 1);

    Parameter m_aparam[] = new Parameter[]{
        mp_grid,
        mp_gridDataTypeSize,
        mp_bufferDataPacker,
    };
       
    /**
       constructs DataSource from the given grid
     */
    public DataSourceGrid(AttributeGrid grid){
        this(grid, null, 1);
    }

    /**
       obsolete

       @noRefGuide    
     */
    public DataSourceGrid(AttributeGrid grid, int subvoxelResolution){
        this(grid,null,subvoxelResolution);
    }

    /**
       params after grid are obsolete and ignored 

       @noRefGuide  
       takes makes grid with given bounds and max attribute value
       obsolete
    */
    public DataSourceGrid(AttributeGrid grid, double _bounds[], int subvoxelResolution){
        
        super.addParams(m_aparam);
        
        mp_grid.setValue(grid);

        m_grid = grid;
        
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
            printf("  grid: (%d x %d x %d) \n", grid.getWidth(),grid.getHeight(),grid.getDepth());
        }
    }

    /**
     * @noRefGuide
     */
    protected void emptyCache(){
        m_cachedByteData = null;
    }

    /**
       
     * @noRefGuide
       sets type used for intervoxel interpolation
       @param value of interpolation (INTERPOLATION_BOX or INTERPOLATION_LINEAR)
       
     */
    public void setInterpolationType(int value){

        emptyCache();
        
        m_interpolationType = value;

    }

    /**
     * @noRefGuide
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

    /**
     * @noRefGuide
     * Set the data size in bytes to use for a grid.  Determines precision of rendering.
     
     * @param size Currently supports 1 or 2
     *
     */
    //public void setGridDataTypeSize(int size) {
    //    mp_gridDataTypeSize.setValue(size);
    //}

    /**
     * @noRefGuide
     */
    //public int getGridDataTypeSize() {
    //    return mp_gridDataTypeSize.getValue();
    //}
    
    public GridDataDesc getGridDataDesc(){

        return m_grid.getDataDesc();

    }

    /**
     * @noRefGuide
       returns buffer data description 
     */
    public GridDataDesc getBufferDataDesc(){

        //int dds = getGridDataTypeSize();
        GridDataDesc gdd = m_grid.getDataDesc();
        // make new GridDataDesc which will be used to make buffer data                
        GridDataDesc bdd = new GridDataDesc();
        int cc = gdd.size();        
        int bitOffset = 0;
        for(int i = 0; i < cc; i++){
            GridDataChannel gc = gdd.getChannel(i);
            //TODO make active channels selectable 
            //TODO make bitCount adjustable           
            int bitCount = 8;
            bdd.addChannel(new GridDataChannel(gc.getType(),gc.getName(),bitCount,bitOffset, gc.getValue0(), gc.getValue1()));
            bitOffset += bitCount;
        }

        return bdd;

    }

    /**
     * @noRefGuide
       returns size (in words) of data buffer needed to store grid data 
     */
    public int getBufferSize(){

        return getBufferSize(getBufferDataDesc());
    }

    /**
     * @noRefGuide
     * returns size of buffer needed for specific grid data description 
     */
    public int getBufferSize(GridDataDesc bufDataDesc){

        AttributePacker packer = bufDataDesc.getAttributePacker();

        int outBits = packer.getBitCount();

        int outBytes = (outBits + BYTE_BITS-1)/BYTE_BITS;

        int nx = m_grid.getWidth();
        int ny = m_grid.getHeight();
        int nz = m_grid.getDepth();

        long attCount = ((long)nx)*ny*nz;
        if(attCount > Integer.MAX_VALUE){
            throw new RuntimeException(fmt("grid is too large to pack in array: [%d x %d x %d] -> 0x%x", nx, ny, nz, attCount));
        }

        int count = (int)attCount;
        switch(outBytes){
        default:
            throw new RuntimeException(fmt("unsupported bytes count:%d", outBytes));
        case 1: 
            return (count + 3)/ 4;
        case 2: 
            return (count + 1)/ 2;
        case 3: 
        case 4: 
            return count;
        }

    }

    public int getBufferTypeSize(){

        return  getBufferTypeSize(getBufferDataDesc());


    }

    public int getBufferTypeSize(GridDataDesc bufferDataDesc){

        int bits = bufferDataDesc.getAttributePacker().getBitCount();
        int bytes = (bits + BYTE_BITS-1)/BYTE_BITS;
        switch(bytes){
        default:
            throw new RuntimeException(fmt("unsupported bytes count:%d", bytes));
        case 1: 
            return 1;
        case 2: 
            return 2;
        case 3: 
        case 4: 
            return 4;
        }                
    }

    /**
       return data in default data format
       
     */
    public void getBuffer(int data[]){

        getBuffer(data, getBufferDataDesc());

    }


    /**
       return data in specific data format
     */
    public void getBuffer(int data[], GridDataDesc bufferDataDesc){

        AttributePacker packer = bufferDataDesc.getAttributePacker();
        int nx = m_nx;
        int ny = m_ny;
        int nz = m_nz;
        int nxy = nx*ny;
        Vec value = new Vec(6);
        AttributePacker unpacker = m_grid.getDataDesc().getAttributePacker();
        int outBits = packer.getBitCount();
        int outBytes = (outBits + BYTE_BITS-1)/BYTE_BITS;
        int outInts = (outBytes + INT_BYTES-1)/INT_BYTES;
        switch(outBytes){
        default:
            throw new RuntimeException(fmt("unsupported bytes count:%d", outBytes));
        case 1: 
            getGridDataUByte(data, unpacker, packer); break;
        case 2: 
            getGridDataUShort(data, unpacker, packer); break;
        case 3: 
        case 4: 
            getGridDataUInt(data, unpacker, packer); break;
        }

    }

    /**
       return grid data as array of bytes packed into array of ints
     */
    protected void getGridDataUByte(int data[], AttributePacker unpacker, AttributePacker packer){
        
        if(DEBUG)printf("getGridDataUByte()\n");
        final int nx = m_nx, ny = m_ny, nz = m_nz, nxz = nx*nz;
        Vec value = new Vec(MAX_DATA_DIMENSION);

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long att = m_grid.getAttribute(x, y, z); 
                    long outAtt = att;
                    if(unpacker != packer){
                        unpacker.getData(att, value);
                        outAtt = packer.makeAttribute(value);
                    }
                    int ind = (x*nz + y * nxz + z); 
                    int wordInd = ind >> 2;
                    int byteInWord = ind  & 3;
                    data[wordInd] = (int)((data[wordInd] & BYTE_COMPLEMENT[byteInWord]) | (outAtt << BYTE_SHIFT[byteInWord]));
                }
            }            
        }        
    }

    /**
       return grid data as array of shorts packed into array of ints
     */
     protected void getGridDataUShort(int data[], AttributePacker unpacker, AttributePacker packer){

         if(DEBUG)printf("getGridDataUShort()\n");
        final int nx = m_nx, ny = m_ny, nz = m_nz, nxz = nx*nz;
        Vec value = new Vec(MAX_DATA_DIMENSION);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long att = m_grid.getAttribute(x, y, z); 
                    long outAtt = att;
                    if(unpacker != packer){
                        unpacker.getData(att, value);
                        outAtt = packer.makeAttribute(value);
                    }

                    unpacker.getData(att, value);
                    int ind = (x*nz + y * nxz + z); 
                    int wordInd = ind >> 1;
                    int shortInWord = ind & 1; 
                    data[wordInd] = (int)((data[wordInd] & SHORT_COMPLEMENT[shortInWord]) | (outAtt << SHORT_SHIFT[shortInWord]));
                }
            }            
        }                
    }

    /**
     * @noRefGuide
       return grid data as array of ints
     */
     protected void getGridDataUInt(int data[], AttributePacker unpacker, AttributePacker packer){
         
         if(DEBUG)printf("getGridDataUInt()\n");
         final int nx = m_nx, ny = m_ny, nz = m_nz, nxz = nx*nz;
         Vec value = new Vec(MAX_DATA_DIMENSION);
         if(false)printf("--------------\n ");
         for(int z = 0; z < nz; z++){
             for(int y = 0; y < ny; y++){
                 for(int x = 0; x < nx; x++){
                     long att = m_grid.getAttribute(x, y, z); 
                     long outAtt = att;
                     if(unpacker != packer){
                         unpacker.getData(att, value);
                         outAtt = packer.makeAttribute(value);
                     }
                     if(false)printf("%8x ", outAtt);
                     int ind = (x*nz + y * nxz + z); 
                     data[ind] = (int)(outAtt & INT_MASK);                    
                 }
                 if(false)printf("\n ");
             }            
             if(false)printf("-----------\n ");
         }        
     }
    
    /**

       @noRefGuide            
     */
    public int initialize(){

        super.initialize();
        
        //m_bufferDataPacker = (AttributePacker)mp_bufferDataPacker.getValue();
        
        //if(m_bufferDataPacker == null) {
            // no specific data packer provided - use default 
        //    m_bufferDataPacker = m_grid.getDataDesc().getAttributePacker();
        //}
        
        GridDataDesc gridDataDesc = m_grid.getDataDesc();
        m_channelsCount = gridDataDesc.size();
        m_dataChannels = new GridDataChannel[m_channelsCount];
        if(DEBUG)printf("m_channelsCount:%d\n",m_channelsCount);
        for(int i = 0; i < m_channelsCount; i++){
            m_dataChannels[i] = gridDataDesc.getChannel(i);
            if(DEBUG)printf("m_dataChannels[%d]:%s\n",i, m_dataChannels[i]);
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
        getLinearInterpolatedValue(pnt, data);

        super.getMaterialDataValue(pnt, data);        
        return RESULT_OK;
        
    }
    /*
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
    */
    
    private int getLinearInterpolatedValue(Vec pnt, Vec data){
        
        double v[] = pnt.v;
        
        // values normalized to voxel size 
        double 
            x = (v[0]-xmin)*xscale-0.5, // half voxel shift voxel centers are located at semi integer positions 
            y = (v[1]-ymin)*yscale-0.5,
            z = (v[2]-zmin)*zscale-0.5;        
        int ix = (int)x;
        int iy = (int)y;
        int iz = (int)z;

        if(ix < 0 || iy < 0 || iz < 0 || ix >= m_nx || iy >= m_ny || iz >= m_nz) {
            //TODO something better 
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
            dz = z - iz;
        
        
        long   
            a000 = m_grid.getAttribute(ix,  iy,  iz),
            a100 = m_grid.getAttribute(ix1, iy,  iz), 
            a010 = m_grid.getAttribute(ix,  iy1, iz), 
            a110 = m_grid.getAttribute(ix1, iy1, iz),
            a001 = m_grid.getAttribute(ix,  iy,  iz1),
            a101 = m_grid.getAttribute(ix1, iy,  iz1),
            a011 = m_grid.getAttribute(ix,  iy1, iz1),
            a111 = m_grid.getAttribute(ix1, iy1, iz1);
        //if(DEBUG) printf("%8x \n", a000);

        for(int ch = 0; ch < m_channelsCount; ch++){
            
            GridDataChannel channel = m_dataChannels[ch];
            double 
                v000 = channel.getValue(a000),
                v100 = channel.getValue(a100),
                v010 = channel.getValue(a010),
                v110 = channel.getValue(a110),
                v001 = channel.getValue(a001),
                v101 = channel.getValue(a101),
                v011 = channel.getValue(a011),
                v111 = channel.getValue(a111);
            //if(DEBUG && debugCount-- > 0) printf("%8.5f ", v000);
            data.v[ch] = lerp3(v000,v100,v010,v110,v001,v101,v011,v111,dx, dy, dz);
        }
        //if(DEBUG && debugCount-- > 0) printf("\n");
        
        return RESULT_OK; 
        
    }
    

    /**
       temp hack to make colored distance grid 
       @noRefGuide            
       
     */
    public void colorize(){

        if(DEBUG)printf("DataSourceGrid.colorize()");
        double vs = m_grid.getVoxelSize();
        AttributeGrid cGrid = new ArrayAttributeGridInt(m_grid.getGridBounds(), vs, vs);
        GridDataChannel distChannel = m_grid.getDataChannel();
        double maxDist = Math.abs(distChannel.getValue0());
        GridDataDesc cDesc = GridDataDesc.getDistBGR(maxDist);
        cGrid.setDataDesc(cDesc);
        AttributePacker packer = cDesc.getAttributePacker();
        Vec pnt = new Vec(3);
        Vec out = new Vec(4);        
        if(false)printf("---------------\n");
        for(int iy = 0; iy < m_ny; iy++){
            for(int ix = 0; ix < m_nx; ix++){
                for(int iz = 0; iz < m_nz; iz++){
                    long iatt = m_grid.getAttribute(ix, iy, iz);
                    //printf("%8x ", iatt);
                    double dist = distChannel.getValue(iatt);
                    //printf("%4d", (int)(100*(dist/maxDist)));
                    out.v[0] = dist;
                    double r = Math.sin(4*Math.PI*ix/m_nx);
                    double g = Math.sin(8*Math.PI*iy/m_ny);
                    double b = Math.sin(16*Math.PI*iz/m_nz);
                    out.v[1] = r*r;
                    out.v[2] = g*g;
                    out.v[3] = b*b;
                    long att = packer.makeAttribute(out);
                    if(false)printf("%8x ", att);
                    cGrid.setAttribute(ix, iy, iz, att);
                }
                if(false)printf("\n");
            }
            if(false)printf("---------------\n");
        }
        
        m_grid = cGrid;
    }
    /**
     * @noRefGuide
     */
    public DataSourceGrid clone() throws CloneNotSupportedException {
        return (DataSourceGrid) super.clone();
    }
}
