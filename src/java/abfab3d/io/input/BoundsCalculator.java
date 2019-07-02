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

package abfab3d.io.input;


import javax.vecmath.Vector3d;

import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.Bounds;
import abfab3d.core.TriangleCollector;

import abfab3d.core.Vec;

import static abfab3d.core.Output.printf;


/**
   class to claculate bounds of triangle collection 

   @author Vladimir Bulatov
 */
public class BoundsCalculator implements TriangleCollector, AttributedTriangleCollector {
    
    
    double
        xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE,
        ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE,
        zmin = Double.MAX_VALUE, zmax = -Double.MAX_VALUE;

    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
       
        checkVertex(v0);
        checkVertex(v1);
        checkVertex(v2);

        return true; // to continue
    }

    @Override
    public boolean addAttTri(Vec v0, Vec v1, Vec v2) {
        checkVertex(v0);
        checkVertex(v1);
        checkVertex(v2);

        return true; // to continue
    }

    public void checkVertex(Vector3d v){

        double x = v.x;
        double y = v.y;
        double z = v.z;

        if(x < xmin) xmin = x;
        if(x > xmax) xmax = x;

        if(y < ymin) ymin = y;
        if(y > ymax) ymax = y;

        if(z < zmin) zmin = z;
        if(z > zmax) zmax = z;
        
    }

    public void checkVertex(Vec v){

        double x = v.v[0];
        double y = v.v[1];
        double z = v.v[2];

        if(x < xmin) xmin = x;
        if(x > xmax) xmax = x;

        if(y < ymin) ymin = y;
        if(y > ymax) ymax = y;

        if(z < zmin) zmin = z;
        if(z > zmax) zmax = z;

    }

    public void getBounds(double bounds[]){

        bounds[0] = xmin;
        bounds[1] = xmax;
        bounds[2] = ymin;
        bounds[3] = ymax;
        bounds[4] = zmin;
        bounds[5] = zmax;

    }

    public Bounds getBoundsObject() {
        Bounds bounds = new Bounds(xmin,xmax,ymin,ymax,zmin,zmax);

        return bounds;
    }

    /**
     * Reset all variables so this class can be reused;
     */
    public void reset() {
        xmin = Double.MAX_VALUE;
        xmax = -Double.MAX_VALUE;
        ymin = Double.MAX_VALUE;
        ymax = -Double.MAX_VALUE;
        zmin = Double.MAX_VALUE;
        zmax = -Double.MAX_VALUE;
    }
}

