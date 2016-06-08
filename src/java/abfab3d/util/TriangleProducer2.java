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

package abfab3d.util;

/**
   interface which generates a raw mesh of triangles
   
 */
public interface TriangleProducer2 extends TriangleProducer {

    /**
       feeds all triangles into supplied TriangleCollector 

       returns true if success, false if faiure        
     */
    public boolean getTriangles2(TriangleCollector2 tc);
    
}
