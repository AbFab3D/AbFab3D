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


import java.util.Vector;

import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;

import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;

import static abfab3d.util.Units.MM;

/**

   Composition of dta sources according to paper 
   <i>Porter T.,Duff T.(1984) Composing Digital Images.</i>
   
   <p>
   The composition can act as union intersection, difference etc.  
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
public class Composition  extends TransformableDataSource {
    
    
    public static final int  
        A = 1,  // first shape 
        B = 2,  // last shape 
        BoverA = 3, // union of shapes, material of voxel is the last material presented at given voxel
        AoverB = 4, // union of shapes, material of voxel is the first material presented at given voxel 
        AinB = 5, // intersection, material of voxel is the first material presented at given voxel 
        BinA = 6, // intersection, material of voxel is the last material presented at given voxel 
        AoutB = 7, // difference of first shape and all others, material is material of first shape 
        BoutA = 8, // difference of last shape and all others, material is material of last shape 
        AatopB = 9, // last shape, material of voxel is material of first shape presented at the voxel 
        BatopA = 10, // first shape, material of voxel is material of last shape presented oin the voxel 
        AxorB = 11;  // exclusive difference of shapes. 

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
    

    protected int m_type = BoverA;

    Vector<DataSource> dataSources = new Vector<DataSource>();
    
    // fixed vector of data components (to speed up the calculations) 
    DataSource vDataSources[];
    // count of channes of each data components
    int m_channelsCounts[];

    /**
       Create empty composition.
       
     */
    public Composition(int type){
        m_type = type;
    }

    /**
     * Composition of single shape
     */
    public Composition(int type, DataSource shape1){
        m_type = type;
        add(shape1);
    }


    /**
       composition of two shapes 
     */
    public Composition(int type, DataSource shape1, DataSource shape2 ){

        m_type = type;
        add(shape1);
        add(shape2);        
    }

    /**
       composition of three shapes 
     */
    public Composition(int type, DataSource shape1, DataSource shape2, DataSource shape3){

        m_type = type;
        add(shape1);
        add(shape2);        
        add(shape3); 

    }

    /**
       add item to union. 
       @param shape item to add to union of multiple shapes 
    */
    public void add(DataSource shape){
        dataSources.add(shape);
    }
    
    /**
       @noRefGuide
    */
    public int initialize(){

        super.initialize();
        vDataSources = (DataSource[])dataSources.toArray(new DataSource[dataSources.size()]);
        m_channelsCounts = new int[dataSources.size()];

        int ccnt = 0;

        for(int i = 0; i < vDataSources.length; i++){
            
            DataSource ds = vDataSources[i];
            if(ds instanceof Initializable){
                ((Initializable)ds).initialize();
            }
            m_channelsCounts[i] = vDataSources[i].getChannelsCount();
            if(m_channelsCounts[i] > ccnt){
                ccnt = m_channelsCounts[i];
            }
        }
        
        // all channels after first are treated as material channels 
        m_materialChannelsCount = ccnt - 1;
        // density channel 
        m_channelsCount = 1;

        return RESULT_OK;
    }
    
    
    /**
     *  calculates values of all data sources and does digital composition of the input data 
     *  using 
     * can be used to make union of few shapes
       @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

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
        for(int k = 1; k < dataCount; k++){
            
            pnt1.set(pnt);
            DataSource dsb = dss[k];            

            res = dsb.getDataValue(pnt1, dataB);
            // density of component B 

            // premult
            int ccntb = m_channelsCounts[k];

            premult(vb, ccntb);

            compose(va,vb, ccnta, m_type);

        }
                
        // get non premult values 
        unpremult(va, ccnta);
        
        // always success
        return RESULT_OK;
    
    }

    //
    // make premult values 
    // all components are multiplied by fist 
    static final void premult(double v[], int ccnt){
        double d = v[0];
        for(int c = 1; c < ccnt;c++){
            v[c] *= d;
        }
    }

    static final void unpremult(double v[], int ccnt){
        double d = v[0];
        if(d == 0.0) 
            return;
        for(int k = 1; k < ccnt;k++){
            v[k] = clamp(v[k]/d);
        }
    }

    static final double clamp(double v){
        if(v < 0.) return 0.;
        if(v > 1) return 1;
        return v;
    }

    /**
       do the composition of premult values and store reslt in first vector 
     */
    static final void compose(double va[], double vb[], int cnt, int type){

        double Da = va[0];
        double Db = vb[0];

        double fa, fb;
        switch(type){
        default:
        case A:      fa = 1;      fb = 0;    break;                
        case B:      fa = 0;      fb = 1;    break;                
        case BoverA: fa = (1-Db); fb = 1;    break;                
        case AoverB: fa = 1;      fb = 1-Da; break;                
        case AinB:   fa = Db;     fb = 0.;   break;                
        case BinA:   fa = 0;      fb = Da;   break;                
        case AoutB:  fa = (1-Db); fb = 0.;   break;                
        case BoutA:  fa = 0;      fb = 1-Da; break;                
        case AatopB: fa = Db;     fb = 1-Da; break;                
        case BatopA: fa = 1-Db;   fb = Da;   break;                
        case AxorB:  fa = 1-Db;   fb = 1-Da; break;                
        }
        
        for(int c = 0; c < cnt;c++){
            // all components are calculated similarly
            va[c]  = va[c]*fa + vb[c]*fb;
        }
    }


    public static String getTypeName(int type){
        
        switch(type){
        default: return "unknown";
        case A:  return "A";
        case B:  return "B";
        case BoverA: return "BoverA"; 
        case AoverB: return "AoverB";
        case AinB:   return "AinB";
        case BinA:   return "BinA";
        case AoutB:  return "AoutB";
        case BoutA:  return "BoutA"; 
        case AatopB: return "AatopB";
        case BatopA: return "BatopA";
        case AxorB:  return "AxorB";        
        }
    }
} // class Composition