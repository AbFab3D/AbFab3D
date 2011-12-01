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
                    case URI:
                        val = raw_val;
                        break;
                    case DOUBLE:
                        val = parseDouble(p, raw_val);
                        break;
                    case INTEGER:
                        val = parseInteger(p, raw_val);
                        break;
                    case DOUBLE_LIST:
                    	val = parseDoubleList(p, raw_val);
                        break;
                    case INTEGER_LIST:
                    	val = parseIntegerList(p, raw_val);
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
                e.printStackTrace();
                throw new IllegalArgumentException("Error parsing: " + p.getName() + " value: " + raw_val);
            }

            ret_val.put(p.getName(), val);
        }

        return ret_val;
    }
    
    /**
     * Parses a string as a double. Throws and IllegalArgumentException
     * if the double is not between the parameter's min and max rage.
     * 
     * @param param The parameter
     * @param raw_val The string to parse
     * @return A double representation of the input string
     */
    private static double parseDouble(Parameter param, String raw_val) {
    	double val = Double.parseDouble(raw_val);

    	if (val < param.getMinRange() || val > param.getMaxRange()) {
    		throw new IllegalArgumentException();
    	}
    	
    	return val;
    }
    
    /**
     * Parses a string as an int. Throws and IllegalArgumentException
     * if the integer is not between the parameter's min and max rage.
     * 
     * @param param The parameter
     * @param raw_val The string to parse
     * @return An int representation of the input string
     */
    private static int parseInteger(Parameter param, String raw_val) {
    	int val = Integer.parseInt(raw_val);

    	if (val < (int) param.getMinRange() || val > (int) param.getMaxRange()) {
    		throw new IllegalArgumentException();
    	}
    	
    	return val;
    }
    
    /**
     * Parses a string as a double array. Throws and IllegalArgumentException
     * if the array values are not between the parameter's min and max rage.
     * 
     * @param param The parameter
     * @param raw_val The string to parse
     * @return A double array representation of the input string
     */
    private static double[] parseDoubleList(Parameter param, String raw_val) {
        String[] dvals = raw_val.split("_");
        
        double[] val = new double[dvals.length];
        
        for(int i=0; i < dvals.length; i++) {
            val[i] = Double.parseDouble(dvals[i]);
            
        	if (val[i] < param.getMinRange() || val[i] > param.getMaxRange()) {
        		throw new IllegalArgumentException();
        	}
        }

    	return val;
    }
    
    /**
     * Parses a string as a int array. Throws and IllegalArgumentException
     * if the array values are not between the parameter's min and max rage.
     * 
     * @param param The parameter
     * @param raw_val The string to parse
     * @return An int array representation of the input string
     */
    private static int[] parseIntegerList(Parameter param, String raw_val) {
        String[] dvals = raw_val.split("_");
        
        int[] val = new int[dvals.length];
        
        for(int i=0; i < dvals.length; i++) {
            val[i] = Integer.parseInt(dvals[i]);
            
        	if (val[i] < (int) param.getMinRange() || val[i] > (int) param.getMaxRange()) {
        		throw new IllegalArgumentException();
        	}
        }

    	return val;
    }
}
