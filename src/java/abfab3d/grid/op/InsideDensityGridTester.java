/** 
 *                        Shapeways, Inc Copyright (c) 2014
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

import abfab3d.core.AttributeGrid;

/**
   inside tester for shape represented as antialised density grid 
   @author Vladimir Bulatov
 */
public class InsideDensityGridTester implements InsideTester {
    
    int m_subvoxelResolution;
    int m_densityThreshold;
    AttributeGrid m_grid;
    

    /**
       @param grid density grid
       @param subvoxelResolution subvoxle resolutioj of density grid 
     */
    public InsideDensityGridTester(AttributeGrid grid, int subvoxelResolution){

        m_grid = grid;
        m_subvoxelResolution = subvoxelResolution;
        m_densityThreshold = subvoxelResolution/2;
    }
    
    public final boolean isInside(int x, int y, int z){

        long v = m_grid.getAttribute(x,y,z);
        if(v >= m_densityThreshold)
            return true;
        else 
            return false;
            
    }
}
