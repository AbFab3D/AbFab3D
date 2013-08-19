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

package abfab3d.datasources;


//import java.awt.image.Raster;

import abfab3d.util.Units;
import abfab3d.util.Vec;

import javax.vecmath.Vector3d;

import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.Output.printf;


/**
 * Solid box of given size
 
   <embed src="doc-files/Box.svg" type="image/svg+xml"/> 

 * @author Vladimir Bulatov
 */
public class Box extends TransformableDataSource {

    static final boolean DEBUG = false;
    static int debugCount = 1000;

    private double m_sizeX = 0.1, m_sizeY = 0.1, m_sizeZ = 0.1, m_centerX = 0, m_centerY = 0, m_centerZ = 0;

    private double
            xmin,
            xmax,
            ymin,
            ymax,
            zmin, zmax;

    protected boolean
            m_hasSmoothBoundaryX = true,
            m_hasSmoothBoundaryY = true,
            m_hasSmoothBoundaryZ = true;

    /**
     * Box with given center and size
     *
     * @param cx  x coordinate of center
     * @param cy  y coordinate of center
     * @param cz  z coordinate of center
     * @param sx x size
     * @param sy y size
     * @param sz z size
     */
    public Box(double cx, double cy, double cz, double sx, double sy, double sz) {
        setCenter(cx, cy, cz);
        setSize(sx, sy, sz);
    }

    /**
     * Box with 0,0,0 center and given size
     *
     * @param sx x size
     * @param sy y size
     * @param sz z size
     */
    public Box(double sx, double sy, double sz) {
        setSize(sx, sy, sz);
    }

    /**
     * Box with 0,0,0 center and given size
     *
     * @param size Size vector
     */
    public Box(Vector3d size) {
        setSize(size.x, size.y, size.z);
    }

    /**
     * Blah blah
     *
     * @noRefGuide
     * @param boundaryX
     * @param boundaryY
     * @param boundaryZ
     */
    public void setSmoothBoundaries(boolean boundaryX, boolean boundaryY, boolean boundaryZ) {
        m_hasSmoothBoundaryX = boundaryX;
        m_hasSmoothBoundaryY = boundaryY;
        m_hasSmoothBoundaryZ = boundaryZ;
    }

    /**
     * Set the size of the box
     *
     * @param sx x size
     * @param sy y size
     * @param sz z size
     */
    public void setSize(double sx, double sy, double sz) {
        m_sizeX = sx;
        m_sizeY = sy;
        m_sizeZ = sz;
    }

    /**
     * Set the size of the box
     *
     * @param size Size vector
     */
    public void setSize(Vector3d size) {
        m_sizeX = size.x;
        m_sizeY = size.y;
        m_sizeZ = size.z;
    }

    /**
     * Set the center of the box
     *
     * @param x  x coordinate of center
     * @param y  y coordinate of center
     * @param z  z coordinate of center
     *
     */
    public void setCenter(double cx, double cy, double cz) {
        m_centerX = cx;
        m_centerY = cy;
        m_centerZ = cz;
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();

        xmin = m_centerX - m_sizeX / 2;
        xmax = m_centerX + m_sizeX / 2;

        ymin = m_centerY - m_sizeY / 2;
        ymax = m_centerY + m_sizeY / 2;

        zmin = m_centerZ - m_sizeZ / 2;
        zmax = m_centerZ + m_sizeZ / 2;

        return RESULT_OK;

    }

    /**
     * Get the data value for a pnt
     *
     * @noRefGuide
     *
     * @return 1 if pnt is inside of box of given size and center 0 otherwise
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

        double res = 1.;
        double
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

        double vs = pnt.getScaledVoxelSize();
        if (DEBUG && debugCount-- > 0)
            printf("vs: %5.3fmm\n", vs / Units.MM);
        if (vs == 0.) {
            // zero voxel size
            if (x < xmin || x > xmax ||
                    y < ymin || y > ymax ||
                    z < zmin || z > zmax) {
                data.v[0] = 0.;
                return RESULT_OK;
            } else {
                data.v[0] = 1.;
                return RESULT_OK;
            }
        } else {

            // finite voxel size

            if (x <= xmin - vs || x >= xmax + vs ||
                    y <= ymin - vs || y >= ymax + vs ||
                    z <= zmin - vs || z >= zmax + vs) {
                data.v[0] = 0.;
                return RESULT_OK;
            }
            double finalValue = 1;

            if (m_hasSmoothBoundaryX)
                finalValue = Math.min(finalValue, intervalCap(x, xmin, xmax, vs));
            if (m_hasSmoothBoundaryY)
                finalValue = Math.min(finalValue, intervalCap(y, ymin, ymax, vs));
            if (m_hasSmoothBoundaryZ)
                finalValue = Math.min(finalValue, intervalCap(z, zmin, zmax, vs));

            data.v[0] = finalValue;
            return RESULT_OK;
        }
    }

}  // class Box
