/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
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

// External Imports


import static abfab3d.core.Output.fmt;

/**
 * implementation of VectorIndexer as uncompressed array of int
 *
 * 
 * @author Vladimir Bulatov 
 */
public class VectorIndexerArray implements VectorIndexer {


    int nx, ny, nz; // dimensions of 3D grid 
    int nxz;

    int data[];

    /**
       creates 3d array of given sizes 
     */
    public VectorIndexerArray(int nx, int ny, int nz){

        if( ((long) nx) * ny * nz > (long)Integer.MAX_VALUE){
            throw new RuntimeException(fmt("array size [%d x %d %d ] exceeds max size",nx,ny,nz));
        }
        
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;

        this.nxz = nx*nz;
        data = new int[nx*ny*nz];

    }

    /**
       @override
     */
    public final void set(int x, int y, int z, int value){
        data[z + x*nz + y*nxz] = value;
    }

    /**
       @override
     */
    public final int get(int x, int y, int z){
        return data[z + x*nz + y*nxz];
    }

    public VectorIndexer createEmpty(int nx, int ny, int nz){
        return new VectorIndexerArray(nx, ny, nz);
    }


}