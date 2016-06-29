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

package abfab3d.core;

/**
   interface to supply object with VecTransform 
   
 */
public interface Transformer {

    /**
       feeds all triangles into supplied TriangleCollector 

       returns true if success, false if faiure        
     */
    public void setTransform(VecTransform vt);
    
}
