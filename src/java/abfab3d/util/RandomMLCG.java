/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2020
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
  generates sequencxe of preudo random numbers using Multiplicative Linear Congruental Generator 
  Generator is defined by 2 numbers. 
  
  X_(n+1) = (X_n * a) % m; 
  
  period of sequence is m-1
  tables of good numbers can be taken from 
  Tables of linear congruential generators of different sizes and good lattice structure
  PIERRE L'ECUYER 
  in Mathematics of Computation 68(225):249-260 · January 1999 
  
*/  
public class RandomMLCG {
  
    // examples of parameters wityh given number of bits 
    
    public static final long PARAM_8[]= new long[] {251, 33};
    public static final long PARAM_12[]= new long[] {509, 160};
    public static final long PARAM_14[]= new long[] {16381, 572};
    public static final long PARAM_22[]= new long[] {4194301, 1406151};
    public static final long PARAM_30[]= new long[]{1073741789, 771645345};
    public static final long PARAM_32[]= new long[]{4294967291L, 1588635695L};

    long seed, a, m;

    public RandomMLCG(long params[], int seed){
        
        this(params[0], params[1], seed);
        
    }

    public RandomMLCG(long m, long a, long seed){
        
        this.seed = seed;
        this.m = m;
        this.a = a;
        
    }
    
    public int nextInt(){
        
        this.seed *= this.a; 
        this.seed %= this.m; 
        return (int)this.seed;    
    }
    
}
