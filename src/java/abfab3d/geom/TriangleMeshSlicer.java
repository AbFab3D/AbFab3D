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

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.distance.DistanceDataHalfSpace;



import javax.vecmath.Vector3d;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static abfab3d.core.MathUtil.str;
import static abfab3d.core.Output.printf;


/**
   calculates slices of triangle mesh by family of planes 
 */
public class TriangleMeshSlicer implements TriangleCollector {
    
    static final boolean DEBUG = true;

    

    public TriangleMeshSlicer(){
        
    }
    

    DistanceDataHalfSpace m_plane;
    TriangleSlicer m_triSlicer;
    
    public void makeSlices(TriangleProducer producer){
        
        m_plane = new DistanceDataHalfSpace(new Vector3d(0,0,1), new Vector3d(0,0,0));

        m_triSlicer = new TriangleSlicer();
        if(DEBUG) printf("makeSlices()\n");
        producer.getTriangles(this);

    }

    public boolean addTri(Vector3d p0,Vector3d p1,Vector3d p2){
        
        double d0 = m_plane.getDistance(p0.x,p0.y,p0.z);
        double d1 = m_plane.getDistance(p1.x,p1.y,p1.z);
        double d2 = m_plane.getDistance(p2.x,p2.y,p2.z);
        if(DEBUG)printf("d0:%8.3e d1:%8.3e d2:%8.3e\n", d0, d1, d2);
        Vector3d q0 = new Vector3d();
        Vector3d q1 = new Vector3d();

        int res = m_triSlicer.getIntersection(p0, p1, p2, d0, d1, d2, q0, q1);
        String format = "%8.5f";
        switch(res){
        case TriangleSlicer.INTERSECT:
            double dq0 = abs(m_plane.getDistance(q0.x,q0.y,q0.z));
            double dq1 = abs(m_plane.getDistance(q1.x,q1.y,q1.z));
            if(DEBUG)printf("intersect q0: %s, q1: %s, dq0:%8.2e dq1:%8.2e\n", str(format, q0),str(format, q1), dq0, dq1);
        }

        return true;
    }


}