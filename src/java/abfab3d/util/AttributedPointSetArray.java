/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

import javax.vecmath.Vector3d;
import javax.vecmath.Tuple3d;

import java.util.Vector;

import abfab3d.util.TriangleProducer;
import abfab3d.util.TriangleCollector;
import abfab3d.util.PointSet;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
   representation of point set as array of values
   @author Vladimir Bulatov
 */
public class AttributedPointSetArray implements AttributedPointSet  {
    
    
    // points are represented via m_dimension coordinates 
    double coord[] = null;
    int m_dimension = 3;
    int m_size=0; // points count 
    int m_arrayCapacity = 0;
    
    /**
       makes data array with given dimension
     */
    public AttributedPointSetArray(int dimension){
        this(dimension, 1);
    }
    

    /**
       makes data array with given dimension and initial capacity 
     */
    public AttributedPointSetArray(int dimension, int initialCapacity){       

        m_dimension = dimension;
        // avoid wrong behavior 
        if(initialCapacity < 1) initialCapacity = 1;            
        m_arrayCapacity = initialCapacity;
        coord = new double[m_dimension*m_arrayCapacity];
        
    }

    /**
       interface PointSet 
     */
    public int dimension(){
        return m_dimension;
    }

    /**
     * Clear all point and triangle data.
     */
    public void clear() {
        m_size = 0;
        m_arrayCapacity = 1;
        coord = new double[m_dimension*m_arrayCapacity];
    }
    
    /**
       interface PointSet 
     */
    public final void addPoint(Vec point){
        if(m_size >= m_arrayCapacity)
            reallocArray();

        int start = m_size*m_dimension;
        //printf("addPoint(");
        for(int i=0; i < m_dimension; i++){
            coord[start+i] = point.v[i];
            //printf(" %5.1f",point.v[i]);
        }
        //printf(")\n");
        
        m_size++;
    }

    /**
       interface PointSet 
     */
    public int size(){

        return m_size;

    }

    public void getPoint(int index, Vec point){

        int start = index*m_dimension;
        for(int i = 0; i < m_dimension; i++)
            point.v[i] = coord[start+i];
        
    }
    
    private void reallocArray(){

        int ncapacity = 2*m_arrayCapacity;
        double ncoord[] = new double[ncapacity*m_dimension];
        System.arraycopy(coord, 0, ncoord,0,coord.length);
        m_arrayCapacity = ncapacity;
        coord = ncoord;

    }
}
