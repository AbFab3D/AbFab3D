/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.cli;

/**
 * Hatches is a collection of lines
 *
 * Command : start hatches
 * Syntax : $$HATCHES/id,n,p1sx,p1sy,p1ex,p1ey,...pnex,pney
 * Parameters:
 *
 * 	id		: INTEGER
 * 	n		: INTEGER
 * 	p1sx..pney	: REAL
 * id : identifier to allow more than one model information in one file.
 * id refers to the parameter id of command $$LABEL (HEADER-section).
 * n : number of hatches (n*4 =number of coordinates)
 * p1sx..pney : coordinates of the hatches 1..n
 * 4 parameters for every hatch (startx,starty,endx,endy)
 */
public class Hatches {
    private int id;
    private double[] coords;

    public Hatches(int id, double[] coords) {
        this.id = id;
        this.coords = coords;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double[] getCoords() {
        return coords;
    }

    public void setCoords(double[] coords) {
        this.coords = coords;
    }
}
