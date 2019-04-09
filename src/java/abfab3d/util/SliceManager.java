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

import java.util.concurrent.atomic.AtomicInteger;


/**
   handles slices for MT processing 
*/
public class SliceManager {
    
    Slice slices[];
    int scount;
    int nextSliceIndex=0;

    // padding at the end of slices array to be filled with null 
    // used as stopping flag 
    int padding = 40;

    AtomicInteger aSliceIndex = new AtomicInteger(0);

    public SliceManager(int gridWidth){
        this(gridWidth,1);
    }

    public SliceManager(int gridWidth, int sliceWidth){
        
        scount = (gridWidth + sliceWidth-1)/sliceWidth;
        slices = new Slice[scount+padding];
        
        for(int k = 0; k < scount; k++){
            int smin = k*sliceWidth;
            int smax = smin + sliceWidth;
            if(smax > gridWidth) smax = gridWidth;                    
            slices[k] = new Slice(smin, smax);
        }

    }
    
    public int getSliceCount(){
        return scount;
    }

    public Slice getSlice(int index){
        return slices[index];
    }
    
    public Slice getNextSlice(){

        return slices[aSliceIndex.getAndIncrement()];

    }

    public synchronized Slice _getNextSlice(){

        if(nextSliceIndex < scount){
            return slices[nextSliceIndex++];
        } else {
            return null;
        }
    }
}

