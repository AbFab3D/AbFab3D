/***************************************************************************
 * 
 *                        Shapeways, Inc Copyright (c) 2020
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.geom;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static java.lang.Math.*;

public class Vertices {
    
    Vertex vertices[] = new Vertex[10];
    int m_count = 0;
    
    Vertex get(int index){
        
        ensureCapacity(index);
        if(vertices[index] == null) {
            m_count++;
            vertices[index] = new Vertex();
        }
        return vertices[index];
    }


    public int size(){
        return m_count;
    }

    void ensureCapacity(int index){
        if(index >= vertices.length){
            Vertex newvert[] = new Vertex[max((index +1), vertices.length)];
            System.arraycopy(vertices, 0, newvert, 0, vertices.length);
            vertices = newvert;
        }        
    }
    
    void toSB(StringBuffer sb){
        for(int i = 0; i < vertices.length; i++){
            Vertex v = vertices[i];
            if(v != null){
                sb.append(fmt("v[%d]:", i));
                v.toSB(sb);
                sb.append("\n");
            }
        }
    }
    
} // class Vertices 

