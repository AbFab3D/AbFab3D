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

import javax.vecmath.Vector3d;
import abfab3d.util.Vec;

import static abfab3d.symmetry.SymmetryUtil.len2; 
import static abfab3d.symmetry.SymmetryUtil.subSet;
import static abfab3d.symmetry.SymmetryUtil.mulSet;
import static abfab3d.symmetry.SymmetryUtil.addSet;
 
/**
 * class to represent sphere
 *
 *
 * @author Vladimir Bulatov
 */
public class Sphere extends SPlane {
    
    // radius of sphere 
    public double r; 
    protected double r2; 
    // center of sphere 
    public double cx, cy, cz;
    // positive radius gives negative distances inside of sphere 
    // negative radius gives positive distances inside of sphere 
    // 
    public Sphere(Vector3d center, double radius){
        
        r = radius;
        r2 = r*r;
        cx = center.x;
        cy = center.y;
        cz = center.z;
    }

    public Vector3d getCenter(){
        return new Vector3d(cx, cy, cz);
    }

    public double getRadius(){
        return r;
    }
    
    public double distance(Vec p){
        
        double 
            x = p.v[0] - cx,
            y = p.v[1] - cy,
            z = p.v[2] - cz;
        
        double dot = len2(x,y,z);
        
        return 0.5*(dot/r-r);
        
    }
    
    public void transform(Vec pnt){
        
        //v = v - s.center;
        subSet(pnt, cx, cy, cz);
        
        double len2 = len2(pnt);        
        double factor = r2/len2;
        //v *= factor;
        mulSet(pnt, factor);
        double scaling = factor;
        pnt.mulScale(scaling);
        //v += s.center;
        addSet(pnt, cx, cy, cz);
        
    }
} // class Sphere

