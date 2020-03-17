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
package abfab3d.util;

import java.util.Arrays;
import java.util.HashMap;
import javax.vecmath.Vector3d;


import static java.lang.Math.*;
import static abfab3d.core.Output.printf;

/**
 * A set-like interface for storing points 
 * points are compared exactly 
 */
public class PointMap3  implements IPointMap {

    static final boolean DEBUG = false;

    HashMap<Entry, Integer> m_map;
    PointSetArray m_points;
    PointSetArray m_workPoint;
    Entry m_workEntry;

    public PointMap3(int initialCapacity, double loadFactor) {

        m_map = new HashMap<Entry,Integer>(initialCapacity, (float)loadFactor);
        m_points = new PointSetArray(initialCapacity);

        m_workPoint = new PointSetArray(3);
        
        m_workEntry = new Entry(m_workPoint,0,0);
    }

    public void clear(){
        m_map.clear();
        m_points.clear();
        
    }

    /**
       add new point to the map if there is new point
       return index of that point 
     */
    public int add(double x, double y, double z) {
        
        int oldIndex = get(x,y,z);
        if(DEBUG) printf("add(%8.6f,%8.6f,%8.6f): oldIndex:%d\n", x, y, z, oldIndex);
        if(oldIndex >= 0) 
            return oldIndex;
        
        int newIndex = m_points.size();
        if(DEBUG) printf("  new point:%d\n", newIndex);
        m_points.addPoint( x, y, z);

        int hash = getHash(x,y,z);
        m_map.put(new Entry(m_points, newIndex, hash), new Integer(newIndex));

        return newIndex;
    }    
    
    /**
       return index of existing point or -1;
     */
    public int get(double x, double y, double z) {
        
        int hash = getHash(x,y,z);

        if(DEBUG) printf("get(%8.6f,%8.6f,%8.6f): %d\n", x, y, z, hash);

        m_workEntry.set(hash, x,y,z);
        Integer value = m_map.get(m_workEntry);        
        if(value != null){
            if(DEBUG) printf(" found: %d\n", value.intValue());
            return value.intValue();
        }
        return -1;
        
    }

    public double[] getPoints() {
        
        double[] pnts = new double[m_points.size()* 3];
        return getPoints(pnts);
        
    }

    /**
     *  fill array with points in the map 3 coordinated per point
     *  Ordering of points in returned array point is the order in which they were added 
     *  If points count exced the capacity of provided array, new array is alocated. 
     *  @param array - memory to receive the points coordinates pntx, pnty, pntz 
     *  @return original array or new allocated array 
     */
    public double[] getPoints(double array[]) {
        if(array.length < m_points.size()* 3){
            array = new double[m_points.size()* 3];
        }
        m_points.getPoints(array);
        return array;
    }

    public int getPointCount(){

        return m_points.size();

    }

    private int getHash(double x,double y,double z){

        long ix = Double.doubleToLongBits(x);
        long iy = Double.doubleToLongBits(y);
        long iz = Double.doubleToLongBits(z);
        
        ix ^= iy;
        ix ^= iz;

        ix = (ix & 0xFFFFFFFF) ^ ((ix >> 32) & 0xFFFFFFFF);

        return (int)ix;
    }

    /**
       represent HashMap key to map point in space onto int hashValue
     */
    class Entry {

        int index;
        PointSetArray points;
        double pnt[] = new double[3];
        int hash;

        Entry(PointSetArray points, int index, int hash){
            this.points = points;
            this.index = index;
            this.hash = hash;
        }

        void set(int hash, double x,double y,double z){

            points.setPoint(this.index, x,y,z);
            this.hash = hash;

        }

        public int hashCode(){
            return hash;
        }    
        public boolean equals(Object obj){
            
            Entry e = (Entry)obj;
            points.getPoint(index, pnt);
            e.points.getPoint(e.index, e.pnt);
            return ((pnt[0] == e.pnt[0]) && 
                    (pnt[1] == e.pnt[1]) && 
                    (pnt[2] == e.pnt[2]));
                    
        }
    } // class Entry 

} // class PointMap3
