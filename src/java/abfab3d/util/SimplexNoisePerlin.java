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

import java.util.Random;

import static abfab3d.core.Output.printf;


// JAVA REFERENCE IMPLEMENTATION OF IMPROVED NOISE - COPYRIGHT 2002 KEN PERLIN.

public final class SimplexNoisePerlin {

    int p[] = new int[512];
    
    public SimplexNoisePerlin(int seed){
        
        init(seed);
        
    }
    
    void init(int seed){
        
        int perm[]=null;
        switch(seed){
        case 0: 
            perm = permutation_orig; 
            break;
        case 1: 
            perm = makeIdentity();
            break;
        default:
            perm = makePerm(seed);
        }
        
        copyPerm(perm, p);
        
    }
    
    /**
       return array of 256 different numbers with given seed
    */
    static int[] makePerm(int seed){
        
        int perm[] = new int[256];
        int key[] = new int[256];
        Random r = new Random(seed);
        r.nextInt(256); // skip first number. It is changes little with small changes in seed 
        int k = 0, m=0;
        while(k < 256){
            int n = r.nextInt(256);
            m++;
            if(key[n] == 0){
                // new number it is accepted 
                perm[k++] = n;
                key[n] = 1;        
            }
        }
        printf("m: %d\n\n",m);
        return perm;
    }
    
    int [] makeIdentity(){
        int perm[] = new int[256];
        for(int i = 0; i < 256; i++){
            perm[i] = i;
        }
        return perm;
    }
    
    static void copyPerm(int src[], int dest[]){
        
        for (int i=0; i < 256 ; i++) 
            dest[256+i] = dest[i] = src[i];
        
    }
    
    static final int ifloor(double x){
        if(x >= 0.0) return (int)x;
        else return (int)x -1;
    }
    
    public double noise(double x, double y, double z) {
        
        int 
            X = ifloor(x),                  // FIND UNIT CUBE THAT
            Y = ifloor(y),                  // CONTAINS POINT.
            Z = ifloor(z);
        
        x -= X;    // FIND RELATIVE X,Y,Z
        y -= Y;                                // OF POINT IN CUBE.
        z -= Z;
        X &= 0xFF;
        Y &= 0xFF;
        Z &= 0xFF;
        
        double 
            u = fade(x),                                // COMPUTE FADE CURVES
            v = fade(y),                                // FOR EACH OF X,Y,Z.
            w = fade(z);
        
        int 
            A  = p[X]+Y, 
            AA = p[A]+Z, 
            AB = p[A+1]+Z,      // HASH COORDINATES OF
            B  = p[X+1]+Y, 
            BA = p[B]+Z, 
            BB = p[B+1]+Z;      // THE 8 CUBE CORNERS,
        
        // add blended results from 8 corners of cube 
        return lerp(w, lerp(v, 
                            lerp(u, 
                                 grad(p[AA], x, y, z), 
                                 grad(p[BA], x-1, y  , z)),
                            lerp(u, 
                                 grad(p[AB], x, y-1, z),  
                                 grad(p[BB], x-1, y-1, z))),
                    lerp(v, lerp(u, 
                                 grad(p[AA+1], x  , y  , z-1 ),
                                 grad(p[BA+1], x-1, y  , z-1 )),
                         lerp(u, 
                              grad(p[AB+1], x  , y-1, z-1 ),
                              grad(p[BB+1], x-1, y-1, z-1 ))));
    }
    
    static double fade(double t) { 
        return t * t * t * (t * (t * 6 - 15) + 10); 
    }
    
    static double lerp(double t, double a, double b) { 
        return a + t * (b - a); 
    }
    
    public double turbulence(double x, double y, double z, int octaves, double persistence) {
        
        double total = 0;
        double freq = 1;
        double amp = 1;
        
        for(int i  = 0; i < octaves; i++){
            
            total += noise(x * freq, y * freq, z * freq) * amp;
            freq = freq*2;
            amp *= persistence;
        }
        
        return total;
        
    }
    
    /**
       gradient is not random.
       it is one of 12 directions from center to the edges of the cube
       (1,1,0),(-1,1,0),(1,-1,0),(-1,-1,0),  
       (1,0,1),(-1,0,1),(1,0,-1),(-1,0,-1),  
       (0,1,1),(0,-1,1),(0,1,-1),(0,-1,-1) 
    */
    static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;                      // CONVERT LO 4 BITS OF HASH CODE
        double u = h<8 ? x : y;                 // INTO 12 GRADIENT DIRECTIONS.
        double v = h<4 ? y : h==12||h==14 ? x : z;
        
        return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
    }
    
    static final int  permutation_orig[] = { 
        151,160,137,91,90,15,
        131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
        190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
        88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
        77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
        102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
        135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
        5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
        223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
        129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
        251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
        49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
        138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
    };        
}
