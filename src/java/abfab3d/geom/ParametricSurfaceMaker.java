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

package abfab3d.geom;

import javax.vecmath.Vector3d;

import abfab3d.core.Vec;

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;

import static abfab3d.core.MathUtil.distance;
import static abfab3d.core.MathUtil.midPoint;
import static abfab3d.core.Output.printf;


/**
 * Creates a triangulated parametric surface 
 *
 * if(tolerance > 0) it splits triangles to make error below given tolerance 
 * by recursively splitting domain into triangles of smaller size to make surface 
 * triangles to be small enough to be close to the surface within given tolerance 
 *
 * @author Vladimir Bulatov
 */
public class ParametricSurfaceMaker implements TriangleProducer { 
  
    static final boolean DEBUG = false;

    ParametricSurface surface; 
    double m_tolerance;     
    double bounds[] = new double[4];

    public ParametricSurfaceMaker(ParametricSurface surface){
        this(surface, 0.);
    }
    
    /**
       makes parametric surface by subdividing 
     */
    public ParametricSurfaceMaker(ParametricSurface surface, double  tolerance){

        this.surface = surface; 
        this.m_tolerance = tolerance;

        System.arraycopy(surface.getDomainBounds(), 0, bounds, 0, 4);
    }

    /**
       returns triangulated surface 
     */
    public boolean getTriangles(TriangleCollector tc){

        int gsize[] = surface.getGridSize();        
        int nu = gsize[0];
        int nv = gsize[1];

        double 
            umin = bounds[0],
            umax = bounds[1],
            vmin = bounds[2],
            vmax = bounds[3];
        
        for(int iv = 0; iv < nv; iv++){
            
            double v0 = (nv*vmin + (vmax-vmin)*iv)/nv;
            double v1 = (nv*vmin + (vmax-vmin)*(iv+1))/nv;

            for(int iu = 0; iu < nu; iu++){
                
                double u0 = (nu*umin + (umax-umin)*iu)/nu;
                double u1 = (nu*umin + (umax-umin)*(iu+1))/nu;

                Vector3d 
                    v00 = new Vector3d(u0,v0,0),
                    v10 = new Vector3d(u1,v0,0),
                    v01 = new Vector3d(u0,v1,0),
                    v11 = new Vector3d(u1,v1,0);

                Vector3d 
                    p00 = surface.getPoint(v00, new Vector3d()),
                    p10 = surface.getPoint(v10, new Vector3d()),
                    p01 = surface.getPoint(v01, new Vector3d()),
                    p11 = surface.getPoint(v11, new Vector3d());
                splitTriangle(tc, v00, v10, v11, p00, p10, p11);
                splitTriangle(tc, v00, v11, v01, p00, p11, p01);
                
            } // for(iu... 
        } // for(iv... 

        return true;
    }
    
    /**
       
     */
    void splitTriangle(TriangleCollector tc, Vector3d v0, Vector3d v1, Vector3d v2, Vector3d p0, Vector3d p1, Vector3d p2){
        
        
        Vector3d 
            v01 = midPoint(v0, v1), 
            v12 = midPoint(v1, v2), 
            v20 = midPoint(v2, v0),
            pp01 = midPoint(p0, p1), // approximated surface at midpoits
            pp12 = midPoint(p1, p2), 
            pp20 = midPoint(p2, p0);
                
        Vector3d // exact surface at midpoints 
            p01 = surface.getPoint(v01, new Vector3d()),
            p12 = surface.getPoint(v12, new Vector3d()),
            p20 = surface.getPoint(v20, new Vector3d());        
        
        int selector = 0;

        if(m_tolerance > 0.0) {
            if(distance(p01, pp01) > m_tolerance) { selector += 1;}
            if(distance(p12, pp12) > m_tolerance) { selector += 2; }
            if(distance(p20, pp20) > m_tolerance) { selector += 4; }
        }

        switch(selector){                
            
        case 0: // no splits 
            if(!isZeroTri(p0, p1, p2))
                tc.addTri(p0, p1, p2);
            break;
            
        case 1: // split 01         
            splitTriangle(tc, v0, v01, v2, p0, p01, p2);
            splitTriangle(tc, v01, v1, v2, p01, p1, p2);
            break;
        
        case 2:  // split 12         
            splitTriangle(tc, v0, v1, v12, p0, p1, p12);
            splitTriangle(tc, v0, v12, v2, p0, p12, p2);
            break;
        
        
        case 4:  // split 20 
            splitTriangle(tc, v1, v2, v20, p1, p2, p20);
            splitTriangle(tc, v1, v20, v0, p1, p20, p0);
            break;
        
        
        case 3:  // split 01, 12         
            splitTriangle(tc, v1, v12, v01,p1, p12, p01);

            if(distance(p01, p2) < distance(p0, p12)) {
                splitTriangle(tc, v01, v12, v2,p01, p12, p2);
                splitTriangle(tc, v0, v01, v2, p0, p01, p2);
            } else {
                splitTriangle(tc, v01, v12, v0, p01, p12, p0);
                splitTriangle(tc, v0, v12, v2, p0, p12, p2);                
            }
            break;
        
        
        case 6:  //split 12 20 
            
            splitTriangle(tc, v12,v2, v20, p12, p2, p20);
            if(distance(p0, p12) < distance(p1, p20)) {
                splitTriangle(tc, v0, v12,v20, p0, p12, p20);
                splitTriangle(tc, v0, v1, v12, p0, p1, p12);
            } else {
                splitTriangle(tc, v0, v1, v20,p0, p1, p20);
                splitTriangle(tc, v1, v12, v20,p1, p12, p20);
            }
            break;
            
        case 5:  // split 01, 20 
            splitTriangle(tc, v0, v01, v20, p0, p01, p20);
            if(distance(p01, p2) < distance(p1, p20)){
                splitTriangle(tc, v01, v2, v20, p01, p2, p20);
                splitTriangle(tc, v01, v1, v2,  p01, p1, p2);
            } else {
                splitTriangle(tc, v01, v1, v20, p01, p1, p20);                
                splitTriangle(tc, v1, v2, v20, p1, p2, p20);                
            }
            break; // split s0, s2       
        
        case 7: // split 01, 12, 20       
            
            splitTriangle(tc, v0, v01, v20,p0, p01, p20);
            splitTriangle(tc, v1, v12, v01,p1, p12, p01);
            splitTriangle(tc, v2, v20, v12,p2, p20, p12);
            splitTriangle(tc, v01, v12, v20, p01, p12, p20 );
            break;                  
        } // switch()
   
    } // splitTriangle 


    // working variables 
    Vec in = new Vec(2);
    Vec out = new Vec(3);
    
    boolean isZeroTri(Vector3d p0, Vector3d p1, Vector3d p2){

        final double EPS = 1.e-10;
        return ((distance(p0, p1) < EPS) || (distance(p1, p2) < EPS) || (distance(p2, p0) < EPS));
    }

    /*
    Vector3d splitEdge(Vector3d v0, Vector3d v1, Vector3d s0, Vector3d s1){
        
        if(DEBUG)
            printf("splitEdge((%6.3f %6.3f ), (%6.3f %6.3f ), (%6.3f %6.3f %6.3f), (%6.3f %6.3f %6.3f))\n", v0.x, v0.y, v1.x, v1.y, s0.x, s0.y, s0.z, s1.x, s1.y, s1.z);

        if(dist(s0, s1) < maxTriangleSize){
            if(DEBUG)
                printf(" min size\n");
            return null;
        }
        
        if(DEBUG)
            printf(" subdivide\n");
        return surface.getPoint(midPoint(v0, v1), new Vector3d()); 
                
    }
    */



}
