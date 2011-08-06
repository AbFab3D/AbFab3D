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

package abfab3d.geom;

// External Imports
import java.util.*;
import java.io.*;
import org.web3d.vrml.sav.ContentHandler;

// Internal Imports
import abfab3d.grid.Grid;

/**
 * Creates a Sphere
 *
 * @author Alan Hudson
 */
public class SphereCreator extends GeometryCreator {
    private static final double EPS = 0.000002;

    protected double r;

    // The center position
    protected double x0;
    protected double y0;
    protected double z0;

    // Rotation to apply
    protected double rx;
    protected double ry;
    protected double rz;
    protected double rangle;

    protected int outerMaterial;
    protected int innerMaterial;

    boolean swapYZ = false;

    /**
     * Constructor.
     *
     */
    public SphereCreator(
        double r, int imat, int omat) {

        this(r,0,0,0,0,1,0,0,imat,omat);
    }

    /**
     * Constructor.
     *
     */
    public SphereCreator(
        double r,
        double x, double y, double z,
        double rx, double ry, double rz, double ra,
        int imat, int omat) {

        this.r = r;
        this.x0 = x;
        this.y0 = y;
        this.z0 = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.rangle = ra;
        outerMaterial = omat;
        innerMaterial = imat;
    }

    /**
     * Generate the geometry and issue commands to the provided handler.
     *
     * @param handler The stream to issue commands
     */
    public void generate(Grid grid) {
        // Wall all grid points and check implicit equations points = 0
        // f(x,y,z) = (or - sqrt(x^2 + y^2)) ^ 2 + z^2 - ir^2

        // TODO: can we detect surface points?

        int w = grid.getWidth();
        int h = grid.getHeight();
        int d = grid.getDepth();

System.out.println("Generating Sphere: " + x0 + " y: " + y0 + " z: " + z0 + " r: " + r);
        double[] wcoords = new double[3];
        double fval;
        double tmp;

        // TODO: use bounds to reduce calcs

        for(int i=0; i < w; i++) {
            for(int j=0; j < h; j++) {
                for(int k=0; k < d; k++) {
                    grid.getWorldCoords(i,j,k,wcoords);


                    if (surface(wcoords[0],wcoords[1],wcoords[2])) {
                        grid.setData(i,j,k,Grid.EXTERIOR, outerMaterial);
                    } else if (inside(wcoords[0],wcoords[1],wcoords[2])) {
                        grid.setData(i,j,k,Grid.INTERIOR, innerMaterial);
                    }

/*
                    if (inside(wcoords[0],wcoords[1],wcoords[2])) {
                        grid.setData(i,j,k,Grid.EXTERIOR, outerMaterial);
                    }
*/
                }
            }
        }
    }

    private boolean inside(double x,double y, double z) {
        double fval = (x - x0) * (x - x0) + (y - y0) * (y - y0) + (z - z0) * (z - z0) - r * r;

        if (fval <= 0) {
            return true;
        }

        return false;
    }

    private boolean surface(double x, double y, double z) {
        double fval = (x - x0) * (x - x0) + (y - y0) * (y - y0) + (z - z0) * (z - z0) - r * r;

/*
if (x <= 0.001 && y <= 0.001 && z <= 0.001) {
    System.out.println("pos: " + x + " " + y + " z: " + z + " fval: " + fval);
}
*/

        if (Math.abs(fval) < EPS) {

//        if (fval == 0) {
            return true;
        }

        return false;
    }
}
