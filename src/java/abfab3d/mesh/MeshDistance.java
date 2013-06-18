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

import java.util.Vector;
import javax.vecmath.Vector3d;

import abfab3d.util.TriangleProducer;
import abfab3d.util.TriangleCollector;
import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;


import static abfab3d.util.MathUtil.distance;
import static abfab3d.util.MathUtil.midPoint;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;



/**
   measures distance between two triangle meshes 

   @author Vladimir Bulatov 

 */
public class MeshDistance {
    
    static final boolean DEBUG = true;

    double m_maxTriangleSize = 0.1*MM;
    // do we need to split source mesh triangles? 
    boolean m_doTriangleSplit = false;
    int maxSplitLevel = 20; // very deep split. THis is mostly to avoid stack overfow on recursion 
    boolean m_hashDistanceValues = false;

    public MeshDistance(){
    }
    
    ProcessedMesh m_targetMesh;
    
    SourceMeshProcessor m_source;


    /**
       hasjh precalculated distance values? 
     */
    public void setHashDistanceValues(boolean value){
        
        m_hashDistanceValues = value;

    }


    /**
       do we need to split source mesh triangles?        
     */
    public void setTriangleSplit(boolean value){
        m_doTriangleSplit = value;
    }

    /**
       set maximum edge length of source triangles 
     */
    public void setMaxTriangleSize(double maxTriangleSize){
        m_maxTriangleSize = maxTriangleSize;
    }


    public void measure(TriangleProducer sourceMesh, TriangleProducer targetMesh){        
        long t0 = time();
        TargetMeshBuilder targetBuilder = new TargetMeshBuilder();
        targetMesh.getTriangles(targetBuilder);
        
        if(DEBUG)
            printf("target mesh: %d triangles\n", targetBuilder.getTriCount());

        IndexedTriangleSetBuilder its = targetBuilder.its;
        m_targetMesh = new ProcessedMesh(its.getVertices(new double[3*its.getVertexCount()]),
                                         its.getFaces(new int[3*its.getFaceCount()])
                                         );
        
        
        m_source = new SourceMeshProcessor();
        sourceMesh.getTriangles(m_source);
        
        if(DEBUG){
            printf("distance measured: %d ms\n", (time() - t0));
            printf("  source triangles: %d\n", m_source.getTriCount());
            printf("  distance measurements: %d\n", m_source.getDistCount());
            printf("  HDF distance: %8.6f\n", m_source.getHausdorffDistance());
            printf("  L_1 distance: %8.6f\n", m_source.getL1Distance());
            printf("  L_2 distance: %8.6f\n", m_source.getL2Distance());
            printf("  min distance: %8.6f\n", m_source.getMinDistance());
            
        }
        
        
    }

    public double getHausdorffDistance(){

        return m_source.getHausdorffDistance();
        
    }
    
    /**
       bilds preprocessed target mesh 
     */
    static class TargetMeshBuilder implements TriangleCollector {

        IndexedTriangleSetBuilder its;
        TargetMeshBuilder(){
            its = new IndexedTriangleSetBuilder();
        }
        
        public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
            its.addTri(v0, v1, v2);
            return true;
        }
        int getTriCount() {
            return its.getFaceCount();
        }

    } // class TargetMeshBuilder

    /**
       
     */
    class SourceMeshProcessor implements TriangleCollector {

        int triCount = 0;
        int distCount = 0;
        double maxDistance = 0.;
        double minDistance = Double.MAX_VALUE;
        double distanceSum1 = 0.;
        double distanceSum2 = 0.;
        
        public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){

            triCount++;
            if(m_doTriangleSplit){
                splitTriangle(v0, v1, v2, maxSplitLevel);
            } else {
                processTriangle(v0, v1, v2);
            }

            return true;
        }

        void processTriangle(Vector3d v0,Vector3d v1,Vector3d v2){

            processVertex(v0);
            processVertex(v1);
            processVertex(v2);
        }

        void splitTriangle(Vector3d v0, Vector3d v1, Vector3d v2, int splitLevel){
                
        splitLevel--;
        if(splitLevel <= 0){
            processTriangle(v0, v1, v2);
            return;
        }            

        Vector3d 
            v01 = null, 
            v12 = null, 
            v20 = null;


        int selector = 0;
        if(distance(v0, v1) > m_maxTriangleSize) { selector += 1; v01 = midPoint(v0, v1);}
        if(distance(v1, v2) > m_maxTriangleSize) { selector += 2; v12 = midPoint(v1, v2);}
        if(distance(v2, v0) > m_maxTriangleSize) { selector += 4; v20 = midPoint(v2, v0);}
        
        switch(selector){                
            
        case 0: // no splits 
            processTriangle(v0, v1, v2);
            break;
            
        case 1: // split 01         
            splitTriangle(v0, v01, v2, splitLevel);
            splitTriangle(v01, v1, v2, splitLevel);
            break;
        
        case 2:  // split 12         
            splitTriangle(v0, v1, v12, splitLevel);
            splitTriangle(v0, v12, v2, splitLevel);
            break;
        
        
        case 4:  // split 20 
            splitTriangle(v1, v2, v20, splitLevel);
            splitTriangle(v1, v20, v0, splitLevel);
            break;
        
        
        case 3:  // split 01, 12         
            splitTriangle(v1, v12, v01,splitLevel);

            if(distance(v01, v2) < distance(v0, v12)) {
                splitTriangle(v01, v12, v2,splitLevel);
                splitTriangle(v0, v01, v2, splitLevel);
            } else {
                splitTriangle(v01, v12, v0,splitLevel);
                splitTriangle(v0, v12, v2, splitLevel);
            }
            break;
        
        
        case 6:  //split 12 20 
            
            splitTriangle(v12,v2, v20,splitLevel);
            if(distance(v0, v12) < distance(v1, v20)) {
                splitTriangle(v0, v12,v20,splitLevel);
                splitTriangle(v0, v1, v12,splitLevel);
            } else {
                splitTriangle(v0, v1, v20,splitLevel);
                splitTriangle(v1, v12, v20, splitLevel);
            }
            break;
            
        case 5:  // split 01, 20 
            splitTriangle(v0, v01, v20, splitLevel);
            if(distance(v01, v2) < distance(v1, v20)){
                splitTriangle(v01, v2, v20, splitLevel);
                splitTriangle(v01, v1, v2,  splitLevel);
            } else {
                splitTriangle(v01, v1, v20, splitLevel);                
                splitTriangle(v1, v2, v20, splitLevel);                
            }
            break; // split s0, s2       
        
        case 7: // split 01, 12, 20       
            
            splitTriangle(v0, v01, v20,splitLevel);
            splitTriangle(v1, v12, v01,splitLevel);
            splitTriangle(v2, v20, v12,splitLevel);
            splitTriangle(v01, v12, v20,splitLevel);
            break;                  
        } // switch()
   
    } // splitTriangle 


        void processVertex(Vector3d v){

            double d2 = m_targetMesh.getDistanceSquared(v);

            if(d2 > maxDistance)
                maxDistance = d2;
            if(d2 < minDistance)
                minDistance = d2;

            distanceSum1 += sqrt(d2);
            distanceSum2 += d2;

            distCount++;
            
        }


        int getTriCount(){
            return triCount;
        }
        int getDistCount(){
            return distCount;
        }
        
        // distance in L_H metric
        double getHausdorffDistance(){
            return sqrt(maxDistance);
        }

        // minimal distacne between meshes
        double getMinDistance(){
            return sqrt(minDistance);
        }
        // distance in L_1 metric
        double getL1Distance(){
            return distanceSum1/distCount;
        }
        // distance in L_2 metric
        double getL2Distance(){
            return sqrt(distanceSum2/distCount);
        }

    } // class SourceMeshProcessor



    /**
       class responsible for calculation of distance between point and mesh 
     */
    class ProcessedMesh {
        
        double m_vertices[];
        int m_faces[];
        
        PointSet pointTable;
        Vector<Double> distArray;
        final double EPSILON = 1.e-8;

        ProcessedMesh(double vertices[], int faces[]){

            m_faces = faces;
            m_vertices = vertices;
            if(m_hashDistanceValues){
                distArray = new Vector<Double>();
                pointTable = new PointSet(EPSILON);
            }
                
        }
               
        /**
           return minimum distance to target mesh 
        */
        Vector3d  // working variables 
            v0 = new Vector3d(),
            v1 = new Vector3d(),
            v2 = new Vector3d();
        // distance from point to mesh 
        double getDistanceSquared(Vector3d v){
            
            int index = 0;
            if(m_hashDistanceValues){
                index = pointTable.add(v.x,v.y,v.z);
                if(index < distArray.size()){
                    // existing point
                    return distArray.get(index).doubleValue();
                }
            }
            double minDist = Double.MAX_VALUE;
            
            for(int i = 0; i < m_faces.length; i += 3){

                int i0 = 3*m_faces[i];
                int i1 = 3*m_faces[i+1];
                int i2 = 3*m_faces[i+2];
                
                v0.x = m_vertices[i0];
                v0.y = m_vertices[i0+1];
                v0.z = m_vertices[i0+2];
                v1.x = m_vertices[i1];
                v1.y = m_vertices[i1+1];
                v1.z = m_vertices[i1+2];
                v2.x = m_vertices[i2];
                v2.y = m_vertices[i2+1];               
                v2.z = m_vertices[i2+2];
                
                double d = PointToTriangleDistance.getSquared(v, v0, v1, v2);
                if(d < minDist)
                    minDist = d;
            }

            if(m_hashDistanceValues){
                distArray.add(new Double(minDist));
            }
            return minDist;
        }

    }
    
} // class MeshDistance 
