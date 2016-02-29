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
    ScrollTextField textField;
    JButton buttonUp, buttonDown;
    
    double increment = 0.01;
    int m_length;

    JPanel m_component;
    final Dimension sliderDimension = new Dimension(15,25);
    
    static final int DEFAULT_LENGTH = 10;
    
    public DoubleEditorScroll(DoubleParameter parameter){
        this(parameter, DEFAULT_LENGTH);
    }
    public DoubleEditorScroll(DoubleParameter parameter, int length){
        
        
        textField = new ScrollTextField(parameter.getValue().toString(), parameter.getDesc(), m_length, false); //false - process floats
        //textField.addValueChangedListener(this);
        textField.addFocusListener(new MyFocusListener());
        textField.addKeyListener(new MyKeyListener());
        
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
        WindowUtils.constrain(m_component,textField, 0,0,1,1,   gbc.HORIZONTAL, gbc.CENTER, 1., 0.1, 0,0,0,0); 
        WindowUtils.constrain(m_component,buttons,  1,0,1,1,   gbc.NONE,       gbc.CENTER,  0., 0.1, 1,0,1,0); 
        this.parameter = parameter;
    }
    
    public Component getComponent(){
        return m_component;
    }
    
    
    public void setParam(DoubleParameter parameter){
        
        this.parameter = parameter;
        setValue(parameter.getValue().toString());  
        
    }
    
    
    public void setEditable(boolean mode){
        
        textField.setEditable(mode);
        
    }
    /*
      public void addValueChangedListener(ValueChangedListener listener) {
      if(valueListeners == null){
      valueListeners = new Vector(1);
      } 
      valueListeners.addElement(listener);
      }
    */
    /**
     *  ValueChangedListener    callback    
     */
    public void valueChanged(Object obj){
        processValueChanged();
    }
    /**
       void valueChanged()
    */
    public void processValueChanged(){
        
        parameter.setValue(textField.getText());
        textField.setText(getFormattedValue());
        informListeners();
    }
    
    void informListeners(){
        
        if(valueListeners == null)
            return;    
        
        for(int i =0; i < valueListeners.size(); i++){
            
            //ValueChangedListener listener = (ValueChangedListener)valueListeners.elementAt(i);
            //listener.valueChanged(this);
            
        }
    }
    
    public String getValue(){
        return textField.getText();
    }
    
    public void setValue(String value){
        textField.setText(value);
    }
    
    public void updateEditor(){
        
        textField.setText(getFormattedValue());
        
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
            
            increment = 0.001;//parameter.getIncrement();
            double v = (parameter.getValue().doubleValue() + sign * increment);
            double res = round(parameter.getValue().doubleValue() + sign * increment, increment);
            //System.out.println("v:" + v + " increment: " + increment + " res: " + res);
            parameter.setValue(res);
            String text = parameter.getValue().toString();
            textField.setText(text);      
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
            parameter.setValue(round(startDragValue + (mouseDownY-y) * startDragIncrement, startDragIncrement));
            String text = parameter.getValue().toString();
            textField.setText(text);      
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
        
        parameter.setValue(new Double(Double.parseDouble(textField.getText())));
        textField.setText(getFormattedValue());
        
    }
    
    String getFormattedValue(){
        return fmt("%10.8f",parameter.getValue().doubleValue());
    }

  class MyFocusListener extends FocusAdapter {

    public void focusLost(FocusEvent e){

      //System.out.println("focusLost()");
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
                valueChanged(null);
                return;
            }
        }
    }// class MyKeyListener 
}
