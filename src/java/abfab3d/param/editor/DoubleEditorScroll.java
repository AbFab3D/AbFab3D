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

package abfab3d.param.editor;

import java.util.Vector;
import java.awt.event.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import abfab3d.param.DoubleParameter;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;


public class DoubleEditorScroll extends BaseEditor { 
  
    
    DoubleParameter parameter;
    Vector valueListeners=null;
    ScrollTextField m_textField;
    JButton buttonUp, buttonDown;

    double m_currentIncrement = 0.0001;
    
    int m_length;

    JPanel m_component;
    final Dimension sliderDimension = new Dimension(15,25);
    
    static final int DEFAULT_LENGTH = 10;
    
    public DoubleEditorScroll(DoubleParameter parameter){
        this(parameter, DEFAULT_LENGTH);
    }
    public DoubleEditorScroll(DoubleParameter parameter, int length){
        
        super(parameter);

        m_textField = new ScrollTextField(parameter.getValue().toString(), parameter.getDesc(), m_length, false); //false - process floats
        m_textField.addChangedListener(new ScrollTextChangedListener());
        m_textField.addFocusListener(new MyFocusListener());
        m_textField.addKeyListener(new MyKeyListener());
        
        ButtonMouseListener ml_up   = new ButtonMouseListener(1);    
        ButtonMouseListener ml_down = new ButtonMouseListener(-1);    
        
        buttonUp = new BasicArrowButton(SwingConstants.NORTH);
        buttonUp.addMouseListener(ml_up);
        buttonUp.addMouseMotionListener(ml_up);
        
        buttonDown = new BasicArrowButton(SwingConstants.SOUTH);
        buttonDown.addMouseListener(ml_down);
        buttonDown.addMouseMotionListener(ml_down);
        
        JPanel buttons = new JPanel();
        buttons.setPreferredSize(sliderDimension);
        buttons.setMinimumSize(sliderDimension);
        
        buttons.setLayout(new GridLayout(2,1));
        buttons.add(buttonUp);
        buttons.add(buttonDown);
        
        m_component = new JPanel();
        m_component.setLayout(new GridBagLayout());
        WindowUtils.constrain(m_component,m_textField, 0,0,1,1,   gbc.HORIZONTAL, gbc.CENTER, 1., 0.1, 0,0,0,0); 
        WindowUtils.constrain(m_component,buttons,  1,0,1,1,   gbc.NONE,       gbc.CENTER,  0., 0.1, 1,0,1,0); 
        this.parameter = parameter;
    }
    
    public Component getComponent(){
        return m_component;
    }
    
    
    public void setParam(DoubleParameter parameter){
        
        this.parameter = parameter;
        //setValue(parameter.getValue().toString());  
        
    }
    
    
    public void setEditable(boolean mode){
        
        m_textField.setEditable(mode);
        
    }

    
    public void updateEditor(){
        
        //textField.setText(getFormattedValue());
        
    }
    
    void processValueChanged(){

        Double nvalue = new Double(Double.parseDouble(m_textField.getText()));
        parameter.setValue(nvalue);
        //textField.setText(getFormattedValue());
        informListeners();    
    }
    public void onActionEvent(Object userData){
        
    }
    
    static private GridBagConstraints gbc = new GridBagConstraints();
    
    static int dragCutoff = 5;
    
        
    /**
       responsible for updating param value using button arrows 
    */
    class ButtonMouseListener implements MouseListener, MouseMotionListener, Runnable {
        
        double sign;
        int repeatDelay = 100; // milliseconds
        int firstDelay = 300;
        Thread repeater;
        int mouseDownY = 0;
        boolean isReallyDragging;
        double startDragValue = 0;
        double startDragIncrement = 0;
        
        ButtonMouseListener(double sign){      
            
            this.sign = sign;
            
        }
        
        
        void doIncrement(){
            
            double increment = m_textField.getIncrement();
            double v = (parameter.getValue().doubleValue() + sign * increment);
            double res = round(parameter.getValue().doubleValue() + sign * increment, increment);
            //System.out.println("v:" + v + " increment: " + increment + " res: " + res);
            parameter.setValue(res);
            m_textField.setValue(parameter.getValue());
            informListeners();
            
        }
        
        public void mouseClicked( MouseEvent e ){          
        }
        
        public void mouseEntered( MouseEvent e ) {          
        }
        
        public void mouseExited( MouseEvent e ) {          
        }      
        
        public void mousePressed( MouseEvent e ) {
            
            updateParameter();
            
            mouseDownY = e.getY();
            isReallyDragging = false;
            
            doStop = false;
            repeater = new Thread(this);
            repeater.start();
            
            doStop = true;
            
        }
        
        public void mouseReleased( MouseEvent e ) {      
            
            if(repeater.isAlive())
                doStop = true;
            
        }    
        
        public void mouseDragged( MouseEvent e ) {
            
            int y = e.getY();
            if( !isReallyDragging ){
                if( Math.abs(y - mouseDownY) < dragCutoff){
                    return;
                } 
                isReallyDragging = true;
                startDragValue = parameter.getValue().doubleValue();
                //TODO 
                startDragIncrement = 0.0001; //parameter.getIncrement();
                if(repeater.isAlive())
                    doStop = true;
                return;
            }
            
            // do sliding mode  
            double value = round(startDragValue + (mouseDownY-y) * startDragIncrement, startDragIncrement);
            parameter.setValue(value);
            m_textField.setValue(value); 
          informListeners();      
          
      }
      
      public void mouseMoved( MouseEvent e ) {          
      }
        
        boolean doStop = false;
        
        public void run(){
            try {	
                doIncrement();
                Thread.sleep(firstDelay);
                if(doStop)
                    return;
                
                while(true){
                    doIncrement();
                    Thread.sleep(repeatDelay);
                    if(doStop)
                        break;
                } 
            }catch (Exception e){        
            }
        }
    } // class ButtonMouseListener
        
    
    public void updateParameter(){
        
        parameter.setValue(m_textField.getValue());
        
    }
    
    String getFormattedValue(){
        return fmt("%10.8f",parameter.getValue().doubleValue());
    }

  class MyFocusListener extends FocusAdapter {

    public void focusLost(FocusEvent e){

      updateParameter();
      
    }
  }


  final static double minIncrement = 0.0000001;
  
  // return rounded value with precision of increment
    
    public static double round(double value, double increment){
        
        increment = Math.abs(increment);
        if(increment < minIncrement)
            increment = minIncrement;	
        
        if( increment == 1){
            
            return (Math.floor(value + 0.5));      
            
        } else if( increment > 1){
            
            int roundFactor = 1;
            while(increment > 10){
                roundFactor *=10;
                increment /= 10;
                //System.out.println(roundFactor);
            }
            roundFactor *=10;
            return (Math.floor(value / roundFactor + 0.5) * roundFactor);
        } else {
            
            int roundFactor = 1;
            while(increment < 0.1){
                roundFactor *=10;
                increment *= 10;
            }	
            roundFactor *=10;
            return (Math.floor(value * roundFactor + 0.5) / roundFactor);
        }     
    }
    
    class MyKeyListener extends KeyAdapter {
        
        public void	keyReleased(KeyEvent e){
            
            switch(e.getKeyCode()){
                
            default: 
                return;
            case KeyEvent.VK_ENTER:  
                processValueChanged();
                return;
            }
        }
    }// class MyKeyListener 

    class ScrollTextChangedListener implements ChangedListener {
        
        /**
         *  ValueChangedListener    callback    
         */
        public void valueChanged(Object obj){
            m_currentIncrement = m_textField.getIncrement();
            processValueChanged();            
        }
    }
    

}
