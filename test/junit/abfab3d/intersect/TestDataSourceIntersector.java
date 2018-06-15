/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.intersect;

//External Imports

import javax.vecmath.Vector3d;


import junit.framework.Test;
import junit.framework.TestSuite;

import abfab3d.BaseTestCase;

import abfab3d.core.ResultCodes;

import abfab3d.datasources.Sphere;
import abfab3d.datasources.Box;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;


// Internal Imports

/**
 * Tests the functionality of DataSourceIntersector
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestDataSourceIntersector extends BaseTestCase  {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDataSourceIntersector.class);
    }

    public void testNothing(){
        
    }
     

    public void devTestRaySphereIntersection() {
        
        double r = 10*MM;

        Sphere s = new Sphere(r);
        s.initialize();
        
        DataSourceIntersector dsi = new DataSourceIntersector();
        
        Vector3d starts[] = new Vector3d[]{new Vector3d(2*r, 0,0),new Vector3d(0, 0,0),new Vector3d(2*r,0,0),new Vector3d(0,2*r,0),};
        Vector3d dirs[] = new Vector3d[]{ new Vector3d(-1,0,0),new Vector3d(-1,0,0),new Vector3d(1,0,0),new Vector3d(0,-1,0),};
        
        printf("test intersecton with sphere radius: %7.3f mm\n",r/MM); 

        for(int i = 0; i < starts.length; i++){
            Vector3d start = starts[i];
            Vector3d dir = dirs[i];
            DataSourceIntersector.IntersectionResult res = dsi.getShapeRayIntersection(s, start, dir);
            
            printf("\n",start.x/MM,start.y/MM,start.z/MM); 
            printf("start: (%7.3f %7.3f %7.3f) mm ",toString(start,MM)); 
            printf("  dir: (%7.3f %7.3f %7.3f) \n",toString(dir,1.0)); 
            switch(res.code){
            case DataSourceIntersector.RESULT_OK: 
                printf("intersection: (%7.3f %7.3f %7.3f)mm\n", toString(res.end, MM)); break;
            case DataSourceIntersector.RESULT_NO_INTERSECTION: 
                printf("no_intersection:\n"); break;
            case DataSourceIntersector.RESULT_INITIAL_INTERSECTION: 
                printf("start is inside\n"); break;            
            }
        }
    }

    public void devTestSpheresIntersection() {

        double r = 10*MM;
        double rp = 5*MM;
        
        Sphere shape = new Sphere(r);
        //Box shape = new Box(2*r,2*r,2*r);
        shape.initialize();

        Sphere probe = new Sphere(rp);
        //Box probe = new Box(2*rp,2*rp,2*rp);
        probe.initialize();

        DataSourceIntersector dsi = new DataSourceIntersector();
        dsi.set("voxelSize",0.5*MM);
        dsi.set("minStep",0.5*MM);

        Vector3d data[] = new Vector3d[]{
            new Vector3d(2*r, 0,0),new Vector3d(-1,0,0),
            //new Vector3d(2*r, 0,0),new Vector3d(1,0,0),
            //new Vector3d(r+rp/2, 0,0),new Vector3d(-1,0,0),
        };
        long t0 = time();
        for(int i = 0; i < data.length/2; i++){
            Vector3d start = data[2*i];
            Vector3d dir = data[2*i+1];
            DataSourceIntersector.IntersectionResult res = dsi.getShapesIntersection(shape, probe,start, dir);
            
            printf("\n",start.x/MM,start.y/MM,start.z/MM); 
            printf("start: (%7.3f %7.3f %7.3f) mm ",start.x/MM,start.y/MM,start.z/MM); 
            printf("  dir: (%7.3f %7.3f %7.3f) \n",dir.x,dir.y, dir.z); 
            switch(res.code){
            case DataSourceIntersector.RESULT_OK: 
                printf("end: %s mm contact: %s length:%13.10f\n",toString(res.end,MM),toString(res.contact,MM),res.end.length()/MM); break;
            case DataSourceIntersector.RESULT_NO_INTERSECTION: 
                printf("no_intersection:\n"); break;
            case DataSourceIntersector.RESULT_INITIAL_INTERSECTION: 
                printf("probe intersects shape\n"); break;            
            }
        }
        
        printf("time: %7.4f sec\n",(time() - t0)/1000.);
        
    }

    static final String toString(Vector3d v, double unit){
        return fmt("(%7.3f %7.3f %7.3f)",v.x/unit,v.y/unit,v.z/unit);
    }



    public static void main(String arg[]){
        for(int i = 0; i < 1; i++){
            //new TestDataSourceIntersector().devTestRaySphereIntersection();
            new TestDataSourceIntersector().devTestSpheresIntersection();
        }
    }

}
