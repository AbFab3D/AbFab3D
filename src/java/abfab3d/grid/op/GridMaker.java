/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *                        Shapeways, Inc Copyright (c) 2012-2014
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

import java.util.Stack;


import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import abfab3d.grid.*;

import abfab3d.grid.util.ExecutionStoppedException;
import abfab3d.util.*;
import abfab3d.transforms.Identity;

import static abfab3d.util.Output.time;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
   class accept Grid , VecTransform, DataSource and AttributePacker and fills voxel attributes of the grid. 

   For each Grid voxel the cordinates of the voxel are transformed using inverse of VecTransform 
   The data value is calculated in the transformed point using DataSource 
   All the channels of the DataSource are converted into voxel attibutes using AttributePacker.
   This allows calculation of multi color and multimaterial grids with custom meaning and 
   resolutiuon of each AttributeChannel. 

   @author Vladimir Bulatov
   
 */
public class GridMaker implements Operation, AttributeOperation {

    
    static final int POINT_DIMENSION = 3;

    static final boolean DEBUG = true;
    static int debugCount = 0;
    
    protected VecTransform m_transform;
    protected DataSource m_dataSource;

    protected double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.1; 
    protected double m_centerX = 0, m_centerY = 0, m_centerZ = 0;  

    // margin around the grid boundary to be kept empty
    protected int m_margin = 0; 
    // threads count to use 
    protected int m_threadCount = 0;

    private double voxelX, voxelY, voxelZ, offsetX, offsetY, offsetZ;
    private int m_slizeSize = 2;

    // custom converter of Vec into long attribute
    AttributePacker m_attributePacker; 
    // dimension of the data channel 
    int m_dataChannelsCount = 1;
    // number of gray levels in the calculations this is being replaces by universal m_attributePacker
    long m_subvoxelResolution = 255;

    //
    AttributeGrid m_grid; 
    // diimensions of the grid 
    int m_nx, m_ny, m_nz;

    // actual voxel size of the grid 
    double voxelSize = 0;
    private boolean boundsSet = false;
    // this is thickness of surface transitional layer (relastive to the voxel size) 
    // data sources are expected to return transitional value inside of that layer
    private double voxelScale = Math.sqrt(3) / 2.0;

    public GridMaker() {
        m_threadCount = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
    }

    public void setSource(DataSource dataSource){
        m_dataSource = dataSource;
    }

    public void setTransform(VecTransform transform){
        m_transform = transform;
    }

    public void setMargin(int margin){

        m_margin = margin;

    }

    public void setThreadCount(int count){
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number)AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        m_threadCount = count;
    }

    /**
       set width of transitional surface area for shape calculations.
       it is obsolete and the value is ignored 
     */
    public void setVoxelSize(double vs){

        //this.voxelSize = vs; 
    }

    /**
       set relative width of transitional surface area for antialised density grid calculations
       default value sqrt(3)/2 - half of large diagonal of the unit cube 
     */
    public void setVoxelScale(double vs){

        this.voxelScale = vs;
    }

    /**
       sets scaling value for attributes
       @deprecated 
       is replaced by setAttributePacker(AttributePacker attributePacker) should be used instead 
     */
    public void setMaxAttributeValue(long value){

        setSubvoxelResolution(value);
        
    }

    /**
       sets subvoxel resolution used for antialiased grids 
       @deprecated 
       setAttributePacker(AttributePacker attributePacker) should be used instead 
     */
    public void setSubvoxelResolution(long value){
        
        m_subvoxelResolution = value;
        
    }

    /**
       sets converter from Vec into long for grid attributes 
     */
    public void setAttributePacker(AttributePacker attributePacker){

        m_attributePacker = attributePacker; 
    }

    /**
       obsolete. Bounds are stored in Grid 
     */
    public void setBounds(Bounds bounds){

        initBounds(bounds);
        boundsSet = true;
        
    }

    /**
     obsolete. Bounds are stored in Grid
     */
    public void setBounds(double bounds[]){

        initBounds(bounds);
        boundsSet = true;

    }

    public VecTransform getTransform() {
        return m_transform;
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    protected void initBounds(double bounds[]){

        m_centerX = (bounds[0] + bounds[1])/2;
        m_centerY = (bounds[2] + bounds[3])/2;
        m_centerZ = (bounds[4] + bounds[5])/2;
        
        m_sizeX = bounds[1] - bounds[0];
        m_sizeY = bounds[3] - bounds[2];
        m_sizeZ = bounds[5] - bounds[4];
    }

    protected void initBounds(Bounds bounds){

        m_centerX = (bounds.xmin + bounds.xmax)/2;
        m_centerY = (bounds.ymin + bounds.ymax)/2;
        m_centerZ = (bounds.zmin + bounds.zmax)/2;

        m_sizeX = bounds.xmax - bounds.xmin;
        m_sizeY = bounds.ymax - bounds.ymin;
        m_sizeZ = bounds.zmax - bounds.zmin;
    }

    public Grid execute(Grid grid) {
        makeGrid((AttributeGrid)grid);
        return grid;
    }
    
    public AttributeGrid execute(AttributeGrid grid) {
        makeGrid(grid);
        return grid;
    }

    /**
       perform the calculation of Grid voxel attributes 
     */
    public void makeGrid(AttributeGrid grid){
        if(m_dataSource == null) 
            throw new RuntimeException(fmt("DataSource is not set"));

        if (!boundsSet) {
            double[] bounds = new double[6];
            grid.getGridBounds(bounds);
            initBounds(bounds);
        }
        
        voxelSize = grid.getVoxelSize() * voxelScale;
        if(DEBUG)printf("gridMaker voxelSize: %7.3f mm\n", voxelSize/Units.MM);
        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

        
        m_grid = grid;
         
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();
        
        if(m_attributePacker == null){
            // no attibute maker given -> try to make one             
            GridDataDesc attDesc = m_grid.getDataDesc();
            if(attDesc == null) {
                // unknow grid 
                m_attributePacker = new AttributePackerDensity((int)m_subvoxelResolution);
            } else {
                m_attributePacker = attDesc.getAttributePacker();
            }
        }
        if(DEBUG)printf("GridMaker using attributePacker: %s\n",m_attributePacker);
        
        long t0 = time();

        makeTransform();
        if(m_transform == null)
            m_transform = new Identity();
       
        if(m_transform instanceof Initializable){
            ((Initializable)m_transform).initialize();
        }
        if(m_dataSource instanceof Initializable){
            ((Initializable)m_dataSource).initialize();
        }

        m_dataChannelsCount = m_dataSource.getChannelsCount();
        printf("GridMaker m_dataChannelsCount: %d\n",m_dataChannelsCount);
        if(DEBUG) printf("GridMaker data initialization %d ms\n", (time() - t0));

        if (m_threadCount == 0) {
            m_threadCount = Runtime.getRuntime().availableProcessors();
        }

        t0 = time();
        if(m_threadCount > 1)
            makeGridMT();
        else 
            makeGridST();
        if(DEBUG) printf("GridMaker grid rendering: %d ms\n", (time() - t0));
    } 

    /**
       multi thread version of makeGrid()
     */
    void makeGridMT(){

        SliceSet slices = new SliceSet(m_margin, m_grid.getHeight()-m_margin, m_slizeSize);

        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);
        for(int i = 0; i < m_threadCount; i++){
            executor.submit(new SliceMaker(slices));
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    /**
       single thread version of makeGrid()
     */
    void makeGridST(){
        
        Vec 
            pntGrid = new Vec(POINT_DIMENSION),
            pntWorld = new Vec(POINT_DIMENSION),            
            pntData = new Vec(POINT_DIMENSION),
            dataValue = new Vec(m_dataChannelsCount);
        if(DEBUG) printf("GridMaker.makeGridST(%d x %d x %d)\n", m_nx, m_ny, m_nz );
        int margin = m_margin; 
        int nx = m_nx, ny = m_ny, nz = m_nz;
        
        int nx1 = nx-margin;
        int ny1 = ny-margin;
        int nz1 = nz-margin;

        for(int iy = margin; iy < ny1; iy++){

            for(int ix = margin; ix < nx1; ix++){

                for(int iz = nz1-1; iz >= margin; iz--){ // this z-order to speed up creation of GridIntervals
                    
                    pntGrid.set(ix, iy, iz);
                    transformToWorldSpace(pntGrid, pntWorld);

                    pntWorld.setVoxelSize(voxelSize);

                    int res = m_transform.inverse_transform(pntWorld, pntData);
                    if(false){                        
                        double s = pntData.getScaleFactor();
                        if(s != 1.0 && debugCount-- > 0)
                            printf("scale: %10.5f\n", s);
                    }
                    //pntData.voxelSize = voxelSize;

                    if(res != VecTransform.RESULT_OK)
                        continue;
                    res = m_dataSource.getDataValue(pntData, dataValue);
                    if(res != VecTransform.RESULT_OK)
                        continue;
                    long vd = m_attributePacker.makeAttribute(dataValue);
                    if(vd != 0)
                    m_grid.setData(ix, iy, iz, Grid.INSIDE, vd);

                }
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }
        }

    }
    

    /**
       processof of single slice of grid 
     */
    class SliceMaker implements Runnable{
        
        SliceSet slices;

        Vec // storage for calculations 
            pntGrid = new Vec(POINT_DIMENSION),
            pntWorld = new Vec(POINT_DIMENSION),            
            pntData = new Vec(POINT_DIMENSION),
            dataValue = new Vec(m_dataChannelsCount);

        SliceMaker(SliceSet slices ){

            this.slices = slices; 

        }
        
        public void run(){
            try {
                while(true){
                    
                    Slice slice = slices.getNextSlice();
                    if(slice == null)
                        break;
                    makeSlice(slice);
                    
                }
            } catch(Exception e){
                e.printStackTrace(Output.out);
            } 
        }

        void makeSlice(Slice slice){
            
            int margin = m_margin; 
            int nx = m_nx, ny = m_ny, nz = m_nz;
            
            int nx1 = nx-margin;
            int ny1 = ny-margin;
            int nz1 = nz-margin;
            int ymin = slice.ymin;
            int ymax = slice.ymax;

            for(int iy = ymin; iy <= ymax; iy++){
                
                for(int ix = margin; ix < nx1; ix++){
                    
                    for(int iz = nz1-1; iz >= margin; iz--){ // this z-order to speed up creation of GridIntervals
                        //TODO make grid.setData() in one call 

                        pntGrid.set(ix, iy, iz);
                        transformToWorldSpace(pntGrid, pntWorld);

                        pntWorld.setVoxelSize(voxelSize);
                        
                        int res = m_transform.inverse_transform(pntWorld, pntData);
                        if(false){
                            double s = pntData.getScaleFactor();
                            if(s != 1.0 &&  debugCount-- > 0) {
                                printf("scale: %10.5f\n", s);
                            }
                        }
                        if(res != VecTransform.RESULT_OK)
                            continue;                        
                        res = m_dataSource.getDataValue(pntData, dataValue);

                        if(res != VecTransform.RESULT_OK)
                            continue;

                        long vd = m_attributePacker.makeAttribute(dataValue);
                        m_grid.setAttribute(ix, iy, iz, vd);
                    }
                }
            }              
        }
    }


    public void getTransform(Grid grid, double[] voxel, double[] offset) {
        voxel[0] = m_sizeX / grid.getWidth();
        voxel[1] = m_sizeY / grid.getHeight();
        voxel[2] = m_sizeZ / grid.getDepth();

        // half voxel shift get coordinate of the center of voxel
        offset[0] = m_centerX - m_sizeX/2 + voxelX/2;
        offset[1] = m_centerY - m_sizeY/2 + voxelY/2;
        offset[2] = m_centerZ - m_sizeZ/2 + voxelZ/2;
        //printf("size: %6.6f %6.6f %6.6f center: %6.6f %6.6f %6.6f voxel: %6.6f %6.6f %6.6f\n",m_sizeX,m_sizeY,m_sizeZ,m_centerX,m_centerY,m_centerZ,voxelX,voxelY,voxelZ);
    }

    void transformToWorldSpace(Vec gridPnt, Vec worldPnt){

        worldPnt.set(gridPnt);
        
        double in[] = gridPnt.v;
        double out[] = worldPnt.v;
        
        out[0] = in[0]*voxelX + offsetX;
        out[1] = in[1]*voxelY + offsetY;
        out[2] = in[2]*voxelZ + offsetZ;       

    }
    
    protected void makeTransform(){

        voxelX = m_sizeX / m_grid.getWidth();
        voxelY = m_sizeY / m_grid.getHeight();
        voxelZ = m_sizeZ / m_grid.getDepth();
        
        // half voxel shift get coordinate of the center of voxel
        offsetX = m_centerX - m_sizeX/2 + voxelX/2;
        offsetY = m_centerY - m_sizeY/2 + voxelY/2;
        offsetZ = m_centerZ - m_sizeZ/2 + voxelZ/2;

    }

    
    static class SliceSet {

        Stack<Slice> slices;
        
        SliceSet(int start, int end, int size){

            slices = new Stack<Slice>();
            
            for(int y = start; y < end; y+= size){
                int ymax = y + size;
                if(ymax > end)
                    ymax = end;
                if(ymax > y){
                    // non zero slice 
                    slices.push(new Slice(y, ymax-1));
                }
            }                
        }

        public synchronized Slice getNextSlice(){
            if(slices.empty())
                return null;            
            return slices.pop();
            
        }
    }

    static class Slice {

        int ymin;
        int ymax;
        Slice(){
            ymin = 0;
            ymax = -1;

        }
        Slice(int ymin, int ymax){

            this.ymin = ymin;
            this.ymax = ymax;
            
        }        
    }
}
