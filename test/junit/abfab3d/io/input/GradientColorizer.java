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

package abfab3d.io.input;

import javax.vecmath.Vector3d;

import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;
import abfab3d.core.MathUtil;


/**
   calculates colored wave orthogonal to gradient direction 
   colors change between 2 given colors 
 */
public class GradientColorizer implements DataSource {

    Vector3d grad = new Vector3d(1,0,0);

    static final double COLOR0[] = new double[]{1,0,0};
    static final double COLOR1[] = new double[]{0,0,1};
    double color0[] = new double[3];
    double color1[] = new double[3];
    
    public GradientColorizer(Vector3d grad){
        this(grad, COLOR0, COLOR1);
    }

    public GradientColorizer(Vector3d grad, double color0[] , double color1[]){
        this.grad.set(grad);
        double len2 = grad.lengthSquared();
        this.grad.scale(1./len2);
        System.arraycopy(color0, 0, this.color0, 0, 3);
        System.arraycopy(color1, 0, this.color1, 0, 3);

    }

    /**
       data value at the given point 
       @param pnt Point where the data is calculated 
       @param dataValue - storage for returned calculated data 
       @return result code 
     */
    public int getDataValue(Vec pnt, Vec dataValue){

        double dot = pnt.v[0]*grad.x + pnt.v[1]*grad.y + pnt.v[2]*grad.z;
        
        double t = 2*Math.abs((dot - Math.floor(dot))-0.5);
        
        MathUtil.lerp(color0, color1, t, dataValue.v);
        return ResultCodes.RESULT_OK;
    }

    /**
       @returns count of data channels, 
       it is the count of data values returned in  getDataValue()        
     */
    public int getChannelsCount(){
        return 3;
    }

    /**
     * Get the bounds of this data source.  The data source can be infinite.
     * @return
     */
    public Bounds getBounds(){
        return Bounds.INFINITE;
    }    

    /**
     * Set the bounds of this data source.  For infinite bounds use Bounds.INFINITE
     * @param bounds
     */
    public void setBounds(Bounds bounds){
        // ignore 
    }

}