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

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;


public class ScrollTextField extends TextField {
    
    private double incrementStep = 0.1;
    double value_down;
    int y_down, y_old;
    private int compHeight = 0;
    private boolean mouseDraggedFlag = false;
    static final int dragSensitivity = 5;
    
    boolean doInteger = false;  
    private int top = Integer.MAX_VALUE;
    private int bottom = Integer.MIN_VALUE;
    
    
    private boolean processUpDownArrowKeys = true;
    
    AFocusListener focusListener = new AFocusListener();
    
    public ScrollTextField(String value, String desc, int columns){
        
        super(value, columns);
        //this.step = step;
        initListeners();
        
    }
    
    public ScrollTextField(String value, int columns){
        
        super(value, columns);
        
        initListeners();
        
    }
    
    public ScrollTextField(String value, String desc, int columns, boolean doInteger){
        super(value, columns);
        initListeners();
        
        this.doInteger = doInteger; 
    }
    
    protected void initListeners(){
        
        this.addMouseMotionListener(new MyMouseMotionListener());    
        this.addMouseListener(new MyMouseListener());
        this.addFocusListener(focusListener);
        
        if(processUpDownArrowKeys){
            this.addKeyListener(new ArrowsListener());
        }
    }
    
    class MyMouseMotionListener extends MouseMotionAdapter {

        public void mouseDragged(MouseEvent e){
            
            int y = e.getY();
            if(!mouseDraggedFlag){
                if(Math.abs(y - y_down) < dragSensitivity){
                    return;
                } else {
                    // user really drags
                    mouseDraggedFlag = true;
                }		
      }
            if(Math.abs(y-y_old) < dragSensitivity){
                return;
            }
            double value = value_down;
                        
            value = value_down - ((y-y_down)/dragSensitivity)*incrementStep;
            
            double scale = 1/incrementStep;
            value = (int)(value*scale)/(scale);    
            
            if(doInteger) {
                
                if(value > top )
                    value = top;
                if(value < bottom)
                    value = bottom;
                
                updateNumber(String.valueOf((int)value));
                
            } else {
                
                updateNumber(fmt("%10.8f",value));
            }
            y_old = y;
            valueChanged();
        } 
    }
    
    private void updateNumber(String number){
        
        setText(number + appendix);
        valueChanged();
        
    }
    
    private String appendix = "mm";  // text at the end of number, should be appended 
    
    private String extractNumber(){
        
        String text = getText();
        int len = text.length();
        int numberEnd = len; 
        int start = 0;
        for(int i = 0; i < len; i++){
            char c = text.charAt(i);
            if(c == ' ' || c == '\t' ){
                continue;
            } else {
                start = i;
                break;
            } 
        }
        
        for(int i = start; i < len; i++){
            char c = text.charAt(i);
            if((c >= '0' && c <= '9') || c == '.' || c ==  '-')
                continue;
            numberEnd = i;
            break;
        }
        
        appendix = text.substring(numberEnd);
        String t = text.substring(0, numberEnd);
        if(t.length() == 0)
            return "0";
        else 
            return t;
        
    }
    
    
    Vector valueListeners=null;
    
    /**
       used by subclasses to know, that value was changed 
    */
    public void valueChanged(){
        
        if(valueListeners != null){
            for(int i = 0; i < valueListeners.size(); i++){
                ChangedListener listener = (ChangedListener)valueListeners.elementAt(i);
                listener.valueChanged(this);        
            }
        }
    }
    /*
      public void addValueChangedListener(ValueChangedListener listener) {
      
      if(valueListeners == null){
      valueListeners = new Vector(1);
      } 
      valueListeners.addElement(listener);
      }
    */
    
    private void makeIncrementStep(int caret_position, String text){
        
        int dot_position = text.lastIndexOf('.');
        if(dot_position < 0){
            // no decimal dot in the number 
            dot_position = text.length();
        }
        
        double delta = 1;
        
        if(caret_position == dot_position){
            
            delta = 0.1;
            
        } else if(caret_position < dot_position){
            
            caret_position++;
            while(caret_position < dot_position){
                delta *= 10;
                caret_position++;
            }        
        } else if(caret_position > dot_position){
            
            while(caret_position > dot_position){
                delta /= 10.;
                caret_position--;
            }              
        }
        
        if(doInteger){
            delta = (int)delta;
            if(delta == 0){
                delta = 1;
            }
        }
        
        incrementStep = delta;
        
    }
    
    private void makeIncrementStep_old(int caret_position, String text){
        
        double v1 = Double.valueOf(text).doubleValue();
        double v2;
        int dot_position = text.lastIndexOf('.');
        if(dot_position < 0){
            // no decimal dot in the number 
            v2 = Double.valueOf(text+".1").doubleValue();      
            
        } else {
            
            v2 = Double.valueOf(text+"1").doubleValue();
            
        }
        System.out.println("v1: " + v1 + " v2: " + v2);
        
        double delta = v2-v1;
        if(delta < 0){
            delta = -delta; 
        } if(delta == 0.0){
            delta = 1;
        }
        
        int diff = text.length() - caret_position;
        if(caret_position <= dot_position){
            diff --;
        }
        while(diff > 0){
            diff --;
            delta *= 10.;
        }
        delta = round10(delta);
        if(doInteger){
            delta = (int)delta;
            if(delta == 0){
                delta = 1;
            }
        }
        //System.out.println("caret_position: " + caret_position + " delta: " + delta);
        incrementStep = delta;
    }
    
    /**
     *
     *
     *
     */
    double round10(double v){
        if(v == 0)
            return 0;
        System.out.println("v: " + v);
        double power = Math.log(v*1.1)/Math.log(10);
        int i = (int)Math.floor(power);
        double x = 1;
        System.out.println("power: " + power + " i: " + i);
        if(i == 0){
            return x;
        } else if( i < 0){
            while(i < 0){
                x /= 10;
                i++;
            }
            return x;
        } else { // i > 0
            while(i > 0){
                x *= 10;
                i--;
            }
            return x;      
        }
    }
    
    
    /**
     *
     *
     *
     */
    class MyMouseListener extends MouseAdapter {
        
        public void mousePressed(MouseEvent e) {
            
            value_down = Double.valueOf(extractNumber()).doubleValue();
            Dimension size = getSize();
            compHeight = size.height;
            y_old = y_down = e.getY();
            // this is to prevent too much sensitivity to dragging 
            mouseDraggedFlag = false;
            int x_down = e.getX();
            FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
            String text = extractNumber();
            int caret_position = 0;
            int x = 0;
            int length = text.length();
            
            for(int i = 0; i < length; i++){
                x += fm.charWidth(text.charAt(i));
                if(x > x_down){
                    break;
                }
                caret_position++;
            }
            
            makeIncrementStep(caret_position, text);
            
        }
        
        public void mouseReleased(MouseEvent e) {
            
            if(mouseDraggedFlag){
                // clear selection in text field 
                select(0,0);
            }
            //System.out.println(e);
        }    
    }
    
    
    String oldValue = "";
    
    class AFocusListener implements FocusListener{
        
        public void focusGained(FocusEvent e) {
            oldValue = getText();
        }    
        
        public void focusLost(FocusEvent e) {
            // potentially editing was changed 
            String newValue = getText();
            if(!oldValue.equals(newValue))
                valueChanged();
        }    
    }
    
    class ArrowsListener extends KeyAdapter {
        
        public void	keyReleased(KeyEvent e){
            
            int sign = 0;
            //System.out.println("keyReleased: " + e);
            switch(e.getKeyCode()){
            default: 
                break;
            case KeyEvent.VK_UP:        
                {
                    sign = 1;        
                }
                break;
                
            case KeyEvent.VK_DOWN:
                {
                    sign = -1;
                }
                break;
            }
            
            if(sign != 0) {
                
                int cp = getCaretPosition();
                double number = Double.parseDouble(extractNumber()); 
                makeIncrementStep(cp, extractNumber());
                
                number += sign*incrementStep;
                
                double scale = 1/incrementStep;
                number = Math.round(number*scale)/(scale);    
                
                if(doInteger){
                    
                    updateNumber(String.valueOf((int)number));
                    
                }  else {
                    
                    updateNumber(String.valueOf(number));
                }
                
                setCaretPosition(cp);
            }      
        }
        
    }
    
}

