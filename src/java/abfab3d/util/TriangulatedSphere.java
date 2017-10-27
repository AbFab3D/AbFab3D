/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2014
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

import javax.vecmath.Vector3d;

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;

import static abfab3d.core.MathUtil.distance;
import static abfab3d.core.MathUtil.midPoint;
import static abfab3d.core.Units.MM;

import static java.lang.Math.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;

/**
   makes triangulated sphere of given radius and center and subdivion level 
*/
public class TriangulatedSphere  implements TriangleProducer {
    
    Vector3d center = new Vector3d();
    double radius; 
    int subdivision;
    double m_tolerance = 0.;
    
    // corner vertices of octahedron 
    Vector3d 
        v100 = new Vector3d(1,0,0),
        v_100 = new Vector3d(-1,0,0),
        v010 = new Vector3d(0,1,0),
        v0_10 = new Vector3d(0,-1,0),
        v001 = new Vector3d(0,0,1),
        v00_1 = new Vector3d(0,0,-1);
    
    
    public TriangulatedSphere(double radius, Vector3d center, int subdivision){
        this(radius, center, subdivision, 0.);
    }

    public TriangulatedSphere(double radius, Vector3d center, int subdivision, double tolerance){
        
        this.center.set(center);        
        this.radius = radius; 
        this.subdivision = subdivision;
        m_tolerance = tolerance;

    }
    
    public void setTolerance(double tolerance){
        m_tolerance = tolerance;
    }
    
    public boolean getTriangles(TriangleCollector tc){
        
        splitTriangle(tc, v100, v010, v001, subdivision);
        splitTriangle(tc, v100, v001, v0_10, subdivision);
        splitTriangle(tc, v100, v0_10, v00_1, subdivision);
        splitTriangle(tc, v100, v00_1, v010, subdivision);
        
        splitTriangle(tc, v_100, v001,v010,  subdivision);
        splitTriangle(tc, v_100, v0_10,v001,  subdivision);
        splitTriangle(tc, v_100, v00_1,v0_10,  subdivision);
        splitTriangle(tc, v_100, v010,v00_1,  subdivision);
        
        return true;
    }      
    
    
    
    protected void splitTriangle(TriangleCollector tc, Vector3d v0, Vector3d v1, Vector3d v2, int subdiv){
        
        if(subdiv <= 0){
            addTri(tc, v0, v1, v2);
            return;                
        }
        
        Vector3d v01 = null, v12=null, v20=null;
        
        int selector = 0;
        if(needSubdivision(v0, v1)) { selector += 1;v01 = getSpherePoint(v0, v1);}
        if(needSubdivision(v1, v2)) { selector += 2;v12 = getSpherePoint(v1, v2); }
        if(needSubdivision(v2, v0)) { selector += 4;v20 = getSpherePoint(v2, v0); }
        
        subdiv--;
        
        switch(selector){                
            
        case 0: // no splits 
            addTri(tc, v0, v1, v2);
            break;            
        case 1: // split 01   
            splitTriangle(tc, v0, v01, v2, subdiv);
            splitTriangle(tc, v01, v1, v2, subdiv);
            break;
            
        case 2:  // split 12         
            splitTriangle(tc, v0, v1, v12, subdiv);
            splitTriangle(tc, v0, v12, v2, subdiv);
            break;
            
        case 4:  // split 20 
            splitTriangle(tc, v1, v2, v20,subdiv);
            splitTriangle(tc, v1, v20, v0, subdiv);
            break;        
            
        case 3:  // split 01, 12         
            splitTriangle(tc, v1, v12, v01,subdiv);
            
            if(distance(v01, v2) < distance(v0, v12)) {
                splitTriangle(tc, v01, v12, v2, subdiv);
                splitTriangle(tc, v0, v01, v2, subdiv);
            } else {
                splitTriangle(tc, v01, v12, v0,subdiv);
                splitTriangle(tc, v0, v12, v2,subdiv);                
            }
            break;
            
            
        case 6:  //split 12 20 
            
            splitTriangle(tc, v12,v2, v20,subdiv );
            if(distance(v0, v12) < distance(v1, v20)) {
                splitTriangle(tc, v0, v12,v20,subdiv);
                splitTriangle(tc, v0, v1, v12,subdiv);
            } else {
                splitTriangle(tc, v0, v1, v20,subdiv);
                splitTriangle(tc, v1, v12, v20,subdiv);
            }
            break;
            
        case 5:  // split 01, 20 
            splitTriangle(tc, v0, v01, v20, subdiv);
            if(distance(v01, v2) < distance(v1, v20)){
                splitTriangle(tc, v01, v2, v20,subdiv);
                splitTriangle(tc, v01, v1, v2, subdiv);
            } else {
                splitTriangle(tc, v01, v1, v20,subdiv);                
                splitTriangle(tc, v1, v2, v20, subdiv);                
            }
            break; // split s0, s2       
            
        case 7: // split 01, 12, 20       
            
            splitTriangle(tc, v0, v01, v20,subdiv);
            splitTriangle(tc, v1, v12, v01,subdiv);
            splitTriangle(tc, v2, v20, v12,subdiv);
            splitTriangle(tc, v01, v12, v20, subdiv);
            break;                  
        } // switch()
        
    } // void splitTriangle()
    
    void addTri(TriangleCollector tc, Vector3d v0, Vector3d v1, Vector3d v2){
        
        tc.addTri(getScaledTri(v0),getScaledTri(v1),getScaledTri(v2));
    }
    
    boolean needSubdivision(Vector3d v0, Vector3d v1, Vector3d v2){
            
        return 
            needSubdivision(v0, v1) || 
            needSubdivision(v1, v2) || 
            needSubdivision(v2, v0);
        
    }
    
    boolean needSubdivision(Vector3d v0, Vector3d v1){
        
        return (radius* distance(midPoint(v0, v1), getSpherePoint(v0, v1)) > m_tolerance);
    }
    
    
    Vector3d getSpherePoint(Vector3d v1, Vector3d v2){
        
        double x = v1.x + v2.x;
        double y = v1.y + v2.y;
        double z = v1.z + v2.z;
        double r = sqrt(x*x + y*y + z*z);
        return new Vector3d(x/r, y/r, z/r);
        
    }
    
    
    Vector3d getScaledTri(Vector3d v){
        
        return new Vector3d(v.x * radius + center.x, v.y * radius + center.y, v.z * radius + center.z);
        
    }
    
} // class TriangulatedSphere 

