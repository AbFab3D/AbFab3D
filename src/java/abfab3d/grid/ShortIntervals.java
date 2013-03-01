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

package abfab3d.grid;

import static abfab3d.util.Output.printf;


/**
   class implements array of shorts encoded as ordered set of intervals 
   
   low bytes of each int  number encode start of the interval 
   hi byte of each int number encode content (material) of the interval 
     
   000000011100001100000
          p  q   r s

   sequential numbers give start of interval of ON/OFF bits
   even number mark start of ON sequence
   off numbers mark start of OFF sequence 


   @author Vladimir Bulatov
   
 */
public class ShortIntervals implements RowOfInt {

    public static final int MASK  = 0xffff;
    public static final int SHIFT = 16;

    static final int MIN_VALUE = makeCode(Short.MIN_VALUE, 0);
    static final int MAX_VALUE = makeCode(Short.MAX_VALUE, 0);
    
    int m_intervals[];

    int m_curcount = 0; // current count of inteval starts 

    /**
       copy constructor 
     */
    public ShortIntervals(ShortIntervals si){

        m_curcount = si.m_curcount;
        m_intervals = si.m_intervals.clone();

    }

    public ShortIntervals(int intervals[]){

        m_intervals = intervals.clone();
        m_curcount = m_intervals.length;
    }

    public ShortIntervals(){
        
        m_intervals = new int[4];
        m_intervals[0] = MIN_VALUE;//0;//makeCode(0, 0);
        m_intervals[1] = MAX_VALUE;//makeCode(Short.MAX_VALUE, 0);
        m_curcount = 2;
        
    }

    /**
       encode interval and material into int 
     */
    static final int makeCode(int start, int data){
        return (start & MASK) | ((data & MASK) << SHIFT);
    }
    /**
       decode material 
     */
    static final int getData(int code){
        return (code >> SHIFT) & MASK;
    }
    /**
       decode interval
    */
    static final int getStart(int code){
        return (int)((short)(code & MASK));
    }

    /**

     */
    public int get(int x){
        //find interval which contains x 
        //
        //0000000aaabbbbccdddddef000
        //       p  q   r s    vw
        // 
        if(m_curcount <= 2) // no intervals exist (all bits are 0s) 
            return 0;

        for(int i = 1; i < m_curcount; i++){
            
            if(getStart(m_intervals[i]) > x){
                // found interval 
                if(i > 0){ // this is not the first interval 
                    // return data of previous interval
                    return getData(m_intervals[i-1]); 
                } else {
                    // we are at the beginning. First semi-interval is always 0s
                    return 0;
                }
            }
        }
        // x is larger than the last interval 
        // we assume, that material of last semi-interval is 0s
        return 0;
        
    }

    public synchronized void set(int x, int material){
        //if(true) return;
        //printX(x, material);

        if(m_curcount == 0) { // no interval exist (all vaues are 0s)             

            if(material == 0){  // nothing to do                 
                return;
            }
            
            if(2 >  m_intervals.length) 
                reallocArray(2);
            
            m_intervals[0] = makeCode(x,  material);
            m_intervals[1] = makeCode(x+1,  0);
            m_curcount = 2;
            return;
        }
        
        for(int i = 1; i < m_curcount; i++){

            //    000aaaaabbbb000
            //            ^         
            int code_b = m_intervals[i];
            int start_b = getStart(code_b);

            //            
            if(start_b == x){ // new x is at the begining of the existing interval 
                //printf("(start == x)\n");
                int mat_b = getData(code_b);
                if(mat_b == material){
                    //    000aaaaabbbb000
                    //            b                                            
                    // old material is the same as new -> nothing to do 
                    return;
                } else {
                    // new material is different 
                    // possible cases 
                    if(getData(m_intervals[i-1]) == material){
                        //  new material is the same as material of previous interval 
                        //     just increment start of the current interval 
                        //
                        //    000aaaaabbbb000
                        //            a                        
                        // increment current interval 
                        m_intervals[i] = makeCode(start_b + 1, mat_b);
                        if((i+1) < m_curcount && (start_b+1) == getStart(m_intervals[i+1])){
                            removePoint(i);
                        }
                        return;
                    } else {                        
                        if(getStart(m_intervals[i+1]) == (x+1)){
                            //    00aaabff000
                            //         c    
                            m_intervals[i] = makeCode(start_b, material);                            
                            if(getData(m_intervals[i+1]) == material){
                                //    00aaabcc00
                                //         c    
                               removePoint(i+1);                         
                            }      
                            return;
                        } else {
                            //    00aaabbbb000
                            //         c    
                            //    00aaacbbb000
                            //    new material is different from previous interval 
                            //    in this case we change material of current interval to new material 
                            //    and insert new interval of old material at the (x+1) 
                            //
                            m_intervals[i] = makeCode(x, material);                                                        
                            insertPointAfter(i, makeCode(x + 1, mat_b));
                            return;
                        }                        
                    }
                } 
            } else if(start_b > x) {  // x is inside or on right end of existing interval

                int mat_b = getData(code_b);
                int code_a = m_intervals[i-1];
                int mat_a = getData(code_a);
                int start_a = getStart(code_a);                
                if(mat_a == material){ // material is the same 
                    // 000aaabbb000
                    //     a
                    // nothing to do 
                    return;

                } else {                    
                    if(start_b == (x+1)){
                        // x is at the right end of existing interval
                        if(mat_b == material){  // next interval has the same meterial -> move left start of next interval 
                            // 000abbb000 such case is not possible, because it would be handled by if(x = start_b) 
                            //    b  
                            // 
                            // 000aaabbb000
                            //      b
                            // 000aabbbb000
                            m_intervals[i] = makeCode(x, material);
                            return;                            
                        } else {
                            // 000aaabbb000
                            //      c  
                            // 000aacbbb000
                            insertPointAfter(i-1, makeCode(x, material)); // insert new start at x
                            return;
                        }
                    } else {
                        // 000aaabbb000
                        //     c 
                        insertIntervalAfter(i-1, makeCode(x, material),makeCode(x+1, mat_a)); // insert new interval
                        return;                        
                    }                    
                }                
            }            
        }
        //printf("x: %d material: %d\n", x, material);
        //
        // no interval was found -> we are larger then the largest interval -> it is 0          
        //
        if(material != 0) {
            // add insert new interval 
            if(m_curcount + 2 >  m_intervals.length) reallocArray(m_curcount+2);
            m_intervals[m_curcount++] = makeCode(x, material);
            m_intervals[m_curcount++] = makeCode((x+1), 0);        
        }
        return;
        
    }


    /**
       inserts single pont of data after ind       
       
     */
    void insertPointAfter(int ind, int code){

        if(m_curcount+1 > m_intervals.length) reallocArray(m_curcount+1);

        for(int i = m_curcount-1; i >= ind; i--){
            m_intervals[i+1] = m_intervals[i];
        }
        m_intervals[ind+1] = code;
        m_curcount ++;

    }

    /**
       inserts single pont of data after ind       
       
     */
    void insertIntervalAfter(int ind, int code1, int code2){

        if(m_curcount+2 > m_intervals.length) reallocArray(m_curcount+2);

        for(int i = m_curcount-1; i >= ind; i--){
            m_intervals[i+2] = m_intervals[i];
        }
        m_intervals[ind+1] = code1;
        m_intervals[ind+2] = code2;
        m_curcount += 2;

    }

    /**
       removes point at ind 
     */
    void removePoint(int ind){

        for(int i = ind; i < m_curcount-1; i++){
            m_intervals[i] = m_intervals[i+1];
        }
        m_curcount--;
    }


    /**
       inserts single bit interval (x, x+1) at the position ind
     */
    void _insertInterval(int ind, int x){
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
    void _collapseInterval(int i){

        m_curcount -= 2; 
        for(int k = i; k < m_curcount; k++){
            m_intervals[k] = m_intervals[k+2];
        }
    }


    void reallocArray(int newSize){
        
        newSize += 2; // alocate a little more to reduce reallocations
        int d[]= new int[newSize];
        int len = m_intervals.length;
        if(newSize < len)            
            len = newSize;
        System.arraycopy(m_intervals, 0, d, 0, len);
        m_intervals = d;

    }

    public void dump(){
        // we always have first point 0 
        for(int i = 0; i < m_curcount; i++){
            printf("%d %d, ",getStart( m_intervals[i]),getData( m_intervals[i])); 
        }

        printf("\n");
    }

    
    public void print(int start, int end){
        
        for(int i = start; i < end; i++){
            printf("%d",get(i));
        }
        printf("( ");
        for(int i = 1; i < m_curcount-1; i++){ // skip first and last entry, which are default  
            
            printf("%d %d, ",getStart( m_intervals[i]),getData( m_intervals[i])); 
        }        
        printf(")\n");
        
    }

    public Object clone(){

        return new ShortIntervals(this);

    }   

    /**
       removes all data, but keeps the memory 
     */
    public void clear(){

        m_intervals[0] = MIN_VALUE;//0;//makeCode(-1, 0);
        m_intervals[1] = MAX_VALUE;//makeCode(Short.MAX_VALUE, 0);
        m_curcount = 2;

    }

    public void release(){

        m_intervals = null;
        m_curcount = 0;
        
    }

    /**
       compare two arrays
     */
    public int compare(RowOfInt ai, int start, int end){

        for(int i =start; i < end; i++){
            int d = get(i) - ai.get(i);
            if(d != 0)
                return d;
        }
        return 0;
    }

    public static void printX(int x, int value){
        
        for(int k = 0; k < x; k++){
            printf(" ");
        }
        printf("%d\n",value);
    }

    /**
     * Traverse a class of voxels types.  May be much faster then scanning all voxels
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */    
    public boolean findInterruptible(int data, IntervalTraverser t) {  
        
        if(data != 0){
            
            // scan interval of data 
            for(int i = 1; i < m_curcount-1; i++){

                int code = m_intervals[i];
                int curData = getData(code);
                
                if(curData != 0){
                    //if(data == curData){
                    
                    int start = getStart(code);
                    int end = getStart(m_intervals[i+1]);
                    for(int x = start; x < end; x++){
                        if (!t.foundInterruptible(x,data)){
                            return false;        
                        }        
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

        // we have to transform intervals of values into codes
        // we assume for now, that values are 1s and 0s, as it should be ideally

        m_intervals = new int[count + 2];
        
        m_curcount = m_intervals.length;
        // first and last points are special 
        m_intervals[0] = makeCode(Short.MIN_VALUE, 0);
        m_intervals[m_curcount-1] = makeCode(Short.MAX_VALUE, 0);

        for(int i = 0; i < count; i++){
            m_intervals[i+1] = makeCode(intervals[i],values[i]); 
        }
        
    }

    public int getDataMemory(){
        return 4*m_intervals.length;
    }

}
