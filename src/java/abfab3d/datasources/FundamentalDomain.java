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


import abfab3d.util.ReflectionGroup;
import abfab3d.util.DataSource;



public class FundamentalDomain {

    public static DataSource getDataSource(ReflectionGroup.SPlane splane){
        if(splane instanceof ReflectionGroup.Plane){

            ReflectionGroup.Plane plane = (ReflectionGroup.Plane)splane;
            
            return new Plane(plane.getNormal(), plane.getDistance());
            
        } else if(splane instanceof ReflectionGroup.Sphere ){
            ReflectionGroup.Sphere sphere = (ReflectionGroup.Sphere)splane;
            return new Sphere(sphere.getCenter(), sphere.getRadius());            
        }
        return null;
    }

    public static DataSource getDataSource(ReflectionGroup.SPlane splanes[]){
        abfab3d.datasources.Intersection inter = new Intersection();
        for(int i = 0; i < splanes.length; i++){
            inter.add(getDataSource(splanes[i]));
        }
        return inter;
    }

    public static double getCosAngle(ReflectionGroup.SPlane sp1, ReflectionGroup.SPlane sp2){
        
        return sp1.getCosAngle(sp2);
        
    }

}