/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.datasources;


import abfab3d.core.ResultCodes;
import abfab3d.param.*;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Vec;

import java.util.List;
import static java.lang.Math.max;
import static abfab3d.core.MathUtil.lerp;
import static abfab3d.core.MathUtil.blendMin;
import static abfab3d.core.MathUtil.blendMax;
import static abfab3d.core.MathUtil.step10;

/**

 Composition of data sources according to paper
 <i>Porter T.,Duff T.(1984) Composing Digital Images.</i>

 <p>
 The composition can act as union, intersection, difference etc.
 The value of the first data channel is assumed to be voxel fill density.
 It plays the same role as alpha channel in image composing.
 Other (materials) channels values are calculated using formulas from the image composing for RGB color component.
 </p>
 <p>
 Important difference. We are NOT using premultipled values of the channels.
 Premult values can be used in internal compitations for efficiency.
 </p>
 <p>
 When more than 2 shapes are composed the operatipon acts sequentally on the list of
 supplied shapes, using result of previous composition as input for next operation
 </p>

 <pre>

 va, vb - premult densities of input shapes
 vc - density of Composition shape
 Ma, Mb - material channels values of input shapes
 Mc material channel values of Composition shape.

 vc = fa*va + fb*vb

 </pre>

 @author Vladimir Bulatov

 */
public class Composition extends TransformableDataSource {


    public static final int
            A = 1,  // shape A
            B = 2,  // shape B
            BoverA = 3,  //  B U (A-B)
            AoverB = 4,  //  A U (B-A)
            AinB = 5,    // (A I B) material A
            BinA = 6,    // (A I B) material B
            AoutB = 7,   // A - B  material A
            BoutA = 8,   // B-A  material B
            AatopB = 9,  // (B-A) U AinB
            BatopA = 10, // (A-B) U BinA
            AxorB = 11;  // (A-B) U (B-A)
    //
    // U = union
    // I = intersection 


    public static final int[] allTypes = new int[]{
            A,
            B,
            BoverA,
            AoverB,
            AinB,
            BinA,
            AoutB,
            BoutA,
            AatopB,
            BatopA,
            AxorB,
    };


    IntParameter mp_type = new IntParameter("type", "type of composition", BoverA);
    DoubleParameter mp_blend = new DoubleParameter("blend", "blend width", 0.);
    SNodeListParameter mp_sources = new SNodeListParameter("sources");

    Parameter m_aparam[] = new Parameter[]{
            mp_blend,
            mp_type,
            mp_sources
    };

    protected int m_type = 0;
    protected int m_count = 0;
    protected double m_blend;

    // fixed vector of data components (to speed up the calculations) 
    DataSource vDataSources[];

    // count of channels of each data components
    int m_channelsCounts[];

    /**
     Create empty composition.

     */
    public Composition(int type) {

        addParams(m_aparam);
        mp_type.setValue(type);

    }

    /**
     * Composition of single shape
     */
    public Composition(int type, DataSource shape1) {

        addParams(m_aparam);

        mp_type.setValue(type);
        add(shape1);
    }


    /**
     composition of two shapes
     */
    public Composition(int type, DataSource shape1, DataSource shape2) {

        addParams(m_aparam);

        mp_type.setValue(type);
        add(shape1);
        add(shape2);
    }

    /**
     composition of three shapes
     */
    public Composition(int type, DataSource shape1, DataSource shape2, DataSource shape3) {

        addParams(m_aparam);

        mp_type.setValue(type);
        add(shape1);
        add(shape2);
        add(shape3);

    }

    /**
     composition of four shapes
     */
    public Composition(int type, DataSource shape1, DataSource shape2, DataSource shape3, DataSource shape4) {

        addParams(m_aparam);

        mp_type.setValue(type);
        add(shape1);
        add(shape2);
        add(shape3);
        add(shape4);

    }

    /**
     set composition type
     */
    public void setType(int value) {
        mp_type.setValue(value);
    }

    /**
     add item to union.
     @param source item to add to union of multiple shapes
     */
    public void add(DataSource source) {

        mp_sources.add((Parameterizable) source);

    }

    /**
     * Set the blending width
     *
     * @param val The value in meters
     */
    public void setBlend(double val) {
        mp_blend.setValue(val);
    }

    /**
     * Get the blending width
     * @return
     */
    public double getBlend() {
        return mp_blend.getValue();
    }

    /**
     * Set an item into the list
     *
     * @param idx The index, it must already exist
     * @param src
     */
    public void set(int idx, DataSource src) {
        mp_sources.set(idx, (Parameterizable) src);
    }

    /**
     * Clear the datasources
     */
    public void clear() {
        mp_sources.clear();
    }

    /**
     @noRefGuide
     */
    public int initialize() {

        super.initialize();

        List sources = mp_sources.getValue();
        m_count = sources.size();
        vDataSources = new DataSource[m_count];
        for (int i = 0; i < m_count; i++) {
            vDataSources[i] = (DataSource) sources.get(i);
        }

        m_type = mp_type.getValue();

        m_channelsCounts = new int[m_count];
        m_blend = mp_blend.getValue();

        int ccnt = 0;

        for (int i = 0; i < m_count; i++) {

            DataSource ds = vDataSources[i];
            if (ds instanceof Initializable) {
                ((Initializable) ds).initialize();
            }
            m_channelsCounts[i] = vDataSources[i].getChannelsCount();
            ccnt = max(ccnt, m_channelsCounts[i]);
        }

        // all channels after first are considered material channels 
        m_materialChannelsCount = ccnt - 1;
        // density channel 
        m_channelsCount = 1;

        // what are bounds of composition? 
        m_bounds =  vDataSources[0].getBounds();

        return ResultCodes.RESULT_OK;
    }


    /**
     *  
     *  
     @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec data) {

        //return getDensityData(pnt, data);
        return getDistanceData(pnt, data);
    }

    /**
     *  
     *  
     @noRefGuide
     */
    public int getDistanceData(Vec pnt, Vec data) {
        
        
        int count = vDataSources.length;
        DataSource dss[] = vDataSources;

        Vec pnt1 = new Vec(pnt);
        Vec data1 = new Vec(data);
        Vec data2 = new Vec(data);
        Vec data3 = new Vec(data);

        dss[0].getDataValue(pnt1, data1);

        for(int k = 1; k < count; k++){

            Vec pnt2 = new Vec(pnt);
            dss[k].getDataValue(pnt2, data2);
            composeDistanceData(data1,data2, data3);
            // transfer result back to data1 
            data1.set(data3);
        }

        // transfer result to output 
        data.set(data1);
        
        return ResultCodes.RESULT_OK;
    }
    
    
    /**
       compose distance data from two data values and store result into dataResult
     */
    private void composeDistanceData(Vec inA,Vec inB, Vec out){
        
        double a = inA.v[0];
        double b = inB.v[0];
        double ca[] = inA.v;
        double cb[] = inB.v;
        
        switch(m_type){
        default:
        case A: 
            out.set(inA);
            break;
        case B:     
            out.set(inB);
            break;        
        case BoverA:
            out.v[0] = blendMin(a, b, m_blend);
            lerp(ca, cb, max(step10(b,a,m_blend), step10(b,0,m_blend)), 1, out.v);
            break;
            /*   
        case COMP_AoverB:Te
        out->v.x = blendMin(a, b, blend);
        //out->v.yzw = mix(ca, cb, max(step01(a,b,blend),step01(a,0,blend)));
        out->v.yzw = mix(ca, cb, min(step01(a,b,blend),step01(a,0,blend)));
        return;

    case COMP_AinB:
        out->v.x = blendMax(a, b, blend);
        out->v.yzw = inA->v.yzw;
        return;

    case COMP_BinA:
        out->v.x = blendMax(a, b, blend);
        out->v.yzw = cb;
        return;

    case COMP_AoutB:
        out->v.x = blendMax(a,-b, blend);
        out->v.yzw = ca;
        return;

    case COMP_BoutA:
        out->v.x = blendMax(-a, b, blend);
        out->v.yzw = cb;
        return;
        
    case COMP_AatopB:
        out->v.x = b;
        out->v.yzw = mix(cb, ca, step10(a,0,blend));
        return;

    case COMP_BatopA:
        out->v.x = a;
        out->v.yzw = mix(ca, cb, step10(b,0,blend));
        return;

    case COMP_AxorB:
        out->v.x = min(blendMax(a,-b, blend), blendMax(-a, b, blend));
        out->v.yzw = mix(ca, cb, step01(a,b,blend));
        return;
    }

            */
        }
    }

    /**
     *  
     *  
     *  obsolete 
     @noRefGuide
     */
    public int getDensityData(Vec pnt, Vec data) {

        int dataCount = vDataSources.length;
        DataSource dss[] = vDataSources;
        //TODO - garbage generation 

        Vec pnt1 = new Vec(pnt); // transformed point 
        Vec dataB = new Vec(data);

        // density of component A 
        double Da = 0.; // initially empty 

        // arrays of data values 
        double va[] = data.v;
        double vb[] = dataB.v;

        pnt1.set(pnt);

        int res = dss[0].getDataValue(pnt1, data);

        Da = va[0];

        // premult first data values
        int ccnta = m_channelsCounts[0];
        premult(va, ccnta);

        //
        // compose with the remaining shapes 
        //
        for (int k = 1; k < dataCount; k++) {

            pnt1.set(pnt);
            DataSource dsb = dss[k];

            res = dsb.getDataValue(pnt1, dataB);
            // density of component B 

            // premult
            int ccntb = m_channelsCounts[k];

            premult(vb, ccntb);

            compose(va, vb, ccnta, m_type);

        }

        // get non premult values 
        unpremult(va, ccnta);

        // always success
        return ResultCodes.RESULT_OK;

    }

    //
    // make premult values 
    // all components are multiplied by first 
    static final void premult(double v[], int ccnt) {
        double d = v[0];
        for (int c = 1; c < ccnt; c++) {
            v[c] *= d;
        }
    }

    static final void unpremult(double v[], int ccnt) {
        double d = v[0];
        if (d == 0.0)
            return;
        for (int k = 1; k < ccnt; k++) {
            v[k] = clamp(v[k] / d);
        }
    }

    static final double clamp(double v) {
        if (v < 0.) return 0.;
        if (v > 1) return 1;
        return v;
    }

    /**
     do the composition of premult values and store result in the first vector
     */
    static final void compose(double va[], double vb[], int cnt, int type) {

        // density of components 
        double Da = va[0];
        double Db = vb[0];

        double fa, fb;
        switch (type) {
            default:
            case A:
                fa = 1;
                fb = 0;
                break;
            case B:
                fa = 0;
                fb = 1;
                break;
            case BoverA:
                fa = (1 - Db);
                fb = 1;
                break;
            case AoverB:
                fa = 1;
                fb = 1 - Da;
                break;
            case AinB:
                fa = Db;
                fb = 0.;
                break;
            case BinA:
                fa = 0;
                fb = Da;
                break;
            case AoutB:
                fa = (1 - Db);
                fb = 0.;
                break;
            case BoutA:
                fa = 0;
                fb = 1 - Da;
                break;
            case AatopB:
                fa = Db;
                fb = 1 - Da;
                break;
            case BatopA:
                fa = 1 - Db;
                fb = Da;
                break;
            case AxorB:
                fa = 1 - Db;
                fb = 1 - Da;
                break;
        }

        for (int c = 0; c < cnt; c++) {
            // all components are calculated similarly
            va[c] = va[c] * fa + vb[c] * fb;
        }
    }


    public static String getTypeName(int type) {

        switch (type) {
            default:
                return "unknown";
            case A:
                return "A";
            case B:
                return "B";
            case BoverA:
                return "BoverA";
            case AoverB:
                return "AoverB";
            case AinB:
                return "AinB";
            case BinA:
                return "BinA";
            case AoutB:
                return "AoutB";
            case BoutA:
                return "BoutA";
            case AatopB:
                return "AatopB";
            case BatopA:
                return "BatopA";
            case AxorB:
                return "AxorB";
        }
    }
} // class Composition