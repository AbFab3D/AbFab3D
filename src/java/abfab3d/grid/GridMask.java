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


import abfab3d.util.Bounds;


import static abfab3d.util.Output.printf;

/**
   grid array to represent on/off mask. Implemented as array of int 
   groups of 32 points in z-direction are stored as bits in one int 

   @author Vladimir Bulatov
*/
public class GridMask extends BaseAttributeGrid implements GridBit {

    int data[];
    int nx, ny, nz;
    int lenz, lenxz;
    static final int INTLEN = 32;
    
    public GridMask(GridMask grid){
        this(grid.nx,grid.ny,grid.nz);
        System.arraycopy(grid.data,0, data, 0, data.length);
        copyBounds(grid, this);
    }
    public GridMask(){
        this(1,1,1);
    }

    public GridMask(Bounds bounds, double voxelSize, double sliceHeight) {
        super( bounds, voxelSize, sliceHeight);
        allocateData();
    }

    public GridMask(int nx, int ny, int nz){
        super(nx, ny, nz,1., 1., null);
        allocateData();        
    }

    protected void allocateData(){
        
        this.nx = getWidth();
        this.ny = getHeight();
        this.nz = getDepth();

        this.lenz = ((nz+INTLEN-1)/INTLEN);
        this.lenxz = lenz * nx;
        
        data = new int[nx*ny*lenz];
        
        // this is binary grid, make default attributeDesc
        m_gridDataDesc = GridDataDesc.getDefaultAttributeDesc(1);
    }


    public long get(int x, int y, int z){

        int zint = z/INTLEN;
        int bit = z % INTLEN;           
        int offset = zint + x*lenz + y * lenxz;
        
        int w = data[offset];
        return ((w >> bit) & 1);

    }
    
    public void set(int x, int y, int z, long value){
        
        //printf("mask.set(%d,%d,%d)\n", x,y,z);
        
        int zint = z/INTLEN;
        int bit = z % INTLEN;           
        int offset = zint + x*lenz + y * lenxz;
        if(value != 0){
            // set bit 
            data[offset] |= (1 << bit);
        } else {
            // clear bit 
            data[offset] &= (0xFFFFFFFF ^ (1 << bit));
        }
    }
    
    public void dump(){
        for(int i = 0; i < data.length; i++){
            printf("%d: %X\n", i, data[i]);
        }
    }

    public void clear(){       
        // clear data, keep the memory 
        for(int i =0; i < data.length; i++)
            data[i] = 0;

        
    }
    public void release(){
        data = null;
    }

    public Object clone() {
        GridMask ret_val = new GridMask(this);
        return ret_val;
    }

    public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
        Grid ret_val = new GridMask(w,h,d);
        return ret_val;
    }

    public void setAttribute(int x,int y,int z, long attribute){
        set(x,y,z,attribute);
    }    

    public long getAttribute(int x,int y,int z){
        return get(x,y,z);
    }    

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @override 
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        return new VoxelDataByte();
    }

    /**
       @override 
    */
    public void getData(int x, int y, int z, VoxelData data){

        long encoded = get(x,y,z);
        long att = encoded;
        byte state = (byte)encoded;
        data.setData(state,att);
    }

    /**
       @override 
     */
    public void setState(int x, int y, int z, byte state) {
        set(x,y,z,state);
    }
    public byte getState(int x, int y, int z) {
        return (byte)get(x,y,z);
    }


} // class GridMask
