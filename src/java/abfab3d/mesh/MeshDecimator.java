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

import java.util.Random;
import java.util.Set;
import java.util.HashSet;


import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import static abfab3d.util.Output.printf; 
import static abfab3d.util.Output.fmt; 

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

    static final boolean DEBUG = true;

    // mesjh we are working on 
    WingedEdgeTriangleMesh m_mesh;

    // array of all edges 
    EdgeArray m_edgeArray;

    // cadidate edges to check for collapse 
    EdgeData m_candidates[];

    // result of individual edge collapse 
    EdgeCollapseResult m_ecr;

    // current count of faces in mesh 
    int m_faceCount;

    /**
       the instance of the MeshDecimator can be reused fpor several meshes  
     */
    public MeshDecimator(){
        
    }
    
    
    /**
       decimates the mesh to have targetFaceCount
       
       returns final face count of the mesh 
       
     */
    public int processMesh(WingedEdgeTriangleMesh mesh, int targetFaceCount){
                
        printf("MeshDecimator.processMesh(%s, %d)\n", mesh, targetFaceCount);
        
        this.m_mesh = mesh;
                        
        m_faceCount = m_mesh.getFaceCount();

        doInitialization();
        
        // do decimation 

        printf("initial face count: %d\n", m_faceCount);

        int count = 100; // to avoid cycling 

        while(m_faceCount > targetFaceCount && count-- > 0){
            if(!doIteration())
                break;
       } 
        
        printf("final face count: %d\n", m_faceCount);
        int actuallFaceCount = mesh.getFaceCount();
        printf("actual face count: %d\n", actuallFaceCount);
        return actuallFaceCount;

    }

    /**
       init vertices with initial quadrics 
     */
    protected void doInitialization(){

        if(DEBUG)
            printf("MeshDecimator.doInitialization()\n");
        
        m_ecr = new EdgeCollapseResult();        
        m_candidates = new EdgeData[10];

        for(int i = 0; i < m_candidates.length; i++){
            m_candidates[i] = new EdgeData();
        }

        /*
        Vertex v = mesh.getVertices();
        
        while(v != null){
            
            printf("vertex: %s\n", v);
            HalfEdge start = v.getLink();
            HalfEdge he = start;
            
            do{ 
                if(DEBUG)
                    printf("he: %s, twin: %s length: %10.7f\n", he, he.getTwin(), getLength(he));  
                HalfEdge twin = he.getTwin();

                if(twin == null){
                    if(DEBUG)
                        printf("twin: null!!!\n");
                    break;
                }
                he = twin.getNext();           
            } while(he != start);
            
            v = v.getNext();  
            
        }
        */

        int ecount = m_mesh.getEdgeCount();
        
        //ecd.edgeCount = count;
        printf("edges count: %d\n", ecount);
        m_edgeArray = new EdgeArray(ecount);
        
        // fill edges array 
        Edge e = m_mesh.getEdges();        
        int count = 0;        
        while(e != null){            
            e.setUserData(new Integer(count));
            m_edgeArray.set(count++, e);
            e = e.getNext();
        }      
        printf("edgesArray done\n");
    }



    /**
       do one iteration 
       return true if collapse was successfull 
       return false otherwise 
     */
    protected boolean doIteration(){
        
        // find candidate to collapse
        if(DEBUG){
            printf("doIteration()\n");
        }
        getCandidateEdges(m_candidates);
        EdgeData bestCandidate = null;

        double minError = Double.MAX_VALUE;
        
        // calculate errorFucntion for 
        for(int i =0; i < m_candidates.length; i++){

            EdgeData ed = m_candidates[i];
            calculateErrorFunction(ed);
            //if(DEBUG)printf("candidate: %d, error: %10.5f\n", ((Integer)ed.edge.getUserData()).intValue(), ed.errorValue );
            if(ed.errorValue < minError){               
                bestCandidate = ed;
                minError = ed.errorValue;
            }
        }

        if(bestCandidate == null ||
           bestCandidate.edge.getHe() == null) {
            printf("!!!ERROR!!! no edge candidate was found\n");
            //Thread.currenThread().dumpStack();
            // should not happens 
            return false;
        }
            
        EdgeData ed = bestCandidate;
        getCandidateVertex(ed);
        if(DEBUG){                
            printf("remove edge: %d error: %10.5f\n", ((Integer)bestCandidate.edge.getUserData()).intValue(), ed.errorValue );
            //printf("v0: %s\n", formatPoint(ed.edge.getHe().getStart().getPoint()));
            //printf("v0: %s\n", formatPoint(ed.edge.getHe().getEnd().getPoint()));
            //printf("new vertex: %s\n", formatPoint(ed.point));
        }
        // do collapse 
        m_ecr.removedEdges.clear();
        m_ecr.insertedVertex = null;
        m_ecr.edgeCount = 0;
        m_ecr.faceCount = 0;
        m_ecr.vertexCount = 0;
        
        if(DEBUG) printf("edge before: %d\n", m_mesh.getEdgeCount());            
        
        
        if(!m_mesh.collapseEdge(ed.edge, ed.point, m_ecr)){

            if(DEBUG) printf("failed to collapse\n");                
            return false;
            
        }
        
        if(DEBUG) printf("edge after: %d\n", m_mesh.getEdgeCount());  
        m_faceCount -= 2;  //m_ecr.faceCount
        if(DEBUG) printf("moved vertex: %s\n", m_ecr.insertedVertex);  
        
        Set<Edge> edges = m_ecr.removedEdges;
        if(DEBUG) printf("removed edges: ");
        for(Edge edge : edges) {
            Integer index = (Integer)edge.getUserData();
            if(DEBUG) printf(" %d", index.intValue());
            // remove edge from array 
            m_edgeArray.set(index.intValue(), null);                
        }

        if(DEBUG) printf("\n");

        return true;

    }    

    /**
       calculates error function for removing this edge 
     */
    double calculateErrorFunction(EdgeData ed){
        
        Edge edge = ed.edge;
        HalfEdge he = edge.getHe();
        if(he == null){
            printf("error: he null in calculateErrorFunction()\n");
            printf("bad edge index: %s\n", edge.getUserData());            
            //Thread.currentThread().dumpStack();
            return Double.MAX_VALUE;
        }
            
        Vertex v0 = he.getStart();
        Vertex v1 = he.getEnd();
        Point3d p0 = v0.getPoint();
        Point3d p1 = v1.getPoint();
        
        ed.errorValue = p0.distanceSquared(p1);

        return ed.errorValue;
        
    }
    
    /**

       
     */
    void getCandidateEdges(EdgeData ed[]){
        
        for(int i = 0; i < ed.length; i++){
            
            m_edgeArray.getRandomEdge(ed[i]);  
            
        }

    }

    /**
       
     */
    void getCandidateVertex(EdgeData ed){
        
        
        /**
           do simple midpoint for now 
        */
        Edge edge = ed.edge;
        HalfEdge he = edge.getHe();
        if(ed.point == null)
            ed.point = new Point3d();

        Point3d point = ed.point; 
        
        point.set(he.getStart().getPoint());
        point.add(he.getEnd().getPoint());
        point.scale(0.5);
               
    }        

    /**       
       
     */
    static double getLength(Edge e){

        return getLength(e.getHe());
    }
    
    /**
       
     */
    static double getLength(HalfEdge he){
        
        Vertex v0 = he.getStart();
        Vertex v1 = he.getEnd();
        
        return v0.getPoint().distance(v1.getPoint());
        
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

    /**

       array of edges 
       allocated once 
       used edges can be removed from array 
       can return random non null element
       
     */
    public static class EdgeArray {
        
        Edge array[];
        int asize = 0; //
        int count = 0; // count of non nul elements 

        //
        // random number generator with specified seed 
        //
        Random m_rnd = new Random(101);

        public EdgeArray(int _asize){

            asize = _asize;
            array = new Edge[asize];
            count = 0;
            
        }
        
        public Edge get(int i){
            return array[i];
        }

        public void set(int i, Edge value){

            Object oldValue = array[i];
            array[i] = value;

            //printf("edgesArray.set(%d, %s)\n", i, value);

            if(value == null && oldValue != null){
                count--;
            } else if(value != null && oldValue == null){
                count++;
            }
        }

        public void getRandomEdge(EdgeData ed){

            ed.edge = null;

            int count = 100;
            
            while(count-- > 0){
                int i = m_rnd.nextInt(asize);            
                if(array[i] != null){
                    ed.edge = array[i];
                    ed.index = i;
                    return;
                }                
            }
        }
    }

    static String formatPoint(Point3d p){

        return fmt("(%8.5f,%8.5f,%8.5f)", p.x, p.y, p.z);

    }

    /**
       class to keep info about an Edge 
    */    
    static class EdgeData {
        
        Edge edge; // reference to Edge (to get to Vertices etc.) 
        int index; // index in array of al edges for random access 
        double errorValue; // error calculated for this edge 
        Point3d point; // place for candidate vertex                 
    }

}

