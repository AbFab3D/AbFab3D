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

package abfab3d.datasources;


import abfab3d.core.DataSource;
import abfab3d.core.ResultCodes;
import abfab3d.param.Shape;
import abfab3d.core.Vec;
import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;
import abfab3d.param.SNode;
import abfab3d.param.SNodeListParameter;

import java.util.ArrayList;
import java.util.List;

import static abfab3d.core.MathUtil.blendMin;
import static abfab3d.core.Output.printf;


/**
 * List of Shape nodes.
 *
 * @author Alan Hudson
 */
public class ShapeList extends TransformableDataSource implements SNode {

    ArrayList<Shape> shapes = new ArrayList<Shape>(4);

    SNodeListParameter mp_shapes = new SNodeListParameter("shapes", ShapesFactory.getInstance());

    Parameter m_aparam[] = new Parameter[]{
        mp_shapes
    };

    // internal variables
    private Shape vShapes[];

    /**
       Create empty union. Use add() method to add arbitrary number of shapes to the union.
     */
    public ShapeList(){
        initParams();
    }

    /**
     * Union Constructor
     */
    public ShapeList(Shape shape1){
        initParams();
        add(shape1);
    }

    /**
       union of two shapes
     */
    public ShapeList(Shape shape1, Shape shape2){
        initParams();

        add(shape1);
        add(shape2);
    }

    /**
     * @noRefGuide
     */
    protected void initParams() {
        super.addParams(m_aparam);
    }

    /**
       Add item to union.
       @param shape item to add to union of multiple shapes 
    */
    public void add(Shape shape){
        shapes.add(shape);
        mp_shapes.add(shape);
    }

    /**
     * Set an item into the list
     *
     * @param idx The index, it must already exist
     * @param src
     */
    public void set(int idx, Shape src) {
        mp_shapes.set(idx, (Parameterizable) src);
        shapes.set(idx, src);
    }

    /**
     * Clear the datasources
     */
    public void clear() {
        mp_shapes.clear();
        shapes.clear();
    }

    /**
       @noRefGuide
    */
    public int initialize(){
        super.initialize();
        vShapes = (Shape[])shapes.toArray(new Shape[shapes.size()]);
        for(int i = 0; i < vShapes.length; i++){
            initializeChild(vShapes[i].getSource());
        }

        return ResultCodes.RESULT_OK;
    }
    
    /**
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
       @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec data) {
        switch(m_dataType){
        default:
        case DATA_TYPE_DENSITY:
            getDensityData(pnt, data);
            break;
        case DATA_TYPE_DISTANCE:
            getDistanceData(pnt, data);
            break;
        }
        return ResultCodes.RESULT_OK;
    }

    public int getDensityData(Vec pnt, Vec data) {

        int len = vShapes.length;
        Shape dss[] = vShapes;
        
        double value = 0.;
        int matIdx = 0;

        for(int i = 0; i < len; i++){
            
            DataSource ds = dss[i].getSource();

            Vec pnt1 = new Vec(pnt);
            int res = ds.getDataValue(pnt1, data);

            if(res != ResultCodes.RESULT_OK){
                // outside of domain
                continue;
            }
            double v = data.v[0];
            if(v >= 1.){
                data.v[0] = 1;
                data.materialIndex = i;                
                return ResultCodes.RESULT_OK;
            }
            
            if( v > value) {
                value = v;
                matIdx = i;
            }
        }
        
        data.v[0] = value;
        // store material index 
        data.materialIndex = matIdx;
        
        return ResultCodes.RESULT_OK;
    }

    public int getDistanceData(Vec pnt, Vec data) {

        int len = vShapes.length;
        Shape dss[] = vShapes;
        
        double value = Double.MAX_VALUE;
        int matIdx=0;

        //TODO garbage collecton 
        Vec pnt1 = new Vec(pnt);

        for(int i = 0; i < len; i++){
            
            DataSource ds = dss[i].getSource();
            pnt1.set(pnt);
            ds.getDataValue(pnt1, data);
            double v = data.v[0];

            if (data.v[0] < value) {
                value = data.v[0];
                matIdx = i;
            }
        }
        
        data.v[0] = value;

        // store material index 
        data.materialIndex = dss[matIdx].getMaterialID();

        return ResultCodes.RESULT_OK;
    }   

    /**
     * @noRefGuide
     */
    
    public SNode[] getChildren() {

        List childrenList = mp_shapes.getValue(); 
        SNode[] children = (SNode[])childrenList.toArray(new SNode[childrenList.size()]);
        return children;

    }

} // class Union
