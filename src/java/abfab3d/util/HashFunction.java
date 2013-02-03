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

package abfab3d.util;

/**
 *  External hashing function.
 *
 * @author Alan Hudson
 */
public interface HashFunction {
    public int calcHashCode(StructMixedData src, int srcIdx);

    public boolean calcEquals(StructMixedData src, int a, int b);
}
