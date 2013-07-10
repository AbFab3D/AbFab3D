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
import abfab3d.grid.ArrayInt;

import abfab3d.util.MathUtil;

import static java.lang.Math.sqrt;


import static abfab3d.util.MathUtil.distance;
import static abfab3d.util.MathUtil.midPoint;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;



/**
   measures distance between two triangle meshes 

   @author Vladimir Bulatov 

i */
public class MeshDistance {
    
    static final boolean DEBUG = false;
    static int debugCount = 1;
    double m_maxTriangleSize = 0.1*MM;
    // do we need to split source mesh triangles? 
    boolean m_doTriangleSplit = false;
    int maxSplitLevel = 20; // very deep split. THis is mostly to avoid stack overfow on recursion 
    boolean m_hashDistanceValues = false;
    boolean m_useTriBuckets = false;
    double m_triBucketSize = 2*MM;

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
       
     */
    public void setUseTriBuckets(boolean value){
        m_useTriBuckets = value;
    }

    public void setTriBucketSize(double value){
        m_triBucketSize = value;
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
            printf("  target mesh triangles: %d\n", targetBuilder.getTriCount());

        IndexedTriangleSetBuilder its = targetBuilder.getIndexedFaceSet();
        m_targetMesh = new ProcessedMesh(its.getVertices(new double[3*its.getVertexCount()]),
                                         its.getFaces(new int[3*its.getFaceCount()])
                                         );
        if(m_useTriBuckets)
            m_targetMesh.buildTriBuckets(m_triBucketSize);
        
        m_source = new SourceMeshProcessor();
        sourceMesh.getTriangles(m_source);
        
        if(DEBUG){
            printf("  source mesh triangles: %d\n", m_source.getTriCount());
            printf("  distance measurements count: %d\n", m_source.getDistCount());
            printf("  HDF distance: %5.3f mm\n", m_source.getHausdorffDistance()/MM);
            printf("  L_1 distance: %5.3f mm\n", m_source.getL1Distance()/MM);
            printf("  L_2 distance: %5.3f mm\n", m_source.getL2Distance()/MM);
            printf("  min distance: %5.3f mm\n", m_source.getMinDistance()/MM);
            printf("  measure time: %d ms\n", (time() - t0));
            if(m_targetMesh.triBuckets != null)
                m_targetMesh.triBuckets.printStat();

        }        
        
    }

    public double getHausdorffDistance(){
        return m_source.getHausdorffDistance();
    }

    public double getL1Distance(){
        return m_source.getL1Distance();
    }

    public double getL2Distance(){
        return m_source.getL2Distance();
    }

    public double getMinDistance(){
        return m_source.getMinDistance();
    }

    
    /**
       builds preprocessed target mesh 
     */
    static class TargetMeshBuilder implements TriangleCollector {

        private IndexedTriangleSetBuilder its;

        TargetMeshBuilder(){
            its = new IndexedTriangleSetBuilder();
        }
        
        public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
            its.addTri(v0, v1, v2);
            return true;
        }

        IndexedTriangleSetBuilder getIndexedFaceSet() {
            return its;
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
        public int getDistCount(){
            return distCount;
        }
        
        // distance in L_H metric
        double getHausdorffDistance(){
            return sqrt(maxDistance);
        }

        // minimal distance between meshes
        double getMinDistance(){
            if(DEBUG)
                printf("minDistance: %s\n",minDistance);
            double s = sqrt(minDistance);
            if(DEBUG)
                printf("         s: %s\n",s);
            return s; 
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

        TriBuckets triBuckets;
        
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

            double minDist = 0;

            if(m_useTriBuckets)
                minDist = getDistanceSquaredBuckets(v);            
            else 
                minDist = getDistanceSquaredSimple(v);            
            
            if(m_hashDistanceValues){
                distArray.add(new Double(minDist));
            }
            return minDist;
        }

        double getDistanceSquaredSimple(Vector3d v){ 

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
            
            return minDist;
        }
        
        ArrayInt triangles = new ArrayInt(10);

        double getDistanceSquaredBuckets(Vector3d v){ 
            
            triBuckets.getTriangles(v.x, v.y, v.z, triangles);
            
            double minDist = Double.MAX_VALUE;

            int s = triangles.size();

            for(int i = 0; i < s; i++){

                int f = 3*triangles.get(i);
                
                int i0 = 3*m_faces[f];
                int i1 = 3*m_faces[f+1];
                int i2 = 3*m_faces[f+2];
                
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
                if(d < 0.){
                    if(DEBUG && debugCount-- > 0 ){
                        printf("NEGATIVE DISTANCE: %s\n", d);
                        printf("v:  %21.17e %21.17e %21.17e\n", v.x, v.y, v.z);
                        printf("v0: %21.17e %21.17e %21.17e\n", v0.x, v0.y, v0.z);
                        printf("v1: %21.17e %21.17e %21.17e\n", v1.x, v1.y, v1.z);
                        printf("v2: %21.17e %21.17e %21.17e\n", v2.x, v2.y, v2.z);
                    }
                    d = 0;
                }
                if(d < minDist)
                    minDist = d;
            }

            return minDist;

        }

        void buildTriBuckets(double bucketSize){
            
            triBuckets = new TriBuckets(m_vertices, m_faces, bucketSize);

        }

    } // class ProcessedMesh 

} // class MeshDistance 
