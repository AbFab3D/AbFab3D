/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2018
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.param.editor;

import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Container;

import javax.swing.*;

public class ParamDialog extends JDialog {//implements ActionListener{
    
    private boolean m_result = false;
    
    public ParamDialog(JFrame frame, String title, Parameter param[]){
        super(frame, title);
        initUI(param);
    }
    
    void initUI(Parameter param[]){
        
        // create a label 
        ParamPanel panel = new ParamPanel("PP", param);
        Container container = this.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(panel,BorderLayout.CENTER);
        
        JButton btnOK = new JButton("OK");
        //btnOK.addActionListener(this);
        
        JButton btnCancel = new JButton("Cancel");
        
        btnCancel.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    m_result = false;
                    setVisible(false);                        
                }
            });
        
        btnOK.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    m_result = true;
                    setVisible(false);                        
                }
            });
        
        Container buttons = new Container();
        buttons.setLayout(new FlowLayout());
        
        buttons.add(btnOK);
        buttons.add(btnCancel);
        container.add(buttons,BorderLayout.SOUTH);
        
        this.pack();
        
    }
    
    public boolean getResult(){
        return m_result;
    }
    
}

