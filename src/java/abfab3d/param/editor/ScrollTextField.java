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

import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


public class ScrollTextField extends JTextField {

    static final boolean DEBUG = false;
    static final int MIN_DIGITS = 6;

    private int m_caretPosition = 0;
    private double m_currentIncrement = 0.0001;
    private String m_currentFormat = "%17.15f";
    double value_down;
    double m_value = Double.MIN_VALUE; // initially undefined 
    int y_down, y_old;
    private int compHeight = 0;
    private boolean mouseDraggedFlag = false;
    static final int dragSensitivity = 5;
    
    boolean doInteger = false;  
    private int top = Integer.MAX_VALUE;
    private int bottom = Integer.MIN_VALUE;

    double m_minValue = -Double.MAX_VALUE;
    double m_maxValue = Double.MAX_VALUE;

    boolean m_hasFocus = false;
    
    private boolean processUpDownArrowKeys = true;
    
    AFocusListener focusListener = new AFocusListener();
        
    public ScrollTextField(double value, int columns){
        
        super("", columns);
        
        initListeners();
        setValue(value);
        
    }
    
    public ScrollTextField(double value, String desc, int columns, boolean doInteger){

        super("", columns);
        initListeners();

        this.doInteger = doInteger; 
        
        if(doInteger) {
            m_currentFormat = "%d";
            m_currentIncrement = 1;
        }
        setValue(value);
        
    }

    /**
       set minimal possible value
     */
    public void setMinRange(double value){
        m_minValue = value;
    }

    /**
       set maximal possible value
     */
    public void setMaxRange(double value){
        m_maxValue = value;
    }
    
    public void doIncrement(double sign) {
        
        updateValue(m_value + sign*m_currentIncrement);
    }

    protected void initListeners(){
        
        this.addMouseMotionListener(new MyMouseMotionListener());    
        this.addMouseListener(new MyMouseListener());
        this.addFocusListener(focusListener);
        this.addMouseWheelListener(new MyMouseWheelListener());
        
        if(processUpDownArrowKeys){
            this.addKeyListener(new MyKeyListener());
        }
    }
    
    class MyMouseMotionListener extends MouseMotionAdapter {

        public void mouseMoved(MouseEvent e){
            //if(DEBUG)printf("mouse moved: %d %d\n", e.getX(), e.getY());            
        }
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

            double value = value_down;
                        
            value = value_down - ((y-y_down)/dragSensitivity)*m_currentIncrement;
            
            double scale = 1/m_currentIncrement;
            //value = (int)(value*scale)/(scale);    
            
            if(doInteger) {
                
                if(value > top )
                    value = top;
                if(value < bottom)
                    value = bottom;
            }
            
            updateValue(value);
            y_old = y;
        } 
    }

    class MyMouseWheelListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {

            if(!m_hasFocus) return;
            if(DEBUG)printf("mouseWheelMoved(%d, %d)\n", e.getX(), e.getY());
            String text = extractNumber();
            //int caretPosition = calculateCaretPosition(e.getX(), text);
            //makeIncrementStep(caretPosition, text);
            
            int y = e.getWheelRotation();
            double oldVal = m_value;
            double value = oldVal - y*m_currentIncrement;

            if(doInteger) {

                if(value > top )
                    value = top;
                if(value < bottom)
                    value = bottom;
            }

            printf("rot: %d  old_val: %f  new_val: %f  inc: %f\n",y,oldVal,value,m_currentIncrement);

            updateValue(value);
            y_old = y;
        }
    }
    /**
       set numeric value from outside
     */
    public void setValue(double value){

        if(m_value == value) 
            return;
        //if(DEBUG) new Exception().printStackTrace();
        if(DEBUG) printf("%s.setValue(%20.18f)\n", this.getClass().getSimpleName(), value);
        
        m_value = value;
        String text = getString(value);
        setText(text);
        if(DEBUG) printf("   text: %s\n", text);
        
    }

    
    /**
       return current numeric value 
     */
    public double getValue(){

        return m_value;
        
    }

    /**
       update value from current text 
     */
    public void updateValue(){

        double newVal = clamp(Double.parseDouble(extractNumber()), m_minValue, m_maxValue);
        
        if (newVal != m_value) {
            m_value = newVal;
            informListeners();
        }
    }

    /**
       set value from parameter
     */
    private void updateValue(double value){
        
        value = clamp(value, m_minValue, m_maxValue);
        if(doInteger)
            value = Math.round(value);

        if(m_value != value){
            m_value = value;
            setText(getString(value));            
            setCaretPosition(m_caretPosition);
            informListeners();
        }        
    }

    private String getString(double value){
        if(doInteger)
            return fmt("%d",(int)value);
        else
            return removeZeroes(fmt(m_currentFormat, value));
        
    }

    /**
       removes zeros at the end of string
     */
    static String removeZeroes(String text){
        int len = text.length();
        int cnt = 0;
        for(int i = len-1; i > 0; i--){
            if(text.charAt(i) != '0')
                break;
            cnt++;
        }
        if(cnt > 0){
            return text.substring(0, len-cnt);
        } else {
            return text;
        }


    }
    
    private String appendix = "";  // text at the end of number, should be appended 
    
    public String extractNumber(){
        
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
    public void informListeners(){
        
        if(valueListeners != null){
            for(int i = 0; i < valueListeners.size(); i++){
                ChangedListener listener = (ChangedListener)valueListeners.elementAt(i);
                listener.valueChanged(this);        
            }
        }
    }
    
    public void addChangedListener(ChangedListener listener) {
        
        if(valueListeners == null){
            valueListeners = new Vector(1);
        } 
        valueListeners.addElement(listener);
    }    
    
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
        
        m_currentIncrement = delta;
        int digits = (int)Math.max(0.,-Math.floor(Math.log10(m_currentIncrement)));
        digits = Math.max(MIN_DIGITS,digits);
        //printf("digits: %d\n", digits);
        // format change creates problems
        //m_currentFormat = "%" + (digits+2) + "." + digits+"f";
        
    }

    public double getIncrement(){
        return m_currentIncrement;
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
            
            String text = extractNumber();
            m_caretPosition = calculateCaretPosition(e.getX(), text);
            makeIncrementStep(m_caretPosition,text);
            
        }
        

        public void mouseReleased(MouseEvent e) {
            
            if(mouseDraggedFlag){
                // clear selection in text field 
                select(0,0);
            }
            //System.out.println(e);
        }    
    }

    int calculateCaretPosition(int mouseX, String text){

        int x_down = mouseX;
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
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
        return caret_position;
    }
        
    
    //
    // 
    //
    class AFocusListener implements FocusListener{
        
        String oldValue = "";

        public void focusGained(FocusEvent e) {
            oldValue = getText();
            m_hasFocus = true;
        }    
        
        public void focusLost(FocusEvent e) {
            m_hasFocus = false;
            // potentially editing was changed 
            String newValue = getText();
            if(!oldValue.equals(newValue))
                informListeners();
        }    
    } // class AFocusListener
    


    //
    //
    //
    class MyKeyListener extends KeyAdapter {
        
        //public void	keyPressed(KeyEvent e){
        //    if(DEBUG)printf("keyPressed: %s\n",e);
        //}

        public void	keyPressed(KeyEvent e){
            
            int sign = 0;
            //if(DEBUG)printf("keyReleased: %s\n",e);
            
            switch(e.getKeyCode()){
            default: 
                break;
            case KeyEvent.VK_UP:        
                sign = 1;                        
                break;
                
            case KeyEvent.VK_DOWN:                
                sign = -1;
                break;
            }
            
            if(sign != 0) {
                
                // arrows was pressed 
                double value = Double.parseDouble(extractNumber()); 
                
                value += sign*m_currentIncrement;
                
                double scale = 1/m_currentIncrement;
                //number = Math.round(number*scale)/(scale);    
                
                if(doInteger){
                    
                    updateValue((int)value);
                    
                }  else {
                    
                    updateValue(value);
                }
            }               
        }


        public void	keyReleased(KeyEvent e){
            
            int sign = 0;            
            switch(e.getKeyCode()){
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_HOME:
            case KeyEvent.VK_END:
                m_caretPosition = getCaretPosition();
                // update current increment 
                makeIncrementStep(m_caretPosition, extractNumber());                
                break;
            }
        }
    }    
}
