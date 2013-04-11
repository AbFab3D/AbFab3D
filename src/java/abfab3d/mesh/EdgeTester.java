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

package abfab3d.mesh;


import abfab3d.util.StructMixedData;

/**
   interface return true if it is OK to collapse the Edge
 */
public interface EdgeTester {

    // method to initilize tester for given mesh 
    public void initialize(TriangleMesh mesh);

    // return false if edge can't be collapsed 
    public boolean canCollapse(int srcIdx);

    public Object clone();

}

