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
import abfab3d.util.Bounds;
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
    private static final double DEFAULT_WIDTH = 0.1;
    private static final double DEFAULT_HEIGHT = 0.1;
    private static final double DEFAULT_DEPTH = 0.1;

    private static final Vector3d DEFAULT_SIZE = new Vector3d(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_DEPTH);

    private double
            xmin,xmax,
            ymin,ymax,
            zmin,zmax;
    
    
    Vector3dParameter mp_center = new Vector3dParameter("center","Center of the box",new Vector3d(0.,0.,0.));
    Vector3dParameter mp_size = new Vector3dParameter("size","Size of the box",DEFAULT_SIZE);
    // rounding of the edges
    DoubleParameter  mp_rounding = new DoubleParameter("rounding","Width of rounding of the box edges", 0.,0,Double.MAX_VALUE,0.1*MM);

    Parameter m_aparam[] = new Parameter[]{
        mp_size,
        mp_center,
        mp_rounding
    };

    protected boolean
            m_hasSmoothBoundaryX = true,
            m_hasSmoothBoundaryY = true,
            m_hasSmoothBoundaryZ = true;

    /**
     * Construct a default box
     */
    public Box() {
        this(0,0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_DEPTH);
    }

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
     * @param sx x size
     * @param sy y size
     * @param sz z size
     * @param rounding The amount of rounding
     */
    public Box(double sx, double sy, double sz,double rounding) {
        this(0, 0, 0, sx, sy, sz,rounding);
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
        this(cx,cy,cz,sx,sy,sz,0);
    }

    /**
     * Box with given center, size and rounding
     * @param center
     * @param size
     * @param rounding
     */
    public Box(Vector3d center, Vector3d size, double rounding) {

        this(center.x,center.y,center.z,size.x,size.y,size.z,rounding);

    }

    /**
     * Box with given center, size and rounding
     * @param center
     * @param size
     */
    public Box(Vector3d center, Vector3d size) {
        this(center.x,center.y,center.z,size.x,size.y,size.z);
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
     * @param rounding The amount of rounding
     */
    public Box(double cx, double cy, double cz, double sx, double sy, double sz, double rounding) {
        initParams();

        setSize(sx,sy,sz);
        setCenter(cx,cy,cz);
        setRounding(rounding);

    }

    /**
     * Set the size
     * @param val The vector of values in meters
     */
    public void setSize(Vector3d val) {
        mp_size.setValue(val);
    }

    /**
     * Set the size
     * @param width width in meters 
     * @param height height  in meters 
     * @param depth depth in meters 
     */
    public void setSize(double width, double height, double depth) {
        mp_size.setValue(new Vector3d(width, height, depth));
    }

    /**
     * Get the size
     * @return The size in meters
     */
    public Vector3d getSize() {
        return new Vector3d(mp_size.getValue());
    }

    /**
     * Set the height
     * @param val The value in meters
     */
    public void setWidth(double val) {
        Vector3d s = mp_size.getValue();
        s.x = val;
    }

    /**
     * Set the height
     * @param val The value in meters
     */
    public void setHeight(double val) {
        Vector3d s = mp_size.getValue();
        s.y = val;
    }

    /**
     * Set the depth
     * @param val The value in meters
     */
    public void setDepth(double val) {
        Vector3d s = mp_size.getValue();
        s.z = val;
    }

    /**
     * Set the center of the coordinate system
     * @param val The value in meters
     */
    public void setCenter(Vector3d val) {
        mp_center.setValue(val);
    }

    /**
     * Set the center of the coordinate system
     */
    public void setCenter(double x,double y, double z) {
        mp_center.setValue(new Vector3d(x,y,z));
    }

    /**
     * Get the center of the coordinate system.
     * @return The value in meters
     */
    public Vector3d getCenter() {
        return new Vector3d(mp_center.getValue());
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
     * Call to update bounds after each param change that affects bounds
     * @noRefGuide;
     */
    protected void updateBounds() {
        Vector3d s = mp_size.getValue();
        double w = s.x;
        double h = s.y;
        double d = s.z;
        Vector3d c = mp_center.getValue();
        double centerX = c.x;
        double centerY = c.y;
        double centerZ = c.z;

        m_bounds = new Bounds(centerX - w / 2,centerX + w / 2,centerY - h / 2,centerY + h /2,centerZ - d / 2,centerZ + d /2);

    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();
        Vector3d c = mp_center.getValue();
        double centerX = c.x;
        double centerY = c.y;
        double centerZ = c.z;

        Vector3d s = mp_size.getValue();
        double sizeX = s.x;
        double sizeY = s.y;
        double sizeZ = s.z;
        
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
