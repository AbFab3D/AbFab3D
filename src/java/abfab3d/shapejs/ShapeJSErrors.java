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

package abfab3d.shapejs;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;



/**
 * A list of error types
 *
 * @author Tony Wong
 */
public class ShapeJSErrors {
	public enum ErrorType {
		UNKNOWN_CRASH,
		PARSING_ERROR,
		EMPTY_SCENE,
		UPDATE_SCENE_ERROR,
		INVALID_PARAMETER_VALUE,
		ONCHANGE_PROPERTY_NOT_FOUND,
		ONCHANGE_FUNCTION_NOT_FOUND,
		MAIN_FUNCTION_NOT_FOUND,
		
		NUM_REGIONS_WARNING,
		CONNECTED_POSITIONS_FAILURE,
		PRINTER_BOUNDS_MIN_FAILURE,
		PRINTER_BOUNDS_MAX_FAILURE,
		PRINTER_BOUNDS_MINSUM_FAILURE;
	}
	
	public static String UNKNOWN_CRASH = "Unexpected crash";
    public static String PARSING_ERROR = "Error parsing script";
    public static String EMPTY_SCENE = "Empty scene";
    public static String UPDATE_SCENE_ERROR = "Error updating scene: {0}";
    public static String INVALID_PARAMETER_VALUE = "Invalid parameter: name {0}, value {1}";
    public static String ONCHANGE_PROPERTY_NOT_FOUND = "Cannot find onChange property for: {0}";
    public static String ONCHANGE_FUNCTION_NOT_FOUND = "Cannot find onChange function: name {0}, onchange {1}";
    public static String MAIN_FUNCTION_NOT_FOUND = "Cannot find function main()";
    
    public static String NUM_REGIONS_WARNING = "Some parts of your design aren't connected, and will be lost during printing.  Adjust your settings to fix.";
	public static String PRINTER_BOUNDS_MIN_FAILURE = "Your design is smaller than the printer bounds.";
	public static String PRINTER_BOUNDS_MAX_FAILURE = "Your design is larger than the printer bounds.";
	public static String PRINTER_BOUNDS_MINSUM_FAILURE = "Your design's bounds do not exceed the minimum required sum.";

    private static final Map<ErrorType, String> errorMsgs = new HashMap<ErrorType, String>();
    static {
    	errorMsgs.put(ErrorType.UNKNOWN_CRASH, UNKNOWN_CRASH);
    	errorMsgs.put(ErrorType.PARSING_ERROR, PARSING_ERROR);
    	errorMsgs.put(ErrorType.EMPTY_SCENE, EMPTY_SCENE);
    	errorMsgs.put(ErrorType.UPDATE_SCENE_ERROR, UPDATE_SCENE_ERROR);
    	errorMsgs.put(ErrorType.INVALID_PARAMETER_VALUE, INVALID_PARAMETER_VALUE);
    	errorMsgs.put(ErrorType.ONCHANGE_PROPERTY_NOT_FOUND, ONCHANGE_PROPERTY_NOT_FOUND);
    	errorMsgs.put(ErrorType.ONCHANGE_FUNCTION_NOT_FOUND, ONCHANGE_FUNCTION_NOT_FOUND);
    	errorMsgs.put(ErrorType.MAIN_FUNCTION_NOT_FOUND, MAIN_FUNCTION_NOT_FOUND);
    	
    	errorMsgs.put(ErrorType.NUM_REGIONS_WARNING, NUM_REGIONS_WARNING);
		errorMsgs.put(ErrorType.PRINTER_BOUNDS_MIN_FAILURE, PRINTER_BOUNDS_MIN_FAILURE);
		errorMsgs.put(ErrorType.PRINTER_BOUNDS_MAX_FAILURE, PRINTER_BOUNDS_MAX_FAILURE);
		errorMsgs.put(ErrorType.PRINTER_BOUNDS_MINSUM_FAILURE, PRINTER_BOUNDS_MINSUM_FAILURE);
    }

    
    public static String getErrorMsg(ErrorType errorType, String[] args) {
    	String msg = errorMsgs.get(errorType);
    	if (msg != null) {
        	MessageFormat msg_fmt = new MessageFormat(msg);
            msg = msg_fmt.format(args);
    	}

        return msg;
    }

}

