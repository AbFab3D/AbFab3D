/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package shapejs;

import abfab3d.util.DataSource;
import abfab3d.grid.Bounds;


/**
   
 */
public class Shape {

    protected DataSource dataSource;
    protected Bounds bounds;
    
    public Shape(DataSource dataSource, Bounds bounds){
        this.dataSource = dataSource;
        this.bounds = bounds;
    }

    public Bounds getBounds(){
        return bounds;
    }
    public void setBounds(Bounds bounds) {
        this.bounds = bounds.clone();
    }

    public DataSource getDataSource(){
        return dataSource;
    }

    public void setDataSource(DataSource source) {
        dataSource = source;
    }
}