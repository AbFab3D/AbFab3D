/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

// External Imports
import java.util.HashMap;

// Local Imports
// None

/**
 * A holder and manager of global parameters
 * <p>
 *
 * @author Alan Hudson
 */
public class AbFab3DGlobals {
    public static final String MAX_PROCESSOR_COUNT_KEY = "maxProcessorCount";
    public static final int MAX_PROCESSOR_COUNT_DEFAULT = 8;

    /** The singleton class */
    private static AbFab3DGlobals globals;

    /** Map of application parameters name = value */
    private static HashMap<String, Object> params;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private AbFab3DGlobals() {
        params = new HashMap<String, Object>();

        params.put(MAX_PROCESSOR_COUNT_KEY, MAX_PROCESSOR_COUNT_DEFAULT);
    }

    /**
     * Put a value.
     *
     * @param name
     * @param value
     */
    public static void put(String name, Object value) {
        if (globals == null) {
            globals = new AbFab3DGlobals();
        }

        params.put(name, value);
    }

    /**
     * Remove a value.
     *
     * @param name
     */
    public static void remove(String name) {
        if (globals == null) {
            return;
        }

        params.remove(name);
    }

    /**
     * Get a value.
     *
     * @param name
     * @return
     */
    public static Object get(String name) {
        if (globals == null) {
            globals = new AbFab3DGlobals();
        }

        return params.get(name);
    }

    /**
     * Check to see if the named property is defined. Returns
     * true if it is defined, otherwise false.
     *
     * @param name The name of the parameter to check on
     * @return true when the parameter is defined, false otherwise
     */
    public static boolean contains(String name) {
        if (globals == null) {
            globals = new AbFab3DGlobals();
        }

        return params.containsKey(name);
    }

    /**
     * Get all the parameters.
     *
     * @return
     */
    public static HashMap<String, Object> getAll() {
        if (globals == null) {
            globals = new AbFab3DGlobals();
        }

        return params;

    }

	public static int getThreadCount(int count){
		
        if(count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }
        
        int max_threads = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        count = Math.min(count,max_threads);
        return count;
		
	}
	
}
