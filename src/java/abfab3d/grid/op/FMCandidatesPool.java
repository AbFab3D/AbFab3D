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

package abfab3d.grid.op;


//import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Iterator;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
   pool of candidate voxels for fast matrching algorithm 
   stores candidate voxel coordinates and value 

   allows fast access to the candidade with lowest value 
   
*/
public class FMCandidatesPool {
    
    Set buckets[];
    int maxValue;

    int minBucket;
    int maxBucket;

    public FMCandidatesPool(int maxValue, int maxIter){

        this.maxValue = maxValue;
        
        buckets = new Set[maxValue*(maxIter+2)];
        minBucket = buckets.length;
        maxBucket = -1;
    }
    
    public void printStat(){
        //printf("%s .printStat()\n", this);
        //printf(" maxValue: %d\n", maxValue);        
        int total = 0;
        for(int i = minBucket; i <= maxBucket; i++){
            Set bucket = buckets[i];
            int count = (bucket != null)? bucket.size(): 0;
            //printf(" %4d : %5d\n", i, count);
            total += count;
        }
        printf("  FMCandidates stat:  minBucket: %d maxBucket: %d coount: %d\n", minBucket,maxBucket, total);

    }

    /**
       modifies existng candidate or adds new one 
     */
    public void update(int x, int y, int z, int value){

        if(value < 0 || value > buckets.length)
            throw new IllegalArgumentException(fmt("x: %d y: %d z: %d, value: %d",x,y,z,value));

        Set bucket = buckets[value];
        if(bucket == null){
            add(x,y,z,value);
            return;
        }
        FMCandidate cand = new FMCandidate(x,y,z,value);
        if( bucket.contains(cand)){
            // remove old candidate if it exists 
            bucket.remove(cand); 
        }
        // always add new candidate 
        bucket.add(cand); 
        if(cand.value > maxBucket)
            maxBucket = cand.value;
    }

    public boolean getNext(FMCandidate candidate){

        // search for non empty bucket 
        for(int i = minBucket; i < buckets.length; i++){
            Set bucket = buckets[i];
            if(bucket != null && bucket.size() > 0){
                Iterator iter = bucket.iterator();
                FMCandidate cand = (FMCandidate)iter.next();
                iter.remove();
                candidate.set(cand);
                minBucket = i;
                return true;                                    
            }
        }
        // no candidates found 
        return false;  
        
    }

    public void add(int x, int y, int z, int value){
        
        if(value < 0 || value > buckets.length)
            throw new IllegalArgumentException(fmt("x: %d y: %d z: %d, value: %d",x,y,z,value));

        
        Set bucket = buckets[value];
        if(bucket == null){
            bucket = new LinkedHashSet();
            buckets[value] = bucket;
        }
        FMCandidate candidate = new FMCandidate(x,y,z,value);
        bucket.add(candidate);

        if(value < minBucket)
            minBucket = value;
        if(value > maxBucket)
            maxBucket = value;
           

    }

}