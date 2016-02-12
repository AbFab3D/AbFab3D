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
import abfab3d.util.Symmetry;
import abfab3d.util.Vec;
import abfab3d.util.VecTransform;

import javax.vecmath.Vector4d;

import static abfab3d.util.Symmetry.toFundamentalDomain;


/**
 Makes transformations to reproduce <a href="http://en.wikipedia.org/wiki/Frieze_group">Frieze Symmetry</a> patterns
 <embed src="doc-files/frieze_groups.svg" type="image/svg+xml"/>

 Note: This node is not implemented in ShapeJS 2.0 yet.

 */
public class FriezeSymmetry extends BaseTransform implements VecTransform, Initializable {

    public static final int     // orbifold notation
            FRIEZE_II = 0,   // oo oo
            FRIEZE_IX = 1,   // oo X
            FRIEZE_IS = 2,   // oo *
            FRIEZE_SII = 3,  // * oo oo
            FRIEZE_22I = 4,  // 2 2 oo
            FRIEZE_2SI = 5,  // 2 * oo
            FRIEZE_S22I = 6; // * 2 2 oo


    public int m_maxCount = 100; // maximal number of iterations to gett to FD 
    public double m_domainWidth = 0.01; // width of fundamental domain in meters
    public int m_friezeType;

    // symmettry group to use 
    Symmetry m_sym;

    /**
     @noRefGuide
     */
    public FriezeSymmetry() {

    }

    /**
     Frieze Symmetry wih specified type and domain width
     @param type the symetry type<br/>
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
    public FriezeSymmetry(int type, double domainWidth) {
        m_friezeType = type;
        m_domainWidth = domainWidth;
    }

    /**
     * Set the frieze type
     * @param friezeType
     */
    public void setFriezeType(int friezeType) {
        m_friezeType = friezeType;
    }

    /**
     * Get the frieze type
     * @return
     */
    public int getFriezeType() {
        return m_friezeType;
    }

    /**
     * Get the domain width
     */
    public double getDomainWidth() {
        return m_domainWidth;
    }

    /**
     * Set the domain width
     */
    public void setDomainWidth(double width) {
        m_domainWidth = width;
    }

    /**
     @noRefGuide
     */
    public int initialize() {

        switch (m_friezeType) {
            default:
            case FRIEZE_II:
                m_sym = Symmetry.getII(m_domainWidth);
                break;
            case FRIEZE_S22I:
                m_sym = Symmetry.getS22I(m_domainWidth);
                break;
            case FRIEZE_IS:
                m_sym = Symmetry.getIS(m_domainWidth);
                break;
            case FRIEZE_SII:
                m_sym = Symmetry.getSII(m_domainWidth);
                break;
            case FRIEZE_2SI:
                m_sym = Symmetry.get2SI(m_domainWidth);
                break;
            case FRIEZE_22I:
                m_sym = Symmetry.get22I(m_domainWidth);
                break;
            case FRIEZE_IX:
                m_sym = Symmetry.getIX(m_domainWidth);
                break;
        }

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
        // TODO - garbage generation 
        Vector4d vin = new Vector4d(in.v[0], in.v[1], in.v[2], 1);

        toFundamentalDomain(vin, m_sym, m_maxCount);

        // save result 
        out.v[0] = vin.x;
        out.v[1] = vin.y;
        out.v[2] = vin.z;

        return RESULT_OK;

    }
} // class FriezeSymmetry
