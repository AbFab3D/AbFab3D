/** 
 *                        Shapeways, Inc Copyright (c) 2019
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


import java.util.Arrays;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.MathUtil.str;
import static java.lang.Math.*;


/**
   represent a contour as circular sequence of point indices 
*/
public class Contour {
    
    static final boolean DEBUG = false;
    
    int m_points[] = new int[2];
    int m_size = 0;
    boolean m_closed = false;
    
    int m_start = m_points.length; // place for next point at the start of contour
    int m_end = 0;  // place for next point at the end of contour 

    Contour(){
        
    }
    
    Contour(int i0, int i1){
        
        append(i0);
        append(i1);
        
    }
    
    public int size(){
        return m_size;
    }
    
    public int get(int index){
        return m_points[(index+m_start) % m_points.length];
    }
    
    
    int getStart(){
        return m_points[m_start % m_points.length];
    }
    
    int getEnd(){

        return m_points[m_end-1];

    }
    
    
    
    /**
       append contour to this contour 
    */
    void append(Contour c){
        
        int newSize =  m_size + c.m_size;
        ensureCapacity(newSize);

        int size1 = c.m_points.length - c.m_start;
        if(size1 > 0) {
            System.arraycopy(c.m_points, c.m_start, m_points, m_end,size1);
            m_end += size1;
        }
        int size2 = c.m_end; 
        if(size2 > 0) {
            System.arraycopy(c.m_points, 0, m_points, m_end,size2);
            m_end += size2;            
        }

        m_size = newSize;
        
    }        
    
    /**
       prepend contour to this contour 
    */
    void prepend(Contour c){
        
        int newSize =  m_size + c.m_size;
        ensureCapacity(newSize);

        int size1 = c.m_end;
        if(size1 > 0) {
            System.arraycopy(c.m_points, 0, m_points, m_start-size1,size1);
            m_start -= size1;
        }
        int size2 = c.m_points.length - c.m_start; 
        if(size2 > 0) {
            System.arraycopy(c.m_points, c.m_start, m_points, m_start-size2,size2);
            m_start -= size2;            
        }

        m_size = newSize;
        
    }        

    /**
       add point at the end of the chain 
    */
    void append(int index){
        if(DEBUG)printf("append(%d), start:%d, end:%d\n", index, m_start, m_end);
        ensureCapacity(m_size+1);        
        m_points[m_end] = index;
        m_end++;
        m_size++;
        if(DEBUG)printf("   append , start:%d, end:%d\n", index, m_start, m_end);
        
    }        

    /**
       add point at the beginning of the chain 
    */
    public void prepend(int index){
        
        if(DEBUG)printf("prepend(%d), start:%d, end:%d, size:%d\n", index, m_start,m_end, m_size);
        
        ensureCapacity(m_size + 1);  

        m_points[--m_start] = index;
        //System.arraycopy(m_points, 0, m_points, 1, m_size);
        //m_points[0]  = index;
        m_size++;
        if(DEBUG)printf("   after prepend, start:%d, end:%d array:%s\n", m_start, m_end, m_size, toString());

    }
    
    public void ensureCapacity(int newSize){
        
        if(DEBUG)printf("ensureCapacity(%d)\n",newSize);
        if(newSize > m_points.length){
            
            int pSize = max(m_points.length*2, newSize);
            if(DEBUG)printf("realloc array pSize:%d\n", pSize);
            int points[] = new int[pSize];
            // copy 
            System.arraycopy(m_points, 0, points, 0, m_end);

            int tailLength = (m_points.length - m_start);
            int newStart = points.length - tailLength;
            if(tailLength > 0) {
                if(DEBUG)printf("arraycopy(from:%d, to:%d, count:%d)\n",m_start, newStart,tailLength);
                System.arraycopy(m_points, m_start, points, newStart, tailLength);
            }

            m_start = newStart;
            m_points = points;
        }
    }
    
    public boolean isClosed(){
        return m_closed;
    }
    
    public void close(){
        m_closed = true;
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for(int i = 0; i < m_size; i++){
            sb.append(fmt("%2d ", get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
    
}// class Contour 

