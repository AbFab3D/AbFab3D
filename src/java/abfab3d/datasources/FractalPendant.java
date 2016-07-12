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

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import abfab3d.core.Bounds;
import abfab3d.core.DataSource;


import abfab3d.datasources.Plane;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Union;
import abfab3d.datasources.Intersection;

import abfab3d.transforms.CompositeTransform;
import abfab3d.transforms.ReflectionSymmetry;
import abfab3d.transforms.ReflectionSymmetries;
import abfab3d.transforms.SphereInversion;
import abfab3d.transforms.Scale;


import abfab3d.util.ShapeProducer;
import abfab3d.util.SymmetryGenerator;
import abfab3d.util.ReflectionGroup;

import abfab3d.param.Vector3dParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.Parameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.Parameterizable;


import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.log;
import static java.lang.Math.abs;
import static java.lang.Math.exp;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;

/**
   makes fractal pendant using as ShapeProducer 
 */
public class FractalPendant extends BaseParameterizable implements ShapeProducer {
    
    final boolean DEBUG = false;
    static final String sm_symNames[] = new String[]{"squad", "rect", "hex", "tri", "dihedral"};

    EnumParameter m_symmetry = new EnumParameter("symmetry",sm_symNames, sm_symNames[0]);
    BooleanParameter m_hasTorus = new BooleanParameter("hasTorus",true);

    Vector3dParameter m_torusAxis = new Vector3dParameter("torusAxis",new Vector3d(-1,-1,1));
    Vector3dParameter m_torusCenter = new Vector3dParameter("torusCenter",new Vector3d(2.4*MM, 1.4*MM,0));
    DoubleParameter m_torusRin = new DoubleParameter("torusRin",1*MM);
    DoubleParameter m_torusRout = new DoubleParameter("torusRout",1.8*MM);
    Vector3dParameter m_torusColor = new Vector3dParameter("torusColor",new Vector3d(1,1,1));

    BooleanParameter m_hasSphere = new BooleanParameter("hasSphere",false);
    Vector3dParameter m_sphereCenter = new Vector3dParameter("sphereCenter",new Vector3d(0,0,0));
    DoubleParameter m_sphereRadius = new DoubleParameter("sphereRadius",1*MM);

    DoubleParameter m_rectWidth = new DoubleParameter("rectWith",5*MM);
    DoubleParameter m_rectHeight = new DoubleParameter("rectHeight",5*MM);
    DoubleParameter m_c3 = new DoubleParameter("center3",10.9*MM);
    DoubleParameter m_c4 = new DoubleParameter("center4",20.0*MM);
    DoubleParameter m_r3 = new DoubleParameter("radius3",6.7*MM);
    DoubleParameter m_hTrans = new DoubleParameter("hTrans",0.);
    DoubleParameter m_distOffset = new DoubleParameter("distOffset",-0.00025);
    IntParameter m_n12 = new IntParameter("n12",3);
    BooleanParameter m_g1 = new BooleanParameter("gen1",true);
    BooleanParameter m_g2 = new BooleanParameter("gen2",true);
    BooleanParameter m_g3 = new BooleanParameter("gen3",true);
    BooleanParameter m_g4 = new BooleanParameter("gen4",true);
    BooleanParameter m_useSymmetry = new BooleanParameter("useSymmetry",true);
    BooleanParameter m_showGens = new BooleanParameter("showGens",false);
    DoubleParameter m_genWidth = new DoubleParameter("genWidth",0.2*MM);
    DoubleParameter m_genThickness = new DoubleParameter("genThickness",1*MM);
    Vector3dParameter m_genColor = new Vector3dParameter("genColor",new Vector3d(0.3,0.3,0.7));
    
    Parameter aparams[] = new Parameter[]{
        m_symmetry,
        m_c3,
        m_r3,
        m_c4,
        m_n12,
        m_rectWidth,
        m_rectHeight,
        m_g1, 
        m_g2, 
        m_g3, 
        m_g4, 
        m_useSymmetry,
        m_showGens,

        m_hasTorus,
        m_torusRin, 
        m_torusRout, 
        m_torusCenter,
        m_torusAxis,
        
        m_hasSphere,
        m_sphereRadius,
        m_sphereCenter,

        
        m_hTrans,
        m_distOffset,
        m_torusColor,
        m_genColor,
        m_genWidth, 
        m_genThickness, 

        
    };

    BooleanParameter m_gens[] = new BooleanParameter[] {
        m_g1,
        m_g2,
        m_g3,
        m_g4
    };    
    
    public FractalPendant(){
        addParams(aparams);
    }

    
    protected SymmetryGenerator getSymmetry(){

        double c3 = m_c3.getValue();
        double c4 = m_c4.getValue();
        double r3 = m_r3.getValue();
        int n12 = m_n12.getValue();
        double a12 = Math.PI/n12;

        switch(m_symmetry.getIndex()){
        default:
        case 0:
            return new ReflectionSymmetries.Quad1(n12, c3, r3, c4); 
        case 1: 
            {
                ReflectionGroup.SPlane[] rect = new ReflectionGroup.SPlane[4];
                rect[0] = new ReflectionGroup.Plane(new Vector3d(-1,0,0), 0.);
                rect[1] = new ReflectionGroup.Plane(new Vector3d(0, -1, 0), 0.);
                rect[2] = new ReflectionGroup.Plane(new Vector3d(1,0,0), m_rectWidth.getValue());
                rect[3] = new ReflectionGroup.Plane(new Vector3d(0,1,0), m_rectHeight.getValue());
                return new ReflectionSymmetries.General(rect); 
            }
        case 2:
            {
                double a = Math.PI/3;
                ReflectionGroup.SPlane[] planes = new ReflectionGroup.SPlane[3];
                planes[0] = new ReflectionGroup.Plane(new Vector3d(0, -1, 0), 0.);
                planes[1] = new ReflectionGroup.Plane(new Vector3d(-sin(a),cos(a),0), 0);
                planes[2] = new ReflectionGroup.Plane(new Vector3d(1,0,0), m_rectWidth.getValue());
                return new ReflectionSymmetries.General(planes); 
            }
        case 3:
            {
                double a = Math.PI/3;
                ReflectionGroup.SPlane[] planes = new ReflectionGroup.SPlane[3];
                planes[0] = new ReflectionGroup.Plane(new Vector3d(0, -1, 0), 0.);
                planes[1] = new ReflectionGroup.Plane(new Vector3d(-sin(a),cos(a),0), 0);
                planes[2] = new ReflectionGroup.Plane(new Vector3d(sin(a),cos(a),0), m_rectWidth.getValue()*sin(a));
                return new ReflectionSymmetries.General(planes); 
            }
        case 4:
            {
                double a = a12;
                ReflectionGroup.SPlane[] planes = new ReflectionGroup.SPlane[2];
                planes[0] = new ReflectionGroup.Plane(new Vector3d(0, -1, 0), 0.);
                planes[1] = new ReflectionGroup.Plane(new Vector3d(-sin(a),cos(a),0), 0);
                return new ReflectionSymmetries.General(planes); 
            }
        }
    }

    public DataSource getShape(){
                
        double c3 = m_c3.getValue();
        double c4 = m_c4.getValue();
        double r3 = m_r3.getValue();
        int n12 = m_n12.getValue();
        double a12 = Math.PI/n12;
        
        double r4 = sqrt(c3*c3 + c4*c4 - 2*c3*c4*cos(a12) - r3*r3);
        SymmetryGenerator symGen = getSymmetry();
        ReflectionSymmetry symm = new ReflectionSymmetry(symGen);
        symm.set("riemannSphereRadius", 30*MM);
        symm.set("iterations", 1000);
        symm.set("g1", m_g1.getValue());
        symm.set("g2", m_g2.getValue());
        symm.set("g3", m_g3.getValue());
        symm.set("g4", m_g4.getValue());
        
        CompositeTransform hyperTrans = null;
        double fp3 = c3;
        double fp4 = -c4;

        if(m_symmetry.getIndex() == 0){
            double d34 = c3+c4;
            double do3 = ((r3*r3 - r4*r4) + d34*d34)/(2*d34); // distance of orthogonal sphere to C3 
            double ro3 = sqrt(do3*do3 - r3*r3); // radius of orthogonal sphere
            
            fp3 = c3 - do3 + ro3; // x-coord of fixed point inside of c3 
            fp4 = fp3-2*ro3; // x-coord of fixed point inside of reflected c4
            if(DEBUG){
                printf("r3:%9.5f mm\n", r3/MM);
                printf("c3:%9.5f mm\n", c3/MM);
                printf("c4:%9.5f mm\n", c4/MM);
                printf("r4:%9.5f mm\n", r4/MM);            
                printf("fp3:%9.5f mm\n", fp3/MM);
                printf("fp4:%9.5f mm\n", fp4/MM);
            }
            // inversion which moves fp4 -> infinity 
            // and fp3 - fixed             
            double xi = fp4;
            double ri = 2*ro3;
            if(DEBUG)printf("xi:%9.5f mm\n", xi/MM);
            if(DEBUG)printf("ri:%9.5f mm\n", ri/MM);
            double p2 = 0;
            double p3 = (-c4-r4);//c3-r3;
            if(DEBUG)printf("p2:%9.5f mm\n", p2/MM);
            if(DEBUG)printf("p3:%9.5f mm\n", p3/MM);
            double p2i =  reflect(p2, xi, ri);
            double p3i =  reflect(p3, xi, ri);
            if(DEBUG)printf("p2i:%9.5f mm\n", p2i/MM);
            if(DEBUG)printf("p3i:%9.5f mm\n", p3i/MM);
            
            double ratio = (p2i - fp3)/(p3i-fp3);
            
            if(DEBUG)printf("ratio:%9.5f\n", ratio);
            if(DEBUG)printf("log ratio:%9.5f\n", log(abs(ratio)));
            
            SphereInversion inversion = new SphereInversion(new Vector3d(fp4,0,0), ri);
            Scale scale = new Scale(exp(m_hTrans.getValue()));
            scale.set("center", new Vector3d(fp3, 0,0));
            if(m_hTrans.getValue() != 0.){
                hyperTrans = new CompositeTransform();
                hyperTrans.add(inversion);
                hyperTrans.add(scale);
                hyperTrans.add(inversion);
            }
        }

        // shapes in the fundamental domain 
        Union fdShapes = new Union();

        if(m_useSymmetry.getValue())
            fdShapes.setTransform(symm);

        if(m_hasTorus.getValue()){
            fdShapes.add(new Torus(m_torusCenter.getValue(), 
                                   m_torusAxis.getValue(),
                                   m_torusRout.getValue(),
                                   m_torusRin.getValue()+m_distOffset.getValue()));
        }
        if(m_hasSphere.getValue()){
            double sr = abs(fp3 - fp4)/2;
            double sx = (fp3 + fp4)/2;
            double sz = m_sphereCenter.getValue().z;
            double sy = m_sphereCenter.getValue().y;
            double sR = sqrt(sr*sr + sz*sz+sy*sy);
            Sphere s1 = new Sphere(new Vector3d(sx, sy, sz), sR);
            Sphere s2 = new Sphere(new Vector3d(sx, sy, -sz), sR);

            Intersection inter = new Intersection(s1, s2);
            fdShapes.add(inter);

        }

        if(hyperTrans != null)fdShapes.addTransform(hyperTrans);
        
        TransformableDataSource thickShape = new Add(fdShapes, new Constant(m_distOffset.getValue()));

        thickShape = new DataSourceMixer(thickShape, getColor(m_torusColor.getValue())); 
        TransformableDataSource shape;
        if(m_showGens.getValue()) {
            TransformableDataSource gens = getGeneratorsShape(symGen.getFundamentalDomain());
            if(hyperTrans != null) {
                gens.setTransform(hyperTrans);
            }
            gens = new DataSourceMixer(gens, getColor(m_genColor.getValue()));            
            //shape = new Composition(Composition.BoverA, thickShape, gens);
            shape = new Union(thickShape, gens);
        } else {
            shape = thickShape;
        }
        // show fixed points 
        //fdShapes.add(new Sphere(new Vector3d(fp3,0,0), 1*MM));
        //fdShapes.add(new Sphere(new Vector3d(fp4,0,0), 0.5*MM));
        
        return shape;
    }
    

    public TransformableDataSource getGeneratorsShape(ReflectionGroup.SPlane[] splanes){
        Union union = new Union();
        Constant widthOffset = new Constant(-m_genWidth.getValue()/2);
        for(int i = 0; i < splanes.length; i++){
            if(m_gens[i].getValue()){
                ReflectionGroup.SPlane sp = splanes[i];
                if(sp instanceof ReflectionGroup.Plane){
                    ReflectionGroup.Plane p = (ReflectionGroup.Plane)sp;
                    union.add(new Add(new Abs(new Plane(p.nx,p.ny,p.nz,p.dist)), widthOffset));
                } else if(sp instanceof ReflectionGroup.Sphere){
                    ReflectionGroup.Sphere s = (ReflectionGroup.Sphere)sp;
                    union.add(new Add(new Abs(new Sphere(s.cx,s.cy,s.cz,s.r)), widthOffset));                    
                }
            }
        }

        Intersection inter = new Intersection(union, new Add(new Abs(new Plane(0,0,1,0)),new Constant(-0.5*m_genThickness.getValue())));
        inter.set("blend", m_genWidth.getValue()/2);
        return inter;
    }


    DataSource getColor(Vector3d c){
        // something wrong with color
        return new Constant(c.x, c.y, c.z);
    }

    double reflect(double x, double cx, double r){
        
        return r*r/(x-cx) + cx;
        
    }
}
