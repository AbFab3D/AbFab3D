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
import javax.vecmath.Vector4d;

import abfab3d.core.Vec;

import static abfab3d.symmetry.SymmetryUtil.dot;
import static abfab3d.symmetry.SymmetryUtil.subSet;

/**
 *
 * class to represent half space bounded by euclidean plane
 * plane is defined via external normal and distance to origin
 *
 *  @author Vladimir Bulatov
 * 
 */
public class EPlane extends SPlane {
    
    public double dist;
    // normal to the plane normalized to 1 
    public double nx, ny, nz;
    public EPlane(double nx,double ny,double nz,double distance){
        this(new Vector3d(nx, ny, nz), distance);
    }

    /**
       EPlane constructed from Vectro4d 
     */
    public EPlane(Vector4d plane){
        this(new Vector3d(plane.x, plane.y, plane.z), -plane.w);
    }
    public EPlane(Vector3d normal, double distance){
        
        double len = normal.length();
        nx = normal.x / len;
        ny = normal.y / len;
        nz = normal.z / len;
        dist = distance; 
    }
        
    /**
       @return normal parameter of plane 
     */
    public Vector3d getNormal(){
        return new Vector3d(nx, ny, nz);
    }
    
    /**
       @return distance parameter of plane 
     */
    public double getDist(){
        return dist;
    }
    

    public double distance(Vec pnt){
        // dot(p,normal) - dist 
        return dot(pnt, nx, ny, nz)-dist; 
        
    }
    
    public void transform(Vec p){
        
        
        //vn = dot( v - normal*dist, normal);             
        double vn = 2*((p.v[0] - nx*dist)*nx + (p.v[1] - ny*dist)*ny + (p.v[2] - nz*dist)*nz); 
        //p -= normal*vn
        subSet(p, nx * vn,ny * vn,nz * vn);            
        
    }
} // class Plane 

