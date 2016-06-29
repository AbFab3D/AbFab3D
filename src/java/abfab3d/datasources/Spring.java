/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
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


import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;

import javax.vecmath.Vector3d;

import static abfab3d.core.MathUtil.step10;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;


/**
 * Spring centered at given point with axis parallel to z-axis
 *
 * <embed src="doc-files/Spring.svg" type="image/svg+xml"/>
 *
 * Working from this definition: http://en.wikipedia.org/wiki/Spring_(math)
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class Spring extends TransformableDataSource{

    /** Distance from the center of the tube */
    private double R;

    /** Radius of the tube */
    private double r;

    /** distance between consequtive coils of the spring. Positive for right handed, negative for left.  */
    private double springPeriod;
    
    private double crossSectionPower = 2;
    private double rpower; //r^power
    private double rpower1; //r^(power-1)

    private double x0,y0,z0;
    private double zmin,zmax;


    /**
     * Spring with specified center, outer radius, tube radius, speed and number of turns
     *
     * @param center - location of helix center
     * @param R -  Distance from the center of the tube 
     * @param r - Radius of the tube
     * @param springPeriod - z-distance between coils of spirng.  Positive for right handed, negative for left.
     * @param springLength - the length of the spring along z axis
     */
    public Spring(Vector3d center, double R, double r, double springPeriod, double springLength) {
        this(center.x,center.y,center.z,R,r, springPeriod, springLength);
    }

    /**
     * Spring with specified outer radius, tube radius, speed and number of turns
     *
     * @param R -  Distance from the center of the tube
     * @param r - Radius of the tube
     * @param springPeriod - z-distance between coils of spirng.  Positive for right handed, negative for left.
     * @param springLength - the length of the spring along z axis
     * Spring centered at origin
     */
    public Spring(double R, double r, double springPeriod, double springLength){
        this(0,0,0,R,r,springPeriod, springLength);
    }

    /**
     * Spring with specified center, outer radius, tube radius, speed and number of turns
     *
     * @param cx - x component of center
     * @param cy - y component of center
     * @param cz - z component of center
     * @param R -  Distance from the center of the tube to center of coil  
     * @param r - Radius of the tube
     * @param springPeriod - z-distance between coils of spirng.  Positive for right handed, negative for left.
     * @param springLength - the length of the spring along z axis
     */
    public Spring(double cx, double cy, double cz, double R,double r, double springPeriod, double springLength){
        setCenter(cx, cy, cz);
        this.R = R;
        this.r = r;
        this.springPeriod = springPeriod;
        zmin = -springLength/2;
        zmax =  springLength/2;
    }

    /**
       @noRefGuide
     */
    public void setCenter(double cx, double cy, double cz) {
        this.x0 = cx;
        this.y0 = cy;
        this.z0 = cz;
    }

    /**
       sets power of the crossection shape (x^p + y^p = r^p) 
       for p = 2 we have circle 
       for p > 2 we have square with rounded corners 
       for p < 2 we have rhombus with rounded corners 

     */
    public void setCrossSectionPower(double value){
        this.crossSectionPower = value;
    }
    
    /**
     * @noRefGuide
     */
    public int initialize(){

        super.initialize();

        this.rpower = pow(r, crossSectionPower);
        this.rpower1 = pow(r, crossSectionPower-1);
        
        return ResultCodes.RESULT_OK;
    }

    /**
     * returns 1 if pnt is inside of Spring
     * returns interpolated value if point is within voxel size to the boundary
     * returns 0 if pnt is outside the Spring
       @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);

        double
                x = pnt.v[0] - x0,
                y = pnt.v[1] - y0,
                z = pnt.v[2] - z0;

        if (zmin != zmax && (z < zmin || z > zmax)){
            data.v[0] = 0;
            return ResultCodes.RESULT_OK;
        }

        double period = springPeriod;
        z -= (period)*(floor(z/period));

        double rxy = sqrt(x*x + y*y) - R;        

        double helixOffset = Math.atan2(y,x) / Math.PI;        
        double z0 = z - period * (0.5*Math.atan2(y,x) / Math.PI);         
        // select the closestpnt.getScaledVoxelSize() branch of helix
        if(z0 > period/2) z0 -= period; 
        if(z0 < -period/2) z0 += period;
        
        //double d0 = (rxy*rxy + z0*z0 - r*r)/(2*r); 
        
        double p = crossSectionPower;
        
        double d0 = (pow(abs(rxy), p) + pow(abs(z0), p) - rpower)/(p*rpower1); 
        

        d0 = step10(d0, 0, pnt.getScaledVoxelSize());
        data.v[0] = d0;

        return ResultCodes.RESULT_OK;
    }
}  // class Spring

