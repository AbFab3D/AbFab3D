
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

import java.io.Serializable;

import static abfab3d.util.Output.printf;


/**
   class implements bit array encoded as ordered set of intervals 

   000000011100001100000
          p  q   r s

   sequential numbers give start of interval of ON/OFF bits
   even number mark start of ON sequence
   off numbers mark start of OFF sequence 


   @author Vladimir Bulatov
   
 */
public class BitIntervalInts implements RowOfInt, Serializable {
    
    int m_intervals[];

    int m_curcount = 0; // current count of inteval starts 

    public BitIntervalInts(){
        
        m_curcount = 0;
        m_intervals = new int[2];
        //m_intervals = new int[]{7, 10, 11,12, 17,21, 22, 23, 25, 30};m_curcount = m_intervals.length;
        //m_intervals = new int[]{1,2,3,38};m_curcount = m_intervals.length;
        //m_intervals = new int[]{-1,39};m_curcount = m_intervals.length;
        
    }

    /**
       copy constructor 
     */
    public BitIntervalInts(BitIntervalInts si){

        m_curcount = si.m_curcount;
        m_intervals = si.m_intervals.clone();

    }

    /**

     */
    public int get(int x){

        //find interval which contains x 
        //
        //00000001110000110000010000
        //       p  q   r s    vw
        // 
        //00000001110000110000011111
        //       p  q   r s    v    w  interval count is ALWAYS even 
        if(m_curcount == 0) // no interval exist (all bits are 0s) 
            return 0;

        for(int i = 0; i < m_curcount; i++){

            if(m_intervals[i] > x){

                if((i & 1) == 1) // odd number - right end of interval, we are inside of 1s
                    return 1;
                else             // we are inside of 0s 
                    return 0;
            }
        }
        // no interval was found -> we are larger then largest interval -> it is 0  
        
        return 0;

    }

    public synchronized void set(int x, int value){
        
        if(m_curcount == 0) {
            // no interval exist (all bits are 0s) 
            if(value != 0) { // set 1 
                if(2 >  m_intervals.length) reallocArray(2);
                m_intervals[0] = (int)x;
                m_intervals[1] = (int)(x+1);
                m_curcount = 2;
                return;
            } else {
                // set 0 - do nothing 
                return;
            }
        }

        for(int i = 0; i < m_curcount; i++){

            int v = m_intervals[i];

            if(v == x){
                if((i & 1) == 1){
                    //  00011100000
                    //        x    
                    if( value != 0){
                        //  00011100000
                        //        1      
                        m_intervals[i]++; //move interval i
                        if(m_curcount > (i+1) && (m_intervals[i+1] == (x+1))){ // check interval disappearance 
                            // 00011101111
                            //       1 
                            // we have to remove (i) and (i+1)
                            collapseInterval(i);
                            return;
                        } else {
                            return;
                        }
                    } else {  // set 0 
                        //  00011100000
                        //        0
                        // nothing to do 
                        return;
                    }
                } else {
                    //  00011100000
                    //     x       
                    if(value != 0){
                        //  00011100000
                        //     1                               
                        // nothing to do 
                        return;
                    } else {
                        //  00011100000
                        //     0
                        m_intervals[i]++;
                        if(m_curcount > (i+1) && m_intervals[i] == m_intervals[i+1]){
                        //  0001001100000
                        //     0
                            collapseInterval(i);
                            return;
                        } else {
                            return;
                        }
                    }
                }
                
            } else if(v > x){
                if((i & 1) == 1){ // we are inside of 1s
                    // 00000011110000
                    //         x    
                    if(value != 0){
                        // 1 inside of 1s - nothing to do 
                        // 00000011110000
                        //         1    
                        // nothing to do 
                        return;
                    } else {  // bit 0
                        // 00000011110000
                        //         0   
                        if(x == (v-1)){
                            // 00000011110000
                            //          0   
                            // move interval 
                            m_intervals[i]--;
                            return;
                        } else {
                            // 00000011110000
                            //        0   
                            //insert new interval 
                            insertInterval(i,x);
                            return;
                        }
                    }
                } else {// we are inside of 0s
                    // 0000011000110000
                    //         x 
                    if(value != 0) {
                        if(x == (v-1)){
                            // 0000011000110000
                            //          1
                            m_intervals[i]--;
                            return;
                        } else {
                            // 0011110000111
                            //        1   
                            //insert new interval 
                            insertInterval(i,x);
                            return;                            
                        }                        
                    } else {
                        // 0 bit inside of 0s interval - nothing to do 
                        return;
                    }
                }                
            }
        }

        //
        // no interval was found -> we are larger then the largest interval -> it is 0          
        //
        if(value != 0) {
            // only need new interval if 1
            if(m_curcount + 2 >  m_intervals.length) reallocArray(m_curcount+2);
            m_intervals[m_curcount++] = (int)x;
            m_intervals[m_curcount++] = (int)(x+1);
        }
        return;
        
    }

    /**
       inserts single bit interval (x, x+1) at the position ind
     */
    void insertInterval(int ind, int x){
        if(m_curcount+2 > m_intervals.length) reallocArray(m_curcount+2);

        for(int i = m_curcount-1; i >= ind; i--){
            m_intervals[i+2] = m_intervals[i];
        }
        m_intervals[ind] = (int)x;
        m_intervals[ind+1] = (int)(x+1);

        m_curcount += 2;

    }

    /**
       removes interval (i, i+1)
     */
    void collapseInterval(int i){

        m_curcount -= 2; 
        for(int k = i; k < m_curcount; k++){
            m_intervals[k] = m_intervals[k+2];
        }
    }


    void reallocArray(int newSize){

        int d[]= new int[newSize];
        int len = m_intervals.length;
        if(newSize < len)            
            len = newSize;
        System.arraycopy(m_intervals, 0, d, 0, len);
        m_intervals = d;

    }

    void dump(){
        for(int i = 0; i < m_curcount; i++){
            printf("%2d ", m_intervals[i]);            
        }
        printf("\n");
    }
    
    void print(int start, int end){
        
        for(int i = start; i < end; i++){
            printf("%d", get(i));
        }
        printf("\n");
        
    }

    public void clear(){
        m_curcount = 0;
    }

    public void release(){
        m_intervals = null;
        m_curcount = 0;
    }

    public Object clone(){
        return new BitIntervalInts(this);
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */    
    public boolean findInterruptible(int data, IntervalTraverser t) {  
        
        if(data != 0){
            // scan interval of 1s 
            for(int i = 0; i < m_curcount; i+=2){
                // cycle over all filled bits 
                for(int x = m_intervals[i];x < m_intervals[i+1]; x++){
                    if (!t.foundInterruptible(x,1)){
                        return false;        
                    }        
                }
            }
            return true;
            
        } else {
            
            // scan interval of 0s 
            // TODO  do we need this ? 
            return false;
        }
    }

    /**
       set pixels to values of given intervals 
     */
    public void setIntervals(int intervals[], int values[], int count){

        // we have to transform intervals of values into intervals of 0s and 1s. 
        // we assume for now, that values are 1s and 0s, as it should be ideally

        m_intervals = new int[count];

        m_curcount = m_intervals.length;

        for(int i =0; i < count; i++){
            m_intervals[i] = (int)intervals[i];
        }

    }

    public int compareIntevals(BitIntervals bi){
        int d = m_intervals.length - bi.m_intervals.length;
        if(d != 0)
            return d;

        for(int i = 0; i < m_intervals.length; i++){
            
            int diff = m_intervals[i] - bi.m_intervals[i];
            if(diff != 0){
                return diff;
            }
        }
        return 0;
    }

    public int getDataMemory(){
        return 2*m_intervals.length;
    }


}
