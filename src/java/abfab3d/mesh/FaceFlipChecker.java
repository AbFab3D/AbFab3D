/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.mesh;

import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

/**
 * class to check face Flip
 */
public class FaceFlipChecker {
    static final double FACE_FLIP_EPSILON = 1.e-20;
    
    // minimal dot product allowed between normals.     
    double m_faceFlipThreshold = 0.001; 

    Vector3d
            // p0 = new Vector3d(), // we move origin to p0
            m_p1 = new Vector3d(),
            m_v0 = new Vector3d(),
            m_v1 = new Vector3d(),
            m_n0 = new Vector3d(),
            m_n1 = new Vector3d();

    /**
     * return true if deforming triangle (p0, p1, v0) into (p0, p1, v1) will flip triangle normal
     * return false otherwise
     */
    public boolean hasFlip(Point3d p0, Point3d p1, Point3d v0, Point3d v1) {

        m_p1.set(p1);
        m_v0.set(v0);
        m_v1.set(v1);

        m_p1.sub(p0);
        m_v0.sub(p0);
        m_v1.sub(p0);

        double lenp1 = m_p1.length();
        double lenv0 = m_v0.length();
        double norm = lenp1;
        if(lenv0 > lenp1)
            norm = lenv0;
        // norm is he length of longes side of orig triangle 
        double s = 1./norm;
        m_p1.scale(s);
        m_v0.scale(s);
        m_v1.scale(s);
        //both triangles have size around 1 
        
        m_n0.cross(m_p1, m_v0);

        m_n1.cross(m_p1, m_v1);

        double dot = m_n0.dot(m_n1);

        if (dot < m_faceFlipThreshold) // face flip
            return true;
        else
            return false;
    }
    /**
     * return true if deforrming trinagle (p0, p1, v0) into (p0, p1, v1) will flip triangle normal
     * return false otherwise
     * NOT USED 
     */
    public boolean _checkFaceFlip(double[] p0, double[] p1, double[] v0, double[] v1) {

        m_p1.set(p1);
        m_v0.set(v0);
        m_v1.set(v1);

        m_p1.x -= p0[0];
        m_p1.y -= p0[1];
        m_p1.z -= p0[2];
        m_v0.x -= p0[0];
        m_v0.y -= p0[1];
        m_v0.z -= p0[2];
        m_v1.x -= p0[0];
        m_v1.y -= p0[1];
        m_v1.z -= p0[2];

        m_n0.cross(m_p1, m_v0);

        m_n1.cross(m_p1, m_v1);

        double dot = m_n0.dot(m_n1);

        if (dot < FACE_FLIP_EPSILON) // face flip
            return true;
        else
            return false;
    }

}// class FaceFlipChecker
