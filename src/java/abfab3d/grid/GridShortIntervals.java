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

import abfab3d.grid.Grid;
import abfab3d.grid.VoxelData;
import abfab3d.grid.VoxelDataByte;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.ClassAttributeTraverser;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
   grid array to represent short values
   Implemented as 2D array of intervals of shorts

   @author Vladimir Bulatov
*/
public class GridShortIntervals extends GridBitIntervals{

    //
    // state is store in 2 most signfican bit 
    // attribute is stored in 14 less significant bits 
    //
    static final int ATT_MASK = 0x3FFF; 
    static final int STATE_SHIFT = 14; 
    static final int STATE_MASK = 0xC000;
    static final int STATE_MASK_SHIFTED = 0x3;

    /**
       copy constrcutor 
     */
    public GridShortIntervals(GridShortIntervals grid){
        
        this(grid.m_nx,grid.m_ny,grid.m_nz,grid.m_orientation, grid.pixelSize, grid.sheight);
        
        for(int i = 0; i < m_data.length; i++){
            RowOfInt dataItem = grid.m_data[i];
            if(dataItem != null)
                m_data[i] = (RowOfInt)dataItem.clone();        
        }
        
    }

    public GridShortIntervals(int nx, int ny, int nz, double pixelSize, double sliceHeight){
        super(nx, ny, nz, pixelSize, sliceHeight);
    }

    public GridShortIntervals(int nx, int ny, int nz, int orientation, double pixelSize, double sliceHeight){
        super(nx, ny, nz, orientation, pixelSize, sliceHeight);
    }

    /**
       method to return new interval (to be overridden by subclass) 
     */
    protected RowOfInt newInterval(){

        return new ShortIntervals();

    }


    public Grid createEmpty(int w, int h, int d, double pixel, double sheight){
        
        return new GridShortIntervals(w, h, d, m_orientation, pixel, sheight); 

    }

    public void setState(int x, int y, int z, byte state){

        int curCode = get(x,y,z);

        int curState = (curCode & STATE_MASK);
        int newState = (state << STATE_SHIFT);
        if(newState == curState)
            return;
        
        int curAtt = curCode & ATT_MASK;
        
        int newCode = newState | curAtt;
        
        set(x,y,z,newCode);

    }

    public byte getState(int x, int y, int z){
        
        int code = get(x,y,z);

        return (byte)((code & STATE_MASK) >> STATE_SHIFT);

    }

    public void setAttribute(int x, int y, int z, int attribute){
        
        int curCode = get(x,y,z);
        int curAtt = curCode & ATT_MASK;
        if(curAtt == attribute)
            return;
        
        int newCode = (curCode & STATE_MASK) | attribute;
        
        set(x,y,z,newCode);
        
    }

    public int getAttribute(int x, int y, int z){

        int code = get(x,y,z);
        return code & ATT_MASK;

    }
    
    public void setData(int x, int y, int z, byte state, int attribute){
        set(x,y,z, (state << STATE_SHIFT)|attribute);
    }

    public void getData(int x, int y, int z, VoxelData data){  
        int code = get(x,y,z);
        data.setState((byte)((code & STATE_MASK) >> STATE_SHIFT));
        data.setMaterial(code & ATT_MASK );
    }


    /**
       interface Grid 
     */
    public Object clone(){

        return new GridShortIntervals(this);
        
    }

}
