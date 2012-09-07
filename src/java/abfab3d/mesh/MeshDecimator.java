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

/**
   decimator to reduce face count of triangle mesh    

   it uses general Quadric error function to calculate penalty and to find new vertex position 
   
   decimator try candidate edges, picks some edge with lowest penalty and 
   find optimal position of new vertex. the edge is collapsed and removed 
   also are removed two triangles adjacent to the edge. Other triangles, which share 
   vertices with collapsed edge chnage it's shape. 
      

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
        
        // init vertices quadrics
        doInitialization();
        
        // do decimation 
        int currentFaceCount = mesh.getFaceCount();
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

    static public class Quadric {
        // holder of quadric matrix 
        public Matrix4d matrix;
        

    }

}

