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

import java.awt.event.*;

import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.util.Vector;

import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;

import javax.swing.plaf.basic.BasicArrowButton;

import static abfab3d.util.MathUtil.clamp;


/**
   combination of ScrollTextField and vertical mini scroller
 */
public class NumberScroller extends  JPanel {
    
    protected Vector<ChangedListener> m_listeners;

    protected double 
        m_value, 
        m_minRange, 
        m_maxRange, 
        m_increment;

    protected JButton m_buttonUp, m_buttonDown;
    protected ScrollTextField m_textField;

    protected int m_length = 10; // default length of text field
    static int dragCutoff = 5;

    final Dimension sliderDimension = new Dimension(15,25);
    double m_currentIncrement = 0.0001;

    protected boolean m_doIntegers = false;
    
    public NumberScroller(double value, double minRange, double maxRange, double increment){
        m_value = value;
        m_minRange = minRange;
        m_maxRange = maxRange;
        m_increment = increment;
        createUI();

    }

    protected void createUI(){

        m_textField = new ScrollTextField(m_value, "", m_length, m_doIntegers); 

        m_textField.addChangedListener(new MyTextChangedListener());
        m_textField.addFocusListener(new MyFocusListener());
        m_textField.addKeyListener(new MyKeyListener());

        m_textField.setMinRange(m_minRange);
        m_textField.setMaxRange(m_maxRange);

        ButtonMouseListener ml_up   = new ButtonMouseListener(1);    
        ButtonMouseListener ml_down = new ButtonMouseListener(-1);    
        
        m_buttonUp = new BasicArrowButton(SwingConstants.NORTH);
        m_buttonUp.addMouseListener(ml_up);
        m_buttonUp.addMouseMotionListener(ml_up);
        
        m_buttonDown = new BasicArrowButton(SwingConstants.SOUTH);
        m_buttonDown.addMouseListener(ml_down);
        m_buttonDown.addMouseMotionListener(ml_down);
        
        JPanel buttons = new JPanel();
        buttons.setPreferredSize(sliderDimension);
        buttons.setMinimumSize(sliderDimension);
        
        buttons.setLayout(new GridLayout(2,1));
        buttons.add(m_buttonUp);
        buttons.add(m_buttonDown);
        
        
        this.setLayout(new GridBagLayout());
        WindowUtils.constrain(this, m_textField, 0,0,1,1, gbc.HORIZONTAL, gbc.CENTER, 1., 0.1, 0,0,0,0); 
        WindowUtils.constrain(this, buttons,  1,0,1,1,   gbc.NONE,       gbc.CENTER,  0., 0.1, 1,0,1,0); 
        
    }

    public double getValue(){
        return m_value;
    }

    public void setValue(double value){
        //TODO update UI 
        m_value = value;
    }
    
   
    public void addChangedListener(ChangedListener listener) {
        if(m_listeners == null)
            m_listeners = new Vector<ChangedListener>();
        m_listeners.add(listener);
    }

    public void informListeners(){

        if(m_listeners != null){            
            for(int i = 0; i < m_listeners.size(); i++){
                m_listeners.get(i).valueChanged(this);
            }
        }
    }
 
    static private GridBagConstraints gbc = new GridBagConstraints();
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
            double v = m_value + sign * increment;
            double res = round(v, increment);
            //printf("v: %10.8f inc: %10.8f, res:%10.8f",v,increment, res);
            m_textField.setValue(res);
            informListeners();
            
        }
        
        public void mouseClicked( MouseEvent e ){          
        }
        
        public void mouseEntered( MouseEvent e ) {          
        }
        
        public void mouseExited( MouseEvent e ) {          
        }      
        
        public void mousePressed( MouseEvent e ) {
            
            //updateParameter();
            
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
                startDragValue = m_value;
                //TODO 
                startDragIncrement = 0.0001; //parameter.getIncrement();
                if(repeater.isAlive())
                    doStop = true;
                return;
            }
            
            // do sliding mode  
            double value = clamp(round(startDragValue + (mouseDownY-y) * startDragIncrement, startDragIncrement),m_minRange, m_maxRange);

            //m_dparam.setValue(value);
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
        
            
    class MyFocusListener extends FocusAdapter {
        
        public void focusLost(FocusEvent e){
            
            m_textField.updateValue();
            
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
                m_textField.updateValue();
                //m_value = m_textField.getValue();
                //informListeners();
                return;
            }
        }
    }// class MyKeyListener 

    class MyTextChangedListener implements ChangedListener {
        
        /**
         *  ValueChangedListener    callback    
         */
        public void valueChanged(Object obj){
            m_value = m_textField.getValue();
            m_currentIncrement = m_textField.getIncrement();
            informListeners();            
        }
    }   
     
}