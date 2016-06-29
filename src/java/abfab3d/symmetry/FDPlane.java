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
   interface to represent single side of fundamental domain 
   it shall return positive distance for point outside of fundamentral domain and negative distance for points inside 
   that sign is consistent with definition of distnce for shapes - outside of shape distance is positive , inside it is negative 
   @author Vladimir Bulatov
 */
public interface FDPlane {
    public double distance(Vec pnt);
} 
