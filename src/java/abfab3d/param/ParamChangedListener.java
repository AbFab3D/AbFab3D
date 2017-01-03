/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.param;

import abfab3d.param.Parameter;

/**
 * Called when an editor changes a parameter
 *
 * @author Alan Hudson
 */
public interface ParamChangedListener {
    public void paramChanged(Parameter param);
}
