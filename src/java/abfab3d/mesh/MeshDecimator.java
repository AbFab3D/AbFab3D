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
import java.util.ArrayList;


import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector3d;


import static abfab3d.util.Output.printf; 
import static abfab3d.util.Output.fmt; 
import static java.lang.System.currentTimeMillis; 

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


    static boolean DEBUG = false;
    static boolean m_printStat = true;
    static final double MM = 1000.; // mm per meter 

    // mesh we are working on
    TriangleMesh m_mesh;

    // array of all edges 
    EdgeArray m_edgeArray;

    // cadidate edges to check for collapse 
    EdgeData m_candidates[];

    // result of individual edge collapse 
    EdgeCollapseResult m_ecr;
    // parameters of individual edge collapse
    EdgeCollapseParams m_ecp; 

    // current count of faces in mesh 
    int m_faceCount;
    // count of collapseEdge() calls
    int m_collapseCount;
    // count of surface pinch prevented 
    int m_surfacePinchCount;
    // count of face flip prevented 
    int m_faceFlipCount;
    // count of long edge creation prevented 
    int m_longEdgeCount;

    double m_maxEdgeLength2=0.; // square of maximal allowed edge length (if positive) 
    double m_maxError=0.;
    // maximal error allowed during collapse 
    double m_maxCollapseError = Double.MAX_VALUE;

    static final int RANDOM_CANDIDATES_COUNT = 10; 

    protected EdgeTester m_edgeTester = null;
    
    
    //
    // object, which calculates errors and new vertex placement 
    //
    ErrorFunction m_errorFunction;


    /**
       interface to calculate erors and vertex placement 
     */
    interface ErrorFunction {

        /**
           one time initialization with given mesh 
         */
        public void init(TriangleMesh mesh);

        /**
           calculate error and place resulkt in ed.errorValue
        */
        public void calculateError(EdgeData ed);
        /**
         calculate error and place resulkt in ed.errorValue
         */
        public void calculateErrorDebug(EdgeData ed);
        /**
           calculate new vertex location and place result in ed.point  
        */
        public void calculateVertex(EdgeData ed);

        

    }

    /**
       the instance of the MeshDecimator can be reused for several meshes  
     */
    public MeshDecimator(){
        
    }

    public void setMaxCollapseError(double maxCollapseError){
        m_maxCollapseError = maxCollapseError;
    }
    
    /**
       set maximal allowed edge length 
     */
    public void setMaxEdgeLength(double maxLength){

        m_maxEdgeLength2 = maxLength*maxLength;

    }

    /**
       supply external tester for edges 
     */
    public void setEdgeTester(EdgeTester edgeTester){
        m_edgeTester = edgeTester;
    }

    /**
       decimates the mesh to have targetFaceCount
       
       returns final face count of the mesh 
       
     */
    public int processMesh(TriangleMesh mesh, int targetFaceCount){
                
        printf("MeshDecimator.processMesh(%s, %d)\n", mesh, targetFaceCount);
                
        this.m_mesh = mesh;
                        
        m_faceCount = m_mesh.getFaceCount();

        m_surfacePinchCount = 0;
        m_faceFlipCount = 0;
        m_longEdgeCount = 0;


        long ts = currentTimeMillis();
        doInitialization();        
        long ts1 = currentTimeMillis();
        printf("MeshDecimator.doInitialization() %d ms\n", (ts1-ts));
        ts = ts1;
        
        //printf("initial face count: %d\n", m_faceCount);

        int targetCount = m_faceCount - targetFaceCount; // to avoid cycling         
        int count = 0;
        int f0 = m_faceCount;
        long t0 = System.currentTimeMillis();
        long time0 = t0;

        // do decimation 

        while(m_faceCount > targetFaceCount && count < targetCount ){
            doIteration();

            count += 2;
            if(m_faceCount % 100000 == 0){
                long t1 = System.currentTimeMillis();
                
                double timeSinceStart = (double)(t1 - t0)/1000;
                double coeff = timeSinceStart/count;
                double totalTime = (coeff*targetCount);
                double timeToFinish = totalTime - timeSinceStart;
                int f1 = m_faceCount;
                double fps = 1000.*(f0-f1)/(t1-time0);
                //printf("face count: %7d time to finish: %5.0f sec, total time: %5.0f, fps: %5.0f \n", m_faceCount, timeToFinish, totalTime, fps );
                f0 = f1;
                time0 = t1;
            }
        }


System.out.println("***Iterations: " + count);

        ts1 = currentTimeMillis();
        printf("MeshDecimator.doIterations() %d ms\n", (ts1-ts));
        ts = ts1;

        int actuallFaceCount = mesh.getFaceCount();
        if(m_printStat){
            printf(" final face count: %d\n", m_faceCount);
            printf("actual face count: %d\n", actuallFaceCount);
            printf("surface pinch count: %d\n", m_surfacePinchCount);
            printf("face flip count: %d\n", m_faceFlipCount);
            printf("long edge count: %d\n", m_longEdgeCount);
            printf("edges collapsed: %d\n", m_collapseCount);
            printf("MAX_COLLAPSE_ERROR: %10.5e\n", m_maxError);
        }
        return actuallFaceCount;

    }

    /**
       init vertices with initial quadric s
     */
    protected void doInitialization(){

        if(DEBUG)
            printf("MeshDecimator.doInitialization()\n");
        
        m_maxError = 0;
        m_ecr = new EdgeCollapseResult();        
        m_ecp = new EdgeCollapseParams();        
        m_candidates = new EdgeData[RANDOM_CANDIDATES_COUNT];
        // 
        //m_errorFunction = new ErrorMidEdge();
        m_errorFunction = new ErrorQuadric();

        m_collapseCount = 0;


        for(int i = 0; i < m_candidates.length; i++){
            m_candidates[i] = new EdgeData();
        }
         
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

        m_errorFunction.init(m_mesh);
        
        printf("edgesArray done\n");
    }



    int ignoreCount = 0;

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

        // calculate errorFunction for
        final int len = m_candidates.length;

        for(int i =0; i < len; i++){

            EdgeData ed = m_candidates[i];
            if(m_edgeTester != null){
                if(!m_edgeTester.canCollapse(ed.edge)){
                    continue;
                }
            }
            m_errorFunction.calculateError(ed);

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

        if(ed.errorValue > m_maxCollapseError){
/*
            bc_cnt++;
            if (bc_cnt < 100) {
                System.out.println("Ignore: " + ed.vertexUserData + " val: " + ed.errorValue);
                m_errorFunction.calculateErrorDebug(ed);

            }
  */
            ignoreCount++;
            return false;
            
        }

        m_errorFunction.calculateVertex(ed);

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
        
        m_ecp.maxEdgeLength2 = m_maxEdgeLength2;

        if(DEBUG) printf("collapseCount: %d, edge before: %d\n", m_collapseCount, m_mesh.getEdgeCount());    

        if(!m_mesh.collapseEdge(ed.edge, ed.point, m_ecp, m_ecr)){

            if(DEBUG) printf("failed to collapse\n");  
            switch(m_ecr.returnCode){
            case EdgeCollapseResult.FAILURE_SURFACE_PINCH:
                m_surfacePinchCount++; 
                break;
            case EdgeCollapseResult.FAILURE_FACE_FLIP:
                m_faceFlipCount++; 
                break;                
            case EdgeCollapseResult.FAILURE_LONG_EDGE:
                m_longEdgeCount++; 
                break;
            default:
                System.out.println("***Unhandled case in return code");
            }
            return false;
            
        } 

        
        if(DEBUG) printf("edge after: %d\n", m_mesh.getEdgeCount());  
        m_faceCount -= m_ecr.faceCount;  //
        if(DEBUG) printf("moved vertex: %s\n", m_ecr.insertedVertex);  
        // asign new quadric to moved vertex 
        ((Quadric)(m_ecr.insertedVertex.getUserData())).set(ed.vertexUserData);
        //m_ecr.insertedVertex.setUserData(ed.vertexUserData.clone());

        ArrayList<Edge> edges = m_ecr.removedEdges;

        m_collapseCount += edges.size();

        if(DEBUG) printf("removed edges: ");
        for(Edge edge : edges) {
            Integer index = (Integer)edge.getUserData();
            if(DEBUG) printf(" %d", index.intValue());
            // remove edge from array 
            m_edgeArray.set(index.intValue(), null);                
        }

        if(ed.errorValue > m_maxError){
            m_maxError = ed.errorValue;
        }            

        if(DEBUG) printf("\n");

        return true;

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
    
    /**
       class to keep info about an Edge 
    */    
    static class EdgeData {
        
        Edge edge; // reference to Edge (to get to Vertices etc.) 
        int index; // index in array of all edges for random access 
        double errorValue; // error calculated for this edge 
        Point3d point; // place for candidate vertex                 
        Quadric vertexUserData = new Quadric(); // user data for new vertex 
    }


    /**
       mid edge vertex placement 
     */
    static class ErrorMidEdge implements ErrorFunction {
        
        
        public void init(TriangleMesh mesh){
            // do nothing 
        }

        public void calculateErrorDebug(EdgeData ed){
        }

        public void calculateError(EdgeData ed){
            
            Edge edge = ed.edge;
            HalfEdge he = edge.getHe();
            if(he == null){
                printf("error: he null in calculateErrorFunction()\n");
                printf("bad edge index: %s\n", edge.getUserData());            
                //Thread.currentThread().dumpStack();
                ed.errorValue = Double.MAX_VALUE;
                return;
            }
            
            Vertex v0 = he.getStart();
            Vertex v1 = he.getEnd();
            Point3d p0 = v0.getPoint();
            Point3d p1 = v1.getPoint();
            
            ed.errorValue = p0.distanceSquared(p1);
            
            return;
            
        }

        //
        // simple mid point placement
        //
        public void calculateVertex(EdgeData ed){

            
            Edge edge = ed.edge;
            HalfEdge he = edge.getHe();

            if(ed.point == null)
                ed.point = new Point3d();
            
            Point3d point = ed.point; 
            
            point.set(he.getStart().getPoint());
            point.add(he.getEnd().getPoint());
            point.scale(0.5);
            
        }            


    } // ErrorMidEdge



    /**

       Quadric error calculation based on ideas of M.Garland PhD thesis (1999) 
     */
    static class ErrorQuadric implements ErrorFunction {
        
        Quadric  // scratch quadric for calculations 
            m_q0 = new Quadric();

        double m_midEdgeQuadricWeight = 1.e-3; // weight of quadric centered at mid edge 
        double m_edgeLengthWeight = 1.e-2;
        double m_quadricWeight = 1;

        /** Scratch vars */
        private Matrix3d sm3d = new Matrix3d();
        private Vector3d sv0 = new Vector3d();
        private Vector3d sv1 = new Vector3d();
        private Vector3d sv2 = new Vector3d();
        private Vector3d sn = new Vector3d();

        private double[] result = new double[9];
        private int[] row_perm = new int[3];
        private double[] row_scale = new double[3];
        private double[] tmp = new double[9];

        public void init(TriangleMesh mesh){
            Vector4d plane = new Vector4d();

            //
            // init vertices data 
            //
            Vertex v = mesh.getVertices();
            while(v != null){
                v.setUserData(makeVertexQuadric(v,plane));
                v = v.getNext();
            }
        }

        /**
           
         */
        public void calculateError(EdgeData ed){
            
            Edge edge = ed.edge;
            HalfEdge he = edge.getHe();
            if(he == null){
                printf("error: he null in calculateError()\n");
                printf("bad edge index: %s\n", edge.getUserData());            
                ed.errorValue = Double.MAX_VALUE;                
                return;
            }
            Vertex 
                v0 = he.getStart(),
                v1 = he.getEnd();
            
            Quadric q0 = (Quadric)v0.getUserData();
            Quadric q1 = (Quadric)v1.getUserData();
            
            if(Double.isNaN(q0.m00) || Double.isNaN(q1.m00)){
                //printf("bad quadric: \n");
                // printf("   q0: %s\n", q0);
                //printf("   q1: %s\n", q1);
                ed.errorValue = Double.MAX_VALUE;
                ed.vertexUserData = new Quadric(q0);
                return;                
            }
            
            Quadric.getMidEdgeQuadric(v0.getPoint(), v1.getPoint(), m_midEdgeQuadricWeight, m_q0);
            m_q0.addSet(q0);
            m_q0.addSet(q1);

            //Quadric midEdge = new Quadric(v0.getPoint(), v1.getPoint(), m_midEdgeQuadricWeight);
            //m_q0.addSet(midEdge);
            
            // add small quadric centered at mid edge 
            //m_q.add(q1);

            if(ed.point == null)
                ed.point = new Point3d();

            try {
                m_q0.getMinimum(ed.point,sm3d, result, row_perm, row_scale, tmp);
                double quadricError = m_quadricWeight * m_q0.evaluate(ed.point);

                double edgeError = m_edgeLengthWeight * v0.getPoint().distanceSquared(v1.getPoint());

                ed.errorValue = quadricError + edgeError;

                ed.vertexUserData.set(m_q0);
                
            } catch (Exception e){

                printf("Quadric inversion exception\n");
                printf("Q0: %s\n", q0);
                printf("Q1: %s\n", q1);
                printf("m_q0: %s\n", m_q0);
                //printf("midedge: %s\n", midEdge);
                ed.errorValue = Double.MAX_VALUE;
                ed.vertexUserData = new Quadric(q0);
                
            }

            return;
            
        }

        public void calculateErrorDebug(EdgeData ed){

            Edge edge = ed.edge;
            HalfEdge he = edge.getHe();
            if(he == null){
                printf("error: he null in calculateError()\n");
                printf("bad edge index: %s\n", edge.getUserData());
                ed.errorValue = Double.MAX_VALUE;
                return;
            }
            Vertex
                    v0 = he.getStart(),
                    v1 = he.getEnd();

            Quadric q0 = (Quadric)v0.getUserData();
            Quadric q1 = (Quadric)v1.getUserData();

            System.out.println("Q0: " + q0);
            System.out.println("Q1: " + q1);
            if(Double.isNaN(q0.m00) || Double.isNaN(q1.m00)){
                //printf("bad quadric: \n");
                // printf("   q0: %s\n", q0);
                //printf("   q1: %s\n", q1);
                ed.errorValue = Double.MAX_VALUE;
                ed.vertexUserData = new Quadric(q0);
                return;
            }

            Quadric.getMidEdgeQuadric(v0.getPoint(), v1.getPoint(), m_midEdgeQuadricWeight, m_q0);
            m_q0.addSet(q0);
            m_q0.addSet(q1);
            System.out.println("midEdge: " + m_q0);

            //Quadric midEdge = new Quadric(v0.getPoint(), v1.getPoint(), m_midEdgeQuadricWeight);
            //m_q0.addSet(midEdge);

            // add small quadric centered at mid edge
            //m_q.add(q1);

            if(ed.point == null)
                ed.point = new Point3d();

            try {
                m_q0.getMinimum(ed.point,sm3d, result, row_perm, row_scale, tmp);
                double quadricError = m_quadricWeight * m_q0.evaluate(ed.point);
                System.out.println("error: " + quadricError);

                double edgeError = m_edgeLengthWeight * v0.getPoint().distanceSquared(v1.getPoint());

                ed.errorValue = quadricError + edgeError;

                ed.vertexUserData.set(m_q0);

            } catch (Exception e){

                printf("Quadric inversion exception\n");
                printf("Q0: %s\n", q0);
                printf("Q1: %s\n", q1);
                printf("m_q0: %s\n", m_q0);
                //printf("midedge: %s\n", midEdge);
                ed.errorValue = Double.MAX_VALUE;
                ed.vertexUserData = new Quadric(q0);

            }

            return;

        }

        public void calculateVertex(EdgeData ed){
            // do nothing, vertex was calculated in calculateError();
            
        }
    

        /**
         creates vertex quadric from surrounding faces
         */
        public Quadric makeVertexQuadric(Vertex v, Vector4d plane){

            // sum of weighted quadrics for each surrounding face
            // weight is area of the corresponding face
            // correction.
            // We are not using weights. All faces give the same contribution
            // it seems this works for our voxel based meshes
            Point3d p0, p1, p2;
            HalfEdge start = v.getLink();
            HalfEdge he = start;

            m_q0.setZero();

            do {
                p0 = he.getStart().getPoint();
                p1 = he.getEnd().getPoint();
                p2 = he.getNext().getEnd().getPoint();

                sv0.set(p0);
                sv1.set(p1);
                sv2.set(p2);

                boolean good = Quadric.makePlane(sv0, sv1, sv2, sn, plane);

                if(good){

                    m_q0.addSet(new Quadric(plane));

                } else {
                    printVertex(v);
                }

                he = he.getTwin().getNext();

            } while(he != start);

            return new Quadric(m_q0);

        }

    } // ErrorQuadric 

    //static int filecount = 0;

    static void printVertex(Vertex v){
        
        Point3d p0, p1, p2; 
        HalfEdge start = v.getLink();
        HalfEdge he = start;
        p0 = he.getStart().getPoint();
        
        printf("p0: (%10.7f,%10.7f,%10.7f) \n", 
               p0.x*MM,p0.y*MM,p0.z*MM);
        
        do {
            p1 = he.getEnd().getPoint();
            p2 = he.getNext().getEnd().getPoint();
            
            printf("  p1: (%10.7f,%10.7f,%10.7f),  p2: (%10.7f,%10.7f,%10.7f), \n", 
                   (p1.x-p0.x)*MM,(p1.y-p0.y)*MM,(p1.z-p0.z)*MM,
                   (p2.x-p0.x)*MM,(p2.y-p0.y)*MM,(p2.z-p0.z)*MM);
            
            he = he.getTwin().getNext(); 
            
        } while(he != start);        
    }    
}

