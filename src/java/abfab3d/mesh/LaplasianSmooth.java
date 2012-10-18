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
package abfab3d.mesh;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;


import static abfab3d.util.Output.printf; 
import static abfab3d.util.Output.fmt; 
import static java.lang.System.currentTimeMillis; 

/**
   performs smooting operation on WIngedEdgeTriangeMesh

   
   @author Vladimir Bulatov

 */
public class LaplasianSmooth {

    static boolean DEBUG = true;
    static boolean m_printStat = true;

    // mesjh we are working on 
    WingedEdgeTriangleMesh m_mesh;

    // maximal error allowed during smooth
    double m_maxError;
    // relative weight of central vertex contribution to new vertex position 
    double m_centerWeight = 1;

    /**
       the instance of the LaplasianSmooth can be reused for several meshes  
     */
    public LaplasianSmooth(){
        
    }

    /**

       maximal error allowed during one step 
       this is not used at the moment 
     */
    public void setMaxError(double maxError){
        m_maxError = maxError;
    }

    public void setCenterWeight(double centerWeight){
        m_centerWeight = centerWeight;
    }
    
    /**
       
       run several smoothing iteratins on the mesh
       
       
     */
    public void processMesh(WingedEdgeTriangleMesh mesh, int iterationsCount){

        m_mesh = mesh;

        Vertex v = m_mesh.getVertices();
        // init new vertex storage 
        while(v != null){
            v.setUserData(new Point3d());            
            v = v.getNext();
        }

        for(int i =0; i < iterationsCount; i++){
            doIteration();
        }

        // clear new vertex storage 
        v = m_mesh.getVertices();
        while(v != null){
            v.setUserData(null);            
            v = v.getNext();
        }
    }
    
    protected void doIteration(){

        Vertex v = m_mesh.getVertices();
        
        Point3d sum = new Point3d();

        while(v != null){
            
            HalfEdge start = v.getLink();

            HalfEdge he = start;
            Point3d p0; 
            sum.set(start.getStart().getPoint());
            sum.scale(m_centerWeight);
            
            int count = 0;
            do {
                p0 = he.getEnd().getPoint();
                sum.add(p0);                
                count++;
                he = he.getTwin().getNext(); 

            } while(he != start);
            
            sum.scale(1./(m_centerWeight + count));
            
            ((Point3d)v.getUserData()).set(sum);

            v = v.getNext();
        }

        // assign calculated values to vertices 
        v = m_mesh.getVertices();
        while(v != null){
            
            v.getPoint().set((Point3d)v.getUserData());

            v = v.getNext();
        }

        
    }
    
} // LaplasianSmooth

