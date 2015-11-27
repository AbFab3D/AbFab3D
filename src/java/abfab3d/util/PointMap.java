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

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static abfab3d.util.Output.printf;

/**
 * A set like interface for making points unique given some epsilon.
 *
 * The main datastructure for this class is an array of Entry objects.  Each entry object
 * has a singly linked list of nodes with the same hash.
 *
 * TODO:  It's possible that two values will have different hashCodes but be equal with the epsilon test.  We'll
 * need to test the other boundary bucket for equality as well.  This would cause non-manifold meshes.
 *
 * @author Alan Hudson
 */
public class PointMap {
    private boolean COLLECT_STATS = true;
    private final boolean DEBUG = true;

    static final double  // arbitrary constants for hashcode calculations
    CX = 10556796.789, CY = 26556797.891, CZ = 37556792.981, CW = 45556795.955;

    /** References to Entry positions or -1 if not filled */
    private int[] table;

    /** The total number of entries in the hash table. */
    private int count;
    private int threshold;
    private double loadFactor;
    private double epsilon;
    private int origCapacity;

    private StructMixedData entries;

    // scratch variable (non MT safe)
    private double[] sd = new double[3];

    public PointMap(double epsilon) {

        this(10000,0.75f,epsilon);
    }

    public PointMap(int initialCapacity, double loadFactor, double epsilon) {
        this.epsilon = epsilon;

        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                    initialCapacity);
        if (loadFactor <= 0)
            throw new IllegalArgumentException("Illegal Load: "+loadFactor);

        if (initialCapacity == 0)
            initialCapacity = 1;

        this.loadFactor = loadFactor;

        origCapacity = initialCapacity;
        table = new int[initialCapacity];

        //System.out.println("Init: " + this + " capacity: " + initialCapacity);

        Arrays.fill(table, -1);

        threshold = (int)(initialCapacity * loadFactor);
        entries = new StructMixedData(Entry.DEFINITION,threshold);
        
    }

    public void clear(){
        
        Arrays.fill(table, -1);
        entries.clear();
        count = 0;

    }


    /**
     * Add a new point. If it already exists returns old point index. 
     *
     * @param x
     * @param y
     * @param z
     * @return The positions old ID or a newly created ID
     */
    public int add(double x, double y, double z) {

        int hash = calcHash(x,y,z);
        //printf("add(%7.5f, %7.5f, %7.5f) hash: %x eps: %e\n",x,y,z,hash, epsilon);

        int index = (hash & 0x7FFFFFFF) % table.length;


        for(int next = table[index]; next != -1; next = Entry.getNext(entries, next)) {

            if (Entry.getHash(entries, next) == hash) {

                Entry.getPosition(entries, next, sd);

                if (calcEquals(x,y,z,sd[0],sd[1],sd[2])) {
                    return Entry.getID(entries, next);
                }

            }
        }

        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash();

            index = (hash & 0x7FFFFFFF) % table.length;
        }

        // Creates the new entry.
        int e = Entry.createEntry(x,y,z,count,hash,table[index], entries);

        table[index] = e;
        count++;

        return e;
    }

    /**
       @return index of point with given coordinates if it is stored in the map
     */
    public int get(double x, double y, double z) {
        int hash = calcHash(x,y,z);

        int index = (hash & 0x7FFFFFFF) % table.length;

        for(int next = table[index]; next != -1; next = Entry.getNext(entries, next)) {
            if (Entry.getHash(entries, next) == hash) {

                Entry.getPosition(entries, next, sd);

                if (calcEquals(x,y,z,sd[0],sd[1],sd[2])) {
                    return Entry.getID(entries, next);
                }
            }
        }

        return -1;
    }

    /**
     * Get a array of all points.  
     *  Ordering of points in returned atrray point is the order in which they were added 
     *
     * @return array of stored points
     */
    public double[] getPoints() {
        
        double[] ret_val = new double[count * 3];
        return getPoints(ret_val);
        
    }

    /**
     *  fill array with points in the map 3 coordinated per point
     *  Ordering of points in returned atrray point is the order in which they were added 
     *  If points count exced the capacity of provided array, new array is alocated. 
     *  @param array - memory to receive the points coordinates pntx, pnty, pntz 
     *  @return original array or new allocated array 
     */
    public double[] getPoints(double array[]) {

        double ret_val[] = array;
        if(ret_val == null || ret_val.length < count * 3){
            ret_val = new double[count * 3];
        }

        int max_length = 0;
        int count = 0;
        for (int j = 0; j < table.length; j++) {
            int e = table[j];

            if (COLLECT_STATS) count = 0;
            for(int next = e; next != -1; next = Entry.getNext(entries, next)) {
                if (COLLECT_STATS) count++;
                Entry.getPosition(entries, next, sd);
                int id = Entry.getID(entries, next);

                ret_val[id*3] = sd[0];
                ret_val[id*3+1] = sd[1];
                ret_val[id*3+2] = sd[2];
            }
            if (COLLECT_STATS) if(count > max_length) max_length = count;
        }


        if (COLLECT_STATS) printf("Max length of entry: %d\n",max_length);
        return ret_val;
        
    }

    /**
       return coordinates in 3 separate arrays 
     */
    public void getPoints(double pntx[],double pnty[],double pntz[]) {

        if(pntx.length < count){
            throw new RuntimeException("array size is too small");
        }

        int max_length = 0;
        long tot_length = 0;
        int count = 0;

        int len = table.length;
        for (int j = 0; j < len; j++) {
            int e = table[j];
            if (COLLECT_STATS) count = 0;

            for(int next = e; next != -1; next = Entry.getNext(entries, next)) {
                if (COLLECT_STATS) count++;
                Entry.getPosition(entries, next, sd);
                int id = Entry.getID(entries, next);

                pntx[id] = sd[0];
                pnty[id] = sd[1];
                pntz[id] = sd[2];
            }
            if (COLLECT_STATS) {
                tot_length += count;
                if(count > max_length) max_length = count;
            }
        }
        if (COLLECT_STATS) printf("Stats.  max chain: %d, avg chain: %f\n",max_length,(float)tot_length/table.length);
    }

    /**
       return xy coordinates in 2 separate arrays 
     */
    public void getPoints(double pntx[],double pnty[]) {

        if(pntx.length < count){
            throw new RuntimeException("array size is too small");
        }

        for (int j = 0; j < table.length; j++) {
            int e = table[j];

            for(int next = e; next != -1; next = Entry.getNext(entries, next)) {
                Entry.getPosition(entries, next, sd);
                int id = Entry.getID(entries, next);
                pntx[id] = sd[0];
                pnty[id] = sd[1];
            }
        }        
    }

    /**
       return coordinates in 3 separate arrays 
     */
    public void getPoints(float pntx[], float pnty[], float pntz[]) {

        if(pntx.length < count){
            throw new RuntimeException("array size is too small");
        }

        for (int j = 0; j < table.length; j++) {
            int e = table[j];

            for(int next = e; next != -1; next = Entry.getNext(entries, next)) {
                Entry.getPosition(entries, next, sd);
                int id = Entry.getID(entries, next);

                pntx[id] = (float)sd[0];
                pnty[id] = (float)sd[1];
                pntz[id] = (float)sd[2];
            }
        }        
    }

    /**
       return xy coordinates in 2 separate arrays 
     */
    public void getPoints(float pntx[], float pnty[]) {

        if(pntx.length < count){
            throw new RuntimeException("array size is too small");
        }

        for (int j = 0; j < table.length; j++) {
            int e = table[j];

            for(int next = e; next != -1; next = Entry.getNext(entries, next)) {
                Entry.getPosition(entries, next, sd);
                int id = Entry.getID(entries, next);

                pntx[id] = (float)sd[0];
                pnty[id] = (float)sd[1];
            }
        }        
    }

    public int getPointCount(){
        return count;
    }
    
    //
    // this hash value is better suited to finite precision \
    // we round each coordinate to nearest int in epsilon units 
    public int calcHash(double x, double y, double z){
        //printf("xh: %4x yh: %4x \n", (int)(x/epsilon), (int)(y/epsilon));
        return 
            (((int)(z/epsilon))<<20) + 
            (((int)(y/epsilon))<<10) + 
            (((int)(x/epsilon)));
    }

    // this 
    public int _calcHash(double x, double y, double z){
        return (int)(CX*x + CY * y + CZ * z + CW);
    }


    public boolean calcEquals(double ax, double ay, double az, double bx, double by, double bz){

        double tmp = max( abs(ax-bx), abs(ay-by));
        double d = max(tmp,abs(az-bz));

        if(d <= epsilon)
            return true;
        else
            return false;

    }

    public void printHistogram() {
        printf("Calculating PointMap histogram\n");
        int max_length = 0;
        long tot_length = 0;
        int count = 0;

        int len = table.length;
        for (int j = 0; j < len; j++) {
            int e = table[j];
            if (COLLECT_STATS) count = 0;

            for(int next = e; next != -1; next = Entry.getNext(entries, next)) {
                if (COLLECT_STATS) count++;
            }
            if (COLLECT_STATS) {
                tot_length += count;
                if(count > max_length) max_length = count;
            }
        }

        int[] counts = new int[max_length+1];

        for (int j = 0; j < len; j++) {
            int e = table[j];
            if (COLLECT_STATS) count = 0;

            for(int next = e; next != -1; next = Entry.getNext(entries, next)) {
                if (COLLECT_STATS) count++;
            }
            counts[count]++;
        }

        for(int i=0; i < counts.length; i++) {
            printf("length: %3d  count: %9d  percent: %4.2f\n",i,counts[i],((float)counts[i] / len) * 100);
        }
    }

    /**
     * Increases the capacity of and internally reorganizes this
     * hashtable, in order to accommodate and access its entries more
     * efficiently.  This method is called automatically when the
     * number of keys in the hashtable exceeds this hashtable's capacity
     * and load factor.
     */
    private void rehash() {
        int[] oldTable = table;
        int oldCapacity = oldTable.length;

        int newCapacity = oldCapacity * 2;
        if(DEBUG)
            printf("*****Rehashing PointSet: new Capacity: %d  orig Capacity: %d\n",newCapacity,origCapacity);

        int[] newTable = new int[newCapacity];
        Arrays.fill(newTable,-1);

        entries.resize(newCapacity);

        transfer(newTable);
        threshold = (int) (newCapacity * loadFactor);

        table = newTable;
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    private void transfer(int[] newTable) {
        for (int j = 0; j < table.length; j++) {
            int e = table[j];

            if (e != -1) {

                int next = e;

                do {
                    e = next;
                    int hash = Entry.getHash(entries, e);
                    int index = (hash & 0x7FFFFFFF) % newTable.length;

                    next = Entry.getNext(entries, e);
                    Entry.setNext(newTable[index], entries, e);
                    newTable[index] = e;  // not next

                } while(next != -1);
            }
        }
    }

    /**
       specific data entry used by PointSet 
     */
    static class Entry extends StructDataDefinition {

        public static final StructDataDefinition DEFINITION = new Entry();
        
        public static final int DOUBLE_DATA_SIZE = 3;
        public static final int INT_DATA_SIZE = 2;
        public static final int POINTER_DATA_SIZE = 1;
        
        // double positions
        public static final int POS_X = 0;
        public static final int POS_Y = 1;
        public static final int POS_Z = 2;
        
        // int positions
        public static final int POS_ID = 0;
        public static final int POS_HASH = 1;
        
        // Pointer positions
        public static final int POS_NEXT = 0;
        
        public static int createEntry(StructMixedData dest) {
            int destIdx = dest.addItem();
            
            setNext(-1, dest, destIdx);
            
            return destIdx;
        }
        
        public static int createEntry(double x, double y, double z, int id, int hash, int next, StructMixedData dest) {
            int destIdx = dest.addItem();
            set(x,y,z,id,hash,next, dest, destIdx);
            
            return destIdx;
        }
        
        public static void set(double x, double y, double z, int id, int hash, int next, StructMixedData dest, int destIdx) {
            int int_pos = destIdx * INT_DATA_SIZE;
            int[] int_data = dest.getIntData();
            int double_pos = destIdx * DOUBLE_DATA_SIZE;
            double[] double_data = dest.getDoubleData();
            int pointer_pos = destIdx * POINTER_DATA_SIZE;
            int[] pointer_data = dest.getPointerData();
            
            double_data[double_pos + POS_X] = x;
            double_data[double_pos + POS_Y] = y;
            double_data[double_pos + POS_Z] = z;
            
            int_data[int_pos + POS_ID] = id;
            int_data[int_pos + POS_HASH] = hash;
            
            pointer_data[pointer_pos + POS_NEXT] = next;
        }
        
        public static void setHash(int hash, StructMixedData dest, int destIdx) {
            int int_pos = destIdx * INT_DATA_SIZE + POS_HASH;
            int[] int_data = dest.getIntData();
            
            int_data[int_pos] = hash;
        }
        
        public static int getHash(StructMixedData src, int srcIdx) {
            int int_pos = srcIdx * INT_DATA_SIZE + POS_HASH;
            int[] int_data = src.getIntData();
            
            return int_data[int_pos];
            
        }
        
        public static void setID(int id, StructMixedData dest, int destIdx) {
            int int_pos = destIdx * INT_DATA_SIZE + POS_ID;
            int[] int_data = dest.getIntData();
            
            int_data[int_pos] = id;
        }
        
        public static int getID(StructMixedData src, int srcIdx) {
            int int_pos = srcIdx * INT_DATA_SIZE + POS_ID;
            int[] int_data = src.getIntData();
            
            return int_data[int_pos];
            
        }
        
        public static void getPosition(StructMixedData src, int srcIdx, double[] pos) {
            int double_pos = srcIdx * DOUBLE_DATA_SIZE;
            double[] double_data = src.getDoubleData();
            
            pos[0] = double_data[double_pos +  POS_X];
            pos[1] = double_data[double_pos +  POS_Y];
            pos[2] = double_data[double_pos +  POS_Z];
        }
        
        /**
         * Get the next entry.
         *
         * @param src
         * @param srcIdx
         * @return The position or -1 if none
         */
        public static int getNext(StructMixedData src, int srcIdx) {
            int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_NEXT;
            int[] pointer_data = src.getPointerData();
            
            return pointer_data[pointer_pos];
        }
        
        public static void setNext(int next, StructMixedData dest, int destIdx) {
            int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_NEXT;
            int[] pointer_data = dest.getPointerData();
            
            pointer_data[pointer_pos] = next;
        }
        
        public int getDoubleDataSize() {
            return DOUBLE_DATA_SIZE;
        }
        
        public int getIntDataSize() {
            return INT_DATA_SIZE;
        }
        
        public int getPointerDataSize() {
            return POINTER_DATA_SIZE;
        }

    } // class Entry
 
} // class PointSet 

