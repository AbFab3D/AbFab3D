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

import java.util.Set;
import javax.vecmath.Vector3d;

import abfab3d.io.output.STLWriter;
import abfab3d.util.TriangleCollector;

import static abfab3d.util.Output.printf;


/**

   class to export ocal environment of a Vertex or of 
 */
public class VertexExporter {


    /**
       writes trianles surrounding set of vertices into STL file 
     */
    public void exportVertexSTL(Set<Vertex> vset, String fpath){
        
        try {
            STLWriter writer = new STLWriter(fpath);
        
            for(Vertex v: vset){
                
                printf("vertex: %s\n", v);
                
                HalfEdge start = v.getLink();
                HalfEdge he = start;
                
                do{ 
                    
                    addTriangle(writer,he);
                    
                    HalfEdge twin = he.getTwin();
                    he = twin.getNext();  
                                        
                } while(he != start);
                
            }

            writer.close();

        } catch (Exception e){
            
            e.printStackTrace();
            
        }
        
    }


    static void addTriangle(TriangleCollector tc, HalfEdge he){
               
        Vertex vert = he.getStart();
        Vector3d v0 = new Vector3d(vert.getPoint());
        
    }
       
}
