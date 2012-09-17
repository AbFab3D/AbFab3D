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
package abfab3d.io.output;

import java.util.Set;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

import abfab3d.util.TriangleCollector;

import abfab3d.mesh.Vertex;
import abfab3d.mesh.HalfEdge;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;


/**

   class to export ocal environment of a Vertex or of 
 */
public class VertexExporter {


    /**
       writes triangles surrounding the given set of vertices into STL file 
     */
    public static void exportVertexSTL(Set<Vertex> vset, String fpath){
        
        try {
            STLWriter writer = new STLWriter(fpath);
        
            for(Vertex v: vset){
                
                printf("vertex: %s\n", v);
                
                HalfEdge start = v.getLink();
                HalfEdge he = start;
                int tricount = 0;

                do{ 
                    
                    printf("he: %s\n", he);
                    writeTriangle(writer,he);
                    
                    HalfEdge twin = he.getTwin();
                    he = twin.getNext();  
                                        
                } while(he != start && tricount++ < 20);
                
            }

            writer.close();

        } catch (Exception e){
            
            e.printStackTrace();
            
        }        
    }

    static void writeTriangle(TriangleCollector tc, HalfEdge he){
               
        Point3d p0 = he.getStart().getPoint();
        he = he.getNext();
        Point3d p1 = he.getStart().getPoint();
        he = he.getNext();
        Point3d p2 = he.getStart().getPoint();
        
        printf("tri: %s, %s %s\n", fp(p0),fp(p1),fp(p2));
        
        tc.addTri(new Vector3d(p0),new Vector3d(p1),new Vector3d(p2));

    }

    public static String fp(Point3d p){

        return fmt("(%8.5f,%8.5f,%8.5f)", p.x, p.y, p.z);
        
    }
       
}
