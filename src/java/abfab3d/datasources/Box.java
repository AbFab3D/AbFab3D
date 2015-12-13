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

    private double
            xmin,xmax,
            ymin,ymax,
            zmin,zmax;
    
    
    DoubleParameter mp_width = new DoubleParameter("width","Width",0.1,0.1*MM,Double.MAX_VALUE,0.1);
    DoubleParameter mp_height = new DoubleParameter("height","Height",0.1,0.1*MM,Double.MAX_VALUE,0.1);
    DoubleParameter mp_depth = new DoubleParameter("depth","Depth",0.1,0.1*MM,Double.MAX_VALUE,0.1);
    DoubleParameter mp_centerX = new DoubleParameter("centerX","Center X",0);
    DoubleParameter mp_centerY = new DoubleParameter("centerY","Center Y",0);
    DoubleParameter mp_centerZ = new DoubleParameter("centerZ","Center Z",0);

    // rounding of the edges
    DoubleParameter  mp_rounding = new DoubleParameter("rounding","rounding of the box edges", 0.);

    Parameter m_aparam[] = new Parameter[]{
        mp_width,mp_height,mp_depth,
        mp_centerX,mp_centerY,mp_centerZ,
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

        setWidth(sx);
        setHeight(sy);
        setDepth(sz);

        setCenterX(cx);
        setCenterY(cy);
        setCenterZ(cz);
    }

    /**
     * Box with given center, size and rounding
     * @param center
     * @param size
     * @param rounding
     */
    public Box(Vector3d center, Vector3d size, double rounding) {

        initParams();
        setWidth(size.x);
        setHeight(size.y);
        setDepth(size.z);

        setCenterX(center.x);
        setCenterY(center.y);
        setCenterZ(center.z);
        mp_rounding.setValue(new Double(rounding));
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

        setWidth(sx);
        setHeight(sy);
        setDepth(sz);

        setCenterX(cx);
        setCenterY(cy);
        setCenterZ(cz);

        setRounding(rounding);
    }

    /**
     * Set the width
     * @param val The value in meters
     */
    public void setWidth(double val) {
        mp_width.setValue(val);
    }

    /**
     * Set the height
     * @param val The value in meters
     */
    public void setHeight(double val) {
        mp_height.setValue(val);
    }

    /**
     * Set the depth
     * @param val The value in meters
     */
    public void setDepth(double val) {
        mp_depth.setValue(val);
    }

    /**
     * Set the center x position
     * @param val The value in meters
     */
    public void setCenterX(double val) {
        mp_centerX.setValue(val);
    }

    /**
     * Set the center y position
     * @param val The value in meters
     */
    public void setCenterY(double val) {
        mp_centerY.setValue(val);
    }

    /**
     * Set the center z position
     * @param val The value in meters
     */
    public void setCenterZ(double val) {
        mp_centerZ.setValue(val);
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

        double centerX = mp_centerX.getValue();
        double centerY = mp_centerY.getValue();
        double centerZ = mp_centerZ.getValue();

        double sizeX = mp_width.getValue();
        double sizeY = mp_height.getValue();
        double sizeZ = mp_depth.getValue();
        
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
