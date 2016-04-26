/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2011
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.transforms;

import abfab3d.util.Initializable;

import abfab3d.param.DoubleParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.Parameter;


import abfab3d.symmetry.SymmetryGroup;
import abfab3d.symmetry.FriezeSymmetries;
import abfab3d.util.Vec;
import abfab3d.util.VecTransform;

import javax.vecmath.Vector4d;

import static abfab3d.util.Symmetry.toFundamentalDomain;


/**
 Makes transformations to reproduce <a href="http://en.wikipedia.org/wiki/Frieze_group">Frieze Symmetry</a> patterns
 <embed src="doc-files/frieze_groups.svg" type="image/svg+xml"/>

 Note: This node is not implemented in ShapeJS 2.0 yet.

 */
public class FriezeSymmetry extends BaseTransform implements Initializable {

    protected DoubleParameter  mp_domainWidth = new DoubleParameter("domainWidth","width of fundamental domain",0.01);
    protected DoubleParameter  mp_domainHeight = new DoubleParameter("domainHeight","height of fundamental domain",0.01);
    protected DoubleParameter  mp_domainSkew = new DoubleParameter("domainSkew","skew of fundamental domain for symmetry O",0.0);
    protected IntParameter  mp_maxCount = new IntParameter("maxCount","max count of iteratioins to get to fundamental domain",100);
    protected EnumParameter  mp_symmetryType = new EnumParameter("symmetryType","type of walpaper symetry",SymmetryNames, SymmetryNames[FRIEZE_II]);

    Parameter aparam[] = new Parameter[]{
        mp_domainWidth,
        mp_domainHeight,
        mp_domainSkew,
        mp_maxCount, 
        mp_symmetryType,       
    };

    static final public String SymmetryNames[] = new String[]{
        "II",
        "IX",
        "IS",
        "SII",
        "22I",
        "2SI",
        "S22I",
    };

    public static final int     // orbifold notation
        FRIEZE_II = 0,   // oo oo
            FRIEZE_IX = 1,   // oo X
            FRIEZE_IS = 2,   // oo *
            FRIEZE_SII = 3,  // * oo oo
            FRIEZE_22I = 4,  // 2 2 oo
            FRIEZE_2SI = 5,  // 2 * oo
            FRIEZE_S22I = 6; // * 2 2 oo

    //public int m_maxCount = 100; // maximal number of iterations to gett to FD 
    //public double m_domainWidth = 0.01; // width of fundamental domain in meters
    //public int m_friezeType;

    // symmettry group to use 
    SymmetryGroup m_group;

    /**
     @noRefGuide
     */
    public FriezeSymmetry() {
        super.addParams(aparam);
    }

    /**
     Frieze Symmetry wih specified type and domain width
     @param symmetryType the symetry type<br/>
     Possible values are
     <ul>
     <li>FriezeSymetry.FRIEZE_II</li>
     <li>FriezeSymetry.FRIEZE_IX</li>
     <li>FriezeSymetry.FRIEZE_IS</li>
     <li>FriezeSymetry.FRIEZE_SII</li>
     <li>FriezeSymetry.FRIEZE_22I</li>
     <li>FriezeSymetry.FRIEZE_2SI</li>
     <li>FriezeSymetry.FRIEZE_S22I</li>
     </ul>
     @param domainWidth width of the fundamental domain
     */
    public FriezeSymmetry(int symmetryType, double domainWidth) {
        super.addParams(aparam);
        mp_symmetryType.setSelectedIndex(symmetryType);
        mp_domainWidth.setValue(domainWidth);
    }

    /**
     * Set the frieze type
     * @param friezeType type of symmetry 
     */
    public void setSymmetryType(int symmetryType) {
        mp_symmetryType.setSelectedIndex(symmetryType);
    }

    public void setSymmetryType(String symmetryTypeName){
        mp_symmetryType.setValue(symmetryTypeName);
    }

    /**
       @param width width of fundamental domain. 
     */
    public void setDomainWidth(double width){
        mp_domainWidth.setValue(width);
    }

    public void setMaxCount(int maxCount){

        mp_maxCount.setValue(maxCount);

    }

    /**
     @noRefGuide
     */
    public int initialize() {

        int symmetryType = mp_symmetryType.getIndex();
        double domainWidth = mp_domainWidth.getValue();
        int maxCount = mp_maxCount.getValue();

        switch (symmetryType) {
            default:
            case FRIEZE_II:
                m_group = FriezeSymmetries.getII(domainWidth);
                break;
            case FRIEZE_S22I:
                m_group = FriezeSymmetries.getS22I(domainWidth);
                break;
            case FRIEZE_IS:
                m_group = FriezeSymmetries.getIS(domainWidth);
                break;
            case FRIEZE_SII:
                m_group = FriezeSymmetries.getSII(domainWidth);
                break;
            case FRIEZE_2SI:
                m_group = FriezeSymmetries.get2SI(domainWidth);
                break;
            case FRIEZE_22I:
                m_group = FriezeSymmetries.get22I(domainWidth);
                break;
            case FRIEZE_IX:
                m_group = FriezeSymmetries.getIX(domainWidth);
                break;
        }

        m_group.setMaxIterations(maxCount);

        return RESULT_OK;

    }


    /**
     @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        out.set(in);
        // this is one  to many transform 
        // it only makes sence for inverse transform 
        // so we apply only identity transform to the input 
        double x = in.v[0];
        double y = in.v[1];
        double z = in.v[2];

        out.v[0] = x;
        out.v[1] = y;
        out.v[2] = z;

        return RESULT_OK;
    }

    /**
     @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        
        out.set(in);
        return m_group.toFD(out);        
    }
} // class FriezeSymmetry
