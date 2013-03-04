/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
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


/**
   implemenation nof stack storage of integers 
   data values can be any integer except NO_DATA = Integer.MIN_VALUE 
   
*/
public class StackOfInt {

    public static final int NO_DATA = Integer.MIN_VALUE;

    int head; // position to write data to 
    int data[];

    public StackOfInt(int initialSize){
        data = new int[initialSize];
        head = 0;
    }
    
    public void push(int value){
        
        if(head == data.length){
            // realloc data 
            int ndata[] = new int[data.length*2];
            System.arraycopy(data, 0, ndata, 0, data.length);
            data = ndata;
        }

        data[head++] = value;
        
    }
    
    public int pop(){
        
        if(head > 0){
            return data[--head];
        } else {
            // stack is empty 
            return NO_DATA;
        }
    }

    public int getSize(){
        return head;
    }

}
