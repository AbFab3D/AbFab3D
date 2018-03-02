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

import javax.swing.*;

public class ParamFrame extends JFrame {
    private ParamPanel panel;

    public ParamFrame(Parameterizable node) {
        this(node.getClass().getSimpleName(), node);
    }

    public ParamFrame(String title, Parameterizable node) {

        super(node.getClass().getSimpleName());

        panel = new ParamPanel(title,node);

        add(panel);
        pack();
    }

    public ParamFrame(String name, Parameter params[]) {

        super(name);

        panel = new ParamPanel(name,params);
        add(panel);

        pack();
    }

    public ParamPanel getPanel() {
        return panel;
    }

}
