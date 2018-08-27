
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

package abfab3d.grid;

import java.util.Arrays;

import static abfab3d.core.Output.printf;


/**
   represent variable length array of int 

   @author Vladimir Bulatov
*/
//public class ArrayInt implements RowOfInt {
    public class ArrayInt {

    int data[];
    int maxSize = 0;
    int size = 0;

    public ArrayInt(int initialSize){

        data = new int[initialSize];
        maxSize = initialSize;

    }

    public ArrayInt(ArrayInt arr){

        data = arr.data.clone();
        maxSize = arr.maxSize;
        size = arr.size();

    }

    public int get(int index){

        return data[index];

    }

    public long getLast(){

        if(size > 0)
            return data[size-1];
        else 
            return Integer.MIN_VALUE;
    }

    public boolean hasLast(){
        return (size > 0);
    }

    public void removeLast(){
        if(size > 0)
            size--;
    }

    /**
       add 3 values at once 
     */
    public void add(int x, int y, int z){
        
        if((size+2) >= maxSize){
            reallocArray();
        }
        data[size++] = x;
        data[size++] = y;
        data[size++] = z;
        
    }
    
    public void add(int value){
        if(size >= maxSize){
            reallocArray();
        }
        data[size++] = value;
    }


    public void add(ArrayInt array){
        int s = array.size();
        for(int i = 0; i < s; i++){
            add((int)array.get(i));
        }
    }

    public int size(){
        return size;
    }

    /**
       sorts array and removes duplicates 
     */
    public void sortAndRemoveDuplicates(){
        
        Arrays.sort(data, 0, size);

        int srcIndex = 0;
        int destIndex = 0;

        while( srcIndex < size){            
            int lastData = data[srcIndex];
            data[destIndex] = lastData;
            srcIndex++;
            destIndex++;
            while(srcIndex < size && data[srcIndex] == lastData){
                srcIndex++;
            }
        }
        
//        size = destIndex+1;
        size = destIndex;  // TODO:  DestIndex already incremented above

    }

    
    public void set(int index, long value){
        if(index >= data.length){
            int newSize = index+1;
            int newdata[] = new int[newSize];
            System.arraycopy(data, 0, newdata, 0, data.length); 
            data = newdata;
            maxSize = newSize; 
            size = index+1;
        }
        data[index] = (int)value;
    }

    protected void reallocArray(){
        if(maxSize < 2) maxSize = 2;
        int newSize = maxSize*2;
        int newdata[] = new int[newSize];
        
        if(data != null)
            System.arraycopy(data, 0, newdata, 0, data.length);
        data = newdata;
        maxSize = newSize;
        //printf("realloc array: %d\n", newSize);
    }                


    public void print(int start, int end){
        for(int i = start; i < end; i++){
            printf("%1d", data[i]);
        }
        printf("\n");
    }

    public void clear(){
        size = 0;
    }

    public void release(){
        
        data = null;
        maxSize = 0;
        size = 0;
    }

    public Object clone(){

        return new ArrayInt(this);

    }

    public boolean findInterruptible(int value, IntervalTraverser traverser){
        
        for(int i =0; i < size; i++)
            if(data[i] == value){
                if (!traverser.foundInterruptible(i,value)){
                    return false;        
                }                        
            }                       
        return true;
    }

    public void find(int value, IntervalTraverser traverser){

        for(int i =0; i < size; i++) {
            if(data[i] == value){
                traverser.found(i,value);
            }
        }
    }

    public int[] toArray(int array[]){
        if(array == null || array.length < size){
            array = new int[size];
        }
        System.arraycopy(data, 0, array, 0, size); 
        return array;
    }

    /**
       set pixels to values of given intervals 
     */
    public void setIntervals(int intervals[], int values[], int count){
        //TODO 
        // we never use it actually

        throw new IllegalArgumentException("Not implemented");
    }

    /**
     set pixels to values of given intervals
     */
    public void setIntervals(int intervals[], long values[], int count){
        //TODO
        // we never use it actually

        throw new IllegalArgumentException("Not implemented");
    }

    public int getDataMemory(){
        return 4*data.length;
    }
    
}  // class ArrayInt3

