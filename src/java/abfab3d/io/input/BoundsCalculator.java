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

import java.io.IOException;

import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;


import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;
import abfab3d.util.TriangleCollector;

import abfab3d.grid.Grid;

import static java.lang.System.currentTimeMillis;
import static abfab3d.util.Output.printf; 


/**
   class to claculate bounds of triangle collection 

   @author Vladimir Bulatov
 */
public class BoundsCalculator implements TriangleCollector {
    
    
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

    public void getBounds(double bounds[]){

        bounds[0] = xmin;
        bounds[1] = xmax;
        bounds[2] = ymin;
        bounds[3] = ymax;
        bounds[4] = zmin;
        bounds[5] = zmax;

    }

}

