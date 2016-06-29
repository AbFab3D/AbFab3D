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

package abfab3d.grid;


import abfab3d.core.AttributeGrid;
import abfab3d.core.ClassAttributeTraverser;
import abfab3d.core.ClassTraverser;
import abfab3d.core.Grid;
import abfab3d.core.GridDataDesc;
import abfab3d.core.VoxelClasses;
import abfab3d.core.VoxelData;
import abfab3d.core.Bounds;

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
   grid array to represent on/off mask.
   Implemented as 2D array of bit intervals

   @author Vladimir Bulatov
*/
public class GridBitIntervals  extends BaseAttributeGrid implements GridBit, GridIntervals, Serializable {
    private static final boolean STATS = false;
    public static final int ORIENTATION_X=0, ORIENTATION_Y=1, ORIENTATION_Z = 2; // orientation of intervals

    protected int m_orientation = ORIENTATION_Z;
    protected int m_nx, m_ny, m_nz;
    protected RowOfInt m_data[];

    /**
       contructor to make this class subclassable with without penalty of allocating bunch of memory here 
    */
    public GridBitIntervals(){
        super(1, 1, 1,  1., 1., null);
    }
    
    /**
       create grid of given orientation
     */
    public GridBitIntervals(GridBitIntervals grid){

        this(grid.m_nx,grid.m_ny,grid.m_nz,grid.m_orientation, grid.pixelSize, grid.sheight);//, null);
        for(int i = 0; i < m_data.length; i++){
            RowOfInt dataItem = grid.m_data[i];
            if(dataItem != null)
                m_data[i] = (RowOfInt)dataItem.clone();
        }
    }
    
    public GridBitIntervals(int nx, int ny, int nz, double pixelSize, double sliceHeight ){
        this(nx, ny, nz, ORIENTATION_Z, pixelSize, sliceHeight);
    }

    public GridBitIntervals(int nx, int ny, int nz, int orientation ){
        this(nx, ny, nz, orientation, 1., 1.);
    }
        
    public GridBitIntervals(int nx, int ny, int nz){
        this(nx, ny, nz, ORIENTATION_Z, 1., 1.);
    }
    
    public GridBitIntervals(int nx, int ny, int nz, int orientation, double pixelSize, double sliceHeight) { 

        super(nx, ny, nz,  pixelSize,sliceHeight,null);
        allocateData();

    }

    public GridBitIntervals(Bounds bounds, double pixelSize, double sliceHeight) {

        super(bounds,  pixelSize,sliceHeight);

        allocateData();

    }
    
    protected void allocateData(){
        // this is binary grid, make default attributeDesc
        m_gridDataDesc = GridDataDesc.getDefaultAttributeDesc(1);

        m_nx = width;
        m_ny = height;
        m_nz = depth;

        switch(m_orientation){
        default:
        case ORIENTATION_X:
            m_data = new RowOfInt[m_ny * m_nz];
            break;
        case ORIENTATION_Y:
            m_data = new RowOfInt[m_nx * m_nz];
            break;
        case ORIENTATION_Z:
            m_data = new RowOfInt[m_nx * m_ny];
            break;
        }

    }

    /**
       return raw data at given point
     */
    public long get(int x, int y, int z){

        switch(m_orientation){

        default:
        case ORIENTATION_X:
            {
                RowOfInt interval = m_data[y + m_ny * z];
                if(interval == null)
                    return 0;
                else
                    return interval.get(x);
            }
        case ORIENTATION_Y:
            {
                RowOfInt interval = m_data[x + m_nx * z];
                if(interval == null)
                    return 0;
                else
                    return interval.get(y);
            }
        case ORIENTATION_Z:
            {
                int ind = x + m_nx * y;
                if(ind < 0){
                    throw new IllegalArgumentException(fmt("x: %d, y: %d, ind: %d\n", x,y,ind));
                    //return 0;
                }
                RowOfInt interval = m_data[x + m_nx * y];
                if(interval == null)
                    return 0;
                else
                    return interval.get(z);
            }
        }

    }

    /**
       set one x,y column of this grid to given intervals and values

       intervals[] - ordered array of starts of intervals of identical pixels
       values[] - values of pixels in each interval

     */
    public void setIntervals(int x, int y, int intervals[], int values[], int count){

        if(m_orientation != ORIENTATION_Z){
            // TODO
            throw new IllegalArgumentException("Not implemented");
        } else {
            int ind = x + m_nx * y;
            RowOfInt interval = m_data[ind];
            if(interval == null){
                m_data[ind] = interval = newInterval();
            }
            interval.setIntervals(intervals, values, count);
        }
    }


    /**
       set raw data at given point
     */
    public void set(int x, int y, int z, long value){

        switch(m_orientation){

        default:
        case ORIENTATION_X:
            {
                int ind = y + m_ny * z;
                RowOfInt interval = m_data[ind];
                if(interval == null){
                    m_data[ind] = interval = newInterval();
                }
                interval.set(x, value);
                break;
            }
        case ORIENTATION_Y:
            {
                int ind = x + m_nx * z;
                RowOfInt interval = m_data[ind];
                if(interval == null){
                    m_data[ind] = interval = newInterval();
                }
                interval.set(y, value);
                break;
            }
        case ORIENTATION_Z:
            {
                int ind = x + m_nx * y;
                RowOfInt interval = m_data[ind];
                if(interval == null){
                    m_data[ind] = interval = newInterval();
                }
                interval.set(z, value);
                break;
            }
        }

    }

    /**

     */
    public void clear(){

        for(int i =0; i < m_data.length; i++){
            RowOfInt ri = m_data[i];
            if(ri != null){
                ri.clear();
            }
        }
    }

    public void release(){

        for(int i =0; i < m_data.length; i++){
            RowOfInt ri = m_data[i];
            if(ri != null){
                ri.release();
                m_data[i] = null;
            }
        }
    }

    /**
       method to be return new interval (to be overriddenb by subclass)
     */
    protected RowOfInt newInterval(){

        return new BitIntervals();

    }


    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        return new VoxelDataByte();
    }

    public void getData(int x, int y, int z, VoxelData data){

        long encoded = get(x,y,z);
        long att = encoded;
        byte state = (byte)encoded;
        data.setData(state,att);
    }

    public byte getState(int x, int y, int z){
        return (byte)get(x,y,z);
    }

    public void setState(int x, int y, int z, byte state){
        set(x,y,z,(long)state);
    }

    public int findCount(VoxelClasses vc){

        VoxelCounter counter = new VoxelCounter();
        findInterruptible(vc, counter);

        //TODO - possible loss of data for large grids
        return (int)counter.getCount();

    }

    /**
       class to count voxels of given type
       used via ClassTraverser interface
     */
    static class VoxelCounter implements ClassTraverser {

        long m_count = 0;
        public void found(int x, int y, int z, byte state){
            m_count++;
        }

        public boolean foundInterruptible(int x, int y, int z, byte state){

            m_count++;
            return true;
        }
        long getCount(){
            return m_count;
        }
    }

    public int findCount(long mat){

        //TODO
        return super.findCount(mat);

    }

    public void find(VoxelClasses vc, ClassTraverser t){

        //printf("%s.find(%s)\n",this, vc);

        RowProcesser rp = new RowProcesser(t);
        int data;

        switch(vc){
        default:
            //printf(" find() default procesing\n");
            // go to default routine
            super.find(vc,  t);
            return;
        case INSIDE:
            data = 1;
            break;
        }

        //printf(" find() custom procesing: y: %d x: %d\n",m_ny,m_nx);

        for(int y = 0; y < m_ny; y++){
            for(int x = 0; x < m_nx; x++){
                RowOfInt row = m_data[x + m_nx*y];
                if(row == null) continue;
                rp.setXY(x,y);
                row.find(data, rp );
            }
        }
    }

    public void findMT(VoxelClasses vc, ClassTraverser t, int threadCount){

        //printf("%s.find(%s)\n",this, vc);

        RowProcesser rp = new RowProcesser(t);
        int data;

        switch(vc){
            default:
                //printf(" find() default procesing\n");
                // go to default routine
                super.find(vc,  t);
                return;
            case INSIDE:
                data = 1;
                break;
        }

        ConcurrentLinkedQueue<MTSlice> slices = new ConcurrentLinkedQueue<MTSlice>();

        int sliceHeight = 1;

        for (int y = 0; y < height; y += sliceHeight) {
            int ymax = y + sliceHeight;
            if (ymax > height)
                ymax = height;

            if (ymax > y) {
                // non zero slice
                slices.add(new MTSlice(y, ymax - 1));
            }
        }

        if (STATS) {
            printf("GridBitIntervals.findMT.  STATS mode.  slices: %d\n",slices.size());
        }
        if (threadCount == 0) {
            threadCount = Runtime.getRuntime().availableProcessors();
        }
        if (threadCount < 1) {
            threadCount = 1;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Runnable runner = new findMTVoxelClasses(vc,slices,this,t);

            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class findMTVoxelClasses implements Runnable {

        private ConcurrentLinkedQueue<MTSlice> slices;
        private VoxelClasses vc;
        private GridBitIntervals grid;
        private ClassTraverser t;

        /** How many slices where processed.  Only collected with STATS true */
        private long slicesProcessed;

        public findMTVoxelClasses(VoxelClasses vc, ConcurrentLinkedQueue<MTSlice> slices, GridBitIntervals grid, ClassTraverser t) {
            this.vc = vc;
            this.slices = slices;
            this.grid = grid;
            this.t = t;
        }

        public void run() {
            int width = grid.getWidth();
            int depth = grid.getDepth();
            RowProcesser rp = new RowProcesser(t);

            while (true) {
                MTSlice slice = slices.poll();

                if (slice == null) {
                    // end of processing
                    break;
                }

                if (STATS) {
                    slicesProcessed++;
                }

                int len_x = width;
                int len_y = slice.ymax;
                int len_z = depth;

                int m_nx = grid.m_nx;
                for(int y = slice.ymin; y <= len_y; y++){
                    for(int x = 0; x <= len_x; x++){
                        RowOfInt row = grid.m_data[x + m_nx*y];
                        if(row == null)
                            continue;
                        rp.setXY(x,y);
                        row.find(1, rp);
                    }
                }


            }

            if (STATS) {
                printf("GridBitIntervals.findMT stats.  Thread: %s  SlicesProcessed: %6d\n",Thread.currentThread().getName(),slicesProcessed);
            }

        }
    }


    public void find(VoxelClasses vc, ClassTraverser t, int xmin, int xmax, int ymin, int ymax){

        //printf("%s.find(%s,[%d,%d,%d,%d])\n",this, vc, xmin, xmax, ymin, ymax);

        //printf("vc: %s\n",vc);

        RowProcesser rp = new RowProcesser(t);
        int data;

        switch(vc){
        default:
            // go to default routine
            //printf(" find() default processing\n");
            super.find(vc,  t, xmin, xmax, ymin, ymax);
            return;

        case INSIDE:
            data = 1;
            break;
        }

        //printf(" find() custom procesing\n");

        for(int y = ymin; y <= ymax; y++){
            for(int x = xmin; x <= xmax; x++){
                RowOfInt row = m_data[x + m_nx*y];
                if(row == null)
                    continue;
                rp.setXY(x,y);
                row.find(data, rp );
            }
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster than
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findInterruptible(VoxelClasses vc, ClassTraverser t) {

        //printf("findInterruptible(%s, %s)\n", vc, t);

        RowProcesser rp = new RowProcesser(t);
        int data;

        switch(vc){
        default:
            // go to default routine
            super.findInterruptible(vc,  t);
            return;
        case INSIDE:
            data = 1;
            break;
        }

        // TODO different orientation ?
        for(int y = 0; y < m_ny; y++){
            for(int x = 0; x < m_nx; x++){
                RowOfInt row = m_data[x + m_nx*y];
                if(row == null) continue;
                rp.setXY(x,y);
                if(!row.findInterruptible(data, rp ))
                    return;
            }
        }
    }

    /**
       utility class to traverse one row of data
     */
    static class RowProcesser implements IntervalTraverser {

        ClassTraverser m_t;
        int x, y;
        RowProcesser(ClassTraverser t){
            m_t = t;
        }

        void setXY(int x, int y){
            this.x = x;
            this.y = y;
        }

        public boolean foundInterruptible(int z, long data){

            return m_t.foundInterruptible(x,y,z, (byte)data);

        }
        public void found(int z, long data){

            m_t.found(x,y,z, (byte)data);

        }

    }

    /**
       interface Grid
     */
    public Object clone(){

        GridBitIntervals ret_val = new GridBitIntervals(this);
        
        BaseGrid.copyBounds(this, ret_val);
        return ret_val;

    }

    public Grid createEmpty(int w, int h, int d, double pixel, double sheight){

        return new GridBitIntervals(w, h, d, m_orientation, pixel,sheight);
        
    }

    public long getAttribute(int x, int y, int z){
        // attribute is the same as state in bit grid 
        return get(x,y,z);
        // no attribute in bit grid
        //return Grid.NO_MATERIAL;
    }

    public void setData(int x, int y, int z, byte state, long attribute){
        set(x,y,z,state);
    }

    public void setAttribute(int x, int y, int z, long attribute){
        set(x,y,z,attribute);
    }

    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t){
        //TODO
        super.findAttribute(vc, t);
    }

    public void findAttributeInterruptible(VoxelClasses vc, ClassAttributeTraverser t){
        //TODO
        super.findAttributeInterruptible(vc, t);
    }

    public void findAttribute(long mat, ClassAttributeTraverser t){
        //TODO
        super.findAttribute(mat, t);
        return;
    }

    public void findAttribute(VoxelClasses vc, long mat, ClassAttributeTraverser t){
        //TODO
        super.findAttribute(vc, mat, t);
    }

    public void findAttributeInterruptible(long mat, ClassAttributeTraverser t){
        //TODO
        super.findAttributeInterruptible(mat, t);
    }

    public void findAttributeInterruptible(VoxelClasses vc, long mat, ClassAttributeTraverser t){
        //TODO
        super.findAttributeInterruptible(vc,mat, t);
    }

    public void removeAttribute(long mat){
        //TODO
        super.removeAttribute(mat);

    }

    public void reassignAttribute(long[] attributes, long matID){
        //TODO
        super.reassignAttribute(attributes, matID);
    }


    public static boolean compareGrids(AttributeGrid grid1, AttributeGrid grid2 ){

        GridBitIntervals g1 = (GridBitIntervals)grid1;
        GridBitIntervals g2 = (GridBitIntervals)grid2;

        int nx = g1.getWidth();
        int ny = g1.getHeight();
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int ind = x + nx*y;

                BitIntervals r1 = (BitIntervals)g1.m_data[ind];
                BitIntervals r2 = (BitIntervals)g2.m_data[ind];

                if(r1 != null && r2 != null){

                    if(r1.compareIntevals(r2) != 0){
                        printf("x: %d, y: %d\n", x, y);
                        r1.dump();
                        r2.dump();
                        //return false;

                    }
                } else if((r1 != null && r2 == null) || (r1 == null && r2 != null)){
                    printf("x: %d, y: %d, r1: %s r2: %s\n",x,y,r1, r2);
                    if(r1 != null){
                        printf("r1:");
                        r1.dump();
                    }
                    if(r2 != null){
                        printf("r2:");
                        r2.dump();
                    }
                    //return false;
                }
            }
        }
        return true;
    }

    public void printStat(){

        long emptyCount = 0;
        long filledCount = 0;
        long dataMemory = 0;

        for(int k = 0; k < m_data.length; k++){
            RowOfInt row = m_data[k];
            if(row != null){
                filledCount++;
                dataMemory += row.getDataMemory();
            } else
                emptyCount++;
        }

        printf("*** GridBitIntervals stats\n ");
        printf("2D fill ratio:%5.2f%%\n",100.*filledCount/(filledCount + emptyCount) );
        printf("data memory size: %dMB (%d bytes)\n",dataMemory/1000000,dataMemory);
        printf("array memory size: %dMB\n",(m_nx*m_ny)/1000000);
        int objectSize = 20;
        printf("objects memory size: %dMB\n",(filledCount*objectSize)/1000000);
        printf("*** end GridBitIntervals stat\n ");

    }


} // GridBitIntervals

