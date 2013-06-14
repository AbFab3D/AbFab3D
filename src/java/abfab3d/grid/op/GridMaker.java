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

import java.util.Stack;


import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;

import abfab3d.grid.util.ExecutionStoppedException;
import abfab3d.util.DataSource;
import abfab3d.util.Vec;
import abfab3d.util.VecTransform;
import abfab3d.util.Initializable;


import static abfab3d.util.Output.time;
import static abfab3d.util.Output.printf;

/**
   class takes premade grid, transfromation and data source and fills the grid's voxel if data according to value of data source 
 */
public class GridMaker {
    
    
    protected VecTransform m_transform;
    protected DataSource m_dataSource;

    protected double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.1; 
    protected double m_centerX = 0, m_centerY = 0, m_centerZ = 0;  

    // margin around the grid boundary to be kept empty
    protected int m_margin = 1; 
    // threads count to use 
    protected int m_threadCount = 0;

    private double voxelX, voxelY, voxelZ, offsetX, offsetY, offsetZ;
    private int m_slizeSize = 2;

    AttributeGrid m_grid; 
    int m_nx, m_ny, m_nz;
    int gridMaxAttributeValue = 0;
    double voxelSize = 0;

    public void setDataSource(DataSource dataSource){
        m_dataSource = dataSource;
    }

    public void setTransform(VecTransform transform){
        m_transform = transform;
    }

    public void setMargin(int margin){

        m_margin = margin;

    }

    public void setThreadCount(int count){

        m_threadCount = count;

    }

    /**
       set width of transitional surface area for shape calculations 
     */
    public void setVoxelSize(double vs){

        this.voxelSize = vs; 
    } 
    
    /**
       sets scaling value for attributes
     */
    public void setMaxAttributeValue(int value){

        gridMaxAttributeValue = value;

    }

    public void setBounds(double bounds[]){

        m_centerX = (bounds[0] + bounds[1])/2;
        m_centerY = (bounds[2] + bounds[3])/2;
        m_centerZ = (bounds[4] + bounds[5])/2;
        
        m_sizeX = bounds[1] - bounds[0];
        m_sizeY = bounds[3] - bounds[2];
        m_sizeZ = bounds[5] - bounds[4];

    }

    public void makeGrid(Grid grid){

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

        m_grid = (AttributeGrid)grid;
         
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();

        long t0 = time();

        makeTransform();
        if(m_transform == null)
            m_transform = new VecTransforms.Identity();

        if(m_transform instanceof Initializable){
            ((Initializable)m_transform).initialize();
        }
        if(m_dataSource instanceof Initializable){
            ((Initializable)m_dataSource).initialize();
        }

        //printf("data initialization %d ms\n", (time() - t0));
        
        t0 = time();
        if(m_threadCount > 0)
            makeGridMT();
        else 
            makeGridST();
        //printf("grid rendering: %d ms\n", (time() - t0));
    } 

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

    void makeGridST(){
        
        Vec 
            pntGrid = new Vec(3),
            pntWorld = new Vec(3),            
            pntData = new Vec(3),
            dataValue = new Vec(3);
        
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

                    pntWorld.voxelSize = voxelSize;

                    int res = m_transform.inverse_transform(pntWorld, pntData);

                    //pntData.voxelSize = voxelSize;

                    if(res != VecTransform.RESULT_OK)
                        continue;
                    res = m_dataSource.getDataValue(pntData, dataValue);
                    if(res != VecTransform.RESULT_OK)
                        continue;
                    
                    switch(gridMaxAttributeValue){
                        
                    case 0: // use grid state 
                        if(dataValue.v[0] > 0.5){
                            m_grid.setState(ix, iy, iz, Grid.INSIDE);
                        }
                        break;
                    default: // use grid attribute
                        int v = (int)(gridMaxAttributeValue * dataValue.v[0] + 0.5);
                        if(v > 0){
                            m_grid.setData(ix, iy, iz, Grid.INSIDE, v);
                        }
                        break;
                    }
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }
        }

    }
    

    class SliceMaker implements Runnable{
        
        SliceSet slices;

        Vec // storage for calculations 
            pntGrid = new Vec(3),
            pntWorld = new Vec(3),            
            pntData = new Vec(3),
            dataValue = new Vec(3);

        SliceMaker(SliceSet slices ){

            this.slices = slices; 

        }
        
        public void run(){

            while(true){

                Slice slice = slices.getNextSlice();
                if(slice == null)
                    break;
                makeSlice(slice);
                
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

                        pntWorld.voxelSize = voxelSize;

                        int res = m_transform.inverse_transform(pntWorld, pntData);
                        if(res != VecTransform.RESULT_OK)
                            continue;                        

                        res = m_dataSource.getDataValue(pntData, dataValue);

                        if(res != VecTransform.RESULT_OK)
                            continue;
                        
                        switch(gridMaxAttributeValue){
                            
                        case 0: // use grid state 
                            if(dataValue.v[0] > 0.5){
                                m_grid.setState(ix, iy, iz, Grid.INSIDE);
                            }
                            break;
                        default: // use grid attribute
                            int v = (int)(gridMaxAttributeValue * dataValue.v[0] + 0.5);
                            if(v > 0){
                                m_grid.setData(ix, iy, iz, Grid.INSIDE, v);
                            }
                            break;
                        }
                    }
                }
            }              
        }
    }


    void transformToWorldSpace(Vec gridPnt, Vec worldPnt){

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
