/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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
import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import java.util.Random;


import abfab3d.io.output.MeshExporter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// External Imports

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static java.lang.System.nanoTime;

import static abfab3d.mesh.TestMeshDecimator.loadMesh;

/**
 * Tests the functionality of MeshDecimatorMT 
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestMeshDecimatorMT extends TestCase {
    
    static final double MM = 1000; // m -> mm conversion 
    static final double MM3 = 1.e9; // m^3 -> mm^3 conversion 

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMeshDecimatorMT.class);
    }


    public void testFile() throws Exception {

        //String fpath = "test/models/speed-knot.x3db";
        //String fpath = "test/models/sphere_10cm_rough_manifold.x3dv";
        //String fpath = "test/models/sphere_10cm_smooth_manifold.x3dv";
        
        //String fpath = "c:/tmp/text_iso_0.stl";
        String fpath = "c:/tmp/leaf_01.stl";
        //String fpath = "c:/tmp/text_iso_2.stl";
        //String fpath = "c:/tmp/sf31.stl";
        //String fpath = "c:/tmp/leaf_01_0832206.stl";
        //String fpath = "c:/tmp/sf21.stl";
        //String fpath = "c:/tmp/rtc_v3_04.stl";
        
        long t0 = time();
        WingedEdgeTriangleMesh mesh = loadMesh(fpath);
        printf("mesh loading: %d ms\n",(time() - t0));
        t0 = time();

        int fcount = mesh.getFaceCount();
        MeshExporter.writeMeshSTL(mesh,fmt("/tmp/00_mesh_orig_%07d.stl", fcount));

        printf("mesh faces: %d, vertices: %d, edges: %d\n", fcount,mesh.getVertexCount(), mesh.getEdgeCount());        
        printf("initial counts: faces: %d, vertices: %d, edges: %d \n", mesh.getFaceCount(),mesh.getVertexCount(), mesh.getEdgeCount());

        //assertTrue("Initial Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));

        //MeshDecimator md = new MeshDecimator();
        MeshDecimatorMT md = new MeshDecimatorMT();
        md.setThreadCount(4); 

        md.setMaxCollapseError(1.0);

        int target;
        int current;

        int count = 1;
        
        while(count-- > 0) {
            
            target = mesh.getTriangleCount() / 2;
            printf("Target face count : %d \n", target);
            t0 = time();
            md.processMesh(mesh, target);
            //md.DEBUG = true;
            printf("processMesh() done %d ms\n",(time()-t0));
            fcount = mesh.getFaceCount();
            printf("Actual face count: %d ms\n",fcount);
            
            MeshExporter.writeMeshSTL(mesh,fmt("/tmp/00_mesh_%07d.stl", fcount));

            current = fcount;
            
            System.out.println("Current face count: " + current);

            if (current >= target * 1.25) {
                System.out.println("Leaving loop");
                // not worth continuing
                break;
            }

        }
        
    }


    public void  _testEdgeArrayMT() throws Exception {

        printf("testEdgeArrayMT()\n");

        int arraySize = 40000000;
        long t0 = nanoTime();

        MeshDecimator.EdgeArray array  = new MeshDecimator.EdgeArray(arraySize);

        printf("array created int %7.3f ms\n", (nanoTime() - t0)/1000000.);

    }

    public void  _testAtomicArray() throws Exception {
        
        printf("testAtomicArray()\n");
        
        int arraySize = 40000000;
        int lockSize = 100;

        AtomicIntegerArray lockedEdges = new AtomicIntegerArray(arraySize);
        AtomicInteger counter = new AtomicInteger(arraySize/2);
        int threadCount = 4;

        int edgeData[] = new int[arraySize];
        for(int i = 0; i < arraySize; i++){
            edgeData[i] = 1;
        }

        printf("initial count: %d, lock size: %d, threads: %d \n",arraySize, lockSize, threadCount);

        long t0 = time();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        EdgeReducer threads[] = new EdgeReducer[threadCount];

        for(int i = 0; i < threadCount; i++){

            threads[i] = new EdgeReducer(edgeData, lockedEdges, counter, lockSize);
            executor.submit(threads[i]);

        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printf("reduction done: %d ms\n", (time()-t0));  
        int cnt = 0;
        for(int i = 0; i < arraySize; i++){
            if(edgeData[i] == 1)
                cnt++;
        }
        printf("final count: %d\n", cnt);        
        for(int i = 0; i < threadCount; i++){
            printf("badEdgeCount: %d, badLockCount: %d\n", threads[i].badEdgeCount, threads[i].badLockCount);
        }

        
    }

    static class EdgeReducer implements Runnable {
        AtomicIntegerArray lockedEdges;
        int edges[];
        AtomicInteger counter;
        int lockSize;
        Random rnd;
        int badEdgeCount = 0;
        int badLockCount = 0;

        EdgeReducer(int edges[], AtomicIntegerArray lockedEdges, AtomicInteger counter, int lockSize){
            
            this.lockedEdges = lockedEdges;
            this.edges = edges;            
            this.counter = counter;
            this.lockSize = lockSize;
            this.rnd = new Random();
            //printf("new EdgeReducer(%s)\n", counter);
            
        }                 
        
        public void run(){

            //printf("%s.run()\n", this);

            while(true){
                // 
                int dataSize = edges.length;
                int edge = rnd.nextInt(dataSize);  

                if(edges[edge] == 0){
                    badEdgeCount++;
                    continue;
                }
                
                //printf("edge: %d\n", edge);
                if(lockNeighbors(edge))
{
                    if(counter.decrementAndGet() >= 0){
                        // do edge collapse 
                        edges[edge] = 0;                       
                        // unlock back 
                        unlockNeighbors(edge);                      
                    } else {
                        // unlock back 
                        unlockNeighbors(edge);
                        break;
                    }
                } else {
                    badLockCount++;
                }
            }
        }
        
        boolean lockNeighbors(int edge){
                        
            int dataSize = edges.length;
            for(int k = -lockSize; k <= lockSize; k++){
                
                int lock = (edge + k + dataSize) % dataSize;
                
                if(!lockedEdges.compareAndSet(lock, 0, 1)){
                    // faled to lock - unlock locked edges 
                    for(int u = -lockSize; u < k; u++){
                        lock = (edge + u + dataSize) % dataSize;
                        lockedEdges.compareAndSet(lock, 0, 0);
                    }
                    return false;
                }
            }

            // we locked all the edges 
            return true;
        }

        boolean unlockNeighbors(int edge){

            int dataSize = edges.length;
            for(int k = -lockSize; k <= lockSize; k++){
                
                int lock = (edge + k + dataSize) % dataSize;
                
                if(!lockedEdges.compareAndSet(lock, 1, 0)){
                    // failed to unlock - what do do? 
                    printf("failed to unlock edge %d\n", lock);
                }
            }

            // we unlocked locked all the edges 
            return true;
        }

    }

}

