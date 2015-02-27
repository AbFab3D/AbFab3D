/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package opencl;

/**
   storage for generated CL code
   
   @author Vladimir Bulatov
*/
public class CLCodeBuffer {

    int code[];
    int size = 0;  // size of the buffer
    int opCount = 0; // opcodes count
    int dataBufferSize = 0; // size of data buffer 
    
    /**
       constructor
       @param initialCapacity initial capacity of the buffer
     */
    public CLCodeBuffer(int initialCapacity){
        code = new int[initialCapacity];
    }
    
    /**
       return size of opcodes buffer in words)
     */
    public final int opcodesSize(){
        return size;
    }

    /**
      @return opcodes count  
     */
    public final int opcodesCount(){
        return opCount;
    }

    /**
       @return size of dataBuffer (in bytes) 
     */
    public final int dataSize(){
        return dataBufferSize;
    }   


    /**
       @return opcodes data 
     */
    public int[] getOpcodesData(){

        int data[] = new int[size];
        System.arraycopy(code, 0, data, 0, size);

        return data;        
    }
    
    public final int get(int index){
        return code[index];
    }

    /**
       add single word of code 
     */
    /**
    public void add(int word){
        reallocArray(size+1);
        code[size] = word;
        size++;
   }
    */

    /**
       add code from array 
       @param buffer with code 
       @param count of words to add 
       
     */
    public void add(int buffer[], int count){
        reallocArray(size+count);
        System.arraycopy(buffer, 0, code, size, count);
        size += count;
        opCount++;
    }

    //public int getOpCount() {
    //    return opCount;
    //}

    protected void reallocArray(int newSize){
        if(newSize <= code.length)
            return;
        int newCode[] = new int[Math.max(2*code.length, newSize)];
        System.arraycopy(code, 0, newCode, 0, size);
        code = newCode;    
    }

} // class CLCodeBuffer 