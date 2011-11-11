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

package abfab3d.creator.util;

// External Imports
import java.util.*;

// Internal Imports
import abfab3d.creator.*;

/**
 * Utilities dealing with parameters.
 *
 * @author Alan Hudson
 */
public class ParameterUtil {
    public static Map<String, Object> parseParams(Map<String,Parameter> defs,
        Map<String,String> vals) {

        Iterator<Parameter> itr = defs.values().iterator();

        HashMap<String, Object>  ret_val = new HashMap<String, Object>();

        while(itr.hasNext()) {
            Parameter p = itr.next();

            String raw_val = vals.get(p.getName());

            if (raw_val == null) {
                raw_val = p.getDefaultValue();
            }

            Object val = null;

            try {
                switch(p.getDataType()) {
                    case STRING:
                        val = raw_val;
                        break;
                    case DOUBLE:
                        val = Double.parseDouble(raw_val);
                        break;
                    case INTEGER:
                        val = Integer.parseInt(raw_val);
                        break;
                    case DOUBLE_LIST:
                        String[] dvals = raw_val.split("_");
                        val = new double[dvals.length];
                        for(int i=0; i < dvals.length; i++) {
                            ((double[]) val)[i] = Double.parseDouble(dvals[i]);
                        }
                        break;
                    case INTEGER_LIST:
                        String[] ivals = raw_val.split("_");
                        val = new int[ivals.length];
                        for(int i=0; i < ivals.length; i++) {
                            ((int[]) val)[i] = Integer.parseInt(ivals[i]);
                        }
                        break;
                    case STRING_LIST:
                        // TODO: Need to handle escaping
                        String[] svals = raw_val.split("_");
                        val = new String[svals.length];
                        for(int i=0; i < svals.length; i++) {
                            ((String[]) val)[i] = svals[i];
                        }
                        break;
                    case BOOLEAN_LIST:
                        String[] bvals = raw_val.split("_");
                        val = new boolean[bvals.length];
                        for(int i=0; i < bvals.length; i++) {
                            ((boolean[]) val)[i] = Boolean.parseBoolean(bvals[i]);
                        }
                        break;
                    case BOOLEAN:
                        val = Boolean.parseBoolean(raw_val);
                        break;
                    case ENUM:
                        val = raw_val;
                        String[] valid_values = p.getEnumValues();

                        if (valid_values == null) {
                            if (!raw_val.equals("")) {
                                throw new IllegalArgumentException("Error Validating " + p.getName() + " invalid enum value: " + raw_val);
                            }
                            break;
                        }
                        boolean valid = false;

                        for(int i=0; i < valid_values.length; i++) {
                            if (raw_val.equals(valid_values[i])) {
                                valid = true;
                                break;
                            }
                        }

                        if (!valid) {
                            throw new IllegalArgumentException("Error Validating " + p.getName() + " invalid enum value: " + raw_val);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled datatype: " + p.getDataType());
                }
            } catch(Exception e) {
                throw new IllegalArgumentException("Error parsing: " + p.getName() + " value: " + raw_val);
            }

            ret_val.put(p.getName(), val);
        }

        return ret_val;
    }
}
