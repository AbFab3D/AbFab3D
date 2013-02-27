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

import abfab3d.util.StructMixedData;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.util.Random;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
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

    int m_ignoreCount;

    double m_maxEdgeLength2=0.; // square of maximal allowed edge length (if positive)
    double m_maxError=0.;
    // maximal error allowed during collapse
    double m_maxCollapseError = Double.MAX_VALUE;

    static final int RANDOM_CANDIDATES_COUNT = 10;

    protected EdgeTester m_edgeTester = null;

    StructMixedData quadrics;

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
        quadrics = new StructMixedData(new Quadric(), mesh.getVertexCount() + RANDOM_CANDIDATES_COUNT);

        printf("MeshDecimator.processMesh(%s, %d)\n", mesh, targetFaceCount);

        this.m_mesh = mesh;
                        
        m_faceCount = m_mesh.getFaceCount();

        m_surfacePinchCount = 0;
        m_faceFlipCount = 0;
        m_longEdgeCount = 0;
        m_ignoreCount = 0;


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
            printStat();
        }

        // Release variables for faster garbage collection

        m_errorFunction = null;
        m_mesh = null;

        return actuallFaceCount;

    }
    
    public void printStat() {

        printf(" final face count: %d\n", m_faceCount);
        printf("surface pinch count: %d\n", m_surfacePinchCount);
        printf("face flip count: %d\n", m_faceFlipCount);
        printf("long edge count: %d\n", m_longEdgeCount);
        printf("edges collapsed: %d\n", m_collapseCount);
        printf("Ignored Count: %d\n", m_ignoreCount);
        printf("MAX_COLLAPSE_ERROR: %10.5e\n", m_maxError);
        
    }

    /**
       init vertices with initial quadrics
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
        m_errorFunction = new ErrorQuadric(quadrics);

        m_collapseCount = 0;


        for(int i = 0; i < m_candidates.length; i++){
            m_candidates[i] = new EdgeData(Quadric.create(quadrics));
        }
         
        int ecount = m_mesh.getEdgeCount();
        
        //ecd.edgeCount = count;
        printf("edges count: %d\n", ecount);
//        System.out.println("Not allocating edgeArray");

        m_edgeArray = new EdgeArray(ecount);
        
        // fill edges array 
        StructMixedData edges = m_mesh.getEdges();
        int e = m_mesh.getStartEdge();
        int count = 0;

        System.out.println("Starting edge: " + e);
        // TODO: I don't think edgeArray is needed now
        while(e != -1){
            Edge.setUserData(count,edges, e);
            m_edgeArray.set(count++, e);

            e = Edge.getNext(edges, e);
        }

        m_errorFunction.init(m_mesh);
        
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

        final int len = m_candidates.length;
        StructMixedData edges = m_mesh.getEdges();

        // calculate errorFunction for
        for(int i =0; i < len; i++){

            EdgeData ed = m_candidates[i];

            if (ed.edge == -1) {
                //  We might of only gotten a partial list of items back.
                break;
            }

            if(m_edgeTester != null){
                if(!m_edgeTester.canCollapse(ed.edge)){
                    continue;
                }
            }
            m_errorFunction.calculateError(ed);

            if(DEBUG)printf("candidate: %d, error: %10.5f\n", Edge.getUserData(edges,ed.edge), ed.errorValue );
            if(ed.errorValue < minError){               
                bestCandidate = ed;
                minError = ed.errorValue;
            }
        }

        if(bestCandidate == null ||
           Edge.getHe(edges,bestCandidate.edge) == -1) {
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
            m_ignoreCount++;

            return false;

        }

        m_errorFunction.calculateVertex(ed);

        if(DEBUG){
            printf("remove edge: %d error: %10.5f\n", Edge.getUserData(edges, bestCandidate.edge), ed.errorValue );
            //printf("v0: %s\n", formatPoint(ed.edge.getHe().getStart().getPoint()));
            //printf("v0: %s\n", formatPoint(ed.edge.getHe().getEnd().getPoint()));
            //printf("new vertex: %s\n", formatPoint(ed.point));
        }

        
        // do collapse
        m_ecr.reset();

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
        // assign new quadric to moved vertex
        int srcIdx = ed.vertexUserData;
        int destIdx = Vertex.getUserData(m_mesh.getVertices(), m_ecr.insertedVertex);
        Quadric.set(quadrics, srcIdx, quadrics, destIdx);

        int[] edges_removed = m_ecr.removedEdges;

        m_collapseCount += edges_removed.length;

        if(DEBUG) printf("removed edges: ");
        for(int i=0; i < edges_removed.length; i++) {
            int index = Edge.getUserData(edges,edges_removed[i]);
            if(DEBUG) printf(" %d", index);
            // remove edge from array 
            m_edgeArray.set(index, -1);
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

       array of edges 
       allocated once 
       used edges can be removed from array 
       can return random non null element
       
     */
    public static class EdgeArray {
        
        int array[];
        int asize = 0; //
        //int count = 0; // count of non nul elements 

        //
        // random number generator with specified seed 
        //
        Random m_rnd = new Random(101);

        public EdgeArray(int _asize){

            asize = _asize;
            array = new int[asize];

            // TODO: a bit more expensive null handling, rethink to 0 as null?
            java.util.Arrays.fill(array, -1);
            //count = 0;
            
        }
        
        public int get(int i){
            return array[i];
        }

        public void set(int i, int value){

            int oldValue = array[i];
            array[i] = value;

            //printf("edgesArray.set(%d, %s)\n", i, value);
            /*
            if(value == -1 && oldValue != -1){
                count--;
            } else if(value != -1 && oldValue == -1){
                count++;
            }
            */
        }

        public void getRandomEdge(EdgeData ed){

            ed.edge = -1;

            int count = 100;
            
            while(count-- > 0){
                int i = m_rnd.nextInt(asize);            
                if(array[i] != -1){
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
    

    /**
       mid edge vertex placement 
     */
    static class ErrorMidEdge implements ErrorFunction {

        private TriangleMesh m_mesh;
        private Point3d p0 = new Point3d();
        private Point3d p1 = new Point3d();
        
        public void init(TriangleMesh mesh){
            // do nothing
            m_mesh = mesh;
        }

        public void calculateError(EdgeData ed){
            
            int edge = ed.edge;
            int he = Edge.getHe(m_mesh.getEdges(), edge);
            if(he == -1){
                printf("error: he null in calculateErrorFunction()\n");
                printf("bad edge index: %s\n", Edge.getUserData(m_mesh.getEdges(), edge));
                //Thread.currentThread().dumpStack();
                ed.errorValue = Double.MAX_VALUE;
                return;
            }
            
            int v0 = HalfEdge.getStart(m_mesh.getHalfEdges(), he);
            int v1 = HalfEdge.getEnd(m_mesh.getHalfEdges(), he);
            Vertex.getPoint(m_mesh.getVertices(), v0, p0);
            Vertex.getPoint(m_mesh.getVertices(), v1, p1);

            ed.errorValue = p0.distanceSquared(p1);
            
            return;
            
        }

        //
        // simple mid point placement
        //
        public void calculateVertex(EdgeData ed){

            
            int edge = ed.edge;
            int he = Edge.getHe(m_mesh.getEdges(), edge);

            if(ed.point == null)
                ed.point = new Point3d();

            Point3d point = ed.point; 

            int start = HalfEdge.getStart(m_mesh.getHalfEdges(), he);
            int end = HalfEdge.getEnd(m_mesh.getHalfEdges(), he);
            Vertex.getPoint(m_mesh.getVertices(), start, point);
            Vertex.getPoint(m_mesh.getVertices(), end, p1);
            point.add(p1);
            point.scale(0.5);
            
        }            


    } // ErrorMidEdge



    /**

       Quadric error calculation based on ideas of M.Garland PhD thesis (1999) 
     */
    static class ErrorQuadric implements ErrorFunction {

        StructMixedData sq = new StructMixedData(new Quadric(), 1);
        int  // scratch quadric for calculations
            m_q0 = Quadric.create(sq);

        double m_midEdgeQuadricWeight = 1.e-3; // weight of quadric centered at mid edge 
        double m_edgeLengthWeight = 1.e-2;
        double m_quadricWeight = 1;

        /** Scratch vars */
        private Matrix3d sm3d = new Matrix3d();
        private Vector3d sv0 = new Vector3d();
        private Vector3d sv1 = new Vector3d();
        private Vector3d sv2 = new Vector3d();
        private Point3d sp0 = new Point3d();
        private Point3d sp1 = new Point3d();
        private Vector3d sn = new Vector3d();

        private double[] result = new double[9];
        private int[] row_perm = new int[3];
        private double[] row_scale = new double[3];
        private double[] tmp = new double[9];
        StructMixedData quadrics;
        private TriangleMesh m_mesh;

        public ErrorQuadric(StructMixedData quadrics) {
            this.quadrics = quadrics;
        }

        public void init(TriangleMesh mesh){
            m_mesh = mesh;

            Vector4d plane = new Vector4d();

            //
            // init vertices data 
            //
            int v = mesh.getStartVertex();
            StructMixedData vertices = mesh.getVertices();

            while(v != -1){
                Vertex.setUserData(makeVertexQuadric(v,plane), vertices, v);
                v = Vertex.getNext(vertices, v);
            }
        }

        /**
           
         */
        public void calculateError(EdgeData ed){
            
            int edge = ed.edge;

            int he = Edge.getHe(m_mesh.getEdges(), edge);
            if(he == -1){
                printf("error: he null in calculateError()\n");
                printf("bad edge index: %s\n", Edge.getUserData(m_mesh.getEdges(), edge));
                ed.errorValue = Double.MAX_VALUE;                
                return;
            }
            int
                v0 = HalfEdge.getStart(m_mesh.getHalfEdges(), he),
                v1 = HalfEdge.getEnd(m_mesh.getHalfEdges(), he);
            
            int q0 = Vertex.getUserData(m_mesh.getVertices(),v0);
            int q1 = Vertex.getUserData(m_mesh.getVertices(),v1);

            //System.out.println("q0: " + q0 + " q1: " + q1);
            if(Double.isNaN(Quadric.getM00(quadrics,q0)) || Double.isNaN(Quadric.getM00(quadrics,q1))){
                printf("bad quadric: \n");
                 printf("   q0: %s\n", q0);
                printf("   q1: %s\n", q1);
                ed.errorValue = Double.MAX_VALUE;
                Quadric.set(quadrics, q0, quadrics, ed.vertexUserData);
                return;
            }

            Vertex.getPoint(m_mesh.getVertices(), v0, sp0);
            Vertex.getPoint(m_mesh.getVertices(), v1, sp1);
            Quadric.getMidEdgeQuadric(sp0, sp1, m_midEdgeQuadricWeight, sq, m_q0);
            Quadric.addSet(quadrics,q0, sq, m_q0);
            Quadric.addSet(quadrics,q1, sq, m_q0);

            //Quadric midEdge = new Quadric(v0.getPoint(), v1.getPoint(), m_midEdgeQuadricWeight);
            //m_q0.addSet(midEdge);
            
            // add small quadric centered at mid edge 
            //m_q.add(q1);

            if(ed.point == null)
                ed.point = new Point3d();

            try {
                Quadric.getMinimum(sq, m_q0, ed.point,sm3d, result, row_perm, row_scale, tmp);

                double quadricError = m_quadricWeight * Quadric.evaluate(ed.point,sq,m_q0);
                double edgeError = m_edgeLengthWeight * sp0.distanceSquared(sp1);

                ed.errorValue = quadricError + edgeError;

                int destIdx = ed.vertexUserData;
                Quadric.set(sq,m_q0,quadrics,destIdx);
            } catch (Exception e){

                printf("Quadric inversion exception\n");
                printf("Q0: %s\n", q0);
                printf("Q1: %s\n", q1);
                printf("m_q0: %s\n", m_q0);
                //printf("midedge: %s\n", midEdge);
                ed.errorValue = Double.MAX_VALUE;
                Quadric.set(quadrics, q0, quadrics, ed.vertexUserData);
            }

            return;
            
        }

        public void calculateVertex(EdgeData ed){
            // do nothing, vertex was calculated in calculateError();
            
        }
    

        /**
         creates vertex quadric from surrounding faces
         */
        public int makeVertexQuadric(int v, Vector4d plane){

            // sum of weighted quadrics for each surrounding face
            // weight is area of the corresponding face
            // correction.
            // We are not using weights. All faces give the same contribution
            // it seems this works for our voxel based meshes
            Point3d p0, p1, p2;
            int start = Vertex.getLink(m_mesh.getVertices(), v);
            int he = start;

            Quadric.setZero(sq,m_q0);

            do {
                int he_start = HalfEdge.getStart(m_mesh.getHalfEdges(), he);
                int he_end = HalfEdge.getEnd(m_mesh.getHalfEdges(), he);
                int he_next = HalfEdge.getNext(m_mesh.getHalfEdges(), he);
                int he_next_end = HalfEdge.getEnd(m_mesh.getHalfEdges(), he_next);
                Vertex.getPoint(m_mesh.getVertices(), he_start, sv0);
                Vertex.getPoint(m_mesh.getVertices(), he_end, sv1);
                Vertex.getPoint(m_mesh.getVertices(), he_next_end, sv2);

                boolean good = Quadric.makePlane(sv0, sv1, sv2, sn, plane);

                if(good){

                    Quadric.addSet(plane,sq,m_q0);
                } else {
                    printVertex(m_mesh, v);
                }

                he = HalfEdge.getNext(m_mesh.getHalfEdges(), HalfEdge.getTwin(m_mesh.getHalfEdges(), he));
            } while(he != start);

            return Quadric.create(sq,m_q0,quadrics);

        }

    } // ErrorQuadric 

    //static int filecount = 0;

    static void printVertex(TriangleMesh mesh, int v){
        
        Point3d p0 = new Point3d(), p1 = new Point3d(), p2 = new Point3d();
        int start = Vertex.getLink(mesh.getVertices(), v);
        int he = start;
        Vertex.getPoint(mesh.getVertices(), HalfEdge.getStart(mesh.getHalfEdges(), he), p0);

        printf("p0: (%10.7f,%10.7f,%10.7f) \n", 
               p0.x*MM,p0.y*MM,p0.z*MM);
        
        do {
            int end = HalfEdge.getEnd(mesh.getHalfEdges(), he);
            int next = HalfEdge.getNext(mesh.getHalfEdges(), he);
            int next_end = HalfEdge.getEnd(mesh.getHalfEdges(), next);
            Vertex.getPoint(mesh.getVertices(), end, p1);
            Vertex.getPoint(mesh.getVertices(), next_end, p2);

            printf("  p1: (%10.7f,%10.7f,%10.7f),  p2: (%10.7f,%10.7f,%10.7f), \n", 
                   (p1.x-p0.x)*MM,(p1.y-p0.y)*MM,(p1.z-p0.z)*MM,
                   (p2.x-p0.x)*MM,(p2.y-p0.y)*MM,(p2.z-p0.z)*MM);

            he = HalfEdge.getNext(mesh.getHalfEdges(), HalfEdge.getTwin(mesh.getHalfEdges(), he));

        } while(he != start);        
    }    
}

