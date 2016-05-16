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

import abfab3d.util.Vec;

/**
   class converts data grid data channel into density value 

   @author Vladimir Bulatov
 */

public class DensityMakerFromDensityChannel implements DensityMaker {


    GridDataChannel m_densityChannel;
    /**
       @param densityChannel - density channel of the grid 
     */
    public DensityMakerFromDensityChannel(GridDataChannel densityChannel){
        m_densityChannel = densityChannel;
    }

    /**
       convert voxel attribute into voxel density to be used for the 3D shape generation 
     */
    public final double makeDensity(long attribute){
        
        return m_densityChannel.getValue(attribute);
        
    }
    
}