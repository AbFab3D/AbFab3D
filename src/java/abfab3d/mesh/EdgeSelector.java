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
package abfab3d.mesh;

import java.util.Random;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;


/**
   
   array of edges 
   allocated once 
   used edges can be removed from array 
   can return random non null element
   
*/
public class EdgeSelector {
    /** Should we collect stats information */
    private static final boolean STATS = false;

    public static final int NO_DATA = Integer.MIN_VALUE;

    int array[];
    int asize = 0; //
    //int count = 0; // count of non nul elements 
    
    //
    // random number generator with specified seed 
    //
    protected Random m_rnd;
    protected long seed;

    // count of no empty edges 
    protected int dataCount = 0;
    // count of calls rnd.nextInt();
    protected int countCount = 0;
    // count of calls getRandomEdge
    protected int callCount = 0;
    
    public EdgeSelector(int array[], long seed){        
        
        this(array, array.length,seed);         
    }

    public EdgeSelector(int array[], int count, long seed){        
        
        this.array = array;
        
        this.seed = seed;
        
        asize = count;
        dataCount = count;
        
        m_rnd = new Random(seed);
        
        //printf("new %s (seed: 0x%x)\n", this, seed);
        //count = 0;        
    }

    public int getDataCount(){
        return dataCount;
    }

    public int get(int i){
        return array[i];
    }
    
    public void set(int i, int value){
        
        int oldValue = array[i];
        array[i] = value;        
        //printf("edgesArray.set(%d, %s)\n", i, value);        
        if(value == NO_DATA && oldValue != NO_DATA){
            dataCount--;
        } else if(value != NO_DATA && oldValue == NO_DATA){
            dataCount++;
        }
        
    }
    
    public boolean getRandomEdge(EdgeData ed){
        
        ed.edge = NO_DATA;
        
        int count = 0;
        
        while(count++ < 100){

            int i = m_rnd.nextInt(asize);
            
            if(array[i] != NO_DATA){
                ed.edge = array[i];
                ed.index = i;

                if (STATS) {
                    countCount += count;
                    callCount++;
                }
                return true;
            }                
        }

        return false;

    }    

    public void printStat(){

        printf("%s.printStat()\n", this);
        printf("  call count: %d\n", callCount);
        printf("  average count: %5.1f\n", ((double)countCount / callCount));
        
    }

}
