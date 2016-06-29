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

package abfab3d.transforms;

import abfab3d.core.ResultCodes;
import abfab3d.param.SNodeParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.Parameter;

import abfab3d.core.Vec;
import abfab3d.core.Initializable;
import abfab3d.core.VecTransform;
import abfab3d.symmetry.SymmetryGroup;
import abfab3d.symmetry.SymmetryGenerator;
import abfab3d.symmetry.Symmetries;

import static abfab3d.core.Output.printf;
import static abfab3d.util.Symmetry.toFundamentalDomain;



/**
   <p>
   Makes transformations for general symmetry group treee dimensions
   </p>
   
   @author Vladimir Bulatov
*/
public class SymmetryTransform  extends BaseTransform implements VecTransform, Initializable  {
    
    static final boolean DEBUG = false;
    
    private SymmetryGroup m_group;

    SNodeParameter mp_symmetryGen = new SNodeParameter("symmetry", "symmetry group to use", new Symmetries.FriezeII(), Symmetries.getFactory());
    IntParameter  mp_iterations = new IntParameter("iterations","max iterations to use to transform into fundamental domain",100);
    //DoubleParameter  mp_riemannSphereRadius = new DoubleParameter("riemannSphereRadius","Riemann Sphere Radius",0.);
    BooleanParameter mp_g1 = new BooleanParameter("g1", true);
    BooleanParameter mp_g2 = new BooleanParameter("g2", true);
    BooleanParameter mp_g3 = new BooleanParameter("g3", true);
    BooleanParameter mp_g4 = new BooleanParameter("g4", true);
    BooleanParameter mp_g5 = new BooleanParameter("g5", true);
    BooleanParameter mp_g6 = new BooleanParameter("g6", true);
    BooleanParameter mp_g7 = new BooleanParameter("g7", true);
    BooleanParameter mp_g8 = new BooleanParameter("g8", true);


    Parameter m_aparam[] = new Parameter[]{
        //mp_splanes,
        mp_symmetryGen,
        mp_iterations,
        //mp_riemannSphereRadius,
        mp_g1,
        mp_g2,
        mp_g3,
        mp_g4,
        mp_g5,
        mp_g6,
        mp_g7,
        mp_g8,
    };
    
    BooleanParameter m_gens[] = {
        mp_g1,
        mp_g2,
        mp_g3,
        mp_g4,
        mp_g5,
        mp_g6,
        mp_g7,
        mp_g8,
    };

    /**
       default symmetry 
     */
    public SymmetryTransform(){            
        super.addParams(m_aparam);
    }

    /**
       creates symmetry transform with given symmetry
     */
    public SymmetryTransform(SymmetryGenerator symmetryGenerator){            
        super.addParams(m_aparam);
        
        mp_symmetryGen.setValue(symmetryGenerator);
    }
    

    public SymmetryGroup getGroup(){
        
        if(m_group == null) 
            initialize();
        return m_group;
    }

    /**
       @noRefGuide
     */
    public int initialize(){
        

        SymmetryGenerator gen = (SymmetryGenerator)mp_symmetryGen.getValue();
        m_group = gen.getSymmetryGroup();
        //TODO enable/disablme Symmetry generators according with active switches 
                
        //m_group.setRiemannSphereRadius(mp_riemannSphereRadius.getValue());
        m_group.setMaxIterations(mp_iterations.getValue());
        return ResultCodes.RESULT_OK;
    }
    
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        // direct transform is identity transform 
        out.set(in);
        // TODO we may use one specific element from the group
        return ResultCodes.RESULT_OK;
        
    }


    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {

        out.set(in);

        int res = m_group.toFD(out);

        return res;
    }

} // class Symmetry 

