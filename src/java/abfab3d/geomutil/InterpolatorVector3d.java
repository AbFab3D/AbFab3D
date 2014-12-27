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

package abfab3d.geomutil;

import javax.vecmath.Vector3d;

public class InterpolatorVector3d implements AInterpolator<Vector3d> {
    
    double maxDistance;

    public InterpolatorVector3d(double maxDistance){

        this.maxDistance = maxDistance;
    }
    
    public Vector3d midpoint(Vector3d pnt1, Vector3d pnt2){
        return new Vector3d(0.5*(pnt1.x + pnt2.x),0.5*(pnt1.y + pnt2.y),0.5*(pnt1.z + pnt2.z));
    }
    public boolean needSubdivision(Vector3d pnt1, Vector3d pnt2){
        double 
            dx = pnt1.x - pnt2.x,
            dy = pnt1.y - pnt2.y,
            dz = pnt1.z - pnt2.z;

        return (Math.sqrt(dx*dx + dy*dy + dz*dz) > maxDistance);
        
    }
    
}