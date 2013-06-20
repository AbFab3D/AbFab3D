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

public interface GridBit {

    public long get(int x, int y, int z);
    public void set(int x, int y, int z, long value);
    
    public void clear();// set all data to 0 (but doesn't releases allocated memory)
    public void release(); // releases all the memory 
    
}
