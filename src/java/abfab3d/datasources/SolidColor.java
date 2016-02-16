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

import abfab3d.util.Bounds;
import abfab3d.util.Vec;
import abfab3d.util.DataSource;

import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;


/**
   represent solid color 
 */
public class SolidColor  extends TransformableDataSource {

    // color components 
    private DoubleParameter  mp_r = new DoubleParameter("red","red component", 1.);
    private DoubleParameter  mp_b = new DoubleParameter("blue","blue component", 0.);
    private DoubleParameter  mp_g = new DoubleParameter("green","green component", 0.);
    private DoubleParameter  mp_a = new DoubleParameter("alpha","alpha component", 1);

    protected double m_r;
    protected double m_g;
    protected double m_b;
    protected double m_a;
    protected int m_channels = 0;

    protected Bounds m_bounds;
    
    Parameter m_aparam[] = new Parameter[]{
        mp_r,
        mp_g,
        mp_b,
        mp_a
    };
    /**
       solid color with red green blue components 
    */
    public SolidColor(double r, double g, double b) {

        super.addParams(m_aparam);
        setRed(r);
        setGreen(g);
        setBlue(b);

        m_channels = 3;
    }

    /**
       solid color with red green blue alpha components 
     */
    public SolidColor(double r, double g, double b, double a) {

        super.addParams(m_aparam);

        setRed(r);
        setGreen(g);
        setBlue(b);
        setAlpha(b);

        m_channels = 4;
    }

    public void setRed(double r){
        mp_r.setValue(r);
    }

    public void setGreen(double g){
        mp_g.setValue(g);
    }

    public void setBlue(double b){
        mp_b.setValue(b);
    }

    public void setAlpha(double a){
        mp_a.setValue(a);
    }
    
    public int getChannelsCount(){

        return m_channels;
        
    }

    public int initialize() {

        super.initialize();
        m_r = mp_r.getValue();
        m_g = mp_g.getValue();
        m_b = mp_b.getValue();
        m_a = mp_a.getValue();

        return RESULT_OK;

    }


    /**
       
       @override 
       
     */
    public final int getDataValue(Vec pnt, Vec dataValue){
        
        switch(m_channels){
        case 4: 
            dataValue.v[3] = m_a;
            // no break here 
        case 3:
            dataValue.v[0] = m_r;
            dataValue.v[1] = m_g;
            dataValue.v[2] = m_b;
        }
        return RESULT_OK;
    }

    /**
     * Get the bounds of this data source.  The data source can be infinite.
     * @return
     */
    public Bounds getBounds() {
        return m_bounds;
    }

    /**
     * Set the bounds of this data source.  For infinite bounds use Bounds.INFINITE
     * @param bounds
     */
    public void setBounds(Bounds bounds) {
        this.m_bounds = bounds.clone();
    }

}