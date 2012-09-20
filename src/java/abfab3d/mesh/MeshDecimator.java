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


import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector3d;


//import abfab3d.io.output.MeshExporter;

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

    static boolean DEBUG = true;

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
    // count of collapseEdge() calls
    int m_collapseCount;

    static final int RANDOM_CANDIDATES_COUNT = 20; 

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

        int count = m_faceCount - targetFaceCount; // to avoid cycling 

        while(m_faceCount > targetFaceCount && count-- > 0){
            doIteration();
        } 
        
        printf("final face count: %d\n", m_faceCount);
        int actuallFaceCount = mesh.getFaceCount();
        printf("actual face count: %d\n", actuallFaceCount);
        return actuallFaceCount;

    }

    /**
       init vertices with initial quadric s
     */
    protected void doInitialization(){

        if(DEBUG)
            printf("MeshDecimator.doInitialization()\n");
        
        m_ecr = new EdgeCollapseResult();        
        m_candidates = new EdgeData[RANDOM_CANDIDATES_COUNT];

        m_collapseCount = 0;

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
        
        if(DEBUG) printf("collapseCount: %d, edge before: %d\n", m_collapseCount, m_mesh.getEdgeCount());                    

        //try {
            //MeshExporter.writeMeshSTL(m_mesh, fmt("c:/tmp/mesh_%04d.stl",m_collapseCount));
        //} catch(Exception e){ e.printStackTrace(); 
        //}
        //exportEdge(fmt("c:/tmp/edge_%04d.stl",m_collapseCount), ed);        

        if(!m_mesh.collapseEdge(ed.edge, ed.point, m_ecr)){

            if(DEBUG) printf("failed to collapse\n");                
            return false;
            
        }

        m_collapseCount++;
        
        if(DEBUG) printf("edge after: %d\n", m_mesh.getEdgeCount());  
        m_faceCount -= m_ecr.faceCount;  //
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
            printf("!!!failed to find new random edge in getRandomEdge()\n");
        }
    }

    public static String formatPoint(Point3d p){

        return fmt("(%8.5f,%8.5f,%8.5f)", p.x, p.y, p.z);

    }
    
    static void exportEdge(String fpath, EdgeData ed){
        HashSet vertices = new HashSet();
        HalfEdge he = ed.edge.getHe();
        vertices.add(he.getStart());
        vertices.add(he.getEnd());

        //VertexExporter.exportVertexSTL(vertices, fpath);

    }
    

    public static Vector4d makePlane(Point3d p0, Point3d p1, Point3d p2, Vector4d plane){
        
        Vector3d v0 = new Vector3d(p0);
        Vector3d v1 = new Vector3d(p1);
        Vector3d v2 = new Vector3d(p2);
        
        v1.sub(p0);
        v2.sub(p0);

        Vector3d normal = new Vector3d();
        normal.cross(v1, v2);
        normal.normalize();
        
        if(plane == null)
            plane = new Vector4d();

        plane.x = normal.x;
        plane.y = normal.y;
        plane.z = normal.z;
        plane.w = -normal.dot(v0);
        
        return plane;

    }
    
    
    public static double planePointDistance2(Vector4d plane, Tuple3d point){

        double d = plane.x * point.x + plane.y * point.y + plane.z * point.z + plane.w;
        return d*d;

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


    /**
       Quadric calculation 
     */
    static public class Quadric {


        // holder of quadric matrix 
        //public Matrix4d M;
        double 
            m00, m01, m02, m03,
            m11, m12, m13,
            m22, m23,
            m33;
            
        /**
           copy constructor 
         */
        public Quadric(Quadric q) {

            m00 = q.m00;
            m01 = q.m01;
            m02 = q.m02;
            m03 = q.m03;
            m11 = q.m11;
            m12 = q.m12;
            m13 = q.m13;
            m22 = q.m22;
            m23 = q.m23;
            m33 = q.m33;

        }

        public Quadric(double e00, double e01, double e02,  double e03, double e11, double e12, double e13, double e22, double e23,double e33) {

            m00 = e00;
            m01 = e01;
            m02 = e02;
            m03 = e03;
            m11 = e11;
            m12 = e12;
            m13 = e13;
            m22 = e22;
            m23 = e23;
            m33 = e33;

        }

        /**
           quadric centered at midpoint of p0, p1 and scaleb by scale
         */
        public Quadric(Point3d p0, Point3d p1, double scale) {
            double 
                x = 0.5*(p0.x + p1.x),
                y = 0.5*(p0.y + p1.y),
                z = 0.5*(p0.z + p1.z);

            m00 = scale;
            m01 = 0;
            m02 = 0;
            m03 = -x*scale;
            m11 = scale;
            m12 = 0;
            m13 = -y*scale;
            m22 = scale;
            m23 = -z*scale;;
            m33 = (x*x + y*y + z*z)*scale;
            
        }

        //
        // Construct a quadric to evaluate the squared distance of any point
        // to the given plane [ax+by+cz+d = 0].  This is the "fundamental error
        // quadric" discussed in the paper.
        //
        public Quadric(double a, double b, double c, double d) {

            m00 = a*a;  m01 = a*b;  m02 = a*c;  m03 = a*d;
            m11 = b*b;  m12 = b*c;  m13 = b*d;
            m22 = c*c;  m23 = c*d;
            m33 = d*d;            
            
        }

        public Quadric(Vector4d plane) {
            this(plane.x, plane.y, plane.z, plane.w);
        }

        
        /**
           return quadric for plane via 3 points 
         */
        public Quadric(Point3d p0,Point3d p1,Point3d p2){
            
            this(makePlane(p0, p1, p2, new Vector4d()));
            
        }

        /**
           evalueate value of the form at given point 
         */
        public double evaluate(Point3d p){
            double 
                x = p.x,
                y = p.y,
                z = p.z;
            
            return 
                x*x*m00 + 2*x*y*m01 + 2*x*z*m02 + 2*x*m03 
                +           y*y*m11 + 2*y*z*m12 + 2*y*m13  
                +                       z*z*m22 + 2*z*m23 
                +                                     m33;        
        }
        
        public Quadric addSet(Quadric q){
             
            m00 += q.m00;
            m01 += q.m01;
            m02 += q.m02;
            m03 += q.m03;
            m11 += q.m11;
            m12 += q.m12;
            m13 += q.m13;
            m22 += q.m22;
            m23 += q.m23;
            m33 += q.m33;

            return this;
        }

        public String toString(){
            return fmt("[%8.5f,%8.5f,%8.5f,%8.5f;%8.5f,%8.5f,%8.5f;%8.5f,%8.5f;%8.5f]", 
                       m00, m01, m02, m03, m11, m12, m13, m22, m23, m33);
        }

        public double determinant(){

            Matrix3d m = new Matrix3d(m00, m01, m02,
                                      m01, m11, m12,
                                      m02, m12, m22
                                      );
            //printf("m: %s\n", m);

            return m.determinant();
        }

        public Point3d getMinimum(Point3d pnt){
            Matrix3d m = new Matrix3d(m00, m01, m02,
                                      m01, m11, m12,
                                      m02, m12, m22
                                      );
            m.invert();
            pnt.set(m03, m13, m23);
            m.transform(pnt);
            pnt.scale(-1);
            return pnt;
        }

    } // Quadric 


}

