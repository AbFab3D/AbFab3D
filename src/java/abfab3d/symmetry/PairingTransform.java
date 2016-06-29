/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.symmetry;
import abfab3d.core.Vec;

/**
   interface to represent pairing transform which maps tile adjacent to fundamental domain into funfdamental domain  
   @author Vladimir Bulatov
 */
public interface PairingTransform {
    public void transform(Vec pnt);
} 
