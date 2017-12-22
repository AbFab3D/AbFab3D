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

import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.Vec;

import static abfab3d.core.MathUtil.distance;
import static abfab3d.core.MathUtil.midPoint;
import static abfab3d.core.Units.MM;

import static java.lang.Math.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.acos;
import static java.lang.Math.PI;

/**
   makes triangulated sphere of given radius and center and subdivion level 
   
   @author Vladimir Bulatov 
*/
public class TriangulatedSphere  implements TriangleProducer, AttributedTriangleProducer {
    
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

    //
    //  initial vertices of octahedron have to be split in p(positive y) and m (negative y) to properly handle texture coordinates
    //  to avoid tear of texture along semi-circle (x < 0; y = 0) 
    //  at that line mapping of (x,y,z) -> (u,v) has discontinuity and we want to avoid to have triangles with tex coodinates on opposite 
    //  sides of discontinuity 
    //     
    Vec 
        av100p = new Vec(1,0,0,1,0),
        av100m = new Vec(1,0,0,-1,0),
        av_100p = new Vec(-1,0,0,1,0),
        av_100m = new Vec(-1,0,0,0,0),
        av010p = new Vec(0,1,0,1,0),
        av010m = new Vec(0,1,0,-1,0),
        av0_10p = new Vec(0,-1,0,1,0),
        av0_10m = new Vec(0,-1,0,-1,0),
        av001p = new Vec(0,0,1,1,0),
        av001m = new Vec(0,0,1,-1,0),
        av00_1p = new Vec(0,0,-1,1,0),
        av00_1m = new Vec(0,0,-1,-1,0);
    
    
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
    
    /**
       method of AttributedTriangleProducer
     */
    public boolean getAttTriangles(AttributedTriangleCollector tc){
        
        // positive y half space 
        splitAttTriangle(tc, av100p,  av010p,  av001p,  subdivision);
        splitAttTriangle(tc, av100p,  av00_1p, av010p,  subdivision);
        splitAttTriangle(tc, av_100p, av001p,  av010p,  subdivision);
        splitAttTriangle(tc, av_100p, av010p,  av00_1p, subdivision);
        // negative y half space 
        splitAttTriangle(tc, av100m,  av001m,  av0_10m, subdivision);
        splitAttTriangle(tc, av100m,  av0_10m, av00_1m, subdivision);
        splitAttTriangle(tc, av_100m, av0_10m, av001m,  subdivision);
        splitAttTriangle(tc, av_100m, av00_1m, av0_10m, subdivision);
        
        return true;
    }      

    /**
       method of AttributedTriangleProducer
     */
    public int getDataDimension(){
        return 5;
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
    

    
    protected void splitAttTriangle(AttributedTriangleCollector tc, Vec v0, Vec v1, Vec v2, int subdiv){
        
        if(subdiv <= 0){
            addAttTri(tc, v0, v1, v2);
            return;                
        }
        
        Vec v01 = null, v12=null, v20=null;
        
        int selector = 0;
        if(needAttSubdivision(v0, v1)) { selector += 1;v01 = getAttSpherePoint(v0, v1);}
        if(needAttSubdivision(v1, v2)) { selector += 2;v12 = getAttSpherePoint(v1, v2); }
        if(needAttSubdivision(v2, v0)) { selector += 4;v20 = getAttSpherePoint(v2, v0); }
        
        subdiv--;
        
        switch(selector){                
            
        case 0: // no splits 
            addAttTri(tc, v0, v1, v2);
            break;            
        case 1: // split 01   
            splitAttTriangle(tc, v0, v01, v2, subdiv);
            splitAttTriangle(tc, v01, v1, v2, subdiv);
            break;
            
        case 2:  // split 12         
            splitAttTriangle(tc, v0, v1, v12, subdiv);
            splitAttTriangle(tc, v0, v12, v2, subdiv);
            break;
            
        case 4:  // split 20 
            splitAttTriangle(tc, v1, v2, v20,subdiv);
            splitAttTriangle(tc, v1, v20, v0, subdiv);
            break;        
            
        case 3:  // split 01, 12         
            splitAttTriangle(tc, v1, v12, v01,subdiv);
            
            if(attDistance(v01, v2) < attDistance(v0, v12)) {
                splitAttTriangle(tc, v01, v12, v2, subdiv);
                splitAttTriangle(tc, v0, v01, v2, subdiv);
            } else {
                splitAttTriangle(tc, v01, v12, v0,subdiv);
                splitAttTriangle(tc, v0, v12, v2,subdiv);                
            }
            break;
            
            
        case 6:  //split 12 20 
            
            splitAttTriangle(tc, v12,v2, v20,subdiv );
            if(attDistance(v0, v12) < attDistance(v1, v20)) {
                splitAttTriangle(tc, v0, v12,v20,subdiv);
                splitAttTriangle(tc, v0, v1, v12,subdiv);
            } else {
                splitAttTriangle(tc, v0, v1, v20,subdiv);
                splitAttTriangle(tc, v1, v12, v20,subdiv);
            }
            break;
            
        case 5:  // split 01, 20 
            splitAttTriangle(tc, v0, v01, v20, subdiv);
            if(attDistance(v01, v2) < attDistance(v1, v20)){
                splitAttTriangle(tc, v01, v2, v20,subdiv);
                splitAttTriangle(tc, v01, v1, v2, subdiv);
            } else {
                splitAttTriangle(tc, v01, v1, v20,subdiv);                
                splitAttTriangle(tc, v1, v2, v20, subdiv);                
            }
            break; // split s0, s2       
            
        case 7: // split 01, 12, 20       
            
            splitAttTriangle(tc, v0, v01, v20,subdiv);
            splitAttTriangle(tc, v1, v12, v01,subdiv);
            splitAttTriangle(tc, v2, v20, v12,subdiv);
            splitAttTriangle(tc, v01, v12, v20, subdiv);
            break;                  
        } // switch()
        
    } // void splitTriangle()
    

    void addTri(TriangleCollector tc, Vector3d v0, Vector3d v1, Vector3d v2){
        
        tc.addTri(getScaledTri(v0),getScaledTri(v1),getScaledTri(v2));
    }


    /**
       adds scaled triangle to trinagle collector
     */
    void addAttTri(AttributedTriangleCollector atc, Vec v0, Vec v1, Vec v2){
        
        atc.addAttTri(getScaledAttVec(v0),getScaledAttVec(v1),getScaledAttVec(v2));
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

    boolean needAttSubdivision(Vec v0, Vec v1){
        
        return (radius* attDistance(attMidPoint(v0, v1), getAttSpherePoint(v0, v1)) > m_tolerance);
    }
    
    
    Vector3d getSpherePoint(Vector3d v1, Vector3d v2){
        
        double x = v1.x + v2.x;
        double y = v1.y + v2.y;
        double z = v1.z + v2.z;
        double r = sqrt(x*x + y*y + z*z);
        return new Vector3d(x/r, y/r, z/r);
        
    }
    
    Vec getAttSpherePoint(Vec v1, Vec v2){
        
        double x = v1.v[0] + v2.v[0];
        double y = v1.v[1] + v2.v[1];
        double z = v1.v[2] + v2.v[2];
        double r = sqrt(x*x + y*y + z*z);

        //real tex coord are calculated in getScaledAttVec
        return new Vec(x/r, y/r, z/r,v1.v[3],v1.v[4]);
        
    }
    
    Vector3d getScaledTri(Vector3d v){
        
        return new Vector3d(v.x * radius + center.x, v.y * radius + center.y, v.z * radius + center.z);
        
    }

    static final double EPS = 1.e-7;

    Vec getScaledAttVec(Vec p){
        
        double 
            x = p.v[0],
            y = p.v[1],
            z = p.v[2],
            u = p.v[3], 
            v = 0;
        double yy = y;
        if(u > 0){
            // positive y half space 
            if(abs(yy) < EPS) yy = EPS;
        } else {
            // negative y half space 
            if(abs(yy) < EPS) yy = -EPS;
        }
        // calculate tex coord 
        //  mapping from (u,v) to (x,y,z)
        //
        //  x = sin(theta)*cos(phi)  
        //  y = sin(theta)*sin(phi) 
        //  z = cos(theta) 
        //
        //  phi = PI*(2*u-1)
        //  theta = PI*(1-v) 
        // 
        // inverse mapping 
        // theta = acos(z) 
        // phi = atan2(y,x);
        // u = 0.5*((phi/PI) + 1)
        // v = 1-(theta/PI) 
        u = 0.5*(atan2(yy,x)/PI + 1);
        v = 1-acos(z)/PI;
        
        return new Vec(x * radius + center.x, y * radius + center.y, z * radius + center.z, u, v);
        
    }

    double attDistance(Vec p1, Vec p2){
        double 
            x = p1.v[0] - p2.v[0],
            y = p1.v[1] - p2.v[1],
            z = p1.v[2] - p2.v[2];

        return sqrt(x*x + y*y + z*z);
    }
    
    Vec attMidPoint(Vec p1, Vec p2){
        double 
            x = (p1.v[0] + p2.v[0])/2,
            y = (p1.v[1] + p2.v[1])/2,
            z = (p1.v[2] + p2.v[2])/2,
            u = (p1.v[3] + p2.v[3])/2,
            v = (p1.v[4] + p2.v[4])/2;
        
        return new Vec(x,y,z,u,v);

    }
    
} // class TriangulatedSphere 

