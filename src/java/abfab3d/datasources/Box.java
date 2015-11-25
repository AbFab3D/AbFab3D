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


import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.util.Units;
import abfab3d.util.Vec;

import javax.vecmath.Vector3d;

import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;


/**
 * Solid box of given size
 
   <embed src="doc-files/Box.svg" type="image/svg+xml"/> 

 * @author Vladimir Bulatov
 */
public class Box extends TransformableDataSource {

    static final boolean DEBUG = false;
    static int debugCount = 1000;

    //private double m_sizeX = 0.1, m_sizeY = 0.1, m_sizeZ = 0.1, m_centerX = 0, m_centerY = 0, m_centerZ = 0;

    private double
            xmin,
            xmax,
            ymin,
            ymax,
            zmin, zmax;
    
    
    Vector3dParameter  mp_center = new Vector3dParameter("center","center of the box",new Vector3d(0,0,0));
    Vector3dParameter  mp_size = new Vector3dParameter("size","size of the box",new Vector3d(0.1,0.1,0.1));
    // rounding of the edges
    DoubleParameter  mp_rounding = new DoubleParameter("rounding","rounding of the box edges", 0.);

    Parameter m_aparam[] = new Parameter[]{
        mp_center,
        mp_size,
        mp_rounding
    };

    protected boolean
            m_hasSmoothBoundaryX = true,
            m_hasSmoothBoundaryY = true,
            m_hasSmoothBoundaryZ = true;

    /**
     * Box with 0,0,0 center and given size
     *
     * @param sx x size
     * @param sy y size
     * @param sz z size
     */
    public Box(double sx, double sy, double sz) {
        this(0, 0, 0, sx, sy, sz);
    }

    /**
     * Box with 0,0,0 center and given size
     *
     * @param size Size vector
     */
    public Box(Vector3d size) {
        this(0,0,0,size.x,size.y,size.z);
    }


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
        initParams();
        
        mp_center.setValue(new Vector3d(cx, cy, cz));
        mp_size.setValue(new Vector3d(sx, sy, sz));

    }

    public Box(Vector3d center, Vector3d size, double rounding) {

        initParams();
        mp_center.setValue(new Vector3d(center));
        mp_size.setValue(new Vector3d(size));
        mp_rounding.setValue(new Double(rounding));

    }

    /**
     * Set the size of the box.
     * @param val The size in meters
     */
    public void setSize(Vector3d val) {
        mp_size.setValue(val);
    }

    /**
     * Get the size
     */
    public Vector3d getSize() {
        return mp_size.getValue();
    }

    /**
     * Set the center of the coordinate system
     * @param val The center
     */
    public void setCenter(Vector3d val) {
        mp_center.setValue(val);
    }

    /**
     * Get the center of the coordinate system
     * @return
     */
    public Vector3d getCenter() {
        return mp_center.getValue();
    }

    /**
     * Set the amount of rounding of the edges
     * @param val
     */
    public void setRounding(double val) {
        mp_rounding.setValue(val);
    }

    /**
     * Get the amount of rounding of the edges
     */
    public double getRounding() {
        return mp_rounding.getValue();
    }

    /**
     * @noRefGuide;
     */
    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();
        Vector3d center = mp_center.getValue();
        Vector3d size = mp_size.getValue();

        double centerX = center.x;
        double centerY = center.y;
        double centerZ = center.z;

        double sizeX = size.x;
        double sizeY = size.y;
        double sizeZ = size.z;
        
        xmin = centerX - sizeX / 2;
        xmax = centerX + sizeX / 2;

        ymin = centerY - sizeY / 2;
        ymax = centerY + sizeY / 2;

        zmin = centerZ - sizeZ / 2;
        zmax = centerZ + sizeZ / 2;

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
            } else {
                data.v[0] = 1.;
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
        }

        super.getMaterialDataValue(pnt, data);        
        return RESULT_OK;        

    }

}  // class Box
