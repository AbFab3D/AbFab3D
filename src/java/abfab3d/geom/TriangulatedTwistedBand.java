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

package abfab3d.geom;

// External Imports
import java.util.*;


import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.core.Vec;
import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.Transformer;
import abfab3d.core.Initializable;
import abfab3d.core.ResultCodes;
import abfab3d.core.VecTransform;


import abfab3d.util.TriangleTransformer;


import abfab3d.transforms.Identity;
import abfab3d.transforms.CompositeTransform;
import abfab3d.transforms.Twist;
import abfab3d.transforms.Translation;
import abfab3d.transforms.Rotation;
import abfab3d.transforms.RingWrap;

import static abfab3d.core.MathUtil.distance;
import static abfab3d.core.MathUtil.midPoint;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;

import static java.lang.Math.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.ceil;


/**
 *  triangulated model 
 *
 *  twisted band of given width and thickness
 * 
 * @author Vladimir Bulatov
 */
public class TriangulatedTwistedBand implements TriangleProducer{

    static final boolean DEBUG = true;

    double m_radius;
    double m_bandWidth;
    double m_bandThickness;
    double m_twist;
    double m_spin;
    Vector3d m_center;
    double m_cellSize;

    VecTransform m_transform;

    public TriangulatedTwistedBand(double radius, 
                                   double bandWidth, 
                                   double bandThickness, 
                                   double twist, 
                                   double spin, 
                                   Vector3d center, 
                                   double cellSize){
        m_radius = radius;
        m_bandWidth = bandWidth;
        m_bandThickness = bandThickness;
        m_twist = twist;
        m_spin = spin;
        m_center = new Vector3d(center);
        m_cellSize = cellSize;       
        
    }
    
    
    public boolean getTriangles(TriangleCollector tc){
        
        int nu = max(1,(int)ceil(m_bandWidth/m_cellSize));
        int nv = max(1,(int)ceil(m_bandThickness/m_cellSize));
        double bandLength = 2*PI*m_spin*m_radius;

        int nt = max(4,(int)ceil((bandLength)/m_cellSize));

        double du = m_bandWidth/nu;
        double dv = m_bandThickness/nv;
        double dt = bandLength/nt;
        double w = m_bandWidth/2;
        double th = m_bandThickness/2;
        CompositeTransform transform = new CompositeTransform();
        if(m_twist != 0.0) transform.add(new Twist(bandLength/m_twist));
        transform.add(new Rotation(0,1,0,PI/2));
        transform.add(new RingWrap(m_radius));
        transform.add(new Translation(m_center));

        transform.initialize();

        m_transform = transform;

        if(DEBUG)printf("nu: %d, nv: %d, nt:%d\n", nu, nv, nt);

        Vector3d 
            v000 = new Vector3d(-th, -w, 0),
            v100 = new Vector3d(th, -w, 0),
            v110 = new Vector3d(th, w, 0),
            v010 = new Vector3d(-th, w, 0),
            v001 = new Vector3d(-th, -w, bandLength),
            v101 = new Vector3d(th, -w, bandLength),
            v111 = new Vector3d(th, w, bandLength),
            v011 = new Vector3d(-th, w, bandLength);


        addPatch(tc, new ParametricSurfaces.Patch(v100, v110, v111, v101, nu, nt));
        addPatch(tc, new ParametricSurfaces.Patch(v010, v000, v001, v011, nu, nt));
        addPatch(tc, new ParametricSurfaces.Patch(v000, v100, v101, v001, nv, nt));
        addPatch(tc, new ParametricSurfaces.Patch(v110, v010, v011, v111, nv, nt));
        return true;            
        
    }
    

    void addPatch(TriangleCollector tc, ParametricSurface ps){

        ParametricSurfaceMaker pm = new ParametricSurfaceMaker(ps);

        TriangleTransformer tt = new TriangleTransformer(pm);
        tt.setTransform(m_transform);
        tt.getTriangles(tc);

        //pm.getTriangles(tc);
        
    }

} // TriangulatedTwistedBand

