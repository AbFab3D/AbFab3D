
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

import static abfab3d.util.Output.printf;


/**
   represent variable length vector of integers
   
*/
public class VectorInt {

    int data[];
    int maxSize = 0;
    int size = 0;

    public VectorInt(int initialSize){

        data = new int[initialSize];
        maxSize = initialSize;

    }

    public int get(int index){

        return data[index];

    }

    public void add(int value){
        if(size >= maxSize){
            reallocArray();
        }
        data[size++] = value;
    }

    public int size(){
        return size;
    }

    
    public void set(int index, int value){
        if(index >= data.length){
            int newSize = index+1;
            int newdata[] = new int[newSize];
            System.arraycopy(data, 0, newdata, 0, data.length); 
            data = newdata;
            maxSize = newSize; 
            size = index+1;
        }
        data[index] = value;
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
    
    public int[] toArray(int array[]){
        if(array == null || array.length < size){
            array = new int[size];
        }
        System.arraycopy(data, 0, array, 0, size); 
        return array;
    }
    
}  // class VectorInt

