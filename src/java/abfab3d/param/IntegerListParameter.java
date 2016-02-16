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

/**
 * A list of Int
 *
 * @author Alan Hudson
 */
public class IntegerListParameter extends ListParameter {
    public IntegerListParameter(String name) {
        this(name, name);
    }

    public IntegerListParameter(String name, String desc) {
        this(name, desc, new ArrayList(), IntParameter.DEFAULT_MIN_RANGE, IntParameter.DEFAULT_MAX_RANGE);
    }

    public IntegerListParameter(String name, String desc, List initialValue,
                            int minRange, int maxRange) {

        super(name, desc);

        if (initialValue == null) initialValue = new ArrayList();
        def = new IntParameter(name,desc,minRange,minRange,maxRange);
        setValue(initialValue);
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.INTEGER_LIST;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (!(val instanceof List)) {
            throw new IllegalArgumentException("Unsupported type for IntList: " + val + " in param: " + getName());
        }
    }

    public void setValue(int[] val) {
        if (value instanceof List) {
            List lval = (List) value;
            lval.clear();
        } else {
            value = new ArrayList<IntParameter>();
        }
        for(int i=0; i < val.length; i++) {
            ((List)value).add(new IntParameter(getName(), getDesc(), val[i], ((IntParameter) def).getMinRange(), ((IntParameter) def).getMaxRange()));
        }
    }

    public int[] getValue(int[] val) {
        int[] ret_val = null;

        int len = ((List)value).size();
        if (val == null || val.length >= len) {
            ret_val = new int[len];
        } else {
            ret_val = val;
        }

        for(int i=0; i < len; i++) {
            ret_val[i] = ((IntParameter)((List)value).get(i)).getValue();
        }

        return ret_val;
    }

    @Override
    public String getParamString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int len = ((List)value).size();
        for(int i=0; i < len; i++) {
            IntParameter ip = (IntParameter) ((List)value).get(i);
            sb.append(ip.getParamString());
            if (i < len -1 ) sb.append(",");
        }
        sb.append(")");

        return sb.toString();
    }
}
