package abfab3d.param.editor;

import java.awt.*;
import java.io.*;

import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.util.StringTokenizer;


public class WindowUtils extends Object{
    
    static public Frame getMainWindow(Component c) {
        while (c.getParent() != null && !(c instanceof Frame))
            c = c.getParent();
        return (Frame)c;
    }
    
    static public Frame getFrame(Component c) {
        return getMainWindow(c);
    }
    
    static private GridBagConstraints cons = new GridBagConstraints();
    
    static public void constrain(Container container, Component component, 
                                 int grid_x, int grid_y, int grid_width, int grid_height,
                                 int fill, int anchor, double weight_x, double weight_y,
                                 int left, int top, int right, int bottom)
    {
        cons.gridx = grid_x; cons.gridy = grid_y;
        cons.gridwidth = grid_width; cons.gridheight = grid_height;
        cons.fill = fill; cons.anchor = anchor;
        cons.weightx = weight_x; cons.weighty = weight_y;
        cons.insets.top = top;
        cons.insets.left = left;
        cons.insets.bottom = bottom;
        cons.insets.right = right;            
        ((GridBagLayout)container.getLayout()).setConstraints(component, cons);
        container.add(component);
    }
    
    static public void constrain(Container container, Component component, 
                                 int grid_x, int grid_y,int grid_width,int grid_height){
        constrain(container, component, grid_x, grid_y, 
                  grid_width, grid_height, GridBagConstraints.NONE, 
                  GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 0, 0);
    }
    
    static public void constrain(Container container, Component component, 
                                 int grid_x, int grid_y, int grid_width,int grid_height,
                                 int left, int top, int right, int bottom) {
        constrain(container, component, grid_x, grid_y, 
                  grid_width, grid_height, GridBagConstraints.NONE, 
                  GridBagConstraints.NORTHWEST, 
                  0.0, 0.0, left, top, right, bottom);
    }
    
    static public void constrain(Container container, Component component, 
                                 int grid_x, int grid_y, int grid_width,int grid_height,
                                 int fill, int anchor, double weight_x,double weight_y){
        constrain(container, component, grid_x, grid_y, 
                  grid_width, grid_height,fill,anchor, 
                  weight_x, weight_y, 0,0,0,0);
        
    }
    
    static public void constrain(Container container, Component component, 
                                 int grid_x, int grid_y, int grid_width,int grid_height,
                                 int fill, int anchor){
        constrain(container, component, grid_x, grid_y, 
                  grid_width, grid_height,fill,anchor, 
                  1.0, 1.0, 0,0,0,0);
        
    }
    
    /**
       return absolute position of given component on screen.
    */
    static public Rectangle getAbsolutePosition(Component c){
        Rectangle r = c.getBounds();
        while(!(c instanceof Frame)){
            c = c.getParent();
            Rectangle r1 = c.getBounds();
            r.setLocation(r.x+r1.x,r.y+r1.y);
        }
        return r;
    }
    
    public static void limitedDump(Exception ex){
        
        int BUFSIZE = 500;
        ByteArrayOutputStream ba = new ByteArrayOutputStream(BUFSIZE);
        PrintStream ps = new PrintStream(ba);
        ex.printStackTrace(ps);
        ps.flush();
        System.out.print(ba.toString().substring(0,BUFSIZE));
    }
            
    public static void centerDialog(Container container){
        
        Dimension dimd = container.getPreferredSize();
        Dimension dims = Toolkit.getDefaultToolkit().getScreenSize();
        int sizex = dimd.width;
        int maxX  = 7*dims.width/8;
        int maxY  = 7*dims.height/8;
        
        if(sizex > maxX) sizex = maxX;
        int sizey = dimd.height;
        if(sizey > maxY) sizey = maxY;
        container.setLocation((dims.width-sizex)/2, (dims.height - sizey)/2);
        container.setSize(sizex, sizey);
        
    }
    
    static char COLON = ':';
    
    
    public static void monitorComponentLocation(Component comp, String name){//, PreferencesManager prefManager){
        
        MyComponentListener listener = new MyComponentListener(name);//, prefManager);
        comp.addComponentListener(listener);
        
    }
    
    
    public static void restoreComponentLocation(Component comp, String name){//, PreferencesManager prefManager){
        //TODO 
        String str = null;//prefManager.getValue(name);
        
        if(str == null)
            return; 
        
        StringTokenizer st = new StringTokenizer(str,": ", false);
        
        int x=0,y=0, w=0,h=0;
        
        if(st.hasMoreTokens())
            x = Integer.parseInt(st.nextToken());
        if(st.hasMoreTokens())
            y = Integer.parseInt(st.nextToken());
        if(st.hasMoreTokens())
            w = Integer.parseInt(st.nextToken());
        if(st.hasMoreTokens())
            h = Integer.parseInt(st.nextToken());
        
        if(w > 0 && h > 0){
            comp.setBounds(x,y,w,h);
        }
    }
    
    static class MyComponentListener implements ComponentListener {
        
        String name;
        //PreferencesManager prefManager; 
        
        MyComponentListener(String name){//, PreferencesManager pm){
            
            this.name = name; 
            //this.prefManager = pm;
            
        }
        
        public void 	componentHidden(ComponentEvent e){
        }
        
        public void 	componentShown(ComponentEvent e){
        }
        
        public void 	componentMoved(ComponentEvent e){
            saveLocation(e.getComponent());
        }
        public void 	componentResized(ComponentEvent e){
            saveLocation(e.getComponent());
        }
        
        void saveLocation(Component c){
            
            Rectangle r = c.getBounds();
            String str = "" + r.x + COLON + r.y + COLON + r.width + COLON + r.height; 
            //prefManager.setValue(name, str);
            
        }
    }
}

