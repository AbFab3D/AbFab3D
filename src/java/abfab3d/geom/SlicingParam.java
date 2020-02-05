/** 
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


package abfab3d.geom;

import javax.vecmath.Vector3d;

import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.str;
import static abfab3d.core.Output.printf;


/**
   contains data for slicing 
*/
public class SlicingParam {

    static final boolean DEBUG = false;

    // count of slices 
    int sliceCount = 0;
    // normal to the sliceing plane 
    Vector3d normal = new Vector3d(0,0,1);
    // point on the plane of the first slice 
    Vector3d firstSliceLocation = new Vector3d(0,0,0);
    double sliceStep = 0.1*MM;
    private boolean m_isAuto = true;

    static final double DEFAULT_TOLERANCE = 0;
    double tolerance = 0;

    double minSliceValue = 0;

    double m_units = MM;
    String m_unitsName = "mm";
   
    
    
    public SlicingParam(){
        this.sliceCount = 0;
    }
    
    public SlicingParam(double sliceStep){
        this.sliceStep  = sliceStep;
        this.sliceCount = 0;
    }
    
    public SlicingParam(Vector3d normal, double sliceStep){
        
        this.sliceStep = sliceStep;
        this.sliceCount = 0;
        setNormal(normal);
    }

    public SlicingParam(Vector3d normal, double sliceStep, double tolerance){
        
        this.sliceStep = sliceStep;
        this.sliceCount = 0;
        this.tolerance = tolerance;
        setNormal(normal);
    }
    

    public SlicingParam(Vector3d normal, Vector3d firstSliceLocation, double sliceStep, int sliceCount){

        this(normal, firstSliceLocation, sliceStep, sliceCount, DEFAULT_TOLERANCE);
    }

    public SlicingParam(Vector3d normal, Vector3d firstSliceLocation, double sliceStep, int sliceCount, double tolerance){
    
        this.firstSliceLocation.set(firstSliceLocation);
        this.sliceStep = sliceStep;
        this.sliceCount = sliceCount;
        this.tolerance = tolerance;
        setNormal(normal);
        this.minSliceValue = this.firstSliceLocation.dot(this.normal);
        if(DEBUG) {
            String f = "%7.5f";
            printf("SlicingParam(%s ,%s, %7.5f, %d)\n", str(f,normal),str(f,firstSliceLocation),sliceStep, sliceCount);
        }
        m_isAuto = false;
    }
    
    public void setNormal(Vector3d normal){
        this.normal.set(normal);
        this.normal.normalize();
    }
    
    void setSlicesRange(double minValue, double maxValue){
        
        if(DEBUG){
            printf("setSlicesRange(%6.3f %s, %6.3f %s)\n",minValue/m_units, m_unitsName, maxValue/m_units, m_unitsName);
        }
        
        double step = this.sliceStep;
        // round down the values 
        double minSlice = step*Math.floor(minValue/step);
        double maxSlice = step*Math.ceil(maxValue/step);
        
        this.sliceCount =  (int)Math.round(( maxSlice -  minSlice)/step) + 1;
        // move the first slice point 
        this.firstSliceLocation.set(this.normal);
        this.firstSliceLocation.scale(minSlice);
        
        this.minSliceValue = minSlice;//firstSliceLocation.dot(this.normal);
        
    }
    
    public void getSlicePoint(int sliceIndex, Vector3d slicePoint){
        
        slicePoint.set(this.normal);
        slicePoint.scale(this.minSliceValue + sliceIndex*this.sliceStep);
        
    }

    public boolean isAuto(){
        return m_isAuto;
    }
    
} // class SlicingParam

