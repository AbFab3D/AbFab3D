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
   class converets denstity stored in the grid as integer in the range [0, subvoxelResolution] inclusive 

   @author Vladimir Bulatov
 */

public class DensityMakerSubvoxel implements DensityMaker {

    double m_factor; 
    long m_maxAttribute;

    /**
       @param subvoxelResolution - maximal value of density 
     */
    public DensityMakerSubvoxel(long subvoxelResolution){

        if(subvoxelResolution <= 0)
            subvoxelResolution = 1;

        m_maxAttribute = subvoxelResolution;
        m_factor = 1./m_maxAttribute;

    }
    /**
       convert voxel attribute into voxel density to be used for the 3D shape generation 
     */
    public final double makeDensity(long attribute){
        if(attribute < 0) 
            return 0;
        else if(attribute >= m_maxAttribute) 
            return 1.;
        else 
            return (attribute*m_factor);
    }
}