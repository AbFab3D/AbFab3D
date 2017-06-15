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
package abfab3d.param;

// External Imports

import java.util.ArrayList;
import java.util.List;

import static abfab3d.core.Output.printf;

/**
 * A pointer to a list of nodes
 *
 * @author Alan Hudson
 */
public class SNodeListParameter extends BaseParameter {


    SNodeFactory m_nodeFactory;

    public SNodeListParameter(String name) {

        this(name, name);
    }

    public SNodeListParameter(String name, SNodeFactory nodeFactory) {
        this(name, name, new ArrayList(), nodeFactory);
    }

    public SNodeListParameter(String name, String desc) {

        this(name, desc, new ArrayList(), new BaseSNodeFactory());
    }

    public SNodeListParameter(String name, String desc, SNodeFactory nodeFactory) {

        this(name, desc, new ArrayList(), nodeFactory);
    }

    public SNodeListParameter(String name, String desc, List initialValue, SNodeFactory nodeFactory) {

        super(name, desc);
        m_nodeFactory = nodeFactory;

        ArrayList copy = new ArrayList(initialValue);
        setValue(copy);
    }

    /**
       retursn factory to create new nodes 
     */
    public SNodeFactory getSNodeFactory(){
        return m_nodeFactory;
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.SNODE_LIST;
    }

    public void add(Parameterizable source){
        ((List) value).add(source);
    }

    /**
       set value to be list of single data source
     */
    public void set(Parameterizable node){
        ((List) value).clear();
        ((List) value).add(node);
    }

    /**
       set value at specific index 
     */
    public void set(int index, Parameterizable source){
        ((List) value).set(index, source);
    }

    /**
     get value at specific index
     */
    public Parameterizable get(int index){
        return (Parameterizable) ((List) value).get(index);
    }

    /**
       remove item with given index
     */
    public void remove(int index){
        ((List) value).remove(index);
    }

    public void clear() {
        ((List) value).clear();
    }

    public void setValue(Object val) {
        if(val instanceof List){
            value = val;
        } else {
            List list = new ArrayList();
            list.add(val);
            value = list;
        }

        changed = true;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (!(val instanceof List)) {
            throw new IllegalArgumentException("Unsupported type for SNodeList: " + val + " in param: " + getName());
        }
    }

    public List getValue() {
        return (List) value;
    }

    /**
     * Has the value changed since the last call.  This method will clear the changed state.
     * @return
     */
    public boolean hasChanged() {
        boolean ret_val = changed;
        List list = (List) value;

        int len = list.size();

        for(int i=0; i < len; i++) {
            Object p = list.get(i);

            if (p instanceof Parameterizable) {
                Parameter[] cp = ((Parameterizable)p).getParams();
                int len2 = cp.length;
                for(int j=0; j < len2; j++) {
                    if (cp[j].hasChanged()) {
                        ret_val = true;
                    }
                }
            }
            if (p instanceof Parameter) {
                Parameter val = (Parameter) p;
                if (val.hasChanged()) {
                    ret_val = true;

                    // Don't early out here so all changed fields are cleared at once
                }
            }
        }
        changed = false;
        return ret_val;
    }

    @Override
    public void getParamString(StringBuilder sb) {
        sb.append("[");
        List list = (List) value;

        int len = list.size();

        for(int i=0; i < len; i++) {

            Object p = list.get(i);
            
            if (p instanceof Parameterizable) {
                sb.append(((Parameterizable)p).getParamString());
            } else if (p instanceof SourceWrapper) {
                sb.append(((SourceWrapper) p).getParamString());
            } else if (p instanceof Parameterizable) {
                sb.append(((Parameterizable) p).getParamString());
            } else if (p == null) {
                sb.append("null");
            } else {
                sb.append(p.toString());            
            }
            if (i < len -1) {
                sb.append(",");
            }            
        }
        sb.append("]");
    }

    public String getParamString() {
        StringBuilder sb = new StringBuilder();
        getParamString(sb);
        return sb.toString();
    }

}
