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

import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.Output.printf;

/**
   class converts grid data channel into density value 

   @author Vladimir Bulatov
 */

public class DensityMakerFromDistanceChannel implements DensityMaker {
    
    static final boolean DEBUG = true;

    GridDataChannel m_distanceChannel;
    double m_surfaceValue;
    double m_surfaceThickness;


    /**
       @param distanceChannel - density channel of the grid 
     */
    public DensityMakerFromDistanceChannel(GridDataChannel distanceChannel, double surfaceValue, double surfaceThickness){

        if(DEBUG) printf("DensityMakerFromDistanceChannel(%s, %7.5f, %7.5f)\n",distanceChannel,surfaceValue, surfaceThickness);
        m_distanceChannel = distanceChannel;
        m_surfaceValue = surfaceValue;
        m_surfaceThickness = surfaceThickness;
        
    }

    /**
       convert voxel attribute into voxel density to be used for the 3D shape generation 
     */
    public final double makeDensity(long attribute){
        
        double dist = m_distanceChannel.getValue(attribute);

        //(m_surfaceValue - dist)/m_surfaceThickness;

        return step10(dist, m_surfaceValue, m_surfaceThickness); 
        
    }
    
}