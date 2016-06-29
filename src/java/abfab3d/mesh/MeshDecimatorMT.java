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

import java.util.concurrent.atomic.AtomicIntegerArray;

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import java.util.Random;

import javax.vecmath.Point3d;

import abfab3d.util.*;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
   decimator to reduce face count of triangle mesh    

   MT version 

   it uses general Quadric error function to calculate penalty and to find new vertex position 
   
   decimator try candidate edges, picks the edge with lowest penalty and 
   find optimal position of new vertex. the edge is collapsed and removed 
   also are removed two triangles adjacent to the edge. Other triangles, which share 
   vertices with collapsed edge change it's shape.       

   @author Vladimir Bulatov

 */
public class MeshDecimatorMT extends MeshDecimator {

    /** Should we print debug information */
    private static final boolean DEBUG = false;

    /** Should we collect stats information */
    private static final boolean STATS = false;

    protected int m_threadCount = 0;

    // array for marking currently locked edges 
    protected AtomicIntegerArray m_edgesLock;
    // array for selection of edges
    protected int m_edgeSelectorArray[];
    
    //protected AtomicInteger m_iterationCount;
    protected int m_iterationCount;

    /**
       the instance of the MeshDecimator can be reused for several meshes
     */
    public MeshDecimatorMT(){
        
    }

    /**
       set count of threads to use 
       
     */
    public void setThreadCount(int count){

        m_threadCount = count;

    }

    public int processMesh(TriangleMesh mesh, int targetFaceCount){

        //printf("MeshDecimatorMT.processMesh(%s, %d)\n", mesh, targetFaceCount);        
        if(m_threadCount <= 0)
            return super.processMesh(mesh, targetFaceCount);       

        // do multithread routine 
        
        return processMeshMT(mesh, targetFaceCount);
        
    }


    /**

       returns final actual face count 
       
     */
    protected int processMeshMT(TriangleMesh mesh, int targetFaceCount){
        
        //printf("MeshDecimatorMT.processMeshMT(%s, %d)\n", mesh, targetFaceCount); 
        long t0 = time();

        this.m_mesh = mesh;
                        
        initCommon();
        
        int faceCount = m_mesh.getFaceCount();        
        int facesToCollapse = faceCount - targetFaceCount;
        
        //m_iterationCount = new AtomicInteger(facesToCollapse/2);

        m_iterationCount = facesToCollapse/2;

        printf("processMeshMT() common code: %d ms\n", (time() - t0));         

        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);

        DecimatorRunner threads[] = new DecimatorRunner[m_threadCount];

        Random rnd = new Random(System.nanoTime());
        for(int i = 0; i < m_threadCount; i++){
            
            long seed = rnd.nextLong();
            threads[i] = new DecimatorRunner(seed);
            executor.submit(threads[i]);

        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        for(int i = 0; i < threads.length; i++){
            faceCount -= threads[i].collapsedFaces;           
        }

        printf("calculated face count: %d\n", faceCount);

        printf("actual face count: %d\n", m_mesh.getFaceCount());

        printStat(threads);
        
        return faceCount;
        
    }

    void printStat(DecimatorRunner threads[]){
        for(int i = 0; i < threads.length; i++){
            threads[i].printStat();
        }
    }


    /**
       initialization of data comon for all threads 
     */
    void initCommon(){

        int ecount = m_mesh.getEdgeCount();
        m_edgeSelectorArray = new int[ecount];
        m_edgesLock = new AtomicIntegerArray(ecount);
        
        // fill edges array 

        StructMixedData edges = m_mesh.getEdges();
        int e = m_mesh.getStartEdge();

        java.util.Arrays.fill(m_edgeSelectorArray, EdgeSelector.NO_DATA);

        int count = 0;
        // put all edges into array 
        while(e != -1){
            Edge.setUserData(count,edges, e);
            m_edgeSelectorArray[count++] = e;
            e = Edge.getNext(edges, e);
        } 
        for(int i = 0; i < m_edgeSelectorArray.length; i++){
            if(m_edgeSelectorArray[i] == EdgeSelector.NO_DATA){
                printf("unused edge: %d\n", i);
            }
        }
    }

    /**
       class doing per thread job 
       1) select good edge candidate to collapse
       2) lock edges around that edge 
       3) collapse the edge
       4) unlock edges 
     */
    class DecimatorRunner implements Runnable{
        
        // count of collapseEdge() calls
        int collapsedEdges;
        // count of collapse faces 
        int collapsedFaces;
        // count of surface pinch prevented
        int surfacePinchCount;
        // count of face flip prevented
        int faceFlipCount;
        // count of long edge creation prevented
        int longEdgeCount;
        // count of collapses ignored because of large error value 
        int largeErrorCount;
        // count of failed collapses because of locking failure
        int failedToLockCount;
        // count of total successfully locked edges 
        int lockedEdgesCount = 0;
        // count of total successfull locks
        int lockCount = 0;    
        // count of time spend in collapseEdge 
        long collapseTime = 0;
        // count of collapse calls 
        int collapseCount = 0;
        long maxCollapseTime = 0;
        long minCollapseTime = Long.MAX_VALUE;

        // maximal actual collapse error
        double maxError; 
        
        EdgeData edgeCandidate;
        EdgeData tempCandidate;            
        // source of random edges 
        EdgeSelector edgeSelector;
     
        EdgeCollapseResult ecresult = new EdgeCollapseResult();
        EdgeCollapseParams ecparam = new EdgeCollapseParams();
        
        ErrorFunction errorFunction;
        
        // stack of locked edges 
        StackOfInt lockedEdges = new StackOfInt(100);
        
        long seed; // seed to use for random numbers 


        /**



         */
        DecimatorRunner(long seed){

            //printf("new DecimatorRunner(%d): %s \n", count, this); 
            this.seed = seed;

        }

        /**
           
           

         */
        public void run(){

            //printf("%s.run()\n", this); 
            initThreadData(); 

            int count = 0;

            while(true){
                doIterationMT();
                if( --m_iterationCount <= 0)
                    break;                
            }                        

        }
        
        /**
           
           

         */
        protected void initThreadData(){

            surfacePinchCount = 0;
            faceFlipCount = 0;
            longEdgeCount = 0;
            largeErrorCount = 0;
            collapsedEdges = 0;
            collapsedFaces = 0;
            failedToLockCount = 0;
            lockedEdgesCount = 0;
            lockCount = 0;
            maxError = 0;

            edgeCandidate  = new EdgeData();
            tempCandidate  = new EdgeData();

            errorFunction = new ErrorMidEdgeMT();            

            errorFunction.init(m_mesh);
            
            ecparam.maxEdgeLength2 = m_maxEdgeLength2;
            // during the edges locking the surface pinch is tested automatically
            ecparam.testSurfacePinch = false;

            edgeSelector = new EdgeSelector(m_edgeSelectorArray, seed);

        }

        /**
           
           

         */
        public void printStat() {

            printf("%s printStat() \n", this);
            printf(" collapsedEdges: %d\n", collapsedEdges);
            printf(" collapsedFace: %d\n", collapsedFaces);
            printf(" surfacePinchCount: %d\n", surfacePinchCount);
            printf(" faceFlipCount: %d\n", faceFlipCount);
            printf(" longEdgeCount: %d\n", longEdgeCount);
            printf(" largeErrorCount: %d\n", largeErrorCount);
            printf(" failedToLockCount: %d\n", failedToLockCount);
            printf(" average edge lock size: %5.1f\n", ((double)lockedEdgesCount/lockCount));
            printf(" time per collapse: %d ns\n", collapseTime / collapseCount);
            printf(" collapseTime: %d ms\n", collapseTime/1000000);
            
            edgeSelector.printStat();
            
        }
        
        /**
           
           

         */
        protected void doIterationMT(){

            //printf("doIterationMT()\n");
            getEdgeCandidate(edgeCandidate); 
            //printf("edgeCandidate: %d\n",edgeCandidate.index);
            errorFunction.calculateError(edgeCandidate);

            double minError = edgeCandidate.errorValue;
            //printf("minError: %f\n",minError);
            
            // calculate errorFunction for few random edges 
            for(int i = 0; i < RANDOM_CANDIDATES_COUNT; i++){
                
                getEdgeCandidate(tempCandidate);                  
                errorFunction.calculateError(tempCandidate);
                //printf("i: %d edge: %d, error: %f\n",i, tempCandidate.index, tempCandidate.errorValue);
                
                if(tempCandidate.errorValue < minError){    
                    
                    edgeCandidate.set(tempCandidate);
                    minError = tempCandidate.errorValue;
                    
                    //printf("new candidate \n");
                    
                }
            }
            
            //printf("best edge: %d, error: %f\n",edgeCandidate.index, edgeCandidate.errorValue);
            
            if(edgeCandidate.errorValue > m_maxCollapseError){
                largeErrorCount++;                
                return;                
            }
            
            errorFunction.calculateVertex(edgeCandidate);
            
            // init 
            ecresult.reset();
            
            if(!lockEdges(edgeCandidate, lockedEdges, m_edgesLock, m_mesh)){

                unlockEdges(m_edgesLock, lockedEdges);
                failedToLockCount++;
                return;

            } else {

                lockCount++;
                lockedEdgesCount += lockedEdges.getSize();                
            }
            
            //if(true){ // to test locking speed 
            //    collapsedEdges += 3;
                //unlockEdges(m_edgesLock, lockedEdges);                
            //    return;
            //}

            // try to collapse the edge
            long t0;
            if (STATS) {
                t0 = System.nanoTime();
            }
            boolean res = m_mesh.collapseEdge(edgeCandidate.edge, edgeCandidate.point, ecparam, ecresult);

            if (STATS) {
                long ct = System.nanoTime() - t0;
                collapseTime += ct;
                if(ct > maxCollapseTime)
                    maxCollapseTime = ct;
                if(ct < minCollapseTime)
                    minCollapseTime = ct;

                collapseCount++;
            }

            if(!res){                
                //printf("failed to collapse\n");
                switch(ecresult.returnCode){
                case EdgeCollapseResult.FAILURE_SURFACE_PINCH:
                    surfacePinchCount++; 
                    break;
                case EdgeCollapseResult.FAILURE_FACE_FLIP:
                    faceFlipCount++; 
                    break;                
                case EdgeCollapseResult.FAILURE_LONG_EDGE:
                    longEdgeCount++; 
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled case in return code");
                }
                unlockEdges(m_edgesLock, lockedEdges);                
                return;                
            } 
                

            //printf("collapsed. collapsedFaces: %d\n",ecresult.faceCount);
            
            collapsedFaces += ecresult.faceCount;
                
            
            // assign new quadric to moved vertex
            //TODO
            //int srcIdx = ed.vertexUserData;
            //int destIdx = Vertex.getUserData(m_mesh.getVertices(), m_ecr.insertedVertex);
            //Quadric.set(quadrics, srcIdx, quadrics, destIdx);
            
            int[] edges_removed = ecresult.removedEdges;
            
            //printf("edges_removed: %s\n",edges_removed);
            //printf("edges_removed count: %d\n",edges_removed.length);

            collapsedEdges += edges_removed.length;            

            StructMixedData edges = m_mesh.getEdges();

            for(int i=0; i < edges_removed.length; i++) {
                int index = Edge.getUserData(edges,edges_removed[i]);
                // remove edge from array 
                edgeSelector.set(index, EdgeSelector.NO_DATA);
            }
            
            if(edgeCandidate.errorValue > maxError){
                maxError = edgeCandidate.errorValue;
            }                                    

            unlockEdges(m_edgesLock, lockedEdges);                

        } // doIterationMT()


        /**
           return next random candidate edge 
           each thread has its own random selector 
        */
        boolean getEdgeCandidate(EdgeData ed){
            
            if(m_edgeTester != null){
                
                int count = 100; // 100 tries to get collapsible edge 
                
                while(count-- > 0){
                    edgeSelector.getRandomEdge(ed); 
                    if(m_edgeTester.canCollapse(ed.edge))
                        return true;
                }
                throw new RuntimeException("failed to find collapsable edge");
            } else {
                // test if this eats the time 
                return edgeSelector.getRandomEdge(ed); 

            }                
        }
        
    
    } // class DecimatorRunner

    

    /**
       tries to lock edge surrounding given edge and return true if succssful 
       if failed to lock any edge - unlocks already locked and return false
       stores locked edges into lockedEdges 
       locked edges are marked in edgesLock
    */
    static boolean lockEdges(EdgeData edgeData, StackOfInt lockedEdges, AtomicIntegerArray edgesLock, TriangleMesh mesh){

        StructMixedData edges = mesh.getEdges();
        StructMixedData hedges = mesh.getHalfEdges();
        
        int e = edgeData.edge;
        
        int hR = Edge.getHe(edges, e);

        int he; // working half edge 
        int edge; // working edge 
        
        // he to stop v1 cycle 
        int hL = HalfEdge.getTwin(hedges, hR);
        // triangles around v1 
        he = hR;
        while (true) {
            
            edge = HalfEdge.getEdge(hedges,he);
            //printf("step 1: edge: %d\n", edge);
            if(lockEdge(edgesLock, edge)){
                lockedEdges.push(edge);
            } else {
                //printf("     -> failed to lock\n"); 
                return false;
            }
            he = HalfEdge.getNext(hedges,he);

            edge = HalfEdge.getEdge(hedges,he);
            //printf("step 2: edge: %d\n", edge);
            if(lockEdge(edgesLock, edge)){
                lockedEdges.push(edge);
            } else {
                //printf("     -> failed to lock\n"); 
                return false;
            }

            he = HalfEdge.getNext(hedges,he);
            if(he == hL)
                break;

            he = HalfEdge.getTwin(hedges,he);
        }

        // remaining triangles around v0 
        int hRn = HalfEdge.getNext(hedges,hR);
        // he to stop the v0 cycle 
        int hRnt = HalfEdge.getTwin(hedges,hRn);
        
        int hLpt = HalfEdge.getTwin(hedges, HalfEdge.getPrev(hedges, hL));
        
        he = hLpt;
        while (true) {

            he = HalfEdge.getNext(hedges, he);  
          
            edge = HalfEdge.getEdge(hedges, he);
            //printf("step 3: edge: %d\n", edge);
            if(lockEdge(edgesLock, edge)){
                lockedEdges.push(edge);
            } else {
                //printf("     -> failed to lock\n"); 
                return false;
            }
            he = HalfEdge.getNext(hedges, he); 

            if(he == hRnt) {
                // last edge was already counted in cycle around v1 
                break;
            }
            
            edge = HalfEdge.getEdge(hedges, he);
            //printf("step 4: edge: %d\n", edge);
            if(lockEdge(edgesLock, edge)){
                lockedEdges.push(edge);
            } else {
                //printf("     -> failed to lock\n"); 
                return false;
            }
            
            he = HalfEdge.getTwin(hedges,he);
            
        }
        
        
        return true;
    }
    

    /**
       unlocks edges locked in edgesLock
    */
    static final void unlockEdges(AtomicIntegerArray edgesLock, StackOfInt lockedEdges){
        
        int edge;
        while((edge = lockedEdges.pop()) != lockedEdges.NO_DATA){
            unlockEdge(edgesLock, edge);
        }
        
    }
        
    static final void unlockEdge(AtomicIntegerArray edgesLock, int edge){
        edgesLock.compareAndSet(edge, 1, 0);        
    }

    static final boolean lockEdge(AtomicIntegerArray edgesLock, int edge){

        return edgesLock.compareAndSet(edge, 0, 1);  

    }
    


    /**
       mid edge vertex placement 
     */
    static class ErrorMidEdgeMT implements ErrorFunction {

        private TriangleMesh mesh;
        private Point3d p0 = new Point3d();
        private Point3d p1 = new Point3d();
        
        public void init(TriangleMesh mesh){
            // do nothing
            this.mesh = mesh;
        }

        public void calculateError(EdgeData ed){
            
            int edge = ed.edge;
            int he = Edge.getHe(mesh.getEdges(), edge);
            if(he == -1){
                printf("error: bad halfEdge:%d in calculateErrorFunction(), edge: %d, index: %d \n", 
                       he, edge, Edge.getUserData(mesh.getEdges(), edge));
                ed.errorValue = Double.MAX_VALUE;
                return;
            }
            
            int v0 = HalfEdge.getStart(mesh.getHalfEdges(), he);
            int v1 = HalfEdge.getEnd(mesh.getHalfEdges(), he);
            Vertex.getPoint(mesh.getVertices(), v0, p0);
            Vertex.getPoint(mesh.getVertices(), v1, p1);

            ed.errorValue = p0.distanceSquared(p1);
            
            return;
            
        }

        //
        // simple mid point placement
        //
        public void calculateVertex(EdgeData ed){

            
            int edge = ed.edge;
            int he = Edge.getHe(mesh.getEdges(), edge);

            if(ed.point == null)
                ed.point = new Point3d();

            Point3d point = ed.point; 

            int start = HalfEdge.getStart(mesh.getHalfEdges(), he);
            int end = HalfEdge.getEnd(mesh.getHalfEdges(), he);
            Vertex.getPoint(mesh.getVertices(), start, point);
            Vertex.getPoint(mesh.getVertices(), end, p1);
            point.add(p1);
            point.scale(0.5);
            
        }            

    } // ErrorMidEdge

}

