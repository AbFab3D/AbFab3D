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

import javax.vecmath.Vector2d;

public class InterpolatorVector2d implements AInterpolator<Vector2d> {
    
    double maxDistance;

    public InterpolatorVector2d(double maxDistance){

        this.maxDistance = maxDistance;
    }
    
    public Vector2d midpoint(Vector2d pnt1, Vector2d pnt2){
        return new Vector2d(0.5*(pnt1.x + pnt2.x),0.5*(pnt1.y + pnt2.y));
    }
    public boolean needSubdivision(Vector2d pnt1, Vector2d pnt2){
        double 
            dx = pnt1.x - pnt2.x,
            dy = pnt1.y - pnt2.y;

        return (Math.sqrt(dx*dx + dy*dy) > maxDistance);
        
    }
    
}