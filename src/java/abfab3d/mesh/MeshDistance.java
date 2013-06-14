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

import javax.vecmath.Vector3d;

import abfab3d.util.TriangleProducer;
import abfab3d.util.TriangleCollector;


import static abfab3d.util.Output.printf;


/**
   measures distance between two triangle meshes 
 */
public class MeshDistance {
    
    static final boolean DEBUG = true;

    protected double m_maxDistance = Double.MAX_VALUE;
    public MeshDistance(){
    }
    
    public void measure(TriangleProducer sourceMesh, TriangleProducer targetMesh){        

        TargetMeshBuilder target = new TargetMeshBuilder();
        targetMesh.getTriangles(target);
        if(DEBUG)
            printf("target mesh: %d triangles\n", target.tcount);
        
        SourceMeshBuilder source = new SourceMeshBuilder();
        sourceMesh.getTriangles(source);
        if(DEBUG)
            printf("source mesh: %d triangles\n", source.tcount);
        
    }


    public double getMaxDistance(){
        return m_maxDistance;
    }

    static class TargetMeshBuilder implements TriangleCollector {

        int tcount = 0;
        public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
            tcount++;
            return true;
        }
        
    } // class TargetMeshBuilder

    static class SourceMeshBuilder implements TriangleCollector {

        int tcount = 0;
        public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
            tcount++;
            return true;
        }
        
    } // class SourceMeshBuilder

    
} // class MeshDistance 
