/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.cli;

import static abfab3d.core.Output.fmt;

/**
 * A polyline is a collection of connected lines
 *
 * Command : start polyline
 * Syntax : $$POLYLINE/id,dir,n,p1x,p1y,...pnx,pny
 * Parameters:
 *
 * 	id		: INTEGER
 * 	dir,n		: INTEGER
 * 	p1x..pny	: REAL
 *
 * id : identifier to allow more than one model information in one file.
 * id refers to the parameter id of command $$LABEL (HEADER-section).
 * dir : Orientation of the line when viewing in the negative z-direction
 * 0 : clockwise (internal)
 * 1 : counter-clockwise (external)
 * 2 : open line (no solid)
 * n : number of points
 * p1x..pny : coordinates of the points 1..n
 *
 * Polylines representing internal contours must be clockwise, polylines representing external contours counter-clockwise (Fig 2).
 * This orientation must be valid for the parameter "dir" and for the order the points are listed. The value of "dir "
 * overwrites the order of listed points if there is a mismatch.
 *
 * In the case of closed polylines (dir = 0,1) p1x = pnx and p1y = pny must be valid.
 * The open line value for the dir flag can be used to indicate a non-closed polyline.
 * This can be used as an input for correction and editing tools based on the CLI format.
 */
public class PolyLine {
    private int id;
    private int dir;
    private double[] points;

    public PolyLine(int id, int dir, double[] points) {
        this.id = id;
        this.dir = dir;
        this.points = points;
    }

    public String toString(double units) {
        StringBuilder bldr = new StringBuilder();

        bldr.append(fmt("id: %d,",id));
        bldr.append(fmt("dir: %d,",dir));
        bldr.append(fmt("n: %d,",points.length/2));
        for(int i=0; i < points.length; i++) {
            bldr.append(fmt("%8.2f,",points[i]/units));
        }

        return bldr.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDir() {
        return dir;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }

    public double[] getPoints() {
        return points;
    }

    public void setPoints(double[] points) {
        this.points = points;
    }
}

