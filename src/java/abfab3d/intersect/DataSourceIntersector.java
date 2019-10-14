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

import abfab3d.core.*;

import abfab3d.util.PointSetArray;

import abfab3d.param.Parameter;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.BaseParameterizable;



import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;



/**
   DataSourceIntersector used to find intersection of ray with given signed distance data source

   @author Vladimir Bulatov
 */
public class DataSourceIntersector extends BaseParameterizable{

    static final boolean DEBUG = false;
    static final double UNIT = MM;

    public static final int RESULT_INTERSECTION_FOUND = 1;
    public static final int RESULT_INITIAL_INTERSECTION = 2;
    public static final int RESULT_NO_INTERSECTION = 3;
    public static final int RESULT_MAX_STEPS_EXCEEDED = 4;
    public static final int RESULT_MAX_DISTANCE_SURPASSED = 5;
    public static final int RESULT_CANT_FIND_SURFACE_POINTS = 6;

    
    DoubleParameter mp_minStep = new DoubleParameter("minStep",0.5*MM);
    DoubleParameter mp_maxStep = new DoubleParameter("maxStep",5*MM);
    DoubleParameter mp_maxDistance = new DoubleParameter("maxDistance",50*MM);
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize",0.5*MM);
    IntParameter mp_maxSteps = new IntParameter("maxStepsCount",1000);
    IntParameter mp_dimension = new IntParameter("dimension",3);

    
    Parameter m_params[] = {
        mp_minStep,
        mp_maxStep,
        mp_maxDistance,
        mp_voxelSize,
        mp_dimension,
        mp_maxSteps
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
    public Result getShapeRayIntersection(DataSource dataSource, Vector3d start, Vector3d direction){

        initialize(dataSource);

        int maxSteps = mp_maxSteps.getValue();
        int dataChannelIndex = 0;
        double step = mp_minStep.getValue();
        double maxDistance = mp_maxDistance.getValue();

        Vec pnt = new Vec(3);
        Vec value = new Vec(dataSource.getChannelsCount());

        pnt.set(start);
        dataSource.getDataValue(pnt, value);
        double dist0 = 0; // distance to travel along the ray 
        double value0 = value.v[dataChannelIndex];  // value at that point 
        if(value0 < 0){
            return new Result(RESULT_INITIAL_INTERSECTION, start,start);
        }
        Vector3d dir = new Vector3d();

        for(int i = 1; i < maxSteps; i++){

            double dist1 = i * step;                
            
            if(dist1 > maxDistance) 
                return new Result(RESULT_MAX_DISTANCE_SURPASSED, start, start); 
            
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
                return new Result(RESULT_INTERSECTION_FOUND, end, end);
            } 
            
            dist0 = dist1;
            value0 = value1;
            
        }
        
        return new Result(RESULT_MAX_STEPS_EXCEEDED, start, start); 

    }

    /**
       calculates point of first intersection of probe with given surface of data source 
       
       @param shape represented via signed distance function 
       @param probe represented as signed distance function 
       @param start initial location of probe
       @param direction normalized ray direction
       @return result of intersection
              
     */
    public Result getShapesIntersection(DataSource shape, DataSource probe, Vector3d start, Vector3d direction){
        
        initialize(shape);
        initialize(probe);

        double voxelSize = mp_voxelSize.getValue();
        double len = direction.length();
        if(len < EPS) 
            throw new RuntimeException(fmt("bad direction vector: (%e %e %e)",direction.x,direction.y,direction.z));
        direction.scale(1/len);
        // 1. Find points on the from moving surface of probe
        
        Bounds probeBounds = probe.getBounds();
        if(DEBUG)printf("probeBounds: %s mm\n",probeBounds.toString(UNIT));
        Vector3d pc = probeBounds.getCenter();
        Vector3d pr = probeBounds.getSize();
        double probeRadius = 0.5*pr.length();

        if(DEBUG)printf("probeBoundsRadius: %s mm\n",probeRadius/UNIT);


        // make plane orthogonal to the direction 
        // plane is defined by point on plane and two basis vectors a and b
        Vector3d a = new Vector3d(),b = new Vector3d();


        int sizeInVoxels = (int)Math.ceil(probeRadius/voxelSize);
        int na = 0, nb = 0;
        int dimension = mp_dimension.getValue();
        switch(dimension){
        case 1:
            na = 0;
            nb = 0;
            break;
        case 2:
            nb = 0;
            na = sizeInVoxels;
            b.set(Z_AXIS);
            Vector3d dir = new Vector3d(direction);
            MathUtil.orthogonalize(dir,Z_AXIS);   
            double dlen = dir.length();
            if(dlen < EPS){
                throw new RuntimeException(fmt("bad direction vector: (%e %e %e) for dimension:%d", 
                                               direction.x, direction.y, direction.z,dimension));
            }
            dir.scale(1./dlen);
            direction.set(dir);
            a.cross(direction, Z_AXIS);
            break;
        default: 
       case 3: 
            a.set(ODD_VECTOR); // some "random" vector
            MathUtil.orthogonalize(a,direction); 
            
            a.normalize();                         
            b.cross(a,direction);
            // a - random vector orthogonal to direction 
            // b - vector orthogoanl to a and direction 
            na = sizeInVoxels;
            nb = sizeInVoxels;
        }

        if(DEBUG)printf("a:(%7.3f %7.3f %7.3f) b:(%7.3f %7.3f %7.3f) na: %d nb:%d\n",a.x,a.y,a.z, b.x,b.y,b.z, na, nb);

        Vector3d stat = new Vector3d();
        Vector3d probeDir = new Vector3d(direction);
        probeDir.scale(-1); // opposite direction 
        // point on plane 
        Vector3d pointOnPlane = new Vector3d(direction);
        pointOnPlane.scale(probeRadius);
        Vector3d probeRayStart = new Vector3d();
        //printf("front surface points search (%d^2)\n", 2*nu+1);
        PointSetArray frontPoints = new PointSetArray();

        for(int ib = -nb; ib <= nb; ib++){
            double ub = ib*voxelSize;

            for(int ia = -na; ia <= na; ia++){
                double ua = ia*voxelSize;
                //if(ua*ua + ub*ub > probeRadius*probeRadius) 
                //    continue;
                probeRayStart.set(a.x*ua + b.x*ub + pc.x,a.y*ua + b.y*ub+pc.y,a.z*ua + b.z*ub+pc.z);
                probeRayStart.add(pointOnPlane);
                //printf("ia: %d\n", ia);
                //printValuesOnRay(probe, 0, probeRayStart, probeDir, 3*voxelSize, 50);

                Result res = getShapeRayIntersection(probe, probeRayStart, probeDir);
                switch(res.code){
                case RESULT_INTERSECTION_FOUND: 
                    if(DEBUG)printf("start: %s -> end: %s contact: %s  radius:%7.3f\n",
                                    toString(probeRayStart, UNIT), toString(res.end,UNIT), toString(res.contact,UNIT), res.end.length()/UNIT);
                    frontPoints.addPoint(res.end);
                    break;
                default:
                    if(DEBUG)printf("start: %s -> %s\n",toString(probeRayStart, UNIT), res.toString(UNIT));
                    break;
                }
            }            
            
        }

        if(DEBUG)printf("front surface points count: %d\n", frontPoints.size());

        if(frontPoints.size() < 1){
            return new Result(RESULT_CANT_FIND_SURFACE_POINTS, start, start);            
        }

        Vector3d minValuePoint0 = new Vector3d();
        double value0 = getMinValue(shape, frontPoints, start,minValuePoint0);
        if(value0 < 0){
            return new Result(RESULT_INITIAL_INTERSECTION, start, minValuePoint0);
        }        
        double dist0 = 0;
        int maxSteps = mp_maxSteps.getValue();
        double step = mp_minStep.getValue();
        double maxDistance = mp_maxDistance.getValue();
        Vector3d dir = new Vector3d();
        Vector3d pnt = new Vector3d();
        Vector3d minValuePoint1 = new Vector3d();
        //
        // find first intersection of moving front surface with the shape
        //
        for(int i = 1; i < maxSteps; i++){

            double dist1 = i * step;                
            
            if(dist1 > maxDistance)  return new Result(RESULT_MAX_DISTANCE_SURPASSED, start,start); 
            
            dir.set(direction);
            dir.scale(dist1);
            pnt.set(start);
            pnt.add(dir);

            double value1 = getMinValue(shape, frontPoints, pnt, minValuePoint1);

            if(value1 < 0){
                double t = -value0/(value1-value0);
                double d = dist0 + t*(dist1-dist0);
                Vector3d end = new Vector3d(start);
                dir.set(direction);
                dir.scale(d);   
                end.add(dir);
                minValuePoint0.interpolate(minValuePoint0,minValuePoint1, t);
                return new Result(RESULT_INTERSECTION_FOUND, end, minValuePoint0);
            } 
            
            dist0 = dist1;
            value0 = value1;
            minValuePoint0.set(minValuePoint1);
        }

        return new Result(RESULT_MAX_STEPS_EXCEEDED,new Vector3d(start), new Vector3d(0,0,0));
        
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
    static final Vector3d Z_AXIS = new Vector3d(0,0,1);
    static final double EPS = 1.e-5;

    static void printValuesOnRay(DataSource ds, int dataChannelIndex, Vector3d start, Vector3d direction, double step, int count){
        Vec pnt = new Vec(3);
        Vec value = new Vec(ds.getChannelsCount());
        Vector3d dir = new Vector3d();

        for(int i = 0; i < count; i++){
            double dist = step*i;
            dir.set(direction);
            dir.scale(dist);
            pnt.set(start);
            pnt.addSet(dir);
            ds.getDataValue(pnt, value);
            printf("(%9.5f %9.5f %9.5f) -> %9.5f\n",pnt.v[0],pnt.v[1],pnt.v[2], value.v[dataChannelIndex]);           
        }
    }

    public static String getCodeString(int code){
        switch(code){
        case RESULT_INTERSECTION_FOUND: return "INTERSECTION";
        case RESULT_INITIAL_INTERSECTION: return "INITIAL_INTERSECTION";
        case RESULT_NO_INTERSECTION:  return "NO_INTERSECTION";
        case RESULT_MAX_DISTANCE_SURPASSED:  return "MAX_DISTANCE_SURPASSED";
        case RESULT_CANT_FIND_SURFACE_POINTS: return "CANT_FIND_SURFACE_POINTS";
        case RESULT_MAX_STEPS_EXCEEDED:  return "MAX_STEPS_EXCEEDED";
        default: return "UNKNOWN";
        }
    }


    /**
       class describes result of intersection search 
     */
    public static class Result {

        int code;
        Vector3d end; // end point of ray to to move probe 
        Vector3d contact; // point or contact between shapes
        
        Result(int code, Vector3d end, Vector3d contact){
            this.code = code;
            this.end = end;
            this.contact = contact;
        }                    

        /**
           return location of probe when shapes make contact 
         */
        public Vector3d getLocation(){
            return end;
        }

        /**
           return point of contact
         */
        public Vector3d getContact(){
            return contact;
        }

        public int getCode(){
            return code;
        }

        public String toString(double unit){

            return fmt("code: %s location: (%7.3f %7.3f %7.3f) contact: (%7.3f %7.3f %7.3f) ",getCodeString(code), 
                      end.x/unit,end.y/unit,end.z/unit,contact.x/unit,contact.y/unit, contact.z/unit );
        }

    }

}