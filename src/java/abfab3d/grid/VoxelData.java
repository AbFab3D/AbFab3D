/*****************************************************************************
 *                        Alan Hudson Copyright (c) 2011
 *                               Java Source
 *
 * This source is private and not licensed for any use.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid;

/**
 * Class for of VoxelData.
 *
 * @author Alan Hudson
 */
public class VoxelData {
    private byte state;
    private byte material;

    public VoxelData(byte state, byte material) {
        this.state = state;
        this.material = material;
    }

    public byte getState() {
        return state;
    }

    public byte getMaterial() {
        return material;
    }
}