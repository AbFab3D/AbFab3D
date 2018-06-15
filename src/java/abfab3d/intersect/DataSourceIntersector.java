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

package abfab3d.intersect;


import javax.vecmath.Vector3d;

import abfab3d.core.MathUtil;
import abfab3d.core.Bounds;
import abfab3d.core.ResultCodes;
import abfab3d.core.DataSource;
import abfab3d.core.Vec;

import abfab3d.util.PointSetArray;

import abfab3d.param.Parameter;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.BaseParameterizable;



import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;



/**
   DataSourceIntersector used to find intersectio of ray with given signed distance data source 

   @author Vladimir Bulatov
 */
public class DataSourceIntersector extends BaseParameterizable{

    public static final int RESULT_OK = ResultCodes.RESULT_OK;
    public static final int RESULT_INITIAL_INTERSECTION = 2;
    public static final int RESULT_NO_INTERSECTION = 3;

    
    DoubleParameter m_minStep = new DoubleParameter("minStep",0.5*MM);
    DoubleParameter m_maxStep = new DoubleParameter("maxStep",5*MM);
    DoubleParameter m_maxDistance = new DoubleParameter("maxDistance",50*MM);
    DoubleParameter m_voxelSize = new DoubleParameter("voxelSize",0.5*MM);
    IntParameter m_maxSteps = new IntParameter("maxStepsCount",1000);
    
    Parameter m_params[] = {
        m_minStep,
        m_maxStep,
        m_maxDistance,
        m_voxelSize,
    };

    
    public DataSourceIntersector(){
        super.addParams(m_params);
    }

    /**
       calculates point of first intersection of ray from given point with surface of data source (value = 0) 
       
       @param dataSource data source which is supposed to return signed distance value in data channel 0 for each point in space. dataSource should be initialized 
       @param start  initial point of ray 
       @param direction vector of ray direction 
     */
    public IntersectionResult getShapeRayIntersection(DataSource dataSource, Vector3d start, Vector3d direction){

        int maxSteps = m_maxSteps.getValue();
        double minStep = m_minStep.getValue();
        int dataChannelIndex = 0;
        double step = m_minStep.getValue();
        double maxDistance = m_maxDistance.getValue();

        Vec pnt = new Vec(3);
        Vec value = new Vec(dataSource.getChannelsCount());
        
        pnt.set(start);
        dataSource.getDataValue(pnt, value);
        double dist0 = 0; // distance to travel along the ray 
        double value0 = value.v[dataChannelIndex];  // value at that point 
        if(value0 < 0){
            return new IntersectionResult(RESULT_INITIAL_INTERSECTION, start,start);
        }
        Vector3d dir = new Vector3d();

        for(int i = 1; i < maxSteps; i++){

            double dist1 = i * step;                
            
            if(dist1 > maxDistance) 
                return new IntersectionResult(RESULT_NO_INTERSECTION, start, start); 
            
            dir.set(direction);
            dir.scale(dist1);
            pnt.set(start);
            pnt.addSet(dir);
            dataSource.getDataValue(pnt, value);            
            double value1 = value.v[dataChannelIndex];
            if(value1 < 0){
                double d = dist0 - value0*(dist1-dist0)/(value1 - value0);
                Vector3d end = new Vector3d(start);
                dir.set(direction);
                dir.scale(d);   
                end.add(dir);
                return new IntersectionResult(RESULT_OK, end, end);
            } 
            
            dist0 = dist1;
            value0 = value1;
            
        }
        
        return new IntersectionResult(RESULT_NO_INTERSECTION, start, start); 

    }

    /**
       calculates point of first intersection of probe with given surface of data source 
       
       @param shape represented via signed distance function 
       @param probe represented as signed distance function 
       @param start initial locatiin of probe 
       @param direction normalized ray direction 

       @param intersection point of contact returned in that vector 
       @param 
              
     */
    public IntersectionResult getShapesIntersection(DataSource shape, DataSource probe, Vector3d start, Vector3d direction){
        
        direction.normalize();
        // 1. Find points on the from moving surface of probe
        
        Bounds probeBounds = probe.getBounds();
        //printf("probeBounds: %s mm\n",probeBounds.toString(MM));
        Vector3d pc = probeBounds.getCenter();
        Vector3d pr = probeBounds.getSize();
        double probeRadius = 0.5*pr.length();
        //printf("probeBoundsRadius: %s mm\n",probeRadius/MM);


        // make plane orthogonal to the direction 
        // plane is defined by point on plane and two basis vectors a and b
        Vector3d a = new Vector3d(ODD_VECTOR); // some "random" vector
        MathUtil.orthogonalize(a,direction);   

        a.normalize();     
                    
        Vector3d b = new Vector3d();
        b.cross(a,direction);

        //printf("a: (%6.3f %6.3f%6.3f)  b:(%6.3f %6.3f%6.3f)\n",a.x,a.y,a.z, b.x,b.y,b.z );
        double voxelSize = m_voxelSize.getValue();

        int nu = (int)Math.ceil(probeRadius/voxelSize);
        Vector3d stat = new Vector3d();
        Vector3d probeDir = new Vector3d(direction);
        probeDir.scale(-1); // opposite direction to find front facing points 
        // point on plane 
        Vector3d pointOnPlane = new Vector3d(direction);
        pointOnPlane.scale(probeRadius);
        Vector3d probeRayStart = new Vector3d();
        //printf("fron surface points search (%d^2)\n", 2*nu+1);
        PointSetArray frontPoints = new PointSetArray();
        for(int i = -nu; i <= nu; i++){
            double u = i*voxelSize;
            for(int j = -nu; j <= nu; j++){
                double v = j*voxelSize;
                if(u*v + v*v > probeRadius*probeRadius) 
                    continue;
                probeRayStart.set(a.x*u + b.x*v,a.y*u + b.y*v,a.z*u + b.z*v);
                probeRayStart.add(pointOnPlane);
                IntersectionResult res = getShapeRayIntersection(probe, probeRayStart, probeDir);
                switch(res.code){
                case RESULT_OK: 
                    if(false)printf("start: %s -> end: %s contact: %s  radius:%7.3f\n",
                                    toString(probeRayStart, MM), toString(res.end,MM), toString(res.contact,MM), res.end.length()/MM);
                    frontPoints.addPoint(res.end);
                    break;
                case RESULT_NO_INTERSECTION: 
                    //printf("start: %s -> no intersection\n",toString(probeRayStart, MM));
                    break;
                case RESULT_INITIAL_INTERSECTION: 
                    printf("start: %s -> initial intersection\n",toString(probeRayStart, MM));
                    break;                    
                }
            }            
            
        }

        printf("front surface points count: %d\n", frontPoints.size());
        Vector3d minValuePoint0 = new Vector3d();
        double value0 = getMinValue(shape, frontPoints, start,minValuePoint0);
        if(value0 < 0){
            return new IntersectionResult(RESULT_INITIAL_INTERSECTION, start, minValuePoint0);
        }        
        double dist0 = 0;
        int maxSteps = m_maxSteps.getValue();
        double step = m_minStep.getValue();
        double maxDistance = m_maxDistance.getValue();
        Vector3d dir = new Vector3d();
        Vector3d pnt = new Vector3d();
        Vector3d minValuePoint1 = new Vector3d();
        //
        // find first intersection of moving front surface with the shape
        //
        for(int i = 1; i < maxSteps; i++){

            double dist1 = i * step;                
            
            if(dist1 > maxDistance) 
                return new IntersectionResult(RESULT_NO_INTERSECTION, start,start); 
            
            dir.set(direction);
            dir.scale(dist1);
            pnt.set(start);
            pnt.add(dir);

            double value1 = getMinValue(shape, frontPoints, pnt, minValuePoint1);

            if(value1 < 0){
                double d = dist0 - value0*(dist1-dist0)/(value1 - value0);
                Vector3d end = new Vector3d(start);
                dir.set(direction);
                dir.scale(d);   
                end.add(dir);
                //TODO interpolate contact point 
                double t = 0;
                minValuePoint0.interpolate(minValuePoint0,minValuePoint1, t);
                return new IntersectionResult(RESULT_OK, end, minValuePoint1);
            } 
            
            dist0 = dist1;
            value0 = value1;
            minValuePoint0.set(minValuePoint1);
        }

        return new IntersectionResult(RESULT_NO_INTERSECTION,new Vector3d(start), new Vector3d(0,0,0));
        
    }

    /**
       return minimal distance from shape of point set
     */
    double getMinValue(DataSource shape, PointSetArray points, Vector3d translation, Vector3d minValuePoint){

        Vec vpnt = new Vec(3);
        Vec value = new Vec(shape.getChannelsCount());
        int dataIndex = 0;

        double minValue = Double.MAX_VALUE;
        Vector3d pp = new Vector3d();
        for(int k = 0; k < points.size(); k++){
            points.getPoint(k, pp);
            vpnt.set(pp);
            vpnt.addSet(translation);            
            shape.getDataValue(vpnt, value); 
            if(value.v[dataIndex] < minValue){
                minValue = value.v[dataIndex];
                minValuePoint.set(vpnt.v[0],vpnt.v[1],vpnt.v[2]);                
            }
        }
        return minValue;
    }


    static final String toString(Vector3d v, double unit){
        return fmt("(%7.3f %7.3f %7.3f)",v.x/unit,v.y/unit,v.z/unit);
    }

    static final Vector3d ODD_VECTOR = new Vector3d(-0.678,0.910,0.123);

    public static class IntersectionResult {

        int code;
        Vector3d end; // end point of ray to to move probe 
        Vector3d contact; // point or contact between shapes
        
        IntersectionResult(int code, Vector3d end, Vector3d contact){
            this.code = code;
            this.end = end;
            this.contact = contact;
        }                    
    }
}