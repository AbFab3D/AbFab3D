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


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.getUnitsName;


/**

   panel to display polyline

   @author Vladimir Bulatov

*/
public class PolylinePanel extends JPanel implements MouseWheelListener, MouseMotionListener, MouseListener{
    
    static final int MIN_SCREEN_GRID_CELL_SIZE = 5; // minimal screen grid cel size
    Dimension m_dim;
    static final double[][] DEFAULT_LINES = new double[][]{{-1,-1,1,-1,1,1,-1,1,-1,-1},{-0.9,-0.9,0.1,-0.9,0.1,0.1,-0.9, 0.1, -0.9,-0.9}};
    
    static final double MIN_SIZE = 1.e-3;
    static final double FIT_FACTOR = 1.05;
    double[][] m_lines = DEFAULT_LINES;
    double m_units = MM;
    
    
    double m_pixelSize = 0.1; // calculated in  paintComponent()
    double m_panIncrement = 20; // in pixels 
    // center and size of world coordinates
    
    Vector2d m_center = new Vector2d(0.,0); 
    Vector2d m_size = new Vector2d(3.,3); 
    Vector2d m_wpnt = new Vector2d(); // world coord of mouse pointer
    Vector2d m_wDownPnt = new Vector2d(); // world coord mouse down pnt
    
    double m_zoomIncrement = 0.02;
    boolean m_grid = true;


    JLabel m_status;

    public PolylinePanel(){
        
        this.addMouseWheelListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        
    }

    public void setStatusLabel(JLabel label){
        m_status = label;
    }
    
    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
        screen2world(new Vector2d(e.getX(), e.getY()), m_wDownPnt);
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {            
        screen2world(new Vector2d(e.getX(), e.getY()), m_wpnt);
        m_wpnt.sub(m_wDownPnt);
        m_center.sub(m_wpnt); 
        repaint();
    }        
    public void mouseMoved(MouseEvent e) {
        screen2world(new Vector2d(e.getX(), e.getY()), m_wpnt);
        if(m_status != null){
            m_status.setText(fmt("[%8.3f,%8.3f]%s",m_wpnt.x/m_units,m_wpnt.y/m_units, getUnitsName(m_units)));
        }
    }

    /**
       @Override
    */
    public void mouseWheelMoved(MouseWheelEvent e) {
        
        int mod = e.getModifiersEx();
        Vector2d spnt = new Vector2d(e.getX(), e.getY());
        screen2world(spnt, m_wpnt);

        if ((mod & e.CTRL_DOWN_MASK) == e.CTRL_DOWN_MASK) {
            
            double zoom = Math.exp(e.getWheelRotation() * m_zoomIncrement);
            Vector2d delta = new Vector2d(m_center);
            delta.sub(m_wpnt);
            delta.scale(zoom);
            delta.add(m_wpnt);
            m_center.set(delta);
            m_size.x *= zoom;  
            repaint();
            
        } else if ((mod & e.SHIFT_DOWN_MASK) == e.SHIFT_DOWN_MASK) {
            
            // horizontal pan 
            double dx = e.getWheelRotation() * m_pixelSize * m_panIncrement;
            m_center.x -= dx;
            //screen2world(spnt, m_wpnt); 
            repaint();

        } else {
            // vertical pan 
            double dy = e.getWheelRotation() * m_pixelSize * m_panIncrement;
            m_center.y -= dy;
            //screen2world(spnt, m_wpnt); 
            repaint();
        }

    }
    
    

    /**
       set polylines to display 
       each polylne is one dimensional array of pairs (x,y)  [x0,y0,x1,y1,x2,y2,...]
       there many be many polylines 
     */
    public void setPolylines(double lines[][]){
        m_lines = lines;
        repaint();
    }

    /**
       set visibility of grid 
     */
    public void setGrid(boolean value){
        m_grid = value;
        repaint();
    }
   
    /**
       fit current data into view
     */
    public void fitToWindow(){

        double a = 1.e10;
        double 
            xmin = a,
            xmax = -a,
            ymin = a,
            ymax = -a;
        

        for(int k = 0; k < m_lines.length; k++){
            double line[] = m_lines[k];
            for(int i = 0; i < line.length-1; i += 2){
                double 
                    x = line[i],
                    y = line[i+1];
                if(x < xmin) xmin = x;
                if(x > xmax) xmax = x;
                if(y < ymin) ymin = y;
                if(y > ymax) ymax = y;                
            }
        }
        printf("xmin: %7.5f xmax: %7.5f ymin:%7.5f, ymax:%7.5f\n", xmin, xmax, ymin, ymax);
        
        m_center.x = (xmin + xmax)/2;
        m_center.y = (ymin + ymax)/2;
        double sx = Math.max(FIT_FACTOR*(xmax-xmin),MIN_SIZE);
        double sy = Math.max(FIT_FACTOR*(ymax-ymin),MIN_SIZE);
        double panelAspect = ((double)m_dim.height/m_dim.width);
        double dataAspect = (sy/sx); 
        if(panelAspect > dataAspect){
            m_size.x = sx;
            m_size.y = sx*panelAspect;
        } else {
            m_size.y = sy;
            m_size.x = sy/panelAspect;
        }
        printf("sx: %7.5f sy: %7.5f\n", sx, sy);
        repaint();
    }


    public Dimension getPreferredSize(){
        return new Dimension(800,800);
    }

    protected void paintComponent(Graphics _g) {
        
        super.paintComponent(_g); 
        Graphics2D g = (Graphics2D)_g;
        
        g.setRenderingHints(new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

        m_dim = this.getSize();
        m_pixelSize = m_size.x/m_dim.width;


        if(m_grid) drawGrid(g);


        g.setColor(Color.BLACK);
        Path2D path = new Path2D.Double();
        for(int k = 0; k < m_lines.length; k++){
            double line[] = m_lines[k];
            Vector2d 
                spnt = new Vector2d(),
                wpnt = new Vector2d();
            wpnt.set(line[0],line[1]);
            world2screen(wpnt, spnt);
            path.reset();
            path.moveTo(spnt.x, spnt.y);
            for(int i = 2; i < line.length-1; i += 2){
                wpnt.set(line[i],line[i+1]);
                world2screen(wpnt, spnt);
                path.lineTo(spnt.x, spnt.y);                    
            }
            g.draw(path);
        }
        // draw border 
        //g.setColor(Color.BLUE);
        //g.drawRect(0,0, m_dim.width-1, m_dim.height-1);
        
    }

    void drawGrid(Graphics2D g){

        // round up gridCell to the nearest power of 10
        double gridCell = Math.pow(10.,Math.ceil(Math.log10(m_pixelSize*MIN_SCREEN_GRID_CELL_SIZE)));
        //printf("gridCell:%7.5e\n",gridCell);
        Vector2d wp0 = new Vector2d(), wp1 = new Vector2d(),sp0 = new Vector2d(), sp1 = new Vector2d();
        
        screen2world(new Vector2d(0,m_dim.height-1), wp0);
        screen2world(new Vector2d(m_dim.width,0), wp1);
        double 
            wxmin = wp0.x,
            wxmax = wp1.x,
            wymin = wp0.y,
            wymax = wp1.y;

        int xmin = (int)Math.floor(wxmin/gridCell);
        int xmax = (int)Math.ceil(wxmax/gridCell);
        int ymin = (int)Math.floor(wymin/gridCell);
        int ymax = (int)Math.ceil(wymax/gridCell);
        Path2D path = new Path2D.Double();
        for(int i = xmin; i <= xmax; i++){
            wp0.set(i*gridCell, 0);
            world2screen(wp0, sp0);
            // printf("x: %d \n", (int)(sp0.x));
            path.moveTo(sp0.x, 0);
            path.lineTo(sp0.x, m_dim.height);
        }
        for(int i = ymin; i <= ymax; i++){
            wp0.set(0,i*gridCell);
            world2screen(wp0, sp0);
            path.moveTo(0, sp0.y);
            path.lineTo(m_dim.width-1, sp0.y);
        }

        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(new BasicStroke(0.5f));
        g.draw(path);
        
        path.reset();
        // draw secondary grid 
        gridCell *= 10;
        xmin = (int)Math.floor(wxmin/gridCell);
        xmax = (int)Math.ceil(wxmax/gridCell);
        ymin = (int)Math.floor(wymin/gridCell);
        ymax = (int)Math.ceil(wymax/gridCell);
        for(int i = xmin; i <= xmax; i++){
            wp0.set(i*gridCell, 0);
            world2screen(wp0, sp0);
            // printf("x: %d \n", (int)(sp0.x));
            path.moveTo(sp0.x, 0);
            path.lineTo(sp0.x, m_dim.height);
        }
        for(int i = ymin; i <= ymax; i++){
            wp0.set(0,i*gridCell);
            world2screen(wp0, sp0);
            path.moveTo(0, sp0.y);
            path.lineTo(m_dim.width-1, sp0.y);
        }

        g.setColor(new Color(150,150,150));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(path);


    }

    
    void screen2world(Vector2d spnt, Vector2d wpnt){

        wpnt.x = (spnt.x + 0.5 - 0.5 * m_dim.width) * m_pixelSize + m_center.x;
        wpnt.y = ((m_dim.height - 1 - spnt.y) + 0.5 - 0.5 * m_dim.height) * m_pixelSize + m_center.y;
        
    }

    void world2screen(Vector2d wpnt, Vector2d spnt) {
        spnt.x = (wpnt.x - m_center.x) / m_pixelSize + 0.5 * m_dim.width - 0.5;
        spnt.y = m_dim.height - 1 - ((wpnt.y - m_center.y) / m_pixelSize + 0.5 * m_dim.height - 0.5);
    }

}

