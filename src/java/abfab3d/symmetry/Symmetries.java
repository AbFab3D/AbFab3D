/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.symmetry;

import javax.vecmath.Vector3d;

import abfab3d.param.Parameterizable;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeFactory;
import abfab3d.param.BaseSNodeFactory;



/**
   a collection of classes which generate specific symmetry groups
   each class implement SymmetryGenerator interface and can be used as parameter in the SymmetryTransform
   each class also impelements Parameterizable interface and can be controled via it's parameters  
 */

public class Symmetries {
    

    static String sm_symNames[] = new String[]{
        "frieze(oo_oo)", 
    };
    static String sm_symClasses[] = new String[]{
        "abfab3d.symmetry.Symmetries$FriezeII",
    };

    static SNodeFactory sm_factory;
    
    public static SNodeFactory getFactory(){
        if(sm_factory == null)
            sm_factory = new BaseSNodeFactory(sm_symNames, sm_symClasses);
        return sm_factory;
    }

    public static abstract class BaseFriezeSymmetry extends BaseParameterizable implements SymmetryGenerator {
        protected DoubleParameter m_domainWidth = new DoubleParameter("width", 1);
        Parameter[] aparam = new Parameter[]{
            m_domainWidth
        };
        
        BaseFriezeSymmetry(){
            super.addParams(aparam);
        }
        
    }

    /**
       frieze group INF INF consisting of pure translations
       domain has given width and is centered at origin 
     */
    public static class FriezeII extends BaseFriezeSymmetry {
        public FriezeII(){
        }
        public FriezeII(double domainWidth){
            m_domainWidth.setValue(domainWidth);
        }

        public SymmetryGroup getSymmetryGroup(){
            double width = m_domainWidth.getValue();
            SPlane planes[] = new SPlane[]{
                new Plane(new Vector3d(1,0,0),width/2),
                new Plane(new Vector3d(-1,0,0),width/2)
            };
            ETransform trans[] = new ETransform[]{
                new ETransform(ETransform.getTranslationMatrix(width,0,0)),
                new ETransform(ETransform.getTranslationMatrix(-width,0,0)),
            };
            return new SymmetryGroup(planes, trans);
        }
    }// class FriezeII
}