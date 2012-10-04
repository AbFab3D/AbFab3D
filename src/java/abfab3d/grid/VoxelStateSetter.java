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
import abfab3d.grid.ClassTraverser;




/**

   sets voxels of given grid to given state

   @author Vladimir Bulatov

*/
public class VoxelStateSetter implements ClassTraverser {

    Grid grid; 
    byte stateToSet;
    public VoxelStateSetter(Grid grid, byte stateToSet){
        this.grid = grid;
        this.stateToSet = stateToSet;
    }
    
    public void found(int x, int y, int z, byte state){
        foundInterruptible(x,y,z,state);            
    }
    
    public boolean foundInterruptible(int x, int y, int z, byte state){
        grid.setState(x,y,z,stateToSet);
        return true;
    }
}

