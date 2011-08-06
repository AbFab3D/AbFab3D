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

package abfab3d.creator;

// External Imports

/**
 * Step Details
 *
 * @author Alan Hudson
 */
public class Step {
    private int step;
    private String name;
    private String desc;

    public Step(int step, String name, String desc) {
        this.step = step;
        this.name = name;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return name;
    }

    public int getStep() {
        return step;
    }
}