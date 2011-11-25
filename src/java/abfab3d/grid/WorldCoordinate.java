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

/**
 * World coordinate holder.
 *
 * Typically stored in a Java collection object.
 *
 * @author Alan Hudson
 */
public class WorldCoordinate {
    public float x,y,z;

    public WorldCoordinate(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int hashCode() {
        int result = 31 + Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        result = 31 * result + Float.floatToIntBits(z);

        return result;
    }

    public boolean equals(Object o) {
        if (!(o instanceof WorldCoordinate))
            return false;

        WorldCoordinate coord = (WorldCoordinate) o;
        if (x == coord.x &&
            y == coord.y &&
            z == coord.z) {

            return true;
        }

        return false;
    }

    public String toString() {
        return "WorldCoordinate(" + hashCode() + ")" + x + " " + y + " " + z;
    }
}