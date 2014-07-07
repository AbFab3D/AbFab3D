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

/**
 * interface to set/get integer value for 3d vector with integer components 
 *
 * it may be used to store / retrive information on a grid
 * 
 * @author Vladimir Bulatov 
 */
public interface VectorIndexer {

    /**
     * sets value at given point 
     *
     @param x
     @param y
     @param z
     @param value
     */
    public void set(int x, int y, int z, int value);

    /**
       return value stored at the given point 
       @param x
       @param y
       @param z
     */
    public int get(int x, int y, int z);

    /**
       makes empty indexer with given dimensions
     */
    public VectorIndexer createEmpty(int nx, int ny, int nz);

}