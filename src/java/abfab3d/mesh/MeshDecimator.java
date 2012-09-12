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

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import static abfab3d.util.Output.printf; 

/**
   decimator to reduce face count of triangle mesh    

   it uses general Quadric error function to calculate penalty and to find new vertex position 
   
   decimator try candidate edges, picks some edge with lowest penalty and 
   find optimal position of new vertex. the edge is collapsed and removed 
   also are removed two triangles adjacent to the edge. Other triangles, which share 
   vertices with collapsed edge change it's shape.       

   @author Vladimir Bulatov

 */
public class MeshDecimator {


    WingedEdgeTriangleMesh mesh;
    
    public MeshDecimator(){
        
    }
    
    
    /**
       decimates the mesh to have targetFaceCount
       
       returns final face count of the mesh 
       
     */
    public int processMesh(WingedEdgeTriangleMesh mesh, int targetFaceCount){
                
        printf("MeshDecimator.processMesh(%s, %d)\n", mesh, targetFaceCount);
        
        this.mesh = mesh;
        
        // init vertices quadrics
        doInitialization();
        
        // do decimation 
        int currentFaceCount = mesh.getFaceCount();
        printf("currentFaceCount: %d\n", currentFaceCount);
        while(currentFaceCount > targetFaceCount){
            if(doIteration()){
                currentFaceCount -= 2;
            } else {
                // failed to collapse 
                break;
            }
        }
        
        return mesh.getFaceCount();
    }

    /**
       init vertices with initial quadrics 
     */
    protected void doInitialization(){
        
        printf("MeshDecimator.doInitialization()\n");
        Vertex v = mesh.getVertices();
        
        while(v != null){
            
            printf("vertex: %s\n", v);
            HalfEdge start = v.getLink();
            HalfEdge he = start;
            
            do{ 
                printf("he: %s, twin: %s length: %10.7f\n", he, he.getTwin(), getLength(he));  
                HalfEdge twin = he.getTwin();
                if(twin == null){
                    printf("twin: null!!!\n");
                    break;
                }
                he = twin.getNext();           
            } while(he != start);

            v = v.getNext();            
        }
        
        Edge e = mesh.getEdges();

        printf("edges: \n");
        int count = 0;
        while(e != null){
            HalfEdge he = e.getHe();
            HalfEdge twin = null;
            HalfEdge twin2 = null;
            if(he != null)
                twin = he.getTwin();
            if(twin != null)
                twin2 = twin.getTwin();            
            printf("e(%d) %s, %s, %s, length: %10.7f\n", count++, he, twin, twin2, getLength(e));            

            e = e.getNext();
        }        

    }

    /**
       
       
     */
    static double getLength(Edge e){

        return getLength(e.getHe());
    }
    
    static double getLength(HalfEdge he){
        
        Vertex v0 = he.getStart();
        Vertex v1 = he.getEnd();
        
        return v0.getPoint().distance(v1.getPoint());
        
    }

    /**
       do one iteration 
       return true if collapse was successfull 
       return fasle id 
     */
    protected boolean doIteration(){

        // find candidate to collapse
        
        
        // do collapse 
        return true;

    }    

    /**
       
     */
    double getCandidate(Vertex v1, Vertex v2, Point3d candidate){
        
        /**
           do simple midpoint for now 
        */
        candidate.set(v1.getPoint());
        candidate.add(v2.getPoint());
        candidate.scale(0.5);
        
        return v1.getPoint().distanceSquared(v2.getPoint());
        
    }        


    /**
       class responsible for calculation with quadric 
     */
    static public class Quadric {


        // holder of quadric matrix 
        public Matrix4d M;

        //
        // Construct a quadric to evaluate the squared distance of any point
        // to the given plane [ax+by+cz+d = 0].  This is the "fundamental error
        // quadric" discussed in the paper.
        //
        public Quadric(double a, double b, double c, double d) {

            M.m00 = a*a;   M.m01 = a*b;   M.m02 = a*c;  M.m03 = a*d;
            M.m10 = M.m01; M.m11 = b*b;   M.m12 = b*c;  M.m13 = b*d;
            M.m20 = M.m02; M.m21 = M.m12; M.m22 = c*c;  M.m23 = c*d;
            M.m30 = M.m03; M.m31 = M.m13; M.m32 = M.m23;M.m33 = d*d;            

        }
        
        /**
           return quadric for plane via 3 points 
         */
        public static Quadric getQuadric(Point3d v1,Point3d v2,Point3d v3){
                        
            // make plane via 3 points
            double p[] = new double[]{1,0,0,0};
            return new Quadric(p[0],p[1],p[2],p[3]);                
        }

    }

}

