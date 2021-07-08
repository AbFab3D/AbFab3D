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
import abfab3d.util.TriangleMesh;

import static abfab3d.core.Output.printf;

/**
   performs smoothing operation on WingedEdgeTriangeMesh

   
   @author Vladimir Bulatov

 */
public class LaplasianSmooth {

    static boolean DEBUG = true;
    static boolean m_printStat = true;

    // mesh we are working on
    private TriangleMesh m_mesh;

    // maximal error allowed during smooth
    private double m_maxError;
    // relative weight of central vertex contribution to new vertex position 
    private double m_centerWeight = 1;

    private StructMixedData points = null;
    private int sum;    // scratch vertex for sum

    /**
       the instance of the LaplasianSmooth can be reused for several meshes  
     */
    public LaplasianSmooth(){
        
    }

    /**

       maximal error allowed during one step 
       this is not used at the moment 
     */
    public void setMaxError(double maxError){
        m_maxError = maxError;
    }

    public void setCenterWeight(double centerWeight){
        m_centerWeight = centerWeight;
    }
    
    /**
       
       run several smoothing iteratins on the mesh
       
       
     */
    public void processMesh(TriangleMesh mesh, int iterationsCount){

        m_mesh = mesh;

        points = new StructMixedData(StructTuple3d.DEFINITION,mesh.getVertexCount() + 1);

        StructMixedData vertices = m_mesh.getVertices();

        // Fast magic, could break if Vertex adds double values
        if (Vertex.DEFINITION.getDoubleDataSize() != 3) {
            throw new IllegalArgumentException("Vertex no longer 3 values, assumption broken");
        }

        // Initialize points to current vertex values with a fast arraycopy
        System.arraycopy(vertices.getDoubleData(), 0, points.getDoubleData(),0, vertices.getDoubleData().length);

        // Add scratch point.  TODO: This is dodgey
        sum = vertices.getDoubleData().length / 3;

        for(int i =0; i < iterationsCount; i++){
            doIteration();
        }
    }

    protected void doIteration(){

        StructMixedData vertices = m_mesh.getVertices();
        StructMixedData hedges = m_mesh.getHalfEdges();

        int v = m_mesh.getStartVertex();

        StructTuple3d.setPoint(0,0,0,points,sum);
        while(v != -1){
            
            int start = Vertex.getLink(vertices, v);

            int he = start;

            int he_start = HalfEdge.getStart(hedges,he);
            StructTuple3d.setPoint(points,he_start,points,sum);

            StructTuple3d.scale(m_centerWeight,points,sum);

            int count = 0;
            do {
                int he_end = HalfEdge.getEnd(hedges,he);

                StructTuple3d.add(vertices,he_end,points,sum);
                count++;

                int twin = HalfEdge.getTwin(hedges,he);
                if (twin == -1) {
                    break;
                }

                he = HalfEdge.getNext(hedges,twin);

            } while(he != start);

            StructTuple3d.scale(1./(m_centerWeight + count), points, sum);

            StructTuple3d.setPoint(points,sum, points, v);

            v = Vertex.getNext(vertices, v);
        }

        // Copy point values back using fast array copy
        System.arraycopy(points.getDoubleData(),0,vertices.getDoubleData(), 0,  vertices.getDoubleData().length);
    }

} // LaplasianSmooth

