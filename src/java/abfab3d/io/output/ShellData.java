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

package abfab3d.io.output;

import abfab3d.mesh.ShellFinder;


public class ShellData implements Comparable<ShellData> {
    public ShellFinder.ShellInfo info;
    public double volume;


    ShellData(ShellFinder.ShellInfo info, double volume) {
        this.info = info;
        this.volume = volume;
    }

    @Override
    public int compareTo(ShellData o) {
        return Double.compare(volume, o.volume);
    }
}
