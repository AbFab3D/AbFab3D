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


import abfab3d.grid.util.ExecutionStoppedException;
import abfab3d.util.DataSource;
import abfab3d.util.Vec;
import abfab3d.util.VecTransform;
import abfab3d.util.Initializable;


import abfab3d.grid.Grid;



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

    private double voxelX, voxelY, voxelZ, offsetX, offsetY, offsetZ;

    Grid m_grid; 

    public void setDataSource(DataSource dataSource){
        m_dataSource = dataSource;
    }

    public void setTransform(VecTransform transform){
        m_transform = transform;
    }

    public void setMargin(int margin){

        m_margin = margin;

    }

    public void setBounds(double bounds[]){

        m_centerX = (bounds[0] + bounds[1])/2;
        m_centerY = (bounds[2] + bounds[3])/2;
        m_centerZ = (bounds[4] + bounds[5])/2;
        
        m_sizeX = bounds[1] - bounds[0];
        m_sizeY = bounds[3] - bounds[2];
        m_sizeZ = bounds[5] - bounds[4];

    }

    public int makeGrid(Grid grid){

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

        m_grid = grid;

        int 
            nx = grid.getWidth(),
            ny = grid.getHeight(),
            nz = grid.getDepth();

        makeTransform();
        if(m_transform == null)
            m_transform = new VecTransforms.Identity();

        if(m_transform instanceof Initializable){
            ((Initializable)m_transform).initialize();
        }
        if(m_dataSource instanceof Initializable){
            ((Initializable)m_dataSource).initialize();
        }
       

        Vec 
            pntGrid = new Vec(3),
            pntWorld = new Vec(3),            
            pntData = new Vec(3),
            dataValue = new Vec(2);
        
        int margin = m_margin; 

        int nx1 = nx-margin;
        int ny1 = ny-margin;
        int nz1 = nz-margin;

        for(int iy = margin; iy < ny1; iy++){

            for(int ix = margin; ix < nx1; ix++){

                for(int iz = nz1-1; iz >= margin; iz--){ // z-order to speed up creation of GridIntervals
                    
                    pntGrid.set(ix, iy, iz);
                    transformToWorldSpace(pntGrid, pntWorld);
                    int res = m_transform.inverse_transform(pntWorld, pntData);
                    if(res != VecTransform.RESULT_OK)
                        continue;
                    res = m_dataSource.getDataValue(pntData, dataValue);
                    if(res != VecTransform.RESULT_OK)
                        continue;
                    
                    if(dataValue.v[0] > 0.5){
                        m_grid.setState(ix, iy, iz, Grid.INTERIOR);
                    }
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }
        }

        return VecTransform.RESULT_OK;

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

}
