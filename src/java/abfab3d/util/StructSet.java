package abfab3d.util;

import abfab3d.util.StructDataDefinition;
import abfab3d.util.StructMixedData;

import java.util.Arrays;

/**
 * A set interface for struct objects.
 *
 * @author Alan Hudson
 */
public class StructSet {
    static final int DEFAULT_INITIAL_CAPACITY = 32;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /** References to Entry positions or -1 if not filled */
    private int[] table;

    /** The total number of entries in the hash table. */
    private int count;
    private int threshold;
    private float loadFactor;
    private int initialCapacity;

    private StructMixedData entries;
    private HashFunction hasher;
    private StructMixedData src;

    public StructSet(HashFunction hasher) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, null, hasher);
    }

    public StructSet(StructMixedData src, HashFunction hasher) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, src, hasher);
    }

    public StructSet(int initialCapacity, float loadFactor, StructMixedData src, HashFunction hasher) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                    initialCapacity);
        if (loadFactor <= 0)
            throw new IllegalArgumentException("Illegal Load: "+loadFactor);

        if (initialCapacity == 0)
            initialCapacity = 1;

        this.initialCapacity = initialCapacity;
        this.loadFactor = loadFactor;
        this.hasher = hasher;
        this.src = src;

        table = new int[initialCapacity];
        Arrays.fill(table, -1);

        threshold = (int)(initialCapacity * loadFactor);
        entries = new StructMixedData(SetEntry.DEFINITION,initialCapacity);
    }

    /**
     * Add a new entry.
     *
     * @return The positions old ID or a newly created ID
     */
    public boolean add(int key) {
        int hash = hasher.calcHashCode(src, key);

        int index = (hash & 0x7FFFFFFF) % table.length;


        for(int next = table[index]; next != -1; next = SetEntry.getNext(entries, next)) {
            if (SetEntry.getHash(entries, next) == hash) {

                int nkey = SetEntry.getKey(entries,next);
                if (hasher.calcEquals(src, key, nkey)) {
                    return false;
                }
            }
        }

        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash();

            index = (hash & 0x7FFFFFFF) % table.length;
        }

        // Creates the new entry.
        int e = SetEntry.createEntry(key,hash,table[index], entries);

        table[index] = e;
        count++;

        return true;
    }

    public boolean contains(int key) {
        int hash = hasher.calcHashCode(src,key);

        int index = (hash & 0x7FFFFFFF) % table.length;

        for(int next = table[index]; next != -1; next = SetEntry.getNext(entries, next)) {
            if (SetEntry.getHash(entries, next) == hash) {
                int nkey = SetEntry.getKey(entries,next);
                if (hasher.calcEquals(src,key,nkey)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get all the values
     *
     * @return
     */
    public int[] keys() {
        int[] ret_val = new int[count];
        int idx = 0;

        for (int j = 0; j < table.length; j++) {
            int e = table[j];

            for(int next = e; next != -1; next = SetEntry.getNext(entries, next)) {
                int value = SetEntry.getKey(entries, next);
                ret_val[idx++] = value;
            }
        }

        return ret_val;
    }


    public void clear() {
        Arrays.fill(table, -1);
        SetEntry.clear(entries);

        count = 0;
    }

    public int size() {
        return count;
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
        //System.out.println("*****Rehashing StructSet: " + newCapacity);
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
                    int key = SetEntry.getKey(entries, e);
                    int hash = hasher.calcHashCode(src, key);
                    int index = (hash & 0x7FFFFFFF) % newTable.length;

                    next = SetEntry.getNext(entries, e);
                    SetEntry.setNext(newTable[index], entries, e);
                    newTable[index] = e;  // not next

                } while(next != -1);
            }
        }
    }

    static class SetEntry extends StructDataDefinition {

        public static final StructDataDefinition DEFINITION = new SetEntry();
        
        public static final int INT_DATA_SIZE = 1;
        public static final int POINTER_DATA_SIZE = 2;
        
        // int positions
        public static final int POS_HASH = 0;
        
        // pointer positions
        public static final int POS_KEY = 0;
        public static final int POS_NEXT = 1;
        
        
        public static int createEntry(StructMixedData dest) {
            int destIdx = dest.addItem();
            
            return destIdx;
        }
        
        public static int createEntry(int key, int hash, int next, StructMixedData dest) {
            int destIdx = dest.addItem();
            set(key,hash, next, dest, destIdx);
            
            return destIdx;
        }
        
        public static void set(int key,int hash, int next, StructMixedData dest, int destIdx) {
            int int_pos = destIdx * INT_DATA_SIZE;
            int[] int_data = dest.getIntData();
            int pointer_pos = destIdx * POINTER_DATA_SIZE;
            int[] pointer_data = dest.getPointerData();
            
            int_data[int_pos + POS_HASH] = hash;
            
            pointer_data[pointer_pos + POS_KEY] = key;
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
        
        public static void setKey(int id, StructMixedData dest, int destIdx) {
            int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_KEY;
            int[] pointer_data = dest.getPointerData();
            
            pointer_data[pointer_pos] = id;
        }
        
        public static int getKey(StructMixedData src, int srcIdx) {
            int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_KEY;
            int[] pointer_data = src.getPointerData();
            
            return pointer_data[pointer_pos];
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
        
        /**
         * Clear all entries.  Do not reallocate but make all entries back to default values.
         *
         * @param dest
         */
        public static void clear(StructMixedData dest) {
            dest.clear();
        }
        
        @Override
            public int getPointerDataSize() {
            return POINTER_DATA_SIZE;
        }
        
        @Override
            public int getIntDataSize() {
            return INT_DATA_SIZE;
        }
    } // class SetEntry 
    

}

