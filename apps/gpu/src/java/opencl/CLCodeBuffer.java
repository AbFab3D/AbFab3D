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

import com.jogamp.opencl.CLBuffer;

import java.util.Vector;

import java.nio.ByteBuffer;


/**
   storage for generated CL code
   
   @author Vladimir Bulatov
*/
public class CLCodeBuffer {

    int code[];
    int opSize = 0;  // size of the opcodes buffer
    int opCount = 0; // opcodes count

    int dataSize = 0; // size of data buffer
    // holds data buffers
    Vector dataBuffers = new Vector();

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
        return opSize;
    }

    /**
      @return opcodes count  
     */
    public final int opcodesCount(){
        return opCount;
    }

    /**
       @return opcodes data 
     */
    public int[] getOpcodesData(){

        int data[] = new int[opSize];
        System.arraycopy(code, 0, data, 0, opSize);

        return data;        
    }
    

    /**
       @return size of dataBuffer (in bytes) 
     */
    public final int dataSize(){
        if( dataSize == 0) 
            return 1;       
        return dataSize;
        
    }   

    /**
       stores data into CL data buffer 
     */
    public final void getData(CLBuffer<ByteBuffer> dataBuffer){
        for(int i = 0; i < dataBuffers.size(); i++){
            byte[] buf = (byte[])dataBuffers.get(i);
            dataBuffer.getBuffer().put(buf);
        }
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
        reallocArray(opSize+count);
        System.arraycopy(buffer, 0, code, opSize, count);
        opSize += count;
        opCount++;
    }

    //public int getOpCount() {
    //    return opCount;
    //}

    protected void reallocArray(int newSize){
        if(newSize <= code.length)
            return;
        int newCode[] = new int[Math.max(2*code.length, newSize)];
        System.arraycopy(code, 0, newCode, 0, opSize);
        code = newCode;    
    }


    public int addData(byte buffer[]){

        int offset = dataSize;

        dataSize += buffer.length;        
        dataBuffers.add(buffer);
        
        return offset;
    }

} // class CLCodeBuffer 