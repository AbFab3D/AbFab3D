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

import javax.vecmath.Vector2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


import java.awt.geom.Path2D;


import abfab3d.param.Parameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.BooleanParameter;
import abfab3d.param.ParamChangedListener;
import abfab3d.param.FunctionParameter;

import abfab3d.param.editor.ParamPanel;



import static abfab3d.core.Output.printf;



/**
   testing TrinaleSlices on special analythical models
 */
public class TriangleSlicerTesterFrame  extends JFrame implements ParamChangedListener{

    JLabel m_status;
    PolylinePanel m_slicePanel;
    ParamPanel m_paramPanel;

    static final String sm_shapeNames[] = {"Lissajous", "torus"};

    EnumParameter mp_shape = new EnumParameter("shape",sm_shapeNames,sm_shapeNames[0]);
    DoubleParameter mp_slice = new DoubleParameter("slice",0.);
    BooleanParameter mp_grid = new BooleanParameter("grid",true);
    IntParameter mp_nu = new IntParameter("nu",1);
    IntParameter mp_nv = new IntParameter("nv",1);
    DoubleParameter mp_shift = new DoubleParameter("shift",0.25);
    IntParameter mp_countU = new IntParameter("countU",10);
    IntParameter mp_countV = new IntParameter("countV",10);
    FunctionParameter mp_fit = new FunctionParameter("Fit to Window", new OnFitToWindow());

    Parameter m_params[] = new Parameter[]{
        mp_shape,
        mp_nu,
        mp_nv,
        mp_shift,
        mp_countU,
        mp_countV,
        mp_slice,
        mp_grid,
        mp_fit,
    };


    public TriangleSlicerTesterFrame(){
        super("Triangle Slicer Tester");

        setLocation(0, 0);
        setLayout(new BorderLayout());
        
        m_slicePanel = new PolylinePanel();
        m_status = new JLabel("Status");
        m_status.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        m_slicePanel.setStatusLabel(m_status);
        m_paramPanel = new ParamPanel("params", m_params);

        m_paramPanel.addParamChangedListener(this);

        add(m_slicePanel, BorderLayout.CENTER);
        add(m_status, BorderLayout.SOUTH);
        add(m_paramPanel, BorderLayout.WEST);
        
        pack();

        paramChanged(mp_shape);

        setVisible(true);

    }

    class OnFitToWindow implements ParamChangedListener {
        public void paramChanged(Parameter param){
            m_slicePanel.fitToWindow();
        }
    }

    /**
       listener for param values change 
       @override 
     */
    public void paramChanged(Parameter parameter) {

        printf("paramChanged\n");
        switch(mp_shape.getSelectedIndex()){
        default:
        case 0: 
            m_slicePanel.setPolylines(makeLissajous()); 
        }
        if(parameter == mp_grid){
            m_slicePanel.setGrid(mp_grid.getValue());
        }
    }
    
    double[][] makeLissajous(){
        int count = mp_countU.getValue();
        int nu = mp_nu.getValue();
        int nv = mp_nv.getValue();
        double shift = mp_shift.getValue();


        double line[] = new double[(count+1)*2];
        
        for(int i = 0; i <= count; i++){
            double a = i*2*Math.PI/count;
            double b = 2*Math.PI*shift;
            line[2*i] = Math.sin(nu*a+shift);
            line[2*i+1] = Math.sin(nv*a);
        }
        return new double[][]{line};
    }

    /**
       @override 
     */
    protected void processWindowEvent(WindowEvent e) {

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0);
        } 
        // Pass along all other events
        super.processWindowEvent(e);

    }



    public static void main(String args[]){
        
        new TriangleSlicerTesterFrame();

    }

}